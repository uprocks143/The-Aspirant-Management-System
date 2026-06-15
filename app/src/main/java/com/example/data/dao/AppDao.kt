package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // === Batches ===
    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun getAllBatches(): Flow<List<Batch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: Batch): Long

    @Update
    suspend fun updateBatch(batch: Batch)

    @Delete
    suspend fun deleteBatch(batch: Batch)

    // === Students ===
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY name ASC")
    fun getStudentsByBatch(batchId: Long): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // === Attendance ===
    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceByDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE batchId = :batchId AND dateString = :dateString")
    fun getAttendanceForBatchAndDate(batchId: Long, dateString: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND dateString = :dateString")
    suspend fun deleteAttendance(studentId: Long, dateString: String)

    // === Fees Payments ===
    @Query("SELECT * FROM fees_payments ORDER BY datePaid DESC")
    fun getAllFeePayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fees_payments WHERE studentId = :studentId ORDER BY datePaid DESC")
    fun getFeePaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePayment(feePayment: FeePayment): Long

    // === Transactions (Ledger or accounts) ===
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinancialTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: FinancialTransaction)

    // === Study Material ===
    @Query("SELECT * FROM study_materials ORDER BY timestamp DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM study_materials WHERE batchId = :batchId ORDER BY timestamp DESC")
    fun getStudyMaterialsForBatch(batchId: Long): Flow<List<StudyMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterial): Long

    @Delete
    suspend fun deleteStudyMaterial(material: StudyMaterial)

    // === Message Templates ===
    @Query("SELECT * FROM message_templates ORDER BY title ASC")
    fun getAllMessageTemplates(): Flow<List<MessageTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageTemplate(template: MessageTemplate): Long

    @Update
    suspend fun updateMessageTemplate(template: MessageTemplate)

    @Delete
    suspend fun deleteMessageTemplate(template: MessageTemplate)

    // === Cloud Backup & Restoration Utilities ===
    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("DELETE FROM batches")
    suspend fun clearBatches()

    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("DELETE FROM attendance")
    suspend fun clearAttendance()

    @Query("DELETE FROM fees_payments")
    suspend fun clearFeePayments()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatchesBulk(batches: List<Batch>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentsBulk(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceBulk(attendanceList: List<Attendance>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeePaymentsBulk(payments: List<FeePayment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsBulk(transactions: List<FinancialTransaction>)

    // === Exams ===
    @Query("SELECT * FROM exams ORDER BY title ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE batchId = :batchId ORDER BY title ASC")
    fun getExamsForBatch(batchId: Long): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Delete
    suspend fun deleteExam(exam: Exam)

    // === Exam Questions ===
    @Query("SELECT * FROM exam_questions WHERE examId = :examId")
    fun getQuestionsForExam(examId: Long): Flow<List<ExamQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamQuestion(question: ExamQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamQuestionsBulk(questions: List<ExamQuestion>)

    // === Exam Attempts ===
    @Query("SELECT * FROM exam_attempts WHERE studentId = :studentId")
    fun getAttemptsForStudent(studentId: Long): Flow<List<ExamAttempt>>

    @Query("SELECT * FROM exam_attempts WHERE examId = :examId")
    fun getAttemptsForExam(examId: Long): Flow<List<ExamAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAttempt(attempt: ExamAttempt): Long
}
