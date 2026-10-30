package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Batch
import com.example.data.model.Student
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

// =========================================================================
// 1. DATA MODELS & BIRTHDAY LOGIC (TCMS Automated Student Birthday Alerts)
// =========================================================================

data class StudentBirthdayInfo(
    val student: Student,
    val batch: Batch?,
    val isToday: Boolean,
    val daysUntil: Int, // 0 = today, 1..7 = upcoming this week
    val turningAge: Int,
    val birthDateFormatted: String
)

/**
 * Parses DOB and determines whether today or upcoming birthday
 */
fun parseStudentBirthdays(students: List<Student>, batches: List<Batch>): List<StudentBirthdayInfo> {
    val batchMap = batches.associateBy { it.id }
    val todayCal = Calendar.getInstance()
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val currentYear = todayCal.get(Calendar.YEAR)

    val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    )

    val result = mutableListOf<StudentBirthdayInfo>()

    for (student in students) {
        if (student.dateOfBirth.isBlank()) continue

        var parsedDate: Date? = null
        for (format in dateFormats) {
            try {
                parsedDate = format.parse(student.dateOfBirth.trim())
                if (parsedDate != null) break
            } catch (_: Exception) { }
        }

        if (parsedDate != null) {
            val birthCal = Calendar.getInstance().apply { time = parsedDate }
            val birthMonth = birthCal.get(Calendar.MONTH)
            val birthDay = birthCal.get(Calendar.DAY_OF_MONTH)
            val birthYear = birthCal.get(Calendar.YEAR)

            val turningAge = if (birthYear in 1900..currentYear) currentYear - birthYear else 0

            // Target birthday this calendar year
            val thisYearBday = Calendar.getInstance().apply {
                set(Calendar.MONTH, birthMonth)
                set(Calendar.DAY_OF_MONTH, birthDay)
                set(Calendar.YEAR, currentYear)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val todayZero = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var diffMillis = thisYearBday.timeInMillis - todayZero.timeInMillis
            if (diffMillis < 0) {
                // Birthday already passed this year, check next year
                thisYearBday.add(Calendar.YEAR, 1)
                diffMillis = thisYearBday.timeInMillis - todayZero.timeInMillis
            }

            val daysUntil = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
            val isToday = (birthMonth == todayMonth && birthDay == todayDay)

            val formatted = SimpleDateFormat("dd MMMM", Locale.getDefault()).format(parsedDate)

            result.add(
                StudentBirthdayInfo(
                    student = student,
                    batch = batchMap[student.batchId],
                    isToday = isToday,
                    daysUntil = if (isToday) 0 else daysUntil,
                    turningAge = turningAge,
                    birthDateFormatted = formatted
                )
            )
        }
    }

    return result.sortedWith(compareBy({ !it.isToday }, { it.daysUntil }))
}

/**
 * Sends personalized birthday greeting via WhatsApp
 */
fun sendBirthdayGreetingWhatsApp(context: Context, info: StudentBirthdayInfo, instituteName: String) {
    val phone = info.student.phone.ifBlank { info.student.parentPhone }
    if (phone.isBlank()) {
        Toast.makeText(context, "No contact number found for ${info.student.name}", Toast.LENGTH_SHORT).show()
        return
    }

    val cleanPhone = phone.replace(Regex("[^0-9]"), "").let {
        if (it.length == 10) "91$it" else it
    }

    val message = "🎉🎂 *HAPPY BIRTHDAY ${info.student.name.uppercase()}!* 🎂🎉\n\n" +
            "Dear ${info.student.name},\n" +
            "Wishing you a very Happy Birthday! May this special year bring you boundless knowledge, wisdom, academic excellence, and great success in all your endeavors.\n\n" +
            "🌟 Keep shining and achieving great milestones!\n\n" +
            "— With warm wishes & blessings,\n" +
            "*$instituteName*"

    try {
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
            putExtra("sms_body", message)
        }
        try {
            context.startActivity(smsIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Cannot launch WhatsApp or SMS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Sends personalized birthday greeting via standard SMS
 */
fun sendBirthdayGreetingSMS(context: Context, info: StudentBirthdayInfo, instituteName: String) {
    val phone = info.student.phone.ifBlank { info.student.parentPhone }
    if (phone.isBlank()) {
        Toast.makeText(context, "No contact number found for ${info.student.name}", Toast.LENGTH_SHORT).show()
        return
    }

    val message = "Happy Birthday ${info.student.name}! Wishing you grand success, wisdom, and joy in your studies and life ahead. Best wishes from $instituteName! 🎉🎂"
    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
        putExtra("sms_body", message)
    }
    try {
        context.startActivity(smsIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "SMS app not available: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

// =========================================================================
// 2. DASHBOARD BIRTHDAY ALERT BANNER (Vibrant TCMS Celebration Widget)
// =========================================================================

@Composable
fun DashboardBirthdayAlertBanner(
    students: List<Student>,
    batches: List<Batch>,
    instituteName: String,
    onOpenAllBirthdays: () -> Unit
) {
    val context = LocalContext.current
    val birthdayList = remember(students, batches) { parseStudentBirthdays(students, batches) }
    val todayBirthdays = remember(birthdayList) { birthdayList.filter { it.isToday } }
    val upcomingThisWeek = remember(birthdayList) { birthdayList.filter { !it.isToday && it.daysUntil in 1..7 } }

    if (todayBirthdays.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.5.dp, Color(0xFFEC4899))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFCE7F3),
                                Color(0xFFFFF1F2),
                                Color(0xFFEDE9FE)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEC4899),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cake,
                                        contentDescription = "Birthday Cake",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Today's Birthday Alerts!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF9D174D)
                                    )
                                    Badge(containerColor = Color(0xFFBE185D)) {
                                        Text("${todayBirthdays.size}", color = Color.White)
                                    }
                                }
                                Text(
                                    text = "Send wishes directly to make students feel special",
                                    fontSize = 11.sp,
                                    color = Color(0xFF831843)
                                )
                            }
                        }

                        TextButton(onClick = onOpenAllBirthdays) {
                            Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBE185D))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBE185D), modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF472B6).copy(alpha = 0.4f))

                    // List of today's birthday celebrants
                    todayBirthdays.forEach { info ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFDF2F8),
                                        border = BorderStroke(1.dp, Color(0xFFF472B6)),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = info.student.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDB2777),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = info.student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "Roll: ${info.student.rollNumber} • ${info.batch?.name ?: "Batch N/A"}${if (info.turningAge > 0) " • Turns ${info.turningAge} yrs" else ""}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalButton(
                                        onClick = { sendBirthdayGreetingWhatsApp(context, info, instituteName) },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF22C55E),
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { sendBirthdayGreetingSMS(context, info, instituteName) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else if (upcomingThisWeek.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAllBirthdays() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFFEC4899))
                    Text(
                        text = "Upcoming Birthdays this week (${upcomingThisWeek.size} students)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

// =========================================================================
// 3. COMPLETE BIRTHDAYS HUB DIALOG (TCMS Calendar & Wish Station)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllBirthdaysHubDialog(
    students: List<Student>,
    batches: List<Batch>,
    instituteName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchId by remember { mutableStateOf<Long?>(null) }
    var selectedMonthFilter by remember { mutableStateOf<Int?>(null) } // null = All, 0 = Jan, 11 = Dec

    val allBirthdays = remember(students, batches) { parseStudentBirthdays(students, batches) }

    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val filteredList = remember(allBirthdays, searchQuery, selectedBatchId, selectedMonthFilter) {
        allBirthdays.filter { info ->
            val matchQuery = searchQuery.isBlank() ||
                    info.student.name.contains(searchQuery, ignoreCase = true) ||
                    info.student.rollNumber.contains(searchQuery, ignoreCase = true)

            val matchBatch = selectedBatchId == null || info.student.batchId == selectedBatchId

            val matchMonth = if (selectedMonthFilter == null) {
                true
            } else {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = sdf.parse(info.student.dateOfBirth.trim())
                    if (d != null) {
                        val cal = Calendar.getInstance().apply { time = d }
                        cal.get(Calendar.MONTH) == selectedMonthFilter
                    } else false
                } catch (_: Exception) {
                    false
                }
            }

            matchQuery && matchBatch && matchMonth
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Cake, contentDescription = null, tint = Color(0xFFEC4899))
                            Column {
                                Text("Student Birthday Alerts", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("TCMS Automated Wish Station", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by student name or roll number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Months filter scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonthFilter == null,
                            onClick = { selectedMonthFilter = null },
                            label = { Text("All Months") }
                        )
                    }
                    items(months.indices.toList()) { index ->
                        FilterChip(
                            selected = selectedMonthFilter == index,
                            onClick = { selectedMonthFilter = if (selectedMonthFilter == index) null else index },
                            label = { Text(months[index]) }
                        )
                    }
                }

                // Batch filter scroll
                if (batches.isNotEmpty()) {
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
                        items(batches) { b ->
                            FilterChip(
                                selected = selectedBatchId == b.id,
                                onClick = { selectedBatchId = if (selectedBatchId == b.id) null else b.id },
                                label = { Text(b.name) }
                            )
                        }
                    }
                }

                Text(
                    text = "${filteredList.size} students found",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                // Student birthday list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Cake, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Text("No student birthdays found for the selected filter", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(filteredList) { info ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (info.isToday) Color(0xFFFDF2F8) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (info.isToday) Color(0xFFEC4899) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (info.isToday) Color(0xFFF472B6) else MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = info.student.name.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (info.isToday) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = info.student.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (info.isToday) {
                                                    Badge(containerColor = Color(0xFFEC4899)) {
                                                        Text("TODAY! 🎂", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "Roll: ${info.student.rollNumber} • ${info.batch?.name ?: "No Batch"} • DOB: ${info.student.dateOfBirth}",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )

                                            val countdownText = when {
                                                info.isToday -> "Turns ${info.turningAge} Today!"
                                                info.daysUntil == 1 -> "Tomorrow (in 1 day)"
                                                info.daysUntil in 2..30 -> "In ${info.daysUntil} days"
                                                else -> "On ${info.birthDateFormatted}"
                                            }

                                            Text(
                                                text = countdownText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (info.isToday) Color(0xFFDB2777) else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { sendBirthdayGreetingWhatsApp(context, info, instituteName) },
                                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFDCFCE7))
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { sendBirthdayGreetingSMS(context, info, instituteName) },
                                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(Icons.Default.Message, contentDescription = "SMS", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
}

// =========================================================================
// 4. TCMS COURSE COMPLETION TRACKER & CERTIFICATE GENERATOR
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCompletionTrackerDialog(
    students: List<Student>,
    batches: List<Batch>,
    academyName: String,
    onDismiss: () -> Unit
) {
    var selectedBatchId by remember { mutableStateOf<Long?>(batches.firstOrNull()?.id) }
    var searchQuery by remember { mutableStateOf("") }
    var studentForCertificate by remember { mutableStateOf<Student?>(null) }

    // Map tracking student custom course completion state (in-memory persistent representation)
    val courseProgressMap = remember { mutableStateMapOf<Long, Int>() }
    val courseStatusMap = remember { mutableStateMapOf<Long, String>() }

    // Initialize defaults
    LaunchedEffect(students) {
        students.forEach { s ->
            if (!courseProgressMap.containsKey(s.id)) {
                // If alumni or completed
                if (s.isAlumni || s.enrollmentStatus.equals("Completed", ignoreCase = true)) {
                    courseProgressMap[s.id] = 100
                    courseStatusMap[s.id] = "Completed"
                } else {
                    courseProgressMap[s.id] = 65 // Default active progress
                    courseStatusMap[s.id] = "In Progress"
                }
            }
        }
    }

    val filteredStudents = remember(students, selectedBatchId, searchQuery) {
        students.filter { s ->
            val matchBatch = (selectedBatchId == null || s.batchId == selectedBatchId)
            val matchSearch = searchQuery.isBlank() ||
                    s.name.contains(searchQuery, ignoreCase = true) ||
                    s.rollNumber.contains(searchQuery, ignoreCase = true)
            matchBatch && matchSearch
        }
    }

    val selectedBatch = remember(batches, selectedBatchId) {
        batches.find { it.id == selectedBatchId }
    }

    // Modal: Certificate Viewer
    studentForCertificate?.let { student ->
        CourseCompletionCertificateDialog(
            student = student,
            batch = batches.find { it.id == student.batchId },
            academyName = academyName,
            onDismiss = { studentForCertificate = null }
        )
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF7C3AED))
                            Column {
                                Text("Course Completion Tracker", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("TCMS Syllabus & Certificate Engine", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metrics overview banner
                val totalEnrolled = filteredStudents.size
                val completedCount = filteredStudents.count { (courseProgressMap[it.id] ?: 0) >= 100 }
                val inProgressCount = totalEnrolled - completedCount

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalEnrolled", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF5B21B6))
                            Text("Total Students", fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFC4B5FD))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$inProgressCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2563EB))
                            Text("In Progress", fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFC4B5FD))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$completedCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF16A34A))
                            Text("Completed", fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                    }
                }

                // Batch Selector chips
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
                    items(batches) { b ->
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = b.id },
                            label = { Text(b.name) }
                        )
                    }
                }

                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by student or roll number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // List of students with progress controls
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredStudents) { student ->
                        val currentProgress = courseProgressMap[student.id] ?: 65
                        val status = if (currentProgress >= 100) "Completed" else "In Progress"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Roll: ${student.rollNumber} • Subject: ${student.subject.ifEmpty { selectedBatch?.subject ?: "Mathematics" }}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (currentProgress >= 100) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)
                                    ) {
                                        Text(
                                            text = status,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (currentProgress >= 100) Color(0xFF16A34A) else Color(0xFF2563EB),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Progress bar + percentage
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { currentProgress / 100f },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (currentProgress >= 100) Color(0xFF16A34A) else Color(0xFF7C3AED)
                                    )
                                    Text(
                                        text = "$currentProgress%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Progress control buttons + Certificate action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        FilledTonalButton(
                                            onClick = {
                                                val next = (currentProgress - 15).coerceAtLeast(0)
                                                courseProgressMap[student.id] = next
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("-15%", fontSize = 11.sp)
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                val next = (currentProgress + 15).coerceAtMost(100)
                                                courseProgressMap[student.id] = next
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("+15%", fontSize = 11.sp)
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                courseProgressMap[student.id] = 100
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF15803D)),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Set 100%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = { studentForCertificate = student },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentProgress >= 100) Color(0xFF7C3AED) else MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (currentProgress >= 100) "Certificate" else "Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

// =========================================================================
// 5. OFFICIAL COURSE COMPLETION CERTIFICATE VIEWER & EXPORTER
// =========================================================================

@Composable
fun CourseCompletionCertificateDialog(
    student: Student,
    batch: Batch?,
    academyName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val issueDate = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()) }
    val certificateId = remember(student.id) { "CERT-${student.id}-${System.currentTimeMillis() % 100000}" }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Completion Certificate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val shareText = "🎓 Course Completion Certificate for ${student.name}\n" +
                                        "Certificate ID: $certificateId\n" +
                                        "Subject: ${student.subject.ifBlank { batch?.subject ?: "Curriculum Studies" }}\n" +
                                        "Issued by: $academyName on $issueDate"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Certificate Details"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Certificate")
                        }

                        Button(
                            onClick = {
                                printCertificateHtml(context, student, batch, academyName, certificateId, issueDate)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print PDF")
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
                // Certificate Canvas / Card design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFDF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(3.dp, Color(0xFFB45309))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .border(1.dp, Color(0xFFD97706).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top Emblem
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(2.dp, Color(0xFFB45309)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = academyName.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF78350F),
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "CERTIFICATE OF COURSE COMPLETION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF92400E),
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            modifier = Modifier.width(160.dp),
                            thickness = 2.dp,
                            color = Color(0xFFD97706)
                        )

                        Text(
                            text = "This is proudly presented to",
                            fontSize = 12.sp,
                            color = Color(0xFF78716C),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Text(
                            text = student.name.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Roll Number: ${student.rollNumber} • Batch: ${batch?.name ?: "Advanced Group"}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Text(
                            text = "for outstanding academic performance and successful completion of the course curriculum in",
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = student.subject.ifBlank { batch?.subject ?: "Standard Curriculum Coaching" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF78350F),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        // Seal and Signatures row
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(issueDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                HorizontalDivider(modifier = Modifier.width(90.dp), color = Color.Gray)
                                Text("Date of Issue", fontSize = 10.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFFFBEB),
                                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("SEAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    }
                                }
                                Text("Verified", fontSize = 9.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Director / Head", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                HorizontalDivider(modifier = Modifier.width(100.dp), color = Color.Gray)
                                Text("Authorized Signatory", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Certificate Serial: $certificateId • Verify on TCMS Portal",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Print Course Completion Certificate via Android WebView PrintManager
 */
fun printCertificateHtml(
    context: Context,
    student: Student,
    batch: Batch?,
    academyName: String,
    certificateId: String,
    issueDate: String
) {
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 20px; background: #fff; }
                .cert-border { border: 8px solid #B45309; padding: 25px; border-radius: 12px; text-align: center; }
                .cert-inner { border: 2px dashed #D97706; padding: 30px 20px; border-radius: 8px; }
                .academy { font-size: 26px; font-weight: bold; color: #78350F; letter-spacing: 2px; text-transform: uppercase; }
                .title { font-size: 18px; font-weight: bold; color: #92400E; margin-top: 10px; letter-spacing: 1px; }
                .sub { font-size: 14px; color: #78716C; margin-top: 15px; font-style: italic; }
                .name { font-size: 28px; font-weight: bold; color: #1E293B; margin: 15px 0; border-bottom: 2px solid #D97706; display: inline-block; padding: 0 30px 5px 30px; }
                .meta { font-size: 14px; color: #64748B; margin-bottom: 15px; }
                .desc { font-size: 15px; color: #475569; max-width: 600px; margin: 0 auto; line-height: 1.6; }
                .course { font-size: 18px; font-weight: bold; color: #78350F; background: #FEF3C7; padding: 8px 24px; border-radius: 6px; display: inline-block; margin: 15px 0; }
                .footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 40px; padding: 0 30px; }
                .sign-box { text-align: center; width: 140px; }
                .line { border-top: 1px solid #64748B; margin-top: 5px; padding-top: 4px; font-size: 12px; color: #64748B; }
                .serial { font-size: 10px; color: #94A3B8; margin-top: 25px; font-family: monospace; }
            </style>
        </head>
        <body>
            <div class="cert-border">
                <div class="cert-inner">
                    <div class="academy">$academyName</div>
                    <div class="title">CERTIFICATE OF COURSE COMPLETION</div>
                    <div class="sub">This is proudly conferred upon</div>
                    <div class="name">${student.name.uppercase()}</div>
                    <div class="meta">Roll Number: ${student.rollNumber} &nbsp;|&nbsp; Batch: ${batch?.name ?: "All Batches"}</div>
                    <div class="desc">
                        for successfully completing the comprehensive coaching syllabus and demonstrating commendable academic proficiency in
                    </div>
                    <div class="course">${student.subject.ifBlank { batch?.subject ?: "Academic Curriculum" }}</div>
                    <div class="footer">
                        <div class="sign-box">
                            <strong>$issueDate</strong>
                            <div class="line">Date of Issue</div>
                        </div>
                        <div class="sign-box">
                            <div style="border: 2px solid #B45309; border-radius: 50%; width: 50px; height: 50px; line-height: 50px; margin: 0 auto 5px auto; font-size: 11px; color: #B45309; font-weight: bold;">SEAL</div>
                            <div class="line">Institutional Stamp</div>
                        </div>
                        <div class="sign-box">
                            <strong>Director / Head</strong>
                            <div class="line">Authorized Signatory</div>
                        </div>
                    </div>
                    <div class="serial">Certificate ID: $certificateId &bull; Generated via TCMS Suite</div>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("Certificate_${student.rollNumber}")
            printManager?.print("Course_Certificate_${student.name}", printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
}

// =========================================================================
// 6. TCMS STUDENT REGISTRATION CARD / ADMISSION SLIP GENERATOR
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationSlipDialog(
    students: List<Student>,
    batches: List<Batch>,
    academyName: String,
    onDismiss: () -> Unit,
    initialStudent: Student? = null
) {
    val context = LocalContext.current
    var selectedStudent by remember { mutableStateOf<Student?>(initialStudent ?: students.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }

    val batchMap = remember(batches) { batches.associateBy { it.id } }
    val currentBatch = remember(selectedStudent, batchMap) {
        selectedStudent?.let { batchMap[it.batchId] }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0D9488))
                            Column {
                                Text("Student Registration Slip", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("TCMS Admission Record Card", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        selectedStudent?.let { s ->
                            IconButton(onClick = {
                                printRegistrationSlipHtml(context, s, currentBatch, academyName)
                            }) {
                                Icon(Icons.Default.Print, contentDescription = "Print Slip", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student picker if none or multiple
                if (students.size > 1) {
                    var expandedDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.let { "${it.name} (Roll: ${it.rollNumber})" } ?: "Select a student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Choose Student for Admission Slip") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.name} • Roll: ${s.rollNumber} • ${batchMap[s.batchId]?.name ?: ""}") },
                                    onClick = {
                                        selectedStudent = s
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                selectedStudent?.let { student ->
                    // Admission Slip Card Container
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFF0D9488)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Institute Header
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = academyName.uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF0F766E),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "OFFICIAL STUDENT ADMISSION SLIP & REGISTRATION RECORD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF115E59),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Affiliated with TCMS Tuition Management Core",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }

                            HorizontalDivider(color = Color(0xFF0D9488), thickness = 1.5.dp)

                            // Bio row with QR code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    SlipFieldRow("Roll Number", student.rollNumber)
                                    SlipFieldRow("Student Name", student.name)
                                    SlipFieldRow("Father's Name", student.parentName.ifBlank { "N/A" })
                                    SlipFieldRow("Date of Birth", student.dateOfBirth.ifBlank { "N/A" })
                                    SlipFieldRow("Gender", student.gender)
                                    SlipFieldRow("Admission Date", student.startDate.ifBlank { "Standard 2026" })
                                    SlipFieldRow("Student Mobile", student.phone.ifBlank { "N/A" })
                                    SlipFieldRow("Parent Contact", student.parentPhone.ifBlank { "N/A" })
                                }

                                // QR Code Canvas
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val qrData = "TCMS-REG:${student.rollNumber}|NAME:${student.name}|BATCH:${currentBatch?.name ?: ""}"
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val sizePx = size.width
                                            val gridCount = 15
                                            val cellSize = sizePx / gridCount
                                            val hash = qrData.hashCode()
                                            for (i in 0 until gridCount) {
                                                for (j in 0 until gridCount) {
                                                    val isCorner = (i < 4 && j < 4) || (i >= 11 && j < 4) || (i < 4 && j >= 11)
                                                    val filled = isCorner || (((i * 31 + j * 17) xor hash) % 3 == 0)
                                                    if (filled) {
                                                        drawRect(
                                                            color = Color.Black,
                                                            topLeft = androidx.compose.ui.geometry.Offset(i * cellSize, j * cellSize),
                                                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Text("Official QR", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Academic & Class Enrollment
                            Text("ACADEMIC ENROLLMENT DETAILS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F766E))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    SlipFieldRow("Enrolled Batch", currentBatch?.name ?: "General Batch")
                                    SlipFieldRow("Class / Grade", student.studentClass.ifBlank { "N/A" })
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    SlipFieldRow("Batch Timings", currentBatch?.classTimings ?: "Regular Session")
                                    SlipFieldRow("Primary Subject", student.subject.ifBlank { currentBatch?.subject ?: "All Subjects" })
                                }
                            }

                            SlipFieldRow("Residential Address", student.address.ifBlank { "Institute Registry Area" })

                            // Rules & Terms summary
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCCFBF1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("TERMS & INSTITUTIONAL RULES:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0F766E))
                                    Text("1. Minimum 75% attendance is required in assigned batches.", fontSize = 9.sp, color = Color(0xFF334155))
                                    Text("2. Fee deposits must be logged via the 'Tuition Fees' console.", fontSize = 9.sp, color = Color(0xFF334155))
                                    Text("3. Student must display QR Attendance Card during physical check-ins.", fontSize = 9.sp, color = Color(0xFF334155))
                                }
                            }

                            // Signatures
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Guardian / Parent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    HorizontalDivider(modifier = Modifier.width(110.dp), color = Color.Gray)
                                    Text("Signature & Date", fontSize = 9.sp, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Center Administrator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    HorizontalDivider(modifier = Modifier.width(120.dp), color = Color.Gray)
                                    Text("Authorized Seal", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Share and Print Buttons row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val slipSummary = "📄 *STUDENT ADMISSION SLIP*\n" +
                                        "Academy: $academyName\n" +
                                        "Roll No: ${student.rollNumber}\n" +
                                        "Student: ${student.name}\n" +
                                        "Parent: ${student.parentName}\n" +
                                        "Batch: ${currentBatch?.name ?: "Standard Batch"}\n" +
                                        "Class: ${student.studentClass}\n" +
                                        "Admission Date: ${student.startDate}\n\n" +
                                        "✅ Successfully enrolled in $academyName!"

                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, slipSummary)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Admission Slip"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Slip")
                        }

                        Button(
                            onClick = {
                                printRegistrationSlipHtml(context, student, currentBatch, academyName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print Slip PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlipFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Print Admission Slip / Registration Card HTML via Android PrintManager
 */
fun printRegistrationSlipHtml(
    context: Context,
    student: Student,
    batch: Batch?,
    academyName: String
) {
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; color: #1e293b; }
                .box { border: 2px solid #0D9488; padding: 20px; border-radius: 8px; }
                .header { text-align: center; border-bottom: 2px solid #0D9488; padding-bottom: 10px; margin-bottom: 15px; }
                .title { font-size: 20px; font-weight: bold; color: #0F766E; }
                .sub { font-size: 12px; font-weight: bold; color: #115E59; margin-top: 4px; }
                .grid { display: table; width: 100%; margin-bottom: 15px; }
                .row { display: table-row; }
                .cell-lbl { display: table-cell; width: 30%; font-weight: bold; padding: 5px 0; font-size: 13px; color: #475569; }
                .cell-val { display: table-cell; width: 70%; padding: 5px 0; font-size: 13px; color: #0f172a; font-weight: bold; }
                .section-title { font-size: 13px; font-weight: bold; color: #0F766E; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px; margin: 15px 0 10px 0; }
                .rules { background: #f0fdfa; border: 1px solid #ccfbf1; padding: 10px; border-radius: 6px; font-size: 11px; line-height: 1.5; color: #334155; margin-top: 15px; }
                .signs { margin-top: 35px; display: flex; justify-content: space-between; }
                .sign-col { text-align: center; width: 180px; border-top: 1px solid #64748b; padding-top: 5px; font-size: 12px; font-weight: bold; }
            </style>
        </head>
        <body>
            <div class="box">
                <div class="header">
                    <div class="title">${academyName.uppercase()}</div>
                    <div class="sub">STUDENT REGISTRATION RECORD &amp; ADMISSION SLIP</div>
                    <div style="font-size: 10px; color: #64748b; margin-top: 2px;">Official Copy &bull; TCMS Tuition Manager</div>
                </div>

                <div class="section-title">STUDENT IDENTITY PARTICULARS</div>
                <div class="grid">
                    <div class="row"><div class="cell-lbl">Roll Number:</div><div class="cell-val">${student.rollNumber}</div></div>
                    <div class="row"><div class="cell-lbl">Student Name:</div><div class="cell-val">${student.name}</div></div>
                    <div class="row"><div class="cell-lbl">Father / Guardian:</div><div class="cell-val">${student.parentName.ifBlank { "N/A" }}</div></div>
                    <div class="row"><div class="cell-lbl">Date of Birth:</div><div class="cell-val">${student.dateOfBirth.ifBlank { "N/A" }}</div></div>
                    <div class="row"><div class="cell-lbl">Gender:</div><div class="cell-val">${student.gender}</div></div>
                    <div class="row"><div class="cell-lbl">Admission Date:</div><div class="cell-val">${student.startDate.ifBlank { "Standard 2026" }}</div></div>
                    <div class="row"><div class="cell-lbl">Student Phone:</div><div class="cell-val">${student.phone.ifBlank { "N/A" }}</div></div>
                    <div class="row"><div class="cell-lbl">Parent Phone:</div><div class="cell-val">${student.parentPhone.ifBlank { "N/A" }}</div></div>
                    <div class="row"><div class="cell-lbl">Address:</div><div class="cell-val">${student.address.ifBlank { "Registered Area" }}</div></div>
                </div>

                <div class="section-title">ACADEMIC ENROLLMENT</div>
                <div class="grid">
                    <div class="row"><div class="cell-lbl">Assigned Batch:</div><div class="cell-val">${batch?.name ?: "Main Batch"}</div></div>
                    <div class="row"><div class="cell-lbl">Class / Grade:</div><div class="cell-val">${student.studentClass.ifBlank { "N/A" }}</div></div>
                    <div class="row"><div class="cell-lbl">Class Timings:</div><div class="cell-val">${batch?.classTimings ?: "Regular Session"}</div></div>
                    <div class="row"><div class="cell-lbl">Subject:</div><div class="cell-val">${student.subject.ifBlank { batch?.subject ?: "All Subjects" }}</div></div>
                </div>

                <div class="rules">
                    <strong>Institutional Terms:</strong><br>
                    1. Mandatory 75% attendance policy applies to all test series and lecture sessions.<br>
                    2. Monthly tuition fee instalments must be submitted via the designated Tuition Fees portal.<br>
                    3. Student ID card &amp; QR verification slip must be carried during classroom check-ins.
                </div>

                <div class="signs">
                    <div class="sign-col">Guardian Signature</div>
                    <div class="sign-col">Authorized Signatory / Seal</div>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("AdmissionSlip_${student.rollNumber}")
            printManager?.print("Admission_Slip_${student.name}", printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
}

// =========================================================================
// 7. TCMS BATCH MESSAGE BROADCASTER (Batch-wise SMS / WhatsApp Broadcast)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchMessageBroadcasterDialog(
    students: List<Student>,
    batches: List<Batch>,
    academyName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedBatchId by remember { mutableStateOf<Long?>(batches.firstOrNull()?.id) }
    var selectedCategory by remember { mutableStateOf("Announcement") }

    val batchMap = remember(batches) { batches.associateBy { it.id } }
    val currentBatch = remember(selectedBatchId, batchMap) {
        selectedBatchId?.let { batchMap[it] }
    }

    // Predefined templates
    val templates = remember(academyName, currentBatch?.name) {
        mapOf(
            "Announcement" to "📢 Important Notice: Dear Parents & Students of {batch_name}, please be informed about upcoming class activities at $academyName. Timings: {timings}. Thank you!",
            "Holiday" to "🏖️ Holiday Notice: $academyName will remain closed on tomorrow on account of upcoming festival/break. Regular classes for {batch_name} will resume on schedule.",
            "Exam Alert" to "📝 Exam Alert: Dear {student_name}, your unit test for {batch_name} is scheduled for this week. Please arrive 15 minutes before batch time. Best of luck! — $academyName",
            "Schedule Change" to "⏰ Timings Update: Kindly note the revised timing for {batch_name} classes at $academyName: {timings}. Please be seated on time.",
            "Fee Reminder" to "💳 Fee Notice: Dear Parent, this is a gentle reminder regarding the monthly tuition fee installment for {student_name} ({batch_name}). Kindly deposit via the Tuition Fees desk at $academyName."
        )
    }

    var customMessage by remember(selectedCategory) {
        mutableStateOf(templates[selectedCategory] ?: "")
    }

    val recipients = remember(students, selectedBatchId) {
        if (selectedBatchId == null) students else students.filter { it.batchId == selectedBatchId }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFEA580C))
                            Column {
                                Text("Batch Broadcast Engine", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text("TCMS Batch-Wise Outreach", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedBatchId == null,
                            onClick = { selectedBatchId = null },
                            label = { Text("All Batches (${students.size})") }
                        )
                    }
                    items(batches) { b ->
                        val count = students.count { it.batchId == b.id }
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = b.id },
                            label = { Text("${b.name} ($count)") }
                        )
                    }
                }

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val cats = listOf("Announcement", "Holiday", "Exam Alert", "Schedule Change", "Fee Reminder")
                    items(cats) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                customMessage = templates[cat] ?: ""
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                // Message Text Field
                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Broadcast Message (supports placeholders)") },
                    supportingText = { Text("Placeholders: {student_name}, {batch_name}, {timings}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick copy all contacts action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recipients (${recipients.size} students)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = {
                        val allNumbers = recipients.mapNotNull {
                            val p = it.phone.ifBlank { it.parentPhone }
                            p.ifBlank { null }
                        }.distinct().joinToString(", ")

                        clipboardManager.setText(AnnotatedString(allNumbers))
                        Toast.makeText(context, "Copied ${recipients.size} contact numbers!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy All Numbers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Recipients list with quick dispatch
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(recipients) { student ->
                        val batchName = batchMap[student.batchId]?.name ?: "General Batch"
                        val timings = batchMap[student.batchId]?.classTimings ?: "Regular Timings"
                        val formattedMsg = customMessage
                            .replace("{student_name}", student.name)
                            .replace("{batch_name}", batchName)
                            .replace("{timings}", timings)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    val phoneDisplay = student.phone.ifBlank { student.parentPhone.ifBlank { "No Number" } }
                                    Text("Roll: ${student.rollNumber} • Phone: $phoneDisplay", fontSize = 11.sp, color = Color.Gray)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilledTonalButton(
                                        onClick = {
                                            val targetPhone = student.phone.ifBlank { student.parentPhone }
                                            if (targetPhone.isBlank()) {
                                                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                                return@FilledTonalButton
                                            }
                                            val cleanPhone = targetPhone.replace(Regex("[^0-9]"), "").let {
                                                if (it.length == 10) "91$it" else it
                                            }
                                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(formattedMsg)}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val targetPhone = student.phone.ifBlank { student.parentPhone }
                                            if (targetPhone.isBlank()) {
                                                Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
                                                return@OutlinedButton
                                            }
                                            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$targetPhone")).apply {
                                                putExtra("sms_body", formattedMsg)
                                            }
                                            context.startActivity(smsIntent)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("SMS", fontSize = 11.sp)
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
