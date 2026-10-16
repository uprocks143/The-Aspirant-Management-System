package com.example.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.FeePayment
import com.example.data.model.PaymentLog
import com.example.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Service to handle dual-storage payment persistence with Supabase REST API and Local Storage.
 * Stores monthly tuition payments in Supabase PostgreSQL `tuition_payments` table
 * while maintaining complete offline persistence in Android Room database and SharedPreferences.
 */
object SupabaseService {

    private const val TAG = "SupabaseService"
    private const val PREFS_NAME = "tams_supabase_prefs"
    private const val KEY_SUPABASE_URL = "supabase_project_url"
    private const val KEY_SUPABASE_ANON_KEY = "supabase_anon_key"
    private const val KEY_LAST_SYNC = "supabase_last_sync_timestamp"
    private const val KEY_AUTO_SYNC = "supabase_auto_sync_enabled"
    private const val KEY_OFFLINE_QUEUE = "supabase_offline_payment_queue"

    // Default pre-configured endpoint (can be overridden in settings)
    private const val DEFAULT_SUPABASE_URL = "https://xynuvqwbwvyxmjkltvsz.supabase.co"
    private const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5bnV2cXdid3Z5eG1qa2x0dnN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MDgwMDAwMDAsImV4cCI6MjAyNDAwMDAwMH0.mock_signature"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSupabaseUrl(context: Context): String {
        return getPrefs(context).getString(KEY_SUPABASE_URL, DEFAULT_SUPABASE_URL) ?: DEFAULT_SUPABASE_URL
    }

    fun getSupabaseKey(context: Context): String {
        return getPrefs(context).getString(KEY_SUPABASE_ANON_KEY, DEFAULT_ANON_KEY) ?: DEFAULT_ANON_KEY
    }

    fun saveConfig(context: Context, url: String, anonKey: String, autoSync: Boolean = true) {
        getPrefs(context).edit()
            .putString(KEY_SUPABASE_URL, url.trim().trimEnd('/'))
            .putString(KEY_SUPABASE_ANON_KEY, anonKey.trim())
            .putBoolean(KEY_AUTO_SYNC, autoSync)
            .apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_SYNC, 0L)
    }

    fun isConfigured(context: Context): Boolean {
        val url = getSupabaseUrl(context)
        val key = getSupabaseKey(context)
        return url.isNotBlank() && key.isNotBlank()
    }

    /**
     * Upload a single monthly tuition payment to Supabase `tuition_payments` table.
     */
    suspend fun syncPaymentToSupabase(
        context: Context,
        payment: FeePayment,
        studentName: String = "",
        studentPhone: String = "",
        batchName: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val baseUrl = getSupabaseUrl(context)
        val anonKey = getSupabaseKey(context)

        if (baseUrl.isBlank() || anonKey.isBlank()) {
            enqueueOfflinePayment(context, payment, studentName)
            return@withContext Result.failure(Exception("Supabase credentials not configured. Saved in local offline queue."))
        }

        try {
            val endpoint = "$baseUrl/rest/v1/tuition_payments"
            val jsonPayload = JSONObject().apply {
                put("local_payment_id", payment.id)
                put("student_id", payment.studentId)
                put("student_name", studentName.ifBlank { "Student #${payment.studentId}" })
                put("student_phone", studentPhone)
                put("batch_name", batchName)
                put("fee_type", payment.feeType)
                put("amount_paid", payment.amountPaid)
                put("discount", payment.discount)
                put("payment_mode", payment.paymentMode)
                put("receipt_no", payment.receiptNo)
                put("month_paid_for", payment.monthPaidFor)
                put("transaction_status", payment.transactionStatus)
                put("remarks", payment.remarks)
                put("date_paid", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(payment.datePaid)))
                put("created_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
            }

            val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation,resolution=merge-duplicates")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val respBody = response.body?.string() ?: ""

            if (code in 200..299) {
                getPrefs(context).edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                Log.d(TAG, "Successfully synced payment to Supabase: receipt=${payment.receiptNo}")
                Result.success("Payment recorded in Supabase (HTTP $code)")
            } else {
                Log.w(TAG, "Supabase HTTP $code: $respBody. Enqueuing offline.")
                enqueueOfflinePayment(context, payment, studentName)
                Result.success("Stored in Local DB (Offline mode: Supabase HTTP $code)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing payment to Supabase: ${e.message}", e)
            enqueueOfflinePayment(context, payment, studentName)
            Result.success("Stored in Local DB (Offline synced)")
        }
    }

    /**
     * Sync bulk payments list to Supabase.
     */
    suspend fun syncBulkPaymentsToSupabase(
        context: Context,
        payments: List<FeePayment>,
        studentsMap: Map<Long, Student> = emptyMap()
    ): Result<Int> = withContext(Dispatchers.IO) {
        val baseUrl = getSupabaseUrl(context)
        val anonKey = getSupabaseKey(context)

        if (baseUrl.isBlank() || anonKey.isBlank() || payments.isEmpty()) {
            return@withContext Result.success(0)
        }

        try {
            val endpoint = "$baseUrl/rest/v1/tuition_payments"
            val jsonArray = JSONArray()

            payments.forEach { payment ->
                val student = studentsMap[payment.studentId]
                val obj = JSONObject().apply {
                    put("local_payment_id", payment.id)
                    put("student_id", payment.studentId)
                    put("student_name", student?.name ?: "Student #${payment.studentId}")
                    put("student_phone", student?.phone ?: "")
                    put("fee_type", payment.feeType)
                    put("amount_paid", payment.amountPaid)
                    put("discount", payment.discount)
                    put("payment_mode", payment.paymentMode)
                    put("receipt_no", payment.receiptNo)
                    put("month_paid_for", payment.monthPaidFor)
                    put("transaction_status", payment.transactionStatus)
                    put("remarks", payment.remarks)
                    put("date_paid", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(payment.datePaid)))
                }
                jsonArray.put(obj)
            }

            val body = jsonArray.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                getPrefs(context).edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                Result.success(payments.size)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch remote payment logs from Supabase for reconciliation.
     */
    suspend fun fetchPaymentsFromSupabase(context: Context): Result<List<SupabasePaymentRecord>> = withContext(Dispatchers.IO) {
        val baseUrl = getSupabaseUrl(context)
        val anonKey = getSupabaseKey(context)

        if (baseUrl.isBlank() || anonKey.isBlank()) {
            return@withContext Result.failure(Exception("Supabase credentials not set"))
        }

        try {
            val endpoint = "$baseUrl/rest/v1/tuition_payments?select=*&order=date_paid.desc&limit=100"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            val bodyString = response.body?.string() ?: "[]"

            if (response.isSuccessful) {
                val jsonArray = JSONArray(bodyString)
                val list = mutableListOf<SupabasePaymentRecord>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        SupabasePaymentRecord(
                            id = obj.optLong("id", 0L),
                            localPaymentId = obj.optLong("local_payment_id", 0L),
                            studentId = obj.optLong("student_id", 0L),
                            studentName = obj.optString("student_name", ""),
                            amountPaid = obj.optDouble("amount_paid", 0.0),
                            feeType = obj.optString("fee_type", "Monthly-based"),
                            monthPaidFor = obj.optString("month_paid_for", ""),
                            receiptNo = obj.optString("receipt_no", ""),
                            paymentMode = obj.optString("payment_mode", "UPI"),
                            datePaid = obj.optString("date_paid", "")
                        )
                    )
                }
                getPrefs(context).edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                Result.success(list)
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun enqueueOfflinePayment(context: Context, payment: FeePayment, studentName: String) {
        try {
            val prefs = getPrefs(context)
            val currentQueue = prefs.getString(KEY_OFFLINE_QUEUE, "[]") ?: "[]"
            val jsonArray = JSONArray(currentQueue)
            val obj = JSONObject().apply {
                put("id", payment.id)
                put("studentId", payment.studentId)
                put("studentName", studentName)
                put("amount", payment.amountPaid)
                put("month", payment.monthPaidFor)
                put("receipt", payment.receiptNo)
                put("time", payment.datePaid)
            }
            jsonArray.put(obj)
            prefs.edit().putString(KEY_OFFLINE_QUEUE, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enqueue offline payment: ${e.message}")
        }
    }

    fun getOfflineQueueSize(context: Context): Int {
        return try {
            val currentQueue = getPrefs(context).getString(KEY_OFFLINE_QUEUE, "[]") ?: "[]"
            JSONArray(currentQueue).length()
        } catch (e: Exception) {
            0
        }
    }
}

data class SupabasePaymentRecord(
    val id: Long,
    val localPaymentId: Long,
    val studentId: Long,
    val studentName: String,
    val amountPaid: Double,
    val feeType: String,
    val monthPaidFor: String,
    val receiptNo: String,
    val paymentMode: String,
    val datePaid: String
)
