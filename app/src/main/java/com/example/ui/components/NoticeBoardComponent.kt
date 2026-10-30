package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Batch
import com.example.data.model.Notice
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PinnedNoticeBanner displays high-priority pinned notices (Urgent Updates & Exam Schedules)
 * directly at the top of the Student Dashboard.
 */
@Composable
fun PinnedNoticeBanner(
    notices: List<Notice>,
    studentBatchId: Long,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relevantPinned = remember(notices, studentBatchId) {
        notices.filter { it.isPinned && (it.targetBatchId == 0L || it.targetBatchId == studentBatchId) }
    }

    if (relevantPinned.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pinned_notice_board_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED) // Warm alert tone
        ),
        border = BorderStroke(1.5.dp, Color(0xFFF97316))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Alert",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "PINNED NOTICE BOARD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFC2410C),
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = Color(0xFFDC2626),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${relevantPinned.size} ACTIVE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Urgent teacher updates & examination schedules",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }

                TextButton(
                    onClick = onViewAllClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("View All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFC2410C))
                }
            }

            // List of pinned items
            relevantPinned.take(3).forEach { notice ->
                NoticePinnedItemCard(notice = notice)
            }
        }
    }
}

@Composable
fun NoticePinnedItemCard(notice: Notice) {
    var isExpanded by remember { mutableStateOf(false) }

    val (badgeBg, badgeText, badgeColor) = when (notice.category) {
        "EXAM_SCHEDULE" -> Triple(Color(0xFFEDE9FE), "📅 EXAM SCHEDULE", Color(0xFF6D28D9))
        "HOLIDAY" -> Triple(Color(0xFFECFDF5), "🏖️ HOLIDAY", Color(0xFF047857))
        "GENERAL" -> Triple(Color(0xFFEFF6FF), "📢 ANNOUNCEMENT", Color(0xFF1D4ED8))
        else -> Triple(Color(0xFFFEE2E2), "🚨 URGENT UPDATE", Color(0xFFDC2626))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("pinned_notice_item_${notice.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = formatDate(notice.createdAt),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                }
            }

            Text(
                text = notice.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            // Exam Schedule details banner if available
            if (notice.category == "EXAM_SCHEDULE" && (notice.examDate.isNotBlank() || notice.examTime.isNotBlank())) {
                Surface(
                    color = Color(0xFFF5F3FF),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
                        Text(
                            text = listOfNotNull(
                                if (notice.examDate.isNotBlank()) "Date: ${notice.examDate}" else null,
                                if (notice.examTime.isNotBlank()) "Time: ${notice.examTime}" else null
                            ).joinToString(" • "),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF5B21B6)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                    Text(
                        text = notice.content,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 17.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Posted by: ${notice.authorName}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (notice.targetBatchId == 0L) "Audience: All Batches" else "Audience: Your Batch",
                            fontSize = 10.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!isExpanded) {
                Text(
                    text = notice.content,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * StudentNoticeBoardScreen: Dedicated full tab view for students to browse all notices
 */
@Composable
fun StudentNoticeBoardScreen(
    notices: List<Notice>,
    studentBatchId: Long,
    batches: List<Batch>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val studentNotices = remember(notices, studentBatchId) {
        notices.filter { it.targetBatchId == 0L || it.targetBatchId == studentBatchId }
    }

    val filteredNotices = remember(studentNotices, searchQuery, selectedCategoryFilter) {
        studentNotices.filter { notice ->
            val matchesCategory = when (selectedCategoryFilter) {
                "ALL" -> true
                else -> notice.category == selectedCategoryFilter
            }
            val matchesSearch = searchQuery.isBlank() ||
                    notice.title.contains(searchQuery, ignoreCase = true) ||
                    notice.content.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_notice_board_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter Header
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Search notices, exam dates, schedules...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true
        )

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "All Notices",
                "URGENT" to "🚨 Urgent",
                "EXAM_SCHEDULE" to "📅 Exam Schedules",
                "GENERAL" to "📢 General",
                "HOLIDAY" to "🏖️ Holidays"
            )
            items(filters) { (key, label) ->
                val isSelected = selectedCategoryFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        if (filteredNotices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.NotificationsNone,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text("No notices in this category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "No announcements or exam schedules match the active criteria.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isNotBlank() || selectedCategoryFilter != "ALL") {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryFilter = "ALL"
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Filter", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotices, key = { it.id }) { notice ->
                    NoticePinnedItemCard(notice = notice)
                }
            }
        }
    }
}

/**
 * TeacherNoticeBoardManagementDialog: Allows teachers / faculty / admins
 * to publish new notices and pin them to the student dashboard, or manage existing ones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherNoticeBoardManagementDialog(
    viewModel: AppViewModel,
    batches: List<Batch>,
    onDismiss: () -> Unit
) {
    val notices by viewModel.notices.collectAsState()
    var activeTab by remember { mutableStateOf("CREATE") } // "CREATE" or "MANAGE"

    // Form inputs
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("Faculty / Teacher") }
    var selectedCategory by remember { mutableStateOf("URGENT") } // URGENT, EXAM_SCHEDULE, GENERAL, HOLIDAY
    var selectedBatchId by remember { mutableStateOf(0L) } // 0 = All Batches
    var isPinned by remember { mutableStateOf(true) }
    var examDate by remember { mutableStateOf("") }
    var examTime by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA580C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Teacher Notice Board Station", fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text("Post urgent alerts & exam schedules to student dashboards", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "CREATE" to "✍️ Post New Notice",
                        "MANAGE" to "📋 Manage Existing (${notices.size})"
                    ).forEach { (tabKey, tabLabel) ->
                        val isSelected = activeTab == tabKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { activeTab = tabKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (activeTab == "CREATE") {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("1. Notice Category & Urgency:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val categories = listOf(
                                    "URGENT" to "🚨 Urgent Update",
                                    "EXAM_SCHEDULE" to "📅 Exam Schedule",
                                    "GENERAL" to "📢 General",
                                    "HOLIDAY" to "🏖️ Holiday Alert"
                                )
                                items(categories) { (catKey, catLabel) ->
                                    val isCatSelected = selectedCategory == catKey
                                    FilterChip(
                                        selected = isCatSelected,
                                        onClick = { selectedCategory = catKey },
                                        label = { Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Notice Title *") },
                                placeholder = { Text("e.g. Physics Mid-Term Exam Schedule or Class Rescheduled") },
                                modifier = Modifier.fillMaxWidth().testTag("notice_title_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        if (selectedCategory == "EXAM_SCHEDULE") {
                            item {
                                Surface(
                                    color = Color(0xFFF5F3FF),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("📅 Exam Schedule Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = examDate,
                                                onValueChange = { examDate = it },
                                                label = { Text("Exam Date") },
                                                placeholder = { Text("e.g. 15 Nov 2026") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = examTime,
                                                onValueChange = { examTime = it },
                                                label = { Text("Timings") },
                                                placeholder = { Text("10:00 AM - 1:00 PM") },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("2. Target Audience & Batch:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedBatchId == 0L,
                                        onClick = { selectedBatchId = 0L },
                                        label = { Text("🌐 All Batches & Students", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }
                                items(batches) { batch ->
                                    FilterChip(
                                        selected = selectedBatchId == batch.id,
                                        onClick = { selectedBatchId = batch.id },
                                        label = { Text("👥 ${batch.name}", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = content,
                                onValueChange = { content = it },
                                label = { Text("Detailed Notice Description / Timetable Instructions *") },
                                placeholder = { Text("Enter the full instructions, reporting time, stationery required, syllabus, or holiday announcement details...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("notice_content_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("📌 Pin as Top Alert on Student Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                    Text("Shows in the high-visibility pinned banner on every student's home screen.", fontSize = 10.sp, color = Color(0xFF78350F))
                                }
                                Switch(
                                    checked = isPinned,
                                    onCheckedChange = { isPinned = it }
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = authorName,
                                onValueChange = { authorName = it },
                                label = { Text("Author / Teacher Signature") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                return@Button
                            }
                            viewModel.postNotice(
                                title = title,
                                content = content,
                                author = authorName,
                                category = selectedCategory,
                                targetBatchId = selectedBatchId,
                                isPinned = isPinned,
                                examDate = examDate,
                                examTime = examTime
                            )
                            title = ""
                            content = ""
                            examDate = ""
                            examTime = ""
                            activeTab = "MANAGE"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("publish_notice_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish Notice to Student Dashboard 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    // MANAGE TAB
                    if (notices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No notices posted yet. Use the 'Post New Notice' tab to create one.", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(notices, key = { it.id }) { notice ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, if (notice.isPinned) Color(0xFFF97316) else Color.LightGray)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = if (notice.isPinned) Color(0xFFDC2626) else Color.Gray,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        if (notice.isPinned) "📌 PINNED" else "UNPINNED",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(notice.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { viewModel.toggleNoticePinned(notice.id, !notice.isPinned) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (notice.isPinned) Icons.Default.PushPin else Icons.Default.VerticalAlignTop,
                                                        contentDescription = "Pin/Unpin",
                                                        tint = if (notice.isPinned) Color(0xFFEA580C) else Color.Gray,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteNotice(notice.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete",
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(notice.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(notice.content, fontSize = 11.sp, color = Color.DarkGray, maxLines = 3, overflow = TextOverflow.Ellipsis)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (notice.targetBatchId == 0L) "Target: All Batches" else "Target: Batch #${notice.targetBatchId}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF2563EB)
                                            )
                                            Text(
                                                text = formatDate(notice.createdAt),
                                                fontSize = 10.sp,
                                                color = Color.Gray
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
    }
}

private fun formatDate(millis: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "Recent"
    }
}
