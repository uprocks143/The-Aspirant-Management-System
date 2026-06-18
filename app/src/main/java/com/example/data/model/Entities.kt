package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val location: String = "Main Center",
    val assignedTeacher: String = "Tutor Admin",
    val classTimings: String = "04:00 PM - 05:00 PM",
    val daysOfWeek: String = "Mon, Wed, Fri", // comma separated
    val maxCapacity: Int = 30,
    val isActive: Boolean = true,
    val feesAmount: Double = 1000.0,
    val subject: String = "Mathematics",
    val instituteEmail: String = "" // SaaS partition column
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["batchId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val rollNumber: String,
    val name: String,
    val parentName: String,
    val dateOfBirth: String, // YYYY-MM-DD
    val phone: String, // Mobile Number
    val parentPhone: String,
    val gender: String = "Male",
    val address: String = "",
    val startDate: String = "", // YYYY-MM-DD
    val studentClass: String = "",
    val subject: String = "",
    val schoolCollegeName: String = "",
    // Custom Fields
    val customField1: String = "",
    val customField2: String = "",
    val customField3: String = "",
    val profilePhotoPath: String? = null,
    val documentAttachments: String? = null, // Path or comma-separated list of local URIs
    val familyId: String = "", // Linking mechanism to group siblings together
    val isAlumni: Boolean = false
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val batchId: Long,
    val dateString: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Leave"
    val trackingMethod: String, // "Manual", "QR Code"
    val checkInTime: String = ""
)

@Entity(
    tableName = "fees_payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"])]
)
data class FeePayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val feeType: String, // "Course-based" vs "Monthly-based"
    val amountPaid: Double,
    val discount: Double = 0.0,
    val paymentMode: String = "CASH", // "UPI", "CASH", "CARD", "NET_BANKING"
    val datePaid: Long = System.currentTimeMillis(),
    val remarks: String = "",
    val receiptNo: String = "",
    val monthPaidFor: String = "",
    val transactionStatus: String = "SUCCESS"
)

@Entity(tableName = "transactions")
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String, // custom Income heads / Expense heads
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val instituteEmail: String = "" // SaaS partition column
)

@Entity(tableName = "study_materials")
data class StudyMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long = 0, // Organizes content strictly by: Batch Access -> Main Category -> Topic/Sub-category
    val mainCategory: String = "General",
    val topicSubCategory: String = "Overview",
    val title: String,
    val contentType: String = "Plain Text", // "Plain Text", "PDF", "Image", "URL", "YouTube video"
    val content: String, // Plain text content or URL string
    val pdfName: String? = null,
    val youtubeUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String, // e.g., "Dear [ParentName], [StudentName]'s fee of [Amount] is pending."
    val platform: String = "SMS" // "SMS", "PUSH", "WHATSAPP"
)

// --- Added Exam Entities ---

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["batchId"])]
)
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val title: String,
    val subject: String = "",
    val durationMinutes: Int = 30,
    val isLocked: Boolean = false
)

@Entity(
    tableName = "exam_questions",
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["examId"])]
)
data class ExamQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String // "A", "B", "C", "D"
)

@Entity(
    tableName = "exam_attempts",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studentId"]), Index(value = ["examId"])]
)
data class ExamAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val examId: Long,
    val score: Int,
    val totalScore: Int,
    val timeSpentSeconds: Int = 0,
    val attemptDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "material_quizzes",
    foreignKeys = [
        ForeignKey(
            entity = StudyMaterial::class,
            parentColumns = ["id"],
            childColumns = ["studyMaterialId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["studyMaterialId"])]
)
data class MaterialQuiz(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studyMaterialId: Long,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 15
)

@Entity(
    tableName = "material_quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = MaterialQuiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class MaterialQuizQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val type: String, // "MULTIPLE_CHOICE", "FILL_IN_THE_BLANKS", "SHORT_ANSWER"
    val questionText: String,
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String // text exact match or "A", "B", "C", "D"
)

@Entity(
    tableName = "material_quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = MaterialQuiz::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quizId"])]
)
data class MaterialQuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long, // Can also store generic roll-number / generic mapping
    val quizId: Long,
    val score: Int,
    val totalQuestions: Int,
    val attemptDate: Long = System.currentTimeMillis(),
    val feedbackJson: String = "" // auto feedback response
)

