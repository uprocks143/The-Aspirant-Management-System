package com.example.services

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FirebaseService {
    private const val TAG = "FirebaseService"

    var isInitialized = false
        private set

    var auth: FirebaseAuth? = null
        private set

    var db: FirebaseFirestore? = null
        private set

    var storage: FirebaseStorage? = null
        private set

    // Saved Configured Settings for Firebase
    var firebaseApiKey = ""
    var firebaseAppId = ""
    var firebaseProjectId = ""
    var firebaseStorageBucket = ""

    /**
     * Initializes Firebase programmatically. If standard google-services.json is missing,
     * this allows dynamic runtime startup via manual settings or default environment fallbacks.
     */
    fun initialize(
        context: Context,
        apiKey: String = "",
        appId: String = "",
        projectId: String = "",
        storageBucket: String = ""
    ): Boolean {
        if (isInitialized) return true

        try {
            // Respect already initialized application
            val existingApps = FirebaseApp.getApps(context)
            if (existingApps.isNotEmpty()) {
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                storage = FirebaseStorage.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase linked successfully via existing app target.")
                return true
            }

            // Fallback manual configurations
            val key = apiKey.ifEmpty { firebaseApiKey }
            val appIdStr = appId.ifEmpty { firebaseAppId }
            val projectStr = projectId.ifEmpty { firebaseProjectId }
            val bucketStr = storageBucket.ifEmpty { firebaseStorageBucket }

            if (key.isNotEmpty() && appIdStr.isNotEmpty() && projectStr.isNotEmpty()) {
                val builder = FirebaseOptions.Builder()
                    .setApiKey(key)
                    .setApplicationId(appIdStr)
                    .setProjectId(projectStr)

                if (bucketStr.isNotEmpty()) {
                    builder.setStorageBucket(bucketStr)
                }

                FirebaseApp.initializeApp(context.applicationContext, builder.build())
                auth = FirebaseAuth.getInstance()
                db = FirebaseFirestore.getInstance()
                storage = FirebaseStorage.getInstance()
                isInitialized = true
                Log.d(TAG, "Firebase initialized dynamically with manual credentials.")
                return true
            } else {
                Log.w(TAG, "Firebase credentials incomplete. Running in Local Offline Fallback mode.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Programmatic configuration failed: ${e.localizedMessage}")
        }
        return false
    }

    // ==========================================
    // 1. FIREBASE AUTH INTERFACE
    // ==========================================

    suspend fun firebaseSignUp(emailStr: String, passwordStr: String): Boolean {
        if (!isInitialized) return false
        return try {
            auth?.createUserWithEmailAndPassword(emailStr, passwordStr)?.await()
            Log.d(TAG, "Firebase User registered: $emailStr")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-up error: ${e.localizedMessage}")
            throw e
        }
    }

    suspend fun firebaseLogin(emailStr: String, passwordStr: String): Boolean {
        if (!isInitialized) return false
        return try {
            auth?.signInWithEmailAndPassword(emailStr, passwordStr)?.await()
            Log.d(TAG, "Firebase User signed in: $emailStr")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase sign-in error: ${e.localizedMessage}")
            throw e
        }
    }

    fun firebaseSignOut() {
        if (isInitialized) {
            auth?.signOut()
        }
    }

    // ==========================================
    // 2. FIRESTORE COLLECTIONS & DATA SCHEMAS
    // ==========================================

    /**
     * Upload class batches to Firestore col "batches"
     */
    suspend fun syncBatchToCloud(batch: Batch) {
        if (!isInitialized) return
        try {
            val batchMap = hashMapOf(
                "id" to batch.id,
                "name" to batch.name,
                "subject" to batch.subject,
                "classTimings" to batch.classTimings,
                "feesAmount" to batch.feesAmount,
                "daysOfWeek" to batch.daysOfWeek,
                "isActive" to batch.isActive,
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("batches")?.document(batch.id.toString())?.set(batchMap)?.await()
            Log.d(TAG, "Batch ${batch.name} synced to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing batch to cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Upload student records to Firestore col "students"
     */
    suspend fun syncStudentToCloud(student: Student) {
        if (!isInitialized) return
        try {
            val studentMap = hashMapOf(
                "id" to student.id,
                "batchId" to student.batchId,
                "name" to student.name,
                "address" to student.address,
                "phone" to student.phone,
                "parentPhone" to student.parentPhone,
                "startDate" to student.startDate,
                "isAlumni" to student.isAlumni,
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("students")?.document(student.id.toString())?.set(studentMap)?.await()
            Log.d(TAG, "Student ${student.name} synced to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing student to cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Upload attendance entries to Firestore col "attendance"
     */
    suspend fun syncAttendanceToCloud(attendance: Attendance) {
        if (!isInitialized) return
        try {
            val attMap = hashMapOf(
                "id" to attendance.id,
                "studentId" to attendance.studentId,
                "batchId" to attendance.batchId,
                "dateString" to attendance.dateString,
                "status" to attendance.status,
                "trackingMethod" to attendance.trackingMethod,
                "checkInTime" to attendance.checkInTime,
                "lastSynced" to System.currentTimeMillis()
            )
            val docId = "${attendance.studentId}_${attendance.dateString}"
            db?.collection("attendance")?.document(docId)?.set(attMap)?.await()
            Log.d(TAG, "Attendance for student ID ${attendance.studentId} on ${attendance.dateString} synced.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing attendance: ${e.localizedMessage}")
        }
    }

    /**
     * Upload fee payments registries to Firestore col "fees"
     */
    suspend fun syncFeePaymentToCloud(payment: FeePayment) {
        if (!isInitialized) return
        try {
            val payMap = hashMapOf(
                "id" to payment.id,
                "studentId" to payment.studentId,
                "feeType" to payment.feeType,
                "amountPaid" to payment.amountPaid,
                "datePaid" to payment.datePaid,
                "receiptNo" to payment.receiptNo,
                "monthPaidFor" to payment.monthPaidFor,
                "paymentMode" to payment.paymentMode,
                "remarks" to payment.remarks,
                "transactionStatus" to payment.transactionStatus,
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("fees")?.document(payment.id.toString())?.set(payMap)?.await()
            Log.d(TAG, "FeePayment transaction #${payment.receiptNo} synced.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing fee payment: ${e.localizedMessage}")
        }
    }

    /**
     * Store and synchronize teacher / institute UPI IDs to Firestore col "settings"
     */
    suspend fun syncTutorUpiToCloud(username: String, upiId: String) {
        if (!isInitialized) return
        try {
            val upiMap = hashMapOf(
                "tutorUsername" to username,
                "upiId" to upiId,
                "updatedAt" to System.currentTimeMillis()
            )
            db?.collection("settings")?.document(username)?.set(upiMap)?.await()
            Log.d(TAG, "Tutor UPI ID $upiId recorded on Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing tutor UPI ID: ${e.localizedMessage}")
        }
    }

    /**
     * Retrieve the recorded UPI ID for a specific teacher/institute
     */
    suspend fun fetchTutorUpiFromCloud(username: String): String? {
        if (!isInitialized) return null
        return try {
            val doc = db?.collection("settings")?.document(username)?.get()?.await()
            doc?.getString("upiId")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading UPI ID from cloud: ${e.localizedMessage}")
            null
        }
    }


    // ==========================================
    // 3. SECURE BULK CLOUD BACKUP & RESTORATION
    // ==========================================

    /**
     * Serializes complete Room database tables as an encrypted or plain JSON formatted String file,
     * and uploads it to Firebase Cloud Storage under user-specific backup folders.
     */
    suspend fun uploadBackupSnapshotToCloud(
        username: String,
        batches: List<Batch>,
        students: List<Student>,
        payments: List<FeePayment>,
        transactions: List<FinancialTransaction>,
        attendanceList: List<Attendance>
    ): String {
        if (!isInitialized) {
            throw IllegalStateException("Firebase service is not initialized yet.")
        }
        val storageRef = storage?.reference ?: throw IllegalStateException("Storage service is unavailable.")

        try {
            // Build absolute snapshot JSON
            val rootObj = JSONObject()
            rootObj.put("version", 1)
            rootObj.put("timestamp", System.currentTimeMillis())
            rootObj.put("username", username)

            // Batches Array
            val batchesArr = JSONArray()
            batches.forEach {
                val item = JSONObject()
                item.put("id", it.id)
                item.put("name", it.name)
                item.put("subject", it.subject)
                item.put("classTimings", it.classTimings)
                item.put("feesAmount", it.feesAmount)
                item.put("daysOfWeek", it.daysOfWeek)
                batchesArr.put(item)
            }
            rootObj.put("batches", batchesArr)

            // Students Array
            val studentsArr = JSONArray()
            students.forEach {
                val item = JSONObject()
                item.put("id", it.id)
                item.put("batchId", it.batchId)
                item.put("name", it.name)
                item.put("address", it.address)
                item.put("phone", it.phone)
                item.put("parentPhone", it.parentPhone)
                item.put("startDate", it.startDate)
                item.put("rollNumber", it.rollNumber)
                item.put("parentName", it.parentName)
                item.put("dateOfBirth", it.dateOfBirth)
                item.put("isAlumni", it.isAlumni)
                studentsArr.put(item)
            }
            rootObj.put("students", studentsArr)

            // Payments Array
            val paymentsArr = JSONArray()
            payments.forEach {
                val item = JSONObject()
                item.put("id", it.id)
                item.put("studentId", it.studentId)
                item.put("feeType", it.feeType)
                item.put("amountPaid", it.amountPaid)
                item.put("discount", it.discount)
                item.put("datePaid", it.datePaid)
                item.put("receiptNo", it.receiptNo)
                item.put("monthPaidFor", it.monthPaidFor)
                item.put("paymentMode", it.paymentMode)
                item.put("remarks", it.remarks)
                item.put("transactionStatus", it.transactionStatus)
                paymentsArr.put(item)
            }
            rootObj.put("fee_payments", paymentsArr)

            // Transactions Array
            val transArr = JSONArray()
            transactions.forEach {
                val item = JSONObject()
                item.put("id", it.id)
                item.put("type", it.type)
                item.put("amount", it.amount)
                item.put("category", it.category)
                item.put("description", it.description)
                item.put("date", it.date)
                transArr.put(item)
            }
            rootObj.put("transactions", transArr)

            // Attendance Array
            val attArr = JSONArray()
            attendanceList.forEach {
                val item = JSONObject()
                item.put("id", it.id)
                item.put("studentId", it.studentId)
                item.put("batchId", it.batchId)
                item.put("dateString", it.dateString)
                item.put("status", it.status)
                item.put("trackingMethod", it.trackingMethod)
                item.put("checkInTime", it.checkInTime)
                attArr.put(item)
            }
            rootObj.put("attendance", attArr)

            val jsonPayload = rootObj.toString(2)
            val ipStream = ByteArrayInputStream(jsonPayload.toByteArray(Charsets.UTF_8))

            // Choose file name based on timing and user name
            val fileName = "backups/${username}_snapshot_${System.currentTimeMillis()}.json"
            val fileRef = storageRef.child(fileName)

            fileRef.putStream(ipStream).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Log.d(TAG, "Backup successfully written to Firebase Storage link: $downloadUrl")
            return downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Cloud storage backup failed: ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Resolves the latest available backup json payload file from secure Firebase Storage,
     * downloaded locally and reconstructed as structured tables.
     */
    suspend fun downloadBackupSnapshotFromCloud(
        username: String,
        onRestoreRestored: (
            batches: List<Batch>,
            students: List<Student>,
            payments: List<FeePayment>,
            transactions: List<FinancialTransaction>,
            attendanceList: List<Attendance>
        ) -> Unit
    ) {
        if (!isInitialized) {
            throw IllegalStateException("Firebase is offline.")
        }
        val storageRef = storage?.reference ?: throw IllegalStateException("Storage is down.")

        try {
            // Locate backups directory list
            val listResult = storageRef.child("backups").listAll().await()
            // Filter backups created by this user and choose the latest snapshot
            val matchingRef = listResult.items
                .filter { it.name.startsWith(username) }
                .maxByOrNull { it.name }
                ?: throw NoSuchElementException("No existing cloud backups found for user account: $username")

            // Download file payload bytes
            val maxBytes: Long = 5 * 1024 * 1024 // 5MB limit
            val byteData = matchingRef.getBytes(maxBytes).await()
            val jsonStr = String(byteData, Charsets.UTF_8)
            val rootObj = JSONObject(jsonStr)

            // Deserialize Batches
            val batchesList = mutableListOf<Batch>()
            val batchesArr = rootObj.optJSONArray("batches")
            if (batchesArr != null) {
                for (i in 0 until batchesArr.length()) {
                    val o = batchesArr.getJSONObject(i)
                    batchesList.add(
                        Batch(
                            id = o.optLong("id", 0),
                            name = o.getString("name"),
                            subject = o.optString("subject", "Mathematics"),
                            classTimings = o.optString("classTimings", o.optString("time", "04:00 PM - 05:00 PM")),
                            feesAmount = o.optDouble("feesAmount", 1000.0),
                            daysOfWeek = o.optString("daysOfWeek", "Mon, Wed, Fri")
                        )
                    )
                }
            }

            // Deserialize Students
            val studentsList = mutableListOf<Student>()
            val studentsArr = rootObj.optJSONArray("students")
            if (studentsArr != null) {
                for (i in 0 until studentsArr.length()) {
                    val o = studentsArr.getJSONObject(i)
                    studentsList.add(
                        Student(
                            id = o.optLong("id", 0),
                            batchId = o.getLong("batchId"),
                            name = o.getString("name"),
                            address = o.optString("address", ""),
                            phone = o.getString("phone"),
                            parentPhone = o.optString("parentPhone", ""),
                            startDate = o.optString("startDate", ""),
                            rollNumber = o.optString("rollNumber", "R-${o.optLong("id", 0)}"),
                            parentName = o.optString("parentName", "Guardian"),
                            dateOfBirth = o.optString("dateOfBirth", "2000-01-01"),
                            isAlumni = o.optBoolean("isAlumni", false)
                        )
                    )
                }
            }

            // Deserialize Payments
            val paymentsList = mutableListOf<FeePayment>()
            val paymentsArr = rootObj.optJSONArray("fee_payments")
            if (paymentsArr != null) {
                for (i in 0 until paymentsArr.length()) {
                    val o = paymentsArr.getJSONObject(i)
                    paymentsList.add(
                        FeePayment(
                            id = o.optLong("id", 0),
                            studentId = o.getLong("studentId"),
                            feeType = o.optString("feeType", "Monthly-based"),
                            amountPaid = o.getDouble("amountPaid"),
                            discount = o.optDouble("discount", 0.0),
                            datePaid = o.optLong("datePaid", System.currentTimeMillis()),
                            receiptNo = o.getString("receiptNo"),
                            monthPaidFor = o.getString("monthPaidFor"),
                            paymentMode = o.optString("paymentMode", "CASH"),
                            remarks = o.optString("remarks", ""),
                            transactionStatus = o.optString("transactionStatus", "SUCCESS")
                        )
                    )
                }
            }

            // Deserialize Transactions
            val transList = mutableListOf<FinancialTransaction>()
            val transArr = rootObj.optJSONArray("transactions")
            if (transArr != null) {
                for (i in 0 until transArr.length()) {
                    val o = transArr.getJSONObject(i)
                    transList.add(
                        FinancialTransaction(
                            id = o.optLong("id", 0),
                            type = o.getString("type"),
                            amount = o.getDouble("amount"),
                            category = o.getString("category"),
                            description = o.getString("description"),
                            date = o.optLong("date", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Deserialize Attendance
            val attList = mutableListOf<Attendance>()
            val attArr = rootObj.optJSONArray("attendance")
            if (attArr != null) {
                for (i in 0 until attArr.length()) {
                    val o = attArr.getJSONObject(i)
                    attList.add(
                        Attendance(
                            id = o.optLong("id", 0),
                            studentId = o.getLong("studentId"),
                            batchId = o.getLong("batchId"),
                            dateString = o.getString("dateString"),
                            status = o.optString("status", o.optString("isPresent", "Present")),
                            trackingMethod = o.optString("trackingMethod", "Manual"),
                            checkInTime = o.optString("checkInTime", "")
                        )
                    )
                }
            }

            Log.d(TAG, "Backup successfully downloaded and parsed from cloud bucket!")
            onRestoreRestored(batchesList, studentsList, paymentsList, transList, attList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading snapshot database: ${e.localizedMessage}")
            throw e
        }
    }
}
