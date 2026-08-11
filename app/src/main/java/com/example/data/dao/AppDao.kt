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
    fun insertBatch(batch: Batch): Long

    @Update
    fun updateBatch(batch: Batch): Int

    @Delete
    fun deleteBatch(batch: Batch): Int

    // === Students ===
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY name ASC")
    fun getStudentsByBatch(batchId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudent(student: Student): Long

    @Update
    fun updateStudent(student: Student): Int

    @Delete
    fun deleteStudent(student: Student): Int

    // === Attendance ===
    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceByDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE batchId = :batchId AND dateString = :dateString")
    fun getAttendanceForBatchAndDate(batchId: Long, dateString: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendance(attendance: Attendance): Long

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND dateString = :dateString")
    fun deleteAttendance(studentId: Long, dateString: String): Int

    // === Fees Payments ===
    @Query("SELECT * FROM fees_payments ORDER BY datePaid DESC")
    fun getAllFeePayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fees_payments WHERE studentId = :studentId ORDER BY datePaid DESC")
    fun getFeePaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeePayment(feePayment: FeePayment): Long

    @Delete
    fun deleteFeePayment(feePayment: FeePayment): Int

    // === Transactions (Ledger or accounts) ===
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinancialTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: FinancialTransaction): Long

    @Delete
    fun deleteTransaction(transaction: FinancialTransaction): Int

    // === Study Material ===
    @Query("SELECT * FROM study_materials ORDER BY timestamp DESC")
    fun getAllStudyMaterials(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM study_materials WHERE batchId = :batchId ORDER BY timestamp DESC")
    fun getStudyMaterialsForBatch(batchId: Long): Flow<List<StudyMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudyMaterial(material: StudyMaterial): Long

    @Delete
    fun deleteStudyMaterial(material: StudyMaterial): Int

    // === Message Templates ===
    @Query("SELECT * FROM message_templates ORDER BY title ASC")
    fun getAllMessageTemplates(): Flow<List<MessageTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageTemplate(template: MessageTemplate): Long

    @Update
    fun updateMessageTemplate(template: MessageTemplate): Int

    @Delete
    fun deleteMessageTemplate(template: MessageTemplate): Int

    // === Cloud Backup & Restoration Utilities ===
    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("DELETE FROM batches")
    fun clearBatches(): Int

    @Query("DELETE FROM students")
    fun clearStudents(): Int

    @Query("DELETE FROM attendance")
    fun clearAttendance(): Int

    @Query("DELETE FROM fees_payments")
    fun clearFeePayments(): Int

    @Query("DELETE FROM transactions")
    fun clearTransactions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBatchesBulk(batches: List<Batch>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStudentsBulk(students: List<Student>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendanceBulk(attendanceList: List<Attendance>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeePaymentsBulk(payments: List<FeePayment>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactionsBulk(transactions: List<FinancialTransaction>): List<Long>

    // === Exams ===
    @Query("SELECT * FROM exams ORDER BY title ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE batchId = :batchId ORDER BY title ASC")
    fun getExamsForBatch(batchId: Long): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExam(exam: Exam): Long

    @Delete
    fun deleteExam(exam: Exam): Int

    // === Exam Questions ===
    @Query("SELECT * FROM exam_questions WHERE examId = :examId")
    fun getQuestionsForExam(examId: Long): Flow<List<ExamQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExamQuestion(question: ExamQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExamQuestionsBulk(questions: List<ExamQuestion>): List<Long>

    // === Exam Attempts ===
    @Query("SELECT * FROM exam_attempts WHERE studentId = :studentId")
    fun getAttemptsForStudent(studentId: Long): Flow<List<ExamAttempt>>

    @Query("SELECT * FROM exam_attempts WHERE examId = :examId")
    fun getAttemptsForExam(examId: Long): Flow<List<ExamAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExamAttempt(attempt: ExamAttempt): Long

    // === Material Quizzes ===
    @Query("SELECT * FROM material_quizzes WHERE studyMaterialId = :studyMaterialId ORDER BY id DESC")
    fun getQuizzesForMaterial(studyMaterialId: Long): Flow<List<MaterialQuiz>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaterialQuiz(quiz: MaterialQuiz): Long

    @Delete
    fun deleteMaterialQuiz(quiz: MaterialQuiz): Int

    // === Material Quiz Questions ===
    @Query("SELECT * FROM material_quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    fun getQuestionsForQuiz(quizId: Long): Flow<List<MaterialQuizQuestion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaterialQuizQuestion(question: MaterialQuizQuestion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaterialQuizQuestionsBulk(questions: List<MaterialQuizQuestion>): List<Long>

    // === Material Quiz Attempts ===
    @Query("SELECT * FROM material_quiz_attempts WHERE quizId = :quizId ORDER BY attemptDate DESC")
    fun getAttemptsForQuiz(quizId: Long): Flow<List<MaterialQuizAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMaterialQuizAttempt(attempt: MaterialQuizAttempt): Long

    // === Community Chats ===
    @Query("SELECT * FROM chat_channels ORDER BY createdDate ASC")
    fun getAllChatChannels(): Flow<List<ChatChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatChannel(channel: ChatChannel): Long

    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    fun getChatMessageById(messageId: Long): ChatMessage?

    @Query("SELECT * FROM chat_channels WHERE id = :channelId LIMIT 1")
    fun getChatChannelById(channelId: Long): ChatChannel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE channelId = :channelId")
    fun clearMessagesForChannel(channelId: Long): Int

    @Query("DELETE FROM chat_channels WHERE id = :channelId")
    fun deleteChatChannel(channelId: Long): Int

    // === Assignments ===
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE batchId = :batchId OR batchId = 0 ORDER BY createdAt DESC")
    fun getAssignmentsForBatch(batchId: Long): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssignment(assignment: Assignment): Long

    @Delete
    fun deleteAssignment(assignment: Assignment): Int
}
