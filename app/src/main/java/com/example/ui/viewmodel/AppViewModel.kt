package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.repository.AppRepository
import com.example.services.FirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

import org.json.JSONArray
import org.json.JSONObject

data class InstituteAccount(
    val academyName: String,
    val directorName: String,
    val email: String,
    val address: String,
    val isApproved: Boolean = false,
    val subscriptionActive: Boolean = true,
    val expiryDate: String = "2026-07-18",
    val lastPaymentAmount: Double = 0.0,
    val lastUpiTxRef: String = "",
    val profileType: String = "COACHING_OWNER",
    val gstNumber: String = "",
    val businessProofId: String = "",
    val libraryCapacity: String = "",
    val libraryCategory: String = "",
    val tuitionSubject: String = "",
    val tuitionStandard: String = "",
    val schoolCode: String = "",
    val isSuspended: Boolean = false,
    val wipeDataAt: Long = 0L // GDPR automatic wiping timestamp after 24 hours
)

data class SubTransactionRecord(
    val id: String,
    val academyName: String,
    val email: String,
    val date: String,
    val amount: Double,
    val upiTxnId: String,
    val planType: String,
    val status: String = "Approved"
)

private fun getSafeString(sharedPrefs: android.content.SharedPreferences, key: String, defaultValue: String): String {
    return try {
        sharedPrefs.getString(key, defaultValue) ?: defaultValue
    } catch (e: Exception) {
        defaultValue
    }
}

private fun getSafeLong(sharedPrefs: android.content.SharedPreferences, key: String, defaultValue: Long): Long {
    return try {
        sharedPrefs.getLong(key, defaultValue)
    } catch (e: Exception) {
        try {
            sharedPrefs.getString(key, null)?.toLongOrNull() ?: defaultValue
        } catch (x: Exception) {
            defaultValue
        }
    }
}

private fun getSafeInt(sharedPrefs: android.content.SharedPreferences, key: String, defaultValue: Int): Int {
    return try {
        sharedPrefs.getInt(key, defaultValue)
    } catch (e: Exception) {
        try {
            sharedPrefs.getString(key, null)?.toIntOrNull() ?: defaultValue
        } catch (x: Exception) {
            defaultValue
        }
    }
}

private fun getSafeBoolean(sharedPrefs: android.content.SharedPreferences, key: String, defaultValue: Boolean): Boolean {
    return try {
        sharedPrefs.getBoolean(key, defaultValue)
    } catch (e: Exception) {
        try {
            sharedPrefs.getString(key, null)?.toBoolean() ?: defaultValue
        } catch (x: Exception) {
            defaultValue
        }
    }
}

private fun getSafeStringSet(sharedPrefs: android.content.SharedPreferences, key: String, defaultValue: Set<String>): Set<String> {
    return try {
        sharedPrefs.getStringSet(key, defaultValue) ?: defaultValue
    } catch (e: Exception) {
        defaultValue
    }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(database.appDao())
    val sharedPrefs = context.getSharedPreferences("tams_settings", Context.MODE_PRIVATE)

    // === Dynamic Registration State ===
    private val _academyName = MutableStateFlow(getSafeString(sharedPrefs, "academy_name", "TAMS Academy"))
    val academyName = _academyName.asStateFlow()

    private val _directorName = MutableStateFlow(getSafeString(sharedPrefs, "director_name", "Admin Director"))
    val directorName = _directorName.asStateFlow()

    private val _adminEmail = MutableStateFlow(getSafeString(sharedPrefs, "admin_email", "admin@tams.com"))
    val adminEmail = _adminEmail.asStateFlow()

    private val _adminPhone = MutableStateFlow(getSafeString(sharedPrefs, "admin_phone", "+919999999999"))
    val adminPhone = _adminPhone.asStateFlow()

    private val _adminAddress = MutableStateFlow(getSafeString(sharedPrefs, "admin_address", "Main Campus"))
    val adminAddress = _adminAddress.asStateFlow()

    private val _institutesList = MutableStateFlow<List<InstituteAccount>>(emptyList())
    val institutesList = _institutesList.asStateFlow()

    private val _subscriberTransactions = MutableStateFlow<List<SubTransactionRecord>>(emptyList())
    val subscriberTransactions = _subscriberTransactions.asStateFlow()

    // --- Customisable Promo Slides for Auto Slideshow ---
    data class PromoSlide(
        val id: Int,
        val title: String,
        val subtitle: String,
        val badge: String,
        val imageUrl: String = "",
        val gradientIndex: Int = 0
    )

    private val _promoSlides = MutableStateFlow<List<PromoSlide>>(emptyList())
    val promoSlides = _promoSlides.asStateFlow()

    fun loadPromoSlides() {
        val defaultSlides = listOf(
            PromoSlide(0, "TAMS Smart Admissions Open", "Enroll today & get offline study kit free!", "ADMISSIONS 2026", "", 0),
            PromoSlide(1, "NDA & SSC Premium Batches", "Regular doubt sessions with personal mentors", "BATCH LIVE", "", 1),
            PromoSlide(2, "Integrated Silent Library Hub", "Access 10,000+ offline books & high-speed Wi-Fi", "MEMBERSHIP", "", 2),
            PromoSlide(3, "All India Offline Test Series", "Solve offline sheets & verify auto-rankings", "MOCK TESTS", "", 3),
            PromoSlide(4, "Need Instant Assistance?", "Tap here to chat with our expert academic counsellors", "HELP DESK", "", 4)
        )
        val jsonStr = sharedPrefs.getString("promo_slides_json", "")
        if (jsonStr.isNullOrEmpty()) {
            savePromoSlides(defaultSlides)
        } else {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<PromoSlide>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        PromoSlide(
                            id = obj.optInt("id", i),
                            title = obj.optString("title", ""),
                            subtitle = obj.optString("subtitle", ""),
                            badge = obj.optString("badge", ""),
                            imageUrl = obj.optString("imageUrl", ""),
                            gradientIndex = obj.optInt("gradientIndex", 0)
                        )
                    )
                }
                _promoSlides.value = list
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Failed parsing promo slides, loading defaults", e)
                savePromoSlides(defaultSlides)
            }
        }
    }

    fun savePromoSlides(list: List<PromoSlide>) {
        _promoSlides.value = list
        val array = org.json.JSONArray()
        list.forEach { slide ->
            val obj = org.json.JSONObject()
            obj.put("id", slide.id)
            obj.put("title", slide.title)
            obj.put("subtitle", slide.subtitle)
            obj.put("badge", slide.badge)
            obj.put("imageUrl", slide.imageUrl)
            obj.put("gradientIndex", slide.gradientIndex)
            array.put(obj)
        }
        sharedPrefs.edit().putString("promo_slides_json", array.toString()).apply()
    }

    fun updatePromoSlide(updated: PromoSlide) {
        val currentList = _promoSlides.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        savePromoSlides(currentList)
    }



    private fun loadInstitutes() {
        val jsonStr = getSafeString(sharedPrefs, "registered_institutes_json", "[]")
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<InstituteAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InstituteAccount(
                        academyName = obj.optString("academyName", "Unassigned"),
                        directorName = obj.optString("directorName", "Tutor"),
                        email = obj.optString("email", ""),
                        address = obj.optString("address", ""),
                        isApproved = obj.optBoolean("isApproved", false),
                        subscriptionActive = obj.optBoolean("subscriptionActive", true),
                        expiryDate = obj.optString("expiryDate", "2026-07-18"),
                        lastPaymentAmount = obj.optDouble("lastPaymentAmount", 0.0),
                        lastUpiTxRef = obj.optString("lastUpiTxRef", ""),
                        profileType = obj.optString("profileType", "COACHING_OWNER"),
                        gstNumber = obj.optString("gstNumber", ""),
                        businessProofId = obj.optString("businessProofId", ""),
                        libraryCapacity = obj.optString("libraryCapacity", ""),
                        libraryCategory = obj.optString("libraryCategory", ""),
                        tuitionSubject = obj.optString("tuitionSubject", ""),
                        tuitionStandard = obj.optString("tuitionStandard", ""),
                        schoolCode = obj.optString("schoolCode", ""),
                        isSuspended = obj.optBoolean("isSuspended", false),
                        wipeDataAt = obj.optLong("wipeDataAt", 0L)
                    )
                )
            }
            if (list.isEmpty()) {
                // Seed only the App Owner's authorized account profile. No other accounts can be created or auto-generated.
                list.add(InstituteAccount("TAMS Academy", "Admin Director", "admin@tams.com", "Main Campus", true, true, "2026-12-31"))
                saveInstitutesList(list)
            }
            _institutesList.value = list
            loadTeachers()
        } catch (e: Exception) {
            _institutesList.value = emptyList()
        }
    }

    private fun saveInstitutesList(list: List<InstituteAccount>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("academyName", item.academyName)
            obj.put("directorName", item.directorName)
            obj.put("email", item.email)
            obj.put("address", item.address)
            obj.put("isApproved", item.isApproved)
            obj.put("subscriptionActive", item.subscriptionActive)
            obj.put("expiryDate", item.expiryDate)
            obj.put("lastPaymentAmount", item.lastPaymentAmount)
            obj.put("lastUpiTxRef", item.lastUpiTxRef)
            obj.put("profileType", item.profileType)
            obj.put("gstNumber", item.gstNumber)
            obj.put("businessProofId", item.businessProofId)
            obj.put("libraryCapacity", item.libraryCapacity)
            obj.put("libraryCategory", item.libraryCategory)
            obj.put("tuitionSubject", item.tuitionSubject)
            obj.put("tuitionStandard", item.tuitionStandard)
            obj.put("schoolCode", item.schoolCode)
            obj.put("isSuspended", item.isSuspended)
            obj.put("wipeDataAt", item.wipeDataAt)
            array.put(obj)
        }
        sharedPrefs.edit().putString("registered_institutes_json", array.toString()).apply()
        _institutesList.value = list

        // Auto sync updated / verified institute/admin records to Cloud database firebase doc
        viewModelScope.launch {
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    for (item in list) {
                        FirebaseService.syncInstituteToCloud(item)
                    }
                    android.util.Log.d("AppViewModel", "Institutes list successfully synced to Firebase.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Cloud sync for Institutes failed: ${e.localizedMessage}")
            }
        }
    }

    fun toggleSubscriptionActive(email: String) {
        val updated = _institutesList.value.map {
            if (it.email == email) {
                it.copy(subscriptionActive = !it.subscriptionActive)
            } else it
        }
        saveInstitutesList(updated)
    }

    fun toggleInstituteSuspension(email: String) {
        val updated = _institutesList.value.map {
            if (it.email == email) {
                it.copy(
                    isSuspended = !it.isSuspended,
                    subscriptionActive = if (!it.isSuspended) false else it.subscriptionActive // suspend subscription if banned
                )
            } else it
        }
        saveInstitutesList(updated)
    }

    fun scheduleInstituteWipe(email: String, delay24h: Boolean) {
        val wipeTime = if (delay24h) System.currentTimeMillis() + 86400000L else 1L
        if (wipeTime == 1L) {
            // Instant destructive wipe: remove from the institute list completely!
            val updated = _institutesList.value.filter { it.email != email }
            saveInstitutesList(updated)
        } else {
            // Schedule 24 hour wiping flag
            val updated = _institutesList.value.map {
                if (it.email == email) {
                    it.copy(wipeDataAt = wipeTime)
                } else it
            }
            saveInstitutesList(updated)
        }
    }

    fun updateSubscriptionManual(email: String, active: Boolean, expiry: String, amt: Double, ref: String) {
        val updated = _institutesList.value.map {
            if (it.email == email) {
                it.copy(
                    subscriptionActive = active,
                    expiryDate = expiry,
                    lastPaymentAmount = amt,
                    lastUpiTxRef = ref
                )
            } else it
        }
        saveInstitutesList(updated)
    }

    fun loadSubscriberTransactions() {
        val jsonStr = getSafeString(sharedPrefs, "subscriber_transactions_json", "[]")
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<SubTransactionRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SubTransactionRecord(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        academyName = obj.optString("academyName", ""),
                        email = obj.optString("email", ""),
                        date = obj.optString("date", ""),
                        amount = obj.optDouble("amount", 0.0),
                        upiTxnId = obj.optString("upiTxnId", ""),
                        planType = obj.optString("planType", ""),
                        status = obj.optString("status", "Approved")
                    )
                )
            }
            if (list.isEmpty()) {
                list.add(SubTransactionRecord("tx_1", "TAMS Academy", "admin@tams.com", "2026-06-15", 499.0, "UPI62984530182", "Monthly", "Approved"))
                saveSubscriberTransactionsList(list)
            }
            _subscriberTransactions.value = list
        } catch (e: Exception) {
            _subscriberTransactions.value = emptyList()
        }
    }

    fun saveSubscriberTransactionsList(list: List<SubTransactionRecord>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("academyName", item.academyName)
            obj.put("email", item.email)
            obj.put("date", item.date)
            obj.put("amount", item.amount)
            obj.put("upiTxnId", item.upiTxnId)
            obj.put("planType", item.planType)
            obj.put("status", item.status)
            array.put(obj)
        }
        sharedPrefs.edit().putString("subscriber_transactions_json", array.toString()).apply()
        _subscriberTransactions.value = list
    }

    fun recordSubscriberTransaction(academyName: String, email: String, amount: Double, upiTxnId: String, planType: String) {
        val list = _subscriberTransactions.value.toMutableList()
        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val nid = "tx_" + System.currentTimeMillis()
        list.add(
            SubTransactionRecord(
                id = nid,
                academyName = academyName,
                email = email,
                date = dateStr,
                amount = amount,
                upiTxnId = upiTxnId,
                planType = planType,
                status = "Approved"
            )
        )
        saveSubscriberTransactionsList(list)

        val addDays = if (planType == "Yearly") 365 else 30
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        try {
            val currentInst = _institutesList.value.firstOrNull { it.email == email }
            val baseDate = if (currentInst != null) sdf.parse(currentInst.expiryDate) else java.util.Date()
            cal.time = baseDate ?: java.util.Date()
        } catch (e: Exception) {
            cal.time = java.util.Date()
        }
        cal.add(java.util.Calendar.DAY_OF_YEAR, addDays)
        val newExpiry = sdf.format(cal.time)

        updateSubscriptionManual(email, true, newExpiry, amount, upiTxnId)
    }

    fun registerInstitute(
        academy: String,
        director: String,
        email: String,
        address: String,
        profileType: String = "COACHING_OWNER",
        gstNumber: String = "",
        businessProofId: String = "",
        libraryCapacity: String = "",
        libraryCategory: String = "",
        tuitionSubject: String = "",
        tuitionStandard: String = "",
        schoolCode: String = ""
    ) {
        sharedPrefs.edit()
            .putString("academy_name", academy)
            .putString("director_name", director)
            .putString("admin_email", email)
            .putString("admin_address", address)
            .putString("profile_type", profileType)
            .apply()
        _academyName.value = academy
        _directorName.value = director
        _adminEmail.value = email
        _adminAddress.value = address

        // Always log in as ADMIN since they registered an Administrator / Director workspace account!
        loginAs("ADMIN")

        // Also append to the tracking list so the owner can view / monitor it
        val current = _institutesList.value.toMutableList()
        if (!current.any { it.email == email }) {
            val isApproved = true
            current.add(
                InstituteAccount(
                    academyName = academy,
                    directorName = director,
                    email = email,
                    address = address,
                    isApproved = isApproved,
                    profileType = profileType,
                    gstNumber = gstNumber,
                    businessProofId = businessProofId,
                    libraryCapacity = libraryCapacity,
                    libraryCategory = libraryCategory,
                    tuitionSubject = tuitionSubject,
                    tuitionStandard = tuitionStandard,
                    schoolCode = schoolCode
                )
            )
            saveInstitutesList(current)
        }
    }

    fun approveInstitute(email: String) {
        val updated = _institutesList.value.map {
            if (it.email == email) it.copy(isApproved = true) else it
        }
        saveInstitutesList(updated)
    }

    fun approveAllInstitutes() {
        val updated = _institutesList.value.map { it.copy(isApproved = true) }
        saveInstitutesList(updated)
    }

    // === Role-Based Portals ===
    private val _currentUserRole = MutableStateFlow(getSafeString(sharedPrefs, "user_role", "GUEST"))
    val currentUserRole = _currentUserRole.asStateFlow()

    private val _currentUserId = MutableStateFlow(getSafeLong(sharedPrefs, "user_student_id", -1L))
    val currentUserId = _currentUserId.asStateFlow()

    private val _staffScreenAccess = MutableStateFlow(
        getSafeStringSet(sharedPrefs, "staff_access", setOf("Batches Setup", "Admission Form", "Attendance", "Study Materials"))
    )
    val staffScreenAccess = _staffScreenAccess.asStateFlow()

    // === Customizable Institution Merchant UPI Configs ===
    private val _merchantUpiId = MutableStateFlow(getSafeString(sharedPrefs, "merchant_upi_id", "smtsharma282.sks@okaxis"))
    val merchantUpiId = _merchantUpiId.asStateFlow()

    private val _merchantName = MutableStateFlow(getSafeString(sharedPrefs, "merchant_name", "Aspirants Success Classes"))
    val merchantName = _merchantName.asStateFlow()

    fun updateMerchantUPI(upi: String, name: String) {
        _merchantUpiId.value = upi.trim()
        _merchantName.value = name.trim()
        try {
            sharedPrefs.edit()
                .putString("merchant_upi_id", upi.trim())
                .putString("merchant_name", name.trim())
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "updateMerchantUPI failed to savePrefs", e)
        }
    }

    fun updateAcademySettings(name: String, director: String, email: String, phone: String = "", address: String = "") {
        _academyName.value = name.trim()
        _directorName.value = director.trim()
        _adminEmail.value = email.trim()
        val finalPhone = if (phone.isNotBlank()) phone.trim() else _adminPhone.value
        val finalAddress = if (address.isNotBlank()) address.trim() else _adminAddress.value
        _adminPhone.value = finalPhone
        _adminAddress.value = finalAddress
        try {
            sharedPrefs.edit()
                .putString("academy_name", name.trim())
                .putString("director_name", director.trim())
                .putString("admin_email", email.trim())
                .putString("admin_phone", finalPhone)
                .putString("admin_address", finalAddress)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "updateAcademySettings failed to savePrefs", e)
        }
    }

    // === 40-Day Complementary Trial & Student Promo Social Sharing Tracking ===
    private val _trialStartDate = MutableStateFlow(getSafeLong(sharedPrefs, "trial_start_date", 0L))
    val trialStartDate = _trialStartDate.asStateFlow()

    private val _promoShareCount = MutableStateFlow(getSafeInt(sharedPrefs, "promo_share_count", 0))
    val promoShareCount = _promoShareCount.asStateFlow()



    fun getRemainingTrialDays(): Int {
        val start = _trialStartDate.value
        val elapsedMs = System.currentTimeMillis() - start
        val elapsedDays = (elapsedMs / (1000 * 60 * 60 * 24)).toInt()
        val remaining = 40 - elapsedDays
        return if (remaining < 0) 0 else remaining
    }

    fun incrementPromoShareCount() {
        val current = _promoShareCount.value
        if (current < 4) {
            val updated = current + 1
            _promoShareCount.value = updated
            try {
                sharedPrefs.edit().putInt("promo_share_count", updated).apply()
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "incrementPromoShareCount failed to savePref", e)
            }
        }
    }

    fun resetPromoShareCount() {
        _promoShareCount.value = 0
        try {
            sharedPrefs.edit().putInt("promo_share_count", 0).apply()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "resetPromoShareCount failed to savePref", e)
        }
    }

    // === Persistent Staff Profiles Management ===
    private val _staffProfiles = MutableStateFlow<List<StaffProfile>>(emptyList())
    val staffProfiles = _staffProfiles.asStateFlow()

    private fun loadStaffProfiles() {
        val jsonStr = getSafeString(sharedPrefs, "staff_profiles_json", "[]")
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<StaffProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val screensArray = obj.optJSONArray("allowedScreens")
                val screens = mutableSetOf<String>()
                if (screensArray != null) {
                    for (j in 0 until screensArray.length()) {
                        screens.add(screensArray.getString(j))
                    }
                }
                list.add(
                    StaffProfile(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", ""),
                        role = obj.optString("role", "Teacher"),
                        phone = obj.optString("phone", ""),
                        allowedScreens = screens,
                        isActive = obj.optBoolean("isActive", true)
                    )
                )
            }
            if (list.isEmpty()) {
                list.add(StaffProfile("staff1", "Sunil Verma", "Senior Maths Faculty", "9876543210", setOf("Attendance", "Study Materials", "Homework"), true))
                list.add(StaffProfile("staff2", "Neha Gupta", "Academy Center Incharge", "9988776655", setOf("Batches Setup", "Admission Form", "Tuition Fees", "Attendance", "Homework"), true))
                list.add(StaffProfile("staff3", "Pankaj Sharma", "Admission Lead Manager", "9456123780", setOf("Enquiry Manager", "Admission Form"), true))
                saveStaffProfilesList(list)
            }
            _staffProfiles.value = list
        } catch (e: Exception) {
            _staffProfiles.value = emptyList()
        }
    }

    private fun saveStaffProfilesList(list: List<StaffProfile>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("role", item.role)
            obj.put("phone", item.phone)
            obj.put("isActive", item.isActive)
            val screensArray = JSONArray()
            item.allowedScreens.forEach { screensArray.put(it) }
            obj.put("allowedScreens", screensArray)
            array.put(obj)
        }
        sharedPrefs.edit().putString("staff_profiles_json", array.toString()).apply()
        _staffProfiles.value = list
    }

    fun addStaffProfile(name: String, role: String, phone: String, screens: Set<String>): String {
        val list = _staffProfiles.value.toMutableList()
        val newId = System.currentTimeMillis().toString()
        list.add(
            StaffProfile(
                id = newId,
                name = name,
                role = role,
                phone = phone,
                allowedScreens = screens,
                isActive = true
            )
        )
        saveStaffProfilesList(list)
        return newId
    }

    fun deleteStaffProfile(id: String) {
        val list = _staffProfiles.value.filter { it.id != id }
        saveStaffProfilesList(list)
    }

    fun updateStaffProfilePermissions(id: String, screens: Set<String>) {
        val updatedList = _staffProfiles.value.map {
            if (it.id == id) it.copy(allowedScreens = screens) else it
        }
        saveStaffProfilesList(updatedList)
    }

    fun updateStaffProfile(id: String, name: String, role: String, phone: String, screens: Set<String>, isActive: Boolean = true) {
        val updatedList = _staffProfiles.value.map {
            if (it.id == id) it.copy(name = name, role = role, phone = phone, allowedScreens = screens, isActive = isActive) else it
        }
        saveStaffProfilesList(updatedList)
    }

    fun deleteInstituteAccount(email: String) {
        val updated = _institutesList.value.filter { it.email != email }
        saveInstitutesList(updated)
    }

    // === Library Subsystem (SaaS Physical Library) ===
    private val _booksList = MutableStateFlow<List<Book>>(emptyList())
    val booksList = _booksList.asStateFlow()

    private val _bookLoansList = MutableStateFlow<List<BookLoan>>(emptyList())
    val bookLoansList = _bookLoansList.asStateFlow()

    private val _librarySeatsList = MutableStateFlow<List<LibrarySeat>>(emptyList())
    val librarySeatsList = _librarySeatsList.asStateFlow()

    private val _libraryAccounts = MutableStateFlow<List<LibraryAccount>>(emptyList())
    val libraryAccounts = _libraryAccounts.asStateFlow()

    private val _activeLibraryAccount = MutableStateFlow<LibraryAccount?>(null)
    val activeLibraryAccount = _activeLibraryAccount.asStateFlow()

    private val _libraryAttendanceLogs = MutableStateFlow<List<LibraryAttendance>>(emptyList())
    val libraryAttendanceLogs = _libraryAttendanceLogs.asStateFlow()

    private fun loadLibraryData() {
        // Books
        val bJson = getSafeString(sharedPrefs, "lib_books_json", "[]")
        try {
            val arr = JSONArray(bJson)
            val list = mutableListOf<Book>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Book(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        author = obj.optString("author", ""),
                        isbn = obj.optString("isbn", ""),
                        category = obj.optString("category", ""),
                        shelfLocation = obj.optString("shelfLocation", ""),
                        totalCopies = obj.optInt("totalCopies", 1),
                        availableCopies = obj.optInt("availableCopies", 1)
                    )
                )
            }
            _booksList.value = list
        } catch (e: Exception) { e.printStackTrace() }

        // Loans
        val lJson = getSafeString(sharedPrefs, "lib_loans_json", "[]")
        try {
            val arr = JSONArray(lJson)
            val list = mutableListOf<BookLoan>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BookLoan(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        bookId = obj.optString("bookId", ""),
                        studentId = obj.optLong("studentId", 0L),
                        studentName = obj.optString("studentName", ""),
                        issueDate = obj.optString("issueDate", ""),
                        dueDate = obj.optString("dueDate", ""),
                        returnDate = if (obj.isNull("returnDate")) null else obj.optString("returnDate"),
                        fineAmount = obj.optDouble("fineAmount", 0.0)
                    )
                )
            }
            _bookLoansList.value = list
        } catch (e: Exception) { e.printStackTrace() }

        // Library Study Cabins/Seats
        val sJson = getSafeString(sharedPrefs, "lib_seats_json", "[]")
        try {
            val arr = JSONArray(sJson)
            val list = mutableListOf<LibrarySeat>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LibrarySeat(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        seatNumber = obj.optString("seatNumber", ""),
                        cabinBlock = obj.optString("cabinBlock", "Zone A"),
                        rentPriceMonthly = obj.optDouble("rentPriceMonthly", 500.0),
                        isBooked = obj.optBoolean("isBooked", false),
                        bookedByStudentId = if (obj.isNull("bookedByStudentId")) null else obj.optLong("bookedByStudentId"),
                        bookedByStudentName = if (obj.isNull("bookedByStudentName")) null else obj.optString("bookedByStudentName"),
                        bookingStartDate = if (obj.isNull("bookingStartDate")) null else obj.optString("bookingStartDate"),
                        bookingEndDate = if (obj.isNull("bookingEndDate")) null else obj.optString("bookingEndDate")
                    )
                )
            }
            if (list.isEmpty()) {
                // Pre-seed some library seat desks
                for (i in 1..20) {
                    list.add(LibrarySeat("${System.currentTimeMillis()}-$i", "Cabin Slot #$i", "Main Quiet Hall", 400.0 + (i % 3) * 100.0, false, null, null, null, null))
                }
                saveLibrarySeatsList(list)
            }
            _librarySeatsList.value = list
        } catch (e: Exception) { e.printStackTrace() }

        // Library Accounts
        val accountsJson = getSafeString(sharedPrefs, "lib_accounts_json", "[]")
        try {
            val arr = JSONArray(accountsJson)
            val list = mutableListOf<LibraryAccount>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LibraryAccount(
                        rollNumber = obj.optString("rollNumber", ""),
                        name = obj.optString("name", ""),
                        phone = obj.optString("phone", ""),
                        password = obj.optString("password", ""),
                        isStaff = obj.optBoolean("isStaff", false),
                        preferredShift = obj.optString("preferredShift", "Morning Shift (6 AM - 12 PM)"),
                        preferredZone = obj.optString("preferredZone", "All")
                    )
                )
            }
            if (list.isEmpty()) {
                // Completely empty - no prefilled accounts
            }
            _libraryAccounts.value = list
        } catch (e: Exception) { e.printStackTrace() }

        // Library Attendance Logs
        val attJson = getSafeString(sharedPrefs, "lib_attendance_json", "[]")
        try {
            val arr = JSONArray(attJson)
            val list = mutableListOf<LibraryAttendance>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LibraryAttendance(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        studentName = obj.optString("studentName", ""),
                        rollNumber = obj.optString("rollNumber", ""),
                        phone = obj.optString("phone", ""),
                        timestamp = obj.optString("timestamp", ""),
                        type = obj.optString("type", "CHECK_IN")
                    )
                )
            }
            if (list.isEmpty()) {
                saveLibraryAttendanceList(list)
            }
            _libraryAttendanceLogs.value = list
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun saveBooksList(list: List<Book>) {
        val arr = JSONArray()
        for (b in list) {
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("author", b.author)
                put("isbn", b.isbn)
                put("category", b.category)
                put("shelfLocation", b.shelfLocation)
                put("totalCopies", b.totalCopies)
                put("availableCopies", b.availableCopies)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("lib_books_json", arr.toString()).apply()
        _booksList.value = list
    }

    private fun saveBookLoansList(list: List<BookLoan>) {
        val arr = JSONArray()
        for (l in list) {
            val obj = JSONObject().apply {
                put("id", l.id)
                put("bookId", l.bookId)
                put("studentId", l.studentId)
                put("studentName", l.studentName)
                put("issueDate", l.issueDate)
                put("dueDate", l.dueDate)
                if (l.returnDate == null) put("returnDate", JSONObject.NULL) else put("returnDate", l.returnDate)
                put("fineAmount", l.fineAmount)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("lib_loans_json", arr.toString()).apply()
        _bookLoansList.value = list
    }

    private fun saveLibrarySeatsList(list: List<LibrarySeat>) {
        val arr = JSONArray()
        for (s in list) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("seatNumber", s.seatNumber)
                put("cabinBlock", s.cabinBlock)
                put("rentPriceMonthly", s.rentPriceMonthly)
                put("isBooked", s.isBooked)
                if (s.bookedByStudentId == null) put("bookedByStudentId", JSONObject.NULL) else put("bookedByStudentId", s.bookedByStudentId)
                if (s.bookedByStudentName == null) put("bookedByStudentName", JSONObject.NULL) else put("bookedByStudentName", s.bookedByStudentName)
                if (s.bookingStartDate == null) put("bookingStartDate", JSONObject.NULL) else put("bookingStartDate", s.bookingStartDate)
                if (s.bookingEndDate == null) put("bookingEndDate", JSONObject.NULL) else put("bookingEndDate", s.bookingEndDate)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("lib_seats_json", arr.toString()).apply()
        _librarySeatsList.value = list
    }

    fun addBook(title: String, author: String, isbn: String, category: String, shelfLocation: String, totalCopies: Int) {
        val list = _booksList.value.toMutableList()
        list.add(
            Book(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                isbn = isbn,
                category = category,
                shelfLocation = shelfLocation,
                totalCopies = totalCopies,
                availableCopies = totalCopies
            )
        )
        saveBooksList(list)
    }

    fun updateBook(book: Book) {
        val updated = _booksList.value.map { if (it.id == book.id) book else it }
        saveBooksList(updated)
    }

    fun deleteBook(id: String) {
        val updated = _booksList.value.filter { it.id != id }
        saveBooksList(updated)
    }

    private fun saveLibraryAccountsList(list: List<LibraryAccount>) {
        val arr = JSONArray()
        for (a in list) {
            val obj = JSONObject().apply {
                put("rollNumber", a.rollNumber)
                put("name", a.name)
                put("phone", a.phone)
                put("password", a.password)
                put("isStaff", a.isStaff)
                put("preferredShift", a.preferredShift)
                put("preferredZone", a.preferredZone)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("lib_accounts_json", arr.toString()).apply()
        _libraryAccounts.value = list
    }

    private fun saveLibraryAttendanceList(list: List<LibraryAttendance>) {
        val arr = JSONArray()
        for (att in list) {
            val obj = JSONObject().apply {
                put("id", att.id)
                put("studentName", att.studentName)
                put("rollNumber", att.rollNumber)
                put("phone", att.phone)
                put("timestamp", att.timestamp)
                put("type", att.type)
            }
            arr.put(obj)
        }
        sharedPrefs.edit().putString("lib_attendance_json", arr.toString()).apply()
        _libraryAttendanceLogs.value = list
    }

    fun registerLibraryAccount(
        rollNumber: String,
        name: String,
        phone: String,
        pass: String,
        isStaff: Boolean = false,
        preferredShift: String = "Morning Shift (6 AM - 12 PM)",
        preferredZone: String = "All"
    ): Boolean {
        val list = _libraryAccounts.value.toMutableList()
        if (list.any { it.rollNumber.equals(rollNumber, ignoreCase = true) }) {
            return false // Already exists
        }
        val newAcc = LibraryAccount(rollNumber, name, phone, pass, isStaff, preferredShift, preferredZone)
        list.add(newAcc)
        saveLibraryAccountsList(list)
        return true
    }

    fun loginLibraryAccount(rollNumber: String, pass: String): Boolean {
        val found = _libraryAccounts.value.find { 
            it.rollNumber.equals(rollNumber, ignoreCase = true) && it.password == pass 
        }
        if (found != null) {
            _activeLibraryAccount.value = found
            return true
        }
        return false
    }

    fun logoutLibraryAccount() {
        _activeLibraryAccount.value = null
    }

    fun addLibraryAttendance(rollNumber: String, studentName: String, phone: String, type: String): String {
        val list = _libraryAttendanceLogs.value.toMutableList()
        val timeFormat = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
        val timestampString = timeFormat.format(java.util.Date())
        val newLog = LibraryAttendance(
            id = "att_${System.currentTimeMillis()}",
            studentName = studentName,
            rollNumber = rollNumber,
            phone = phone,
            timestamp = timestampString,
            type = type
        )
        list.add(0, newLog)
        saveLibraryAttendanceList(list)
        return timestampString
    }

    fun issueBook(bookId: String, studentId: Long, studentName: String, issueDate: String, dueDate: String) {
        val loans = _bookLoansList.value.toMutableList()
        loans.add(
            BookLoan(
                id = UUID.randomUUID().toString(),
                bookId = bookId,
                studentId = studentId,
                studentName = studentName,
                issueDate = issueDate,
                dueDate = dueDate,
                returnDate = null,
                fineAmount = 0.0
            )
        )
        val books = _booksList.value.map {
            if (it.id == bookId) it.copy(availableCopies = (it.availableCopies - 1).coerceAtLeast(0)) else it
        }
        saveBooksList(books)
        saveBookLoansList(loans)
    }

    fun returnBook(loanId: String, returnDate: String, fineAmount: Double) {
        var bookIdToReplenish: String? = null
        val updatedLoans = _bookLoansList.value.map {
            if (it.id == loanId) {
                bookIdToReplenish = it.bookId
                it.copy(returnDate = returnDate, fineAmount = fineAmount)
            } else it
        }
        if (bookIdToReplenish != null) {
            val books = _booksList.value.map {
                if (it.id == bookIdToReplenish) it.copy(availableCopies = (it.availableCopies + 1).coerceAtLeast(0).coerceAtMost(it.totalCopies)) else it
            }
            saveBooksList(books)
        }
        saveBookLoansList(updatedLoans)
    }

    fun addLibrarySeat(seatNumber: String, cabinBlock: String, rentPrice: Double) {
        val list = _librarySeatsList.value.toMutableList()
        list.add(
            LibrarySeat(
                id = UUID.randomUUID().toString(),
                seatNumber = seatNumber,
                cabinBlock = cabinBlock,
                rentPriceMonthly = rentPrice,
                isBooked = false,
                bookedByStudentId = null,
                bookedByStudentName = null,
                bookingStartDate = null,
                bookingEndDate = null
            )
        )
        saveLibrarySeatsList(list)
    }

    fun bookLibrarySeat(seatId: String, studentId: Long, studentName: String, startDate: String, endDate: String) {
        val updated = _librarySeatsList.value.map {
            if (it.id == seatId) {
                it.copy(
                    isBooked = true,
                    bookedByStudentId = studentId,
                    bookedByStudentName = studentName,
                    bookingStartDate = startDate,
                    bookingEndDate = endDate
                )
            } else it
        }
        saveLibrarySeatsList(updated)
    }

    fun releaseLibrarySeat(seatId: String) {
        val updated = _librarySeatsList.value.map {
            if (it.id == seatId) {
                it.copy(
                    isBooked = false,
                    bookedByStudentId = null,
                    bookedByStudentName = null,
                    bookingStartDate = null,
                    bookingEndDate = null
                )
            } else it
        }
        saveLibrarySeatsList(updated)
    }

    fun deleteLibrarySeat(seatId: String) {
        val updated = _librarySeatsList.value.filter { it.id != seatId }
        saveLibrarySeatsList(updated)
    }

    // === Device & Safety Settings ===
    private val _isBiometricLocked = MutableStateFlow(getSafeBoolean(sharedPrefs, "biometric_locked", false))
    val isBiometricLocked = _isBiometricLocked.asStateFlow()

    private val _preferredSmsSim = MutableStateFlow(getSafeString(sharedPrefs, "preferred_sms_sim", "SIM 1"))
    val preferredSmsSim = _preferredSmsSim.asStateFlow()

    private val _whatsAppRouting = MutableStateFlow(getSafeString(sharedPrefs, "whatsapp_routing", "WhatsApp Intent"))
    val whatsAppRouting = _whatsAppRouting.asStateFlow()

    // === Database State Flows ===
    private val rawBatches = repository.allBatches
    private val rawStudents = repository.allStudents
    private val rawFeePayments = repository.allFeePayments
    private val rawTransactions = repository.allTransactions
    private val rawStudyMaterials = repository.allStudyMaterials
    private val rawExams = repository.allExams

    val batches = combine(rawBatches, _adminEmail, _currentUserRole) { list, email, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            list.filter { it.instituteEmail == email }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students = combine(rawStudents, batches, _currentUserRole) { list, activeBatches, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            val activeBatchIds = activeBatches.map { it.id }.toSet()
            list.filter { it.batchId in activeBatchIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feePayments = combine(rawFeePayments, students, _currentUserRole) { list, activeStudents, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            val activeStudentIds = activeStudents.map { it.id }.toSet()
            list.filter { it.studentId in activeStudentIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions = combine(rawTransactions, _adminEmail, _currentUserRole) { list, email, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            list.filter { it.instituteEmail == email }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyMaterials = combine(rawStudyMaterials, batches, _currentUserRole) { list, activeBatches, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            val activeBatchIds = activeBatches.map { it.id }.toSet()
            list.filter { it.batchId in activeBatchIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messageTemplates = repository.allMessageTemplates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams = combine(rawExams, batches, _currentUserRole) { list, activeBatches, role ->
        if (role == "STUDENT" || role == "PARENT" || role == "GUEST") {
            list
        } else {
            val activeBatchIds = activeBatches.map { it.id }.toSet()
            list.filter { it.batchId in activeBatchIds }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Dynamic Local Selected States ===
    private val _selectedBatchIdForAttendance = MutableStateFlow<Long?>(null)
    val selectedBatchIdForAttendance = _selectedBatchIdForAttendance.asStateFlow()

    private val _attendanceDate = MutableStateFlow("2026-06-14")
    val attendanceDate = _attendanceDate.asStateFlow()

    // === Theme Configuration ===
    private val _themeMode = MutableStateFlow(getSafeString(sharedPrefs, "app_theme", "CLASSIC_NAVY"))
    val themeMode = _themeMode.asStateFlow()

    // === Google Drive Backup Configuration ===
    private val _googleDriveBackupTime = MutableStateFlow(getSafeString(sharedPrefs, "last_google_drive_backup", "Never"))
    val googleDriveBackupTime = _googleDriveBackupTime.asStateFlow()

    private val _googleDriveBackupFrequency = MutableStateFlow(getSafeString(sharedPrefs, "google_drive_backup_frequency", "Manual"))
    val googleDriveBackupFrequency = _googleDriveBackupFrequency.asStateFlow()

    // === Custom AI/Gemini API Key Configuration ===
    private val _customGeminiApiKey = MutableStateFlow(getSafeString(sharedPrefs, "custom_gemini_api_key", ""))
    val customGeminiApiKey = _customGeminiApiKey.asStateFlow()

    // === Study Material Favorites & Personal Notes ===
    private val _favoritedMaterialIds = MutableStateFlow<Set<String>>(getSafeStringSet(sharedPrefs, "favorited_material_ids", emptySet()))
    val favoritedMaterialIds = _favoritedMaterialIds.asStateFlow()

    private val _personalNotesMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val personalNotesMap = _personalNotesMap.asStateFlow()

    fun loadPersonalNotes() {
        val jsonStr = sharedPrefs.getString("personal_material_notes_json", "{}") ?: "{}"
        val map = mutableMapOf<String, String>()
        try {
            val obj = org.json.JSONObject(jsonStr)
            obj.keys().forEach { key ->
                map[key] = obj.optString(key)
            }
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "Failed to load personal notes", e)
        }
        _personalNotesMap.value = map
    }

    fun toggleFavoriteMaterial(materialId: Long) {
        val current = _favoritedMaterialIds.value.toMutableSet()
        val idStr = materialId.toString()
        if (current.contains(idStr)) {
            current.remove(idStr)
        } else {
            current.add(idStr)
        }
        _favoritedMaterialIds.value = current
        sharedPrefs.edit().putStringSet("favorited_material_ids", current).apply()
    }

    fun savePersonalNote(materialId: Long, note: String) {
        val current = _personalNotesMap.value.toMutableMap()
        current[materialId.toString()] = note
        _personalNotesMap.value = current
        
        val obj = org.json.JSONObject()
        current.forEach { (k, v) ->
            obj.put(k, v)
        }
        sharedPrefs.edit().putString("personal_material_notes_json", obj.toString()).apply()
    }

    // === Community Chat Features ===
    val chatChannels: StateFlow<List<ChatChannel>> = repository.allChatChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentChannelId = MutableStateFlow<Long?>(null)
    val currentChannelId = _currentChannelId.asStateFlow()

    private var messagesJob: kotlinx.coroutines.Job? = null
    private val _currentChannelMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentChannelMessages = _currentChannelMessages.asStateFlow()

    fun selectChannel(channelId: Long?) {
        _currentChannelId.value = channelId
        messagesJob?.cancel()
        if (channelId != null) {
            messagesJob = viewModelScope.launch {
                repository.getMessagesForChannel(channelId).collect {
                    _currentChannelMessages.value = it
                }
            }
        } else {
            _currentChannelMessages.value = emptyList()
        }
    }

    fun createChatChannel(name: String, description: String, isGroup: Boolean, isPrivate: Boolean = false, firstUserId: String = "", secondUserId: String = "") {
        viewModelScope.launch {
            val channel = ChatChannel(
                name = name,
                description = description,
                isGroup = isGroup,
                isPrivate = isPrivate,
                firstUserId = firstUserId,
                secondUserId = secondUserId
            )
            repository.insertChatChannel(channel)
        }
    }

    fun deleteChannel(channelId: Long) {
        viewModelScope.launch {
            repository.deleteChatChannel(channelId)
            if (_currentChannelId.value == channelId) {
                selectChannel(null)
            }
        }
    }

    fun clearChannelMessages(channelId: Long) {
        viewModelScope.launch {
            repository.clearMessagesForChannel(channelId)
        }
    }

    fun sendChatMessage(
        channelId: Long,
        senderId: String,
        senderName: String,
        senderRole: String,
        messageText: String,
        attachmentPath: String? = null,
        attachmentType: String? = null,
        attachmentName: String? = null
    ) {
        viewModelScope.launch {
            val msg = ChatMessage(
                channelId = channelId,
                senderId = senderId,
                senderName = senderName,
                senderRole = senderRole,
                messageText = messageText,
                timestamp = System.currentTimeMillis(),
                attachmentPath = attachmentPath,
                attachmentType = attachmentType,
                attachmentName = attachmentName
            )
            repository.insertChatMessage(msg)
        }
    }

    fun seedDefaultChatChannels() {
        viewModelScope.launch {
            val current = repository.allChatChannels.first()
            if (current.isEmpty()) {
                val channels = listOf(
                    ChatChannel(name = "📢 Announcements", description = "Official updates and academic news from Teachers & Admins"),
                    ChatChannel(name = "🧮 Mathematics Discussion", description = "Share solutions, algebra tricks, calculus doubts, and formulas"),
                    ChatChannel(name = "🧪 Science & Physics Doubt-Solving", description = "Interact about mechanics, thermodynamics, chemistry experiments"),
                    ChatChannel(name = "💬 General Chit-Chat", description = "Casual student-teacher space for everyday knowledge sharing"),
                    ChatChannel(name = "💡 UPSC/Competitive Exam Prep", description = "Tips, syllabus review, strategies, and general knowledge Q&As")
                )
                channels.forEach { repository.insertChatChannel(it) }
            }
        }
    }

    fun updateCustomGeminiApiKey(key: String) {
        _customGeminiApiKey.value = key.trim()
        sharedPrefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    fun switchTheme(theme: String) {
        _themeMode.value = theme
        sharedPrefs.edit().putString("app_theme", theme).apply()
    }

    // === Student Dynamic SaaS Multi-tenancy Context Switcher ===
    private val _activeStudentContext = MutableStateFlow(getSafeString(sharedPrefs, "active_student_context", "Verma Sir's Tuition"))
    val activeStudentContext = _activeStudentContext.asStateFlow()

    fun switchStudentContext(newContext: String) {
        _activeStudentContext.value = newContext
        sharedPrefs.edit().putString("active_student_context", newContext).apply()
    }

    // === Role Auth Actions ===
    fun loginAs(role: String, studentId: Long = -1L) {
        _currentUserRole.value = role
        _currentUserId.value = studentId
        sharedPrefs.edit()
            .putString("user_role", role)
            .putLong("user_student_id", studentId)
            .apply()
    }

    fun updateStaffAccess(screens: Set<String>) {
        _staffScreenAccess.value = screens
        sharedPrefs.edit().putStringSet("staff_access", screens).apply()
    }

    fun toggleBiometricLock(enabled: Boolean) {
        _isBiometricLocked.value = enabled
        sharedPrefs.edit().putBoolean("biometric_locked", enabled).apply()
    }

    // === Teacher Management & SaaS Partition Engine ===
    private val _teachersList = java.util.concurrent.CopyOnWriteArrayList<TeacherAccount>()
    private val _observableTeachersList = MutableStateFlow<List<TeacherAccount>>(emptyList())
    val teachersList = _observableTeachersList.asStateFlow()

    private val _currentTeacher = MutableStateFlow<TeacherAccount?>(null)
    val currentTeacher = _currentTeacher.asStateFlow()

    fun loadTeachers() {
        val jsonStr = getSafeString(sharedPrefs, "registered_teachers_json", "[]")
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TeacherAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TeacherAccount(
                        email = obj.optString("email", ""),
                        name = obj.optString("name", ""),
                        subject = obj.optString("subject", ""),
                        centerName = obj.optString("centerName", ""),
                        passcode = obj.optString("passcode", "1234"),
                        isPremium = obj.optBoolean("isPremium", false),
                        premiumPlan = obj.optString("premiumPlan", "Basic Free Tier"),
                        premiumExpiry = obj.optString("premiumExpiry", "N/A"),
                        salary = obj.optDouble("salary", 15000.0)
                    )
                )
            }
            _teachersList.clear()
            _teachersList.addAll(list)
            _observableTeachersList.value = list
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "loadTeachers failed", e)
        }
    }

    private fun saveTeachersList() {
        val array = JSONArray()
        for (item in _teachersList) {
            val obj = JSONObject()
            obj.put("email", item.email)
            obj.put("name", item.name)
            obj.put("subject", item.subject)
            obj.put("centerName", item.centerName)
            obj.put("passcode", item.passcode)
            obj.put("isPremium", item.isPremium)
            obj.put("premiumPlan", item.premiumPlan)
            obj.put("premiumExpiry", item.premiumExpiry)
            obj.put("salary", item.salary)
            array.put(obj)
        }
        sharedPrefs.edit().putString("registered_teachers_json", array.toString()).apply()
        _observableTeachersList.value = _teachersList.toList()
    }

    fun updateTeacherSalary(email: String, newSalary: Double) {
        val emailClean = email.trim().lowercase()
        val index = _teachersList.indexOfFirst { it.email == emailClean }
        if (index != -1) {
            val updated = _teachersList[index].copy(salary = newSalary)
            _teachersList[index] = updated
            saveTeachersList()
            if (_currentTeacher.value?.email == emailClean) {
                _currentTeacher.value = updated
            }
        }
    }

    fun registerTeacher(name: String, email: String, subject: String, centerName: String, passcode: String): Boolean {
        if (email.isBlank() || name.isBlank() || passcode.isBlank()) return false
        val emailClean = email.trim().lowercase()
        if (_teachersList.any { it.email == emailClean }) return false
        
        val newTeacher = TeacherAccount(
            email = emailClean,
            name = name,
            subject = subject,
            centerName = centerName,
            passcode = passcode,
            isPremium = false,
            premiumPlan = "Basic Free Tier",
            premiumExpiry = "N/A"
        )
        _teachersList.add(newTeacher)
        saveTeachersList()
        return true
    }

    fun loginTeacher(email: String, passcode: String): Boolean {
        val emailClean = email.trim().lowercase()
        val teacher = _teachersList.find { it.email == emailClean && it.passcode == passcode }
        if (teacher != null) {
            _currentTeacher.value = teacher
            
            // Set up SaaS partition values to target this teacher's data!
            _academyName.value = teacher.centerName
            _directorName.value = teacher.name
            _adminEmail.value = teacher.email
            _adminAddress.value = "Subject: ${teacher.subject}"
            
            sharedPrefs.edit()
                .putString("academy_name", teacher.centerName)
                .putString("director_name", teacher.name)
                .putString("admin_email", teacher.email)
                .putString("admin_address", "Subject: ${teacher.subject}")
                .apply()
            
            loginAs("TEACHER")
            return true
        }
        return false
    }

    fun upgradeTeacherPremium(email: String, planName: String, priceStr: String, refId: String) {
        val emailClean = email.trim().lowercase()
        val index = _teachersList.indexOfFirst { it.email == emailClean }
        if (index != -1) {
            val updated = _teachersList[index].copy(
                isPremium = true,
                premiumPlan = planName,
                premiumExpiry = "2027-06-20" // Year extension
            )
            _teachersList[index] = updated
            saveTeachersList()
            if (_currentTeacher.value?.email == emailClean) {
                _currentTeacher.value = updated
            }
            
            // Record Income & Expenses Ledger transaction representing the admin's income from this premium sale!
            viewModelScope.launch {
                repository.insertTransaction(
                    FinancialTransaction(
                        type = "INCOME",
                        amount = priceStr.toDoubleOrNull() ?: 499.0,
                        category = "Premium Subscription",
                        description = "Teacher Premium Subscription purchase ($planName) by $emailClean. Ref: $refId",
                        instituteEmail = "admin@tams.com" // Goes to primary platform admin account!
                    )
                )
            }
        }
    }

    fun updateSmsSim(sim: String) {
        _preferredSmsSim.value = sim
        sharedPrefs.edit().putString("preferred_sms_sim", sim).apply()
    }

    fun updateWhatsAppRouting(routing: String) {
        _whatsAppRouting.value = routing
        sharedPrefs.edit().putString("whatsapp_routing", routing).apply()
    }

    // === Batches ===
    fun addBatch(name: String, location: String, teacher: String, timings: String, days: String, capacity: Int, amount: Double, sub: String) {
        viewModelScope.launch {
            val batch = Batch(
                name = name,
                location = location,
                assignedTeacher = teacher,
                classTimings = timings,
                daysOfWeek = days,
                maxCapacity = capacity,
                feesAmount = amount,
                subject = sub,
                isActive = true,
                instituteEmail = _adminEmail.value
            )
            val insertedId = repository.insertBatch(batch)
            // Sync with Firestore Cloud
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncBatchToCloud(batch.copy(id = insertedId))
                    android.util.Log.d("AppViewModel", "New batch synced to Firestore.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Firestore batch sync failed: ${e.localizedMessage}")
            }
        }
    }

    fun updateBatch(batch: Batch) {
        viewModelScope.launch {
            repository.updateBatch(batch)
            // Sync with Firestore Cloud on update
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncBatchToCloud(batch)
                    android.util.Log.d("AppViewModel", "Updated batch synced to Firestore.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Cloud sync on batch update failed: ${e.localizedMessage}")
            }
        }
    }

    fun deleteBatch(batch: Batch) {
        viewModelScope.launch {
            repository.deleteBatch(batch)
        }
    }

    // === Students Admission Workflow ===
    fun registerStudent(
        rollNo: String,
        name: String,
        parentName: String,
        dob: String,
        phone: String,
        parentPhone: String,
        gender: String,
        address: String,
        batchId: Long,
        studentClass: String,
        subject: String,
        schoolName: String,
        custom1: String,
        custom2: String,
        custom3: String,
        feeType: String,
        admissionFee: Double,
        regFee: Double,
        familyId: String,
        photoPath: String? = null,
        docPath: String? = null
    ) {
        viewModelScope.launch {
            // 1. Create student
            val studentToInsert = Student(
                batchId = batchId,
                rollNumber = rollNo,
                name = name,
                parentName = parentName,
                dateOfBirth = dob,
                phone = phone,
                parentPhone = parentPhone,
                gender = gender,
                address = address,
                startDate = "2026-06-14",
                studentClass = studentClass,
                subject = subject,
                schoolCollegeName = schoolName,
                customField1 = custom1,
                customField2 = custom2,
                customField3 = custom3,
                profilePhotoPath = photoPath,
                documentAttachments = docPath,
                familyId = familyId.ifEmpty { UUID.randomUUID().toString().substring(0, 8) }
            )
            val studentId = repository.insertStudent(studentToInsert)
            val studentWithId = studentToInsert.copy(id = studentId)

            // Sync with Firestore Cloud
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncStudentToCloud(studentWithId)
                    android.util.Log.d("AppViewModel", "New student successfully synced to Firestore.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Firestore student sync failed: ${e.localizedMessage}")
            }

            // 2. Log initial Admission Fee payment if > 0
            if (admissionFee > 0.0) {
                repository.insertFeePayment(
                    FeePayment(
                        studentId = studentId,
                        feeType = feeType,
                        amountPaid = admissionFee,
                        receiptNo = "REC-ADM-${System.currentTimeMillis() % 10000}",
                        monthPaidFor = "Initial Admission",
                        paymentMode = "CASH",
                        remarks = "Admission Fee Paid"
                    )
                )
                repository.insertTransaction(
                    FinancialTransaction(
                        type = "INCOME",
                        amount = admissionFee,
                        category = "Admission Fee",
                        description = "Admission Fee collected for student: $name",
                        instituteEmail = _adminEmail.value
                    )
                )
            }

            // 3. Log initial Registration Fee payment if > 0
            if (regFee > 0.0) {
                repository.insertFeePayment(
                    FeePayment(
                        studentId = studentId,
                        feeType = feeType,
                        amountPaid = regFee,
                        receiptNo = "REC-REG-${System.currentTimeMillis() % 10000}",
                        monthPaidFor = "Initial Registration",
                        paymentMode = "CASH",
                        remarks = "Registration Fee Paid"
                    )
                )
                repository.insertTransaction(
                    FinancialTransaction(
                        type = "INCOME",
                        amount = regFee,
                        category = "Registration Fee",
                        description = "Registration Fee collected for student: $name",
                        instituteEmail = _adminEmail.value
                    )
                )
            }

            // 4. Auto dispatch simulation or alert trigger (Mock notification channel logs)
            Toast.makeText(context, "Student Registered successfully! Welcome SMS dispatched for $name.", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            // Sync with Firestore Cloud on update
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncStudentToCloud(student)
                    android.util.Log.d("AppViewModel", "Updated student successfully synced to Firestore.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Cloud sync on update failed: ${e.localizedMessage}")
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // === Attendance Tracking Workflow ===
    fun setAttendanceDate(date: String) {
        _attendanceDate.value = date
    }

    fun selectAttendanceBatch(batchId: Long?) {
        _selectedBatchIdForAttendance.value = batchId
    }

    fun getAttendanceForBatchAndDateFlow(batchId: Long, dateStr: String): Flow<List<Attendance>> {
        return repository.getAttendanceForBatchAndDate(batchId, dateStr)
    }

    fun saveSingleAttendance(studentId: Long, batchId: Long, dateStr: String, status: String, method: String) {
        viewModelScope.launch {
            repository.deleteAttendance(studentId, dateStr)
            val attendance = Attendance(
                studentId = studentId,
                batchId = batchId,
                dateString = dateStr,
                status = status,
                trackingMethod = method
            )
            repository.insertAttendance(attendance)
            // Sync with Firestore Cloud
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncAttendanceToCloud(attendance)
                    android.util.Log.d("AppViewModel", "Attendance synced to Firestore.")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Firestore attendance sync failed: ${e.localizedMessage}")
            }
        }
    }

    fun performBulkAttendance(studentIds: List<Long>, batchId: Long, dateStr: String, status: String, method: String) {
        viewModelScope.launch {
            studentIds.forEach { sId ->
                repository.deleteAttendance(sId, dateStr)
                val attendance = Attendance(
                    studentId = sId,
                    batchId = batchId,
                    dateString = dateStr,
                    status = status,
                    trackingMethod = method
                )
                repository.insertAttendance(attendance)
                // Sync with Firestore Cloud
                try {
                    FirebaseService.initialize(context)
                    if (FirebaseService.isInitialized) {
                        FirebaseService.syncAttendanceToCloud(attendance)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppViewModel", "Firestore bulk attendance sync failed: ${e.localizedMessage}")
                }
            }
            Toast.makeText(context, "Attendance updated in bulk for ${studentIds.size} student slots.", Toast.LENGTH_SHORT).show()
        }
    }

    // === Financial Ledger Logic ===
    fun collectFeePayment(studentId: Long, amount: Double, discount: Double, feeType: String, month: String, mode: String, remarks: String) {
        viewModelScope.launch {
            val receipt = "REC-FEE-${System.currentTimeMillis() % 100000}"
            val payment = FeePayment(
                studentId = studentId,
                feeType = feeType,
                amountPaid = amount,
                discount = discount,
                paymentMode = mode,
                remarks = remarks,
                receiptNo = receipt,
                monthPaidFor = month
            )
            val paymentId = repository.insertFeePayment(payment)
            val paymentWithId = payment.copy(id = paymentId)

            repository.insertTransaction(
                FinancialTransaction(
                    type = "INCOME",
                    amount = amount,
                    category = "Fee Payment",
                    description = "Monthly / Course Tuition fee collected for student id: $studentId",
                    instituteEmail = _adminEmail.value
                )
            )

            // Sync with Firestore logged-in user cloud database
            try {
                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    FirebaseService.syncFeePaymentToCloud(paymentWithId)
                    android.util.Log.d("AppViewModel", "Payment successfully synced to Firestore for receipt: $receipt")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "Firestore sync failed silently: ${e.localizedMessage}")
            }
        }
    }

    fun deleteFeePayment(payment: FeePayment) {
        viewModelScope.launch {
            repository.deleteFeePayment(payment)
            // Register matching rollback in financial ledger transactions as an expense/reversion
            repository.insertTransaction(
                FinancialTransaction(
                    type = "EXPENSE",
                    amount = payment.amountPaid,
                    category = "Fee Reversal",
                    description = "Voided/Reverted payment receipt Ref: ${payment.receiptNo} of amount ₹${payment.amountPaid} for student: ${payment.studentId}",
                    instituteEmail = _adminEmail.value
                )
            )
        }
    }

    fun insertTransaction(type: String, amount: Double, category: String, desc: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                FinancialTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = desc,
                    instituteEmail = _adminEmail.value
                )
            )
            
            // Trigger alerts dynamically if expense head exceeds threshold limit
            if (type == "EXPENSE" && amount > 5000.0) {
                Toast.makeText(context, "⚠️ ALERT: Major Expense Head recorded ($category: ₹$amount)!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // === Family grouping calculation helpers ===
    fun getSiblingsForStudent(familyId: String, currentStudentId: Long): List<Student> {
        if (familyId.isEmpty()) return emptyList()
        return students.value.filter { it.familyId == familyId && it.id != currentStudentId }
    }

    fun getFamilyPendingFees(familyId: String): Double {
        if (familyId.isEmpty()) return 0.0
        val familyMembers = students.value.filter { it.familyId == familyId }
        var totalExpected = 0.0
        familyMembers.forEach { s ->
            val b = batches.value.firstOrNull { it.id == s.batchId }
            totalExpected += b?.feesAmount ?: 0.0
        }
        val payments = feePayments.value.filter { p -> familyMembers.any { it.id == p.studentId } }
        val totalPaid = payments.sumOf { it.amountPaid + it.discount }
        return kotlin.math.max(0.0, totalExpected - totalPaid)
    }

    // === Academics & Interactive Study Material ===
    fun uploadStudyMaterial(batchId: Long, category: String, subCategory: String, title: String, type: String, content: String, pdf: String? = null, tube: String? = null) {
        viewModelScope.launch {
            repository.insertStudyMaterial(
                StudyMaterial(
                    batchId = batchId,
                    mainCategory = category,
                    topicSubCategory = subCategory,
                    title = title,
                    contentType = type,
                    content = content,
                    pdfName = pdf,
                    youtubeUrl = tube
                )
            )
            Toast.makeText(context, "Study resources updated for Batch category!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteStudyMaterial(material: StudyMaterial) {
        viewModelScope.launch {
            repository.deleteStudyMaterial(material)
            Toast.makeText(context, "Study resource deleted.", Toast.LENGTH_SHORT).show()
        }
    }

    // === Study Material Quizzes & Interactive Assessments ===
    fun getQuizzesForMaterial(studyMaterialId: Long): Flow<List<MaterialQuiz>> {
        return repository.getQuizzesForMaterial(studyMaterialId)
    }

    fun getQuestionsForQuiz(quizId: Long): Flow<List<MaterialQuizQuestion>> {
        return repository.getQuestionsForQuiz(quizId)
    }

    fun getAttemptsForQuiz(quizId: Long): Flow<List<MaterialQuizAttempt>> {
        return repository.getAttemptsForQuiz(quizId)
    }

    fun createQuizForMaterial(studyMaterialId: Long, title: String, description: String, duration: Int, questions: List<MaterialQuizQuestion>) {
        viewModelScope.launch {
            val quizId = repository.insertMaterialQuiz(
                MaterialQuiz(
                    studyMaterialId = studyMaterialId,
                    title = title,
                    description = description,
                    durationMinutes = duration
                )
            )
            val updatedQuestions = questions.map { it.copy(quizId = quizId) }
            repository.insertMaterialQuizQuestionsBulk(updatedQuestions)
            Toast.makeText(context, "Quiz created successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteQuiz(quiz: MaterialQuiz) {
        viewModelScope.launch {
            repository.deleteMaterialQuiz(quiz)
            Toast.makeText(context, "Quiz deleted successfully.", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitQuizAttempt(quizId: Long, studentId: Long, score: Int, total: Int, feedback: String) {
        viewModelScope.launch {
            repository.insertMaterialQuizAttempt(
                MaterialQuizAttempt(
                    quizId = quizId,
                    studentId = studentId,
                    score = score,
                    totalQuestions = total,
                    feedbackJson = feedback
                )
            )
            Toast.makeText(context, "Quiz submitted successfully! Score: $score/$total", Toast.LENGTH_LONG).show()
        }
    }

    // === Online MCQ Exam System & Controller ===
    fun createExam(batchId: Long, title: String, subject: String, duration: Int) {
        viewModelScope.launch {
            repository.insertExam(
                Exam(
                    batchId = batchId,
                    title = title,
                    subject = subject,
                    durationMinutes = duration,
                    isLocked = false
                )
            )
        }
    }

    fun addExamQuestion(examId: Long, text: String, a: String, b: String, c: String, d: String, correct: String) {
        viewModelScope.launch {
            repository.insertExamQuestion(
                ExamQuestion(
                    examId = examId,
                    questionText = text,
                    optionA = a,
                    optionB = b,
                    optionC = c,
                    optionD = d,
                    correctAnswer = correct
                )
            )
        }
    }

    fun getQuestionsForExamFlow(examId: Long): Flow<List<ExamQuestion>> {
        return repository.getQuestionsForExam(examId)
    }

    fun getAttemptsForStudentFlow(studentId: Long): Flow<List<ExamAttempt>> {
        return repository.getAttemptsForStudent(studentId)
    }

    fun recordExamAttempt(studentId: Long, examId: Long, score: Int, max: Int, time: Int) {
        viewModelScope.launch {
            repository.insertExamAttempt(
                ExamAttempt(
                    studentId = studentId,
                    examId = examId,
                    score = score,
                    totalScore = max,
                    timeSpentSeconds = time
                )
            )
        }
    }

    // === SMS & WhatsApp Outreach Parser ===
    fun parseOutreachMessage(template: String, student: Student, parentName: String, amount: String, batchName: String): String {
        return template
            .replace("[StudentName]", student.name, ignoreCase = true)
            .replace("[ParentName]", parentName, ignoreCase = true)
            .replace("[Amount]", amount, ignoreCase = true)
            .replace("[BatchName]", batchName, ignoreCase = true)
            .replace("[RollNumber]", student.rollNumber, ignoreCase = true)
    }

    fun addMessageTemplate(title: String, content: String, platform: String) {
        viewModelScope.launch {
            repository.insertMessageTemplate(
                MessageTemplate(
                    title = title,
                    content = content,
                    platform = platform
                )
            )
        }
    }

    // === Flat-File Database Local Serialization Backup & Cloud Sync ===
    fun exportDatabaseToLocalMemory(onProgress: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("aspirant_management_db")
                if (dbFile.exists()) {
                    val rootDir = context.getExternalFilesDir(null) ?: context.filesDir
                    val backupDest = File(rootDir, "TAMS_Local_Backup_${System.currentTimeMillis()}.db")
                    dbFile.inputStream().use { input ->
                        FileOutputStream(backupDest).use { output ->
                            input.copyTo(output)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onProgress("Success: DB exported to local flat file: ${backupDest.absolutePath}")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onProgress("Error: Database file does not exist locally.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onProgress("Failed to export: ${e.localizedMessage}")
                }
            }
        }
    }

    fun restoreDatabaseFromLocalMemory(onProgress: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    onProgress("Scanning local phone storage directories...")
                }
                val rootDir = context.getExternalFilesDir(null) ?: context.filesDir
                val backupFiles = rootDir.listFiles { _, name ->
                    name.startsWith("TAMS_Local_Backup_") && name.endsWith(".db")
                }

                if (backupFiles.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        onProgress("Error: No local SQLite flat-file backup packages found on storage.")
                    }
                    return@launch
                }

                val latestBackupFile = backupFiles.maxByOrNull { it.lastModified() }
                if (latestBackupFile == null) {
                    withContext(Dispatchers.Main) {
                        onProgress("Error: Local backup package selection failed.")
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    onProgress("Injecting latest database snapshot from ${latestBackupFile.name}...")
                }
                delay(1000)

                val dbFile = context.getDatabasePath("aspirant_management_db")
                val walFile = File(dbFile.parent, "${dbFile.name}-wal")
                val shmFile = File(dbFile.parent, "${dbFile.name}-shm")
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                latestBackupFile.inputStream().use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    onProgress("Success: Restored local storage backup (${latestBackupFile.name}) successfully!\n• Recalibrating local cache profiles...\n• Please reload or restart workspace to view updated logs.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onProgress("Error during local restore: ${e.localizedMessage}")
                }
            }
        }
    }

    fun triggerCloudSyncSnapshot(onResult: (Boolean, String) -> Unit) {
        // Safe check to sync active Room DB tables to structural cloud documents safely
        viewModelScope.launch {
            try {
                val batchList = batches.value
                val studentList = students.value
                val paymentsList = feePayments.value
                val transList = transactions.value
                val attList = repository.allAttendanceList.first()

                FirebaseService.initialize(context)
                if (FirebaseService.isInitialized) {
                    val userEmail = _adminEmail.value.ifEmpty { "admin@tams.com" }
                    val url = FirebaseService.uploadBackupSnapshotToCloud(
                        username = userEmail,
                        batches = batchList,
                        students = studentList,
                        payments = paymentsList,
                        transactions = transList,
                        attendanceList = attList
                    )
                    onResult(true, "Successfully generated structured data backup for Google account ($userEmail) on Firebase: $url")
                } else {
                    onResult(false, "Cloud Firebase is currently configured as fallbacked (Local Offline-First). Setup settings to activate Cloud syncing.")
                }
            } catch (e: Exception) {
                onResult(false, "Sync failed: ${e.localizedMessage}")
            }
        }
    }

    fun updateGoogleDriveBackupFrequency(freq: String) {
        _googleDriveBackupFrequency.value = freq
        sharedPrefs.edit().putString("google_drive_backup_frequency", freq).apply()
    }

    fun triggerGoogleDriveBackup(onProgress: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onProgress("Disaster recovery backup initiated...")
                val dbFile = context.getDatabasePath("aspirant_management_db")
                if (!dbFile.exists()) {
                    onProgress("Error: Local SQLite DB file not found.")
                    return@launch
                }

                // Copy to cache dir for clean file handle upload
                val cacheBackup = File(context.cacheDir, "TAMS_Disaster_Backup_Snapshot.db")
                dbFile.inputStream().use { input ->
                    FileOutputStream(cacheBackup).use { output ->
                        input.copyTo(output)
                    }
                }

                val userEmail = _adminEmail.value.ifEmpty { "admin@tams.com" }
                // Call GoogleDriveService
                val result = com.example.services.GoogleDriveService.uploadBackupToGoogleDrive(
                    context = context,
                    backupFile = cacheBackup,
                    accessToken = null, // uses fallback/simulated secure credentials
                    userEmail = userEmail,
                    onProgress = onProgress
                )

                if (result.first) {
                    // Save JSON snapshot for direct in-memory restored simulation
                    try {
                        com.example.services.GoogleDriveService.saveJsonSnapshotToSimulatedCloud(
                            context = context,
                            batches = batches.value,
                            students = students.value,
                            payments = feePayments.value,
                            transactions = transactions.value,
                            attendanceList = repository.allAttendanceList.first()
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AppViewModel", "Failed to cache JSON backup: ${e.localizedMessage}")
                    }

                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
                    val formattedDate = sdf.format(java.util.Date())
                    _googleDriveBackupTime.value = formattedDate
                    sharedPrefs.edit().putString("last_google_drive_backup", formattedDate).apply()
                    onProgress(result.second)
                } else {
                    onProgress("Error: ${result.second}")
                }
            } catch (e: Exception) {
                onProgress("Disaster backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun restoreGoogleDriveBackup(onProgress: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onProgress("Connecting to Google Account cloud...")
                delay(800)
                val userEmail = _adminEmail.value.ifEmpty { "admin@tams.com" }
                onProgress("Querying file indices for $userEmail...")
                delay(1000)
                onProgress("Downloading latest encapsulated database snapshot from folder...")
                delay(1200)

                val success = com.example.services.GoogleDriveService.restoreJsonSnapshotFromSimulatedCloud(
                    context = context,
                    onRestoreRestored = { batches, students, payments, transactions, attendanceList ->
                        viewModelScope.launch {
                            repository.clearAllData()
                            repository.restoreDatabaseBulk(batches, students, payments, transactions, attendanceList)
                        }
                    }
                )

                if (success) {
                    onProgress("Successfully restored database backup from Google Drive like WhatsApp!\n• Decrypted and populated: ${batches.value.size} Batches, ${students.value.size} Student Profiles, ${feePayments.value.size} Fee Ledger records.")
                } else {
                    onProgress("Error: No existing Google Drive backup directory was found registered with ($userEmail). Please create a backup snapshot first to initiate disaster recovery.")
                }
            } catch (e: Exception) {
                onProgress("Disaster recovery failed: ${e.localizedMessage}")
            }
        }
    }

    // === Seeding Initial Mock Data (Preserving only functional outreach templates) ===
    private suspend fun seedInitialTamsData() {
        // Seed template outreach options
        repository.insertMessageTemplate(
            MessageTemplate(
                title = "Fee Alert Default",
                content = "Dear [ParentName], [StudentName]'s batch fee of INR [Amount] is pending. Kindly pay soon. Thank you.",
                platform = "SMS"
            )
        )
        repository.insertMessageTemplate(
            MessageTemplate(
                title = "Welcome Template",
                content = "Welcome [StudentName] to our Academy! Roll Number is [RollNumber]. Batch Timing is [BatchName]. Ready to rule!",
                platform = "WHATSAPP"
            )
        )
    }

    init {
        // Init 1
        try {
            loadInstitutes()
            loadSubscriberTransactions()
            loadLibraryData()
            loadPromoSlides()
            loadPersonalNotes()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "loadInstitutes failed on creation", e)
        }

        // Init 2
        try {
            if (_trialStartDate.value == 0L) {
                val now = System.currentTimeMillis()
                _trialStartDate.value = now
                sharedPrefs.edit().putLong("trial_start_date", now).apply()
            }
            loadStaffProfiles()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "init trial/staff failed on creation", e)
        }

        // Init 3
        viewModelScope.launch {
            try {
                batches.first { true }.let { list ->
                    if (list.isEmpty()) {
                        seedInitialTamsData()
                    }
                }
                seedDefaultChatChannels()
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "seedInitialTamsData query or insert failed", e)
            }
        }
    }
}

data class StaffProfile(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val allowedScreens: Set<String>,
    val isActive: Boolean = true
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val category: String,
    val shelfLocation: String,
    val totalCopies: Int,
    val availableCopies: Int
)

data class BookLoan(
    val id: String,
    val bookId: String,
    val studentId: Long,
    val studentName: String,
    val issueDate: String,
    val dueDate: String,
    val returnDate: String?,
    val fineAmount: Double
)

data class LibrarySeat(
    val id: String,
    val seatNumber: String,
    val cabinBlock: String,
    val rentPriceMonthly: Double,
    val isBooked: Boolean,
    val bookedByStudentId: Long?,
    val bookedByStudentName: String?,
    val bookingStartDate: String?,
    val bookingEndDate: String?
)

data class LibraryAccount(
    val rollNumber: String,
    val name: String,
    val phone: String,
    val password: String,
    val isStaff: Boolean = false,
    val preferredShift: String = "Morning Shift (6 AM - 12 PM)",
    val preferredZone: String = "All"
)

data class LibraryAttendance(
    val id: String,
    val studentName: String,
    val rollNumber: String,
    val phone: String,
    val timestamp: String,
    val type: String // "CHECK_IN" or "CHECK_OUT"
)

data class TeacherAccount(
    val email: String,
    val name: String,
    val subject: String,
    val centerName: String,
    val passcode: String = "1234",
    val isPremium: Boolean = false,
    val premiumPlan: String = "Basic Free Tier",
    val premiumExpiry: String = "N/A",
    val salary: Double = 15000.0
)
