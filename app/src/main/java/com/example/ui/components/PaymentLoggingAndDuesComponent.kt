package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Batch
import com.example.data.model.FeePayment
import com.example.data.model.PaymentLog
import com.example.data.model.Student
import com.example.services.SupabaseService
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Status representation of a student's tuition dues for the current/selected billing cycle.
 */
enum class DuesState(val label: String, val color: Color, val badgeColor: Color) {
    PAID("Completed", Color(0xFF2E7D32), Color(0xFFE8F5E9)),
    PARTIAL("Partial Due", Color(0xFFF57F17), Color(0xFFFFF9C4)),
    PENDING("Pending Due", Color(0xFFC62828), Color(0xFFFFEBEE))
}

data class StudentDuesInfo(
    val student: Student,
    val batch: Batch?,
    val expectedMonthlyFee: Double,
    val totalPaid: Double,
    val pendingAmount: Double,
    val duesState: DuesState,
    val lastPaymentDate: Long?
)

/**
 * Helper to compute dues status for all students.
 */
fun computeStudentDues(
    students: List<Student>,
    batches: List<Batch>,
    payments: List<FeePayment>,
    targetMonth: String = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
): List<StudentDuesInfo> {
    val batchMap = batches.associateBy { it.id }

    return students.map { student ->
        val batch = batchMap[student.batchId]
        // Determine expected monthly fee from batch fee
        val expectedFee = when {
            batch != null && batch.feesAmount > 0 -> batch.feesAmount
            else -> 1200.0 // Default institute monthly standard fee
        }

        // Total payments made by this student (for the month or overall)
        val studentPayments = payments.filter { it.studentId == student.id }
        val monthPayments = studentPayments.filter {
            it.monthPaidFor.equals(targetMonth, ignoreCase = true) || it.remarks.contains(targetMonth, ignoreCase = true)
        }

        val totalPaidForMonth = if (monthPayments.isNotEmpty()) {
            monthPayments.sumOf { it.amountPaid + it.discount }
        } else {
            // Check recent payments in current month window (30 days)
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            studentPayments.filter { it.datePaid >= thirtyDaysAgo }.sumOf { it.amountPaid + it.discount }
        }

        val pending = (expectedFee - totalPaidForMonth).coerceAtLeast(0.0)
        val state = when {
            pending <= 0.0 -> DuesState.PAID
            totalPaidForMonth > 0 -> DuesState.PARTIAL
            else -> DuesState.PENDING
        }

        val lastPayment = studentPayments.maxByOrNull { it.datePaid }?.datePaid

        StudentDuesInfo(
            student = student,
            batch = batch,
            expectedMonthlyFee = expectedFee,
            totalPaid = totalPaidForMonth,
            pendingAmount = pending,
            duesState = state,
            lastPaymentDate = lastPayment
        )
    }
}

/**
 * Visual Indicator Chip for a student's pending or completed dues.
 */
@Composable
fun StudentDuesVisualBadge(
    duesState: DuesState,
    pendingAmount: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Surface(
        color = duesState.badgeColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, duesState.color.copy(alpha = 0.4f)),
        modifier = modifier.testTag("dues_badge_${duesState.name}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(duesState.color, CircleShape)
            )
            val text = when (duesState) {
                DuesState.PAID -> "Completed • ₹0 Due"
                DuesState.PARTIAL -> "Partial • ₹${pendingAmount.toInt()} Due"
                DuesState.PENDING -> "Pending • ₹${pendingAmount.toInt()} Due"
            }
            Text(
                text = text,
                color = duesState.color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Comprehensive Payment Logging System with Local Storage & Supabase Sync.
 */
@Composable
fun PaymentLoggingAndDuesConsole(
    students: List<Student>,
    batches: List<Batch>,
    payments: List<FeePayment>,
    paymentLogs: List<PaymentLog>,
    viewModel: AppViewModel,
    onDirectSmsReminder: ((Student, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedMonth by remember {
        mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()))
    }
    var duesFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "PAID", "PARTIAL"
    var studentSearchQuery by remember { mutableStateOf("") }

    // Dialog controllers
    var studentToLogPaymentFor by remember { mutableStateOf<StudentDuesInfo?>(null) }
    var studentToRemind by remember { mutableStateOf<StudentDuesInfo?>(null) }
    var showSupabaseConfigDialog by remember { mutableStateOf(false) }
    var isSyncingSupabase by remember { mutableStateOf(false) }

    val merchantUpiId by viewModel.merchantUpiId.collectAsState()
    val merchantName by viewModel.merchantName.collectAsState()

    val duesList = remember(students, batches, payments, selectedMonth) {
        computeStudentDues(students, batches, payments, selectedMonth)
    }

    val filteredDues = remember(duesList, duesFilter, studentSearchQuery) {
        duesList.filter { item ->
            val matchesFilter = when (duesFilter) {
                "PENDING" -> item.duesState == DuesState.PENDING
                "PAID" -> item.duesState == DuesState.PAID
                "PARTIAL" -> item.duesState == DuesState.PARTIAL
                else -> true
            }
            val matchesSearch = studentSearchQuery.isBlank() ||
                    item.student.name.contains(studentSearchQuery, ignoreCase = true) ||
                    item.student.phone.contains(studentSearchQuery, ignoreCase = true) ||
                    (item.batch?.name ?: "").contains(studentSearchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    // Key metrics
    val totalExpected = remember(duesList) { duesList.sumOf { it.expectedMonthlyFee } }
    val totalCollected = remember(duesList) { duesList.sumOf { it.totalPaid } }
    val totalPending = remember(duesList) { duesList.sumOf { it.pendingAmount } }
    val pendingCount = remember(duesList) { duesList.count { it.duesState == DuesState.PENDING || it.duesState == DuesState.PARTIAL } }
    val paidCount = remember(duesList) { duesList.count { it.duesState == DuesState.PAID } }

    val lastSupabaseSync = remember { SupabaseService.getLastSyncTime(context) }
    val isSupabaseConfigured = remember { SupabaseService.isConfigured(context) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Supabase Cloud Sync Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF3ECF8E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Dual Storage Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    color = if (isSupabaseConfigured) Color(0xFF134E4A) else Color(0xFF701A75),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isSupabaseConfigured) "Room DB + Supabase" else "Local Room DB",
                                        color = if (isSupabaseConfigured) Color(0xFF5EEAD4) else Color(0xFFF0ABFC),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            val lastSyncStr = if (lastSupabaseSync > 0) {
                                SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(lastSupabaseSync))
                            } else "Never"
                            Text("Last synced: $lastSyncStr", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showSupabaseConfigDialog = true },
                            modifier = Modifier.size(32.dp).testTag("supabase_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Supabase Settings", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = {
                                isSyncingSupabase = true
                                coroutineScope.launch {
                                    val studentsMap = students.associateBy { it.id }
                                    val result = SupabaseService.syncBulkPaymentsToSupabase(context, payments, studentsMap)
                                    isSyncingSupabase = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "Successfully synced ${result.getOrNull()} payments to Supabase!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Local DB synced. Supabase notice: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp).testTag("sync_supabase_now_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ECF8E), contentColor = Color.Black),
                            enabled = !isSyncingSupabase
                        ) {
                            if (isSyncingSupabase) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Cloud", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Expected
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Due", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${totalExpected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("${students.size} students", fontSize = 10.sp, color = Color.Gray)
                }
            }

            // Collected
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Collected", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${totalCollected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$paidCount paid", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                }
            }

            // Pending
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pending Dues", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${totalPending.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB71C1C))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$pendingCount pending", fontSize = 10.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                }
            }
        }

        // Month Selector & Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month indicator chip
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Text(selectedMonth, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Filter Chips
            FilterChip(
                selected = duesFilter == "ALL",
                onClick = { duesFilter = "ALL" },
                label = { Text("All (${duesList.size})", fontSize = 11.sp) },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("filter_all_dues")
            )

            FilterChip(
                selected = duesFilter == "PENDING",
                onClick = { duesFilter = "PENDING" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFFC62828), CircleShape))
                        Text("Pending (${duesList.count { it.duesState == DuesState.PENDING }})", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("filter_pending_dues")
            )

            FilterChip(
                selected = duesFilter == "PAID",
                onClick = { duesFilter = "PAID" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFF2E7D32), CircleShape))
                        Text("Paid (${paidCount})", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("filter_paid_dues")
            )

            FilterChip(
                selected = duesFilter == "PARTIAL",
                onClick = { duesFilter = "PARTIAL" },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(7.dp).background(Color(0xFFF57F17), CircleShape))
                        Text("Partial (${duesList.count { it.duesState == DuesState.PARTIAL }})", fontSize = 11.sp, color = Color(0xFFF57F17), fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("filter_partial_dues")
            )
        }

        // Student Search Input
        OutlinedTextField(
            value = studentSearchQuery,
            onValueChange = { studentSearchQuery = it },
            placeholder = { Text("Search students to check dues or log payment...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (studentSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { studentSearchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("student_dues_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // Student Dues List
        if (filteredDues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text("No students match the current dues filter.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredDues.forEach { item ->
                    StudentDuesCard(
                        duesInfo = item,
                        onLogPaymentClick = { studentToLogPaymentFor = item },
                        onReminderClick = {
                            if (onDirectSmsReminder != null) {
                                onDirectSmsReminder.invoke(item.student, item.pendingAmount)
                            } else {
                                studentToRemind = item
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal: Log Monthly Tuition Payment
    studentToLogPaymentFor?.let { duesInfo ->
        LogMonthlyTuitionPaymentDialog(
            duesInfo = duesInfo,
            targetMonth = selectedMonth,
            onDismiss = { studentToLogPaymentFor = null },
            onConfirmPayment = { amount, discount, mode, month, remarks ->
                viewModel.collectFeePayment(
                    studentId = duesInfo.student.id,
                    amount = amount,
                    discount = discount,
                    feeType = "Monthly-based",
                    month = month,
                    mode = mode,
                    remarks = remarks
                )

                // Trigger Supabase asynchronous dual-storage persistence
                coroutineScope.launch {
                    val samplePayment = FeePayment(
                        studentId = duesInfo.student.id,
                        feeType = "Monthly-based",
                        amountPaid = amount,
                        discount = discount,
                        paymentMode = mode,
                        remarks = remarks,
                        receiptNo = "REC-${System.currentTimeMillis() % 100000}",
                        monthPaidFor = month
                    )
                    SupabaseService.syncPaymentToSupabase(
                        context = context,
                        payment = samplePayment,
                        studentName = duesInfo.student.name,
                        studentPhone = duesInfo.student.phone,
                        batchName = duesInfo.batch?.name ?: ""
                    )
                }

                studentToLogPaymentFor = null
                Toast.makeText(context, "Payment of ₹${amount.toInt()} logged & stored in Local DB + Supabase!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Modal: Send WhatsApp Payment Reminder with Direct UPI Link
    studentToRemind?.let { duesInfo ->
        val effectiveUpi = merchantUpiId.ifBlank { "smtsharma282.sks@okaxis" }
        val effectiveAcademyName = merchantName.ifBlank { "TAMS Academy" }
        val dueAmount = duesInfo.pendingAmount.toInt()
        val clickToPayLink = "https://upiqr.in/pay/$effectiveUpi/$dueAmount"
        val encodedAcademy = Uri.encode(effectiveAcademyName)
        val encodedStudent = Uri.encode(duesInfo.student.name)
        val upiDeepLink = "upi://pay?pa=$effectiveUpi&pn=$encodedAcademy&am=$dueAmount&cu=INR&tn=Fee%20Dues%20for%20$encodedStudent"

        val msg = "📢 *FEE PAYMENT REMINDER - $effectiveAcademyName*\n\n" +
                "Dear Parent,\n" +
                "This is a gentle reminder that tuition fee dues of *₹$dueAmount* are pending for *${duesInfo.student.name}* (${duesInfo.batch?.name ?: "Tuition Batch"}).\n\n" +
                "👉 *Click here to Pay directly via any UPI App (GPay / PhonePe / Paytm / BHIM):*\n" +
                "$clickToPayLink\n\n" +
                "🔗 *Direct UPI Link:*\n$upiDeepLink\n\n" +
                "💳 *UPI ID:* $effectiveUpi\n\n" +
                "Kindly clear the dues at your earliest convenience.\n\n" +
                "Thank you,\n*$effectiveAcademyName*"

        AlertDialog(
            onDismissRequest = { studentToRemind = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFC62828))
                    Text("Send Payment Notice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recipient: ${duesInfo.student.name} • Due: ₹$dueAmount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = msg,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Message with UPI Pay Link") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val rawPhone = duesInfo.student.parentPhone.ifEmpty { duesInfo.student.phone }.replace(Regex("[^0-9]"), "")
                            if (rawPhone.isBlank()) {
                                Toast.makeText(context, "No contact number for ${duesInfo.student.name}", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val cleanPhone = when {
                                rawPhone.startsWith("91") && rawPhone.length == 12 -> rawPhone
                                rawPhone.startsWith("0") && rawPhone.length == 11 -> "91" + rawPhone.substring(1)
                                rawPhone.length == 10 -> "91$rawPhone"
                                else -> rawPhone
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(msg)}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Payment Reminder"))
                                } catch (e2: Exception) {
                                    Toast.makeText(context, "Could not launch messaging app", Toast.LENGTH_SHORT).show()
                                }
                            }
                            studentToRemind = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val rawPhone = duesInfo.student.parentPhone.ifEmpty { duesInfo.student.phone }.replace(Regex("[^0-9]"), "")
                            if (rawPhone.isBlank()) {
                                Toast.makeText(context, "No contact number for ${duesInfo.student.name}", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            try {
                                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$rawPhone")).apply {
                                    putExtra("sms_body", msg)
                                }
                                context.startActivity(smsIntent)
                            } catch (e: Exception) {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Send SMS"))
                                } catch (e2: Exception) {
                                    Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show()
                                }
                            }
                            studentToRemind = null
                        }
                    ) {
                        Text("SMS", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToRemind = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Supabase Settings Dialog
    if (showSupabaseConfigDialog) {
        SupabaseSettingsDialog(
            onDismiss = { showSupabaseConfigDialog = false }
        )
    }
}

/**
 * Individual Student Dues Row Card with Visual Indicator
 */
@Composable
fun StudentDuesCard(
    duesInfo: StudentDuesInfo,
    onLogPaymentClick: () -> Unit,
    onReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_dues_card_${duesInfo.student.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (duesInfo.duesState == DuesState.PENDING) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = duesInfo.student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${duesInfo.batch?.name ?: "No Batch"} • Phone: ${duesInfo.student.phone.ifBlank { "N/A" }}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Dues Status Visual Indicator Badge
                StudentDuesVisualBadge(
                    duesState = duesInfo.duesState,
                    pendingAmount = duesInfo.pendingAmount
                )
            }

            // Fee Progress Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expected: ₹${duesInfo.expectedMonthlyFee.toInt()}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Paid: ₹${duesInfo.totalPaid.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Pending: ₹${duesInfo.pendingAmount.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (duesInfo.pendingAmount > 0) Color(0xFFC62828) else Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (duesInfo.pendingAmount > 0) {
                    OutlinedButton(
                        onClick = onReminderClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Notice", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Button(
                    onClick = onLogPaymentClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp).testTag("log_payment_btn_${duesInfo.student.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog to Log Monthly Tuition Payment with Supabase & Local Storage
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMonthlyTuitionPaymentDialog(
    duesInfo: StudentDuesInfo,
    targetMonth: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, discount: Double, mode: String, month: String, remarks: String) -> Unit
) {
    var amountText by remember { mutableStateOf(duesInfo.pendingAmount.takeIf { it > 0 }?.toInt()?.toString() ?: duesInfo.expectedMonthlyFee.toInt().toString()) }
    var discountText by remember { mutableStateOf("0") }
    var selectedMode by remember { mutableStateOf("UPI") }
    var monthText by remember { mutableStateOf(targetMonth) }
    var remarksText by remember { mutableStateOf("Monthly tuition fee for $targetMonth") }

    val paymentModes = listOf("UPI", "CASH", "BANK_TRANSFER", "CARD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Log Monthly Tuition Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student Info Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(duesInfo.student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Batch: ${duesInfo.batch?.name ?: "General"} | Pending: ₹${duesInfo.pendingAmount.toInt()}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Received (₹) *") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Discount
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Scholarship / Discount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Month Paid For
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it },
                    label = { Text("Month Billing Cycle") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Payment Mode Selector
                Text("Payment Mode:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    paymentModes.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode, fontSize = 11.sp) }
                        )
                    }
                }

                // Remarks
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Remarks / Reference Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Dual Storage Notice
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(16.dp))
                        Text(
                            text = "Auto-persists to Local Room SQLite + Supabase cloud",
                            fontSize = 10.sp,
                            color = Color(0xFF0F766E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val discount = discountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirmPayment(amount, discount, selectedMode, monthText, remarksText)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Record & Sync")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Settings dialog for Supabase configuration
 */
@Composable
fun SupabaseSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var url by remember { mutableStateOf(SupabaseService.getSupabaseUrl(context)) }
    var anonKey by remember { mutableStateOf(SupabaseService.getSupabaseKey(context)) }
    var autoSync by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF3ECF8E))
                Text("Supabase Cloud Database Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configure Supabase REST endpoint to mirror monthly tuition logs to your PostgreSQL database.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://xyzcompany.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it },
                    label = { Text("Supabase Anon Public API Key") },
                    placeholder = { Text("eyJhbGciOi...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-sync on payment log", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Switch(checked = autoSync, onCheckedChange = { autoSync = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SupabaseService.saveConfig(context, url, anonKey, autoSync)
                    Toast.makeText(context, "Supabase configuration updated!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Save Configuration")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
