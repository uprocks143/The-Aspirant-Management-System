package com.example.services

import android.content.Context
import android.util.Log
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var firebaseProjectId = "1039749549760" // Defaulting to the user's project
    var firebaseStorageBucket = ""

    /**
     * Saves Firebase credentials to SharedPreferences and reinitializes.
     */
    fun saveFirebaseConfiguration(
        context: Context,
        apiKey: String,
        appId: String,
        projectId: String,
        storageBucket: String
    ) {
        val sharedPrefs = context.getSharedPreferences("tams_settings", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("fb_api_key", apiKey.trim())
            .putString("fb_app_id", appId.trim())
            .putString("fb_project_id", projectId.trim())
            .putString("fb_storage_bucket", storageBucket.trim())
            .apply()
        
        // Reset initialization so it can be re-run
        isInitialized = false
        initialize(context)
    }

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
            val existingApps = FirebaseApp.getApps(context.applicationContext)
            if (existingApps.isNotEmpty()) {
                try {
                    auth = FirebaseAuth.getInstance()
                } catch (t: Throwable) {
                    Log.w(TAG, "FirebaseAuth initialization fallback: ${t.localizedMessage}")
                }
                try {
                    db = FirebaseFirestore.getInstance()
                } catch (t: Throwable) {
                    Log.w(TAG, "FirebaseFirestore initialization fallback: ${t.localizedMessage}")
                }
                try {
                    storage = FirebaseStorage.getInstance()
                } catch (t: Throwable) {
                    Log.w(TAG, "FirebaseStorage initialization fallback: ${t.localizedMessage}")
                }
                isInitialized = (db != null || auth != null)
                Log.d(TAG, "Firebase linked successfully via existing app target. isInitialized=$isInitialized")
                return isInitialized
            }

            // Fallback manual configurations with SharedPreferences fallback
            val sharedPrefs = context.getSharedPreferences("tams_settings", Context.MODE_PRIVATE)
            val spApiKey = sharedPrefs.getString("fb_api_key", "") ?: ""
            val spAppId = sharedPrefs.getString("fb_app_id", "") ?: ""
            val spProjectId = sharedPrefs.getString("fb_project_id", "1039749549760") ?: "1039749549760"
            val spStorageBucket = sharedPrefs.getString("fb_storage_bucket", "") ?: ""

            val key = apiKey.ifEmpty { spApiKey.ifEmpty { firebaseApiKey } }
            val appIdStr = appId.ifEmpty { spAppId.ifEmpty { firebaseAppId } }
            val projectStr = projectId.ifEmpty { spProjectId.ifEmpty { firebaseProjectId } }
            val bucketStr = storageBucket.ifEmpty { spStorageBucket.ifEmpty { firebaseStorageBucket } }

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
            Log.w(TAG, "Firebase sign-up info: ${e.localizedMessage}")
            false
        }
    }

    suspend fun firebaseLogin(emailStr: String, passwordStr: String): Boolean {
        if (!isInitialized) return false
        return try {
            auth?.signInWithEmailAndPassword(emailStr, passwordStr)?.await()
            Log.d(TAG, "Firebase User signed in: $emailStr")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase sign-in info: ${e.localizedMessage}")
            false
        }
    }

    suspend fun firebaseLoginWithGoogle(idToken: String): Boolean {
        if (!isInitialized) return false
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth?.signInWithCredential(credential)?.await()
            Log.d(TAG, "Firebase logged in with Google credential successfully.")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Google login info: ${e.localizedMessage}")
            false
        }
    }

    suspend fun firebaseResetPassword(emailStr: String): Boolean {
        if (!isInitialized) return false
        return try {
            auth?.sendPasswordResetEmail(emailStr)?.await()
            Log.d(TAG, "Firebase reset email sent to $emailStr")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase password reset info: ${e.localizedMessage}")
            false
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
                "rollNumber" to student.rollNumber,
                "parentName" to student.parentName,
                "dateOfBirth" to student.dateOfBirth,
                "address" to student.address,
                "phone" to student.phone,
                "parentPhone" to student.parentPhone,
                "gender" to student.gender,
                "startDate" to student.startDate,
                "studentClass" to student.studentClass,
                "subject" to student.subject,
                "schoolCollegeName" to student.schoolCollegeName,
                "familyId" to student.familyId,
                "isAlumni" to student.isAlumni,
                "enrollmentStatus" to student.enrollmentStatus,
                "customField1" to student.customField1,
                "customField2" to student.customField2,
                "customField3" to student.customField3,
                "profilePhotoPath" to (student.profilePhotoPath ?: ""),
                "documentAttachments" to (student.documentAttachments ?: ""),
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("students")?.document(student.id.toString())?.set(studentMap)?.await()
            Log.d(TAG, "Student ${student.name} synced to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing student to cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Upload register / admin user accounts (institutes) to Firestore col "institutes"
     */
    suspend fun syncInstituteToCloud(inst: com.example.ui.viewmodel.InstituteAccount) {
        if (!isInitialized) return
        try {
            val instMap = hashMapOf(
                "academyName" to inst.academyName,
                "directorName" to inst.directorName,
                "email" to inst.email,
                "address" to inst.address,
                "phone" to inst.phone,
                "isApproved" to inst.isApproved,
                "subscriptionActive" to inst.subscriptionActive,
                "expiryDate" to inst.expiryDate,
                "lastPaymentAmount" to inst.lastPaymentAmount,
                "lastUpiTxRef" to inst.lastUpiTxRef,
                "profileType" to inst.profileType,
                "gstNumber" to inst.gstNumber,
                "businessProofId" to inst.businessProofId,
                "libraryCapacity" to inst.libraryCapacity,
                "libraryCategory" to inst.libraryCategory,
                "tuitionSubject" to inst.tuitionSubject,
                "tuitionStandard" to inst.tuitionStandard,
                "schoolCode" to inst.schoolCode,
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("institutes")?.document(inst.email)?.set(instMap)?.await()
            Log.d(TAG, "Institute ${inst.academyName} (${inst.email}) synced to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing institute to cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Retrieve a registered institute's account details from Firestore col "institutes"
     */
    suspend fun fetchInstituteFromCloud(email: String): com.example.ui.viewmodel.InstituteAccount? {
        if (!isInitialized) return null
        return try {
            val doc = db?.collection("institutes")?.document(email)?.get()?.await()
            if (doc != null && doc.exists()) {
                com.example.ui.viewmodel.InstituteAccount(
                    academyName = doc.getString("academyName") ?: "TAMS Academy",
                    directorName = doc.getString("directorName") ?: "Admin Director",
                    email = doc.getString("email") ?: email,
                    address = doc.getString("address") ?: "Main Campus",
                    phone = doc.getString("phone") ?: "",
                    isApproved = doc.getBoolean("isApproved") ?: true,
                    subscriptionActive = doc.getBoolean("subscriptionActive") ?: true,
                    expiryDate = doc.getString("expiryDate") ?: "2027-06-30",
                    lastPaymentAmount = doc.getDouble("lastPaymentAmount") ?: 0.0,
                    lastUpiTxRef = doc.getString("lastUpiTxRef") ?: "",
                    profileType = doc.getString("profileType") ?: "COACHING_OWNER",
                    gstNumber = doc.getString("gstNumber") ?: "",
                    businessProofId = doc.getString("businessProofId") ?: "",
                    libraryCapacity = doc.getString("libraryCapacity") ?: "",
                    libraryCategory = doc.getString("libraryCategory") ?: "",
                    tuitionSubject = doc.getString("tuitionSubject") ?: "",
                    tuitionStandard = doc.getString("tuitionStandard") ?: "",
                    schoolCode = doc.getString("schoolCode") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching institute: ${e.localizedMessage}")
            null
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

    /**
     * Upload financial transactions to Firestore col "transactions"
     */
    suspend fun syncTransactionToCloud(transaction: FinancialTransaction) {
        if (!isInitialized) return
        try {
            val transMap = hashMapOf(
                "id" to transaction.id,
                "type" to transaction.type,
                "amount" to transaction.amount,
                "category" to transaction.category,
                "description" to transaction.description,
                "date" to transaction.date,
                "lastSynced" to System.currentTimeMillis()
            )
            db?.collection("transactions")?.document(transaction.id.toString())?.set(transMap)?.await()
            Log.d(TAG, "FinancialTransaction #${transaction.id} synced to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing financial transaction: ${e.localizedMessage}")
        }
    }

    suspend fun deleteBatchFromCloud(batchId: Long) {
        if (!isInitialized) return
        try {
            db?.collection("batches")?.document(batchId.toString())?.delete()?.await()
            Log.d(TAG, "Batch $batchId deleted from Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting batch from cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteStudentFromCloud(studentId: Long) {
        if (!isInitialized) return
        try {
            db?.collection("students")?.document(studentId.toString())?.delete()?.await()
            Log.d(TAG, "Student $studentId deleted from Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting student from cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteFeePaymentFromCloud(paymentId: Long) {
        if (!isInitialized) return
        try {
            db?.collection("fees")?.document(paymentId.toString())?.delete()?.await()
            Log.d(TAG, "Fee payment $paymentId deleted from Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fee payment from cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteTransactionFromCloud(transactionId: Long) {
        if (!isInitialized) return
        try {
            db?.collection("transactions")?.document(transactionId.toString())?.delete()?.await()
            Log.d(TAG, "Transaction $transactionId deleted from Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction from cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteAttendanceFromCloud(studentId: Long, dateString: String) {
        if (!isInitialized) return
        try {
            val docId = "${studentId}_${dateString}"
            db?.collection("attendance")?.document(docId)?.delete()?.await()
            Log.d(TAG, "Attendance for $studentId on $dateString deleted from Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting attendance from cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Attaches real-time listeners to Firestore collections ("batches", "students", "fees", "transactions", "attendance")
     * to keep the local Room database automatically updated in real-time when remote updates occur.
     */
    fun startRealtimeFirestoreSync(
        scope: kotlinx.coroutines.CoroutineScope,
        appDao: com.example.data.dao.AppDao
    ): List<com.google.firebase.firestore.ListenerRegistration> {
        if (!isInitialized || db == null) return emptyList()

        val registrations = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

        // 1. Batches real-time listener
        db?.collection("batches")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Real-time batches listener error: ${error.localizedMessage}")
                return@addSnapshotListener
            }
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val batchId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@forEach
                val batch = Batch(
                    id = batchId,
                    name = doc.getString("name") ?: "",
                    subject = doc.getString("subject") ?: "Mathematics",
                    classTimings = doc.getString("classTimings") ?: "04:00 PM - 05:00 PM",
                    feesAmount = doc.getDouble("feesAmount") ?: 1000.0,
                    daysOfWeek = doc.getString("daysOfWeek") ?: "Mon, Wed, Fri"
                )
                scope.launch(Dispatchers.IO) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> appDao.insertBatch(batch)
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> appDao.deleteBatch(batch)
                    }
                }
            }
        }?.let { registrations.add(it) }

        // 2. Students real-time listener
        db?.collection("students")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Real-time students listener error: ${error.localizedMessage}")
                return@addSnapshotListener
            }
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val studentId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@forEach
                val student = Student(
                    id = studentId,
                    batchId = doc.getLong("batchId") ?: 0L,
                    name = doc.getString("name") ?: "",
                    address = doc.getString("address") ?: "",
                    phone = doc.getString("phone") ?: "",
                    parentPhone = doc.getString("parentPhone") ?: "",
                    startDate = doc.getString("startDate") ?: "",
                    rollNumber = doc.getString("rollNumber") ?: "R-${doc.id}",
                    parentName = doc.getString("parentName") ?: "Guardian",
                    dateOfBirth = doc.getString("dateOfBirth") ?: "2000-01-01",
                    gender = doc.getString("gender") ?: "Male",
                    studentClass = doc.getString("studentClass") ?: "",
                    subject = doc.getString("subject") ?: "",
                    schoolCollegeName = doc.getString("schoolCollegeName") ?: "",
                    familyId = doc.getString("familyId") ?: "",
                    isAlumni = doc.getBoolean("isAlumni") ?: false,
                    enrollmentStatus = doc.getString("enrollmentStatus") ?: "Active",
                    customField1 = doc.getString("customField1") ?: "",
                    customField2 = doc.getString("customField2") ?: "",
                    customField3 = doc.getString("customField3") ?: "",
                    profilePhotoPath = doc.getString("profilePhotoPath")?.ifEmpty { null },
                    documentAttachments = doc.getString("documentAttachments")?.ifEmpty { null }
                )
                scope.launch(Dispatchers.IO) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> appDao.insertStudent(student)
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> appDao.deleteStudent(student)
                    }
                }
            }
        }?.let { registrations.add(it) }

        // 3. Fee Payments real-time listener
        db?.collection("fees")?.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val feeId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@forEach
                val payment = FeePayment(
                    id = feeId,
                    studentId = doc.getLong("studentId") ?: 0L,
                    feeType = doc.getString("feeType") ?: "Monthly-based",
                    amountPaid = doc.getDouble("amountPaid") ?: 0.0,
                    discount = doc.getDouble("discount") ?: 0.0,
                    datePaid = doc.getLong("datePaid") ?: System.currentTimeMillis(),
                    receiptNo = doc.getString("receiptNo") ?: "",
                    monthPaidFor = doc.getString("monthPaidFor") ?: "",
                    paymentMode = doc.getString("paymentMode") ?: "CASH",
                    remarks = doc.getString("remarks") ?: "",
                    transactionStatus = doc.getString("transactionStatus") ?: "SUCCESS"
                )
                scope.launch(Dispatchers.IO) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> appDao.insertFeePayment(payment)
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> appDao.deleteFeePayment(payment)
                    }
                }
            }
        }?.let { registrations.add(it) }

        // 4. Financial Transactions real-time listener
        db?.collection("transactions")?.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val transId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: return@forEach
                val trans = FinancialTransaction(
                    id = transId,
                    type = doc.getString("type") ?: "EXPENSE",
                    amount = doc.getDouble("amount") ?: 0.0,
                    category = doc.getString("category") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: System.currentTimeMillis()
                )
                scope.launch(Dispatchers.IO) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> appDao.insertTransaction(trans)
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> appDao.deleteTransaction(trans)
                    }
                }
            }
        }?.let { registrations.add(it) }

        // 5. Attendance real-time listener
        db?.collection("attendance")?.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.documentChanges?.forEach { change ->
                val doc = change.document
                val studentId = doc.getLong("studentId") ?: 0L
                val dateStr = doc.getString("dateString") ?: ""
                val att = Attendance(
                    id = doc.getLong("id") ?: 0L,
                    studentId = studentId,
                    batchId = doc.getLong("batchId") ?: 0L,
                    dateString = dateStr,
                    status = doc.getString("status") ?: "Present",
                    trackingMethod = doc.getString("trackingMethod") ?: "Manual",
                    checkInTime = doc.getString("checkInTime") ?: ""
                )
                scope.launch(Dispatchers.IO) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED,
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> appDao.insertAttendance(att)
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            if (studentId > 0 && dateStr.isNotEmpty()) {
                                appDao.deleteAttendance(studentId, dateStr)
                            }
                        }
                    }
                }
            }
        }?.let { registrations.add(it) }

        return registrations
    }

    /**
     * Performs direct individual Firestore record sync of all local tables
     */
    suspend fun syncAllRecordsToFirestore(
        batches: List<Batch>,
        students: List<Student>,
        payments: List<FeePayment>,
        transactions: List<FinancialTransaction>,
        attendanceList: List<Attendance>
    ): Int {
        if (!isInitialized) return 0
        var count = 0
        try {
            for (batch in batches) {
                syncBatchToCloud(batch)
                count++
            }
            for (student in students) {
                syncStudentToCloud(student)
                count++
            }
            for (payment in payments) {
                syncFeePaymentToCloud(payment)
                count++
            }
            for (transaction in transactions) {
                syncTransactionToCloud(transaction)
                count++
            }
            for (attendance in attendanceList) {
                syncAttendanceToCloud(attendance)
                count++
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during direct Firestore collection sync: ${e.localizedMessage}")
        }
        return count
    }

    /**
     * Downloads and parses all documents from Firestore collections to merge with local Room cache.
     */
    suspend fun pullAllFromFirestore(): FirestoreSyncPayload? {
        if (!isInitialized) return null
        return try {
            val batchesList = mutableListOf<Batch>()
            val studentsList = mutableListOf<Student>()
            val paymentsList = mutableListOf<FeePayment>()
            val transList = mutableListOf<FinancialTransaction>()
            val attList = mutableListOf<Attendance>()

            // 1. Pull Batches
            val batchesSnap = db?.collection("batches")?.get()?.await()
            batchesSnap?.documents?.forEach { doc ->
                try {
                    batchesList.add(
                        Batch(
                            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                            name = doc.getString("name") ?: "",
                            subject = doc.getString("subject") ?: "Mathematics",
                            classTimings = doc.getString("classTimings") ?: "04:00 PM - 05:00 PM",
                            feesAmount = doc.getDouble("feesAmount") ?: 1000.0,
                            daysOfWeek = doc.getString("daysOfWeek") ?: "Mon, Wed, Fri"
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing batch: ${e.localizedMessage}")
                }
            }

            // 2. Pull Students
            val studentsSnap = db?.collection("students")?.get()?.await()
            studentsSnap?.documents?.forEach { doc ->
                try {
                    studentsList.add(
                        Student(
                            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                            batchId = doc.getLong("batchId") ?: 0L,
                            name = doc.getString("name") ?: "",
                            address = doc.getString("address") ?: "",
                            phone = doc.getString("phone") ?: "",
                            parentPhone = doc.getString("parentPhone") ?: "",
                            startDate = doc.getString("startDate") ?: "",
                            rollNumber = doc.getString("rollNumber") ?: "R-${doc.id}",
                            parentName = doc.getString("parentName") ?: "Guardian",
                            dateOfBirth = doc.getString("dateOfBirth") ?: "2000-01-01",
                            isAlumni = doc.getBoolean("isAlumni") ?: false
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing student: ${e.localizedMessage}")
                }
            }

            // 3. Pull Payments
            val paymentsSnap = db?.collection("fees")?.get()?.await()
            paymentsSnap?.documents?.forEach { doc ->
                try {
                    paymentsList.add(
                        FeePayment(
                            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                            studentId = doc.getLong("studentId") ?: 0L,
                            feeType = doc.getString("feeType") ?: "Monthly-based",
                            amountPaid = doc.getDouble("amountPaid") ?: 0.0,
                            discount = doc.getDouble("discount") ?: 0.0,
                            datePaid = doc.getLong("datePaid") ?: System.currentTimeMillis(),
                            receiptNo = doc.getString("receiptNo") ?: "",
                            monthPaidFor = doc.getString("monthPaidFor") ?: "",
                            paymentMode = doc.getString("paymentMode") ?: "CASH",
                            remarks = doc.getString("remarks") ?: "",
                            transactionStatus = doc.getString("transactionStatus") ?: "SUCCESS"
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing payment: ${e.localizedMessage}")
                }
            }

            // 4. Pull Transactions
            val transSnap = db?.collection("transactions")?.get()?.await()
            transSnap?.documents?.forEach { doc ->
                try {
                    transList.add(
                        FinancialTransaction(
                            id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: 0L,
                            type = doc.getString("type") ?: "EXPENSE",
                            amount = doc.getDouble("amount") ?: 0.0,
                            category = doc.getString("category") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getLong("date") ?: System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing transaction: ${e.localizedMessage}")
                }
            }

            // 5. Pull Attendance
            val attSnap = db?.collection("attendance")?.get()?.await()
            attSnap?.documents?.forEach { doc ->
                try {
                    attList.add(
                        Attendance(
                            id = doc.getLong("id") ?: 0L,
                            studentId = doc.getLong("studentId") ?: 0L,
                            batchId = doc.getLong("batchId") ?: 0L,
                            dateString = doc.getString("dateString") ?: "",
                            status = doc.getString("status") ?: "Present",
                            trackingMethod = doc.getString("trackingMethod") ?: "Manual",
                            checkInTime = doc.getString("checkInTime") ?: ""
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing attendance: ${e.localizedMessage}")
                }
            }

            FirestoreSyncPayload(batchesList, studentsList, paymentsList, transList, attList)
        } catch (e: Exception) {
            Log.e(TAG, "Failed downloading snapshot database: ${e.localizedMessage}")
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

    /**
     * Performs automated daily backup of local Room database tables to Firebase Firestore.
     * Records the backup under a daily document snapshot 'daily_backups/{yyyy-MM-dd}' as well as
     * syncing live collections to ensure data persistence across app resets/devices.
     */
    suspend fun performDailyRoomToFirestoreBackup(
        context: Context,
        batches: List<Batch>,
        students: List<Student>,
        payments: List<FeePayment>,
        transactions: List<FinancialTransaction>,
        attendanceList: List<Attendance>,
        force: Boolean = false
    ): Boolean {
        try {
            val prefs = context.getSharedPreferences("tams_settings", Context.MODE_PRIVATE)
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val lastBackupDate = prefs.getString("last_daily_firestore_backup_date", "") ?: ""

            if (!force && todayStr == lastBackupDate) {
                Log.d(TAG, "Daily Firestore backup already performed today ($todayStr). Skipping duplicate backup.")
                return true
            }

            if (!isInitialized) {
                initialize(context)
            }

            // 1. Sync live collections
            syncAllRecordsToFirestore(batches, students, payments, transactions, attendanceList)

            // 2. Store dated backup snapshot in Firestore 'daily_backups' collection
            val backupDocMap = hashMapOf<String, Any>(
                "backupDate" to todayStr,
                "timestamp" to System.currentTimeMillis(),
                "batchCount" to batches.size,
                "studentCount" to students.size,
                "paymentCount" to payments.size,
                "transactionCount" to transactions.size,
                "attendanceCount" to attendanceList.size
            )
            db?.collection("daily_backups")?.document(todayStr)?.set(backupDocMap)?.await()

            prefs.edit().putString("last_daily_firestore_backup_date", todayStr).apply()
            Log.d(TAG, "Automated Daily Firestore backup completed successfully for $todayStr!")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Daily Firestore backup failed: ${e.localizedMessage}")
            return false
        }
    }

    /**
     * Checks student pending dues using Firebase Firestore data and triggers local push notifications
     * when a student's pending dues reach or exceed the given threshold.
     */
    suspend fun checkFirestorePendingDuesAndNotify(context: Context, thresholdAmount: Double = 1000.0): Int = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize(context)
        }
        val currentDb = db ?: return@withContext 0
        var notificationsTriggered = 0

        try {
            // 1. Fetch Firestore collections: students, batches, fees
            val studentSnapshots = currentDb.collection("students").get().await()
            val batchSnapshots = currentDb.collection("batches").get().await()
            val feeSnapshots = currentDb.collection("fees").get().await()

            val batchFeesMap = mutableMapOf<Long, Double>()
            for (doc in batchSnapshots) {
                val bId = doc.getLong("id") ?: continue
                val feeAmt = doc.getDouble("feesAmount") ?: 0.0
                batchFeesMap[bId] = feeAmt
            }

            val studentPaidMap = mutableMapOf<Long, Double>()
            for (doc in feeSnapshots) {
                val sId = doc.getLong("studentId") ?: continue
                val amt = doc.getDouble("amountPaid") ?: 0.0
                val discount = doc.getDouble("discount") ?: 0.0
                val status = doc.getString("transactionStatus") ?: "SUCCESS"
                if (status.equals("SUCCESS", ignoreCase = true) || status.isEmpty()) {
                    studentPaidMap[sId] = (studentPaidMap[sId] ?: 0.0) + amt + discount
                }
            }

            for (doc in studentSnapshots) {
                val isAlumni = doc.getBoolean("isAlumni") ?: false
                if (isAlumni) continue

                val studentName = doc.getString("name") ?: "Student"
                val sId = doc.getLong("id") ?: continue
                val batchId = doc.getLong("batchId") ?: continue

                val batchFee = batchFeesMap[batchId] ?: 0.0
                val totalPaid = studentPaidMap[sId] ?: 0.0

                val pendingDues = batchFee - totalPaid
                if (pendingDues >= thresholdAmount) {
                    Log.d(TAG, "Student $studentName has Firestore pending dues ₹$pendingDues reaching threshold ₹$thresholdAmount")
                    NotificationHelper.triggerDuesThresholdNotification(
                        context = context,
                        studentName = studentName,
                        totalPending = pendingDues,
                        threshold = thresholdAmount
                    )
                    notificationsTriggered++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Firestore pending dues threshold: ${e.localizedMessage}")
        }
        return@withContext notificationsTriggered
    }

    /**
     * Uploads digital study material PDF / image file to Firebase Storage under study_materials/{grade}/{subject}/
     * and returns the public Firebase Storage download URL string.
     */
    suspend fun uploadStudyMaterialToFirebaseStorage(
        context: Context,
        fileUri: android.net.Uri,
        fileName: String,
        grade: String,
        subject: String
    ): String? = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize(context)
        }
        val currentStorage = storage ?: return@withContext null
        return@withContext try {
            val sanitizeGrade = grade.replace(" ", "_")
            val sanitizeSubject = subject.replace(" ", "_")
            val timestamp = System.currentTimeMillis()
            val storagePath = "study_materials/${sanitizeGrade}/${sanitizeSubject}/${timestamp}_${fileName}"
            
            val storageRef = currentStorage.reference.child(storagePath)
            
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return@withContext null
            val uploadTask = storageRef.putStream(inputStream).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "Study material file uploaded to Firebase Storage successfully: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage study material upload failed: ${e.localizedMessage}")
            null
        }
    }
}

data class FirestoreSyncPayload(
    val batches: List<Batch>,
    val students: List<Student>,
    val payments: List<FeePayment>,
    val transactions: List<FinancialTransaction>,
    val attendanceList: List<Attendance>
)
