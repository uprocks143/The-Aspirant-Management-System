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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Batch
import com.example.data.model.StudyMaterial
import com.example.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI Grid component to organize, filter, search, and display digital study materials and PDF resources.
 * Supports category chips, format filters, instant search, adaptive 2-column grid or list view,
 * PDF in-app document reader, and resource addition dialogs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialsGridComponent(
    studyMaterials: List<StudyMaterial>,
    batches: List<Batch>,
    viewModel: AppViewModel,
    userRole: String = "STUDENT",
    currentBatchId: Long = 0L,
    onQuizClick: ((StudyMaterial) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Categories") }
    var selectedFormat by remember { mutableStateOf("All Formats") }
    var isGridView by remember { mutableStateOf(true) }
    var showOnlyBookmarks by remember { mutableStateOf(false) }

    val favoritedIds by viewModel.favoritedMaterialIds.collectAsState()

    // Dialog States
    var selectedMaterialForReader by remember { mutableStateOf<StudyMaterial?>(null) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<StudyMaterial?>(null) }

    // Categories derived dynamically from study materials
    val categories = remember(studyMaterials) {
        val unique = studyMaterials.map { it.mainCategory.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val standard = listOf("All Categories", "Science", "Mathematics", "Physics", "Chemistry", "Biology", "English", "Social Science", "Formula Sheets", "PYQ Papers")
        (standard + unique).distinct()
    }

    val formats = listOf("All Formats", "PDF Document", "Plain Text", "YouTube Video", "Web Link")

    // Filter study materials
    val filteredMaterials = remember(
        studyMaterials,
        searchQuery,
        selectedCategory,
        selectedFormat,
        showOnlyBookmarks,
        favoritedIds,
        currentBatchId
    ) {
        studyMaterials.filter { material ->
            val matchesBatch = currentBatchId == 0L || material.batchId == 0L || material.batchId == currentBatchId

            val matchesSearch = searchQuery.isBlank() ||
                    material.title.contains(searchQuery, ignoreCase = true) ||
                    material.mainCategory.contains(searchQuery, ignoreCase = true) ||
                    material.topicSubCategory.contains(searchQuery, ignoreCase = true) ||
                    material.content.contains(searchQuery, ignoreCase = true) ||
                    material.subject.contains(searchQuery, ignoreCase = true) ||
                    material.grade.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategory == "All Categories" ||
                    material.mainCategory.equals(selectedCategory, ignoreCase = true) ||
                    material.subject.equals(selectedCategory, ignoreCase = true) ||
                    material.topicSubCategory.contains(selectedCategory, ignoreCase = true)

            val matchesFormat = when (selectedFormat) {
                "PDF Document" -> material.contentType.equals("PDF", ignoreCase = true) || material.pdfName != null
                "Plain Text" -> material.contentType.equals("Plain Text", ignoreCase = true)
                "YouTube Video" -> material.contentType.contains("YouTube", ignoreCase = true) || material.youtubeUrl != null
                "Web Link" -> material.contentType.equals("URL", ignoreCase = true)
                else -> true
            }

            val matchesBookmark = !showOnlyBookmarks || favoritedIds.contains(material.id.toString())

            matchesBatch && matchesSearch && matchesCategory && matchesFormat && matchesBookmark
        }
    }

    val pdfCount = remember(studyMaterials) {
        studyMaterials.count { it.contentType.equals("PDF", ignoreCase = true) || it.pdfName != null }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header & Statistics Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Digital Library & PDF Center",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${filteredMaterials.size} resources available • $pdfCount PDFs",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Toggle Grid / List View
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(36.dp).testTag("toggle_study_grid_view")
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle View",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Add Resource Button for Teachers/Admins
                        if (userRole != "STUDENT") {
                            FilledTonalButton(
                                onClick = { showAddMaterialDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("add_study_resource_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_material_search_input"),
            placeholder = { Text("Search digital resources, PDFs, notes, subjects...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bookmarks Quick Filter
            FilterChip(
                selected = showOnlyBookmarks,
                onClick = { showOnlyBookmarks = !showOnlyBookmarks },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (showOnlyBookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (showOnlyBookmarks) Color(0xFFFFB300) else Color.Gray
                        )
                        Text("Saved (${favoritedIds.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("filter_chip_bookmarks")
            )

            // Category Chips
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_chip_category_$cat")
                )
            }
        }

        // Format Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Format:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 2.dp)
            )

            formats.forEach { fmt ->
                val isSelected = selectedFormat == fmt
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFormat = fmt },
                    label = {
                        val icon = when (fmt) {
                            "PDF Document" -> Icons.Default.PictureAsPdf
                            "Plain Text" -> Icons.Default.Description
                            "YouTube Video" -> Icons.Default.SmartDisplay
                            "Web Link" -> Icons.Default.Link
                            else -> Icons.Default.FolderOpen
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
                            Text(fmt, fontSize = 10.sp)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_chip_format_$fmt")
                )
            }
        }

        // Content Area: Empty State or Grid/List
        if (filteredMaterials.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != "All Categories" || showOnlyBookmarks)
                            "No digital study resources match your filters"
                        else
                            "No digital study materials uploaded yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    if (searchQuery.isNotBlank() || selectedCategory != "All Categories" || showOnlyBookmarks) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                selectedCategory = "All Categories"
                                selectedFormat = "All Formats"
                                showOnlyBookmarks = false
                            }
                        ) {
                            Text("Reset Filters", fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            if (isGridView) {
                // Adaptive 2-column Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 1400.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMaterials, key = { it.id }) { material ->
                        val isBookmarked = favoritedIds.contains(material.id.toString())
                        StudyMaterialGridCard(
                            material = material,
                            isBookmarked = isBookmarked,
                            userRole = userRole,
                            onReadClick = { selectedMaterialForReader = material },
                            onBookmarkToggle = { viewModel.toggleFavoriteMaterial(material.id) },
                            onQuizClick = { onQuizClick?.invoke(material) },
                            onDeleteClick = { materialToDelete = material }
                        )
                    }
                }
            } else {
                // Compact List View
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredMaterials.forEach { material ->
                        val isBookmarked = favoritedIds.contains(material.id.toString())
                        StudyMaterialListRow(
                            material = material,
                            isBookmarked = isBookmarked,
                            userRole = userRole,
                            onReadClick = { selectedMaterialForReader = material },
                            onBookmarkToggle = { viewModel.toggleFavoriteMaterial(material.id) },
                            onQuizClick = { onQuizClick?.invoke(material) },
                            onDeleteClick = { materialToDelete = material }
                        )
                    }
                }
            }
        }
    }

    // Modal: PDF & Document Reader Dialog
    selectedMaterialForReader?.let { material ->
        StudyMaterialDocumentReaderDialog(
            material = material,
            onDismiss = { selectedMaterialForReader = null },
            viewModel = viewModel
        )
    }

    // Modal: Add Digital Material / Upload PDF
    if (showAddMaterialDialog) {
        AddStudyMaterialDialog(
            batches = batches,
            onDismiss = { showAddMaterialDialog = false },
            onAdd = { batchId, category, subCategory, title, type, content, pdfName, youtubeUrl ->
                viewModel.uploadStudyMaterial(
                    batchId = batchId,
                    category = category,
                    subCategory = subCategory,
                    title = title,
                    type = type,
                    content = content,
                    pdf = pdfName,
                    tube = youtubeUrl
                )
                showAddMaterialDialog = false
                Toast.makeText(context, "Digital resource added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Confirm Delete Material
    materialToDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Resource") },
            text = { Text("Are you sure you want to delete '${material.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudyMaterial(material)
                        materialToDelete = null
                        Toast.makeText(context, "Study material removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Grid Card for a single Study Material / PDF Resource
 */
@Composable
fun StudyMaterialGridCard(
    material: StudyMaterial,
    isBookmarked: Boolean,
    userRole: String,
    onReadClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onQuizClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPdf = material.contentType.equals("PDF", ignoreCase = true) || material.pdfName != null
    val isVideo = material.contentType.contains("YouTube", ignoreCase = true) || material.youtubeUrl != null
    val isLink = material.contentType.equals("URL", ignoreCase = true)

    val headerGradient = when {
        isPdf -> Brush.verticalGradient(listOf(Color(0xFFE53935), Color(0xFFC62828)))
        isVideo -> Brush.verticalGradient(listOf(Color(0xFF8E24AA), Color(0xFF6A1B9A)))
        isLink -> Brush.verticalGradient(listOf(Color(0xFF00897B), Color(0xFF00695C)))
        else -> Brush.verticalGradient(listOf(Color(0xFF1E88E5), Color(0xFF1565C0)))
    }

    val typeIcon = when {
        isPdf -> Icons.Default.PictureAsPdf
        isVideo -> Icons.Default.SmartDisplay
        isLink -> Icons.Default.Link
        else -> Icons.Default.Description
    }

    val formatLabel = when {
        isPdf -> "PDF"
        isVideo -> "VIDEO"
        isLink -> "LINK"
        else -> "NOTES"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_material_card_${material.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Card Header with Subject / Format Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(headerGradient)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Format Tag
                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(typeIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            Text(formatLabel, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Bookmark Action Button
                    IconButton(
                        onClick = onBookmarkToggle,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Category & Subject label at bottom of header
                Text(
                    text = material.mainCategory.ifBlank { material.subject }.uppercase(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }

            // Card Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    text = material.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Topic / Subcategory
                if (material.topicSubCategory.isNotBlank()) {
                    Text(
                        text = "• ${material.topicSubCategory}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Brief snippet
                val snippet = material.content.trim().replace("\n", " ")
                if (snippet.isNotBlank()) {
                    Text(
                        text = snippet,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (isVideo && material.youtubeUrl != null) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.youtubeUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open video link", Toast.LENGTH_SHORT).show()
                                }
                            } else if (isLink && material.content.startsWith("http")) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(material.content))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                onReadClick()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("read_material_btn_${material.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPdf) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPdf) Icons.Default.MenuBook else if (isVideo) Icons.Default.PlayArrow else Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPdf) "Read PDF" else if (isVideo) "Watch" else "Open",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Admin Delete
                    if (userRole != "STUDENT") {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * List row for Study Material
 */
@Composable
fun StudyMaterialListRow(
    material: StudyMaterial,
    isBookmarked: Boolean,
    userRole: String,
    onReadClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onQuizClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPdf = material.contentType.equals("PDF", ignoreCase = true) || material.pdfName != null
    val isVideo = material.contentType.contains("YouTube", ignoreCase = true) || material.youtubeUrl != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_material_row_${material.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Format icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isPdf) Color(0xFFFFEBEE) else if (isVideo) Color(0xFFF3E5F5) else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else if (isVideo) Icons.Default.SmartDisplay else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (isPdf) Color(0xFFC62828) else if (isVideo) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = material.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${material.mainCategory} • ${material.topicSubCategory.ifBlank { "Study Guide" }}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bookmark button
            IconButton(onClick = onBookmarkToggle, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Open Action
            Button(
                onClick = onReadClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(if (isPdf) "Read" else "Open", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (userRole != "STUDENT") {
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Built-in PDF & Digital Resource Document Reader Modal Dialog
 */
@Composable
fun StudyMaterialDocumentReaderDialog(
    material: StudyMaterial,
    onDismiss: () -> Unit,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var fontSize by remember { mutableStateOf(14.sp) }
    var isNightMode by remember { mutableStateOf(false) }
    val favoritedIds by viewModel.favoritedMaterialIds.collectAsState()
    val isBookmarked = favoritedIds.contains(material.id.toString())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            color = if (isNightMode) Color(0xFF121212) else Color(0xFFF9F9FB)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = if (isNightMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close",
                                tint = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(
                                text = material.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isNightMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${material.mainCategory} • ${material.contentType}",
                                fontSize = 10.sp,
                                color = if (isNightMode) Color.LightGray else Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Font Zoom Out
                            IconButton(
                                onClick = { if (fontSize.value > 10f) fontSize = (fontSize.value - 2f).sp },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("A-", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isNightMode) Color.White else Color.Black)
                            }

                            // Font Zoom In
                            IconButton(
                                onClick = { if (fontSize.value < 26f) fontSize = (fontSize.value + 2f).sp },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("A+", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isNightMode) Color.White else Color.Black)
                            }

                            // Night Mode Toggle
                            IconButton(
                                onClick = { isNightMode = !isNightMode },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Theme",
                                    tint = if (isNightMode) Color(0xFFFFD54F) else Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Bookmark Toggle
                            IconButton(
                                onClick = { viewModel.toggleFavoriteMaterial(material.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isBookmarked) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Document Content Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Title Header Banner in Document
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNightMode) Color(0xFF263238) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = material.title,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = if (isNightMode) Color.White else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Subject: ${material.mainCategory} | Topic: ${material.topicSubCategory} | Grade: ${material.grade}",
                                    fontSize = 11.sp,
                                    color = if (isNightMode) Color.LightGray else Color.DarkGray
                                )
                                if (material.pdfName != null) {
                                    Text(
                                        text = "📄 PDF Source: ${material.pdfName}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isNightMode) Color(0xFF81D4FA) else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Actual Document Content
                        val displayText = material.content.ifBlank {
                            "This digital resource contains official syllabus references for ${material.title}.\n\nKey Concepts & Curriculum Outline:\n1. Introduction and Fundamental Principles\n2. Analytical Theory & Formulas\n3. Worked Examples & Practice Exercises\n4. Previous Year Board & Competitive Examination Patterns\n\nStudy carefully and use the TAMS Practice Quiz module to verify your mastery."
                        }

                        Text(
                            text = displayText,
                            fontSize = fontSize,
                            lineHeight = (fontSize.value * 1.55f).sp,
                            color = if (isNightMode) Color(0xFFECEFF1) else Color(0xFF212121),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Share / Export Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, material.title)
                                            putExtra(Intent.EXTRA_TEXT, "${material.title}\n\n${material.content}\n\nShared via TAMS Digital Academy")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Study Material"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Sharing unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Resource", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add / upload a new digital study material or PDF
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyMaterialDialog(
    batches: List<Batch>,
    onDismiss: () -> Unit,
    onAdd: (batchId: Long, category: String, subCategory: String, title: String, type: String, content: String, pdfName: String?, youtubeUrl: String?) -> Unit
) {
    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 0L) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Science") }
    var subCategory by remember { mutableStateOf("") }
    var contentType by remember { mutableStateOf("PDF") }
    var contentText by remember { mutableStateOf("") }
    var pdfName by remember { mutableStateOf("") }
    var youtubeUrl by remember { mutableStateOf("") }

    val categories = listOf("Science", "Mathematics", "Physics", "Chemistry", "Biology", "English", "Social Science", "Formula Sheets", "PYQ Papers", "General Knowledge")
    val contentTypes = listOf("PDF", "Plain Text", "YouTube Video", "URL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Add Digital Study Material", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Selector
                Text("Target Batch Track:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedBatchId == 0L,
                        onClick = { selectedBatchId = 0L },
                        label = { Text("All Batches", fontSize = 11.sp) }
                    )
                    batches.forEach { b ->
                        FilterChip(
                            selected = selectedBatchId == b.id,
                            onClick = { selectedBatchId = b.id },
                            label = { Text(b.name, fontSize = 11.sp) }
                        )
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Resource Title *") },
                    placeholder = { Text("e.g. Chapter 4 Thermodynamics Complete Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Selector
                Text("Subject / Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                // Topic / Subcategory
                OutlinedTextField(
                    value = subCategory,
                    onValueChange = { subCategory = it },
                    label = { Text("Topic / Chapter Tag") },
                    placeholder = { Text("e.g. NCERT Solutions, Formula Sheet") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Content Type Selector
                Text("Resource Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    contentTypes.forEach { ct ->
                        FilterChip(
                            selected = contentType == ct,
                            onClick = { contentType = ct },
                            label = { Text(ct, fontSize = 10.sp) }
                        )
                    }
                }

                if (contentType == "PDF") {
                    OutlinedTextField(
                        value = pdfName,
                        onValueChange = { pdfName = it },
                        label = { Text("PDF File Name / Document Reference") },
                        placeholder = { Text("e.g. thermodynamics_handwritten_guide.pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                if (contentType == "YouTube Video") {
                    OutlinedTextField(
                        value = youtubeUrl,
                        onValueChange = { youtubeUrl = it },
                        label = { Text("YouTube Lecture URL") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Detailed Content / Notes
                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    label = { Text(if (contentType == "PDF") "Chapter Notes / PDF Summary Text" else "Study Material Text Content *") },
                    placeholder = { Text("Enter detailed notes, equations, theory summary...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            selectedBatchId,
                            category,
                            subCategory.ifBlank { "Overview" },
                            title,
                            contentType,
                            contentText.ifBlank { "Official notes for $title" },
                            if (contentType == "PDF") pdfName.ifBlank { "${title.lowercase().replace(" ", "_")}.pdf" } else null,
                            if (contentType == "YouTube Video") youtubeUrl.ifBlank { null } else null
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Resource")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
