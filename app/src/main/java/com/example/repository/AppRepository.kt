package com.example.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    // === Batches ===
    val allBatches: Flow<List<Batch>> = appDao.getAllBatches()

    suspend fun insertBatch(batch: Batch): Long = appDao.insertBatch(batch)
    suspend fun updateBatch(batch: Batch) = appDao.updateBatch(batch)
    suspend fun deleteBatch(batch: Batch) = appDao.deleteBatch(batch)

    // === Students ===
    val allStudents: Flow<List<Student>> = appDao.getAllStudents()

    fun getStudentsByBatch(batchId: Long): Flow<List<Student>> = appDao.getStudentsByBatch(batchId)
    suspend fun insertStudent(student: Student): Long = appDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = appDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = appDao.deleteStudent(student)

    // === Attendance ===
    fun getAttendanceByDate(dateString: String): Flow<List<Attendance>> = appDao.getAttendanceByDate(dateString)
    fun getAttendanceForBatchAndDate(batchId: Long, dateString: String): Flow<List<Attendance>> = 
        appDao.getAttendanceForBatchAndDate(batchId, dateString)

    suspend fun insertAttendance(attendance: Attendance) = appDao.insertAttendance(attendance)
    suspend fun deleteAttendance(studentId: Long, dateString: String) = appDao.deleteAttendance(studentId, dateString)

    // === Fees Payments ===
    val allFeePayments: Flow<List<FeePayment>> = appDao.getAllFeePayments()
    fun getFeePaymentsForStudent(studentId: Long): Flow<List<FeePayment>> = appDao.getFeePaymentsForStudent(studentId)
    suspend fun insertFeePayment(feePayment: FeePayment): Long = appDao.insertFeePayment(feePayment)
    suspend fun deleteFeePayment(feePayment: FeePayment) = appDao.deleteFeePayment(feePayment)

    // === Transactions ===
    val allTransactions: Flow<List<FinancialTransaction>> = appDao.getAllTransactions()
    suspend fun insertTransaction(transaction: FinancialTransaction): Long = appDao.insertTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinancialTransaction) = appDao.deleteTransaction(transaction)

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
}
