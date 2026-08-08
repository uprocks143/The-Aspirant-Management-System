package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.Exam
import com.example.data.model.ExamAttempt
import com.example.data.model.ExamQuestion
import com.example.data.model.Student
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSchedulerOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database states
    val exams by viewModel.exams.collectAsState(initial = emptyList())
    val batches by viewModel.batches.collectAsState(initial = emptyList())
    val students by viewModel.students.collectAsState(initial = emptyList())

    // Tabs
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("📅 Schedule & Manage", "📊 Exam Analytics")

    // Active selected exam for question management
    var selectedExamForQuestions by remember { mutableStateOf<Exam?>(null) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .safeDrawingPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Exam Scheduling & Analytics",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Set exam dates, subjects, MCQ/FITB questions, and track scores",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> ScheduleAndManageTab(
                        exams = exams,
                        batches = batches,
                        viewModel = viewModel,
                        selectedExamForQuestions = selectedExamForQuestions,
                        onSelectExamForQuestions = { selectedExamForQuestions = it }
                    )
                    1 -> ExamAnalyticsTab(
                        exams = exams,
                        students = students,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAndManageTab(
    exams: List<Exam>,
    batches: List<Batch>,
    viewModel: AppViewModel,
    selectedExamForQuestions: Exam?,
    onSelectExamForQuestions: (Exam?) -> Unit
) {
    val context = LocalContext.current
    val qList = if (selectedExamForQuestions != null) {
        viewModel.getQuestionsForExamFlow(selectedExamForQuestions.id).collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }

    // Scheduling Form States
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var examDate by remember { mutableStateOf("") }
    var examTime by remember { mutableStateOf("") }
    var selectedBatchId by remember { mutableStateOf<Long?>(null) }
    var isBatchDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedExamForQuestions == null) {
            // 1. Scheduling Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Schedule a New Exam",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Exam Title (e.g. Midterm, Unit Test 1)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject / Syllabus (e.g. Physics, Chemistry)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Duration & Date / Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Duration (Mins)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = examDate,
                                onValueChange = { examDate = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                placeholder = { Text("e.g. 2026-07-20") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Time & Batch selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = examTime,
                                onValueChange = { examTime = it },
                                label = { Text("Time (e.g. 10:00 AM)") },
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Batch Dropdown Box
                            Box(modifier = Modifier.weight(1f)) {
                                val currentBatchName = batches.find { it.id == selectedBatchId }?.name ?: "Select Batch"
                                OutlinedButton(
                                    onClick = { isBatchDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = currentBatchName,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        fontSize = 12.sp
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = isBatchDropdownExpanded,
                                    onDismissRequest = { isBatchDropdownExpanded = false }
                                ) {
                                    batches.forEach { batch ->
                                        DropdownMenuItem(
                                            text = { Text(batch.name) },
                                            onClick = {
                                                selectedBatchId = batch.id
                                                isBatchDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (title.isBlank() || subject.isBlank() || selectedBatchId == null) {
                                    Toast.makeText(context, "Please enter title, subject, and select a batch!", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val durationVal = duration.toIntOrNull() ?: 30
                                viewModel.createExam(
                                    batchId = selectedBatchId!!,
                                    title = title,
                                    subject = subject,
                                    duration = durationVal,
                                    examDate = examDate,
                                    examTime = examTime
                                )
                                Toast.makeText(context, "Exam scheduled successfully!", Toast.LENGTH_SHORT).show()
                                // reset
                                title = ""
                                subject = ""
                                examDate = ""
                                examTime = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Schedule & Create Exam")
                        }
                    }
                }
            }

            // 2. Scheduled Exams List
            item {
                Text(
                    text = "Scheduled & Created Exams (${exams.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (exams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No exams scheduled yet. Use the form above.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(exams) { exam ->
                    val batchName = batches.find { it.id == exam.batchId }?.name ?: "Unknown Batch"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Subject: ${exam.subject}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Batch Segment: $batchName", fontSize = 12.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${exam.durationMinutes} Mins",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date & Time
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (exam.examDate.isNotBlank()) "Scheduled: ${exam.examDate}" else "Self-paced",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                if (exam.examTime.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = exam.examTime,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.deleteExam(exam) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onSelectExamForQuestions(exam) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Questions Bank", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 3. Questions Bank Management Sub-View
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onSelectExamForQuestions(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Questions Bank: ${selectedExamForQuestions.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            item {
                QuestionCreatorCard(
                    examId = selectedExamForQuestions.id,
                    viewModel = viewModel
                )
            }

            item {
                Text(
                    text = "Added Questions Pool",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (qList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No questions added to this exam yet. Create one above!", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(qList) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (q.questionType == "MCQ") Color(0xFFE3F2FD) else Color(0xFFE8F5E9))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (q.questionType == "MCQ") "MCQ" else "Fill Blank (FITB)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (q.questionType == "MCQ") Color(0xFF1E88E5) else Color(0xFF43A047)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = q.questionText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            if (q.questionType == "MCQ") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("• A: ${q.optionA}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("• B: ${q.optionB}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("• C: ${q.optionC}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("• D: ${q.optionD}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Correct Answer: ${q.correctAnswer}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCreatorCard(
    examId: Long,
    viewModel: AppViewModel
) {
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("MCQ") } // "MCQ" or "FITB"
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Create & Inject New Question",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Select Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Question Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "MCQ", onClick = { type = "MCQ"; correctAnswer = "A" })
                    Text("MCQ", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "FITB", onClick = { type = "FITB"; correctAnswer = "" })
                    Text("Fill in Blank", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Question Text
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Question Text (For FITB, use _____ to indicate blank)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (type == "MCQ") {
                // Options list
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Correct Answer A, B, C, D Selection
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Select Correct Option:  ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    listOf("A", "B", "C", "D").forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                            RadioButton(selected = correctAnswer == opt, onClick = { correctAnswer = opt })
                            Text(opt)
                        }
                    }
                }
            } else {
                // FITB exact correct answer
                OutlinedTextField(
                    value = correctAnswer,
                    onValueChange = { correctAnswer = it },
                    label = { Text("Exact Correct Answer (Case insensitive comparison)") },
                    placeholder = { Text("e.g. Gravity, Newton, 42") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (text.isBlank()) {
                        Toast.makeText(context, "Question text cannot be blank!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (type == "MCQ" && (optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank() || correctAnswer.isBlank())) {
                        Toast.makeText(context, "Please fill in all MCQ options and choose a correct answer!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (type == "FITB" && correctAnswer.isBlank()) {
                        Toast.makeText(context, "FITB correct answer text cannot be blank!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    viewModel.addExamQuestion(
                        examId = examId,
                        text = text,
                        a = optionA,
                        b = optionB,
                        c = optionC,
                        d = optionD,
                        correct = correctAnswer,
                        type = type
                    )

                    Toast.makeText(context, "Question added successfully!", Toast.LENGTH_SHORT).show()
                    // reset
                    text = ""
                    optionA = ""
                    optionB = ""
                    optionC = ""
                    optionD = ""
                    if (type == "MCQ") correctAnswer = "A" else correctAnswer = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Exam Question Pool")
            }
        }
    }
}

@Composable
fun ExamAnalyticsTab(
    exams: List<Exam>,
    students: List<Student>,
    viewModel: AppViewModel
) {
    if (exams.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scheduled exams available for analytical review.", color = Color.Gray)
        }
        return
    }

    var selectedExamIndex by remember { mutableStateOf(0) }
    val selectedExam = exams.getOrNull(selectedExamIndex)

    var examAttempts by remember { mutableStateOf<List<ExamAttempt>>(emptyList()) }

    // Reactive attempts retrieval
    if (selectedExam != null) {
        val attemptsFlow = remember(selectedExam.id) { viewModel.getAttemptsForExamFlow(selectedExam.id) }
        val stateAttempts by attemptsFlow.collectAsState(initial = emptyList())
        examAttempts = stateAttempts
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dropdown Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Exam for Metrics Review:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(selectedExam?.title ?: "Select Exam", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            exams.forEachIndexed { i, ex ->
                                DropdownMenuItem(
                                    text = { Text("${ex.title} (${ex.subject})") },
                                    onClick = {
                                        selectedExamIndex = i
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedExam == null) return@LazyColumn

        // Metric Counters Card
        item {
            val totalAttempts = examAttempts.size
            val avgScore = if (totalAttempts > 0) {
                examAttempts.map { (it.score.toDouble() / it.totalScore) * 100 }.average()
            } else 0.0
            val passRate = if (totalAttempts > 0) {
                examAttempts.count { (it.score.toDouble() / it.totalScore) >= 0.5 }.toDouble() / totalAttempts * 100
            } else 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Live Analytics: ${selectedExam.title}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Total Enrolled takers", fontSize = 11.sp, color = Color.Gray)
                            Text("$totalAttempts", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Average Grade", fontSize = 11.sp, color = Color.Gray)
                            Text("%.1f%%".format(avgScore), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF2E7D32))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Pass Percentage", fontSize = 11.sp, color = Color.Gray)
                            Text("%.1f%%".format(passRate), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }

        // Student Attempts List
        item {
            Text("Students Marks Sheet & Progress Cards", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (examAttempts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No students have taken this exam yet.", color = Color.Gray)
                    }
                }
            }
        } else {
            items(examAttempts) { attempt ->
                val student = students.find { it.id == attempt.studentId }
                val pct = (attempt.score.toDouble() / attempt.totalScore) * 100
                val scoreColor = if (pct >= 50.0) Color(0xFF2E7D32) else Color(0xFFD32F2F)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(student?.name ?: "Unknown Roll ID", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Roll Number: ${student?.rollNumber ?: "N/A"}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${attempt.score} / ${attempt.totalScore}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = scoreColor
                                )
                                Text(
                                    "Grade: %.1f%%".format(pct),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Duration Spent: ${attempt.timeSpentSeconds} seconds",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (pct >= 50.0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (pct >= 50.0) "PASSED" else "NEEDS HELP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (pct >= 50.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
