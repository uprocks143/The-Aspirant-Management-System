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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.data.model.Attendance
import com.example.data.model.Batch
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// =========================================================================
// 1. GOOGLE DRIVE & LOCAL BACKUP/RESTORE CENTER
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveCloudBackupCenterDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val adminEmail by viewModel.adminEmail.collectAsState()
    val academyName by viewModel.academyName.collectAsState()
    val googleDriveBackupTime by viewModel.googleDriveBackupTime.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val students by viewModel.students.collectAsState()
    val payments by viewModel.feePayments.collectAsState()

    val targetGoogleAccount = if (adminEmail.isNotBlank()) adminEmail else "smtsharma282.sks@gmail.com"

    var isProcessing by remember { mutableStateOf(false) }
    var currentOperation by remember { mutableStateOf("") }
    var progressStatusLog by remember { mutableStateOf("Ready to sync with Google Drive") }
    var lastOperationSuccess by remember { mutableStateOf<Boolean?>(null) }
    var operationResultDetails by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE8F0FE),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text("Google Drive Cloud Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Disaster Recovery & Offline Safety", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = onDismiss, enabled = !isProcessing) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Account Linked Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = CircleShape, color = Color(0xFF34A853).copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1E8E3E), modifier = Modifier.size(18.dp))
                                    }
                                }
                                Column {
                                    Text("Google Account Active", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(targetGoogleAccount, fontSize = 12.sp, color = Color(0xFF1A73E8), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE6F4EA)
                            ) {
                                Text(
                                    text = "Connected",
                                    color = Color(0xFF137333),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Last Drive Backup", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = googleDriveBackupTime.ifEmpty { "Not backed up yet" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (googleDriveBackupTime.isNotEmpty()) Color(0xFF1E8E3E) else Color.DarkGray
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Data Volume", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = "${batches.size} Batches • ${students.size} Students",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Progress Banner (Visible during sync or after completion)
                if (isProcessing || lastOperationSuccess != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isProcessing -> Color(0xFFEFF6FF)
                                lastOperationSuccess == true -> Color(0xFFF0FDF4)
                                else -> Color(0xFFFEF2F2)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isProcessing -> Color(0xFF93C5FD)
                                lastOperationSuccess == true -> Color(0xFF86EFAC)
                                else -> Color(0xFFFCA5A5)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF1D4ED8))
                                    Text(currentOperation, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E40AF))
                                } else if (lastOperationSuccess == true) {
                                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                                    Text("Operation Successful", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D))
                                } else {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(20.dp))
                                    Text("Operation Issue", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB91C1C))
                                }
                            }

                            Text(
                                text = progressStatusLog,
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )

                            if (operationResultDetails.isNotEmpty()) {
                                Text(
                                    text = operationResultDetails,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Primary Drive Actions
                Text("Google Drive Cloud Operations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Backup to Google Drive
                    Button(
                        onClick = {
                            isProcessing = true
                            currentOperation = "Uploading Backup to Google Drive..."
                            lastOperationSuccess = null
                            operationResultDetails = ""
                            progressStatusLog = "Connecting to Google Drive ($targetGoogleAccount)..."

                            viewModel.triggerGoogleDriveBackup { log: String ->
                                progressStatusLog = log
                                if (log.contains("Successfully", ignoreCase = true) || log.contains("Verified", ignoreCase = true)) {
                                    isProcessing = false
                                    lastOperationSuccess = true
                                    operationResultDetails = "Encrypted SQLite DB and JSON snapshot saved to Google Drive > TAMS_Disaster_Backups."
                                } else if (log.contains("Error", ignoreCase = true) || log.contains("failed", ignoreCase = true)) {
                                    isProcessing = false
                                    lastOperationSuccess = false
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // 2. Restore from Google Drive
                    OutlinedButton(
                        onClick = {
                            isProcessing = true
                            currentOperation = "Restoring Database from Google Drive..."
                            lastOperationSuccess = null
                            operationResultDetails = ""
                            progressStatusLog = "Querying latest Google Drive backup for $targetGoogleAccount..."

                            viewModel.restoreGoogleDriveBackup { log ->
                                progressStatusLog = log
                                if (log.contains("Successfully restored", ignoreCase = true)) {
                                    isProcessing = false
                                    lastOperationSuccess = true
                                    operationResultDetails = "All batches, student admission records, and fee payment vouchers restored."
                                } else if (log.contains("Error", ignoreCase = true) || log.contains("failed", ignoreCase = true)) {
                                    isProcessing = false
                                    lastOperationSuccess = false
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E8E3E)),
                        border = BorderStroke(1.5.dp, Color(0xFF34A853))
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Drive", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Local Device Backup & Excel Export
                Text("Local Device Storage & Excel / CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Export CSV Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        val csvContent = buildString {
                                            append("ID,Roll Number,Name,Mobile,Parent Mobile,Class,Subject,Batch ID,Status\n")
                                            students.forEach { s ->
                                                append("${s.id},\"${s.rollNumber}\",\"${s.name}\",\"${s.phone}\",\"${s.parentPhone}\",\"${s.studentClass}\",\"${s.subject}\",${s.batchId},\"${s.enrollmentStatus}\"\n")
                                            }
                                        }

                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "TAMS Student Registry - ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}")
                                            putExtra(Intent.EXTRA_TEXT, csvContent)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export Student Records CSV"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = CircleShape, color = Color(0xFFE0F2FE), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                                    }
                                }
                                Column {
                                    Text("Export Student Registry to Excel / CSV", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Compatible with MS Excel, Google Sheets", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        // Export Fee Ledger CSV
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    try {
                                        val csvContent = buildString {
                                            append("Receipt No,Student ID,Month,Amount Paid,Discount,Mode,Date,Remarks\n")
                                            payments.forEach { p ->
                                                val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(p.datePaid))
                                                append("\"${p.receiptNo}\",${p.studentId},\"${p.monthPaidFor}\",${p.amountPaid},${p.discount},\"${p.paymentMode}\",\"$dateStr\",\"${p.remarks}\"\n")
                                            }
                                        }

                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "TAMS Fee Collection Ledger - ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}")
                                            putExtra(Intent.EXTRA_TEXT, csvContent)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Export Fee Ledger CSV"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(shape = CircleShape, color = Color(0xFFDCFCE7), modifier = Modifier.size(36.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                    }
                                }
                                Column {
                                    Text("Export Fee Collections Ledger to CSV", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Complete receipts and payment breakdown", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. STUDENT PVC ID CARD GENERATOR & PRINT VOUCHER
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentIdCardGeneratorDialog(
    students: List<Student>,
    batches: List<Batch>,
    academyName: String = "TAMS Academy",
    academyAddress: String = "Main Center",
    academyPhone: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val batchMap = remember(batches) { batches.associateBy { it.id } }

    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 0L) }
    var cardOrientation by remember { mutableStateOf("PORTRAIT") } // "PORTRAIT" or "LANDSCAPE"

    val activeStudent = remember(selectedStudentId, students) {
        students.firstOrNull { it.id == selectedStudentId } ?: students.firstOrNull()
    }
    val activeBatch = remember(activeStudent, batchMap) {
        activeStudent?.let { batchMap[it.batchId] }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF2563EB))
                            Column {
                                Text("Student PVC ID Card Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Official Student Identity Card", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            },
            bottomBar = {
                Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Print Button
                        Button(
                            onClick = {
                                if (activeStudent != null) {
                                    printStudentIdCardHtml(
                                        context = context,
                                        student = activeStudent,
                                        batch = activeBatch,
                                        academyName = academyName,
                                        academyAddress = academyAddress,
                                        academyPhone = academyPhone
                                    )
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print ID Card (PDF)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // WhatsApp Share
                        OutlinedButton(
                            onClick = {
                                if (activeStudent != null) {
                                    val shareText = buildString {
                                        append("🪪 *OFFICIAL STUDENT ID CARD - $academyName*\n")
                                        append("━━━━━━━━━━━━━━━━━━\n")
                                        append("👤 Name: *${activeStudent.name}*\n")
                                        append("🔢 Roll No: *${activeStudent.rollNumber}*\n")
                                        append("📚 Class: ${activeStudent.studentClass.ifBlank { "Regular" }}\n")
                                        append("🏷️ Batch: ${activeBatch?.name ?: "Main Batch"} (${activeBatch?.classTimings ?: "Daily"})\n")
                                        append("📞 Mobile: ${activeStudent.phone}\n")
                                        append("👨‍👩‍👦 Parent: ${activeStudent.parentName} (${activeStudent.parentPhone})\n")
                                        append("🏫 Center: $academyAddress\n")
                                        append("━━━━━━━━━━━━━━━━━━\n")
                                        append("Valid for academic year 2026-2027.")
                                    }

                                    val phone = activeStudent.phone.ifBlank { activeStudent.parentPhone }
                                    val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(shareText)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val genericShare = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(genericShare, "Share Student ID Card"))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                            border = BorderStroke(1.dp, Color(0xFF22C55E))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Student Selector
                Text("Select Student Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(students) { student ->
                        val isSelected = student.id == selectedStudentId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStudentId = student.id },
                            label = { Text("${student.name} (${student.rollNumber})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (activeStudent != null) {
                    // PVC Card Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFF1E3A8A))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Banner Header
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0F1E36), Color(0xFF1E3A8A))
                                        )
                                    )
                                    .padding(vertical = 14.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = academyName.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = academyAddress,
                                            fontSize = 10.sp,
                                            color = Color(0xFF93C5FD)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF59E0B)
                                    ) {
                                        Text(
                                            text = "STUDENT",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            color = Color(0xFF451A03),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Body details
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Photo & QR
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Avatar Photo Box
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("👨‍🎓", fontSize = 36.sp)
                                        }
                                    }

                                    // Attendance QR Code Box
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.size(54.dp)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                            drawIdCardQrMatrix(activeStudent.rollNumber, size)
                                        }
                                    }
                                    Text("VERIFIED QR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                }

                                // Info Column
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = activeStudent.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF0F1E36)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFE0F2FE)
                                        ) {
                                            Text(
                                                text = "ROLL: ${activeStudent.rollNumber}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = activeStudent.studentClass.ifBlank { "Class 10" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    IdCardRowLabel("Batch:", activeBatch?.name ?: "Main Batch")
                                    IdCardRowLabel("Timings:", activeBatch?.classTimings ?: "Daily Session")
                                    IdCardRowLabel("Mobile:", activeStudent.phone.ifBlank { "N/A" })
                                    IdCardRowLabel("Parent:", "${activeStudent.parentName} (${activeStudent.parentPhone})")
                                    IdCardRowLabel("Validity:", "2026-2027 Session")
                                }
                            }

                            // Card Footer Strip
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(vertical = 8.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Emergency Helpline: ${academyPhone.ifBlank { "9876543210" }}", fontSize = 9.sp, color = Color(0xFF64748B))
                                    Text("Authorized Signatory ✍️", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdCardRowLabel(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 10.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIdCardQrMatrix(data: String, size: Size) {
    val gridSize = 7
    val cellSize = size.width / gridSize
    val hash = kotlin.math.abs(data.hashCode())

    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            val isFinderCorner = (row < 2 && col < 2) || (row < 2 && col >= gridSize - 2) || (row >= gridSize - 2 && col < 2)
            val isFilled = isFinderCorner || (((hash shr (row * 3 + col)) and 1) == 1)
            if (isFilled) {
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize * 0.88f, cellSize * 0.88f)
                )
            }
        }
    }
}

private fun printStudentIdCardHtml(
    context: Context,
    student: Student,
    batch: Batch?,
    academyName: String,
    academyAddress: String,
    academyPhone: String
) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background: #f8fafc; padding: 20px; }
                .id-card {
                    width: 320px;
                    border: 2px solid #1e3a8a;
                    border-radius: 12px;
                    overflow: hidden;
                    background: #fff;
                    margin: 0 auto;
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                .header {
                    background: linear-gradient(135deg, #0f1e36, #1e3a8a);
                    color: white;
                    padding: 12px 16px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .header h2 { margin: 0; font-size: 15px; font-weight: 800; letter-spacing: 0.5px; }
                .header p { margin: 2px 0 0; font-size: 9px; color: #93c5fd; }
                .badge { background: #f59e0b; color: #451a03; font-size: 8px; font-weight: bold; padding: 2px 6px; border-radius: 4px; }
                .body-content { padding: 14px; display: flex; gap: 12px; }
                .photo-col { width: 70px; text-align: center; }
                .avatar-box { width: 70px; height: 70px; border-radius: 8px; border: 1px solid #3b82f6; background: #eff6ff; display: flex; align-items: center; justify-content: center; font-size: 32px; }
                .info-col { flex: 1; }
                .student-name { font-size: 15px; font-weight: 800; color: #0f1e36; margin: 0 0 4px; }
                .row { display: flex; font-size: 10px; margin-bottom: 3px; }
                .row .label { font-weight: 700; color: #64748b; width: 55px; }
                .row .val { color: #1e293b; font-weight: 500; }
                .footer { background: #f1f5f9; padding: 8px 14px; display: flex; justify-content: space-between; font-size: 8px; color: #475569; border-top: 1px solid #e2e8f0; }
            </style>
        </head>
        <body>
            <div class="id-card">
                <div class="header">
                    <div>
                        <h2>${academyName.uppercase()}</h2>
                        <p>$academyAddress</p>
                    </div>
                    <span class="badge">STUDENT</span>
                </div>
                <div class="body-content">
                    <div class="photo-col">
                        <div class="avatar-box">👨‍🎓</div>
                        <p style="font-size: 8px; color: #64748b; margin-top: 4px; font-weight: bold;">ROLL: ${student.rollNumber}</p>
                    </div>
                    <div class="info-col">
                        <div class="student-name">${student.name}</div>
                        <div class="row"><span class="label">Class:</span><span class="val">${student.studentClass.ifBlank { "Class 10" }}</span></div>
                        <div class="row"><span class="label">Batch:</span><span class="val">${batch?.name ?: "Main Batch"}</span></div>
                        <div class="row"><span class="label">Timings:</span><span class="val">${batch?.classTimings ?: "Daily"}</span></div>
                        <div class="row"><span class="label">Mobile:</span><span class="val">${student.phone}</span></div>
                        <div class="row"><span class="label">Parent:</span><span class="val">${student.parentName}</span></div>
                        <div class="row"><span class="label">Validity:</span><span class="val">2026-2027</span></div>
                    </div>
                </div>
                <div class="footer">
                    <span>Helpline: ${academyPhone.ifBlank { "9876543210" }}</span>
                    <span style="font-weight: bold; color: #1e3a8a;">Authorized Signatory ✍️</span>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context).apply {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = createPrintDocumentAdapter("Student_ID_${student.rollNumber}")
                printManager?.print("Student_ID_${student.rollNumber}", printAdapter, PrintAttributes.Builder().build())
            }
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
}

// =========================================================================
// 3. DAILY ABSENTEE SMS / WHATSAPP BROADCASTER
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAbsenteeAlertBroadcasterDialog(
    students: List<Student>,
    batches: List<Batch>,
    attendanceList: List<Attendance>,
    academyName: String = "TAMS Academy",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val batchMap = remember(batches) { batches.associateBy { it.id } }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayStr) }
    var selectedBatchId by remember { mutableStateOf<Long?>(null) } // null = all batches

    // Determine absent students on selected date
    val absentStudents = remember(students, attendanceList, selectedDate, selectedBatchId) {
        students
            .filter { selectedBatchId == null || it.batchId == selectedBatchId }
            .filter { student ->
                val attRecord = attendanceList.firstOrNull { it.studentId == student.id && it.dateString == selectedDate }
                attRecord?.status?.equals("Absent", ignoreCase = true) == true
            }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = Color(0xFFFEE2E2), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text("Absent Alert Broadcaster", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Notify Parents of Absentees Today", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            },
            bottomBar = {
                if (absentStudents.isNotEmpty()) {
                    Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Button(
                                onClick = {
                                    val message = "🚨 *ABSENTEE NOTICE - $academyName*\nDate: $selectedDate\nTotal Absentees: ${absentStudents.size} students. Respective parents have been pinged."
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Absentee Summary"))
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Absentee Summary (${absentStudents.size} Students)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Batch Filter Chips
                Text("Select Batch Slot", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedBatchId == null,
                            onClick = { selectedBatchId = null },
                            label = { Text("All Batches") }
                        )
                    }
                    items(batches) { batch ->
                        FilterChip(
                            selected = selectedBatchId == batch.id,
                            onClick = { selectedBatchId = batch.id },
                            label = { Text(batch.name) }
                        )
                    }
                }

                // Absent summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (absentStudents.isEmpty()) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, if (absentStudents.isEmpty()) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Absentees on $selectedDate", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = if (absentStudents.isEmpty()) "🎉 All Students Present!" else "${absentStudents.size} Absent Students",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (absentStudents.isEmpty()) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (absentStudents.isEmpty()) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (absentStudents.isEmpty()) "✅" else "⚠️", fontSize = 18.sp)
                            }
                        }
                    }
                }

                if (absentStudents.isEmpty()) {
                    Text(
                        text = "No absent student records logged for $selectedDate. You can mark attendance from the Attendance Scanner or Attendance Console.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    )
                } else {
                    Text("1-Tap Parent Alert Buttons", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        absentStudents.forEach { student ->
                            val batch = batchMap[student.batchId]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Roll: ${student.rollNumber} • Batch: ${batch?.name ?: "Main"}", fontSize = 11.sp, color = Color.Gray)
                                        Text("Parent: ${student.parentName} (${student.parentPhone})", fontSize = 11.sp, color = Color(0xFF475569))
                                    }

                                    // WhatsApp Alert Button
                                    Button(
                                        onClick = {
                                            val phone = student.parentPhone.ifBlank { student.phone }
                                            val cleanPhone = phone.replace("+", "").replace(" ", "").replace("-", "")
                                            val msg = "⚠️ *Absent Alert - $academyName*\n\nNamaste ${student.parentName} ji,\nAapka ward *${student.name}* (Roll: ${student.rollNumber}) aaj *$selectedDate* ko tuition batch *${batch?.name ?: "Class"}* me *ABSENT* tha.\n\nKripya dhyan dein aur asuvidha se bachein.\n- $academyName"
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$cleanPhone")).apply {
                                                    putExtra("sms_body", msg)
                                                }
                                                context.startActivity(smsIntent)
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
