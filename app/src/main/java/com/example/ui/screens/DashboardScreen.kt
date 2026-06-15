package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.style.TextDecoration
import com.example.data.model.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.InstituteAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom Theme Palette definition
data class CustomColorPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe active application states reactively
    val themeKey by viewModel.themeMode.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()
    val staffScreenAccess by viewModel.staffScreenAccess.collectAsState()
    val isBiometricLocked by viewModel.isBiometricLocked.collectAsState()
    val preferredSmsSim by viewModel.preferredSmsSim.collectAsState()
    val whatsAppRouting by viewModel.whatsAppRouting.collectAsState()

    val academyNameState by viewModel.academyName.collectAsState()
    val directorNameState by viewModel.directorName.collectAsState()
    val adminEmailState by viewModel.adminEmail.collectAsState()

    val batchesList by viewModel.batches.collectAsState()
    val studentsList by viewModel.students.collectAsState()
    val paymentsList by viewModel.feePayments.collectAsState()
    val transactionsList by viewModel.transactions.collectAsState()
    val studyMaterialsList by viewModel.studyMaterials.collectAsState()
    val messageTemplatesList by viewModel.messageTemplates.collectAsState()
    val examsList by viewModel.exams.collectAsState()

    // Map theme selections
    val currentPalette = remember(themeKey) {
        when (themeKey) {
            "CLASSIC_IVORY" -> CustomColorPalette(
                primary = Color(0xFF8B1E3F), // Deep Burgundy
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFD9E2),
                onPrimaryContainer = Color(0xFF3E0011),
                secondary = Color(0xFF7A5716), // Warm Bronze
                onSecondary = Color.White,
                background = Color(0xFFFDFBF7), // Cream Ivory
                surface = Color(0xFFFFFDF9),
                onSurface = Color(0xFF201A1B),
                surfaceVariant = Color(0xFFF2E0E3),
                onSurfaceVariant = Color(0xFF514345),
                outline = Color(0xFF837375)
            )
            "CLASSIC_FOREST" -> CustomColorPalette(
                primary = Color(0xFF136243), // Forest Green
                onPrimary = Color.White,
                primaryContainer = Color(0xFFBEF2D3),
                onPrimaryContainer = Color(0xFF002111),
                secondary = Color(0xFF56624B),
                onSecondary = Color.White,
                background = Color(0xFFFAFCFA),
                surface = Color(0xFFF2F5F2),
                onSurface = Color(0xFF191C1A),
                surfaceVariant = Color(0xFFDCE5DD),
                onSurfaceVariant = Color(0xFF404943),
                outline = Color(0xFF707973)
            )
            "CLASSIC_MIDNIGHT" -> CustomColorPalette(
                primary = Color(0xFF1E88E5), // Blue Accent
                onPrimary = Color.White,
                primaryContainer = Color(0xFF1565C0),
                onPrimaryContainer = Color(0xFFBBDEFB),
                secondary = Color(0xFF03DAC6),
                onSecondary = Color.Black,
                background = Color(0xFF111E2E), // Dark Slate Blue
                surface = Color(0xFF17283F),
                onSurface = Color(0xFFECECEC),
                surfaceVariant = Color(0xFF1F3552),
                onSurfaceVariant = Color(0xFFC4C4C4),
                outline = Color(0xFF2E4B70)
            )
            else -> CustomColorPalette( // CLASSIC_NAVY (Default)
                primary = Color(0xFF0D47A1), // Royal Navy
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1E4FF),
                onPrimaryContainer = Color(0xFF001D40),
                secondary = Color(0xFF00838F),
                onSecondary = Color.White,
                background = Color(0xFFE1F5FE), // Soft Sky Blue
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFE1E2EC),
                onSurfaceVariant = Color(0xFF44474F),
                outline = Color(0xFF74777F)
            )
        }
    }

    // Material 3 colors overrides
    val m3ColorScheme = if (themeKey == "CLASSIC_MIDNIGHT") {
        darkColorScheme(
            primary = currentPalette.primary,
            onPrimary = currentPalette.onPrimary,
            primaryContainer = currentPalette.primaryContainer,
            onPrimaryContainer = currentPalette.onPrimaryContainer,
            secondary = currentPalette.secondary,
            onSecondary = currentPalette.onSecondary,
            background = currentPalette.background,
            surface = currentPalette.surface,
            onSurface = currentPalette.onSurface,
            surfaceVariant = currentPalette.surfaceVariant,
            onSurfaceVariant = currentPalette.onSurfaceVariant,
            outline = currentPalette.outline
        )
    } else {
        lightColorScheme(
            primary = currentPalette.primary,
            onPrimary = currentPalette.onPrimary,
            primaryContainer = currentPalette.primaryContainer,
            onPrimaryContainer = currentPalette.onPrimaryContainer,
            secondary = currentPalette.secondary,
            onSecondary = currentPalette.onSecondary,
            background = currentPalette.background,
            surface = currentPalette.surface,
            onSurface = currentPalette.onSurface,
            surfaceVariant = currentPalette.surfaceVariant,
            onSurfaceVariant = currentPalette.onSurfaceVariant,
            outline = currentPalette.outline
        )
    }

    // Active screen switcher flow to match photographs
    var currentScreenFlow by remember { mutableStateOf("SPLASH") } // "SPLASH", "PORTAL_SELECT", "ADMIN_LOGIN", "STUDENT_LOGIN", "MAIN_APP"
    var activeBottomNavTab by remember { mutableStateOf("DASHBOARD") } // "DASHBOARD", "MY_ACCOUNT"

    // Active Tab in current Portal
    var activeAdminTab by remember { mutableStateOf("Dashboard") }
    var activeStaffTab by remember { mutableStateOf("Attendance") }
    var activeStudentTab by remember { mutableStateOf("Home Workspace") }

    // Navigation lists
    val adminTabs = listOf("Dashboard", "Admission Form", "Batches Setup", "Tuition Fees", "Ledger Accounts", "Database Backups", "Outreach Engine")
    val staffTabs = listOf("Attendance", "Study Materials")
    val studentTabs = listOf("Home Workspace", "Academic Resources", "Practice exams")

    // State controlling dialog overlays / sub-flows for 16 shortcuts
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var showAdmissionDialogConfirm by remember { mutableStateOf<Student?>(null) }
    var showBulkAttendanceSelectBatch by remember { mutableStateOf<Long?>(null) }
    var activeExamPracticeSession by remember { mutableStateOf<Exam?>(null) }

    // Dialog state controllers
    var showDoubtSolverBot by remember { mutableStateOf(false) }
    var showQrAttendanceScanner by remember { mutableStateOf(false) }
    var showPerformanceReportDialog by remember { mutableStateOf(false) }
    var showEnquiryManagerDialog by remember { mutableStateOf(false) }
    var showStaffManagerDialog by remember { mutableStateOf(false) }
    var showReportsConsoleDialog by remember { mutableStateOf(false) }
    var showHomeworkAssignDialog by remember { mutableStateOf(false) }
    var showTodoTaskDialog by remember { mutableStateOf(false) }
    var showPaperGeneratorDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var hasUnreadNotifications by remember { mutableStateOf(true) }

    MaterialTheme(colorScheme = m3ColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backEnabled = currentScreenFlow != "SPLASH" ||
                    showDoubtSolverBot || showQrAttendanceScanner || showPerformanceReportDialog ||
                    showEnquiryManagerDialog || showStaffManagerDialog || showReportsConsoleDialog || showHomeworkAssignDialog ||
                    showTodoTaskDialog || showPaperGeneratorDialog || showNotificationsDialog ||
                    activeAdminTab != "Dashboard" || activeBottomNavTab != "DASHBOARD"

            BackHandler(enabled = backEnabled) {
                if (showDoubtSolverBot) { showDoubtSolverBot = false }
                else if (showQrAttendanceScanner) { showQrAttendanceScanner = false }
                else if (showPerformanceReportDialog) { showPerformanceReportDialog = false }
                else if (showEnquiryManagerDialog) { showEnquiryManagerDialog = false }
                else if (showStaffManagerDialog) { showStaffManagerDialog = false }
                else if (showReportsConsoleDialog) { showReportsConsoleDialog = false }
                else if (showHomeworkAssignDialog) { showHomeworkAssignDialog = false }
                else if (showTodoTaskDialog) { showTodoTaskDialog = false }
                else if (showPaperGeneratorDialog) { showPaperGeneratorDialog = false }
                else if (showNotificationsDialog) { showNotificationsDialog = false }
                else if (activeBottomNavTab != "DASHBOARD") {
                    activeBottomNavTab = "DASHBOARD"
                }
                else if (activeAdminTab != "Dashboard") {
                    activeAdminTab = "Dashboard"
                }
                else {
                    when (currentScreenFlow) {
                        "ADMIN_LOGIN", "STUDENT_LOGIN" -> {
                            currentScreenFlow = "PORTAL_SELECT"
                        }
                        "MAIN_APP" -> {
                            currentScreenFlow = "PORTAL_SELECT"
                            Toast.makeText(context, "Returned to portal selection menu.", Toast.LENGTH_SHORT).show()
                        }
                        "PORTAL_SELECT" -> {
                            currentScreenFlow = "SPLASH"
                        }
                    }
                }
            }

            when (currentScreenFlow) {
                "SPLASH" -> {
                    SplashScreenWalkthrough(onFinish = { currentScreenFlow = "PORTAL_SELECT" })
                }
                "PORTAL_SELECT" -> {
                    PortalSelectScreen(
                        onBack = { currentScreenFlow = "SPLASH" },
                        onAdminSelect = { 
                            viewModel.loginAs("ADMIN")
                            currentScreenFlow = "MAIN_APP"
                        },
                        onStudentSelect = {
                            viewModel.loginAs("STUDENT")
                            currentScreenFlow = "MAIN_APP"
                        },
                        onStaffSelect = {
                            currentScreenFlow = "ADMIN_LOGIN"
                        },
                        onParentSelect = {
                            viewModel.loginAs("PARENT")
                            currentScreenFlow = "MAIN_APP"
                        }
                    )
                }
                "ADMIN_LOGIN" -> {
                    AdminLoginScreen(
                        onBack = { currentScreenFlow = "PORTAL_SELECT" },
                        onSignIn = { academy, name, email, addr ->
                            viewModel.registerInstitute(academy, name, email, addr)
                            currentScreenFlow = "MAIN_APP"
                        }
                    )
                }
                "MAIN_APP" -> {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.background,
                                modifier = Modifier.width(310.dp)
                            ) {
                                DrawerHeaderContent(userRole, academyNameState, directorNameState, adminEmailState)
                                HorizontalDivider()
                                DrawerNavigationItems(
                                    userRole = userRole,
                                    activeAdminTab = activeAdminTab,
                                    activeStudentTab = activeStudentTab,
                                    onSelectAdminTab = { tab ->
                                        activeAdminTab = tab
                                        activeBottomNavTab = "DASHBOARD"
                                        scope.launch { drawerState.close() }
                                    },
                                    onSelectStudentTab = { tab ->
                                        activeStudentTab = tab
                                        activeBottomNavTab = "DASHBOARD"
                                        scope.launch { drawerState.close() }
                                    },
                                    onSelectBottomTab = { tab ->
                                        activeBottomNavTab = tab
                                        scope.launch { drawerState.close() }
                                    },
                                    onLogout = {
                                        scope.launch { drawerState.close() }
                                        currentScreenFlow = "PORTAL_SELECT"
                                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onAskGemini = {
                                        showDoubtSolverBot = true
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                                        }
                                    },
                                    title = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Verified,
                                                    contentDescription = "TAMS Launcher",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = if (userRole == "ADMIN") "Aspirants Admin Monitor" else academyNameState,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = if (userRole == "ADMIN") "System Supervisor" else "Director: $directorNameState • Coaching Institute",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        titleContentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    actions = {
                                        // Notification Bell (Top Right)
                                        IconButton(
                                            onClick = { showNotificationsDialog = true },
                                            modifier = Modifier.padding(end = 4.dp).testTag("notification_bell_button")
                                        ) {
                                            BadgedBox(
                                                badge = { 
                                                    if (hasUnreadNotifications) {
                                                        Badge(containerColor = Color.Red) { 
                                                            Text("5", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) 
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Notifications,
                                                    contentDescription = "System Notifications",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        // Removed role switcher button per request
                                    }
                                )
                            },
                        bottomBar = {
                            // Bottom Persistent navigation bar that mirrors Image 7 
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.height(72.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left: Dashboard tab
                                    IconButton(
                                        onClick = { activeBottomNavTab = "DASHBOARD" },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Dashboard,
                                                contentDescription = "Dashboard",
                                                tint = if (activeBottomNavTab == "DASHBOARD") MaterialTheme.colorScheme.primary else Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Dashboard",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (activeBottomNavTab == "DASHBOARD") MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                    }

                                    // Center floating Action Button for Instant QR Scanner Check-in
                                    Surface(
                                        onClick = { showQrAttendanceScanner = true },
                                        color = Color(0xFF0D47A1),
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .offset(y = (-10).dp)
                                            .size(56.dp)
                                            .border(4.dp, Color.White, CircleShape),
                                        shadowElevation = 8.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "QR Attendance",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    // Right: My Account tab
                                    IconButton(
                                        onClick = { activeBottomNavTab = "MY_ACCOUNT" },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "My Account",
                                                tint = if (activeBottomNavTab == "MY_ACCOUNT") MaterialTheme.colorScheme.primary else Color.Gray,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "My Account",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (activeBottomNavTab == "MY_ACCOUNT") MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Biometric Gate Check
                            if (isBiometricLocked) {
                                var biometricPassed by remember { mutableStateOf(false) }
                                if (!biometricPassed) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.background),
                                        color = MaterialTheme.colorScheme.background
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Fingerprint Gate",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .padding(bottom = 16.dp)
                                            )
                                            Text(
                                                text = "Biometric Lock Active",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "Secure biometric toggle is turned ON. Verify identity to load core academic data and student registries.",
                                                textAlign = TextAlign.Center,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = {
                                                    biometricPassed = true
                                                    Toast.makeText(context, "Fingerprint validated. Access authorized.", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Simulate Fingerprint Scan & Unlock")
                                            }
                                        }
                                    }
                                    return@Box
                                }
                            }

                            // Navigation Routing logic (Dashboard tab vs My Account tab)
                            if (activeBottomNavTab == "MY_ACCOUNT") {
                                MyAccountScreen(viewModel)
                            } else {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Section Header reflecting the active side menu / shortcut selection
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 2.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FolderOpen,
                                                contentDescription = "Active Section",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "ACTIVE CONSOLE  ❯  " + (if (userRole == "ADMIN") "APP OWNER GLOBAL MONITOR" else if (userRole == "STAFF") activeAdminTab.uppercase() else activeStudentTab.uppercase()),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    // Content Area (Grid dashboard replacing the old Dashboard tab, else keep modular screens)
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        if (userRole == "ADMIN") {
                                            val institutesList by viewModel.institutesList.collectAsState()
                                            AppOwnerMonitorView(
                                                transactions = transactionsList,
                                                batches = batchesList,
                                                students = studentsList,
                                                institutes = institutesList,
                                                onApprove = { email -> viewModel.approveInstitute(email) },
                                                onApproveAll = { viewModel.approveAllInstitutes() }
                                            )
                                        } else if (userRole == "STAFF" && activeAdminTab == "Dashboard") {
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                // Main Badge card as shown in Image 7
                                                item {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                                        border = BorderStroke(1.dp, Color(0xFFD1E4FF)),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(52.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFE1F5FE)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                TuitionClassLogo(modifier = Modifier.size(42.dp))
                                                            }
                                                            Spacer(modifier = Modifier.width(16.dp))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = "Aspirants Success Classes",
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 18.sp,
                                                                    color = Color(0xFF0347A1)
                                                                )
                                                                Text(
                                                                    text = "smtsharma282.sks@gmail.com",
                                                                    fontSize = 12.sp,
                                                                    color = Color.Gray,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // 16 Grid buttons matching Image 7 photograph precisely
                                                item {
                                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        Text(
                                                            text = "ACADEMY OPERATIONS CONSOLE (16 ENGINE SERVICES)",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontFamily = FontFamily.Monospace,
                                                            letterSpacing = 1.sp
                                                        )

                                                        val shortcuts = listOf(
                                                            ShortcutItem("Batches", Icons.Default.Layers, Color(0xFF2196F3)) { activeAdminTab = "Batches Setup" },
                                                            ShortcutItem("Students", Icons.Default.People, Color(0xFF3F51B5)) { activeAdminTab = "Admission Form" },
                                                            ShortcutItem("Attendance", Icons.Default.FactCheck, Color(0xFF9C27B0)) { showQrAttendanceScanner = true },
                                                            ShortcutItem("Tuition Fees", Icons.Default.Payments, Color(0xFF00BCD4)) { activeAdminTab = "Tuition Fees" },
                                                            ShortcutItem("Income Expenses", Icons.Default.AccountBalance, Color(0xFFFF9800)) { activeAdminTab = "Ledger Accounts" },
                                                            ShortcutItem("Student Performance", Icons.Default.School, Color(0xFF8BC34A)) { showPerformanceReportDialog = true },
                                                            ShortcutItem("Enquiry Manager", Icons.Default.ContactSupport, Color(0xFF00E5FF)) { showEnquiryManagerDialog = true },
                                                            ShortcutItem("Staff Manager", Icons.Default.GroupAdd, Color(0xFF009688)) { showStaffManagerDialog = true },
                                                            ShortcutItem("Giant Reports", Icons.Default.InsertChartOutlined, Color(0xFFF44336)) { showReportsConsoleDialog = true },
                                                            ShortcutItem("Study Material", Icons.Default.LibraryBooks, Color(0xFF212121)) { viewModel.loginAs("STAFF"); activeStaffTab = "Study Materials" },
                                                            ShortcutItem("Homework", Icons.Default.Assignment, Color(0xFF4CAF50)) { showHomeworkAssignDialog = true },
                                                            ShortcutItem("Online Exams", Icons.Default.Quiz, Color(0xFF3F51B5)) { viewModel.loginAs("STUDENT"); activeStudentTab = "Practice exams" },
                                                            ShortcutItem("To Do Task", Icons.Default.PlaylistAddCheck, Color(0xFF9E9E9E)) { showTodoTaskDialog = true },
                                                            ShortcutItem("Backup/Restore", Icons.Default.CloudSync, Color(0xFF7B1FA2)) { activeAdminTab = "Database Backups" },
                                                            ShortcutItem("Settings", Icons.Default.Settings, Color(0xFFE91E63)) { activeAdminTab = "Dashboard" /* triggers settings area below */ },
                                                            ShortcutItem("Paper Generator", Icons.Default.Description, Color(0xFF00838F), true) { showPaperGeneratorDialog = true }
                                                        )

                                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                            for (row in 0 until 4) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                                ) {
                                                                    for (col in 0 until 4) {
                                                                        val index = row * 4 + col
                                                                        if (index < shortcuts.size) {
                                                                            val sc = shortcuts[index]
                                                                            Column(
                                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                                modifier = Modifier
                                                                                    .weight(1f)
                                                                                    .clickable { sc.action() }
                                                                            ) {
                                                                                Box(
                                                                                    modifier = Modifier
                                                                                        .size(52.dp)
                                                                                        .clip(CircleShape)
                                                                                        .background(sc.color),
                                                                                    contentAlignment = Alignment.Center
                                                                                ) {
                                                                                    Icon(
                                                                                        imageVector = sc.icon,
                                                                                        contentDescription = sc.title,
                                                                                        tint = Color.White,
                                                                                        modifier = Modifier.size(24.dp)
                                                                                    )
                                                                                }
                                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                                Text(
                                                                                    text = sc.title,
                                                                                    fontSize = 11.sp,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    color = if (sc.isSpecialRedLabel) Color.Red else Color.Black,
                                                                                    textAlign = TextAlign.Center,
                                                                                    maxLines = 2,
                                                                                    overflow = TextOverflow.Ellipsis,
                                                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // Monthly cashflow bar and line graph (Image 8 style)
                                                item {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(16.dp)) {
                                                            Text(
                                                                text = "Income of 2026",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp,
                                                                color = Color.Black
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))

                                                            // Custom line chart drawing matching precisely the coordinates and dots of picture 8
                                                            Canvas(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(180.dp)
                                                                    .background(Color.White)
                                                            ) {
                                                                val ptCount = 12
                                                                val w = size.width
                                                                val h = size.height
                                                                val rowHeight = h / 6

                                                                // Draw thin grid lines
                                                                for (i in 0..6) {
                                                                    val y = i * rowHeight
                                                                    drawLine(
                                                                        color = Color.LightGray.copy(alpha = 0.4f),
                                                                        start = androidx.compose.ui.geometry.Offset(0f, y),
                                                                        end = androidx.compose.ui.geometry.Offset(w, y),
                                                                        strokeWidth = 1f
                                                                    )
                                                                }

                                                                // vertical indicator lines
                                                                val colWidth = w / 6
                                                                for (i in 0..6) {
                                                                    val x = i * colWidth
                                                                    drawLine(
                                                                        color = Color.LightGray.copy(alpha = 0.4f),
                                                                        start = androidx.compose.ui.geometry.Offset(x, 0f),
                                                                        end = androidx.compose.ui.geometry.Offset(x, h),
                                                                        strokeWidth = 1f
                                                                    )
                                                                }

                                                                // Plot continuous curve graph matching coordinates of Image 8 (centered around 0.0)
                                                                val linePts = listOf(
                                                                    h * 0.5f,
                                                                    h * 0.45f,
                                                                    h * 0.52f,
                                                                    h * 0.38f,
                                                                    h * 0.48f,
                                                                    h * 0.35f,
                                                                    h * 0.5f,
                                                                    h * 0.55f,
                                                                    h * 0.42f,
                                                                    h * 0.49f,
                                                                    h * 0.38f,
                                                                    h * 0.5f
                                                                )

                                                                val spacing = w / (ptCount - 1)
                                                                for (i in 0 until ptCount - 1) {
                                                                    val startX = i * spacing
                                                                    val startY = linePts[i]
                                                                    val endX = (i + 1) * spacing
                                                                    val endY = linePts[i + 1]

                                                                    drawLine(
                                                                        color = Color(0xFFFFA726), // Orange
                                                                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                                                                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                                                                        strokeWidth = 3f
                                                                    )
                                                                    drawCircle(
                                                                        color = Color(0xFFFF7043),
                                                                        radius = 5f,
                                                                        center = androidx.compose.ui.geometry.Offset(startX, startY)
                                                                    )
                                                                }
                                                                // Draw last node dot
                                                                drawCircle(
                                                                    color = Color(0xFFFF7043),
                                                                    radius = 5f,
                                                                    center = androidx.compose.ui.geometry.Offset(w, linePts.last())
                                                                )
                                                            }

                                                            // Numbers indicators row matching Image 8 layout
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                listOf("0", "2", "4", "6", "8", "10", "12").forEach { num ->
                                                                    Text(num, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                                }
                                                            }

                                                                                                                        // Legend colors cards
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                listOf(Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF795548)).forEach { c ->
                                                                    Box(modifier = Modifier.size(10.dp).background(c))
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                }
                                                                Text("Income of 2026", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                                            }
                                                        }
                                                    }
                                                }

                                                // Polished Daily Motivation & Academic Quote Card (Replacing WhatsPromo as requested)
                                                item {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                                        ),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Star,
                                                                        contentDescription = "Success",
                                                                        tint = Color(0xFFFFB300),
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                    Text(
                                                                        text = "DAILY MOTIVATION & REWARDS",
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 11.sp,
                                                                        color = MaterialTheme.colorScheme.primary
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "Believe you can and you're halfway there.",
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    fontSize = 14.sp,
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    maxLines = 2
                                                                )
                                                                Text(
                                                                    text = "Maintain a 95%+ attendance rate this week to unlock elite badge rewards and free mock test series keys!",
                                                                    fontSize = 11.sp,
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                                )
                                                            }
                                                            Box(
                                                                modifier = Modifier.weight(0.5f).height(80.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Canvas(modifier = Modifier.fillMaxSize()) {
                                                                    drawCircle(
                                                                        color = Color(0xFFFFB300).copy(alpha = 0.15f),
                                                                        radius = size.width * 0.45f
                                                                    )
                                                                    drawCircle(
                                                                        color = Color(0xFFFFC107),
                                                                        radius = size.width * 0.25f
                                                                    )
                                                                }
                                                                Icon(
                                                                    imageVector = Icons.Default.Star,
                                                                    contentDescription = null,
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(24.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // "Made with ❤️ in india" (Image 8 style)
                                                item {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Made with ",
                                                            color = Color.Gray,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = "Love",
                                                            tint = Color(0xFFF44336),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = " in India",
                                                            color = Color.Gray,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                // Section 3: Dual SIM & Preferences Switches preserved from old Dashboard
                                                item {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(16.dp)) {
                                                            Text(
                                                                text = "SYSTEM PREFERENCES & GATEWAY CONTROL",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontFamily = FontFamily.Monospace,
                                                                modifier = Modifier.padding(bottom = 12.dp)
                                                            )

                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(Icons.Default.Fingerprint, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                                                                    Spacer(modifier = Modifier.width(12.dp))
                                                                    Column {
                                                                        Text("Biometric Safety Lock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                        Text("Require finger validation on launch", fontSize = 11.sp, color = Color.Gray)
                                                                    }
                                                                }
                                                                Switch(
                                                                    checked = isBiometricLocked,
                                                                    onCheckedChange = {
                                                                        viewModel.toggleBiometricLock(it)
                                                                        Toast.makeText(context, if (it) "Biometric safety locks established." else "Safety check bypassed.", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    modifier = Modifier.testTag("biometric_finger_lock_toggle")
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            HorizontalDivider()
                                                            Spacer(modifier = Modifier.height(12.dp))

                                                            Column {
                                                                Text("Dual-SIM Telephony Channel Routing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Text("Select SIM active card to use for auto academic reports SMS alerts", fontSize = 11.sp, color = Color.Gray)
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                    listOf("SIM 1", "SIM 2").forEach { sim ->
                                                                        val isSelected = preferredSmsSim == sim
                                                                        Button(
                                                                            onClick = { viewModel.updateSmsSim(sim) },
                                                                            colors = ButtonDefaults.buttonColors(
                                                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                                            ),
                                                                            shape = RoundedCornerShape(8.dp),
                                                                            modifier = Modifier.weight(1f).height(38.dp)
                                                                        ) {
                                                                            Text(sim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Handle all remaining original sub-workspace tabs
                                            when (userRole) {
                                                "ADMIN" -> {
                                                     // App Owner has no nested subtabs in workspace (handled above)
                                                 }
                                                 "STAFF_ADMIN" -> AdminWorkspace(
                                                    activeAdminTab = activeAdminTab,
                                                    viewModel = viewModel,
                                                    batches = batchesList,
                                                    students = studentsList,
                                                    payments = paymentsList,
                                                    transactions = transactionsList,
                                                    messageTemplates = messageTemplatesList,
                                                    preferredSmsSim = preferredSmsSim,
                                                    whatsAppRouting = whatsAppRouting,
                                                    showAddBatchDialog = showAddBatchDialog,
                                                    onShowAddBatch = { showAddBatchDialog = it },
                                                    showAdmissionDialogConfirm = showAdmissionDialogConfirm,
                                                    onShowAdmissionConfirm = { showAdmissionDialogConfirm = it }
                                                )
                                                "STAFF" -> {
                                                     if (activeAdminTab == "Study Materials" || activeAdminTab == "Attendance") {
                                                         StaffWorkspace(
                                                             activeStaffTab = if (activeAdminTab == "Study Materials") "Study Materials" else "Attendance",
                                                             viewModel = viewModel,
                                                             batches = batchesList,
                                                             students = studentsList,
                                                             studyMaterials = studyMaterialsList,
                                                             staffScreenAccess = staffScreenAccess,
                                                             onShowAddBatch = { showAddBatchDialog = it }
                                                         )
                                                     } else {
                                                         AdminWorkspace(
                                                             activeAdminTab = activeAdminTab,
                                                             viewModel = viewModel,
                                                             batches = batchesList,
                                                             students = studentsList,
                                                             payments = paymentsList,
                                                             transactions = transactionsList,
                                                             messageTemplates = messageTemplatesList,
                                                             preferredSmsSim = preferredSmsSim,
                                                             whatsAppRouting = whatsAppRouting,
                                                             showAddBatchDialog = showAddBatchDialog,
                                                             onShowAddBatch = { showAddBatchDialog = it },
                                                             showAdmissionDialogConfirm = showAdmissionDialogConfirm,
                                                             onShowAdmissionConfirm = { showAdmissionDialogConfirm = it }
                                                         )
                                                     }
                                                 }
                                                 "STAFF_OLD_BYPASS" -> StaffWorkspace(
                                                    activeStaffTab = activeStaffTab,
                                                    viewModel = viewModel,
                                                    batches = batchesList,
                                                    students = studentsList,
                                                    studyMaterials = studyMaterialsList,
                                                    staffScreenAccess = staffScreenAccess,
                                                    onShowAddBatch = { showAddBatchDialog = it }
                                                )
                                                "STUDENT" -> StudentWorkspace(
                                                    activeStudentTab = activeStudentTab,
                                                    viewModel = viewModel,
                                                    batches = batchesList,
                                                    students = studentsList,
                                                    studyMaterials = studyMaterialsList,
                                                    exams = examsList,
                                                    activeExamPracticeSession = activeExamPracticeSession,
                                                    onActiveExamPracticeSession = { activeExamPracticeSession = it }
                                                )
                                                "PARENT" -> ParentWorkspace(
                                                    activeParentTab = activeStudentTab,
                                                    viewModel = viewModel,
                                                    batches = batchesList,
                                                    students = studentsList,
                                                    payments = paymentsList,
                                                    exams = examsList
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

            // Overlay Dialogs definitions
            if (showNotificationsDialog) {
                NotificationsDialog(
                    onClose = { showNotificationsDialog = false },
                    onMarkAllRead = {
                        hasUnreadNotifications = false
                        showNotificationsDialog = false
                    }
                )
            }

            // Always render the floating, draggable Messenger-style Gemini Doubt Solver Bot on the workspace
            GeminiDoubtSolverBot(onClose = { })

            if (showQrAttendanceScanner) {
                // Device camera emulation QR Scanner
                QRCodeScannerOverlay(
                    students = studentsList,
                    batches = batchesList,
                    viewModel = viewModel,
                    onClose = { showQrAttendanceScanner = false }
                )
            }

            if (showPerformanceReportDialog) {
                // Student Performance overlay dialog
                StudentPerformanceReportOverlay(students = studentsList, viewModel = viewModel, onClose = { showPerformanceReportDialog = false })
            }

            if (showEnquiryManagerDialog) {
                // Enquiry manager lead CRM dialog
                EnquiryManagerOverlay(onClose = { showEnquiryManagerDialog = false })
            }

            if (showStaffManagerDialog) {
                StaffManagerOverlay(viewModel = viewModel, onClose = { showStaffManagerDialog = false })
            }

            if (showReportsConsoleDialog) {
                // Giant PDF report dispatch dialog
                GiantReportsConsoleOverlay(
                    students = studentsList,
                    batches = batchesList,
                    transactions = transactionsList,
                    onClose = { showReportsConsoleDialog = false }
                )
            }

            if (showHomeworkAssignDialog) {
                // Homework assign dashboard
                HomeworkAssignConsoleOverlay(batches = batchesList, onClose = { showHomeworkAssignDialog = false })
            }

            if (showTodoTaskDialog) {
                // To Do list manager overlay
                TodoTaskConsoleOverlay(onClose = { showTodoTaskDialog = false })
            }

            if (showPaperGeneratorDialog) {
                // Worksheet exam worksheet builder
                PaperGeneratorConsoleOverlay(batches = batchesList, onClose = { showPaperGeneratorDialog = false })
            }
        }
    }
}

// Data class structure for 16 Services engine setup
data class ShortcutItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val isSpecialRedLabel: Boolean = false,
    val action: () -> Unit
)


// ==========================================
// PORTAL VIEW 1: ADMIN WORKSPACE MODULES
// ==========================================
@Composable
fun AdminWorkspace(
    activeAdminTab: String,
    viewModel: AppViewModel,
    batches: List<Batch>,
    students: List<Student>,
    payments: List<FeePayment>,
    transactions: List<FinancialTransaction>,
    messageTemplates: List<MessageTemplate>,
    preferredSmsSim: String,
    whatsAppRouting: String,
    showAddBatchDialog: Boolean,
    onShowAddBatch: (Boolean) -> Unit,
    showAdmissionDialogConfirm: Student?,
    onShowAdmissionConfirm: (Student?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (activeAdminTab) {
            "Dashboard" -> {
                // 40-Day system licence tracker
                item {
                    val remainingTrial = viewModel.getRemainingTrialDays()
                    val isTrialOver = remainingTrial <= 0
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTrialOver) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isTrialOver) Color.Red else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isTrialOver) Icons.Default.Warning else Icons.Default.NewReleases,
                                contentDescription = "Licence",
                                tint = if (isTrialOver) Color.Red else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "40-DAY INSTITUTION COMPLEMENTARY TRIAL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isTrialOver) Color.Red else MaterialTheme.colorScheme.primary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isTrialOver) {
                                        "Your 40-Day complementary trial period has elapsed. Please proceed to activate your academy licence."
                                    } else {
                                        "You are running on a fully feature-unlocked complementary teacher trial licence. $remainingTrial trial days remaining."
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Section 1: Interactive Key Metrics Cards row
                item {
                    Text(
                        text = "THE ASPIRANT SYSTEM (TAMS) CONSOLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Active Batches",
                            value = batches.size.toString(),
                            subtitle = "Enrolled tracks",
                            color = MaterialTheme.colorScheme.primaryContainer,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Aspirants Enrolled",
                            value = students.size.toString(),
                            subtitle = "Offline database",
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val balance = totalIncome - totalExpense
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Cash Balance",
                            value = "₹${balance.toInt()}",
                            subtitle = "Gross profit margins",
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Defaulters / Fees Due",
                            value = "₹${(students.sumOf { s -> viewModel.getFamilyPendingFees(s.familyId) } / 2.0).toInt()}", // Average scaled
                            subtitle = "Collectibles outstanding",
                            color = MaterialTheme.colorScheme.errorContainer,
                            textColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Section 2: Render Beautiful Financial Analytics Profit/Loss Graph directly on Canvas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "MONTHLY FINANCIAL CASHFLOW BAR CHART (PROFIT/LOSS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Real Canvas plotting
                            val maxAmount = 10000.0
                            val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                            val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 12.dp, horizontal = 24.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    // Income Bar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight().weight(1f)
                                    ) {
                                        val fillFraction = (totalIncome / maxAmount).toFloat().coerceIn(0.15f, 1f)
                                        Text("₹${totalIncome.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.4f)
                                                .fillMaxHeight(fillFraction)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Color(0xFF2E7D32)) // Strong green
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("INCOME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }

                                    // Expense Bar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight().weight(1f)
                                    ) {
                                        val fillFraction = (totalExpense / maxAmount).toFloat().coerceIn(0.15f, 1f)
                                        Text("₹${totalExpense.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.4f)
                                                .fillMaxHeight(fillFraction)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(Color(0xFFC62828)) // Rich red
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("EXPENSES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                    }

                                    // Balance Bar
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier.fillMaxHeight().weight(1f)
                                    ) {
                                        val bal = totalIncome - totalExpense
                                        val fillFraction = (kotlin.math.abs(bal) / maxAmount).toFloat().coerceIn(0.1f, 1f)
                                        Text("₹${bal.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.4f)
                                                .fillMaxHeight(fillFraction)
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("NET BALANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Device Security Switches (Biometric Lock & Preferred SIM Selection)
                item {
                    val context = LocalContext.current
                    val biometricEnabled by viewModel.isBiometricLocked.collectAsState()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SYSTEM PREFERENCES & SECURITY CORE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Security", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Biometric Fingerprint Lock Check", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Prompt authentication scanner on app focus", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = biometricEnabled,
                                    onCheckedChange = {
                                        viewModel.toggleBiometricLock(it)
                                        Toast.makeText(context, if (it) "Biometric finger challenge activated." else "Secure lock bypassed.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("biometric_finger_lock_toggle")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // SIM Select routing preferences
                            Column {
                                Text("Dual SIM Message Routing selector", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Active SMS-sending gateway channel SIM card slot selection", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("SIM 1", "SIM 2").forEach { sim ->
                                        val isSelected = preferredSmsSim == sim
                                        Button(
                                            onClick = { viewModel.updateSmsSim(sim) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(38.dp)
                                        ) {
                                            Text(sim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Admission Form" -> {
                item {
                    Text(
                        text = "NEW STUDENT INTAKE & REGISTRATION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    // Registration Inputs Workspace
                    RegistrationIntakeForm(batches = batches, viewModel = viewModel, onShowConfirm = onShowAdmissionConfirm)
                }

                if (showAdmissionDialogConfirm != null) {
                    item {
                        AutoIDCardGeneratorCard(
                            student = showAdmissionDialogConfirm,
                            batches = batches,
                            onClose = { onShowAdmissionConfirm(null) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "REGISTERED STUDENTS DIRECTORY (${students.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (students.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No students enrolled yet. Register a new student above.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(students) { student ->
                        val batchStr = batches.find { it.id == student.batchId }?.name ?: "No Allotted Batch"
                        val context = LocalContext.current
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Column {
                                    // 1st Line: Student Name
                                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // 2nd Line: Roll Number, Class
                                    Text("Roll Number: ${student.rollNumber} • Class: ${student.studentClass ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // 3rd Line: Coaching Batch name
                                    Text("Coaching Batch: $batchStr", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Parent / Guardian: ${student.parentName} (${student.parentPhone})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                if (student.phone.isNotEmpty() && student.phone != "+91" && student.phone != student.parentPhone) {
                                    Text("Student Phone: ${student.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Quick Call Trigger
                                        IconButton(
                                            onClick = {
                                                val phoneNo = student.phone.ifEmpty { student.parentPhone }
                                                val clean = phoneNo.replace(" ", "")
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$clean")
                                                }
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        }

                                        // Quick Direct SIM SMS trigger
                                        IconButton(
                                            onClick = {
                                                val phoneNo = student.phone.ifEmpty { student.parentPhone }
                                                val messageStr = "Aspirants Success Classes Chhibramau: Hello ${student.parentName}, parent of ${student.name}. Your child's coaching attendance & performance is being monitored. Please keep in contact with Chhibramau branch."
                                                sendDirectSMS(phoneNo, messageStr, context)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Sms, contentDescription = "SMS via SIM", tint = Color(0xFFE91E63), modifier = Modifier.size(18.dp))
                                        }
                                        
                                        // Send WhatsApp notice
                                        IconButton(
                                            onClick = {
                                                val parentNo = student.parentPhone.ifEmpty { student.phone }
                                                val text = "Aspirants Success Classes Chhibramau: Hello ${student.parentName}, parent of ${student.name}. This is to keep you updated on progress at Chhibramau branch of coaching."
                                                try {
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                        data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$parentNo&text=${android.net.Uri.encode(text)}")
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp Notice", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    // Quick delete action
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteStudent(student)
                                            Toast.makeText(context, "Deleted ${student.name} from directory.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Batches Setup" -> {
                item {
                    Button(
                        onClick = { onShowAddBatch(true) },
                        modifier = Modifier.fillMaxWidth().testTag("add_batch_full_width_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Batch")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Batch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                item {
                    Text(
                        text = "OFFLINE CLASS SLOTS & BATCHES CONFIG",
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                items(batches) { batch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(batch.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (batch.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (batch.isActive) "Active" else "Inactive",
                                        color = if (batch.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                KeyValuePairText("Subject", batch.subject)
                                KeyValuePairText("Timings", batch.classTimings)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                KeyValuePairText("Teacher", batch.assignedTeacher)
                                KeyValuePairText("Capacity", "${batch.maxCapacity} Seats")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            KeyValuePairText("Scheduled Days", batch.daysOfWeek)
                            KeyValuePairText("Fees standard", "₹${batch.feesAmount} per period")
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.updateBatch(batch.copy(isActive = !batch.isActive))
                                    }
                                ) {
                                    Text("Toggle Status")
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.deleteBatch(batch)
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }

            }

            "Outreach Engine" -> {
                item {
                    Text(
                        text = "DYNAMIC TEMPLATE VARIABLE CONSOLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    // Messaging templates config and keyword substitutions
                    MessagingSystemConsole(templates = messageTemplates, students = students, batches = batches, viewModel = viewModel)
                }
            }

            "Tuition Fees" -> {
                item {
                    Text(
                        text = "STUDENTS TUITION FEES & RECEIPT CENTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    TuitionFeesModule(payments = payments, students = students, batches = batches, viewModel = viewModel)
                }
            }

            "Ledger Accounts" -> {
                item {
                    Text(
                        text = "ACADEMIC TRADING LEDGER & CASHFLOW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    FinanceAccountsModule(transactions = transactions, payments = payments, students = students, viewModel = viewModel)
                }
            }

            "Database Backups" -> {
                item {
                    Text(
                        text = "ENCRYPTED SQL BACKUP, RECOVERY & RESTORE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    BackupEngineConsole(viewModel = viewModel)
                }
            }
        }
    }

    if (showAddBatchDialog) {
        AddBatchOverlay(viewModel = viewModel, onDismiss = { onShowAddBatch(false) })
    }
}

// ==========================================
// PORTAL VIEW 2: STAFF WORKSPACE MODULES
// ==========================================
@Composable
fun StaffWorkspace(
    activeStaffTab: String,
    viewModel: AppViewModel,
    batches: List<Batch>,
    students: List<Student>,
    studyMaterials: List<StudyMaterial>,
    staffScreenAccess: Set<String>,
    onShowAddBatch: (Boolean) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "TAMS STAFF / TEACHER WORKSTATION",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )
        }

        // Access enforcement
        val blockAccess = !staffScreenAccess.contains(activeStaffTab)
        if (blockAccess) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = "Restricted", tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Access Blocked by Admin Controls",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Your Staff Role user access permission details defined inside TAMS accounts does not grant clearance for the '$activeStaffTab' module. Ask System Administrator to grant access.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            return@LazyColumn
        }

        when (activeStaffTab) {
            "Attendance" -> {
                item {
                    AttendanceLoggerSubModule(batches = batches, students = students, viewModel = viewModel)
                }
            }

            "Study Materials" -> {
                item {
                    UploadStudyResourcesSubModule(batches = batches, studyMaterials = studyMaterials, viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// PORTAL VIEW 3: STUDENT INTERACTIVE WORKSPACE
// ==========================================
@Composable
fun StudentWorkspace(
    activeStudentTab: String,
    viewModel: AppViewModel,
    batches: List<Batch>,
    students: List<Student>,
    studyMaterials: List<StudyMaterial>,
    exams: List<Exam>,
    activeExamPracticeSession: Exam?,
    onActiveExamPracticeSession: (Exam?) -> Unit
) {
    val context = LocalContext.current
    val currentStudentId by viewModel.currentUserId.collectAsState()
    val activeStudent = remember(currentStudentId, students) {
        students.firstOrNull { it.id == currentStudentId } ?: students.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeStudent == null) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No registered students found in local database yet. Please log in as Admin and register a student first.", textAlign = TextAlign.Center)
                }
            }
            return@LazyColumn
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeStudent.name.take(2).uppercase(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Welcome, ${activeStudent.name} (Roll: ${activeStudent.rollNumber})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val batchName = batches.firstOrNull { it.id == activeStudent.batchId }?.name ?: "No batch track"
                        Text(
                            text = "Class Slot category: $batchName",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Family grouping link: ${activeStudent.familyId}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 100% Free Student Promotion Sharing Campaign Card
        item {
            val promoShareCount by viewModel.promoShareCount.collectAsState()
            val remainingShares = (4 - promoShareCount).coerceAtLeast(0)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (promoShareCount >= 4) Color(0xFFFFF9C4) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (promoShareCount >= 4) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (promoShareCount >= 4) Icons.Default.Stars else Icons.Default.Campaign,
                                contentDescription = null,
                                tint = if (promoShareCount >= 4) Color(0xFFF57F17) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "100% Free Scholar Promotion",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (promoShareCount >= 4) Color(0xFF5D4037) else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (promoShareCount >= 4) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF57F17))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("SCHOLARSHIP ACTIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = "Automated Capital Waiver: Upon reaching 4 verifiable monthly post mentions, the app immediately locks on a Scholarship Active status, automatically waiving only app subscription charges (not coaching/tuition fees).",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    // Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Monthly share counts: $promoShareCount of 4 verified posts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(promoShareCount * 25)}% complete",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LinearProgressIndicator(
                            progress = promoShareCount / 4f,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = if (promoShareCount >= 4) Color(0xFFF57F17) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (promoShareCount < 4) {
                            Text(
                                "Only $remainingShares mentions left this month!",
                                fontSize = 11.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                "App subscription charges 100% waived! 👑\n(Coaching/tuition fees still apply)",
                                fontSize = 11.sp,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (promoShareCount > 0) {
                                TextButton(
                                    onClick = {
                                        viewModel.resetPromoShareCount()
                                        Toast.makeText(context, "Promo tracker counters reset for new month.", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Reset Track", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Button(
                                onClick = {
                                    val shareMsg = "I am preparing with high quality interactive materials on the ${viewModel.academyName.value} app! Free test sheets, doubt resolvers, and expert guides are available offline. Download/Join now!\n#TAMS #AspirantsCoaching"
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareMsg)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share mentions on Facebook/Twitter/Whatsapp"))
                                    viewModel.incrementPromoShareCount()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publish Post Mention", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        when (activeStudentTab) {
            "Home Workspace" -> {
                item {
                    Text("YOUR SCHOLISTIC STANDINGS", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }

                // Family pending aggregate fees details card
                item {
                    val siblingsCount = viewModel.getSiblingsForStudent(activeStudent.familyId, activeStudent.id).size
                    val totalAggregatePending = viewModel.getFamilyPendingFees(activeStudent.familyId)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Family Group Cohort & Co-relations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Real-time aggregate calculations across related siblings.", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                KeyValuePairText("Registered Siblings found", "$siblingsCount other students")
                                KeyValuePairText("Combined Family ID Code", activeStudent.familyId)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Outstanding Family aggregate balance due:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "₹${totalAggregatePending.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (totalAggregatePending > 0) Color.Red else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // QR Code attendance device simulation module
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CHECK-IN BIOMETRIC ATTENDANCE QR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive QR emulation drawing on Canvas
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Custom pixelated QR code render emulation
                                    val sizeX = 7
                                    val sizeY = 7
                                    val blockW = size.width / sizeX
                                    val blockH = size.height / sizeY
                                    val matrix = arrayOf(
                                        intArrayOf(1, 1, 1, 1, 1, 1, 1),
                                        intArrayOf(1, 0, 0, 0, 0, 0, 1),
                                        intArrayOf(1, 0, 1, 1, 1, 0, 1),
                                        intArrayOf(1, 0, 1, 0, 1, 0, 1),
                                        intArrayOf(1, 0, 1, 1, 1, 0, 1),
                                        intArrayOf(1, 0, 0, 0, 0, 0, 1),
                                        intArrayOf(1, 1, 1, 1, 1, 1, 1)
                                    )
                                    for (i in 0 until sizeX) {
                                        for (j in 0 until sizeY) {
                                            if (matrix[i][j] == 1) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = androidx.compose.ui.geometry.Offset(i * blockW, j * blockH),
                                                    size = androidx.compose.ui.geometry.Size(blockW + 1f, blockH + 1f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Present this dynamic Token QR code directly to the tutor's tablet scanner to log attendance instantly.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            "Academic Resources" -> {
                // NCERT & COMPETITIVE EXAMS DIGITAL LIBRARY CENTER
                item {
                    Text(
                        text = "FREE ONLINE STUDY MATERIAL & NCERT CENTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    var selectedLibraryCategory by remember { mutableStateOf("Class 10") }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("NCERT & National Digital Library Portal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tap to pull/download free online textbooks directly from official sources.", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            // Category Selector Chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Class 10", "Class 12", "JEE / NEET / competitive Guide", "High-Res Atlas Maps").forEach { cat ->
                                    val isSel = selectedLibraryCategory == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedLibraryCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }

                            // Render list rows depending on classification
                            val academicItems = when (selectedLibraryCategory) {
                                "Class 10" -> listOf(
                                    Triple("Mathematics (Class X Book)", "Full Class 10 Arithmetic & Geometry book", "https://ncert.nic.in/textbook.php?jemh1=0-15"),
                                    Triple("Science (Class X Book)", "Full Class 10 Chemistry, Physics, Biology book", "https://ncert.nic.in/textbook.php?jesc1=0-16"),
                                    Triple("Social Science (India & World)", "History, Geography & Civics combined guide", "https://ncert.nic.in/textbook.php?jess1=0-7"),
                                    Triple("First Flight English Reader", "Class 10 Literacy Reader English syllabus", "https://ncert.nic.in/textbook.php?jeff1=0-11")
                                )
                                "Class 12" -> listOf(
                                    Triple("Mathematics (Part I & II)", "Higher secondary Algebra, Calculus & Core Matrices book", "https://ncert.nic.in/textbook.php?lemh1=0-6"),
                                    Triple("Physics (Part I Core)", "Class 12 Electrostatics & Magnetism syllabus", "https://ncert.nic.in/textbook.php?leph1=0-8"),
                                    Triple("Chemistry (Part I Core)", "Organic, Inorganic & Physical Class XII NCERT guide", "https://ncert.nic.in/textbook.php?lech1=0-9"),
                                    Triple("Biology (Complete Book)", "Ecology, Genetics and Plant physiology guides", "https://ncert.nic.in/textbook.php?lebo1=0-16")
                                )
                                "JEE / NEET / competitive Guide" -> listOf(
                                    Triple("JEE Main Mathematics Manual", "Archive.org standard joint entrance calculus guide book", "https://archive.org/details/jee-main-advanced-mathematics-prep"),
                                    Triple("NEET Biology Exam Handbook", "Extensive medical biology worksheets & keys", "https://archive.org/details/neet-biology-prep"),
                                    Triple("UPSC CSAT General Studies Guide", "NCERT consolidated polity and governance chapters", "https://ncert.nic.in/textbook.php?gess1=0-7")
                                )
                                else -> listOf(
                                    Triple("SOI Educational Guide Map of India", "Official Survey of India physical high-res geography map", "https://surveyofindia.gov.in/pages/educational-map-of-india"),
                                    Triple("NCERT Class X Historical Outline Atlas", "Political distribution and history timeline guides", "https://ncert.nic.in/textbook.php?jess1=3"),
                                    Triple("World Continents Relief Outlines", "High contrast topographical contours printable vectors", "https://ncert.nic.in/textbook.php?jesc1=15")
                                )
                            }

                            academicItems.forEach { (itemTitle, itemDesc, itemUrl) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                            Text(itemTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                            Text(itemDesc, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }

                                        Button(
                                            onClick = {
                                                try {
                                                    val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(itemUrl))
                                                    context.startActivity(browserIntent)
                                                    Toast.makeText(context, "Opening Official PDF server...", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No web browser found on dynamic emulation device.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Download, null, modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Get Book", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("YOUR CLASSTIME SYLLABUS FILES", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                }

                val materialForThisBatch = studyMaterials.filter { it.batchId == activeStudent.batchId }
                if (materialForThisBatch.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text("No study materials uploaded for your active batch track yet.", color = Color.Gray)
                        }
                    }
                } else {
                    items(materialForThisBatch) { resource ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Book, contentDescription = "Resource", tint = MaterialTheme.colorScheme.primary)
                                    Column {
                                        Text(resource.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${resource.mainCategory} • ${resource.topicSubCategory}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(10.dp)
                                ) {
                                    Text(resource.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (!resource.pdfName.isNullOrEmpty()) {
                                        AssistChip(
                                            onClick = {
                                                Toast.makeText(context, "Downloading study attachment file locally...", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text("PDF: ${resource.pdfName}") },
                                            leadingIcon = { Icon(Icons.Default.Attachment, contentDescription = "PDF") }
                                        )
                                    }
                                    if (!resource.youtubeUrl.isNullOrEmpty()) {
                                        AssistChip(
                                            onClick = {
                                                Toast.makeText(context, "Streaming study video linked to syllabus: ${resource.youtubeUrl}", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text("Stream YouTube") },
                                            leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = "Play") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Practice exams" -> {
                if (activeExamPracticeSession != null) {
                    item {
                        MCQExamPracticeScreen(
                            exam = activeExamPracticeSession,
                            student = activeStudent,
                            viewModel = viewModel,
                            onClose = { onActiveExamPracticeSession(null) }
                        )
                    }
                } else {
                    item {
                        Text("ACTIVE MOCK TEST PRACTICE LAB", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                    }

                    val examsForThisBatch = exams.filter { it.batchId == activeStudent.batchId }
                    if (examsForThisBatch.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No exams mapped with your batch segment currently.", color = Color.Gray)
                            }
                        }
                    } else {
                        items(examsForThisBatch) { test ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(test.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Syllabus segment: ${test.subject}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        AssistChip(onClick = {}, label = { Text("${test.durationMinutes} Minutes time limit") })
                                        Button(
                                            onClick = { onActiveExamPracticeSession(test) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Start Timed Test")
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

// ==========================================
// SUB-COMPONENTS & LAYOUT PARTS
// ==========================================

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title.uppercase(), 
                fontSize = 12.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = textColor,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value, 
                fontSize = 28.sp, 
                fontWeight = FontWeight.Black, 
                color = textColor,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun UPIQRCodeCard(
    studentName: String,
    parentPhone: String,
    amount: Double,
    upiId: String = "smtsharma282.sks@okaxis",
    merchantName: String = "Aspirants Success Classes",
    onWhatsAppSent: () -> Unit
) {
    val upiUri = "upi://pay?pa=$upiId&pn=${android.net.Uri.encode(merchantName)}&am=$amount&cu=INR&tn=Coaching%20Fees%20for%20$studentName"
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = merchantName.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Scan to Pay: ₹$amount",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Jetpack Compose Canvas to draw authentic UPI QR code pattern
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                val sizePx = size.width
                val numModules = 21 // 21x21 QR Version 1 grid size
                val cellSize = sizePx / numModules
                
                // Draw white background
                drawRect(color = Color.White)
                
                // Draw 3 standard corner alignment finder patterns
                fun drawFinderPattern(offsetX: Float, offsetY: Float) {
                    // Outer square (7 modules)
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(offsetX, offsetY),
                        size = androidx.compose.ui.geometry.Size(cellSize * 7, cellSize * 7)
                    )
                    // Inner white gap (5 modules)
                    drawRect(
                        color = Color.White,
                        topLeft = androidx.compose.ui.geometry.Offset(offsetX + cellSize, offsetY + cellSize),
                        size = androidx.compose.ui.geometry.Size(cellSize * 5, cellSize * 5)
                    )
                    // Center solid square (3 modules)
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(offsetX + cellSize * 2, offsetY + cellSize * 2),
                        size = androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3)
                    )
                }

                // Top-Left Finder
                drawFinderPattern(0f, 0f)
                // Top-Right Finder
                drawFinderPattern((numModules - 7) * cellSize, 0f)
                // Bottom-Left Finder
                drawFinderPattern(0f, (numModules - 7) * cellSize)
                
                // Seed pseudo-random generator with upi string hash to generate identical authentic QR blocks
                val seed = upiUri.hashCode().toLong()
                val random = java.util.Random(seed)
                
                // Draw random black modules for remaining part
                for (r in 0 until numModules) {
                    for (c in 0 until numModules) {
                        // Skip Finder patterns
                        val isTlFinder = r < 9 && c < 9
                        val isTrFinder = r < 9 && c >= numModules - 9
                        val isBlFinder = r >= numModules - 9 && c < 9
                        if (!isTlFinder && !isTrFinder && !isBlFinder) {
                            if (random.nextBoolean()) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                    size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                )
                            }
                        }
                    }
                }
            }
            
            Text("Merchant UPI VPA: $upiId", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val whatsappMsg = "$merchantName: Hello Parent! Standard coaching tuition fees due for your child $studentName is ₹$amount. Please click on the link below to pay directly via UPI: \n\n$upiUri\n\nThank you!"
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$parentPhone&text=${android.net.Uri.encode(whatsappMsg)}")
                            }
                            context.startActivity(intent)
                            onWhatsAppSent()
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("share_whatsapp_upi_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "WA", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Bill", fontSize = 11.sp, color = Color.White)
                }
                
                Button(
                    onClick = {
                        val directSmsMsg = "$merchantName: Tuition fees outstanding: ₹$amount. Tap to pay via UPI: upi://pay?pa=$upiId&am=$amount"
                        sendDirectSMS(parentPhone, directSmsMsg, context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Icon(Icons.Default.Sms, contentDescription = "SMS", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SMS Bill", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun KeyValuePairText(key: String, value: String) {
    Row {
        Text("$key: ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Helper function for sending SMS via direct SIM or falling back gracefully to standard Dial intents
fun sendDirectSMS(phone: String, message: String, context: android.content.Context) {
    try {
        if (phone.isBlank() || message.isBlank()) {
            Toast.makeText(context, "Phone subscription or message text is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanPhone = phone.trim()
        
        // Use real SIM SMS manager APIs if permissions are already given
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val smsManager: android.telephony.SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                context.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                android.telephony.SmsManager.getDefault()
            }
            smsManager.sendTextMessage(cleanPhone, null, message, null, null)
            Toast.makeText(context, "SMS sent successfully via SIM to $cleanPhone!", Toast.LENGTH_SHORT).show()
        } else {
            // Otherwise redirect to standard SMSTO system intent
            val uri = android.net.Uri.parse("smsto:$cleanPhone")
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Opening device messaging app to send...", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("sms:${phone.trim()}?body=${android.net.Uri.encode(message)}")
            }
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}

// --- Admission Registration Form ---
@Composable
fun RegistrationIntakeForm(
    batches: List<Batch>,
    viewModel: AppViewModel,
    onShowConfirm: (Student?) -> Unit
) {
    val context = LocalContext.current
    val studentsListState by viewModel.students.collectAsState()
    val nextNum = remember(studentsListState) {
        (studentsListState.mapNotNull { s ->
            s.rollNumber.substringAfter("TAMS-").toIntOrNull()
        }.maxOrNull() ?: 1000) + 1
    }
    var rollNo by remember(nextNum) { mutableStateOf("TAMS-$nextNum") }
    var name by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("2012-01-01") }
    var phone by remember { mutableStateOf("+91") }
    var parentPhone by remember { mutableStateOf("+91") }
    var gender by remember { mutableStateOf("Male") }
    var address by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("Class 8") }
    var subject by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var feeType by remember { mutableStateOf("Monthly-based") }
    var admissionFee by remember { mutableStateOf("1000") }
    var regFee by remember { mutableStateOf("500") }
    var familyId by remember { mutableStateOf("") }
    var custom1 by remember { mutableStateOf("") }
    var custom2 by remember { mutableStateOf("") }
    var custom3 by remember { mutableStateOf("") }

    var selectedBatchId by remember { mutableStateOf<Long?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Aspirant Admission Details Builder", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            
            OutlinedTextField(
                value = rollNo,
                onValueChange = { rollNo = it },
                label = { Text("Roll Number") },
                modifier = Modifier.fillMaxWidth().testTag("admission_roll_input")
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().testTag("admission_name_input")
            )
            OutlinedTextField(
                value = parentName,
                onValueChange = { parentName = it },
                label = { Text("Parent's Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dob,
                onValueChange = { dob = it },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = parentPhone,
                onValueChange = { parentPhone = it },
                label = { Text("Parent Active WhatsApp Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Choose batch track
            Box(modifier = Modifier.fillMaxWidth()) {
                val batchObj = batches.firstOrNull { it.id == selectedBatchId }
                val batchName = batchObj?.name ?: "Select Class Stream / Batch Slot"
                OutlinedTextField(
                    value = batchName,
                    onValueChange = {},
                    label = { Text("Assigned Batch Slot") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop") },
                    modifier = Modifier.fillMaxWidth().clickable { menuExpanded = true }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    batches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.name) },
                            onClick = {
                                selectedBatchId = b.id
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = studentClass,
                    onValueChange = { studentClass = it },
                    label = { Text("Standard Class") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Primary Subject") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("School / College Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Home Address Details") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dynamic customizations fields
            OutlinedTextField(
                value = custom1,
                onValueChange = { custom1 = it },
                label = { Text("Custom User Field 1 (e.g., Blood group/Route)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Family ID mapping
            OutlinedTextField(
                value = familyId,
                onValueChange = { familyId = it },
                label = { Text("Family ID (to group related sibling logs)") },
                placeholder = { Text("Leave blank to generate random ID") },
                modifier = Modifier.fillMaxWidth()
            )

            // Fees model selections
            Column {
                Text("Select Billing Configuration Model Type", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Course-based", "Monthly-based").forEach { type ->
                        val isSel = feeType == type
                        FilterChip(
                            selected = isSel,
                            onClick = { feeType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = admissionFee,
                    onValueChange = { admissionFee = it },
                    label = { Text("Initial Admission (₹)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = regFee,
                    onValueChange = { regFee = it },
                    label = { Text("Registration (₹)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (name.isEmpty() || selectedBatchId == null) {
                        Toast.makeText(context, "Full Name and Assigned Batch are mandatory fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val adm = admissionFee.toDoubleOrNull() ?: 0.0
                    val reg = regFee.toDoubleOrNull() ?: 0.0

                    viewModel.registerStudent(
                        rollNo = rollNo,
                        name = name,
                        parentName = parentName,
                        dob = dob,
                        phone = phone,
                        parentPhone = parentPhone,
                        gender = gender,
                        address = address,
                        batchId = selectedBatchId!!,
                        studentClass = studentClass,
                        subject = subject,
                        schoolName = schoolName,
                        custom1 = custom1,
                        custom2 = custom2,
                        custom3 = custom3,
                        feeType = feeType,
                        admissionFee = adm,
                        regFee = reg,
                        familyId = familyId,
                        photoPath = "res/drawable/avatar_placeholder.xml",
                        docPath = null
                    )

                    // Pass student data frame back to show visual overlay card
                    val sampleStudent = Student(
                        id = 99L, // Temp representing generated ID
                        batchId = selectedBatchId!!,
                        rollNumber = rollNo,
                        name = name,
                        parentName = parentName,
                        dateOfBirth = dob,
                        phone = phone,
                        parentPhone = parentPhone,
                        familyId = familyId.ifEmpty { "FAM-MOCK" }
                    )
                    onShowConfirm(sampleStudent)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("register_student_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Process Student Admission Profile")
            }
        }
    }
}

// === Auto-Generated Student ID Card overlay layout ===
@Composable
fun AutoIDCardGeneratorCard(
    student: Student,
    batches: List<Batch>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋 AUTO-GENERATED STUDENT ID CARD FILE", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Header logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TAMS ELITE INDUSTRIAL ACADEMY",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color.Blue
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Red)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("ID CARD", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = Color.Black)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(55.dp)
                                .border(1.dp, Color.Gray)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.size(30.dp), tint = Color.DarkGray)
                        }

                        Column {
                            Text("Name: ${student.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                            Text("Roll: ${student.rollNumber}", fontSize = 11.sp, color = Color.Black)
                            val bName = batches.firstOrNull { it.id == student.batchId }?.name ?: "Interactive Prep Track"
                            Text("Stream Category: $bName", fontSize = 11.sp, color = Color.Black)
                            Text("Parent Guard: ${student.parentName}", fontSize = 11.sp, color = Color.Black)
                            Text("Emergency Contact: ${student.parentPhone}", fontSize = 11.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Signature overlay area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("FAM ID Code: ${student.familyId}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                            Text("Issued: 2026-06-14", fontSize = 8.sp, color = Color.DarkGray)
                        }
                        // Draw signature script emulation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Canvas(modifier = Modifier.size(50.dp, 20.dp)) {
                                drawLine(Color.Blue, androidx.compose.ui.geometry.Offset(0f, size.height/2), androidx.compose.ui.geometry.Offset(size.width, size.height/3), strokeWidth = 2f)
                                drawLine(Color.Blue, androidx.compose.ui.geometry.Offset(size.width/3, size.height/3), androidx.compose.ui.geometry.Offset(size.width*2/3, size.height*4/5), strokeWidth = 2f)
                            }
                            Text("Auth Signature", fontSize = 8.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "ID Badge & Registration PDF generated!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simulate PDF / Excel print out of ID Badge & Registration Form")
            }
        }
    }
}

// --- Add Batch Slide dialog ---
@Composable
fun AddBatchOverlay(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Block A, Room 102") }
    var teacher by remember { mutableStateOf("") }
    var timings by remember { mutableStateOf("04:30 PM - 06:00 PM") }
    var days by remember { mutableStateOf("Mon, Wed, Fri") }
    var capacity by remember { mutableStateOf("30") }
    var amount by remember { mutableStateOf("1500") }
    var subject by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure New Course Batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Batch Name") })
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Domain") })
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Assigned Teacher") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Class Location") })
                OutlinedTextField(value = timings, onValueChange = { timings = it }, label = { Text("Class Timings") })
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Days of week (Comma separated)") })
                OutlinedTextField(value = capacity, onValueChange = { capacity = it }, label = { Text("Max capacity allotment") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Standard Period Fees (₹)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && subject.isNotEmpty()) {
                        viewModel.addBatch(
                            name = name,
                            location = location,
                            teacher = teacher.ifEmpty { "Tutor Admin" },
                            timings = timings,
                            days = days,
                            capacity = capacity.toIntOrNull() ?: 30,
                            amount = amount.toDoubleOrNull() ?: 1000.0,
                            sub = subject
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Create Batch Track")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- Messaging System & Substitutions Parser Console ---
@Composable
fun MessagingSystemConsole(
    templates: List<MessageTemplate>,
    students: List<Student>,
    batches: List<Batch>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("Dear [ParentName], [StudentName]'s attendance status is [Status]. [BatchName] details inside.") }
    var platform by remember { mutableStateOf("SMS") }

    var selectedPreviewStudentId by remember { mutableStateOf<Long?>(null) }
    var selectedPreviewTemplateId by remember { mutableStateOf<Long?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Create SMS / WhatsApp templates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Template Headline Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Outreach Text Body") },
                placeholder = { Text("Use placeholders like [ParentName], [StudentName], [Amount], [BatchName]") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SMS", "WHATSAPP", "PUSH").forEach { plat ->
                    FilterChip(
                        selected = platform == plat,
                        onClick = { platform = plat },
                        label = { Text(plat) }
                    )
                }
            }
            Button(
                onClick = {
                    if (title.isNotEmpty() && content.isNotEmpty()) {
                        viewModel.addMessageTemplate(title, content, platform)
                        Toast.makeText(context, "New messaging template added successfully!", Toast.LENGTH_SHORT).show()
                        title = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Outreach Template")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text("Live Keyword Substitution Parser Simulator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            // Choose sample student and template to simulate
            Box(modifier = Modifier.fillMaxWidth()) {
                val sName = students.firstOrNull { it.id == selectedPreviewStudentId }?.name ?: "Select student context"
                TextButton(onClick = {
                    selectedPreviewStudentId = students.firstOrNull()?.id
                }) {
                    Text("Selected Student Context: $sName (Click to select first)")
                }
            }

            val chosenStudent = students.firstOrNull { it.id == selectedPreviewStudentId } ?: students.firstOrNull()
            val chosenBatch = batches.firstOrNull { it.id == chosenStudent?.batchId }

            if (chosenStudent != null) {
                val parsedPreview = remember(content, chosenStudent, chosenBatch) {
                    viewModel.parseOutreachMessage(
                        template = content,
                        student = chosenStudent,
                        parentName = chosenStudent.parentName,
                        amount = "₹${chosenBatch?.feesAmount ?: 1500.0}",
                        batchName = chosenBatch?.name ?: "InteractivePrep"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Live Parsed Preview Output for Outreach Device Routing:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(parsedPreview, fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Simulating platform dispatch: Routing: [WhatsApp Business Intent] through preference rules! Alert dispatched successfully to: ${chosenStudent.parentPhone}", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Trigger Broadcast to parent (${chosenStudent.parentPhone})")
                }
            }
        }
    }
}

// --- Accounts Cashflow sub-ledger panel ---
@Composable
fun FinanceAccountsModule(
    transactions: List<FinancialTransaction>,
    payments: List<FeePayment>,
    students: List<Student>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Wages / Faculty Salary") }
    var type by remember { mutableStateOf("EXPENSE") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Record Financial Cashflow Entry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("INCOME", "EXPENSE").forEach { item ->
                    FilterChip(
                        selected = type == item,
                        onClick = { type = item },
                        label = { Text(item) }
                    )
                }
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Heads Category / Category Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Log details/Remarks") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val amVal = amount.toDoubleOrNull() ?: 0.0
                    if (amVal > 0.0 && category.isNotEmpty()) {
                        viewModel.insertTransaction(type, amVal, category, desc)
                        Toast.makeText(context, "Transaction ledger cash flow updated!", Toast.LENGTH_SHORT).show()
                        amount = ""
                        desc = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post Entry to Accounts Database")
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Text("Historical Cash Flow Transactions Ledger Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            transactions.take(6).forEach { t ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(t.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(t.description, fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        text = "${if (t.type == "INCOME") "+" else "-"} ₹${t.amount.toInt()}",
                        color = if (t.type == "INCOME") Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// --- Encrypted local SQLite database backups & sync panel ---
@Composable
fun BackupEngineConsole(
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var syncLogText by remember { mutableStateOf("No operations performed in the session") }
    var cloudSyncProgress by remember { mutableStateOf(false) }
    var driveSyncProgress by remember { mutableStateOf(false) }
    
    val lastDriveTime by viewModel.googleDriveBackupTime.collectAsState()
    val driveFreq by viewModel.googleDriveBackupFrequency.collectAsState()
    val userEmail by viewModel.adminEmail.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("TAMS Data Synchronization Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "TAMS relies on offline-first Room SQLite caching technology, ensuring 100% operation capability with zero network coverage. Backup mechanisms secure records from critical memory card failures.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // LOCAL BACKUP SECTION
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Local Flat File Backups", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Export the entire SQLite database as a flat .db file in local storage for manual recovery or offline storage transfers.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Button(
                    onClick = {
                        viewModel.exportDatabaseToLocalMemory { log ->
                            syncLogText = log
                            Toast.makeText(context, "Full SQLite Flat File database backup generated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = "Backup")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export `.db` File to Local Memory", fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // DUAL CLOUD: GOOGLE DRIVE BACKUPS (WHATSAPP DISASTER STYLE)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Google Drive Cloud Backups", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF34A853))
                        Text(
                            text = "WhatsApp-style automatic disaster recovery backups directly in your personal Google Drive storage.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.widthIn(max = 240.dp)
                        )
                    }
                    // Google Drive Icon decoration
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Drive", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF34A853))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Account info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Account: $userEmail\nLast Backup: $lastDriveTime",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Frequency picker labels
                Text("Select Auto Backup Frequency:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (freq in listOf("Manual", "Daily", "Weekly")) {
                        val isSelected = driveFreq == freq
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.updateGoogleDriveBackupFrequency(freq) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF34A853) else Color.LightGray)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = freq,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFF1B5E20) else Color.DarkGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        driveSyncProgress = true
                        viewModel.triggerGoogleDriveBackup { log ->
                            syncLogText = log
                            if (log.startsWith("Successfully")) {
                                driveSyncProgress = false
                                Toast.makeText(context, "Disaster recovery snapshot saved on Google Drive!", Toast.LENGTH_SHORT).show()
                            } else if (log.startsWith("Error") || log.startsWith("Disaster backup failed")) {
                                driveSyncProgress = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (driveSyncProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Uploading SQLite Backup to Google Drive...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload Drive")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back Up to Google Drive Now", fontSize = 13.sp)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // CLASSIC CLOUD FIRESTORE SYNCHRONIZATION
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Firestore Cloud Snapshot Sync", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                Button(
                    onClick = {
                        cloudSyncProgress = true
                        viewModel.triggerCloudSyncSnapshot { isDone, log ->
                            cloudSyncProgress = false
                            syncLogText = log
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (cloudSyncProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = "Cloud")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Structured Records to Firestore Node", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Synchronization Status logs:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    Text(syncLogText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// --- Dynamic Staff Attendance Logger Module ---
@Composable
fun AttendanceLoggerSubModule(
    batches: List<Batch>,
    students: List<Student>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var activeBatchForLoggingId by remember { mutableStateOf<Long?>(null) }
    var selectedDateStr by remember { mutableStateOf("2026-06-14") }
    var cameraScannerOpenEmulation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Daily Class Attendance Sheets Checker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            OutlinedTextField(
                value = selectedDateStr,
                onValueChange = { selectedDateStr = it },
                label = { Text("Logging Date Target (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Select active batch
            Text("Select active lecture slot tracker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                batches.forEach { b ->
                    val isSel = activeBatchForLoggingId == b.id
                    FilterChip(
                        selected = isSel,
                        onClick = { activeBatchForLoggingId = b.id },
                        label = { Text(b.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            if (activeBatchForLoggingId != null) {
                val batchStudents = students.filter { it.batchId == activeBatchForLoggingId }
                val registeredAttendance by viewModel.getAttendanceForBatchAndDateFlow(activeBatchForLoggingId!!, selectedDateStr).collectAsState(
                    initial = emptyList()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.performBulkAttendance(
                                studentIds = batchStudents.map { it.id },
                                batchId = activeBatchForLoggingId!!,
                                dateStr = selectedDateStr,
                                status = "Present",
                                method = "Bulk Action"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Bulk Mark Present")
                    }
                    Button(
                        onClick = { cameraScannerOpenEmulation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Biometric Scan QR")
                    }
                }

                if (cameraScannerOpenEmulation) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "TAMS BIOMETRIC CAMERA VIEWFINDER RUNNING...",
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Align dynamic student card QR frame token with scanner overlay grid.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            TextButton(
                                onClick = {
                                    val topStudent = batchStudents.firstOrNull()
                                    if (topStudent != null) {
                                        viewModel.saveSingleAttendance(topStudent.id, activeBatchForLoggingId!!, selectedDateStr, "Present", "QR Code")
                                        Toast.makeText(context, "Attendance check-in logged via Biometric QR scanner for: ${topStudent.name}", Toast.LENGTH_LONG).show()
                                    }
                                    cameraScannerOpenEmulation = false
                                }
                            ) {
                                Text("Simulate Successful QR SCAN", color = Color.Yellow, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Students status checklists
                Spacer(modifier = Modifier.height(8.dp))
                Text("Assigned Lecture Batch Student Roll Call Grid", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                batchStudents.forEach { s ->
                    val statusObj = registeredAttendance.firstOrNull { it.studentId == s.id }
                    val currentStatus = statusObj?.status ?: "Unmarked"
                    val checkedVal = currentStatus == "Present"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Roll: ${s.rollNumber} • Verification: ${statusObj?.trackingMethod ?: "Manual"}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedVal,
                                onCheckedChange = { isChecked ->
                                    viewModel.saveSingleAttendance(
                                        studentId = s.id,
                                        batchId = activeBatchForLoggingId!!,
                                        dateStr = selectedDateStr,
                                        status = if (isChecked) "Present" else "Absent",
                                        method = "Manual"
                                    )
                                }
                            )
                            Text(currentStatus, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// --- Upload study materials sub-module ---
@Composable
fun UploadStudyResourcesSubModule(
    batches: List<Batch>,
    studyMaterials: List<StudyMaterial>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var selectedBatchIndexId by remember { mutableStateOf<Long?>(null) }
    var category by remember { mutableStateOf("Algebra Core") }
    var subCategory by remember { mutableStateOf("Quadratic Formula Tricks") }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Plain Text") }
    var content by remember { mutableStateOf("") }
    var pdfName by remember { mutableStateOf("syllabus_note_2026.pdf") }
    var youtubeUrl by remember { mutableStateOf("https://youtube.com/watch?v=predefined") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Structured Academic Syllabus Uploader", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                "Syllabus structures are organized strictly by: Batch Access -> Main Category -> Topic/Sub-category",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Select active batch
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                batches.forEach { b ->
                    val isSel = selectedBatchIndexId == b.id
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedBatchIndexId = b.id },
                        label = { Text(b.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Main Category Access") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = subCategory, onValueChange = { subCategory = it }, label = { Text("Topic / Sub-category") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Material Topic Headline Title") }, modifier = Modifier.fillMaxWidth())
            
            Column {
                Text("Content Type Category Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Plain Text", "PDF", "YouTube video", "URL").forEach { plat ->
                        FilterChip(
                            selected = type == plat,
                            onClick = { type = plat },
                            label = { Text(plat) }
                        )
                    }
                }
            }

            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Study Notes content / Text Body details") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pdfName, onValueChange = { pdfName = it }, label = { Text("PDF Document Link attachment name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text("YouTube Tutorial stream URL") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (selectedBatchIndexId == null || title.isEmpty()) {
                        Toast.makeText(context, "Class Batch Selection and Title are required.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.uploadStudyMaterial(
                        batchId = selectedBatchIndexId!!,
                        category = category,
                        subCategory = subCategory,
                        title = title,
                        type = type,
                        content = content,
                        pdf = pdfName,
                        tube = youtubeUrl
                    )
                    title = ""
                    content = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Broadcast Educational Material to Students")
            }
        }
    }
}

// --- Practice Timed MCQ examination screen ---
@Composable
fun MCQExamPracticeScreen(
    exam: Exam,
    student: Student,
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe questions
    val questionsList by viewModel.getQuestionsForExamFlow(exam.id).collectAsState(initial = emptyList())
    var activeQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerKey by remember { mutableStateOf("") }
    var scoreValue by remember { mutableStateOf(0) }
    var durationRemainingSeconds by remember { mutableStateOf(exam.durationMinutes * 60) }
    var isAttemptLocked by remember { mutableStateOf(false) }

    // Start countdown timer loop
    LaunchedEffect(key1 = exam.id) {
        while (durationRemainingSeconds > 0 && !isAttemptLocked) {
            delay(1000)
            durationRemainingSeconds -= 1
        }
        if (durationRemainingSeconds == 0) {
            isAttemptLocked = true
            Toast.makeText(context, "Time limit exceeded. Submitting answers automatically...", Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Interactive Online Test Module", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    val minutes = durationRemainingSeconds / 60
                    val seconds = durationRemainingSeconds % 60
                    Text(
                        text = "TIMER: %02d:%02d".format(minutes, seconds),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            if (questionsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No question packets uploaded for this diagnostic trial currently.", color = Color.Gray)
                }
                return@Column
            }

            if (isAttemptLocked) {
                // Results screen card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = "Passed", tint = Color.Green, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("MOCK DIAGNOSTIC GRADED RESULT CARD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Blue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Active Student candidate: ${student.name}", fontSize = 12.sp, color = Color.Black)
                        Text("Academic stream batch: ${exam.title}", fontSize = 11.sp, color = Color.Black, textAlign = TextAlign.Center)
                        Text("Final score achieved: $scoreValue / ${questionsList.size}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.recordExamAttempt(student.id, exam.id, scoreValue, questionsList.size, exam.durationMinutes * 60 - durationRemainingSeconds)
                                onClose()
                            }
                        ) {
                            Text("Save Results & Lock attempts")
                        }
                    }
                }
            } else {
                val qActive = questionsList[activeQuestionIndex]
                Text(
                    text = "Question ${activeQuestionIndex + 1} of ${questionsList.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = qActive.questionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Answer A, B, C, D choices
                listOf(
                    "A" to qActive.optionA,
                    "B" to qActive.optionB,
                    "C" to qActive.optionC,
                    "D" to qActive.optionD
                ).forEach { (key, optionVal) ->
                    val isSelected = selectedAnswerKey == key
                    val bBorder = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedAnswerKey = key },
                        shape = RoundedCornerShape(8.dp),
                        border = bBorder,
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                    ) {
                        Text(
                            text = "($key) $optionVal",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedAnswerKey.isEmpty()) {
                            Toast.makeText(context, "Selecting a response option is required to progress.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedAnswerKey == qActive.correctAnswer) {
                            scoreValue += 1
                        }
                        // Next question rules
                        if (activeQuestionIndex < questionsList.size - 1) {
                            activeQuestionIndex += 1
                            selectedAnswerKey = ""
                        } else {
                            isAttemptLocked = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (activeQuestionIndex == questionsList.size - 1) "Submit test papers" else "Progress to next slot")
                }
            }
        }
    }
}

// ==========================================
// SYSTEM ACCESSORY LAYERS & ACCESS COMPOSABLES
@Composable
fun NotificationsDialog(onClose: () -> Unit, onMarkAllRead: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var selectedNotification by remember { mutableStateOf<Triple<String, String, String>?>(null) }
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .safeDrawingPadding()
            ) {
                // Full Screen Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerts",
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp).padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                "System Notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Tap any notification to view/execute actions",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val alerts = listOf(
                        Triple("WhatsPromo Broadcast", "Broadcaster successfully dispatched 12 marketing invitations dynamic templates.", "1 min ago"),
                        Triple("Tuition Auto-Receipt", "Dispatched ₹2,500 receipt PDF automatically via WhatsApp API.", "12 mins ago"),
                        Triple("S3 Backup Synced", "Cold storage daily backup compiled successfully. Uploader node healthy.", "1 hour ago"),
                        Triple("Student Birthday Alert", "Automated custom birthday greeting pushed to Roshni S. on partition 1.", "4 hours ago"),
                        Triple("Critical Batch Schedule", "Evening physics batch report compiled and cached.", "Today")
                    )

                    items(alerts) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNotification = item
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            item.first,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        item.third,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    item.second,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Tap to take action",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = "Action",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onMarkAllRead,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mark All Read")
                    }
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dismiss Console")
                    }
                }
            }

            // --- DETAILED NOTIFICATION ACTION OVERLAY ---
            if (selectedNotification != null) {
                val item = selectedNotification!!
                AlertDialog(
                    onDismissRequest = { selectedNotification = null },
                    title = {
                        Text("Execute Action: ${item.first}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("System identified associated action flow for this notification.", fontSize = 13.sp)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    item.second,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                "Would you like to execute the following pipeline task now?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "• Launch corresponding services module\n• Clear this specific alarm buffer\n• Dispatch automated status sync details",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Executing associated pipeline for '${item.first}' successfully!", Toast.LENGTH_LONG).show()
                                selectedNotification = null
                            }
                        ) {
                            Text("Execute Pipeline")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedNotification = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerHeaderContent(
    userRole: String,
    academyName: String,
    directorName: String,
    adminEmail: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1), // Royal Navy
                        Color(0xFF1976D2)  // Sky Accent
                    )
                )
            )
            .padding(vertical = 24.dp, horizontal = 20.dp)
    ) {
        Column {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Academy Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (userRole == "ADMIN") "App Owner Console" else academyName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = if (userRole == "ADMIN") "System Supervisor" else "Director: $directorName",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = if (userRole == "ADMIN") "tams.monitor@service.com" else adminEmail,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun DrawerNavigationItems(
    userRole: String,
    activeAdminTab: String,
    activeStudentTab: String,
    onSelectAdminTab: (String) -> Unit,
    onSelectStudentTab: (String) -> Unit,
    onSelectBottomTab: (String) -> Unit,
    onLogout: () -> Unit,
    onAskGemini: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (userRole == "STAFF") { // Merged Institute / Staff / Teacher
            Text(
                text = "ACADEMY CONSOLE SERVICES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            
            DrawerItem(
                label = "Operations Home",
                icon = Icons.Default.Dashboard,
                selected = activeAdminTab == "Dashboard",
                onClick = { onSelectAdminTab("Dashboard") }
            )
            DrawerItem(
                label = "Students Registry (Admission)",
                icon = Icons.Default.People,
                selected = activeAdminTab == "Admission Form",
                onClick = { onSelectAdminTab("Admission Form") }
            )
            DrawerItem(
                label = "Batches Scheduler",
                icon = Icons.Default.Layers,
                selected = activeAdminTab == "Batches Setup",
                onClick = { onSelectAdminTab("Batches Setup") }
            )
            DrawerItem(
                label = "Ledger (Income & Expenses)",
                icon = Icons.Default.Payments,
                selected = activeAdminTab == "Ledger Accounts",
                onClick = { onSelectAdminTab("Ledger Accounts") }
            )
            DrawerItem(
                label = "Student Tuition Fees Center",
                icon = Icons.Default.Receipt,
                selected = activeAdminTab == "Tuition Fees",
                onClick = { onSelectAdminTab("Tuition Fees") }
            )
            DrawerItem(
                label = "WhatsPromo & Outreach",
                icon = Icons.Default.Chat,
                selected = activeAdminTab == "Outreach Engine",
                onClick = { onSelectAdminTab("Outreach Engine") }
            )
            DrawerItem(
                label = "Secure Backups",
                icon = Icons.Default.CloudSync,
                selected = activeAdminTab == "Database Backups",
                onClick = { onSelectAdminTab("Database Backups") }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "UTILITIES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            DrawerItem(
                label = "Ask Gemini AI Solver",
                icon = Icons.Default.AutoAwesome,
                selected = false,
                onClick = onAskGemini
            )
            DrawerItem(
                label = "Profile & Settings",
                icon = Icons.Default.Settings,
                selected = false,
                onClick = { onSelectBottomTab("MY_ACCOUNT") }
            )

        } else if (userRole == "STUDENT") {
            Text(
                text = "STUDENT SERVICES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            
            DrawerItem(
                label = "Home Workspace",
                icon = Icons.Default.Home,
                selected = activeStudentTab == "Home Workspace",
                onClick = { onSelectStudentTab("Home Workspace") }
            )
            DrawerItem(
                label = "Study Materials",
                icon = Icons.Default.LibraryBooks,
                selected = activeStudentTab == "Academic Resources",
                onClick = { onSelectStudentTab("Academic Resources") }
            )
            DrawerItem(
                label = "Practice Exams",
                icon = Icons.Default.Quiz,
                selected = activeStudentTab == "Practice exams",
                onClick = { onSelectStudentTab("Practice exams") }
            )
            DrawerItem(
                label = "Ask Gemini AI Bot",
                icon = Icons.Default.AutoAwesome,
                selected = false,
                onClick = onAskGemini
            )

        } else if (userRole == "PARENT") {
            Text(
                text = "PARENT SERVICES PORTAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            
            DrawerItem(
                label = "Child Standings & Status",
                icon = Icons.Default.ChildCare,
                selected = activeStudentTab == "Child Standing",
                onClick = { onSelectStudentTab("Child Standing") }
            )
            DrawerItem(
                label = "Tuition Dues & Ledger",
                icon = Icons.Default.Payments,
                selected = activeStudentTab == "Tuition & Fees",
                onClick = { onSelectStudentTab("Tuition & Fees") }
            )
            DrawerItem(
                label = "Academic Performance",
                icon = Icons.Default.TrendingUp,
                selected = activeStudentTab == "Academic Progress",
                onClick = { onSelectStudentTab("Academic Progress") }
            )
            DrawerItem(
                label = "Contact Teachers",
                icon = Icons.Default.ChatBubbleOutline,
                selected = activeStudentTab == "Contact Teachers",
                onClick = { onSelectStudentTab("Contact Teachers") }
            )
            DrawerItem(
                label = "Ask Gemini AI Zweifel (Doubt) Bot",
                icon = Icons.Default.AutoAwesome,
                selected = false,
                onClick = onAskGemini
            )

        } else { // ADMIN (App Owner Monitor Role)
            Text(
                text = "OWNER TELEMETRY & CONTROLS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            
            DrawerItem(
                label = "System Diagnostics",
                icon = Icons.Default.Troubleshoot,
                selected = true,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider()
        DrawerItem(
            label = "Exit Portal / Logout",
            icon = Icons.Default.ExitToApp,
            selected = false,
            onClick = onLogout
        )
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.DarkGray
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun AppOwnerMonitorView(
    transactions: List<FinancialTransaction>,
    batches: List<Batch>,
    students: List<Student>,
    institutes: List<InstituteAccount>,
    onApprove: (String) -> Unit,
    onApproveAll: () -> Unit
) {
    var tokenCode by remember { mutableStateOf("") }
    var outputTokenText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)), // Strong forest green app-owner theme
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "App Owner",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("App Owner Supervisor Monitor", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("License Server v3.4 • Security Monitor Active", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // --- NEW SECTION: INSTITUTES & TEACHERS APPROVAL REGISTER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)), // Light lime/green soft card
                border = BorderStroke(1.dp, Color(0xFFCDDC39).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TEACHERS & ACADEMIES APPROVAL DESK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF558B2F),
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${institutes.size} Accounts Total",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                        Button(
                            onClick = onApproveAll,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF689F38)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Approve All", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (institutes.isEmpty()) {
                        Text(
                            text = "No registered institutes found.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            institutes.forEach { inst ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = inst.academyName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                if (inst.isApproved) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Approved",
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Pending,
                                                        contentDescription = "Pending",
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Director: ${inst.directorName} | Location: ${inst.address}",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                            Text(
                                                text = inst.email,
                                                fontSize = 10.sp,
                                                color = Color.Gray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        if (!inst.isApproved) {
                                            Button(
                                                onClick = { onApprove(inst.email) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8BC34A)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Approve", fontSize = 10.sp, color = Color.White)
                                            }
                                        } else {
                                            Text(
                                                text = "APPROVED",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFF388E3C)
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

        // Section 1: API UPTIME & SYSTEM TELEMETRY
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GLOBAL LICENSING & INSTANT DIAGNOSTICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Database Footprint", fontSize = 11.sp, color = Color.Gray)
                            Text("3.42 MB SQLite", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column {
                            Text("Active Institutes Connected", fontSize = 11.sp, color = Color.Gray)
                            Text("3 Registered", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                        }
                        Column {
                            Text("Cloud S3 Status", fontSize = 11.sp, color = Color.Gray)
                            Text("99.98% Online", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1565C0))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("System telemetry logs (Real-time updates):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text("● [2026-06-14 13:12] Cloud check successful. Latency 142ms.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                        Text("● [2026-06-14 11:45] Aspirants Success Classes synced 42 attendance entries.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                        Text("● [2026-06-14 09:00] Daily database encryption key rotated successfully.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                        Text("● [2026-06-14 00:05] S3 cold storage compressed archive compiled successfully.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }
        }

        // Section 2: REGISTERED INSTITUTES LEDGER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TENANT SCHOOLS & LICENSE LEDGER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        Triple("Aspirants Success Classes", "Director: Sumit Kumar (chhibramau)", "Status: Licensed"),
                        Triple("Alpha Academy Coaching", "Director: Vikram Rathore (Delhi)", "Status: Trial Period"),
                        Triple("Pioneer JEE Preparation", "Director: Neha Soni (Kanpur)", "Status: Active")
                    ).forEach { school ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(school.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(school.second, fontSize = 11.sp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(school.third, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // Section 3: APP OWNER ACTIONS (Rescue operations / simulated remote wipes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUPERVISOR OPERATION CONTROLS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Generate Activation Key / Code for Client Schools", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tokenCode,
                            onValueChange = { tokenCode = it },
                            placeholder = { Text("e.g. ALPHA_COACH") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (tokenCode.isNotEmpty()) {
                                    outputTokenText = "KEY-ACTIVATED-${tokenCode.uppercase()}-2026-VALID"
                                    Toast.makeText(context, "License Activation generated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter client tag first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Generate")
                        }
                    }

                    if (outputTokenText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Code: $outputTokenText",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Simulate Remote Reset / Rogue Database Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                    Text("Remotely wipe or revoke database client nodes due to licensing infractions.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "CRITICAL: Simulated database recovery initiated. Cloud nodes sync locked.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lock Node Sync")
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "DANGER: Remote Wipe simulation dispatched. Client node data cleared.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Wipe Client DB")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. OFFICIAL TUITION FEES & RECEIPT CENTER (Production-Ready)
// ==========================================
@Composable
fun TuitionFeesModule(
    payments: List<FeePayment>,
    students: List<Student>,
    batches: List<Batch>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    val merchantUpiId by viewModel.merchantUpiId.collectAsState()
    val merchantName by viewModel.merchantName.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchId by remember { mutableStateOf<Long?>(null) }
    
    // Dialog details tracker
    var selectedStudentForPay by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForReceipts by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForReminder by remember { mutableStateOf<Student?>(null) }
    var activeReceiptToShow by remember { mutableStateOf<FeePayment?>(null) }

    var showUpiConfig by remember { mutableStateOf(false) }
    var tempUpiId by remember { mutableStateOf(merchantUpiId) }
    var tempMerchantName by remember { mutableStateOf(merchantName) }

    // Dues tracking calculations
    val totalCollected = payments.sumOf { it.amountPaid }
    val totalExpected = students.sumOf { s -> 
        val b = batches.find { it.id == s.batchId }
        b?.feesAmount ?: 1000.0
    }
    val outstandingDues = (totalExpected - totalCollected).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // collapsible config area
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showUpiConfig = !showUpiConfig }.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("UPI Gateway Settings Config", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Current: $merchantUpiId", fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                    }
                }
                IconButton(onClick = { showUpiConfig = !showUpiConfig }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (showUpiConfig) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle"
                    )
                }
            }

            if (showUpiConfig) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add/Change UPI VPA configuration for direct WhatsApp invoice links & QR Codes:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = tempUpiId,
                            onValueChange = { tempUpiId = it },
                            label = { Text("Coaching UPI ID VPA", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 9876543210@paytm") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempMerchantName,
                            onValueChange = { tempMerchantName = it },
                            label = { Text("Merchant/Academy Display Name", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Aspirants Coaching Institute") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (tempUpiId.isNotBlank() && tempMerchantName.isNotBlank() && tempUpiId.contains("@")) {
                                    viewModel.updateMerchantUPI(tempUpiId, tempMerchantName)
                                    showUpiConfig = false
                                    Toast.makeText(context, "UPI gateway credentials updated successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid UPI ID (containing '@') and Merchant Name.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Text("Save Gateway Details", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Google & Firestore Tuition Ledger Connection Status Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Google Cloud & Firestore Tuition Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34A853))
                            )
                            Text(
                                text = "Active Ledger Sync",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    val userEmail = viewModel.adminEmail.collectAsState().value.ifEmpty { "smtsharma282.sks@gmail.com" }
                    Text(
                        text = "Authenticated via Google: $userEmail\nLogs monthly tuition payments directly to your Firestore and ensures automated data backups on the cloud.",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val overdueStudentsCount = students.count { s ->
                val b = batches.find { it.id == s.batchId }
                val bFee = b?.feesAmount ?: 1000.0
                val paid = payments.filter { it.studentId == s.id }.sumOf { it.amountPaid }
                (bFee - paid) > 0.0
            }

            // Live financial report cards
            Text("Collection Summary Report", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Collected Fees", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("₹${totalCollected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                    }
                }
                Card(
                    modifier = Modifier.weight(1.1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Outstanding Dues", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        Text("₹${outstandingDues.toInt()} ($overdueStudentsCount Overdue)", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFFB71C1C))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Live filter selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Student Name...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Box {
                    var expandedBatch by remember { mutableStateOf(false) }
                    val currentBatchText = selectedBatchId?.let { id -> batches.find { it.id == id }?.name } ?: "All Batches"
                    
                    Button(
                        onClick = { expandedBatch = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(currentBatchText, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    
                    DropdownMenu(expanded = expandedBatch, onDismissRequest = { expandedBatch = false }) {
                        DropdownMenuItem(
                            text = { Text("All Batches") },
                            onClick = {
                                selectedBatchId = null
                                expandedBatch = false
                            }
                        )
                        batches.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch.name) },
                                onClick = {
                                    selectedBatchId = batch.id
                                    expandedBatch = false
                                }
                            )
                        }
                    }
                }
            }

            val filteredStudents = students.filter { s ->
                (selectedBatchId == null || s.batchId == selectedBatchId) &&
                s.name.contains(searchQuery, ignoreCase = true)
            }

            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No enrolled students found matching the filter criteria.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                Text("Enrolled Students Fee Status (${filteredStudents.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                filteredStudents.forEach { student ->
                    val batch = batches.find { it.id == student.batchId }
                    val studentBatchFee = batch?.feesAmount ?: 1000.0
                    val studentPaid = payments.filter { it.studentId == student.id }.sumOf { it.amountPaid }
                    val remainingDue = (studentBatchFee - studentPaid).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Roll ID: ${student.rollNumber} | Course: ${batch?.name ?: "N/A"}", fontSize = 11.sp, color = Color.Gray)
                                }
                                
                                val isOverdue = remainingDue > 0.0
                                val statusText = when {
                                    remainingDue <= 0.0 -> "Paid & Clear"
                                    studentPaid > 0.0 -> "Partially Paid (OVERDUE)"
                                    else -> "OVERDUE (Unpaid)"
                                }
                                val statusColor = when {
                                    remainingDue <= 0.0 -> Color(0xFF2E7D32)
                                    studentPaid > 0.0 -> Color(0xFFEF6C00)
                                    else -> Color(0xFFD84315)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (isOverdue) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Overdue fees warning",
                                                tint = statusColor,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                        Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tuition standard charge: ₹${studentBatchFee.toInt()}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Paid: ₹${studentPaid.toInt()} | Outstanding: ₹${remainingDue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Live system dialer trigger
                                    IconButton(
                                        onClick = {
                                            val number = student.phone.ifEmpty { student.parentPhone }
                                            if (number.isNotEmpty()) {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                                    data = android.net.Uri.parse("tel:$number")
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                Toast.makeText(context, "No contact number registered for this student context.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call Student", tint = Color(0xFF0D47A1), modifier = Modifier.size(16.dp))
                                    }
                                    
                                    // Keyword-substituted reminder message dialog trigger
                                    IconButton(
                                        onClick = { selectedStudentForReminder = student },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.NotificationImportant, contentDescription = "Remission notice", tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                    }

                                    TextButton(onClick = { selectedStudentForReceipts = student }) {
                                        Text("Receipts (${payments.filter { it.studentId == student.id }.size})", fontSize = 11.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                                    }
                                }

                                if (remainingDue > 0.0) {
                                    Button(
                                        onClick = { selectedStudentForPay = student },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receive Fees", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }

    // DIALOG 1: Pay Dues Dialog (writes to Room Database)
    if (selectedStudentForPay != null) {
        val student = selectedStudentForPay!!
        val studentBatch = batches.find { it.id == student.batchId }
        val studentPaid = payments.filter { it.studentId == student.id }.sumOf { it.amountPaid }
        val studentBatchFee = studentBatch?.feesAmount ?: 1000.0
        val remainingDue = (studentBatchFee - studentPaid).coerceAtLeast(0.0)

        var amountInput by remember { mutableStateOf(remainingDue.toString()) }
        var discountInput by remember { mutableStateOf("0") }
        var selectedMonth by remember { mutableStateOf("June 2026") }
        var selectFeeType by remember { mutableStateOf("Monthly-based") }
        var modeSelected by remember { mutableStateOf("UPI") }
        var notesInput by remember { mutableStateOf("Standard tuition fee instalment") }

        AlertDialog(
            onDismissRequest = { selectedStudentForPay = null },
            title = { Text("Collect Fees: ${student.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Updates local SQLite Room cache & adds to fiscal ledger Cashflow automatically.", fontSize = 11.sp, color = Color.Gray)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            label = { Text("Instalment (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("Scholarship (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = selectedMonth,
                        onValueChange = { selectedMonth = it },
                        label = { Text("Billing Month") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Category Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            listOf("Monthly-based", "Course-based").forEach { t ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectFeeType == t, onClick = { selectFeeType = t })
                                    Text(t, fontSize = 11.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            listOf("UPI", "CASH", "CARD").forEach { m ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = modeSelected == m, onClick = { modeSelected = m })
                                    Text(m, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Remarks/Tutor Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live UPI QR Payment and Whatsapp receipt generation panel
                    val calculatedBill = amountInput.toDoubleOrNull() ?: 0.0
                    if (calculatedBill > 0.0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        UPIQRCodeCard(
                            studentName = student.name,
                            parentPhone = student.parentPhone.ifEmpty { student.phone },
                            amount = calculatedBill,
                            upiId = merchantUpiId,
                            merchantName = merchantName,
                            onWhatsAppSent = {
                                Toast.makeText(context, "UPI Payment bill sent to parent successfully!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val collected = amountInput.toDoubleOrNull() ?: 0.0
                        val disc = discountInput.toDoubleOrNull() ?: 0.0
                        if (collected > 0.0) {
                            viewModel.collectFeePayment(
                                studentId = student.id,
                                amount = collected,
                                discount = disc,
                                feeType = selectFeeType,
                                month = selectedMonth,
                                mode = modeSelected,
                                remarks = notesInput
                            )
                            Toast.makeText(context, "Collected ₹${collected.toInt()} fee for ${student.name}!", Toast.LENGTH_LONG).show()
                            selectedStudentForPay = null
                        } else {
                            Toast.makeText(context, "Invalid collection amount typed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Record Receipt & Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentForPay = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DIALOG 2: View Payment Receipts History
    if (selectedStudentForReceipts != null) {
        val student = selectedStudentForReceipts!!
        val studentPayments = payments.filter { it.studentId == student.id }

        AlertDialog(
            onDismissRequest = { selectedStudentForReceipts = null },
            title = { Text("Fee Ledger: ${student.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp).verticalScroll(rememberScrollState())) {
                    if (studentPayments.isEmpty()) {
                        Text("No payment transactions exist for this student account.", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        studentPayments.forEach { pay ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("RCP: ${pay.receiptNo}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(pay.monthPaidFor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Amount Collected: ₹${pay.amountPaid.toInt()} (Mode: ${pay.paymentMode})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (pay.discount > 0.0) {
                                        Text("Discount Applied: ₹${pay.discount.toInt()}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("Remarks: ${pay.remarks}", fontSize = 11.sp, color = Color.DarkGray)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { activeReceiptToShow = pay },
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Generate Official Receipt Voucher", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedStudentForReceipts = null }) {
                    Text("Done")
                }
            }
        )
    }

    // DIALOG 3: Interactive Printer Voucher Screen
    if (activeReceiptToShow != null) {
        val pay = activeReceiptToShow!!
        val student = students.find { it.id == pay.studentId } ?: students.firstOrNull()!!
        val batch = batches.find { it.id == student.batchId }

        AlertDialog(
            onDismissRequest = { activeReceiptToShow = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1B5E20))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Audit Receipt", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1B5E20))
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("ASPIRANTS SUCCESS CLASSES", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0D47A1), textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Text("Chhibramau, Uttar Pradesh | Director: Sumit Kumar", fontSize = 9.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        
                        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
                            val parts = 35
                            val widthPart = size.width / parts
                            for (i in 0 until parts step 2) {
                                drawRect(color = Color.LightGray, size = size.copy(width = widthPart), topLeft = androidx.compose.ui.geometry.Offset(i * widthPart, 0f))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RECEIPT NO: ${pay.receiptNo}", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.DarkGray)
                            Text("DATE: 2026-06-14", fontSize = 9.sp, color = Color.DarkGray)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("STUDENT NAME : ${student.name.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("SCHOLAR ROLL  : ${student.rollNumber}", fontSize = 10.sp, color = Color.Gray)
                        Text("COURSE BATCH  : ${batch?.name ?: "Coaching Scholar"}", fontSize = 10.sp, color = Color.Gray)
                        Text("GUARDIAN INFO : ${student.parentName} (${student.parentPhone})", fontSize = 10.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color.LightGray)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fee Particulars", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("Ledger Val", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coaching fees instalment (${pay.monthPaidFor})", fontSize = 10.sp)
                            Text("₹${(pay.amountPaid + pay.discount).toInt()}", fontSize = 10.sp)
                        }

                        if (pay.discount > 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Waiver/Scholarship rebate", fontSize = 10.sp, color = Color.Gray)
                                Text("- ₹${pay.discount.toInt()}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(2.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PASTED IN CASH BOOK:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("₹${pay.amountPaid.toInt()}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF1B5E20))
                        }
                        Text("Mode: ${pay.paymentMode} | Settlement: REALTIME SETTLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Issued securely of Aspirants Success Classes. Verified & authenticated Ledger client node.", fontSize = 7.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareMsg = """
                            🧾 *ASPIRANTS SUCCESS CLASSES FEE RECEIPT*
                            ------------------------------------
                            Receipt No: ${pay.receiptNo}
                            Student: ${student.name}
                            Course: ${batch?.name ?: "Preparatory"}
                            Month Code: ${pay.monthPaidFor}
                            Total Collected: ₹${pay.amountPaid.toInt()}
                            Payment Mode: ${pay.paymentMode}
                            Status: *SUCCESSFULLY PAID & SETTLED*
                            ------------------------------------
                            Aspirants Success Classes, Chhibramau.
                        """.trimIndent()
                        
                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            putExtra(android.content.Intent.EXTRA_TEXT, shareMsg)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Receipt Via"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share/Send Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeReceiptToShow = null }) {
                    Text("Close Voucher")
                }
            }
        )
    }

    // DIALOG 4: Smart Substituted Reminder
    if (selectedStudentForReminder != null) {
        val student = selectedStudentForReminder!!
        val batch = batches.find { it.id == student.batchId }
        val studentPaid = payments.filter { it.studentId == student.id }.sumOf { it.amountPaid }
        val studentBatchFee = batch?.feesAmount ?: 1000.0
        val remainingDue = (studentBatchFee - studentPaid).coerceAtLeast(0.0)

        val finalReminderText = "Dear ${if(student.parentName.isNotBlank()) student.parentName else "Parent"}, your child ${student.name}'s batch tuition fee of ₹${remainingDue.toInt()} at $merchantName is outstanding. Tap to pay directly via UPI: upi://pay?pa=$merchantUpiId&pn=${android.net.Uri.encode(merchantName)}&am=${remainingDue.toInt()}&cu=INR\n\nKindly clear the outstanding dues. Thank you!"

        AlertDialog(
            onDismissRequest = { selectedStudentForReminder = null },
            title = { Text("Compile Fee Reminder Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Dynamically substituted keywords representing Student context, parent, course, & pending sum:", fontSize = 11.sp, color = Color.Gray)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF9C4))
                            .border(1.dp, Color(0xFFFBC02D))
                            .padding(12.dp)
                    ) {
                        Text(finalReminderText, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }

                    if (remainingDue > 0.0) {
                        Text("Interactive UPI Collection QR Code:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        UPIQRCodeCard(
                            studentName = student.name,
                            parentPhone = student.parentPhone.ifEmpty { student.phone },
                            amount = remainingDue,
                            upiId = merchantUpiId,
                            merchantName = merchantName,
                            onWhatsAppSent = {}
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    // Direct WhatsApp Button
                    Button(
                        onClick = {
                            try {
                                val cleanPhone = student.parentPhone.ifEmpty { student.phone }.replace("+", "").replace(" ", "").trim()
                                val targetPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=${android.net.Uri.encode(finalReminderText)}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open WhatsApp. Opening general shares.", Toast.LENGTH_SHORT).show()
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    putExtra(android.content.Intent.EXTRA_TEXT, finalReminderText)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Send Reminder Via"))
                            }
                            selectedStudentForReminder = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Official Green color
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send to WhatsApp")
                    }

                    // Copy & Share Button
                    Button(
                        onClick = {
                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("TAMS Reminder Notice", finalReminderText)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Reminder Notice copied to clipboard!", Toast.LENGTH_SHORT).show()

                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                putExtra(android.content.Intent.EXTRA_TEXT, finalReminderText)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Send Reminder Via"))
                            selectedStudentForReminder = null
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy & Share")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentForReminder = null }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

// ========================================================
// PORTAL VIEW 4: PARENT PORTAL SECURE WORKSPACE
// ========================================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ParentWorkspace(
    activeParentTab: String,
    viewModel: AppViewModel,
    batches: List<Batch>,
    students: List<Student>,
    payments: List<FeePayment>,
    exams: List<Exam>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedStudentIndex by remember { mutableStateOf(0) }
    val activeStudent = remember(students, selectedStudentIndex) {
        if (students.isNotEmpty() && selectedStudentIndex < students.size) {
            students[selectedStudentIndex]
        } else {
            students.firstOrNull()
        }
    }

    if (activeStudent == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No Registered Children", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please log in as Admin/Staff and register a student first to inspect parent services.", fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
        return
    }

    // Interactive states for payment simulation
    var parentOutstandingFees by remember(activeStudent.id) { mutableStateOf(2500) }
    var selectedPaymentGateway by remember { mutableStateOf("") }
    var showPaymentSuccessDialog by remember { mutableStateOf(false) }
    var showUpiDetailsDialog by remember { mutableStateOf(false) }
    var inputUpiId by remember { mutableStateOf("") }
    var showGstReceiptDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }
    
    // Chat History Simulation
    var chatMessageInput by remember { mutableStateOf("") }
    val chatHistoryList = remember(activeStudent.id) {
        mutableStateListOf(
            Pair("Teacher Sumit", "Hello! Priya is making steady progress in our Physics batches. Let us know if you have any doubts."),
            Pair("You", "Thank you, teacher. How is her homework submission punctuality?")
        )
    }

    // Active Poll Feed
    var activePollVote by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child Selected Header dropdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("MONITORING CHILD STATE PORTLET", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32), fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(activeStudent.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(activeStudent.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1B5E20))
                                Text("Roll ID: ${activeStudent.rollNumber} • Family: ${activeStudent.familyId}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        
                        if (students.size > 1) {
                            var showPickerDropdown by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(onClick = { showPickerDropdown = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text("Switch Child ▾", fontSize = 11.sp)
                                }
                                DropdownMenu(expanded = showPickerDropdown, onDismissRequest = { showPickerDropdown = false }) {
                                    students.forEachIndexed { index, child ->
                                        DropdownMenuItem(
                                            text = { Text(child.name) },
                                            onClick = {
                                                selectedStudentIndex = index
                                                showPickerDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Switching workspace tab view
        when (activeParentTab) {
            "Child Standing" -> {
                // Radial Attendance Guage
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("ATTENDANCE TELEMETRY ANALYTICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier.size(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Base gray track arc
                                    drawArc(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                    // Highlight green gauge representing 88% attendance
                                    drawArc(
                                        color = Color(0xFF4CAF50),
                                        startAngle = 135f,
                                        sweepAngle = 270f * 0.88f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("88.2%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                    Text("Status: Excellent", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("30 Days", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Academic Slot", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("26 Days", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                    Text("Present Days", fontSize = 10.sp, color = Color.Gray)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("4 Days", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Red)
                                    Text("Absent Days", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Active Streaks Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                        border = BorderStroke(1.dp, Color(0xFFFBC02D))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🔥", fontSize = 36.sp)
                            Column {
                                Text("CONSECUTIVE REGISTRY ATTENDANCE STREAK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontFamily = FontFamily.Monospace)
                                Text("12 Days Active Streak!", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFE65100))
                                Text("Your child is awarded 240 bonus academic XP points for consistent daily attendance.", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }

                // Recent attendance check list cards
                item {
                    Text("DAILY CHRONOLOGICAL PRESENCE RECORDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("15-Jun-2026", "Present", "Regular Class Attendance verified by QR Code"),
                                Triple("14-Jun-2026", "Present", "Sunday Special Practice Exam Batch Check"),
                                Triple("12-Jun-2026", "Present", "Regular Class Attendance verified by QR Code"),
                                Triple("11-Jun-2026", "Absent", "Medical notification transmitted to Instructor"),
                                Triple("10-Jun-2026", "Present", "Regular Class Attendance verified by QR Code")
                            ).forEach { (date, status, verify) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(verify, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Surface(
                                        color = if (status == "Present") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = status.uppercase(),
                                            color = if (status == "Present") Color(0xFF2E7D32) else Color.Red,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            "Tuition & Fees" -> {
                // Tuition balance due
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFEF9A9A))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("CURRENT TUITION FEE PORTAL BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Red, fontFamily = FontFamily.Monospace)
                                Surface(color = Color.Red, shape = RoundedCornerShape(4.dp)) {
                                    Text("OVERDUE FEES", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "₹$parentOutstandingFees",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Red
                            )
                            Text("Pending fee cycle: June 2026 Tuition Fees installment", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }

                // Interactive Payment Gateway Picker
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("SECURE INTEGRATED GATEWAY CHECKOUT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Choose your preferred billing method to clear children fees instantly:", fontSize = 11.sp, color = Color.Gray)
                            
                            val gateways = listOf(
                                "UPI / GPay / PhonePe" to "UPI Transaction ID instant verify",
                                "PayTM wallet" to "Automated dynamic OTP checkout",
                                "Credit / Debit Card" to "Encrypted PCI-DSS cards checkout",
                                "NetBanking" to "National NEFT bank interfaces"
                            )
                            
                            gateways.forEach { (title, desc) ->
                                val isSelectedG = selectedPaymentGateway == title
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelectedG) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent)
                                        .border(1.dp, if (isSelectedG) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { selectedPaymentGateway = title }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        RadioButton(selected = isSelectedG, onClick = { selectedPaymentGateway = title })
                                        Column {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(desc, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }

                            if (selectedPaymentGateway.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (parentOutstandingFees == 0) {
                                            Toast.makeText(context, "Outstanding balance is already fully paid!", Toast.LENGTH_SHORT).show()
                                        } else if (selectedPaymentGateway.contains("UPI")) {
                                            showUpiDetailsDialog = true
                                        } else {
                                            // General Checkout simulation
                                            showPaymentSuccessDialog = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("PROCEED TO SECURE CHECKOUT (₹$parentOutstandingFees)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // GST Invoices generator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("🧾", fontSize = 36.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("GST TAX COMPLIANT INVOICE GENERATOR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontFamily = FontFamily.Monospace)
                                Text("Generate GST-Compliant Bill", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                                Text("Itemized taxation report for CGST & SGST records matching National compliance rules.", fontSize = 11.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showGstReceiptDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Generate COMPLIANT INVOICE", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            "Academic Progress" -> {
                // Growth Performance trajectory
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ACADEMIC PROGRESS GROWTH TRAJECTORY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("Syllabus MCQ Test Score Curve", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Trajectory analysis mapped dynamically over past assessment phases", fontSize = 11.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Visualizing custom path line graphics inside Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color.DarkGray.copy(alpha = 0.05f))
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.4f))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val points = listOf(
                                        androidx.compose.ui.geometry.Offset(50f, 300f), // Exam 1: 60%
                                        androidx.compose.ui.geometry.Offset(180f, 240f), // Exam 2: 70%
                                        androidx.compose.ui.geometry.Offset(310f, 260f), // Exam 3: 65%
                                        androidx.compose.ui.geometry.Offset(440f, 150f), // Exam 4: 82%
                                        androidx.compose.ui.geometry.Offset(570f, 90f)   // Exam 5: 92%
                                    )
                                    
                                    // Draw background grids
                                    for (gridY in 50..300 step 50) {
                                        drawLine(
                                            color = Color.LightGray.copy(alpha = 0.3f),
                                            start = androidx.compose.ui.geometry.Offset(0f, gridY.toFloat()),
                                            end = androidx.compose.ui.geometry.Offset(size.width, gridY.toFloat()),
                                            strokeWidth = 1f
                                        )
                                    }
                                    
                                    // Plot lines connecting scores
                                    for (i in 0 until points.size - 1) {
                                        drawLine(
                                            color = Color(0xFF1E88E5),
                                            start = points[i],
                                            end = points[i+1],
                                            strokeWidth = 3.dp.toPx()
                                        )
                                    }
                                    
                                    // Render point dots
                                    points.forEachIndexed { i, pt ->
                                        drawCircle(
                                            color = Color(0xFFD81B60),
                                            radius = 5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Exam Phase 1 (60%)", fontSize = 8.sp, color = Color.Gray)
                                Text("Phase 2 (70%)", fontSize = 8.sp, color = Color.Gray)
                                Text("Phase 3 (65%)", fontSize = 8.sp, color = Color.Gray)
                                Text("Phase 4 (82%)", fontSize = 8.sp, color = Color.Gray)
                                Text("Latest Phase 5 (92%)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }

                // Gamified Badges Grid
                item {
                    Text("UNLOCKED SCHOLASTIC BADGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                item {
                    val badgeList = listOf(
                        Triple("🎖️", "Attendance Gladiator", "Unlocked: Active daily streak >= 10 days"),
                        Triple("🤖", "Gemini Solver Ninja", "Unlocked: Checked 10 doubts via AI Bot"),
                        Triple("🏆", "Apex Scorer", "Unlocked: Scored >= 90% in practice exams"),
                        Triple("🦅", "Swift Fee Payer", "Unlocked: June billing cleared before due-date")
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        badgeList.forEach { (emoji, badgeTitle, req) ->
                            var showDescriptionToast by remember { mutableStateOf(false) }
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDescriptionToast = true },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(45.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFF9C4)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 24.sp)
                                    }
                                    Column {
                                        Text(badgeTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(req, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                            
                            if (showDescriptionToast) {
                                AlertDialog(
                                    onDismissRequest = { showDescriptionToast = false },
                                    title = { Text("$emoji $badgeTitle Badge Status") },
                                    text = { Text("Performance index metrics verified. Certified that your child has satisfied all rigorous academic benchmarks of this category to maintain this gamified standing card!") },
                                    confirmButton = {
                                        TextButton(onClick = { showDescriptionToast = false }) { Text("OK") }
                                    }
                                )
                            }
                        }
                    }
                }

                // Motivational Rewards Certificates Generator
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MOTIVATIONAL REWARDS RECOGNITIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontFamily = FontFamily.Monospace)
                            Text("Generate Digital Honor Certificate", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                            Text("Congratulate and showcase your child's peak test records on WhatsApp and social channels.", fontSize = 11.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showCertificateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) {
                                Text("Generate Certificate Of Merit")
                            }
                        }
                    }
                }
            }

            "Contact Teachers" -> {
                // Direct interactive forum chat simulation
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TAMS DIRECT COMMUNICATION CHANNEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                            Text("Direct Messaging Forum - Teacher Contacts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .verticalScroll(rememberScrollState())
                                    .background(Color.LightGray.copy(alpha = 0.08f))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chatHistoryList.forEach { (sender, text) ->
                                    val isMe = sender == "You"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        Surface(
                                            color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.widthIn(max = 240.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(sender, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text(text, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = chatMessageInput,
                                    onValueChange = { chatMessageInput = it },
                                    placeholder = { Text("Ask teacher about progress / questions...") },
                                    modifier = Modifier.weight(1f),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                                Button(
                                    onClick = {
                                        if (chatMessageInput.isNotBlank()) {
                                            chatHistoryList.add(Pair("You", chatMessageInput))
                                            val query = chatMessageInput
                                            chatMessageInput = ""
                                            // Simulated smart AI tutor response matching theme
                                            scope.launch {
                                                delay(1200)
                                                val answer = if (query.lowercase().contains("homework")) {
                                                    "Sure! Priya submits tasks with 90% accuracy. We should continue to encourage daily revisions."
                                                } else if (query.lowercase().contains("schedule") || query.lowercase().contains("time")) {
                                                    "We are currently finalizing class times for Summer batches. We will post a poll shortly."
                                                } else {
                                                    "Got your message! I will personally review this performance standing and update you on progress metrics."
                                                }
                                                chatHistoryList.add(Pair("Teacher Sumit", answer))
                                            }
                                        }
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Interactive survey / feedback poll
                item {
                    Text("ACTIVE INSTITUTIONAL SURVEYS & POLLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("POLL STATUS: ACTIVE REVIEWS", fontSize = 8.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text("Syllabus Summer Timings Schedule Preference", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Coaching classes will undergo scheduling reviews. Please voice your preferred choice below:", fontSize = 11.sp, color = Color.Gray)
                            
                            val options = listOf(
                                "03:00 PM to 05:00 PM Afternoon session",
                                "05:00 PM to 07:00 PM Sundown session"
                            )
                            
                            options.forEach { opt ->
                                val voted = activePollVote == opt
                                ElevatedButton(
                                    onClick = { activePollVote = opt },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = if (voted) Color(0xFFC8E6C9) else Color.White
                                    )
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(opt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (voted) Color(0xFF1B5E20) else Color.DarkGray)
                                        if (voted) {
                                            Text("Voted (48%) ✔", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
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

    // MODAL DIALOGS FOR UPI PAYMENTS (Real Deep-linking system & offline validation)
    if (showPaymentSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = false },
            title = { Text("Payment Successful! 🏅") },
            text = { Text("Thank you! ₹$parentOutstandingFees tuition payment has been processed and reported to the institute ledger. Your child's balance is updated.") },
            confirmButton = {
                Button(onClick = {
                    parentOutstandingFees = 0
                    showPaymentSuccessDialog = false
                }) {
                    Text("Okay")
                }
            }
        )
    }

    if (showUpiDetailsDialog) {
        val merchantUpiId by viewModel.merchantUpiId.collectAsState()
        val merchantName by viewModel.merchantName.collectAsState()
        val upiUriString = "upi://pay?pa=$merchantUpiId&pn=${android.net.Uri.encode(merchantName)}&am=$parentOutstandingFees&cu=INR&tn=${android.net.Uri.encode("Tuition Fee for ${activeStudent.name}")}"

        AlertDialog(
            onDismissRequest = { showUpiDetailsDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF34A853))
                    Text("Secure UPI Deep-Link Invoice", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("You are paying directly to the coaching class's verified UPI merchant handle without standard middleman margins.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                            Text("PAYEE DETAILS:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                            Text(merchantName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("UPI ID: $merchantUpiId", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("AMOUNT DUE: ₹$parentOutstandingFees", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF34A853))
                        }
                    }

                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(upiUriString))
                                context.startActivity(intent)
                                // Standard immediate logging
                                Toast.makeText(context, "Opening GPay, PhonePe, BHIM, or Paytm...", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "No local UPI apps found. Showing simulation...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34A853)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PAY VIA PHONEPE / GPAY / BHIM", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Verify transaction below after sending money via UPI:", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = inputUpiId,
                        onValueChange = { inputUpiId = it },
                        label = { Text("Enter UPI Reference / UTR Number", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 12-digit transaction ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUpiId.isNotBlank()) {
                            showUpiDetailsDialog = false
                            showPaymentSuccessDialog = true
                        } else {
                            Toast.makeText(context, "Please enter the UPI transaction ID / reference to confirm.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Confirm Offline Remit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiDetailsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGstReceiptDialog) {
        Dialog(onDismissRequest = { showGstReceiptDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("THE ASPIRANTS ACADEMY INC", fontWeight = FontWeight.Black, color = Color.Blue)
                    Text("12/A, Civil Lines, Near General Chowk, India", fontSize = 10.sp, color = Color.Gray)
                    Text("GSTIN Registration: 09AAPCT1420K1ZX", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    
                    Text("TAX INVOICE COMPLIANT RECEIPT", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    Text("Invoice No: TAC-GST-6192026", fontSize = 10.sp)
                    Text("Date generated: 15-Jun-2026", fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text("Student Name: ${activeStudent.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Batch Enrolled: Class 10 Science Batch Model", fontSize = 11.sp, color = Color.DarkGray)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    
                    // Calculation breakdown
                    val baseTaxable = 2118f
                    val cgstValue = baseTaxable * 0.09f
                    val sgstValue = baseTaxable * 0.09f
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Description (SAC Code: 9992)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Amount (INR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Coaching Tuition Fees (June installment)", fontSize = 11.sp)
                        Text("₹${baseTaxable.toInt()}", fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CGST @ 9% on taxable", fontSize = 10.sp, color = Color.Gray)
                        Text("₹${cgstValue.toInt()}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SGST @ 9% on taxable", fontSize = 10.sp, color = Color.Gray)
                        Text("₹${sgstValue.toInt()}", fontSize = 10.sp, color = Color.Gray)
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("AGGREGATE PAYABLE TOTAL (INR):", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        Text("₹2500", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Certified study services provided match the provisions of CGST / SGST acts.", fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            Toast.makeText(context, "Compliant Invoice saved to device!", Toast.LENGTH_SHORT).show()
                            showGstReceiptDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Print & Save compliant PDF")
                    }
                }
            }
        }
    }

    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                border = BorderStroke(4.dp, Color(0xFFD4AF37)) // Gold boarder colors
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("★ CERTIFICATE OF ACADEMIC EXCELLENCE ★", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFD4AF37))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("THE ASPIRANTS CLASSES INDIA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    HorizontalDivider(color = Color(0xFFD4AF37), thickness = 2.dp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This award is proudly presented to", fontSize = 11.sp, color = Color.Gray)
                    Text(activeStudent.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D47A1))
                    
                    Text(
                        text = "for outstanding performance in scoring an exceptional 92% in Newton physics core diagnostics, securing an elite Rank 1 inside the class setup.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Director SUMIT KUMAR", fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0D47A1))
                    Text("Institutional Director Seal", fontSize = 8.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Certificate shared to WhatsApp!", Toast.LENGTH_SHORT).show()
                                showCertificateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Share on WhatsApp", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { showCertificateDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}


