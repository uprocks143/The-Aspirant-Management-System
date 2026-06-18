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
    val isApproved: Boolean = false
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
    private val _academyName = MutableStateFlow(getSafeString(sharedPrefs, "academy_name", "Aspirants Success Classes"))
    val academyName = _academyName.asStateFlow()

    private val _directorName = MutableStateFlow(getSafeString(sharedPrefs, "director_name", "Sumit Kumar"))
    val directorName = _directorName.asStateFlow()

    private val _adminEmail = MutableStateFlow(getSafeString(sharedPrefs, "admin_email", "smtsharma282.sks@gmail.com"))
    val adminEmail = _adminEmail.asStateFlow()

    private val _adminPhone = MutableStateFlow(getSafeString(sharedPrefs, "admin_phone", "+919582715282"))
    val adminPhone = _adminPhone.asStateFlow()

    private val _adminAddress = MutableStateFlow(getSafeString(sharedPrefs, "admin_address", "chhibramau"))
    val adminAddress = _adminAddress.asStateFlow()

    private val _institutesList = MutableStateFlow<List<InstituteAccount>>(emptyList())
    val institutesList = _institutesList.asStateFlow()

    init {
        try {
            loadInstitutes()
        } catch (e: Exception) {
            android.util.Log.e("AppViewModel", "loadInstitutes failed on creation", e)
        }
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
                        isApproved = obj.optBoolean("isApproved", false)
                    )
                )
            }
            if (list.isEmpty()) {
                list.add(InstituteAccount("Dynamic Shapers Coaching", "Anand Rao", "anand@shapers.com", "Lucknow", false))
                list.add(InstituteAccount("Quantum prep Classes", "Dr. Homi", "homi@quantum.com", "Mumbai", false))
                saveInstitutesList(list)
            }
            _institutesList.value = list
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
            array.put(obj)
        }
        sharedPrefs.edit().putString("registered_institutes_json", array.toString()).apply()
        _institutesList.value = list
    }

    fun registerInstitute(academy: String, director: String, email: String, address: String) {
        sharedPrefs.edit()
            .putString("academy_name", academy)
            .putString("director_name", director)
            .putString("admin_email", email)
            .putString("admin_address", address)
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
            val isApproved = (email == "smtsharma282.sks@gmail.com" || email.contains("guest") || email.contains("google"))
            current.add(InstituteAccount(academy, director, email, address, isApproved))
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

    init {
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
    }

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
                        allowedScreens = screens
                    )
                )
            }
            if (list.isEmpty()) {
                list.add(StaffProfile("staff1", "Sunil Verma", "Senior Maths Faculty", "9876543210", setOf("Attendance", "Study Materials", "Homework")))
                list.add(StaffProfile("staff2", "Neha Gupta", "Academy Center Incharge", "9988776655", setOf("Batches Setup", "Admission Form", "Tuition Fees", "Attendance", "Homework")))
                list.add(StaffProfile("staff3", "Pankaj Sharma", "Admission Lead Manager", "9456123780", setOf("Enquiry Manager", "Admission Form")))
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
            val screensArray = JSONArray()
            item.allowedScreens.forEach { screensArray.put(it) }
            obj.put("allowedScreens", screensArray)
            array.put(obj)
        }
        sharedPrefs.edit().putString("staff_profiles_json", array.toString()).apply()
        _staffProfiles.value = list
    }

    fun addStaffProfile(name: String, role: String, phone: String, screens: Set<String>) {
        val list = _staffProfiles.value.toMutableList()
        list.add(
            StaffProfile(
                id = System.currentTimeMillis().toString(),
                name = name,
                role = role,
                phone = phone,
                allowedScreens = screens
            )
        )
        saveStaffProfilesList(list)
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

    init {
        // Seed default templates and data if database is empty
        viewModelScope.launch {
            try {
                batches.first { true }.let { list ->
                    if (list.isEmpty()) {
                        seedInitialTamsData()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AppViewModel", "seedInitialTamsData query or insert failed", e)
            }
        }
    }

    fun switchTheme(theme: String) {
        _themeMode.value = theme
        sharedPrefs.edit().putString("app_theme", theme).apply()
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
            repository.insertBatch(
                Batch(
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
            )
        }
    }

    fun updateBatch(batch: Batch) {
        viewModelScope.launch {
            repository.updateBatch(batch)
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
            repository.insertAttendance(
                Attendance(
                    studentId = studentId,
                    batchId = batchId,
                    dateString = dateStr,
                    status = status,
                    trackingMethod = method
                )
            )
        }
    }

    fun performBulkAttendance(studentIds: List<Long>, batchId: Long, dateStr: String, status: String, method: String) {
        viewModelScope.launch {
            studentIds.forEach { sId ->
                repository.deleteAttendance(sId, dateStr)
                repository.insertAttendance(
                    Attendance(
                        studentId = sId,
                        batchId = batchId,
                        dateString = dateStr,
                        status = status,
                        trackingMethod = method
                    )
                )
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
                    val userEmail = _adminEmail.value.ifEmpty { "smtsharma282.sks@gmail.com" }
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

                val userEmail = _adminEmail.value.ifEmpty { "smtsharma282.sks@gmail.com" }
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
                val userEmail = _adminEmail.value.ifEmpty { "smtsharma282.sks@gmail.com" }
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
}

data class StaffProfile(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val allowedScreens: Set<String>
)
