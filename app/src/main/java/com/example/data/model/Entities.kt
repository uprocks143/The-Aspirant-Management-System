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
    val isAlumni: Boolean = false,
    val enrollmentStatus: String = "Active"
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

@Entity(tableName = "transactions", indices = [Index(value = ["type"]), Index(value = ["date"])])
data class FinancialTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String, // custom Income heads / Expense heads
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val instituteEmail: String = "" // SaaS partition column
)

@Entity(tableName = "study_materials", indices = [Index(value = ["batchId"])])
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
    val isLocked: Boolean = false,
    val examDate: String = "", // YYYY-MM-DD
    val examTime: String = ""  // HH:MM AM/PM
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
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String, // "A", "B", "C", "D" or exact blank text
    val questionType: String = "MCQ" // "MCQ" or "FITB"
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

@Entity(tableName = "chat_channels")
data class ChatChannel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val isGroup: Boolean = true, // true for public channel/group, false for direct 1-to-1 chat
    val isPrivate: Boolean = false, // direct messages or private groups
    val firstUserId: String = "", // for direct chat (sender rollNo or role, etc.)
    val secondUserId: String = "", // for direct chat (receiver rollNo or role, etc.)
    val createdDate: Long = System.currentTimeMillis(),
    val isMuted: Boolean = false,
    val slowModeSeconds: Int = 0,
    val dailyTopic: String? = null,
    val isLocked: Boolean = false,
    val isAnnouncementOnly: Boolean = false
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatChannel::class,
            parentColumns = ["id"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channelId"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long,
    val senderId: String, // Unique user identifier (e.g. name, role, email or phone)
    val senderName: String,
    val senderRole: String, // "ADMIN", "STAFF", "STUDENT", "TEACHER"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentPath: String? = null, // Path/Uri for attachment or mock content
    val attachmentType: String? = null, // "IMAGE", "DOCUMENT", "LINK", "NONE", "POLL" etc
    val attachmentName: String? = null,
    val replyToId: Long? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: String = "", // e.g. "❤️:Sumit,👍:Verma"
    val isPinned: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isReported: Boolean = false,
    val isBookmarked: Boolean = false,
    val pollQuestion: String? = null,
    val pollOptions: String? = null, // e.g. "Yes,No,Maybe"
    val pollVotes: String? = null // e.g. "Yes:2,No:1"
)

@Entity(tableName = "assignments", indices = [Index(value = ["batchId"])])
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long = 0, // 0 means All Batches, or refer to Batch ID
    val batchName: String, // "All Batches" or the specific batch name
    val title: String,
    val description: String,
    val dueDate: String = "", // e.g., "Tomorrow" or a date string
    val dueDateMillis: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L, // 1 day default
    val createdAt: Long = System.currentTimeMillis()
)


