package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.example.security.BiometricSecurityHeaderBanner
import com.example.security.BiometricSecurityManager
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

    val isProtected by viewModel.isStudentPaymentBiometricProtected.collectAsState()
    val isUnlocked by viewModel.isStudentAndPaymentBiometricUnlocked.collectAsState()

    val students by viewModel.students.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("All Grades") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var selectedPendingDuesFilter by remember { mutableStateOf("ALL") } // "ALL", "HAS_DUES", "PAID_CLEAR"
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) } // null = All Batches
    var activeCategoryFilter by remember { mutableStateOf("NONE") } // "NONE", "GRADE", "STATUS", "DUES", "BATCH"

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentToEditStatus by remember { mutableStateOf<Student?>(null) }
    var isSyncingCloud by remember { mutableStateOf(false) }

    fun runWithBiometricCheck(action: () -> Unit) {
        if (isProtected && !isUnlocked) {
            val activity = context as? androidx.fragment.app.FragmentActivity
            if (activity != null) {
                BiometricSecurityManager.promptAuthentication(
                    activity = activity,
                    title = "Unlock Student Records 🔐",
                    subtitle = "Scan fingerprint, face recognition, or PIN to view student contacts & actions.",
                    description = "Confidential student profiles and contact information are protected.",
                    onSuccess = {
                        viewModel.setStudentAndPaymentBiometricUnlocked(true)
                        Toast.makeText(context, "Student records biometrically unlocked!", Toast.LENGTH_SHORT).show()
                        action()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                action()
            }
        } else {
            action()
        }
    }

    // Count of students with pending dues
    val studentsWithPendingDuesCount = remember(students, batches, feePayments) {
        students.count { student ->
            val batch = batches.find { it.id == student.batchId }
            val expectedFee = batch?.feesAmount ?: 0.0
            val paid = feePayments.filter { it.studentId == student.id }.sumOf { it.amountPaid + it.discount }
            (expectedFee - paid) > 0.0 && expectedFee > 0.0
        }
    }

    // List of grade/class options derived dynamically + defaults
    val gradeOptions = remember(students) {
        val classesFromStudents = students.map { it.studentClass }.filter { it.isNotBlank() }.distinct()
        val defaultGrades = listOf("Class 8th", "Class 9th", "Class 10th", "Class 11th", "Class 12th", "JEE / NEET")
        (listOf("All Grades") + (classesFromStudents + defaultGrades).distinct()).sorted()
    }

    val statusOptions = listOf("All Status", "Active", "On-Leave", "Inactive", "Graduated")

    // Filter students
    val filteredStudents = remember(students, searchQuery, selectedGradeFilter, selectedStatusFilter, selectedPendingDuesFilter, selectedBatchFilter, batches, feePayments) {
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

            // Pending Dues match
            val batch = batches.find { it.id == student.batchId }
            val expectedFee = batch?.feesAmount ?: 0.0
            val paid = feePayments.filter { it.studentId == student.id }.sumOf { it.amountPaid + it.discount }
            val hasDues = (expectedFee - paid) > 0.0 && expectedFee > 0.0

            val matchesDues = when (selectedPendingDuesFilter) {
                "HAS_DUES" -> hasDues
                "PAID_CLEAR" -> !hasDues
                else -> true
            }

            matchesSearch && matchesGrade && matchesStatus && matchesBatch && matchesDues
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

            // Biometric Protection Status Banner
            BiometricSecurityHeaderBanner(
                isProtected = isProtected,
                isUnlocked = isUnlocked,
                itemLabel = "Student Directory & Contact Info",
                onUnlockRequest = { runWithBiometricCheck { } },
                onLockRequest = {
                    viewModel.setStudentAndPaymentBiometricUnlocked(false)
                    Toast.makeText(context, "Student records re-locked 🔒", Toast.LENGTH_SHORT).show()
                }
            )

            // Floating Search Bar & Category Filter Chips Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search student name, roll, phone, parent...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    // Category Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Grade Category Chip
                        item {
                            val isGradeActive = selectedGradeFilter != "All Grades"
                            FilterChip(
                                selected = activeCategoryFilter == "GRADE" || isGradeActive,
                                onClick = {
                                    activeCategoryFilter = if (activeCategoryFilter == "GRADE") "NONE" else "GRADE"
                                },
                                label = {
                                    Text(
                                        text = if (isGradeActive) "Grade: $selectedGradeFilter" else "Grade",
                                        fontSize = 12.sp,
                                        fontWeight = if (isGradeActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    Icon(
                                        if (activeCategoryFilter == "GRADE") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        // Status Category Chip
                        item {
                            val isStatusActive = selectedStatusFilter != "All Status"
                            FilterChip(
                                selected = activeCategoryFilter == "STATUS" || isStatusActive,
                                onClick = {
                                    activeCategoryFilter = if (activeCategoryFilter == "STATUS") "NONE" else "STATUS"
                                },
                                label = {
                                    Text(
                                        text = if (isStatusActive) "Status: $selectedStatusFilter" else "Status",
                                        fontSize = 12.sp,
                                        fontWeight = if (isStatusActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    Icon(
                                        if (activeCategoryFilter == "STATUS") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        // Pending Dues Category Chip
                        item {
                            val isDuesActive = selectedPendingDuesFilter != "ALL"
                            FilterChip(
                                selected = activeCategoryFilter == "DUES" || isDuesActive,
                                onClick = {
                                    activeCategoryFilter = if (activeCategoryFilter == "DUES") "NONE" else "DUES"
                                },
                                label = {
                                    Text(
                                        text = when (selectedPendingDuesFilter) {
                                            "HAS_DUES" -> "Pending Dues ($studentsWithPendingDuesCount)"
                                            "PAID_CLEAR" -> "Dues: Fully Paid"
                                            else -> "Pending Dues ($studentsWithPendingDuesCount)"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (isDuesActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (isDuesActive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        if (activeCategoryFilter == "DUES") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEBEE),
                                    selectedLabelColor = Color(0xFFC62828)
                                )
                            )
                        }

                        // Batch Category Chip
                        item {
                            val selectedBatch = batches.find { it.id == selectedBatchFilter }
                            val isBatchActive = selectedBatchFilter != null
                            FilterChip(
                                selected = activeCategoryFilter == "BATCH" || isBatchActive,
                                onClick = {
                                    activeCategoryFilter = if (activeCategoryFilter == "BATCH") "NONE" else "BATCH"
                                },
                                label = {
                                    Text(
                                        text = if (isBatchActive) "Batch: ${selectedBatch?.name ?: ""}" else "Batch",
                                        fontSize = 12.sp,
                                        fontWeight = if (isBatchActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    Icon(
                                        if (activeCategoryFilter == "BATCH") Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }

                        // Quick Reset All Button
                        if (searchQuery.isNotEmpty() || selectedGradeFilter != "All Grades" || selectedStatusFilter != "All Status" || selectedPendingDuesFilter != "ALL" || selectedBatchFilter != null) {
                            item {
                                AssistChip(
                                    onClick = {
                                        searchQuery = ""
                                        selectedGradeFilter = "All Grades"
                                        selectedStatusFilter = "All Status"
                                        selectedPendingDuesFilter = "ALL"
                                        selectedBatchFilter = null
                                        activeCategoryFilter = "NONE"
                                    },
                                    label = { Text("Reset All", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }

                    // Dynamic Sub-Category Selection Options
                    AnimatedVisibility(visible = activeCategoryFilter != "NONE") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                when (activeCategoryFilter) {
                                    "GRADE" -> {
                                        Text("SELECT GRADE / CLASS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(gradeOptions) { grade ->
                                                val isSel = selectedGradeFilter == grade
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedGradeFilter = grade },
                                                    label = { Text(grade, fontSize = 11.sp) },
                                                    leadingIcon = if (isSel) {
                                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                    } else null
                                                )
                                            }
                                        }
                                    }
                                    "STATUS" -> {
                                        Text("SELECT ENROLLMENT STATUS:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(statusOptions) { status ->
                                                val isSel = selectedStatusFilter == status
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedStatusFilter = status },
                                                    label = { Text(status, fontSize = 11.sp) },
                                                    leadingIcon = if (isSel) {
                                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                    } else null
                                                )
                                            }
                                        }
                                    }
                                    "DUES" -> {
                                        Text("FILTER BY PENDING TUITION DUES:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilterChip(
                                                selected = selectedPendingDuesFilter == "ALL",
                                                onClick = { selectedPendingDuesFilter = "ALL" },
                                                label = { Text("All Dues Status", fontSize = 11.sp) }
                                            )
                                            FilterChip(
                                                selected = selectedPendingDuesFilter == "HAS_DUES",
                                                onClick = { selectedPendingDuesFilter = "HAS_DUES" },
                                                label = { Text("🔥 Has Pending Dues ($studentsWithPendingDuesCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFFFEBEE),
                                                    selectedLabelColor = Color(0xFFC62828)
                                                )
                                            )
                                            FilterChip(
                                                selected = selectedPendingDuesFilter == "PAID_CLEAR",
                                                onClick = { selectedPendingDuesFilter = "PAID_CLEAR" },
                                                label = { Text("✅ Dues Fully Paid", fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                    "BATCH" -> {
                                        Text("SELECT STUDENT BATCH:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            item {
                                                FilterChip(
                                                    selected = selectedBatchFilter == null,
                                                    onClick = { selectedBatchFilter = null },
                                                    label = { Text("All Batches", fontSize = 11.sp) }
                                                )
                                            }
                                            items(batches) { b ->
                                                val isSel = selectedBatchFilter == b.id
                                                FilterChip(
                                                    selected = isSel,
                                                    onClick = { selectedBatchFilter = b.id },
                                                    label = { Text(b.name, fontSize = 11.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PeopleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No students match the criteria",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your search query or adjusting your filters.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isNotBlank() || selectedGradeFilter != "All Grades" || selectedStatusFilter != "All Status" || selectedPendingDuesFilter != "ALL" || selectedBatchFilter != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedGradeFilter = "All Grades"
                                    selectedStatusFilter = "All Status"
                                    selectedPendingDuesFilter = "ALL"
                                    selectedBatchFilter = null
                                    activeCategoryFilter = "NONE"
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset All Filters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val studentBatch = remember(batches, student.batchId) { batches.find { it.id == student.batchId } }
                        val isLocked = isProtected && !isUnlocked
                        StudentDirectoryCard(
                            student = student,
                            batch = studentBatch,
                            isLocked = isLocked,
                            onUnlockPrompt = { runWithBiometricCheck { } },
                            onStatusClick = {
                                runWithBiometricCheck {
                                    studentToEditStatus = student
                                }
                            },
                            onCall = { phone ->
                                runWithBiometricCheck {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not launch dialer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onWhatsApp = { phone ->
                                runWithBiometricCheck {
                                    val raw = phone.replace(Regex("[^0-9]"), "")
                                    if (raw.isBlank()) {
                                        Toast.makeText(context, "No contact number registered", Toast.LENGTH_SHORT).show()
                                        return@runWithBiometricCheck
                                    }
                                    val cleanPhone = when {
                                        raw.startsWith("91") && raw.length == 12 -> raw
                                        raw.startsWith("0") && raw.length == 11 -> "91" + raw.substring(1)
                                        raw.length == 10 -> "91$raw"
                                        else -> raw
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val chooser = Intent.createChooser(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone")),
                                                "Open WhatsApp"
                                            )
                                            context.startActivity(chooser)
                                        } catch (e2: Exception) {
                                            Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
                                        }
                                    }
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
    isLocked: Boolean = false,
    onUnlockPrompt: () -> Unit = {},
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

                if (student.parentName.isNotBlank() || student.phone.isNotBlank() || student.parentPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    val rawPhone = student.parentPhone.ifEmpty { student.phone }
                    val displayPhone = if (isLocked) {
                        if (rawPhone.length > 4) rawPhone.take(2) + "••••••" + rawPhone.takeLast(2) + " 🔒"
                        else "•••••••••• 🔒"
                    } else rawPhone
                    val displayParent = if (isLocked && student.parentName.isNotBlank()) {
                        student.parentName.take(3) + "•••"
                    } else student.parentName

                    Text(
                        text = if (displayParent.isNotBlank()) "Parent: $displayParent ($displayPhone)" else "Contact: $displayPhone",
                        fontSize = 11.sp,
                        color = if (isLocked) Color.Gray else Color.DarkGray,
                        modifier = if (isLocked) Modifier.clickable { onUnlockPrompt() } else Modifier
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
                        onClick = {
                            if (isLocked) onUnlockPrompt()
                            else onCall(student.phone)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Phone,
                            contentDescription = if (isLocked) "Biometric Lock" else "Call",
                            tint = if (isLocked) Color.Gray else Color(0xFF1E88E5),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (student.parentPhone.isNotBlank() || student.phone.isNotBlank()) {
                    IconButton(
                        onClick = {
                            if (isLocked) onUnlockPrompt()
                            else onWhatsApp(student.parentPhone.ifEmpty { student.phone })
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Fingerprint else Icons.Default.Chat,
                            contentDescription = if (isLocked) "Biometric Lock" else "WhatsApp",
                            tint = if (isLocked) Color.Gray else Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
