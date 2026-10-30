package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Batch
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * TCMS-Style Tuition Fees Management Console
 * (Modelled directly after "TCMS - Tuition Manager" by InvoTech)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TcmsTuitionFeesConsole(
    payments: List<FeePayment>,
    students: List<Student>,
    batches: List<Batch>,
    viewModel: AppViewModel,
    academyName: String = "TAMS Academy",
    adminAddress: String = "Main Center",
    adminPhone: String = "",
    adminEmail: String = "",
    merchantUpiId: String = "",
    merchantName: String = ""
) {
    val context = LocalContext.current
    val academyNameFromVm by viewModel.academyName.collectAsState()
    val adminAddressFromVm by viewModel.adminAddress.collectAsState()
    val adminPhoneFromVm by viewModel.adminPhone.collectAsState()
    val adminEmailFromVm by viewModel.adminEmail.collectAsState()
    val merchantUpiIdFromVm by viewModel.merchantUpiId.collectAsState()
    val merchantNameFromVm by viewModel.merchantName.collectAsState()

    val effectiveAcademyName = if (academyName != "TAMS Academy" && academyName.isNotBlank()) academyName else academyNameFromVm.ifEmpty { "TAMS Academy" }
    val effectiveAddress = if (adminAddress != "Main Center" && adminAddress.isNotBlank()) adminAddress else adminAddressFromVm.ifEmpty { "Main Center" }
    val effectivePhone = if (adminPhone.isNotBlank()) adminPhone else adminPhoneFromVm
    val effectiveEmail = if (adminEmail.isNotBlank()) adminEmail else adminEmailFromVm
    val effectiveUpiId = if (merchantUpiId.isNotBlank()) merchantUpiId else merchantUpiIdFromVm
    val effectiveMerchantName = if (merchantName.isNotBlank()) merchantName else merchantNameFromVm

    // 1. Month Cycle Selector (Defaults to current month, e.g. "October 2026")
    val calendar = remember { Calendar.getInstance() }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    var currentMonthIndexOffset by remember { mutableIntStateOf(0) }

    val selectedMonth = remember(currentMonthIndexOffset) {
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, currentMonthIndexOffset) }
        monthFormat.format(cal.time)
    }

    // 2. Batch Selector
    var selectedBatchId by remember { mutableStateOf<Long?>(null) } // null = All Batches
    val batchMap = remember(batches) { batches.associateBy { it.id } }

    // 3. Tab Index: 0 = Pending Dues, 1 = Paid Receipts, 2 = Direct Deposit
    var activeTabIndex by remember { mutableIntStateOf(0) }

    // 4. Search and Filter
    var pendingSearchQuery by remember { mutableStateOf("") }
    var historySearchQuery by remember { mutableStateOf("") }
    var historyModeFilter by remember { mutableStateOf("ALL") }

    // 5. Active Dialogs State
    var studentToDepositFeeFor by remember { mutableStateOf<StudentDuesSummary?>(null) }
    var studentForReminder by remember { mutableStateOf<StudentDuesSummary?>(null) }
    var activeReceiptToShow by remember { mutableStateOf<FeePayment?>(null) }

    // Compute dues per student for the selected month and batch
    val duesList = remember(students, batches, payments, selectedMonth, selectedBatchId) {
        students
            .filter { selectedBatchId == null || it.batchId == selectedBatchId }
            .map { student ->
                val batch = batchMap[student.batchId]
                val expectedFee = if (batch != null && batch.feesAmount > 0) batch.feesAmount else 1200.0

                val studentPayments = payments.filter { it.studentId == student.id }
                val monthPayments = studentPayments.filter {
                    it.monthPaidFor.equals(selectedMonth, ignoreCase = true) ||
                            it.remarks.contains(selectedMonth, ignoreCase = true)
                }

                val totalPaid = monthPayments.sumOf { it.amountPaid + it.discount }
                val pending = (expectedFee - totalPaid).coerceAtLeast(0.0)
                val isPaid = pending <= 0.0

                StudentDuesSummary(
                    student = student,
                    batch = batch,
                    expectedFee = expectedFee,
                    paidAmount = totalPaid,
                    pendingDue = pending,
                    isPaid = isPaid,
                    month = selectedMonth
                )
            }
    }

    // Filtered lists
    val pendingStudents = remember(duesList, pendingSearchQuery) {
        duesList.filter { !it.isPaid && (
                pendingSearchQuery.isBlank() ||
                        it.student.name.contains(pendingSearchQuery, ignoreCase = true) ||
                        it.student.rollNumber.contains(pendingSearchQuery, ignoreCase = true) ||
                        it.student.phone.contains(pendingSearchQuery, ignoreCase = true)
                )}
    }

    val paidReceipts = remember(payments, selectedMonth, selectedBatchId, historySearchQuery, historyModeFilter) {
        val studentIdsInBatch = if (selectedBatchId == null) {
            students.map { it.id }.toSet()
        } else {
            students.filter { it.batchId == selectedBatchId }.map { it.id }.toSet()
        }

        payments.filter { pay ->
            val matchBatch = studentIdsInBatch.contains(pay.studentId)
            val matchMonth = pay.monthPaidFor.equals(selectedMonth, ignoreCase = true) ||
                    pay.remarks.contains(selectedMonth, ignoreCase = true) ||
                    pay.monthPaidFor.isBlank()

            val studentObj = students.find { it.id == pay.studentId }
            val studentName = studentObj?.name ?: ""
            val rollNo = studentObj?.rollNumber ?: ""

            val matchSearch = historySearchQuery.isBlank() ||
                    studentName.contains(historySearchQuery, ignoreCase = true) ||
                    rollNo.contains(historySearchQuery, ignoreCase = true) ||
                    pay.receiptNo.contains(historySearchQuery, ignoreCase = true)

            val matchMode = (historyModeFilter == "ALL" || pay.paymentMode.equals(historyModeFilter, ignoreCase = true))

            matchBatch && matchMonth && matchSearch && matchMode
        }.sortedByDescending { it.datePaid }
    }

    // Metrics
    val totalExpected = remember(duesList) { duesList.sumOf { it.expectedFee } }
    val totalCollected = remember(duesList) { duesList.sumOf { it.paidAmount } }
    val totalPending = remember(duesList) { duesList.sumOf { it.pendingDue } }
    val paidCount = remember(duesList) { duesList.count { it.isPaid } }
    val pendingCount = remember(duesList) { duesList.count { !it.isPaid } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =========================================================================
        // TOP CONTROLS: Month Selector + Batch Filter Chips (TCMS Header)
        // =========================================================================

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Month Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonthIndexOffset -= 1 },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.clickable { currentMonthIndexOffset = 0 }
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = selectedMonth,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (currentMonthIndexOffset != 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text("Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    IconButton(
                        onClick = { currentMonthIndexOffset += 1 },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }

                // Batch Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedBatchId == null,
                            onClick = { selectedBatchId = null },
                            label = { Text("All Batches (${students.size})") },
                            leadingIcon = if (selectedBatchId == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }

                    items(batches) { b ->
                        val count = students.count { it.batchId == b.id }
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = if (selectedBatchId == b.id) null else b.id },
                            label = { Text("${b.name} ($count)") },
                            leadingIcon = if (selectedBatchId == b.id) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SUMMARY METRICS CARDS (TCMS Collection Overview)
        // =========================================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Expected
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Expected", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text("₹${totalExpected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("${duesList.size} Students", fontSize = 9.sp, color = Color(0xFF64748B))
                }
            }

            // Collected
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Collected", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.SemiBold)
                    Text("₹${totalCollected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    Text("$paidCount Paid", fontSize = 9.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                }
            }

            // Pending Dues
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Pending Dues", fontSize = 10.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.SemiBold)
                    Text("₹${totalPending.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                    Text("$pendingCount Due", fontSize = 9.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                }
            }
        }

        // =========================================================================
        // TCMS 3 CORE TABS: Pending Fees | Paid Receipts | Direct Deposit
        // =========================================================================

        TabRow(
            selectedTabIndex = activeTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTabIndex == 0,
                onClick = { activeTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Pending Fees", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Badge(containerColor = Color(0xFFDC2626)) {
                            Text("$pendingCount", color = Color.White)
                        }
                    }
                }
            )

            Tab(
                selected = activeTabIndex == 1,
                onClick = { activeTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Paid Receipts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Badge(containerColor = Color(0xFF16A34A)) {
                            Text("${paidReceipts.size}", color = Color.White)
                        }
                    }
                }
            )

            Tab(
                selected = activeTabIndex == 2,
                onClick = { activeTabIndex = 2 },
                text = {
                    Text("Direct Deposit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            )
        }

        // =========================================================================
        // TAB 1: PENDING FEES LIST (बकाया फीस सूची - TCMS Core Screen)
        // =========================================================================

        if (activeTabIndex == 0) {
            // Search Input
            OutlinedTextField(
                value = pendingSearchQuery,
                onValueChange = { pendingSearchQuery = it },
                placeholder = { Text("Search pending student by name, roll, or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (pendingSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { pendingSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (pendingStudents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
                        Text(
                            text = "All Fees Cleared for $selectedMonth!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "No pending fee accounts for the selected batch/cycle.",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }
            } else {
                Text(
                    text = "${pendingStudents.size} students have pending fees for $selectedMonth",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingStudents.forEach { duesSummary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Top row: Name, Roll, Batch & Pending Amount Highlight
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = duesSummary.student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFEFF6FF)
                                            ) {
                                                Text(
                                                    text = "Roll: ${duesSummary.student.rollNumber}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1D4ED8),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Batch: ${duesSummary.batch?.name ?: "Main Batch"} • Phone: ${duesSummary.student.phone.ifBlank { duesSummary.student.parentPhone.ifBlank { "N/A" } }}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    // Due Amount in bold red box
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFEE2E2),
                                        border = BorderStroke(1.dp, Color(0xFFF87171))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text("DUE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF991B1B))
                                            Text("₹${duesSummary.pendingDue.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB91C1C))
                                        }
                                    }
                                }

                                // Payment status progress detail
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Monthly: ₹${duesSummary.expectedFee.toInt()} | Paid: ₹${duesSummary.paidAmount.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569)
                                    )

                                    if (duesSummary.paidAmount > 0) {
                                        Text("Partially Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    } else {
                                        Text("Unpaid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // TWO PRIMARY TCMS ACTIONS: "Deposit Fee" & "Send Reminder"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Primary Green Deposit Button
                                    Button(
                                        onClick = { studentToDepositFeeFor = duesSummary },
                                        modifier = Modifier.weight(1.3f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Deposit Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Reminder WhatsApp/SMS Button
                                    OutlinedButton(
                                        onClick = { studentForReminder = duesSummary },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C)),
                                        border = BorderStroke(1.dp, Color(0xFFFB923C))
                                    ) {
                                        Icon(Icons.Default.NotificationAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reminder", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 2: PAID RECEIPTS & HISTORY (जमा रसीदें - TCMS Receipts)
        // =========================================================================

        if (activeTabIndex == 1) {
            // Search & Mode Filter
            OutlinedTextField(
                value = historySearchQuery,
                onValueChange = { historySearchQuery = it },
                placeholder = { Text("Search receipt by number, student name, roll...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Mode Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("ALL", "CASH", "UPI", "CARD", "NET_BANKING").forEach { mode ->
                    item {
                        FilterChip(
                            selected = historyModeFilter == mode,
                            onClick = { historyModeFilter = mode },
                            label = { Text(mode) }
                        )
                    }
                }
            }

            if (paidReceipts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                        Text("No payment receipts found for $selectedMonth", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    paidReceipts.forEach { pay ->
                        val studentObj = students.find { it.id == pay.studentId }
                        val studentName = studentObj?.name ?: "Student #${pay.studentId}"
                        val rollNo = studentObj?.rollNumber ?: "N/A"
                        val batchName = studentObj?.let { batchMap[it.batchId]?.name } ?: "Batch N/A"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Text(
                                            text = pay.receiptNo.ifBlank { "REC-${pay.id}" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0F766E),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(pay.datePaid))
                                    Text(dateStr, fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Roll: $rollNo • $batchName", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${pay.amountPaid.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF15803D))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (pay.paymentMode.uppercase()) {
                                                "UPI" -> Color(0xFFE0E7FF)
                                                "CASH" -> Color(0xFFDCFCE7)
                                                else -> Color(0xFFFEF3C7)
                                            }
                                        ) {
                                            Text(
                                                text = pay.paymentMode,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                if (pay.discount > 0.0) {
                                    Text("Discount/Waiver: ₹${pay.discount.toInt()}", fontSize = 10.sp, color = Color(0xFFB45309))
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // Action Buttons: View/Print Receipt & Share WhatsApp
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { activeReceiptToShow = pay },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View Receipt", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            sendDigitalReceiptWhatsApp(context, pay, studentObj, batchName, academyName)
                                        },
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // TAB 3: DIRECT DEPOSIT (त्वरित फीस जमा फॉर्म)
        // =========================================================================

        if (activeTabIndex == 2) {
            DirectDepositSection(
                students = students,
                batches = batches,
                selectedMonth = selectedMonth,
                viewModel = viewModel,
                academyName = academyName
            )
        }
    }

    // =========================================================================
    // MODAL DIALOGS: 1. Fee Deposit Dialog, 2. Fee Reminder, 3. Digital Receipt
    // =========================================================================

    // 1. TCMS Dedicated Fee Deposit Dialog
    studentToDepositFeeFor?.let { duesSummary ->
        TcmsFeeDepositDialog(
            duesSummary = duesSummary,
            targetMonth = selectedMonth,
            academyName = effectiveAcademyName,
            onDismiss = { studentToDepositFeeFor = null },
            onPaymentRecorded = { payment ->
                studentToDepositFeeFor = null
                activeReceiptToShow = payment
            },
            viewModel = viewModel
        )
    }

    // 2. TCMS Smart Fee Reminder Dialog
    studentForReminder?.let { duesSummary ->
        TcmsFeeReminderDialog(
            duesSummary = duesSummary,
            academyName = effectiveAcademyName,
            merchantUpiId = effectiveUpiId,
            onDismiss = { studentForReminder = null }
        )
    }

    // 3. TCMS Official Receipt Voucher & PDF Print Dialog
    activeReceiptToShow?.let { pay ->
        val student = students.find { it.id == pay.studentId }
        val batch = student?.let { batchMap[it.batchId] }

        TcmsFeeReceiptViewerDialog(
            payment = pay,
            student = student,
            batch = batch,
            academyName = effectiveAcademyName,
            adminAddress = effectiveAddress,
            adminPhone = effectivePhone,
            adminEmail = effectiveEmail,
            onDismiss = { activeReceiptToShow = null }
        )
    }
}

data class StudentDuesSummary(
    val student: Student,
    val batch: Batch?,
    val expectedFee: Double,
    val paidAmount: Double,
    val pendingDue: Double,
    val isPaid: Boolean,
    val month: String
)

// =========================================================================
// TCMS DEDICATED FEE DEPOSIT DIALOG (फीस जमा करने का मुख्य विंडो)
// =========================================================================

@Composable
fun TcmsFeeDepositDialog(
    duesSummary: StudentDuesSummary,
    targetMonth: String,
    academyName: String,
    onDismiss: () -> Unit,
    onPaymentRecorded: (FeePayment) -> Unit,
    viewModel: AppViewModel
) {
    val context = LocalContext.current

    var depositAmountText by remember { mutableStateOf(duesSummary.pendingDue.toInt().toString()) }
    var discountText by remember { mutableStateOf("0") }
    var monthPaidFor by remember { mutableStateOf(targetMonth) }
    var selectedPaymentMode by remember { mutableStateOf("CASH") } // "CASH", "UPI", "CARD", "NET_BANKING"
    var remarksText by remember { mutableStateOf("Tuition fee for $targetMonth") }
    var feeTypeSelected by remember { mutableStateOf("Monthly-based") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF16A34A))
                        Column {
                            Text("Deposit Tuition Fee", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("TCMS Instant Receipt Voucher", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            bottomBar = {
                Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Primary Action: Deposit & WhatsApp Receipt
                        Button(
                            onClick = {
                                val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                                val disc = discountText.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val receiptNo = "REC-${System.currentTimeMillis() % 1000000}"
                                viewModel.collectFeePayment(
                                    studentId = duesSummary.student.id,
                                    amount = amt,
                                    discount = disc,
                                    feeType = feeTypeSelected,
                                    month = monthPaidFor,
                                    mode = selectedPaymentMode,
                                    remarks = remarksText
                                )

                                val samplePayment = FeePayment(
                                    studentId = duesSummary.student.id,
                                    feeType = feeTypeSelected,
                                    amountPaid = amt,
                                    discount = disc,
                                    paymentMode = selectedPaymentMode,
                                    remarks = remarksText,
                                    receiptNo = receiptNo,
                                    monthPaidFor = monthPaidFor
                                )

                                sendDigitalReceiptWhatsApp(
                                    context = context,
                                    payment = samplePayment,
                                    student = duesSummary.student,
                                    batchName = duesSummary.batch?.name ?: "Main Batch",
                                    academyName = academyName
                                )

                                Toast.makeText(context, "Fee of ₹${amt.toInt()} deposited successfully!", Toast.LENGTH_SHORT).show()
                                onPaymentRecorded(samplePayment)
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit & Send WhatsApp Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // 2. Secondary Row: Deposit & Print PDF | Deposit Only
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                                    val disc = discountText.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                        return@OutlinedButton
                                    }

                                    val receiptNo = "REC-${System.currentTimeMillis() % 1000000}"
                                    viewModel.collectFeePayment(
                                        studentId = duesSummary.student.id,
                                        amount = amt,
                                        discount = disc,
                                        feeType = feeTypeSelected,
                                        month = monthPaidFor,
                                        mode = selectedPaymentMode,
                                        remarks = remarksText
                                    )

                                    val samplePayment = FeePayment(
                                        studentId = duesSummary.student.id,
                                        feeType = feeTypeSelected,
                                        amountPaid = amt,
                                        discount = disc,
                                        paymentMode = selectedPaymentMode,
                                        remarks = remarksText,
                                        receiptNo = receiptNo,
                                        monthPaidFor = monthPaidFor
                                    )

                                    printFeeReceiptHtml(
                                        context = context,
                                        payment = samplePayment,
                                        student = duesSummary.student,
                                        batch = duesSummary.batch,
                                        academyName = academyName
                                    )

                                    Toast.makeText(context, "Fee deposited & sent to printer!", Toast.LENGTH_SHORT).show()
                                    onPaymentRecorded(samplePayment)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deposit & Print", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val amt = depositAmountText.toDoubleOrNull() ?: 0.0
                                    val disc = discountText.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                        return@FilledTonalButton
                                    }

                                    val receiptNo = "REC-${System.currentTimeMillis() % 1000000}"
                                    viewModel.collectFeePayment(
                                        studentId = duesSummary.student.id,
                                        amount = amt,
                                        discount = disc,
                                        feeType = feeTypeSelected,
                                        month = monthPaidFor,
                                        mode = selectedPaymentMode,
                                        remarks = remarksText
                                    )

                                    val samplePayment = FeePayment(
                                        studentId = duesSummary.student.id,
                                        feeType = feeTypeSelected,
                                        amountPaid = amt,
                                        discount = disc,
                                        paymentMode = selectedPaymentMode,
                                        remarks = remarksText,
                                        receiptNo = receiptNo,
                                        monthPaidFor = monthPaidFor
                                    )

                                    Toast.makeText(context, "Fee of ₹${amt.toInt()} deposited successfully!", Toast.LENGTH_SHORT).show()
                                    onPaymentRecorded(samplePayment)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deposit Only", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Student Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(duesSummary.student.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text("Roll No: ${duesSummary.student.rollNumber} • ${duesSummary.batch?.name ?: "Batch N/A"}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEE2E2),
                                border = BorderStroke(1.dp, Color(0xFFF87171))
                            ) {
                                Text(
                                    text = "Due: ₹${duesSummary.pendingDue.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB91C1C),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text("Monthly Fee: ₹${duesSummary.expectedFee.toInt()} | Already Paid: ₹${duesSummary.paidAmount.toInt()}", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }

                // Amount to Collect (prefilled with due amount)
                OutlinedTextField(
                    value = depositAmountText,
                    onValueChange = { depositAmountText = it },
                    label = { Text("Deposit Amount (₹) *") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quick pre-set amount chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { depositAmountText = duesSummary.pendingDue.toInt().toString() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Full Due (₹${duesSummary.pendingDue.toInt()})", fontSize = 11.sp)
                    }

                    if (duesSummary.pendingDue > 500) {
                        FilledTonalButton(
                            onClick = { depositAmountText = "500" },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("₹500 Part", fontSize = 11.sp)
                        }
                    }

                    if (duesSummary.pendingDue > 1000) {
                        FilledTonalButton(
                            onClick = { depositAmountText = "1000" },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("₹1000 Part", fontSize = 11.sp)
                        }
                    }
                }

                // Discount / Waiver
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("Discount / Waiver Concession (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Month Paid For
                OutlinedTextField(
                    value = monthPaidFor,
                    onValueChange = { monthPaidFor = it },
                    label = { Text("Month Paid For") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Payment Mode Selection Pills
                Text("Select Payment Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CASH", "UPI", "CARD", "NET_BANKING").forEach { mode ->
                        val isSelected = selectedPaymentMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { selectedPaymentMode = mode },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF16A34A) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF15803D) else Color(0xFFE2E8F0))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = mode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                if (selectedPaymentMode == "UPI") {
                    val merchantUpi = viewModel.merchantUpiId.collectAsState().value.ifBlank { "tamsacademy@upi" }
                    val depositAmt = depositAmountText.toDoubleOrNull() ?: 0.0
                    val upiUri = "upi://pay?pa=$merchantUpi&pn=${Uri.encode(academyName)}&am=$depositAmt&cu=INR&tn=${Uri.encode("Fee for ${duesSummary.student.name} ($monthPaidFor)")}"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                                    Text("Instant UPI Scan & Pay", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                    Text("₹${depositAmt.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF16A34A), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            // Dynamic Canvas QR Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.size(130.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                    val gridSize = 11
                                    val cellSize = size.width / gridSize
                                    val hash = kotlin.math.abs(upiUri.hashCode())

                                    for (r in 0 until gridSize) {
                                        for (c in 0 until gridSize) {
                                            val isFinder = (r < 3 && c < 3) || (r < 3 && c >= gridSize - 3) || (r >= gridSize - 3 && c < 3)
                                            val isFilled = isFinder || (((hash shr ((r * 4 + c) % 31)) and 1) == 1)
                                            if (isFilled) {
                                                drawRect(
                                                    color = Color(0xFF0F172A),
                                                    topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                                    size = androidx.compose.ui.geometry.Size(cellSize * 0.9f, cellSize * 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text("UPI ID: $merchantUpi", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                            Text("Scan using GPay, PhonePe, Paytm, or BHIM", fontSize = 10.sp, color = Color.Gray)

                            Button(
                                onClick = {
                                    try {
                                        val upiIntent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                                        context.startActivity(Intent.createChooser(upiIntent, "Pay via UPI App"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay via UPI App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Remarks
                OutlinedTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    label = { Text("Remarks / Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }
    }
}

// =========================================================================
// TCMS DIRECT DEPOSIT SECTION (त्वरित फीस जमा)
// =========================================================================

@Composable
fun DirectDepositSection(
    students: List<Student>,
    batches: List<Batch>,
    selectedMonth: String,
    viewModel: AppViewModel,
    academyName: String
) {
    val context = LocalContext.current
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var monthPaid by remember { mutableStateOf(selectedMonth) }
    var remarks by remember { mutableStateOf("Tuition fee payment") }

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students.take(15)
        else students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.rollNumber.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedStudent = remember(students, selectedStudentId) {
        students.find { it.id == selectedStudentId }
    }

    val batchMap = remember(batches) { batches.associateBy { it.id } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Quick Direct Fee Deposit", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text("Select student to immediately record tuition fee deposit and generate receipt.", fontSize = 11.sp, color = Color.Gray)

            // Search student
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Student by Name or Roll No") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // Student selection dropdown / chips
            if (selectedStudent == null) {
                Text("Tap student to select:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    filteredStudents.forEach { s ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStudentId = s.id
                                    val b = batchMap[s.batchId]
                                    amountText = (b?.feesAmount?.toInt() ?: 1200).toString()
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${s.name} (Roll: ${s.rollNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(batchMap[s.batchId]?.name ?: "Batch N/A", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                // Selected Student Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(selectedStudent.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                            Text("Roll: ${selectedStudent.rollNumber} • ${batchMap[selectedStudent.batchId]?.name ?: ""}", fontSize = 11.sp, color = Color(0xFF1E40AF))
                        }
                        TextButton(onClick = { selectedStudentId = null }) {
                            Text("Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Month
                OutlinedTextField(
                    value = monthPaid,
                    onValueChange = { monthPaid = it },
                    label = { Text("Month Paid For") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Mode
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "CASH", "CARD").forEach { m ->
                        val isSel = paymentMode == m
                        FilterChip(
                            selected = isSel,
                            onClick = { paymentMode = m },
                            label = { Text(m) }
                        )
                    }
                }

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        val disc = discountText.toDoubleOrNull() ?: 0.0
                        if (amt <= 0.0) {
                            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val receipt = "REC-${System.currentTimeMillis() % 1000000}"
                        viewModel.collectFeePayment(
                            studentId = selectedStudent.id,
                            amount = amt,
                            discount = disc,
                            feeType = "Monthly-based",
                            month = monthPaid,
                            mode = paymentMode,
                            remarks = remarks
                        )

                        val p = FeePayment(
                            studentId = selectedStudent.id,
                            feeType = "Monthly-based",
                            amountPaid = amt,
                            discount = disc,
                            paymentMode = paymentMode,
                            remarks = remarks,
                            receiptNo = receipt,
                            monthPaidFor = monthPaid
                        )

                        sendDigitalReceiptWhatsApp(context, p, selectedStudent, batchMap[selectedStudent.batchId]?.name ?: "", academyName)
                        Toast.makeText(context, "Fee of ₹${amt.toInt()} deposited & receipt sent!", Toast.LENGTH_LONG).show()
                        selectedStudentId = null
                        amountText = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deposit & Generate Receipt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================
// TCMS SMART FEE REMINDER DIALOG (फीस रिमाइंडर - WhatsApp & SMS)
// =========================================================================

@Composable
fun TcmsFeeReminderDialog(
    duesSummary: StudentDuesSummary,
    academyName: String,
    merchantUpiId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val phone = duesSummary.student.parentPhone.ifBlank { duesSummary.student.phone }

    val defaultReminderText = remember(duesSummary, academyName, merchantUpiId) {
        val effectiveUpi = merchantUpiId.ifBlank { "smtsharma282.sks@okaxis" }
        val amount = duesSummary.pendingDue.toInt()
        val clickToPayLink = "https://upiqr.in/pay/$effectiveUpi/$amount"
        val upiIntent = "upi://pay?pa=$effectiveUpi&pn=${Uri.encode(academyName)}&am=$amount&cu=INR&tn=${Uri.encode("Tuition Fee for ${duesSummary.student.name}")}"

        "📢 *FEE REMINDER - $academyName*\n\n" +
                "Dear Parent/Guardian,\n" +
                "This is a gentle reminder that the tuition fee for *${duesSummary.student.name}* (Roll No: ${duesSummary.student.rollNumber}) for the month of *${duesSummary.month}* is currently pending.\n\n" +
                "📌 *Pending Amount: ₹$amount*\n\n" +
                "👉 *Click to Pay via Any UPI App (GPay / PhonePe / Paytm / BHIM):*\n" +
                "$clickToPayLink\n\n" +
                "🔗 *Direct UPI Link:* $upiIntent\n" +
                "💳 *UPI ID:* $effectiveUpi\n\n" +
                "Kindly clear the fee at your earliest convenience.\n\n" +
                "Thank you,\n" +
                "*$academyName*"
    }

    var reminderMessage by remember { mutableStateOf(defaultReminderText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.NotificationAdd, contentDescription = null, tint = Color(0xFFEA580C))
                Text("Send Fee Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Recipient: ${duesSummary.student.name} (${if (phone.isNotBlank()) phone else "No contact number"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = reminderMessage,
                    onValueChange = { reminderMessage = it },
                    label = { Text("Reminder Message") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val raw = phone.replace(Regex("[^0-9]"), "")
                        if (raw.isBlank()) {
                            Toast.makeText(context, "No contact number for student", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val cleanPhone = when {
                            raw.startsWith("91") && raw.length == 12 -> raw
                            raw.startsWith("0") && raw.length == 11 -> "91" + raw.substring(1)
                            raw.length == 10 -> "91$raw"
                            else -> raw
                        }
                        try {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(reminderMessage)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            onDismiss()
                        } catch (e: Exception) {
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, reminderMessage)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Fee Reminder"))
                                onDismiss()
                            } catch (e2: Exception) {
                                Toast.makeText(context, "Could not open messaging app", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp")
                }

                OutlinedButton(
                    onClick = {
                        val raw = phone.replace(Regex("[^0-9]"), "")
                        if (raw.isBlank()) {
                            Toast.makeText(context, "No contact number for student", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        try {
                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$raw")).apply {
                                putExtra("sms_body", reminderMessage)
                            }
                            context.startActivity(smsIntent)
                            onDismiss()
                        } catch (e: Exception) {
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, reminderMessage)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send SMS"))
                                onDismiss()
                            } catch (e2: Exception) {
                                Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("SMS")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// =========================================================================
// TCMS OFFICIAL FEE RECEIPT VIEWER & PDF PRINT ENGINE
// =========================================================================

@Composable
fun TcmsFeeReceiptViewerDialog(
    payment: FeePayment,
    student: Student?,
    batch: Batch?,
    academyName: String,
    adminAddress: String,
    adminPhone: String,
    adminEmail: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val studentName = student?.name ?: "Student #${payment.studentId}"
    val rollNo = student?.rollNumber ?: "N/A"
    val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.datePaid))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tuition Fee Receipt Voucher", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            bottomBar = {
                Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                printFeeReceiptHtml(context, payment, student, batch, academyName)
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print PDF Receipt")
                        }

                        Button(
                            onClick = {
                                sendDigitalReceiptWhatsApp(context, payment, student, batch?.name ?: "Main Batch", academyName)
                            },
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share WhatsApp")
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Official Receipt Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = academyName.uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFF15803D),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "OFFICIAL TUITION PAYMENT RECEIPT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                letterSpacing = 0.5.sp
                            )
                            if (adminAddress.isNotBlank()) {
                                Text(adminAddress, fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        HorizontalDivider(color = Color(0xFF16A34A), thickness = 1.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt No: ${payment.receiptNo}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                            Text(dateStr, fontSize = 10.sp, color = Color.Gray)
                        }

                        // Student Details
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            ReceiptDetailRow("Student Name", studentName.uppercase())
                            ReceiptDetailRow("Roll Number", rollNo)
                            ReceiptDetailRow("Batch Name", batch?.name ?: "Preparatory Batch")
                            if (!student?.parentName.isNullOrBlank()) {
                                ReceiptDetailRow("Father/Guardian", student?.parentName ?: "")
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // Payment Particulars
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fee Particulars", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Amount", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tuition installment (${payment.monthPaidFor})", fontSize = 11.sp, color = Color(0xFF334155))
                            Text("₹${(payment.amountPaid + payment.discount).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (payment.discount > 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Scholarship / Waiver Rebate", fontSize = 10.sp, color = Color(0xFFB45309))
                                Text("- ₹${payment.discount.toInt()}", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // Amount Received Highlight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET RECEIVED (PAID):", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF0F172A))
                            Text("₹${payment.amountPaid.toInt()}", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color(0xFF15803D))
                        }

                        Text(
                            text = "Payment Mode: ${payment.paymentMode} | Status: SUCCESSFUL SETTLED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )

                        if (payment.remarks.isNotBlank()) {
                            Text("Remarks: ${payment.remarks}", fontSize = 10.sp, color = Color.Gray)
                        }

                        // Seal & Signatures
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("PAID", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                HorizontalDivider(modifier = Modifier.width(80.dp), color = Color.Gray)
                                Text("Payment Status", fontSize = 9.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Authorized Signatory", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                HorizontalDivider(modifier = Modifier.width(110.dp), color = Color.Gray)
                                Text("Institute Seal", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Text(
                            text = "Generated electronically via TCMS Core • Valid without physical stamp",
                            fontSize = 8.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("$label:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

/**
 * Sends TCMS formatted digital receipt via WhatsApp
 */
fun sendDigitalReceiptWhatsApp(
    context: Context,
    payment: FeePayment,
    student: Student?,
    batchName: String,
    academyName: String
) {
    val phone = student?.parentPhone?.ifBlank { student.phone } ?: ""
    val studentName = student?.name ?: "Student #${payment.studentId}"
    val rollNo = student?.rollNumber ?: "N/A"
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.datePaid))

    val message = "🧾 *${academyName.uppercase()} - FEE RECEIPT*\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "📌 *Receipt No:* ${payment.receiptNo}\n" +
            "📅 *Date:* $dateStr\n" +
            "👤 *Student:* $studentName\n" +
            "🔢 *Roll No:* $rollNo\n" +
            "📚 *Batch:* $batchName\n" +
            "🗓️ *Fee Month:* ${payment.monthPaidFor}\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "💰 *Amount Received:* ₹${payment.amountPaid.toInt()}\n" +
            (if (payment.discount > 0) "🎁 *Discount:* ₹${payment.discount.toInt()}\n" else "") +
            "💳 *Payment Mode:* ${payment.paymentMode}\n" +
            "✅ *Status:* SUCCESSFULLY PAID & RECORDED\n" +
            "━━━━━━━━━━━━━━━━━━━━\n" +
            "Thank you for your prompt payment!\n" +
            "— *$academyName*"

    if (phone.isNotBlank()) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").let {
            if (it.length == 10) "91$it" else it
        }
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: Exception) {
            shareTextGeneral(context, message)
        }
    } else {
        shareTextGeneral(context, message)
    }
}

private fun shareTextGeneral(context: Context, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt Via"))
}

/**
 * Print Fee Receipt via Android PrintManager
 */
fun printFeeReceiptHtml(
    context: Context,
    payment: FeePayment,
    student: Student?,
    batch: Batch?,
    academyName: String
) {
    val studentName = student?.name ?: "Student #${payment.studentId}"
    val rollNo = student?.rollNumber ?: "N/A"
    val dateStr = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault()).format(Date(payment.datePaid))

    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; color: #1e293b; }
                .receipt { border: 2px solid #16A34A; padding: 20px; border-radius: 8px; max-width: 500px; margin: 0 auto; }
                .header { text-align: center; border-bottom: 2px solid #16A34A; padding-bottom: 8px; margin-bottom: 12px; }
                .academy { font-size: 18px; font-weight: bold; color: #15803D; text-transform: uppercase; }
                .sub { font-size: 11px; font-weight: bold; color: #166534; margin-top: 3px; }
                .meta-row { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 10px; color: #64748b; }
                .details { border-bottom: 1px solid #e2e8f0; padding-bottom: 8px; margin-bottom: 10px; font-size: 12px; line-height: 1.6; }
                .particulars { width: 100%; border-collapse: collapse; margin-bottom: 12px; font-size: 12px; }
                .particulars th { text-align: left; border-bottom: 1px solid #cbd5e1; padding: 5px 0; }
                .particulars td { padding: 6px 0; }
                .total-row { border-top: 2px solid #1e293b; font-weight: bold; font-size: 14px; color: #15803D; }
                .status { background: #dcfce7; color: #166534; padding: 4px 8px; border-radius: 4px; font-size: 10px; font-weight: bold; display: inline-block; margin-top: 6px; }
                .footer { margin-top: 30px; display: flex; justify-content: space-between; font-size: 11px; font-weight: bold; }
                .sign { border-top: 1px solid #64748b; padding-top: 4px; width: 120px; text-align: center; }
            </style>
        </head>
        <body>
            <div class="receipt">
                <div class="header">
                    <div class="academy">$academyName</div>
                    <div class="sub">TUITION FEE PAYMENT RECEIPT</div>
                </div>
                <div class="meta-row">
                    <span>Receipt No: <strong>${payment.receiptNo}</strong></span>
                    <span>Date: $dateStr</span>
                </div>
                <div class="details">
                    <div>Student Name: <strong>${studentName.uppercase()}</strong></div>
                    <div>Roll Number: <strong>$rollNo</strong> &nbsp;|&nbsp; Batch: <strong>${batch?.name ?: "All Batches"}</strong></div>
                    ${if (!student?.parentName.isNullOrBlank()) "<div>Father/Guardian: ${student?.parentName}</div>" else ""}
                </div>
                <table class="particulars">
                    <thead>
                        <tr>
                            <th>Particulars</th>
                            <th style="text-align: right;">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>Tuition installment (${payment.monthPaidFor})</td>
                            <td style="text-align: right;">₹${(payment.amountPaid + payment.discount).toInt()}</td>
                        </tr>
                        ${if (payment.discount > 0) "<tr><td style='color: #b45309;'>Discount / Waiver</td><td style='text-align: right; color: #b45309;'>- ₹${payment.discount.toInt()}</td></tr>" else ""}
                        <tr class="total-row">
                            <td>TOTAL PAID (${payment.paymentMode}):</td>
                            <td style="text-align: right;">₹${payment.amountPaid.toInt()}</td>
                        </tr>
                    </tbody>
                </table>
                <div class="status">PAYMENT SETTLED &amp; CONFIRMED</div>
                <div class="footer">
                    <div class="sign">Student/Parent</div>
                    <div class="sign">Authorized Signatory</div>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("FeeReceipt_${payment.receiptNo}")
            printManager?.print("Fee_Receipt_${payment.receiptNo}", printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
}
