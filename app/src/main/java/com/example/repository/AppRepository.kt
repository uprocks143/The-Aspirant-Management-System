package com.example.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import com.example.services.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppRepository(private val appDao: AppDao) {

    /**
     * Start real-time Firestore listeners to sync cloud changes directly into local Room database.
     */
    fun startRealtimeFirestoreSync(scope: CoroutineScope) {
        FirebaseService.startRealtimeFirestoreSync(scope, appDao)
    }

    // === Batches ===
    val allBatches: Flow<List<Batch>> = appDao.getAllBatches()

    suspend fun insertBatch(batch: Batch): Long {
        val id = appDao.insertBatch(batch)
        val batchToSync = if (batch.id == 0L) batch.copy(id = id) else batch
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncBatchToCloud(batchToSync)
        }
        return id
    }

    suspend fun updateBatch(batch: Batch) {
        appDao.updateBatch(batch)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncBatchToCloud(batch)
        }
    }

    suspend fun deleteBatch(batch: Batch) {
        appDao.deleteBatch(batch)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.deleteBatchFromCloud(batch.id)
        }
    }

    // === Students ===
    val allStudents: Flow<List<Student>> = appDao.getAllStudents()

    fun getStudentsByBatch(batchId: Long): Flow<List<Student>> = appDao.getStudentsByBatch(batchId)
    suspend fun getStudentById(id: Long): Student? = appDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long {
        val id = appDao.insertStudent(student)
        val studentToSync = if (student.id == 0L) student.copy(id = id) else student
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncStudentToCloud(studentToSync)
        }
        return id
    }

    suspend fun updateStudent(student: Student) {
        appDao.updateStudent(student)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncStudentToCloud(student)
        }
    }

    suspend fun deleteStudent(student: Student) {
        appDao.deleteStudent(student)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.deleteStudentFromCloud(student.id)
        }
    }

    // === Attendance ===
    fun getAttendanceByDate(dateString: String): Flow<List<Attendance>> = appDao.getAttendanceByDate(dateString)
    fun getAttendanceForBatchAndDate(batchId: Long, dateString: String): Flow<List<Attendance>> = 
        appDao.getAttendanceForBatchAndDate(batchId, dateString)

    suspend fun insertAttendance(attendance: Attendance) {
        appDao.insertAttendance(attendance)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncAttendanceToCloud(attendance)
        }
    }

    suspend fun deleteAttendance(studentId: Long, dateString: String) {
        appDao.deleteAttendance(studentId, dateString)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.deleteAttendanceFromCloud(studentId, dateString)
        }
    }

    // === Fees Payments ===
    val allFeePayments: Flow<List<FeePayment>> = appDao.getAllFeePayments()
    fun getFeePaymentsForStudent(studentId: Long): Flow<List<FeePayment>> = appDao.getFeePaymentsForStudent(studentId)

    suspend fun insertFeePayment(feePayment: FeePayment): Long {
        val id = appDao.insertFeePayment(feePayment)
        val paymentToSync = if (feePayment.id == 0L) feePayment.copy(id = id) else feePayment
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncFeePaymentToCloud(paymentToSync)
        }
        return id
    }

    suspend fun deleteFeePayment(feePayment: FeePayment) {
        appDao.deleteFeePayment(feePayment)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.deleteFeePaymentFromCloud(feePayment.id)
        }
    }

    // === Payment Logs (Room-based payment logging) ===
    val allPaymentLogs: Flow<List<PaymentLog>> = appDao.getAllPaymentLogs()
    fun getPaymentLogsForStudent(studentId: Long): Flow<List<PaymentLog>> = appDao.getPaymentLogsForStudent(studentId)

    suspend fun insertPaymentLog(paymentLog: PaymentLog): Long {
        return appDao.insertPaymentLog(paymentLog)
    }

    suspend fun deletePaymentLog(paymentLog: PaymentLog) {
        appDao.deletePaymentLog(paymentLog)
    }

    suspend fun deletePaymentLogsForStudent(studentId: Long) {
        appDao.deletePaymentLogsForStudent(studentId)
    }

    // === Transactions ===
    val allTransactions: Flow<List<FinancialTransaction>> = appDao.getAllTransactions()

    suspend fun insertTransaction(transaction: FinancialTransaction): Long {
        val id = appDao.insertTransaction(transaction)
        val transactionToSync = if (transaction.id == 0L) transaction.copy(id = id) else transaction
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.syncTransactionToCloud(transactionToSync)
        }
        return id
    }

    suspend fun deleteTransaction(transaction: FinancialTransaction) {
        appDao.deleteTransaction(transaction)
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseService.deleteTransactionFromCloud(transaction.id)
        }
    }

    // === Study Materials ===
    val allStudyMaterials: Flow<List<StudyMaterial>> = appDao.getAllStudyMaterials()
    fun getStudyMaterialsForBatch(batchId: Long): Flow<List<StudyMaterial>> = appDao.getStudyMaterialsForBatch(batchId)
    suspend fun insertStudyMaterial(material: StudyMaterial): Long = appDao.insertStudyMaterial(material)
    suspend fun deleteStudyMaterial(material: StudyMaterial) = appDao.deleteStudyMaterial(material)

    // === Message Templates ===
    val allMessageTemplates: Flow<List<MessageTemplate>> = appDao.getAllMessageTemplates()
    suspend fun insertMessageTemplate(template: MessageTemplate): Long = appDao.insertMessageTemplate(template)
    suspend fun updateMessageTemplate(template: MessageTemplate) = appDao.updateMessageTemplate(template)
    suspend fun deleteMessageTemplate(template: MessageTemplate) = appDao.deleteMessageTemplate(template)

    // === Exams ===
    val allExams: Flow<List<Exam>> = appDao.getAllExams()
    fun getExamsForBatch(batchId: Long): Flow<List<Exam>> = appDao.getExamsForBatch(batchId)
    suspend fun insertExam(exam: Exam): Long = appDao.insertExam(exam)
    suspend fun deleteExam(exam: Exam) = appDao.deleteExam(exam)

    // === Exam Questions ===
    fun getQuestionsForExam(examId: Long): Flow<List<ExamQuestion>> = appDao.getQuestionsForExam(examId)
    suspend fun insertExamQuestion(question: ExamQuestion): Long = appDao.insertExamQuestion(question)
    suspend fun insertExamQuestionsBulk(questions: List<ExamQuestion>) = appDao.insertExamQuestionsBulk(questions)

    // === Exam Attempts ===
    fun getAttemptsForStudent(studentId: Long): Flow<List<ExamAttempt>> = appDao.getAttemptsForStudent(studentId)
    fun getAttemptsForExam(examId: Long): Flow<List<ExamAttempt>> = appDao.getAttemptsForExam(examId)
    suspend fun insertExamAttempt(attempt: ExamAttempt): Long = appDao.insertExamAttempt(attempt)

    // === Material Quizzes ===
    fun getQuizzesForMaterial(studyMaterialId: Long): Flow<List<MaterialQuiz>> = appDao.getQuizzesForMaterial(studyMaterialId)
    suspend fun insertMaterialQuiz(quiz: MaterialQuiz): Long = appDao.insertMaterialQuiz(quiz)
    suspend fun deleteMaterialQuiz(quiz: MaterialQuiz) = appDao.deleteMaterialQuiz(quiz)

    // === Material Quiz Questions ===
    fun getQuestionsForQuiz(quizId: Long): Flow<List<MaterialQuizQuestion>> = appDao.getQuestionsForQuiz(quizId)
    suspend fun insertMaterialQuizQuestion(question: MaterialQuizQuestion): Long = appDao.insertMaterialQuizQuestion(question)
    suspend fun insertMaterialQuizQuestionsBulk(questions: List<MaterialQuizQuestion>) = appDao.insertMaterialQuizQuestionsBulk(questions)

    // === Material Quiz Attempts ===
    fun getAttemptsForQuiz(quizId: Long): Flow<List<MaterialQuizAttempt>> = appDao.getAttemptsForQuiz(quizId)
    suspend fun insertMaterialQuizAttempt(attempt: MaterialQuizAttempt): Long = appDao.insertMaterialQuizAttempt(attempt)

    // === Cloud Backup & Restoration ===
    val allAttendanceList: Flow<List<Attendance>> = appDao.getAllAttendance()
    suspend fun clearAllData() {
        appDao.clearBatches()
        appDao.clearStudents()
        appDao.clearAttendance()
        appDao.clearFeePayments()
        appDao.clearTransactions()
    }
    suspend fun restoreDatabaseBulk(
        batches: List<Batch>,
        students: List<Student>,
        payments: List<FeePayment>,
        transactions: List<FinancialTransaction>,
        attendanceList: List<Attendance>
    ) {
        appDao.insertBatchesBulk(batches)
        appDao.insertStudentsBulk(students)
        appDao.insertFeePaymentsBulk(payments)
        appDao.insertTransactionsBulk(transactions)
        appDao.insertAttendanceBulk(attendanceList)
    }

    // === Community Chats ===
    val allChatChannels: Flow<List<ChatChannel>> = appDao.getAllChatChannels()
    
    suspend fun insertChatChannel(channel: ChatChannel): Long = appDao.insertChatChannel(channel)

    suspend fun getChatMessageById(messageId: Long): ChatMessage? = appDao.getChatMessageById(messageId)

    suspend fun getChatChannelById(channelId: Long): ChatChannel? = appDao.getChatChannelById(channelId)
    
    fun getMessagesForChannel(channelId: Long): Flow<List<ChatMessage>> = appDao.getMessagesForChannel(channelId)
    
    suspend fun insertChatMessage(message: ChatMessage): Long = appDao.insertChatMessage(message)
    
    suspend fun clearMessagesForChannel(channelId: Long) = appDao.clearMessagesForChannel(channelId)
    
    suspend fun deleteChatChannel(channelId: Long) = appDao.deleteChatChannel(channelId)

    // === Assignments ===
    val allAssignments: Flow<List<Assignment>> = appDao.getAllAssignments()
    fun getAssignmentsForBatch(batchId: Long): Flow<List<Assignment>> = appDao.getAssignmentsForBatch(batchId)
    suspend fun insertAssignment(assignment: Assignment): Long = appDao.insertAssignment(assignment)
    suspend fun deleteAssignment(assignment: Assignment) = appDao.deleteAssignment(assignment)
}
