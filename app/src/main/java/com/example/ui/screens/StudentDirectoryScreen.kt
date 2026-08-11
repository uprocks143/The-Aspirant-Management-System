package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Batch
import com.example.data.model.Student
import com.example.services.FirebaseService
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDirectoryScreen(
    viewModel: AppViewModel,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val students by viewModel.students.collectAsState()
    val batches by viewModel.batches.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All Grades") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) } // null = All Batches

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEditStatus by remember { mutableStateOf<Student?>(null) }
    var isSyncingCloud by remember { mutableStateOf(false) }

    // List of grade/class options derived dynamically + defaults
    val gradeOptions = remember(students) {
        val classesFromStudents = students.map { it.studentClass }.filter { it.isNotBlank() }.distinct()
        val defaultGrades = listOf("Class 8th", "Class 9th", "Class 10th", "Class 11th", "Class 12th", "JEE / NEET")
        (listOf("All Grades") + (classesFromStudents + defaultGrades).distinct()).sorted()
    }

    val statusOptions = listOf("All Status", "Active", "On-Leave", "Inactive", "Graduated")

    // Filter students
    val filteredStudents = remember(students, searchQuery, selectedGradeFilter, selectedStatusFilter, selectedBatchFilter) {
        students.filter { student ->
            // Search match (Name, Roll No, Phone, Parent)
            val matchesSearch = searchQuery.isBlank() ||
                    student.name.contains(searchQuery, ignoreCase = true) ||
                    student.rollNumber.contains(searchQuery, ignoreCase = true) ||
                    student.phone.contains(searchQuery, ignoreCase = true) ||
                    student.parentName.contains(searchQuery, ignoreCase = true) ||
                    student.parentPhone.contains(searchQuery, ignoreCase = true)

            // Grade/Class match
            val matchesGrade = selectedGradeFilter == "All Grades" ||
                    student.studentClass.equals(selectedGradeFilter, ignoreCase = true)

            // Enrollment Status match
            val matchesStatus = selectedStatusFilter == "All Status" ||
                    student.enrollmentStatus.equals(selectedStatusFilter, ignoreCase = true)

            // Batch match
            val matchesBatch = selectedBatchFilter == null || student.batchId == selectedBatchFilter

            matchesSearch && matchesGrade && matchesStatus && matchesBatch
        }
    }

    // Stats
    val activeCount = remember(students) { students.count { it.enrollmentStatus.equals("Active", true) } }
    val leaveCount = remember(students) { students.count { it.enrollmentStatus.equals("On-Leave", true) } }
    val inactiveCount = remember(students) { students.count { it.enrollmentStatus.equals("Inactive", true) } }
    val graduatedCount = remember(students) { students.count { it.enrollmentStatus.equals("Graduated", true) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Firestore Student Directory", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Realtime Firestore Sync • ${students.size} Total Students",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Directory",
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncingCloud = true
                                try {
                                    FirebaseService.initialize(context)
                                    students.forEach { st ->
                                        FirebaseService.syncStudentToCloud(st)
                                    }
                                    Toast.makeText(context, "Synced ${students.size} students to Cloud Firestore!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Sync error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSyncingCloud = false
                                }
                            }
                        }
                    ) {
                        if (isSyncingCloud) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud", tint = Color(0xFF0D47A1))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStudentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadgeCard(
                    title = "Active",
                    count = activeCount,
                    color = Color(0xFF2E7D32),
                    container = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusFilter = "Active" }
                )
                StatusBadgeCard(
                    title = "On Leave",
                    count = leaveCount,
                    color = Color(0xFFEF6C00),
                    container = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusFilter = "On-Leave" }
                )
                StatusBadgeCard(
                    title = "Inactive",
                    count = inactiveCount,
                    color = Color(0xFFC62828),
                    container = Color(0xFFFFEBEE),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusFilter = "Inactive" }
                )
                StatusBadgeCard(
                    title = "Graduated",
                    count = graduatedCount,
                    color = Color(0xFF6A1B9A),
                    container = Color(0xFFF3E5F5),
                    modifier = Modifier.weight(1f),
                    onClick = { selectedStatusFilter = "Graduated" }
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search student name, roll no, phone, parent...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Grade / Class Filter Chips Horizontal Scroll
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FILTER BY GRADE / CLASS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gradeOptions) { grade ->
                        FilterChip(
                            selected = selectedGradeFilter == grade,
                            onClick = { selectedGradeFilter = grade },
                            label = { Text(grade, fontSize = 12.sp) },
                            leadingIcon = if (selectedGradeFilter == grade) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Enrollment Status Filter Row & Batch Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enrollment status chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(statusOptions) { status ->
                        val isSel = selectedStatusFilter == status
                        AssistChip(
                            onClick = { selectedStatusFilter = status },
                            label = { Text(status, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }

                if (selectedStatusFilter != "All Status" || selectedGradeFilter != "All Grades" || searchQuery.isNotEmpty() || selectedBatchFilter != null) {
                    TextButton(
                        onClick = {
                            searchQuery = ""
                            selectedGradeFilter = "All Grades"
                            selectedStatusFilter = "All Status"
                            selectedBatchFilter = null
                        }
                    ) {
                        Text("Reset", fontSize = 11.sp, color = Color.Red)
                    }
                }
            }

            // Results summary text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredStudents.size} of ${students.size} students",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )

                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Firestore Online", fontSize = 10.sp, color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Student Cards List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No students found matching your criteria.", fontWeight = FontWeight.Medium, color = Color.Gray)
                        Text("Try adjusting your grade or enrollment status filter.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val studentBatch = remember(batches, student.batchId) { batches.find { it.id == student.batchId } }
                        StudentDirectoryCard(
                            student = student,
                            batch = studentBatch,
                            onStatusClick = { studentToEditStatus = student },
                            onCall = { phone ->
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not launch dialer", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onWhatsApp = { phone ->
                                try {
                                    val cleaned = phone.replace(Regex("[^0-9]"), "")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$cleaned"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog for status change & Firestore sync
    if (studentToEditStatus != null) {
        val st = studentToEditStatus!!
        var newStatus by remember { mutableStateOf(st.enrollmentStatus) }
        var newGrade by remember { mutableStateOf(st.studentClass.ifEmpty { "Class 10th" }) }

        AlertDialog(
            onDismissRequest = { studentToEditStatus = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Student Status & Grade", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Student: ${st.name} (Roll: ${st.rollNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Text("ENROLLMENT STATUS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Active", "On-Leave", "Inactive", "Graduated").forEach { statusChoice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { newStatus = statusChoice }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = newStatus == statusChoice, onClick = { newStatus = statusChoice })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(statusChoice, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newGrade,
                        onValueChange = { newGrade = it },
                        label = { Text("Grade / Class") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = st.copy(
                            enrollmentStatus = newStatus,
                            studentClass = newGrade
                        )
                        viewModel.addStudent(updated) // Saves to Room DB
                        scope.launch {
                            FirebaseService.syncStudentToCloud(updated) // Syncs to Firestore
                        }
                        Toast.makeText(context, "Updated ${st.name} status to $newStatus and synced to Firestore!", Toast.LENGTH_SHORT).show()
                        studentToEditStatus = null
                    }
                ) {
                    Text("Save & Sync Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToEditStatus = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog to add a new student directly into Firestore directory
    if (showAddStudentDialog) {
        var newName by remember { mutableStateOf("") }
        var newRoll by remember { mutableStateOf("R-${(100..999).random()}") }
        var newClass by remember { mutableStateOf("Class 10th") }
        var newParent by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("") }
        var newParentPhone by remember { mutableStateOf("") }
        var newStatus by remember { mutableStateOf("Active") }
        var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }

        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Student to Firestore Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRoll,
                            onValueChange = { newRoll = it },
                            label = { Text("Roll Number") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newClass,
                            onValueChange = { newClass = it },
                            label = { Text("Grade / Class") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Student Mobile Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newParent,
                            onValueChange = { newParent = it },
                            label = { Text("Parent Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newParentPhone,
                            onValueChange = { newParentPhone = it },
                            label = { Text("Parent Mobile") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("STATUS:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Active", "On-Leave", "Inactive").forEach { st ->
                            FilterChip(
                                selected = newStatus == st,
                                onClick = { newStatus = st },
                                label = { Text(st, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank()) {
                            Toast.makeText(context, "Student Name is required!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newStudent = Student(
                            id = System.currentTimeMillis(),
                            batchId = selectedBatchId,
                            rollNumber = newRoll,
                            name = newName,
                            parentName = newParent.ifEmpty { "Guardian" },
                            dateOfBirth = "2008-01-01",
                            phone = newPhone,
                            parentPhone = newParentPhone,
                            studentClass = newClass,
                            enrollmentStatus = newStatus,
                            startDate = "2026-04-01"
                        )
                        viewModel.addStudent(newStudent)
                        scope.launch {
                            FirebaseService.syncStudentToCloud(newStudent)
                        }
                        Toast.makeText(context, "Added ${newStudent.name} & Synced to Firestore!", Toast.LENGTH_SHORT).show()
                        showAddStudentDialog = false
                    }
                ) {
                    Text("Save & Sync Firestore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadgeCard(
    title: String,
    count: Int,
    color: Color,
    container: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
fun StudentDirectoryCard(
    student: Student,
    batch: Batch?,
    onStatusClick: () -> Unit,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val statusColor = when (student.enrollmentStatus.lowercase()) {
        "active" -> Color(0xFF2E7D32)
        "on-leave", "leave" -> Color(0xFFEF6C00)
        "inactive" -> Color(0xFFC62828)
        "graduated" -> Color(0xFF6A1B9A)
        else -> Color.Gray
    }

    val statusBg = when (student.enrollmentStatus.lowercase()) {
        "active" -> Color(0xFFE8F5E9)
        "on-leave", "leave" -> Color(0xFFFFF3E0)
        "inactive" -> Color(0xFFFFEBEE)
        "graduated" -> Color(0xFFF3E5F5)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initial Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusBg)
                    .border(1.5.dp, statusColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )

                    // Enrollment Status Pill
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onStatusClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = student.enrollmentStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Grade Badge
                    if (student.studentClass.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = student.studentClass,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = "Roll: ${student.rollNumber}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    if (batch != null) {
                        Text(
                            text = "• ${batch.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (student.parentName.isNotBlank() || student.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Parent: ${student.parentName} (${student.parentPhone.ifEmpty { student.phone }})",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Quick Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (student.phone.isNotBlank()) {
                    IconButton(
                        onClick = { onCall(student.phone) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF1E88E5), modifier = Modifier.size(18.dp))
                    }
                }

                if (student.parentPhone.isNotBlank() || student.phone.isNotBlank()) {
                    IconButton(
                        onClick = { onWhatsApp(student.parentPhone.ifEmpty { student.phone }) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
