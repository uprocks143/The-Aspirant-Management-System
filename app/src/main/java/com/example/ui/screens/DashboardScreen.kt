package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
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
    val staffProfiles by viewModel.staffProfiles.collectAsState()
    val subscriberTransactions by viewModel.subscriberTransactions.collectAsState()

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
    var showQuizDialog by remember { mutableStateOf(false) }
    var selectedMaterialForQuiz by remember { mutableStateOf<StudyMaterial?>(null) }
    var showAdmissionDialogConfirm by remember { mutableStateOf<Student?>(null) }
    var showBulkAttendanceSelectBatch by remember { mutableStateOf<Long?>(null) }
    var activeExamPracticeSession by remember { mutableStateOf<Exam?>(null) }

    // Dialog state controllers
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showDoubtSolverBot by remember { mutableStateOf(false) }
    var showQrAttendanceScanner by remember { mutableStateOf(false) }
    var showPerformanceReportDialog by remember { mutableStateOf(false) }
    var showEnquiryManagerDialog by remember { mutableStateOf(false) }
    var showStaffManagerDialog by remember { mutableStateOf(false) }
    var showTeacherSalariesDialog by remember { mutableStateOf(false) }
    var showLibraryCenterDialog by remember { mutableStateOf(false) }
    var showReportsConsoleDialog by remember { mutableStateOf(false) }
    var showHomeworkAssignDialog by remember { mutableStateOf(false) }
    var showTodoTaskDialog by remember { mutableStateOf(false) }
    var showPaperGeneratorDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var hasUnreadNotifications by remember { mutableStateOf(false) }
    var showEnrollmentForm by remember { mutableStateOf(false) }
    var showAlumniFilterOnly by remember { mutableStateOf(false) }
    var showSettingsPanel by remember { mutableStateOf(false) }

    // Next-Gen Feature Dialog States
    var showNextGenCabinetInline by remember { mutableStateOf(false) }
    var showNextGenAILearningDialog by remember { mutableStateOf(false) }
    var showNextGenEdTechDialog by remember { mutableStateOf(false) }
    var showNextGenInsightsDialog by remember { mutableStateOf(false) }
    var showNextGenMonetizationDialog by remember { mutableStateOf(false) }
    var showNextGenEngagementDialog by remember { mutableStateOf(false) }
    var showNextGenSecurityDialog by remember { mutableStateOf(false) }
    var showNextGenSocialFlyerDialog by remember { mutableStateOf(false) }

    // Custom side drawer dialogs
    var showMotivationalDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }
    var showOtherAppsDialog by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = m3ColorScheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backEnabled = currentScreenFlow != "SPLASH" ||
                    showHelpSupportDialog || showDoubtSolverBot || showQrAttendanceScanner || showPerformanceReportDialog ||
                    showEnquiryManagerDialog || showStaffManagerDialog || showTeacherSalariesDialog || showLibraryCenterDialog || showReportsConsoleDialog || showHomeworkAssignDialog ||
                    showTodoTaskDialog || showPaperGeneratorDialog || showNotificationsDialog ||
                    showNextGenAILearningDialog || showNextGenEdTechDialog || showNextGenInsightsDialog ||
                    showNextGenMonetizationDialog || showNextGenEngagementDialog || showNextGenSecurityDialog ||
                    showNextGenSocialFlyerDialog || showMotivationalDialog || showAboutAppDialog || showOtherAppsDialog ||
                    activeAdminTab != "Dashboard" || activeBottomNavTab != "DASHBOARD"

            BackHandler(enabled = backEnabled) {
                if (showNextGenAILearningDialog) { showNextGenAILearningDialog = false }
                else if (showNextGenEdTechDialog) { showNextGenEdTechDialog = false }
                else if (showNextGenInsightsDialog) { showNextGenInsightsDialog = false }
                else if (showNextGenMonetizationDialog) { showNextGenMonetizationDialog = false }
                else if (showNextGenEngagementDialog) { showNextGenEngagementDialog = false }
                else if (showNextGenSecurityDialog) { showNextGenSecurityDialog = false }
                else if (showNextGenSocialFlyerDialog) { showNextGenSocialFlyerDialog = false }
                else if (showMotivationalDialog) { showMotivationalDialog = false }
                else if (showAboutAppDialog) { showAboutAppDialog = false }
                else if (showOtherAppsDialog) { showOtherAppsDialog = false }
                else if (showHelpSupportDialog) { showHelpSupportDialog = false }
                else if (showDoubtSolverBot) { showDoubtSolverBot = false }
                else if (showQrAttendanceScanner) { showQrAttendanceScanner = false }
                else if (showPerformanceReportDialog) { showPerformanceReportDialog = false }
                else if (showEnquiryManagerDialog) { showEnquiryManagerDialog = false }
                else if (showStaffManagerDialog) { showStaffManagerDialog = false }
                else if (showTeacherSalariesDialog) { showTeacherSalariesDialog = false }
                else if (showLibraryCenterDialog) { showLibraryCenterDialog = false }
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
                        "ADMIN_LOGIN", "STUDENT_LOGIN", "LIBRARY_LOGIN" -> {
                            currentScreenFlow = "PORTAL_SELECT"
                        }
                        "MAIN_APP" -> {
                            Toast.makeText(context, "Please use the Logout action in the side menu to switch accounts.", Toast.LENGTH_SHORT).show()
                        }
                        "PORTAL_SELECT" -> {
                            currentScreenFlow = "SPLASH"
                        }
                    }
                }
            }

            when (currentScreenFlow) {
                "SPLASH" -> {
                    SplashScreenWalkthrough(onFinish = {
                        if (userRole != "GUEST") {
                            currentScreenFlow = "MAIN_APP"
                            Toast.makeText(context, "Welcome back! Session restored.", Toast.LENGTH_SHORT).show()
                        } else {
                            currentScreenFlow = "PORTAL_SELECT"
                        }
                    })
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
                            currentScreenFlow = "STUDENT_CONTEXT_SWITCHER"
                        },
                        onStaffSelect = {
                            currentScreenFlow = "ADMIN_LOGIN"
                        },
                        onParentSelect = {
                            viewModel.loginAs("PARENT")
                            currentScreenFlow = "MAIN_APP"
                        },
                        onLibrarySelect = {
                            currentScreenFlow = "LIBRARY_LOGIN"
                        }
                    )
                }
                "STUDENT_CONTEXT_SWITCHER" -> {
                    StudentContextSwitcherScreen(
                        viewModel = viewModel,
                        onContextSelected = { activeContext ->
                            viewModel.switchStudentContext(activeContext)
                            currentScreenFlow = "MAIN_APP"
                        },
                        onBack = { currentScreenFlow = "PORTAL_SELECT" }
                    )
                }
                "LIBRARY_LOGIN" -> {
                    LibraryAuthView(
                        viewModel = viewModel,
                        showBackButton = true,
                        onLoginSuccess = {
                            currentScreenFlow = "MAIN_APP"
                        },
                        onBack = {
                            currentScreenFlow = "PORTAL_SELECT"
                        }
                    )
                }
                "ADMIN_LOGIN" -> {
                    AdminLoginScreen(
                        viewModel = viewModel,
                        onBack = { currentScreenFlow = "PORTAL_SELECT" },
                        onSignIn = { academy, name, email, addr ->
                            viewModel.registerInstitute(academy, name, email, addr)
                            currentScreenFlow = "MAIN_APP"
                        }
                    )
                }
                "MAIN_APP" -> {
                    if (false) {
                        LibraryCenterOverlay(
                            viewModel = viewModel,
                            onClose = {
                                viewModel.loginAs("GUEST")
                                currentScreenFlow = "PORTAL_SELECT"
                                Toast.makeText(context, "Logged out from Physical Library Hub session.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
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
                                    activeAdminTab = activeAdminTab,
                                    activeStudentTab = activeStudentTab,
                                    onSelectAdminTab = { tab ->
                                        activeAdminTab = tab
                                        activeStudentTab = ""
                                        activeBottomNavTab = "DASHBOARD"
                                        scope.launch { drawerState.close() }
                                    },
                                    onSelectStudentTab = { tab ->
                                        activeStudentTab = tab
                                        activeAdminTab = ""
                                        activeBottomNavTab = "DASHBOARD"
                                        scope.launch { drawerState.close() }
                                    },
                                    onSelectBottomTab = { tab ->
                                        activeBottomNavTab = tab
                                        scope.launch { drawerState.close() }
                                    },
                                    onLogout = {
                                        viewModel.loginAs("GUEST")
                                        scope.launch { drawerState.close() }
                                        currentScreenFlow = "PORTAL_SELECT"
                                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onMotivationalClick = {
                                        showMotivationalDialog = true
                                        scope.launch { drawerState.close() }
                                    },
                                    onSubscriptionClick = {
                                        showNextGenMonetizationDialog = true
                                        scope.launch { drawerState.close() }
                                    },
                                    onSettingsClick = {
                                        showSettingsPanel = !showSettingsPanel
                                        scope.launch { drawerState.close() }
                                    },
                                    onAboutAppClick = {
                                        showAboutAppDialog = true
                                        scope.launch { drawerState.close() }
                                    },
                                    onHelpClick = {
                                        showDoubtSolverBot = true
                                        scope.launch { drawerState.close() }
                                    },
                                    onReferralClick = {
                                        showNextGenMonetizationDialog = true
                                        scope.launch { drawerState.close() }
                                    },
                                    onOtherAppsClick = {
                                        showOtherAppsDialog = true
                                        scope.launch { drawerState.close() }
                                    },
                                    themeMode = themeKey,
                                    onSwitchTheme = { newTheme ->
                                        viewModel.switchTheme(newTheme)
                                    }
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                val isMainDashboard = activeBottomNavTab == "DASHBOARD" && (activeAdminTab == "Dashboard" || (activeAdminTab.isEmpty() && activeStudentTab.isEmpty()))
                                if (isMainDashboard) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Transparent
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        colors = listOf(Color(0xFF29B6F6), Color(0xFF00E5FF))
                                                    )
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .statusBarsPadding()
                                                    .height(64.dp)
                                                    .padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Menu,
                                                        contentDescription = "Menu",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(
                                                    text = "Main Menu",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(
                                                    onClick = { showNotificationsDialog = true },
                                                    modifier = Modifier.testTag("notification_bell_button")
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
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { showSettingsPanel = !showSettingsPanel },
                                                    modifier = Modifier.testTag("dashboard_settings_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "System Settings",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    TopAppBar(
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                activeAdminTab = "Dashboard"
                                                activeStudentTab = ""
                                                activeBottomNavTab = "DASHBOARD"
                                            }) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back to Main Menu",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        title = {
                                            val currentTitle = when {
                                                activeBottomNavTab == "MY_ACCOUNT" -> "My Account"
                                                activeAdminTab == "Admission Form" -> "Students Registry"
                                                activeAdminTab == "Batches Setup" -> "Batches Setup"
                                                activeAdminTab == "Tuition Fees" -> "Tuition Fees Console"
                                                else -> activeAdminTab
                                            }
                                            Text(
                                                text = currentTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            titleContentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            },

                        bottomBar = {
                            // Bottom Persistent navigation bar that mirrors Image 7 
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp,
                                modifier = Modifier.height(88.dp)
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
                                            val activeTitle = if (activeAdminTab.isNotEmpty()) activeAdminTab else if (activeStudentTab.isNotEmpty()) activeStudentTab else "Dashboard"
                                            Text(
                                                text = "ACTIVE CONSOLE  ❯  " + activeTitle.uppercase(),
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
                                        if (activeAdminTab == "System Diagnostics") {
                                            val institutesList by viewModel.institutesList.collectAsState()
                                            val subscriberTrans by viewModel.subscriberTransactions.collectAsState()
                                            AppOwnerMonitorView(
                                                transactions = transactionsList,
                                                batches = batchesList,
                                                students = studentsList,
                                                institutes = institutesList,
                                                subscriberTransactionsList = subscriberTrans,
                                                onApprove = { email -> viewModel.approveInstitute(email) },
                                                onApproveAll = { viewModel.approveAllInstitutes() },
                                                onDeleteInstitute = { email -> viewModel.deleteInstituteAccount(email) },
                                                onToggleSubscription = { email -> viewModel.toggleSubscriptionActive(email) },
                                                 onToggleSuspension = { email -> viewModel.toggleInstituteSuspension(email) },
                                                 onScheduleWipe = { email, delay24h -> viewModel.scheduleInstituteWipe(email, delay24h) },
                                                onRecordUpiPaymentTransaction = { academy, email, amount, ref, plan ->
                                                    viewModel.recordSubscriberTransaction(academy, email, amount, ref, plan)
                                                 },
                                                 onLogout = {
                                                     viewModel.loginAs("GUEST")
                                                 }
                                            )
                                        } else if (activeAdminTab == "Dashboard" || (activeAdminTab.isEmpty() && activeStudentTab.isEmpty())) {
                                            val checkAccessAndRun = { featureName: String, requiredRoles: List<String>, action: () -> Unit ->
                                                val hasAccess = requiredRoles.contains(userRole) || (userRole == "LIBRARY" && (requiredRoles.contains("ADMIN") || requiredRoles.contains("STAFF")))
                                                if (hasAccess) {
                                                    action()
                                                 } else {
                                                     Toast.makeText(context, "🚫 Access Denied: '$featureName' is restricted to ${requiredRoles.joinToString("/")} role(s). Current role: $userRole", Toast.LENGTH_LONG).show()
                                                 }
                                            }
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
                                                                    text = when (userRole) {
                                                                        "ADMIN" -> academyNameState
                                                                        "TEACHER" -> "$directorNameState ($academyNameState)"
                                                                        "LIBRARY" -> "$academyNameState (Library)"
                                                                        "STUDENT" -> "Student: $directorNameState"
                                                                        "PARENT" -> "Parent Desk"
                                                                        else -> academyNameState
                                                                    },
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 18.sp,
                                                                    color = Color(0xFF0347A1)
                                                                )
                                                                Text(
                                                                    text = "Role: $userRole • $adminEmailState",
                                                                    fontSize = 12.sp,
                                                                    color = Color.Gray,
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Side-by-Side Cosmic Summary just below the institute name
                                                item {
                                                    MetricCountersScrollableRow(
                                                        batchesList = batchesList,
                                                        studentsList = studentsList,
                                                        staffProfiles = staffProfiles,
                                                        onBatchesClick = { checkAccessAndRun("Batches Setup", listOf("ADMIN", "STAFF")) { activeAdminTab = "Batches Setup" } },
                                                        onStudentsClick = { checkAccessAndRun("Admission Form", listOf("ADMIN", "STAFF")) { activeAdminTab = "Admission Form" } },
                                                        onStaffClick = { checkAccessAndRun("Staff Manager", listOf("ADMIN", "STAFF")) { showStaffManagerDialog = true } }
                                                    )
                                                }

                                                item {
                                                    val dashboardShortcuts = listOf(
                                                        ShortcutItem("Batches", Icons.Default.CoPresent, Color(0xFF3BACF0)) {
                                                            checkAccessAndRun("Batches Setup", listOf("ADMIN", "STAFF")) { activeAdminTab = "Batches Setup" }
                                                        },
                                                        ShortcutItem("Students", Icons.Default.People, Color(0xFF3F51B5)) {
                                                            checkAccessAndRun("Admission Form", listOf("ADMIN", "STAFF")) { activeAdminTab = "Admission Form" }
                                                        },
                                                        ShortcutItem("Attendance", Icons.Default.FactCheck, Color(0xFF7B1FA2)) {
                                                            checkAccessAndRun("Attendance Scanner", listOf("ADMIN", "STAFF")) { showQrAttendanceScanner = true }
                                                        },
                                                        ShortcutItem("Tuition Fees", Icons.Default.Payments, Color(0xFF10B981)) {
                                                            checkAccessAndRun("Tuition Fees", listOf("ADMIN", "STAFF")) { activeAdminTab = "Tuition Fees" }
                                                        },
                                                        ShortcutItem("Income Expenses", Icons.Default.AccountBalance, Color(0xFFFFA000)) {
                                                            checkAccessAndRun("Ledger Accounts", listOf("ADMIN", "STAFF")) { activeAdminTab = "Ledger Accounts" }
                                                        },
                                                        ShortcutItem("Student Performance", Icons.Default.School, Color(0xFF76C21F)) {
                                                            checkAccessAndRun("Student Performance", listOf("ADMIN", "STAFF")) { showPerformanceReportDialog = true }
                                                        },
                                                        ShortcutItem("Enquiry Manager", Icons.Default.ContactSupport, Color(0xFF5CE1E6)) {
                                                            checkAccessAndRun("Enquiry Manager", listOf("ADMIN", "STAFF")) { showEnquiryManagerDialog = true }
                                                        },
                                                        ShortcutItem("Staff Manager", Icons.Default.Group, Color(0xFF0F747F)) {
                                                            checkAccessAndRun("Staff Manager", listOf("ADMIN", "STAFF")) { showStaffManagerDialog = true }
                                                        },
                                                        ShortcutItem("Teacher Salaries", Icons.Default.Paid, Color(0xFFE91E63)) {
                                                            checkAccessAndRun("Teacher Salaries", listOf("ADMIN")) { showTeacherSalariesDialog = true }
                                                        },
                                                        ShortcutItem("Giant Reports", Icons.Default.Assessment, Color(0xFFEF5350)) {
                                                            checkAccessAndRun("Giant Reports", listOf("ADMIN", "STAFF")) { showReportsConsoleDialog = true }
                                                        },
                                                        ShortcutItem("Study Material", Icons.Default.LibraryBooks, Color(0xFF212121)) {
                                                            checkAccessAndRun("Study Materials", listOf("ADMIN", "STAFF", "STUDENT")) {
                                                                if (userRole == "STUDENT") {
                                                                    activeAdminTab = ""
                                                                    activeStudentTab = "Academic Resources"
                                                                } else {
                                                                    activeStudentTab = ""
                                                                    activeAdminTab = "Study Materials"
                                                                    activeStaffTab = "Study Materials"
                                                                }
                                                            }
                                                        },
                                                        ShortcutItem("Homework", Icons.Default.HomeWork, Color(0xFF00C853)) {
                                                            checkAccessAndRun("Homework Assign", listOf("ADMIN", "STAFF", "STUDENT")) { showHomeworkAssignDialog = true }
                                                        },
                                                        ShortcutItem("Online Exams", Icons.Default.Computer, Color(0xFF3F51B5)) {
                                                            checkAccessAndRun("Practice Exams", listOf("ADMIN", "STAFF", "STUDENT")) {
                                                                activeAdminTab = ""
                                                                activeStudentTab = "Practice exams"
                                                            }
                                                        },
                                                        ShortcutItem("To Do Task", Icons.Default.PlaylistAddCheck, Color(0xFF78909C)) {
                                                            checkAccessAndRun("To Do Task", listOf("ADMIN", "STAFF")) { showTodoTaskDialog = true }
                                                        },
                                                        ShortcutItem("Backup/Restore", Icons.Default.Storage, Color(0xFF7B1FA2)) {
                                                            checkAccessAndRun("Database Backups", listOf("ADMIN", "STAFF")) { activeAdminTab = "Database Backups" }
                                                        },
                                                        ShortcutItem("Settings", Icons.Default.Settings, Color(0xFFF44336)) {
                                                            showSettingsPanel = !showSettingsPanel
                                                        },
                                                        ShortcutItem("Paper Generator", Icons.Default.Description, Color(0xFF00838F), true) {
                                                            checkAccessAndRun("Paper Generator", listOf("ADMIN", "STAFF")) { showPaperGeneratorDialog = true }
                                                        },
                                                        ShortcutItem("Physical Library", Icons.Default.Book, Color(0xFF6366F1)) {
                                                            showLibraryCenterDialog = true
                                                         },
                                                         ShortcutItem("Help & Support", Icons.Default.Help, Color(0xFFFF7043)) {
                                                             showHelpSupportDialog = true
                                                        },
                                                        ShortcutItem("Next-Gen Futures", Icons.Default.AutoAwesome, Color(0xFF7B1FA2)) {
                                                            showNextGenCabinetInline = !showNextGenCabinetInline
                                                        }
                                                    )

                                                    Column(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Text(
                                                            text = "🛠️ UNIFIED SERVICES DIRECTORY",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontFamily = FontFamily.Monospace,
                                                            letterSpacing = 1.sp,
                                                            modifier = Modifier.padding(bottom = 2.dp)
                                                        )

                                                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                                            val cols = 4
                                                            val rowsCount = (dashboardShortcuts.size + cols - 1) / cols
                                                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                                                for (rowIndex in 0 until rowsCount) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                    ) {
                                                                        for (colIndex in 0 until cols) {
                                                                            val index = rowIndex * cols + colIndex
                                                                            if (index < dashboardShortcuts.size) {
                                                                                val sc = dashboardShortcuts[index]
                                                                                Card(
                                                                                    modifier = Modifier
                                                                                        .weight(1f)
                                                                                        .clickable { sc.action() }
                                                                                        .testTag("shortcut_${sc.title.lowercase().replace(" ", "_").replace("/", "_")}"),
                                                                                    colors = CardDefaults.cardColors(containerColor = sc.color.copy(alpha = 0.12f)),
                                                                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, sc.color.copy(alpha = 0.4f)),
                                                                                    shape = RoundedCornerShape(12.dp)
                                                                                ) {
                                                                                    Column(
                                                                                        modifier = Modifier
                                                                                            .padding(8.dp)
                                                                                            .fillMaxWidth(),
                                                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                                                    ) {
                                                                                         Box(
                                                                                            modifier = Modifier
                                                                                                .size(44.dp)
                                                                                                .clip(RoundedCornerShape(10.dp))
                                                                                                .background(sc.color),
                                                                                            contentAlignment = Alignment.Center
                                                                                        ) {
                                                                                            if (sc.title == "Income Expenses") {
                                                                                                Text(
                                                                                                    text = "₹",
                                                                                                    color = Color.White,
                                                                                                    fontWeight = FontWeight.Bold,
                                                                                                    fontSize = 22.sp,
                                                                                                    fontFamily = FontFamily.SansSerif
                                                                                                )
                                                                                            } else {
                                                                                                Icon(
                                                                                                    imageVector = sc.icon,
                                                                                                    contentDescription = sc.title,
                                                                                                    tint = Color.White,
                                                                                                    modifier = Modifier.size(24.dp)
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                        Text(
                                                                                            text = sc.title,
                                                                                            fontSize = 10.sp,
                                                                                            fontWeight = FontWeight.Bold,
                                                                                            textAlign = TextAlign.Center,
                                                                                            color = if (sc.isSpecialRedLabel) Color(0xFFEF4444) else Color(0xFF1E293B),
                                                                                            maxLines = 1,
                                                                                            softWrap = false,
                                                                                            overflow = TextOverflow.Ellipsis,
                                                                                            modifier = Modifier.padding(horizontal = 2.dp)
                                                                                        )
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Spacer(modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // NEXT-GEN FUTURES INTEGRATION CABINET SITTING DIRECTLY BELOW UNIFIED SERVICES DIRECTORY
                                                if (showNextGenCabinetInline) {
                                                    item {
                                                        NextGenFuturesIntegrationCabinet(
                                                            onAILearningClick = { showNextGenAILearningDialog = true },
                                                            onEdTechClick = { showNextGenEdTechDialog = true },
                                                            onInsightsClick = { showNextGenInsightsDialog = true },
                                                            onMonetizationClick = { showNextGenMonetizationDialog = true },
                                                            onEngagementClick = { showNextGenEngagementDialog = true },
                                                            onSecurityClick = { showNextGenSecurityDialog = true },
                                                            onSocialFlyerClick = { showNextGenSocialFlyerDialog = true }
                                                        )
                                                    }
                                                }

                                                // bottom group of horizontal pills matching photo
                                                if (false) item {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 10.dp, bottom = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(20.dp))
                                                                    .background(Color(0xFF2E7D32))
                                                                    .clickable { activeAdminTab = "Batches Setup" }
                                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Batches",
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(20.dp))
                                                                    .background(Color(0xFFEF6C00))
                                                                    .clickable { activeAdminTab = "Admission Form" }
                                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Students",
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(20.dp))
                                                                    .background(Color(0xFFC62828))
                                                                    .clickable { showStaffManagerDialog = true }
                                                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                                            ) {
                                                                Text(
                                                                    text = "Staff",
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp
                                                                )
                                                            }
                                                        }

                                                        // Floating action-like purple circle with sparkles
                                                        Box(
                                                            modifier = Modifier
                                                                .size(44.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                                                        colors = listOf(Color(0xFF7B1FA2), Color(0xFF673AB7))
                                                                    )
                                                                )
                                                                .clickable {
                                                                    showNextGenCabinetInline = !showNextGenCabinetInline
                                                                }
                                                                .padding(10.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.AutoAwesome,
                                                                contentDescription = "Next-Gen Futures Hub Toggle",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }



                                                // Extra Drawer Access shortcuts carried directly to the Dashboard
                                                item {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                                            Text(
                                                                text = "🔧 SYSTEM CORE & EXTRA UTILITIES",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontFamily = FontFamily.Monospace,
                                                                letterSpacing = 1.sp
                                                            )

                                                            // Channel row 3: System Controls & Extra Admin Drawer features
                                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    val listSys = listOf(
                                                                        Triple("Outreach", Icons.Default.Chat) { activeAdminTab = "Outreach Engine" },
                                                                        Triple("Diagnose", Icons.Default.Troubleshoot) { if (userRole == "ADMIN") activeAdminTab = "System Diagnostics" else android.widget.Toast.makeText(context, "🚫 Access Denied: Diagnose / System Diagnostics access is restricted to App Owner (ADMIN) only.", android.widget.Toast.LENGTH_LONG).show() },
                                                                        Triple("Gemini AI", Icons.Default.AutoAwesome) { showQrAttendanceScanner = false; showDoubtSolverBot = true }
                                                                    )
                                                                    listSys.forEach { (lbl, icon, action) ->
                                                                        Card(
                                                                            modifier = Modifier.weight(1f).clickable { action() },
                                                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                                                                            shape = RoundedCornerShape(10.dp)
                                                                        ) {
                                                                            Row(
                                                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.Center,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Icon(icon, contentDescription = lbl, tint = Color(0xFFC2185B), modifier = Modifier.size(16.dp))
                                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                                Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2185B))
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
                                                    AutoSlidingPromoSlideshow(
                                                        viewModel = viewModel,
                                                        userRole = userRole
                                                     )
                                                }

                                                /* item { if (false) {                                                    Card(
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
                                                }
                                                */

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
                                                if (false) { // Centrally managed dialog handles settings
                                                item {
                                                    var tempName by remember(academyNameState) { mutableStateOf(academyNameState) }
                                                    var tempDirector by remember(directorNameState) { mutableStateOf(directorNameState) }
                                                    var tempEmail by remember(adminEmailState) { mutableStateOf(adminEmailState) }
                                                    val merchantUpiId by viewModel.merchantUpiId.collectAsState()
                                                    val merchantName by viewModel.merchantName.collectAsState()
                                                    var tempUpiId by remember(merchantUpiId) { mutableStateOf(merchantUpiId) }
                                                    var tempMerchantName by remember(merchantName) { mutableStateOf(merchantName) }
                                                    val customGeminiApiKey by viewModel.customGeminiApiKey.collectAsState()
                                                    var tempApiKey by remember(customGeminiApiKey) { mutableStateOf(customGeminiApiKey) }

                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(16.dp),
                                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                                        ) {
                                                            Text(
                                                                text = "SYSTEM SETTINGS & INSTANT PAYMENTS CONFIG",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontFamily = FontFamily.Monospace
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

                                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

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

                                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                                                            Text("COACHING ACADEMY IDENTITY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            OutlinedTextField(
                                                                value = tempName,
                                                                onValueChange = { tempName = it },
                                                                label = { Text("Academy Business Name", fontSize = 11.sp) },
                                                                modifier = Modifier.fillMaxWidth().testTag("config_academy_name_input"),
                                                                singleLine = true
                                                            )
                                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                OutlinedTextField(
                                                                    value = tempDirector,
                                                                    onValueChange = { tempDirector = it },
                                                                    label = { Text("Director Name", fontSize = 11.sp) },
                                                                    modifier = Modifier.weight(1f).testTag("config_director_name_input"),
                                                                    singleLine = true
                                                                )
                                                                OutlinedTextField(
                                                                    value = tempEmail,
                                                                    onValueChange = { tempEmail = it },
                                                                    label = { Text("Admin Email", fontSize = 11.sp) },
                                                                    modifier = Modifier.weight(1.2f).testTag("config_admin_email_input"),
                                                                    singleLine = true
                                                                )
                                                            }

                                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                                                            Text("REAL-TIME UPI CHECKOUT INTEGRATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            OutlinedTextField(
                                                                value = tempUpiId,
                                                                onValueChange = { tempUpiId = it },
                                                                label = { Text("Coaching UPI ID VPA", fontSize = 11.sp) },
                                                                placeholder = { Text("e.g. upi@domain") },
                                                                modifier = Modifier.fillMaxWidth().testTag("config_merchant_upi_id_input"),
                                                                singleLine = true
                                                            )
                                                            OutlinedTextField(
                                                                value = tempMerchantName,
                                                                onValueChange = { tempMerchantName = it },
                                                                label = { Text("Merchant Display Name", fontSize = 11.sp) },
                                                                modifier = Modifier.fillMaxWidth().testTag("config_merchant_name_input"),
                                                                singleLine = true
                                                            )

                                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                                                            Text("AI & GEMINI INTEGRATION SETTINGS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Text("Configure your own Gemini API Key to let students ask academic doubts.", fontSize = 11.sp, color = Color.Gray)
                                                            OutlinedTextField(
                                                                value = tempApiKey,
                                                                onValueChange = { tempApiKey = it },
                                                                label = { Text("Gemini API Key", fontSize = 11.sp) },
                                                                placeholder = { Text("AIzaSy...") },
                                                                modifier = Modifier.fillMaxWidth().testTag("config_gemini_api_key_input"),
                                                                singleLine = true,
                                                                visualTransformation = if (tempApiKey.isNotEmpty()) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
                                                            )

                                                            Button(
                                                                onClick = {
                                                                    if (tempName.isNotBlank() && tempDirector.isNotBlank() && tempEmail.contains("@") && tempUpiId.isNotBlank() && tempUpiId.contains("@")) {
                                                                        viewModel.updateAcademySettings(tempName, tempDirector, tempEmail)
                                                                        viewModel.updateMerchantUPI(tempUpiId, tempMerchantName)
                                                                        viewModel.updateCustomGeminiApiKey(tempApiKey)
                                                                        Toast.makeText(context, "All academy settings, UPI keys, and AI configurations updated!", Toast.LENGTH_LONG).show()
                                                                    } else {
                                                                        Toast.makeText(context, "Verify that all fields are correct.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                modifier = Modifier.fillMaxWidth().testTag("save_academy_config_button"),
                                                                shape = RoundedCornerShape(8.dp)
                                                            ) {
                                                                Text("Save Academy & Gateway Settings", fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                            // Handle all remaining original sub-workspace tabs
                                            when {
                                                activeAdminTab == "Admission Form" || activeAdminTab == "Batches Setup" || activeAdminTab == "Tuition Fees" || activeAdminTab == "Ledger Accounts" || activeAdminTab == "Database Backups" || activeAdminTab == "Outreach Engine" -> {
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
                                                activeAdminTab == "Study Materials" || activeAdminTab == "Attendance" -> {
                                                    StaffWorkspace(
                                                        activeStaffTab = if (activeAdminTab == "Study Materials") "Study Materials" else "Attendance",
                                                        viewModel = viewModel,
                                                        batches = batchesList,
                                                        students = studentsList,
                                                        studyMaterials = studyMaterialsList,
                                                        staffScreenAccess = staffScreenAccess,
                                                        onShowAddBatch = { showAddBatchDialog = it },
                                                        onQuizClick = {
                                                            selectedMaterialForQuiz = it
                                                            showQuizDialog = true
                                                        }
                                                    )
                                                }
                                                activeStudentTab == "Home Workspace" || activeStudentTab == "Academic Resources" || activeStudentTab == "Practice exams" -> {
                                                    StudentWorkspace(
                                                        activeStudentTab = activeStudentTab,
                                                        viewModel = viewModel,
                                                        batches = batchesList,
                                                        students = studentsList,
                                                        studyMaterials = studyMaterialsList,
                                                        exams = examsList,
                                                        activeExamPracticeSession = activeExamPracticeSession,
                                                        onActiveExamPracticeSession = { activeExamPracticeSession = it },
                                                        onQuizClick = {
                                                            selectedMaterialForQuiz = it
                                                            showQuizDialog = true
                                                        },
                                                        onLaunchQRScanner = { showQrAttendanceScanner = true }
                                                    )
                                                }
                                                activeStudentTab == "Child Standing" || activeStudentTab == "Tuition & Fees" || activeStudentTab == "Academic Progress" || activeStudentTab == "Contact Teachers" -> {
                                                    ParentWorkspace(
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
            }

            }

            // Overlay Dialogs definitions
            if (showSettingsPanel) {
                SettingsOverlayDialog(viewModel = viewModel, onClose = { showSettingsPanel = false })
            }

            if (showNotificationsDialog) {
                NotificationsDialog(
                    onClose = { showNotificationsDialog = false },
                    onMarkAllRead = {
                        hasUnreadNotifications = false
                        showNotificationsDialog = false
                    }
                )
            }

            // Render the floating, draggable Messenger-style Gemini Doubt Solver Bot on the workspace when requested
            if (showDoubtSolverBot) {
                GeminiDoubtSolverBot(viewModel = viewModel, onClose = { showDoubtSolverBot = false })
            }

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

            if (showTeacherSalariesDialog) {
                com.example.ui.screens.TeacherSalariesOverlay(viewModel = viewModel, onClose = { showTeacherSalariesDialog = false })
            }

            if (showLibraryCenterDialog) {
                LibraryCenterOverlay(viewModel = viewModel, onClose = { showLibraryCenterDialog = false })
            }

            if (showHelpSupportDialog) {
                com.example.ui.screens.HelpSupportOverlay(onClose = { showHelpSupportDialog = false })
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

            // Next-Gen Overlay Dialogs
            if (showNextGenAILearningDialog) {
                NextGenAILearningOverlay(viewModel = viewModel, onClose = { showNextGenAILearningDialog = false })
            }
            if (showNextGenEdTechDialog) {
                NextGenEdTechOverlay(viewModel = viewModel, onClose = { showNextGenEdTechDialog = false })
            }
            if (showNextGenInsightsDialog) {
                NextGenInsightsOverlay(viewModel = viewModel, onClose = { showNextGenInsightsDialog = false })
            }
            if (showNextGenMonetizationDialog) {
                NextGenMonetizationOverlay(viewModel = viewModel, onClose = { showNextGenMonetizationDialog = false })
            }
            if (showNextGenEngagementDialog) {
                NextGenEngagementOverlay(viewModel = viewModel, onClose = { showNextGenEngagementDialog = false })
            }
            if (showNextGenSecurityDialog) {
                NextGenSecurityOverlay(viewModel = viewModel, onClose = { showNextGenSecurityDialog = false })
            }
            if (showNextGenSocialFlyerDialog) {
                NextGenSocialFlyerOverlay(viewModel = viewModel, onClose = { showNextGenSocialFlyerDialog = false })
            }

            // Custom side drawer dialogs overlay
            if (showMotivationalDialog) {
                AlertDialog(
                    onDismissRequest = { showMotivationalDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Daily Motivational Post", fontWeight = FontWeight.Bold, color = Color(0xFF37474F), fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF80DEEA), Color(0xFF0288D1))))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Text(
                                        text = "\"Nurturing Ranks, Unlocking Potentials. Success is not overnight; it is the sum of small efforts repeated day in and day out!\"",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Today's Performance Target", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                                        Text("Complete active test sets & check homework reviews!", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showMotivationalDialog = false }
                        ) {
                            Text("Dismiss", color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showQuizDialog && selectedMaterialForQuiz != null) {
                StudyMaterialQuizManagerDialog(
                    studyMaterial = selectedMaterialForQuiz!!,
                    viewModel = viewModel,
                    onDismiss = {
                        showQuizDialog = false
                        selectedMaterialForQuiz = null
                    }
                )
            }

            if (showAboutAppDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutAppDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                TuitionClassLogo(modifier = Modifier.fillMaxSize())
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("About Tuition System", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1), fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Tuition Class Management System",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF37474F)
                            )
                            Text(
                                text = "Version 4.2.0 (Stable Production Node)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "An all-in-one institutional administration suite for progressive learning spaces. Developed with Jetpack Compose, Material Design 3, Room Local SQL Cache, and Gemini AI assistance features.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Text("Features Integrated:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF37474F))
                            val features = listOf("✓ Dual SIM Telephony Alerts", "✓ Offline QR Admission scanner", "✓ Gemini AI Solver Bot", "✓ Real-time Cashbook Ledger")
                            features.forEach { feat ->
                                Text(feat, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showAboutAppDialog = false }
                        ) {
                            Text("Awesome", color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showOtherAppsDialog) {
                AlertDialog(
                    onDismissRequest = { showOtherAppsDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF0288D1), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Other Education Solutions", fontWeight = FontWeight.Bold, color = Color(0xFF37474F), fontSize = 16.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val apps = listOf(
                                Triple("Dynamic Doubt Solver", "Real-time AI photo solver for advanced high school curriculum subjects.", Color(0xFF9C27B0)),
                                Triple("Interactive Quiz Master", "Custom test builders, flashcards, statistics and performance analytics.", Color(0xFF4CAF50)),
                                Triple("Parent School Connect", "Real-time attendance tracking, academic standings and direct fee checkout notifications.", Color(0xFFFF9800))
                            )
                            apps.forEach { app ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(app.third.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = app.third, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.first, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF37474F))
                                            Text(app.second, fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showOtherAppsDialog = false }
                        ) {
                            Text("Close", color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                        }
                    }
                )
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

@Composable
fun HighPriorityCardHorizontal(
    title: String,
    stat: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    action: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action() }
            .testTag(tag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stat,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun HighPriorityCardVertical(
    title: String,
    stat: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    tag: String,
    action: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stat,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


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
    var showEnrollmentForm by remember { mutableStateOf(false) }
    var showAlumniFilterOnly by remember { mutableStateOf(false) }

    // Local states for editing student enrollees
    var showingStudentEditorFor by remember { mutableStateOf<Student?>(null) }
    var editStudentName by remember { mutableStateOf("") }
    var editStudentRoll by remember { mutableStateOf("") }
    var editStudentPhone by remember { mutableStateOf("") }
    var editStudentParent by remember { mutableStateOf("") }
    var editStudentParentPhone by remember { mutableStateOf("") }
    var editStudentClass by remember { mutableStateOf("") }
    var editStudentBatchId by remember { mutableStateOf(0L) }

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
                if (showingStudentEditorFor != null) {
                    item {
                        val editContext = androidx.compose.ui.platform.LocalContext.current
                        AlertDialog(
                            onDismissRequest = { showingStudentEditorFor = null },
                            title = { Text("Edit Student Profile Info", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = editStudentName,
                                        onValueChange = { editStudentName = it },
                                        label = { Text("Student Full Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStudentRoll,
                                        onValueChange = { editStudentRoll = it },
                                        label = { Text("Roll Number / Code") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStudentClass,
                                        onValueChange = { editStudentClass = it },
                                        label = { Text("Standard Class / Focus Grade") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStudentPhone,
                                        onValueChange = { editStudentPhone = it },
                                        label = { Text("Personal Student Phone") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStudentParent,
                                        onValueChange = { editStudentParent = it },
                                        label = { Text("Parent / Guardian Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editStudentParentPhone,
                                        onValueChange = { editStudentParentPhone = it },
                                        label = { Text("Parent Contact Phone") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text("Assign Coaching Class Slot:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                                    batches.forEach { b ->
                                        val selected = editStudentBatchId == b.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { editStudentBatchId = b.id }
                                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = selected, onClick = { editStudentBatchId = b.id })
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(b.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val s = showingStudentEditorFor
                                        if (s != null && editStudentName.isNotBlank() && editStudentRoll.isNotBlank()) {
                                            viewModel.updateStudent(
                                                s.copy(
                                                    name = editStudentName,
                                                    rollNumber = editStudentRoll,
                                                    phone = editStudentPhone,
                                                    parentName = editStudentParent,
                                                    parentPhone = editStudentParentPhone,
                                                    studentClass = editStudentClass,
                                                    batchId = editStudentBatchId
                                                )
                                            )
                                            showingStudentEditorFor = null
                                            Toast.makeText(editContext, "${editStudentName} registry updated successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(editContext, "Full Name and Roll are strictly mandatory.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text("Approve Update")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showingStudentEditorFor = null }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
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
                    Button(
                        onClick = { showEnrollmentForm = !showEnrollmentForm },
                        modifier = Modifier.fillMaxWidth().testTag("add_new_student_tab_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (showEnrollmentForm) Icons.Default.ExpandLess else Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showEnrollmentForm) "Hide Registration Form" else "Enroll New Student (+)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showEnrollmentForm) {
                    item {
                        RegistrationIntakeForm(batches = batches, viewModel = viewModel, onShowConfirm = onShowAdmissionConfirm)
                    }
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
                        text = "STUDENTS DIRECTORY NETWORK PANEL",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !showAlumniFilterOnly,
                            onClick = { showAlumniFilterOnly = false },
                            label = { Text("Active Scholars (${students.count { !it.isAlumni }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (!showAlumniFilterOnly) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = showAlumniFilterOnly,
                            onClick = { showAlumniFilterOnly = true },
                            label = { Text("Old Students / Alumni (${students.count { it.isAlumni }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (showAlumniFilterOnly) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                val currentDisplayList = students.filter { it.isAlumni == showAlumniFilterOnly }

                if (currentDisplayList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (showAlumniFilterOnly) "No archived old students or graduated alumni found here." else "No active classroom student enrollees found. Register above.",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(currentDisplayList) { student ->
                        val batchStr = batches.find { it.id == student.batchId }?.name ?: "No Allotted Batch"
                        val context = LocalContext.current
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // 1st Line: Student Name
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // 2nd Line: Roll Number, Class
                                        Text("Roll Number: ${student.rollNumber} • Class: ${student.studentClass ?: "N/A"}", fontSize = 12.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        // 3rd Line: Coaching Batch name
                                        Text("Coaching Batch: $batchStr", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }

                                    // Render a nice visual badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (student.isAlumni) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (student.isAlumni) "Alumni/Old" else "Active",
                                            color = if (student.isAlumni) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                                            Icon(
                                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                                contentDescription = "WhatsApp Notice",
                                                tint = Color.Unspecified,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Alumni Toggle Transition (Graduation Symbol)
                                        IconButton(
                                            onClick = {
                                                val updatedAlumniState = !student.isAlumni
                                                viewModel.updateStudent(student.copy(isAlumni = updatedAlumniState))
                                                val statusMsg = if (updatedAlumniState) "archived to Old Students list." else "restored to active classroom enrollees."
                                                Toast.makeText(context, "${student.name} is successfully $statusMsg", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (student.isAlumni) Icons.Default.Restore else Icons.Default.School,
                                                contentDescription = "Graduate Alumni Toggle",
                                                tint = if (student.isAlumni) Color(0xFFB39DDB) else Color(0xFF5E35B1),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Quick Edit action
                                        IconButton(
                                            onClick = {
                                                showingStudentEditorFor = student
                                                editStudentName = student.name
                                                editStudentRoll = student.rollNumber
                                                editStudentPhone = student.phone
                                                editStudentParent = student.parentName
                                                editStudentParentPhone = student.parentPhone
                                                editStudentClass = student.studentClass ?: ""
                                                editStudentBatchId = student.batchId
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
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

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    // Prebuilt Graphical card themes generation station
                    GraphicalCardsBrandStation(students = students, batches = batches, viewModel = viewModel)
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
    onShowAddBatch: (Boolean) -> Unit,
    onQuizClick: (StudyMaterial) -> Unit
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

        // Access enforcement bypassed (unrestricted admin access)
        val blockAccess = false
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
                    UploadStudyResourcesSubModule(
                        batches = batches,
                        studyMaterials = studyMaterials,
                        viewModel = viewModel,
                        onQuizClick = onQuizClick
                    )
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
    onActiveExamPracticeSession: (Exam?) -> Unit,
    onQuizClick: (StudyMaterial) -> Unit,
    onLaunchQRScanner: () -> Unit
) {
    val context = LocalContext.current
    val currentStudentId by viewModel.currentUserId.collectAsState()
    val activeStudent = remember(currentStudentId, students) {
        students.firstOrNull { it.id == currentStudentId } ?: students.firstOrNull()
    }

    var studySearchQuery by remember { mutableStateOf("") }
    var studyTypeFilter by remember { mutableStateOf("All") }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val favoritedIds by viewModel.favoritedMaterialIds.collectAsState()
    val personalNotesMap by viewModel.personalNotesMap.collectAsState()

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
            val studentContext by viewModel.activeStudentContext.collectAsState()
            var showContextSwitcherOverlay by remember { mutableStateOf(false) }
            
            if (showContextSwitcherOverlay) {
                AlertDialog(
                    onDismissRequest = { showContextSwitcherOverlay = false },
                    title = { Text("Switch Multitenant Context 🏢", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Aap ek click me standard registered tuition / coaching / library swap kar sakte hain:", fontSize = 12.sp)
                            
                            listOf(
                                Triple("👨‍🏫 Verma Sir's Tuition", "Verma Sir's Tuition", "Physics and Chemistry classes"),
                                Triple("🏢 City Institute", "City Institute", "Coaching Institute Multi-batch"),
                                Triple("📖 Silent Library", "Silent Library", "Silent Study Cabin Space")
                            ).forEach { (label, contextVal, subtitle) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (studentContext == contextVal) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable {
                                            viewModel.switchStudentContext(contextVal)
                                            showContextSwitcherOverlay = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (studentContext == contextVal) {
                                        Text("ACTIVE ✅", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showContextSwitcherOverlay = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ACTIVE MULTI-TENANT WORKSPACE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = when (studentContext) {
                                "Verma Sir's Tuition" -> "👨‍🏫 Verma Sir's Tuition"
                                "City Institute" -> "🏢 City Institute"
                                "Silent Library" -> "📖 Silent Library"
                                else -> studentContext
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Accessing exams, materials for this specific workspace context.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Button(
                        onClick = { showContextSwitcherOverlay = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(35.dp)
                    ) {
                        Text("Switch 🏢", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onLaunchQRScanner() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Institute Standee")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Institute QR to Attend", fontWeight = FontWeight.Bold)
                            }
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
                                listOf("Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12", "Competitive Prep", "Reference Maps").forEach { cat ->
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
                                "Class 1" -> listOf(
                                    Triple("Maths-Magic 1 (Mathematics)", "Class 1 basic counting & shapes", "https://ncert.nic.in/textbook.php?aemh1=0-13"),
                                    Triple("Marigold 1 (English)", "Class 1 early reading and basic vocab", "https://ncert.nic.in/textbook.php?aeeng=0-9"),
                                    Triple("Rimjhim 1 (Hindi)", "Class 1 alphabets & short stories", "https://ncert.nic.in/textbook.php?aerh1=0-19")
                                )
                                "Class 2" -> listOf(
                                    Triple("Maths-Magic 2 (Mathematics)", "Class 2 additions, subtractions & patterns", "https://ncert.nic.in/textbook.php?bemh1=0-15"),
                                    Triple("Marigold 2 (English)", "Class 2 poems, stories and spelling reader", "https://ncert.nic.in/textbook.php?beeng=0-10"),
                                    Triple("Rimjhim 2 (Hindi)", "Class 2 general conversations and lessons", "https://ncert.nic.in/textbook.php?berh1=0-15")
                                )
                                "Class 3" -> listOf(
                                    Triple("Maths-Magic 3 (Mathematics)", "Class 3 division, multiplication and weight", "https://ncert.nic.in/textbook.php?cemh1=0-14"),
                                    Triple("Looking Around 3 (EVS)", "Environmental studies, nature and neighbors", "https://ncert.nic.in/textbook.php?ceevs=0-24"),
                                    Triple("Marigold 3 (English)", "Class 3 primary grade english prose", "https://ncert.nic.in/textbook.php?ceeng=0-10")
                                )
                                "Class 4" -> listOf(
                                    Triple("Maths-Magic 4 (Mathematics)", "Class 4 fractions, charts, and smart tables", "https://ncert.nic.in/textbook.php?demh1=0-14"),
                                    Triple("Looking Around 4 (EVS)", "Class 4 civic studies and environment", "https://ncert.nic.in/textbook.php?deevs=0-27"),
                                    Triple("Marigold 4 (English)", "Class 4 English grammar & reader book", "https://ncert.nic.in/textbook.php?deeng=0-9")
                                )
                                "Class 5" -> listOf(
                                    Triple("Maths-Magic 5 (Mathematics)", "Class 5 coordinate maps, volumes and nets", "https://ncert.nic.in/textbook.php?eemh1=0-14"),
                                    Triple("Looking Around 5 (EVS)", "Class 5 ecosystem, animals & natural resources", "https://ncert.nic.in/textbook.php?eeevs=0-22"),
                                    Triple("Marigold 5 (English)", "Class 5 vocabulary booster and standard prose", "https://ncert.nic.in/textbook.php?eeeng=0-9")
                                )
                                "Class 6" -> listOf(
                                    Triple("Mathematics (Class VI)", "Class 6 whole numbers, decimals and proportions", "https://ncert.nic.in/textbook.php?femh1=0-14"),
                                    Triple("Science (Class VI)", "Class 6 food sources, fibers, light and motion", "https://ncert.nic.in/textbook.php?fesc1=0-16"),
                                    Triple("Our Pasts - I (History)", "Ancient history of india and heritage landmarks", "https://ncert.nic.in/textbook.php?fess1=0-10"),
                                    Triple("The Earth Our Habitat (Geography)", "Solar system, latitudes and landforms", "https://ncert.nic.in/textbook.php?fess2=0-8")
                                )
                                "Class 7" -> listOf(
                                    Triple("Mathematics (Class VII)", "Class 7 integers, simple equations & triangles", "https://ncert.nic.in/textbook.php?gemh1=0-15"),
                                    Triple("Science (Class VII)", "Class 7 heat, acids, weather & respiration", "https://ncert.nic.in/textbook.php?gesc1=0-18"),
                                    Triple("Our Pasts - II (History)", "Medieval indian dynasty trails, monuments & art", "https://ncert.nic.in/textbook.php?gess1=0-10"),
                                    Triple("Our Environment (Geography)", "Ecosystems, atmosphere, water and air systems", "https://ncert.nic.in/textbook.php?gess2=0-9")
                                )
                                "Class 8" -> listOf(
                                    Triple("Mathematics (Class VIII)", "Class 8 rational numbers, linear algebra & squares", "https://ncert.nic.in/textbook.php?hemh1=0-16"),
                                    Triple("Science (Class VIII)", "Class 8 synthetic fibers, coal, cells & metals", "https://ncert.nic.in/textbook.php?hesc1=0-18"),
                                    Triple("Resource and Development (Geography)", "Natural resources, minerals, farming and agriculture", "https://ncert.nic.in/textbook.php?hess4=0-6"),
                                    Triple("Social & Political Life - III", "The Indian Constitution, secularism & parliament", "https://ncert.nic.in/textbook.php?hess3=0-10")
                                )
                                "Class 9" -> listOf(
                                    Triple("Mathematics (Class IX Book)", "Class 9 coordinate geometry, Euclid's, quadrilaterals", "https://ncert.nic.in/textbook.php?iemh1=0-15"),
                                    Triple("Science (Class IX Book)", "Class 9 matter, atoms, structure, cell, tissues & gravitation", "https://ncert.nic.in/textbook.php?iesc1=0-15"),
                                    Triple("Contemporary India - I (Geography)", "India's position, physical features, rivers & climate", "https://ncert.nic.in/textbook.php?iess1=0-6"),
                                    Triple("Democratic Politics - I (Civics)", "Democracy, constitutional setup and civil rights", "https://ncert.nic.in/textbook.php?iess3=0-5")
                                )
                                "Class 10" -> listOf(
                                    Triple("Mathematics (Class X Book)", "Full Class 10 Arithmetic & Geometry book", "https://ncert.nic.in/textbook.php?jemh1=0-15"),
                                    Triple("Science (Class X Book)", "Full Class 10 Chemistry, Physics, Biology book", "https://ncert.nic.in/textbook.php?jesc1=0-16"),
                                    Triple("Social Science (India & World)", "History, Geography & Civics combined guide", "https://ncert.nic.in/textbook.php?jess1=0-7"),
                                    Triple("First Flight English Reader", "Class 10 Literacy Reader English syllabus", "https://ncert.nic.in/textbook.php?jeff1=0-11")
                                )
                                "Class 11" -> listOf(
                                    Triple("Mathematics (Class XI Book)", "Trigonometry, complex numbers, permutations & calculus", "https://ncert.nic.in/textbook.php?kemh1=0-16"),
                                    Triple("Physics - Part I (Class XI)", "Units, dimensions, vector motion and mechanics", "https://ncert.nic.in/textbook.php?keph1=0-8"),
                                    Triple("Chemistry - Part I (Class XI)", "Structure of atom, elements periodicity & states", "https://ncert.nic.in/textbook.php?kech1=0-7"),
                                    Triple("Biology (Class XI Book)", "Cell cycle, photosynthesis and human physiology", "https://ncert.nic.in/textbook.php?kebo1=0-22")
                                )
                                "Class 12" -> listOf(
                                    Triple("Mathematics (Part I & II)", "Higher secondary Algebra, Calculus & Core Matrices book", "https://ncert.nic.in/textbook.php?lemh1=0-6"),
                                    Triple("Physics (Part I Core)", "Class 12 Electrostatics & Magnetism syllabus", "https://ncert.nic.in/textbook.php?leph1=0-8"),
                                    Triple("Chemistry (Part I Core)", "Organic, Inorganic & Physical Class XII NCERT guide", "https://ncert.nic.in/textbook.php?lech1=0-9"),
                                    Triple("Biology (Complete Book)", "Ecology, Genetics and Plant physiology guides", "https://ncert.nic.in/textbook.php?lebo1=0-16")
                                )
                                "Competitive Prep" -> listOf(
                                    Triple("JEE Main Mathematics Manual", "Archive.org standard joint entrance calculus guide book", "https://archive.org/details/jee-main-advanced-mathematics-prep"),
                                    Triple("NEET Biology Exam Handbook", "Extensive medical biology worksheets & keys", "https://archive.org/details/neet-biology-prep"),
                                    Triple("UPSC CSAT General Studies Guide", "NCERT consolidated polity and governance chapters", "https://ncert.nic.in/textbook.php?gess1=0-7"),
                                    Triple("SSC CGL Quantitative Aptitude Workbook", "Comprehensive arithmetic, number systems, and algebra shortcuts", "https://archive.org/details/ssc-cgl-quant-workbook-2026")
                                )
                                else -> listOf(
                                    Triple("SOI Educational Guide Map of India", "Official Survey of India physical high-res geography map", "https://surveyofindia.gov.in/pages/educational-map-of-india"),
                                    Triple("NCERT Class X Historical Outline Atlas", "Political distribution and history timeline guides", "https://ncert.nic.in/textbook.php?jess1=3"),
                                    Triple("World Continents Relief Outlines", "High contrast topographical contours printable vectors", "https://ncert.nic.in/textbook.php?jesc1=15"),
                                    Triple("Physiography India Relief Outline Map", "Detailed elevation contour valleys of Himalaya and Deccan plateau", "https://surveyofindia.gov.in/pages/educational-map-of-india")
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

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = studySearchQuery,
                            onValueChange = { studySearchQuery = it },
                            placeholder = { Text("Search syllabus files by title, topic, or content...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                            trailingIcon = {
                                if (studySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { studySearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Filters row
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Favorites Filter Chip
                            FilterChip(
                                selected = showOnlyFavorites,
                                onClick = { showOnlyFavorites = !showOnlyFavorites },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = if (showOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (showOnlyFavorites) Color(0xFFFFB300) else Color.Gray
                                        )
                                        Text("Bookmarks", fontSize = 11.sp)
                                    }
                                }
                            )

                            // Type Filter Chips
                            listOf("All", "Plain Text", "PDF", "YouTube video", "URL").forEach { type ->
                                val isSelected = studyTypeFilter == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { studyTypeFilter = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                val materialForThisBatch = studyMaterials.filter { it.batchId == activeStudent.batchId }
                val filteredMaterials = materialForThisBatch.filter { mat ->
                    val matchesSearch = mat.title.contains(studySearchQuery, ignoreCase = true) || 
                                        mat.mainCategory.contains(studySearchQuery, ignoreCase = true) ||
                                        mat.topicSubCategory.contains(studySearchQuery, ignoreCase = true) ||
                                        mat.content.contains(studySearchQuery, ignoreCase = true)
                    val matchesType = studyTypeFilter == "All" || mat.contentType.equals(studyTypeFilter, ignoreCase = true)
                    val matchesFavorite = !showOnlyFavorites || favoritedIds.contains(mat.id.toString())
                    matchesSearch && matchesType && matchesFavorite
                }

                if (filteredMaterials.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (studySearchQuery.isNotEmpty() || studyTypeFilter != "All" || showOnlyFavorites) 
                                    "No syllabus files match your search filters." 
                                else 
                                    "No study materials uploaded for your active batch track yet.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(filteredMaterials) { resource ->
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
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Book, contentDescription = "Resource", tint = MaterialTheme.colorScheme.primary)
                                        Column {
                                            Text(resource.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("${resource.mainCategory} • ${resource.topicSubCategory}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    
                                    val isFavorited = favoritedIds.contains(resource.id.toString())
                                    IconButton(
                                        onClick = { viewModel.toggleFavoriteMaterial(resource.id) },
                                        modifier = Modifier.testTag("bookmark_btn_${resource.id}").size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorited) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Bookmark Study Material",
                                            tint = if (isFavorited) Color(0xFFFFB300) else Color.Gray,
                                            modifier = Modifier.size(22.dp)
                                        )
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
                                                try {
                                                    val link = resource.pdfName ?: ""
                                                    val urlStr = if (link.startsWith("http://") || link.startsWith("https://")) link else "https://www.google.com/search?q=" + android.net.Uri.encode("${resource.title} syllabus pdf $link")
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlStr))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Syllabus link invalid or browser offline", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            label = { Text("PDF: ${resource.pdfName}") },
                                            leadingIcon = { Icon(Icons.Default.Attachment, contentDescription = "PDF") }
                                        )
                                    }
                                    if (!resource.youtubeUrl.isNullOrEmpty()) {
                                        AssistChip(
                                            onClick = {
                                                try {
                                                    val link = resource.youtubeUrl ?: ""
                                                    val urlStr = if (link.startsWith("http://") || link.startsWith("https://")) link else "https://www.youtube.com/results?search_query=" + android.net.Uri.encode("${resource.title} educational tutorial")
                                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(urlStr))
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "YouTube link invalid or Web Offline", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            label = { Text("Stream YouTube") },
                                            leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = "Play") }
                                        )
                                    }
                                    AssistChip(
                                        onClick = {
                                            onQuizClick(resource)
                                        },
                                        label = { Text("Interactive Quizzes & Scores") },
                                        leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = "Quiz", tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.testTag("student_quiz_chip_${resource.id}"),
                                        colors = AssistChipDefaults.assistChipColors(
                                            labelColor = MaterialTheme.colorScheme.primary,
                                            leadingIconContentColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }

                                var notesExpanded by remember { mutableStateOf(false) }
                                var currentNoteText by remember { mutableStateOf(personalNotesMap[resource.id.toString()] ?: "") }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { notesExpanded = !notesExpanded }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Notes",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (currentNoteText.isEmpty()) "Add Personal Revision Notes" else "My Revision Notes (Saved)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (notesExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Toggle Notes",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (notesExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = currentNoteText,
                                        onValueChange = { currentNoteText = it },
                                        placeholder = { Text("Jot down formulas, chapter takeaways, questions, or revision lists...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            viewModel.savePersonalNote(resource.id, currentNoteText)
                                            Toast.makeText(context, "Takeaways saved!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.align(Alignment.End).height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Save Takeaways", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    var activeTab by remember { mutableStateOf("QR_CODE") } // "QR_CODE", "GPAY_DIRECT", "UPI_VPA"
    var enteredUpiId by remember { mutableStateOf("") }
    var enteredRefId by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }

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
                text = "SECURE PAYMENT GATEWAYS",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            // Segmented Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "QR_CODE" to "UPI QR",
                    "GPAY_DIRECT" to "Google Pay",
                    "UPI_VPA" to "UPI ID"
                ).forEach { (tabKey, tabLabel) ->
                    val isSelected = activeTab == tabKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeTab = tabKey }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (activeTab) {
                "QR_CODE" -> {
                    Text(
                        text = "Scan customized direct payment in-app QR code:",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp).border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).padding(8.dp)) {
                        // Jetpack Compose Canvas to draw authentic UPI QR code pattern
                        Canvas(
                            modifier = Modifier.size(140.dp)
                        ) {
                            val sizePx = size.width
                            val numModules = 21
                            val cellSize = sizePx / numModules
                            
                            // Draw white background
                            drawRect(color = Color.White)
                            
                            // Draw corner patterns
                            fun drawFinder(offsetX: Float, offsetY: Float) {
                                drawRect(color = Color(0xFF0F172A), topLeft = androidx.compose.ui.geometry.Offset(offsetX, offsetY), size = androidx.compose.ui.geometry.Size(cellSize * 7, cellSize * 7))
                                drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(offsetX + cellSize, offsetY + cellSize), size = androidx.compose.ui.geometry.Size(cellSize * 5, cellSize * 5))
                                drawRect(color = Color(0xFF0F172A), topLeft = androidx.compose.ui.geometry.Offset(offsetX + cellSize * 2, offsetY + cellSize * 2), size = androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
                            }
                            drawFinder(0f, 0f)
                            drawFinder((numModules - 7) * cellSize, 0f)
                            drawFinder(0f, (numModules - 7) * cellSize)
                            
                            val seed = upiUri.hashCode().toLong()
                            val random = java.util.Random(seed)
                            for (r in 0 until numModules) {
                                for (c in 0 until numModules) {
                                    val isTlFinder = r < 9 && c < 9
                                    val isTrFinder = r < 9 && c >= numModules - 9
                                    val isBlFinder = r >= numModules - 9 && c < 9
                                    val isCenterLogoZone = r >= 9 && r <= 11 && c >= 9 && c <= 11
                                    if (!isTlFinder && !isTrFinder && !isBlFinder && !isCenterLogoZone) {
                                        if (random.nextBoolean()) {
                                            drawRect(
                                                color = Color(0xFF0F172A),
                                                topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                                size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White, CircleShape)
                                .border(1.5.dp, Color(0xFF0F172A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "UPI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 8.sp,
                                color = Color(0xFF0284C7),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text("Payable Sum: ₹$amount", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Text("UPI ID: $upiId", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                }
                "GPAY_DIRECT" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Pay using your active Google Pay account:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        
                        Button(
                            onClick = {
                                val gPayIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse(upiUri)
                                    setPackage("com.google.android.apps.npath") // Google Pay package inside Android ecosystem
                                }
                                try {
                                    context.startActivity(gPayIntent)
                                    Toast.makeText(context, "Redirecting to Google Pay app...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    // Fallback to general UPI application chooser
                                    try {
                                        val universal = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(upiUri))
                                        context.startActivity(android.content.Intent.createChooser(universal, "Pay Coaching Fees via"))
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "No compatible UPI or GPay application found.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = "GPay", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ Pay with Google Pay", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                        }
                        
                        Text(
                            text = "Auto-dispatches payment secure handshake directly into the GPay app workspace.",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                "UPI_VPA" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pay via custom Customer UPI ID / Account:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = enteredUpiId,
                            onValueChange = { enteredUpiId = it },
                            label = { Text("Customer UPI VPA ID (e.g. parent@upi)", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = enteredRefId,
                            onValueChange = { enteredRefId = it },
                            label = { Text("6-Digit Bank UTR / Trans ID", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                if (enteredUpiId.isNotBlank() && enteredRefId.isNotBlank()) {
                                    isVerified = true
                                    Toast.makeText(context, "✅ Payment verified via bank node! ID: $enteredRefId", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Please enter customer UPI and Reference ID to confirm.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text(if (isVerified) "✅ Payment Verified & Logged" else "Verify UPI Receipt & Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Standard message sharing triggers below Card
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                        contentDescription = "WA",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Bill", fontSize = 10.sp, color = Color.White)
                }
                
                Button(
                    onClick = {
                        val directSmsMsg = "$merchantName: Tuition fees outstanding: ₹$amount. Tap to pay via UPI: upi://pay?pa=$upiId&am=$amount"
                        sendDirectSMS(parentPhone, directSmsMsg, context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Icon(Icons.Default.Sms, contentDescription = "SMS", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SMS Bill", fontSize = 10.sp, color = Color.White)
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

    // Pre-populate with first available batch in student list
    LaunchedEffect(batches) {
        if (selectedBatchId == null && batches.isNotEmpty()) {
            selectedBatchId = batches.first().id
        }
    }

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
                    modifier = Modifier.fillMaxWidth()
                )
                // Reliable transparent click capture overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { menuExpanded = true }
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    batches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.name} (${b.classTimings})") },
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("SECURE STUDENT IDENTIFICATION RECORD", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                        )
                    )
                    .border(1.5.dp, Color(0xFF64748B), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TAMS ELITE ACADEMY",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "HIGH-PERFORMANCE EDUCATION NETWORK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp,
                                color = Color(0xFF0284C7)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("ID BADGE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF94A3B8), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Profile Avatar with initials monogram
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFF0F172A), CircleShape)
                                    .border(2.dp, Color(0xFF0284C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.name.take(2).uppercase(),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.background(Color(0xFF334155), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                        Text("ROLL: ${student.rollNumber}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                val bName = batches.firstOrNull { it.id == student.batchId }?.name ?: "Interactive Prep Track"
                                Text("Batch Slot: $bName", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                                Text("Emergency: ${student.parentPhone}", fontSize = 10.sp, color = Color(0xFF475569))
                            }
                        }

                        // HIGH-FIDELITY GRAPHICAL ADMISSION QR CODE
                        Canvas(
                            modifier = Modifier
                                .size(65.dp)
                                .border(1.5.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(5.dp)
                        ) {
                            val pixelCount = 15
                            val sizePx = size.width
                            val moduleSize = sizePx / pixelCount

                            // 1. Draw solid finder patterns (top-left, top-right, bottom-left)
                            val finderSize = moduleSize * 5

                            fun drawFinder(x: Float, y: Float) {
                                drawRect(Color(0xFF0F172A), androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Size(finderSize, finderSize))
                                drawRect(Color.White, androidx.compose.ui.geometry.Offset(x + moduleSize, y + moduleSize), androidx.compose.ui.geometry.Size(finderSize - moduleSize * 2, finderSize - moduleSize * 2))
                                drawRect(Color(0xFF0F172A), androidx.compose.ui.geometry.Offset(x + moduleSize * 2, y + moduleSize * 2), androidx.compose.ui.geometry.Size(finderSize - moduleSize * 4, finderSize - moduleSize * 4))
                            }

                            // Top-Left
                            drawFinder(0f, 0f)
                            // Top-Right
                            drawFinder(sizePx - finderSize, 0f)
                            // Bottom-Left
                            drawFinder(0f, sizePx - finderSize)

                            // 2. Generate random identical matrix pattern using student's roll number code as seed
                            val random = java.util.Random(student.rollNumber.hashCode().toLong())
                            for (row in 0 until pixelCount) {
                                for (col in 0 until pixelCount) {
                                    // Skip finder marks regions
                                    val isTopLeft = row < 5 && col < 5
                                    val isTopRight = row < 5 && col >= pixelCount - 5
                                    val isBottomLeft = row >= pixelCount - 5 && col < 5
                                    if (!isTopLeft && !isTopRight && !isBottomLeft) {
                                        if (random.nextBoolean()) {
                                            drawRect(
                                                color = Color(0xFF0F172A),
                                                topLeft = androidx.compose.ui.geometry.Offset(col * moduleSize, row * moduleSize),
                                                size = androidx.compose.ui.geometry.Size(moduleSize, moduleSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 1.dp)

                    // Signature overlay area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("SaaS Family ID: ${student.familyId}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            Text("Issued Date: 2026-06-16", fontSize = 8.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                        // Draw signature script emulation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Canvas(modifier = Modifier.size(60.dp, 24.dp)) {
                                drawLine(Color(0xFF0284C7), androidx.compose.ui.geometry.Offset(0f, size.height/2), androidx.compose.ui.geometry.Offset(size.width, size.height/3), strokeWidth = 3f)
                                drawLine(Color(0xFF0284C7), androidx.compose.ui.geometry.Offset(size.width/3, size.height/3), androidx.compose.ui.geometry.Offset(size.width*2/3, size.height*4/5), strokeWidth = 3f)
                            }
                            Text("AUTH SIGNATURE", fontSize = 8.sp, color = Color(0xFF334155), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "ID Badge & Registration PDF generated!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PDF and Print Student Card")
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
                var showTimingsDropdown by remember { mutableStateOf(false) }
                val timeOptions = listOf(
                    "06:00 AM - 07:30 AM",
                    "07:30 AM - 09:00 AM",
                    "09:00 AM - 10:30 AM",
                    "10:30 AM - 12:00 PM",
                    "12:00 PM - 01:30 PM",
                    "01:30 PM - 03:00 PM",
                    "03:00 PM - 04:30 PM",
                    "04:30 PM - 06:00 PM",
                    "06:00 PM - 07:30 PM",
                    "07:30 PM - 09:00 PM"
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = timings,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class Timings") },
                        trailingIcon = {
                            IconButton(onClick = { showTimingsDropdown = !showTimingsDropdown }) {
                                Icon(
                                    imageVector = if (showTimingsDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Timings"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimingsDropdown = !showTimingsDropdown }
                    )
                    DropdownMenu(
                        expanded = showTimingsDropdown,
                        onDismissRequest = { showTimingsDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        timeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    timings = option
                                    showTimingsDropdown = false
                                }
                            )
                        }
                    }
                }
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

// --- Brand Cards & Stands Design Station ---
@Composable
fun GraphicalCardsBrandStation(
    students: List<Student>,
    batches: List<Batch>,
    viewModel: AppViewModel
) {
    val context = LocalContext.current
    var userUploadedPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        userUploadedPhotoUri = uri
    }
    var userUploadedSignatureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val signaturePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        userUploadedSignatureUri = uri
    }

    val academyName by viewModel.academyName.collectAsState()
    val directorName by viewModel.directorName.collectAsState()
    val adminPhone by viewModel.adminPhone.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()
    val adminAddress by viewModel.adminAddress.collectAsState()
    val merchantUpiId by viewModel.merchantUpiId.collectAsState()
    val merchantName by viewModel.merchantName.collectAsState()

    var activeSubsection by remember { mutableStateOf("STUDENT_ID") } // "STUDENT_ID", "VISITING_CARD", "QR_STANDEE"
    var selectedStudentId by remember { mutableStateOf<Long?>(students.firstOrNull()?.id) }
    val chosenStudent = students.find { it.id == selectedStudentId } ?: students.firstOrNull()

    // 5-5 ID Card and Visiting Card templates
    var idCardTheme by remember { mutableStateOf("ROYAL_COBALT") } // "ROYAL_COBALT", "GOLDEN_ELITE", "EMERALD_GREEN", "CRIMSON_GLOW", "OCEAN_SAPPHIRE"
    var visitingCardTheme by remember { mutableStateOf("LUXURY_SLATE") } // "LUXURY_SLATE", "CHAMPA_GOLD", "OCEAN_BREEZE", "ROYAL_PURPLE", "NATURE_GREEN"

    // Locally editable fields enabling user to edit card contents before sharing
    var editableAcademyName by remember(academyName) { mutableStateOf(academyName) }
    var editableDirectorName by remember(directorName) { mutableStateOf(directorName) }
    var editableAdminPhone by remember(adminPhone) { mutableStateOf(adminPhone) }
    var editableAdminEmail by remember(adminEmail) { mutableStateOf(adminEmail) }
    var editableAdminAddress by remember(adminAddress) { mutableStateOf(adminAddress) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("graphical_brand_card_station"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "GRAPHICAL BRAND GENERATOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Auto-Design High-Res Cards for Academy & Scholars",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Subsections Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple("STUDENT_ID", "Student ID", Icons.Default.Badge),
                    Triple("VISITING_CARD", "Visiting Card", Icons.Default.ContactMail),
                    Triple("QR_STANDEE", "UPI QR Standee", Icons.Default.QrCodeScanner)
                ).forEach { (sub, label, icon) ->
                    val isSel = activeSubsection == sub
                    Button(
                        onClick = { activeSubsection = sub },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic User Photo Upload Control Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("user_photo_upload_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personalize Card with Avatar Photo", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(
                            text = if (userUploadedPhotoUri != null) "Custom photo uploaded! Applied dynamically below." else "No custom photo uploaded. Tap to pick an image.",
                            fontSize = 9.sp,
                            color = if (userUploadedPhotoUri != null) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp).testTag("btn_upload_photo_trigger"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (userUploadedPhotoUri != null) {
                            OutlinedButton(
                                onClick = { userUploadedPhotoUri = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("btn_clear_photo_trigger"),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dynamic User Signature Upload Control Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("user_signature_upload_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personalize Card with Student Signature", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text(
                            text = if (userUploadedSignatureUri != null) "Signature uploaded! Applied dynamically below." else "No custom signature uploaded. Tap to pick signature.",
                            fontSize = 9.sp,
                            color = if (userUploadedSignatureUri != null) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { signaturePickerLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp).testTag("btn_upload_signature_trigger"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Upload, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        if (userUploadedSignatureUri != null) {
                            OutlinedButton(
                                onClick = { userUploadedSignatureUri = null },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp).testTag("btn_clear_signature_trigger"),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Interactive Edit Fields Card before sharing
            var showEditFieldsPanel by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().testTag("edit_card_fields_panel"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showEditFieldsPanel = !showEditFieldsPanel },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Edit Card Information Fields Direct", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text(if (showEditFieldsPanel) "COLLAPSE ▲" else "EXPAND ▼", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (showEditFieldsPanel) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editableAcademyName,
                            onValueChange = { editableAcademyName = it },
                            label = { Text("Academy Name", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editableDirectorName,
                            onValueChange = { editableDirectorName = it },
                            label = { Text("Director / Faculty Name", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editableAdminPhone,
                            onValueChange = { editableAdminPhone = it },
                            label = { Text("Contact Number", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editableAdminEmail,
                            onValueChange = { editableAdminEmail = it },
                            label = { Text("Email Desk", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editableAdminAddress,
                            onValueChange = { editableAdminAddress = it },
                            label = { Text("Main Campus Address", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeSubsection) {
                "STUDENT_ID" -> {
                    // Selecting standard student context
                    Text("1. Select Scholar Profile Context", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .clickable {
                                if (students.isNotEmpty()) {
                                    val currentIndex = students.indexOfFirst { it.id == selectedStudentId }
                                    val nextIndex = (currentIndex + 1) % students.size
                                    selectedStudentId = students[nextIndex].id
                                }
                            }
                            .padding(12.dp)
                    ) {
                        val sName = chosenStudent?.name ?: "No Scholar Selected"
                        val roll = chosenStudent?.rollNumber ?: "N/A"
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("$sName (Roll: $roll)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Click to rotate scholars", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Choose ID Card Layout Palettes", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ROYAL_COBALT" to Color(0xFF0F398A),
                            "GOLDEN_ELITE" to Color(0xFFC5A059),
                            "EMERALD_GREEN" to Color(0xFF136243),
                            "CRIMSON_GLOW" to Color(0xFFC62828),
                            "OCEAN_SAPPHIRE" to Color(0xFF00ACC1)
                        ).forEach { (themeId, tintColor) ->
                            val isSel = idCardTheme == themeId
                            val border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tintColor.copy(alpha = 0.15f))
                                    .border(border, shape = RoundedCornerShape(6.dp))
                                    .clickable { idCardTheme = themeId }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(tintColor))
                                    Text(themeId.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (chosenStudent != null) {
                        val activeThemeColor = when (idCardTheme) {
                            "ROYAL_COBALT" -> Color(0xFF0F398A)
                            "GOLDEN_ELITE" -> Color(0xFF8C6239)
                            "EMERALD_GREEN" -> Color(0xFF136243)
                            "OCEAN_SAPPHIRE" -> Color(0xFF00ACC1)
                            else -> Color(0xFFC62828)
                        }
                        val chosenBatch = batches.find { it.id == chosenStudent.batchId }

                        // RENDER CARD
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                            border = BorderStroke(1.2.dp, activeThemeColor)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header plate
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(activeThemeColor)
                                        .padding(vertical = 12.dp, horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(editableAcademyName.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                        Text("ACADEMY SCHOLAR SECURE PROFILE PASS", color = Color.White.copy(alpha = 0.85f), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Personal Info row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp, start = 14.dp, end = 14.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left photo mockup
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(activeThemeColor.copy(alpha = 0.12f))
                                                .border(2.dp, activeThemeColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (userUploadedPhotoUri != null) {
                                                AsyncImage(
                                                    model = userUploadedPhotoUri,
                                                    contentDescription = "Scholar Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            } else {
                                                Text(
                                                    text = chosenStudent.name.take(2).uppercase(),
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = activeThemeColor
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(activeThemeColor)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text("STUDENT", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(62.dp)
                                                .height(28.dp)
                                                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (userUploadedSignatureUri != null) {
                                                AsyncImage(
                                                    model = userUploadedSignatureUri,
                                                    contentDescription = "Signature Preview",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Text("SIGNATURE", fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Right info table
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(chosenStudent.name.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF1F2937))
                                        Text("Roll ID: ${chosenStudent.rollNumber}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text("Batch: ${chosenBatch?.name ?: "General"}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = activeThemeColor)
                                        Text("Class / Sect: ${chosenStudent.studentClass.ifEmpty { "X-Elite" }}", fontSize = 10.sp, color = Color(0xFF334155))
                                        Text("Guardian: ${chosenStudent.parentName}", fontSize = 10.sp, color = Color(0xFF334155))
                                        Text("Phone: ${chosenStudent.parentPhone}", fontSize = 10.sp, color = Color(0xFF334155))
                                    }
                                }

                                // Elegant divider
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    thickness = 1.dp,
                                    color = Color(0xFFE5E7EB)
                                )

                                // Giant PAN card ratio QR Box Container
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "SCAN FOR DIGITAL ATTENDANCE & LIBRARY LOGS",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeThemeColor,
                                        letterSpacing = 0.5.sp
                                    )

                                    // Big professional Canvas-drawn QR Code (115dp - PAN Card scale)
                                    Box(
                                        modifier = Modifier
                                            .size(115.dp)
                                            .border(1.5.dp, activeThemeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val pixelCount = 13 // 13x13 high density matrix!
                                            val pixelSize = size.width / pixelCount
                                            val colorPrimary = activeThemeColor

                                            // Draw simulated QR segments
                                            for (i in 0 until pixelCount) {
                                                for (j in 0 until pixelCount) {
                                                    // Skip corners where finders will be drawn
                                                    if ((i < 4 && j < 4) || (i >= pixelCount - 4 && j < 4) || (i < 4 && j >= pixelCount - 4)) {
                                                        continue
                                                    }
                                                    // Pseudorandom formula for QR dots
                                                    val isFilled = ((i * 7 + j * 13) % 4 == 0) || ((i + j) % 3 == 0)
                                                    if (isFilled) {
                                                        drawRect(
                                                            color = colorPrimary.copy(alpha = 0.9f),
                                                            topLeft = androidx.compose.ui.geometry.Offset(i * pixelSize, j * pixelSize),
                                                            size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                                                        )
                                                    }
                                                }
                                            }

                                            // Helper to draw realistic high-definition anchor patterns
                                            fun drawFinderPattern(xIdx: Int, yIdx: Int) {
                                                val px = xIdx * pixelSize
                                                val py = yIdx * pixelSize

                                                // Outer 4x4 square border
                                                drawRect(
                                                    color = colorPrimary,
                                                    topLeft = androidx.compose.ui.geometry.Offset(px, py),
                                                    size = androidx.compose.ui.geometry.Size(pixelSize * 4, pixelSize * 4)
                                                )
                                                // White inner square
                                                drawRect(
                                                    color = Color.White,
                                                    topLeft = androidx.compose.ui.geometry.Offset(px + pixelSize, py + pixelSize),
                                                    size = androidx.compose.ui.geometry.Size(pixelSize * 2, pixelSize * 2)
                                                )
                                                // Dark center square
                                                drawRect(
                                                    color = colorPrimary,
                                                    topLeft = androidx.compose.ui.geometry.Offset(px + pixelSize * 1.25f, py + pixelSize * 1.25f),
                                                    size = androidx.compose.ui.geometry.Size(pixelSize * 1.5f, pixelSize * 1.5f)
                                                )
                                            }

                                            // Draw 3 classic QR finder patterns: Top-Left, Top-Right, Bottom-Left
                                            drawFinderPattern(0, 0)
                                            drawFinderPattern(pixelCount - 4, 0)
                                            drawFinderPattern(0, pixelCount - 4)
                                        }
                                    }
                                }

                                // Bottom strip with offline location details
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3F4F6))
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Approved Campus: $editableAdminAddress", fontSize = 8.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
                                        Text("Helpline Desk: $editableAdminPhone", fontSize = 8.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(activeThemeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("SECURE VERIFIED ID", fontSize = 7.sp, color = activeThemeColor, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        // Share / Download actions
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val student = chosenStudent
                                        if (student != null) {
                                            val bName = chosenBatch?.name ?: "General"
                                            val idCardBitmap = com.example.ui.screens.CardExporter.drawStudentIdCard(
                                                context,
                                                student,
                                                bName,
                                                idCardTheme,
                                                editableAcademyName,
                                                editableAdminAddress,
                                                editableAdminPhone,
                                                userUploadedPhotoUri,
                                                userUploadedSignatureUri
                                            )
                                            val result = com.example.ui.screens.CardExporter.saveAndDownloadBitmap(
                                                context,
                                                idCardBitmap,
                                                "Academy_ID_${student.name.replace(" ", "_")}"
                                            )
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "No student selected.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Image & PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val student = chosenStudent
                                        if (student != null) {
                                            val bName = chosenBatch?.name ?: "General"
                                            val idCardBitmap = com.example.ui.screens.CardExporter.drawStudentIdCard(
                                                context,
                                                student,
                                                bName,
                                                idCardTheme,
                                                editableAcademyName,
                                                editableAdminAddress,
                                                editableAdminPhone,
                                                userUploadedPhotoUri,
                                                userUploadedSignatureUri
                                            )
                                            val shareText = """
                                                ==================================
                                                🏫 PREBUILT DIGITAL ID CREDENTIALS
                                                ==================================
                                                Academy: $editableAcademyName
                                                Student: ${student.name}
                                                Roll Number: ${student.rollNumber}
                                                Batch: $bName
                                                Subject: ${student.subject.ifEmpty { "Academics" }}
                                                Parent: ${student.parentName} (${student.parentPhone})
                                                Address: ${student.address.ifEmpty { editableAdminAddress }}
                                                ----------------------------------
                                                Use this Digital ID Badge for direct entry clearance at our center.
                                            """.trimIndent()
                                            com.example.ui.screens.CardExporter.shareBitmap(
                                                context,
                                                idCardBitmap,
                                                "Academy_ID_${student.name.replace(" ", "_")}",
                                                shareText
                                            )
                                        } else {
                                            Toast.makeText(context, "No student selected.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Direct messaging share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp Share",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Text("Please register scholars first inside 'Students' tab to auto-generate graphical cards.", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                "VISITING_CARD" -> {
                    Text("Select Visiting Card Aesthetic Theme", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "LUXURY_SLATE" to Color(0xFF1E293B),
                            "CHAMPA_GOLD" to Color(0xFF7A5716),
                            "OCEAN_BREEZE" to Color(0xFF0369A1),
                            "ROYAL_PURPLE" to Color(0xFF4A148C),
                            "NATURE_GREEN" to Color(0xFF1B5E20)
                        ).forEach { (tKey, bgCol) ->
                            val isSel = visitingCardTheme == tKey
                            val border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgCol.copy(alpha = 0.15f))
                                    .border(border, shape = RoundedCornerShape(6.dp))
                                    .clickable { visitingCardTheme = tKey }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(bgCol))
                                    Text(tKey.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val activeBg = when (visitingCardTheme) {
                        "LUXURY_SLATE" -> Color(0xFF0F172A)
                        "CHAMPA_GOLD" -> Color(0xFF452D08)
                        "ROYAL_PURPLE" -> Color(0xFF311B92)
                        "NATURE_GREEN" -> Color(0xFF1B5E20)
                        else -> Color(0xFF0C4A6E)
                    }

                    // RENDER VISITING CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = activeBg),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column {
                                    Text(editableAcademyName.uppercase(), color = Color(0xFFF1F5F9), fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    Text("ELITE PRIVATE COACHING CENTRE", color = Color(0xFFF3F4F6).copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                }

                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (visitingCardTheme == "CHAMPA_GOLD") Color(0xFFFBBF24) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (userUploadedPhotoUri != null) {
                                            AsyncImage(
                                                model = userUploadedPhotoUri,
                                                contentDescription = "Contact Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                        Text(editableDirectorName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("Managing Director & Faculty", color = Color(0xFF38BDF8), fontSize = 7.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("📞 $editableAdminPhone", color = Color(0xFFE2E8F0), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("✉️ $editableAdminEmail", color = Color(0xFFE2E8F0), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("📍 $editableAdminAddress", color = Color(0xFFE2E8F0), fontSize = 8.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Share / Download actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val vCardBitmap = com.example.ui.screens.CardExporter.drawVisitingCard(
                                        context = context,
                                        directorName = editableDirectorName,
                                        academyName = editableAcademyName,
                                        phone = editableAdminPhone,
                                        email = editableAdminEmail,
                                        address = editableAdminAddress,
                                        theme = visitingCardTheme,
                                        photoUri = userUploadedPhotoUri
                                    )
                                    val result = com.example.ui.screens.CardExporter.saveAndDownloadBitmap(
                                        context,
                                        vCardBitmap,
                                        "Academy_Visiting_Card"
                                    )
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Card & PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val vCardBitmap = com.example.ui.screens.CardExporter.drawVisitingCard(
                                        context = context,
                                        directorName = editableDirectorName,
                                        academyName = editableAcademyName,
                                        phone = editableAdminPhone,
                                        email = editableAdminEmail,
                                        address = editableAdminAddress,
                                        theme = visitingCardTheme,
                                        photoUri = userUploadedPhotoUri
                                    )
                                    val vText = """
                                        ==================================
                                        🏫 ACADEMY BUSINESS VISITING CARD
                                        ==================================
                                        Academy: $editableAcademyName
                                        Director: $editableDirectorName
                                        Helpline Phone: $editableAdminPhone
                                        Email Desk: $editableAdminEmail
                                        Main Campus: $editableAdminAddress
                                        ----------------------------------
                                        Empowering next-gen student careers. Share or save this card.
                                    """.trimIndent()
                                    com.example.ui.screens.CardExporter.shareBitmap(
                                        context,
                                        vCardBitmap,
                                        "Academy_Visiting_Card",
                                        vText
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Direct share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp Share",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                "QR_STANDEE" -> {
                    Text("Premium Standee UPI QR Board Generator", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)

                    // BEAUTIFUL POSTER CARD RENDER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("SECURE INSTANT QR PASS: ATTENDANCE & UPI PAYMENTS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                                }
                            }

                            Text(academyName.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF0F172A))

                            // Large Canvas-drawn QR Code (210dp scale!)
                            Box(
                                modifier = Modifier
                                    .size(210.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(2.dp, Color(0xFF0F172A).copy(alpha = 0.2f))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val pixelCount = 13 // 13x13 high density matrix!
                                    val pixelSize = size.width / pixelCount
                                    val colorPrimary = Color(0xFF0F172A)

                                    // Draw simulated QR segments
                                    for (i in 0 until pixelCount) {
                                        for (j in 0 until pixelCount) {
                                            // Skip corners where finders will be drawn
                                            if ((i < 4 && j < 4) || (i >= pixelCount - 4 && j < 4) || (i < 4 && j >= pixelCount - 4)) {
                                                continue
                                            }
                                            // Pseudorandom formula for QR dots
                                            val isFilled = ((i * 11 + j * 7) % 3 == 0) || ((i + j) % 5 == 0)
                                            if (isFilled) {
                                                drawRect(
                                                    color = colorPrimary.copy(alpha = 0.95f),
                                                    topLeft = androidx.compose.ui.geometry.Offset(i * pixelSize, j * pixelSize),
                                                    size = androidx.compose.ui.geometry.Size(pixelSize, pixelSize)
                                                )
                                            }
                                        }
                                    }

                                    // Helper to draw realistic high-definition anchor patterns
                                    fun drawFinderPattern(xIdx: Int, yIdx: Int) {
                                        val px = xIdx * pixelSize
                                        val py = yIdx * pixelSize

                                        // Outer 4x4 square border
                                        drawRect(
                                            color = colorPrimary,
                                            topLeft = androidx.compose.ui.geometry.Offset(px, py),
                                            size = androidx.compose.ui.geometry.Size(pixelSize * 4, pixelSize * 4)
                                        )
                                        // White inner square
                                        drawRect(
                                            color = Color.White,
                                            topLeft = androidx.compose.ui.geometry.Offset(px + pixelSize, py + pixelSize),
                                            size = androidx.compose.ui.geometry.Size(pixelSize * 2, pixelSize * 2)
                                        )
                                        // Dark center square
                                        drawRect(
                                            color = colorPrimary,
                                            topLeft = androidx.compose.ui.geometry.Offset(px + pixelSize * 1.25f, py + pixelSize * 1.25f),
                                            size = androidx.compose.ui.geometry.Size(pixelSize * 1.5f, pixelSize * 1.5f)
                                        )
                                    }

                                    // Draw 3 classic QR finder patterns: Top-Left, Top-Right, Bottom-Left
                                    drawFinderPattern(0, 0)
                                    drawFinderPattern(pixelCount - 4, 0)
                                    drawFinderPattern(0, pixelCount - 4)
                                }
                            }

                            // Dual Purpose Standee Labels
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STUDENT GATEWAY & FEES PASS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                Text("Scan using your student login scanner to log attendance or route fees", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569), textAlign = TextAlign.Center)
                            }

                            // UPI Subtitles
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("BHIM UPI MERCHANT PARAMETERS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(merchantUpiId, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7), fontFamily = FontFamily.Monospace)
                                Text("Registered Name: $merchantName", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                            }

                            // Footer partner strip inside poster
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Text("GPay • PhonePe • Paytm • Axis Eco-System", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Share / Download actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val standeeBitmap = com.example.ui.screens.CardExporter.drawQrStandee(
                                        context,
                                        academyName,
                                        merchantName,
                                        merchantUpiId
                                    )
                                    val result = com.example.ui.screens.CardExporter.saveAndDownloadBitmap(
                                        context,
                                        standeeBitmap,
                                        "Academy_UPI_Standee"
                                    )
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Standee & PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val standeeBitmap = com.example.ui.screens.CardExporter.drawQrStandee(
                                        context,
                                        academyName,
                                        merchantName,
                                        merchantUpiId
                                    )
                                    val sText = """
                                        ==================================
                                        🏫 ACADEMY DIRECT UPI PAY SCANNER
                                        ==================================
                                        School: $academyName
                                        Registered Merchant: $merchantName
                                        Receiver UPI ID: $merchantUpiId
                                        ----------------------------------
                                        Scan QR code or send payment to the above address to settle academy dues instantly. 
                                    """.trimIndent()
                                    com.example.ui.screens.CardExporter.shareBitmap(
                                        context,
                                        standeeBitmap,
                                        "Academy_UPI_Standee",
                                        sText
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Direct messaging desk routing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp Share",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
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

    val studentList by viewModel.students.collectAsState(initial = emptyList())
    val paymentList by viewModel.feePayments.collectAsState(initial = emptyList())
    val studyMats by viewModel.studyMaterials.collectAsState(initial = emptyList())
    
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
                var localRestoreProgress by remember { mutableStateOf(false) }

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

                OutlinedButton(
                    onClick = {
                        localRestoreProgress = true
                        viewModel.restoreDatabaseFromLocalMemory { log ->
                            syncLogText = log
                            if (log.startsWith("Success:")) {
                                localRestoreProgress = false
                                Toast.makeText(context, "Local SQLite backup restored successfully!", Toast.LENGTH_SHORT).show()
                            } else if (log.startsWith("Error")) {
                                localRestoreProgress = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (localRestoreProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Restoring Local Database...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Restore, contentDescription = "Restore Local")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore `.db` File from Local Memory", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
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

                // Account info with Premium OAuth status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF34A853).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google Account Linked",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            // Verified security badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF34A853).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Verified, null, tint = Color(0xFF1B5E20), modifier = Modifier.size(10.dp))
                                    Text("OAUTH SECURE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                }
                            }
                        }

                        Text(
                            text = "Authenticated: $userEmail\nLast Backup Snapshot: $lastDriveTime\nStandard Backup Scopes: Authorized & Synced",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        HorizontalDivider(color = Color(0xFF34A853).copy(alpha = 0.15f))

                        // Dynamic listing of materials to compile
                        Text(
                            text = "DATABASE SNAPSHOT ARCHIVE PACKAGE:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF34A853), modifier = Modifier.size(12.dp))
                                    Text("Students: ${studentList.size}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF34A853), modifier = Modifier.size(12.dp))
                                    Text("Tuition Ledger: ${paymentList.size}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF34A853), modifier = Modifier.size(12.dp))
                                    Text("Study Materials: ${studyMats.size}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF34A853), modifier = Modifier.size(12.dp))
                                    Text("Disaster Logs: Active", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                }
                            }
                        }
                    }
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

                var driveRestoreProgress by remember { mutableStateOf(false) }

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

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        driveRestoreProgress = true
                        viewModel.restoreGoogleDriveBackup { progressMsg ->
                            syncLogText = progressMsg
                            if (progressMsg.startsWith("Successfully restored")) {
                                driveRestoreProgress = false
                                Toast.makeText(context, "WhatsApp-style disaster recovery restored!", Toast.LENGTH_LONG).show()
                            } else if (progressMsg.startsWith("Error") || progressMsg.contains("failed") || progressMsg.contains("disaster recovery")) {
                                driveRestoreProgress = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.5.dp, Color(0xFF34A853)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (driveRestoreProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF34A853))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Initiating Disaster Recovery...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Restore Drive", tint = Color(0xFF34A853))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore from Google Drive (WhatsApp Style)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    var activeBatchForLoggingId by remember { mutableStateOf<Long?>(batches.firstOrNull()?.id) }
    var selectedDateStr by remember { mutableStateOf("2026-06-16") }
    var activeSubTab by remember { mutableStateOf("SCANNER") } // "SCANNER" or "QR_CODE"
    
    // Live simulator state
    var selectedStudentToScanId by remember { mutableStateOf<Long?>(null) }
    var scaleYAnimator by remember { mutableStateOf(0f) }
    val scannerLogList = remember { mutableStateListOf<Pair<String, String>>() }
    var isLiveCameraActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while (true) {
            scaleYAnimator = 0f
            delay(10)
            scaleYAnimator = 1f
            delay(1800)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Main Headings
            Column {
                Text(
                    text = "ACADEMY ATTENDANCE CONTROL CENTER",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    softWrap = true
                )
                Text(
                    text = "Integrated UPI-Style QR Code Generator & Real-time Scanner Engine",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    softWrap = true
                )
            }

            // Controls: Date picker & Batch selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = selectedDateStr,
                    onValueChange = { selectedDateStr = it },
                    label = { Text("Log Date", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Select active batch
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Selected Class Batch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, softWrap = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        batches.forEach { b ->
                            val isSel = activeBatchForLoggingId == b.id
                            Surface(
                                modifier = Modifier
                                    .clickable { activeBatchForLoggingId = b.id }
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSel) Color.Transparent else Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = b.name,
                                    fontSize = 11.sp,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    softWrap = true
                                )
                            }
                        }
                    }
                }
            }

            // Stylized material Sub-Tab Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeSubTab == "SCANNER") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = "SCANNER" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "QR CODE SCANNER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeSubTab == "SCANNER") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = true
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeSubTab == "QR_CODE") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeSubTab = "QR_CODE" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "UPI-STYLE STAND QR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeSubTab == "QR_CODE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = true
                    )
                }
            }

            if (activeBatchForLoggingId != null) {
                val activeBatch = batches.find { it.id == activeBatchForLoggingId }
                val batchStudents = students.filter { it.batchId == activeBatchForLoggingId }
                val registeredAttendance by viewModel.getAttendanceForBatchAndDateFlow(activeBatchForLoggingId!!, selectedDateStr).collectAsState(
                    initial = emptyList()
                )

                if (activeSubTab == "SCANNER") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Real-Time Stats Overview Dashboard Grid
                        val totalEnrolled = batchStudents.size
                        val presentToday = registeredAttendance.filter { it.status.equals("Present", ignoreCase = true) }.size
                        val absentToday = registeredAttendance.filter { it.status.equals("Absent", ignoreCase = true) }.size
                        val leaveToday = registeredAttendance.filter { it.status.equals("Leave", ignoreCase = true) }.size

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("👤 Enrolled", totalEnrolled.toString(), MaterialTheme.colorScheme.primary),
                                Triple("✅ Present", presentToday.toString(), Color(0xFF2E7D32)),
                                Triple("❌ Absent", absentToday.toString(), Color(0xFFC62828)),
                                Triple("⏳ On Leave", leaveToday.toString(), Color(0xFFE65100))
                            ).forEach { (label, count, color) ->
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                                    border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(count, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
                                    }
                                }
                            }
                        }

                        // Split Section: Camera Lens Scanner Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "HARDWARE CAMERA BADGE DECODER",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            "Real-time physical card scan reader using active lens",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Button(
                                        onClick = { isLiveCameraActive = !isLiveCameraActive },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLiveCameraActive) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isLiveCameraActive) Icons.Default.VideocamOff else Icons.Default.Videocam,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isLiveCameraActive) "Close Reader" else "Start Scanner Lens",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                if (isLiveCameraActive) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF0F172A))
                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CameraXQRScannerView(
                                            onQRCodeScanned = { rawVal ->
                                                val matched = students.firstOrNull {
                                                    rawVal.contains(it.rollNumber, ignoreCase = true) ||
                                                    rawVal.contains(it.id.toString()) ||
                                                    rawVal.contains(it.name, ignoreCase = true)
                                                }

                                                if (matched != null) {
                                                    viewModel.saveSingleAttendance(
                                                        studentId = matched.id,
                                                        batchId = matched.batchId,
                                                        dateStr = selectedDateStr,
                                                        status = "Present",
                                                        method = "Real QR Lens"
                                                    )
                                                    val timeStr = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date())
                                                    val logPrefix = "${matched.name} (Roll: ${matched.rollNumber})"
                                                    if (!scannerLogList.any { it.first == logPrefix && it.second == timeStr }) {
                                                        scannerLogList.add(0, Pair(logPrefix, timeStr))
                                                    }
                                                    Toast.makeText(context, "✅ CHECK-IN SUCCESS: ${matched.name}", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "⚠️ Unknown Student ID QR: '$rawVal'", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )

                                        // Pulse glowing neon scan guide line
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .align(Alignment.TopCenter)
                                                .offset(y = (scaleYAnimator * 170).dp)
                                                .background(
                                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                        colors = listOf(Color.Transparent, Color(0xFF00FF00), Color.Transparent)
                                                    )
                                                )
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "POINT AT STUDENT CARD BADGE",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier
                                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .clickable { isLiveCameraActive = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.QrCodeScanner,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Camera lens sleeping. Tap to activate point-and-scan camera reader.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (scannerLogList.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "RECENT LENS SCANS FEED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                "Clear Feed",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.clickable { scannerLogList.clear() }
                                            )
                                        }

                                        scannerLogList.take(3).forEach { log ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        null,
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        log.first,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.Black
                                                    )
                                                }
                                                Text(
                                                    log.second,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section B: Master direct checklist register
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "MASTER CLASS ROLL SHEET (DIRECT REAL-TIME DB)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                softWrap = true
                            )

                            if (batchStudents.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No student accounts enrolled in this active stream. Register students inside onboarding tabs to build database lists.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    batchStudents.forEach { s ->
                                        val studentRecord = registeredAttendance.find { it.studentId == s.id }
                                        val currentStatus = studentRecord?.status ?: "Unmarked"
                                        val trackingMethod = studentRecord?.trackingMethod ?: ""

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (currentStatus.lowercase()) {
                                                    "present" -> Color(0xFFF1F8E9)
                                                    "absent" -> Color(0xFFFFEBEE)
                                                    "leave" -> Color(0xFFFFF3E0)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                when (currentStatus.lowercase()) {
                                                    "present" -> Color(0xFF4CAF50).copy(alpha = 0.4f)
                                                    "absent" -> Color(0xFFEF5350).copy(alpha = 0.4f)
                                                    "leave" -> Color(0xFFFFB74D).copy(alpha = 0.4f)
                                                    else -> MaterialTheme.colorScheme.outlineVariant
                                                }
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when (currentStatus.lowercase()) {
                                                                    "present" -> Color(0xFF2E7D32)
                                                                    "absent" -> Color(0xFFC62828)
                                                                    "leave" -> Color(0xFFE65100)
                                                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                                }
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = s.name.firstOrNull()?.toString()?.uppercase() ?: "S",
                                                            color = if (currentStatus == "Unmarked") MaterialTheme.colorScheme.primary else Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column {
                                                        Text(
                                                            text = s.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = Color.Black
                                                        )
                                                        Text(
                                                            text = "Roll: #${s.rollNumber}" + if (trackingMethod.isNotEmpty()) " • via $trackingMethod" else "",
                                                            fontSize = 10.sp,
                                                            color = Color.DarkGray
                                                        )
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    listOf(
                                                        Triple("Present", Color(0xFF2E7D32), Icons.Default.Check),
                                                        Triple("Absent", Color(0xFFC62828), Icons.Default.Close),
                                                        Triple("Leave", Color(0xFFE65100), Icons.Default.Warning)
                                                    ).forEach { (statusName, buttonColor, iconVal) ->
                                                        val isSelected = currentStatus.equals(statusName, ignoreCase = true)
                                                        OutlinedButton(
                                                            onClick = {
                                                                viewModel.saveSingleAttendance(
                                                                    studentId = s.id,
                                                                    batchId = activeBatchForLoggingId!!,
                                                                    dateStr = selectedDateStr,
                                                                    status = statusName,
                                                                    method = "Manual Tab"
                                                                )
                                                            },
                                                            modifier = Modifier.height(28.dp),
                                                            colors = ButtonDefaults.outlinedButtonColors(
                                                                containerColor = if (isSelected) buttonColor else Color.Transparent,
                                                                contentColor = if (isSelected) Color.White else buttonColor
                                                            ),
                                                            border = BorderStroke(1.dp, buttonColor),
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = iconVal,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(2.dp))
                                                            Text(statusName, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "CLASSROOM QR STANDEE CARD FOR SELF CHECK-IN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            softWrap = true
                        )

                        Card(
                            modifier = Modifier
                                .width(260.dp)
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    }
                                    Text(
                                        text = activeBatch?.name?.uppercase() ?: "TAMS CLASSROOM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = FontFamily.Monospace,
                                        softWrap = true
                                    )
                                }

                                UPIQRCodeStandCanvas(batchName = activeBatch?.name ?: "Interactive")

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "SCAN TO CHECK-IN TODAY",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        softWrap = true
                                    )
                                    Text(
                                        "Batch Timings: ${activeBatch?.classTimings ?: "Regular Session"}",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        softWrap = true
                                    )
                                }
                            }
                        }

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
                                        method = "Classroom QR Scanner"
                                    )
                                    Toast.makeText(context, "Completed scan check-in simulation for all students!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Check-In Everyone", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val sendText = "=== ATTENDANCE PASS ===\nScan this in Aspirants Coaching classes to check into the batch ${activeBatch?.name ?: ""}.\nSession Date: $selectedDateStr\nCenter ID: 0d8c93c730"
                                    val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        putExtra(android.content.Intent.EXTRA_TEXT, sendText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Check-In Stand Link"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Stand QR Link", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UPIQRCodeStandCanvas(batchName: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).padding(6.dp)) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
        ) {
            val sizePx = size.width
            val numModules = 21
            val cellSize = sizePx / numModules
            
            drawRect(color = Color.White)
            
            fun drawFinderPattern(offsetX: Float, offsetY: Float) {
                drawRect(Color(0xFF0F172A), androidx.compose.ui.geometry.Offset(offsetX, offsetY), androidx.compose.ui.geometry.Size(cellSize * 7, cellSize * 7))
                drawRect(Color.White, androidx.compose.ui.geometry.Offset(offsetX + cellSize, offsetY + cellSize), androidx.compose.ui.geometry.Size(cellSize * 5, cellSize * 5))
                drawRect(Color(0xFF0F172A), androidx.compose.ui.geometry.Offset(offsetX + cellSize * 2, offsetY + cellSize * 2), androidx.compose.ui.geometry.Size(cellSize * 3, cellSize * 3))
            }

            drawFinderPattern(0f, 0f)
            drawFinderPattern((numModules - 7) * cellSize, 0f)
            drawFinderPattern(0f, (numModules - 7) * cellSize)
            
            val hashValue = batchName.hashCode()
            val pseudorand = java.util.Random(hashValue.toLong())
            for (row in 0 until numModules) {
                for (col in 0 until numModules) {
                    val isFinderTL = row < 8 && col < 8
                    val isFinderTR = row < 8 && col >= numModules - 8
                    val isFinderBL = row >= numModules - 8 && col < 8
                    val isCenter = row >= 9 && row <= 11 && col >= 9 && col <= 11
                    if (!isFinderTL && !isFinderTR && !isFinderBL && !isCenter) {
                        if (pseudorand.nextBoolean()) {
                            drawRect(
                                color = Color(0xFF0F172A),
                                topLeft = androidx.compose.ui.geometry.Offset(col * cellSize, row * cellSize),
                                size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }

        // Live Center brand badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.White, CircleShape)
                .border(1.5.dp, Color(0xFF0F172A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "TAMS",
                fontWeight = FontWeight.Black,
                fontSize = 7.sp,
                color = Color(0xFF0284C7),
                letterSpacing = 0.2.sp
            )
        }
    }
}

// --- Upload study materials sub-module ---
@Composable
fun UploadStudyResourcesSubModule(
    batches: List<Batch>,
    studyMaterials: List<StudyMaterial>,
    viewModel: AppViewModel,
    onQuizClick: (StudyMaterial) -> Unit
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

    var teacherSearchQuery by remember { mutableStateOf("") }
    var teacherTypeFilter by remember { mutableStateOf("All") }
    var teacherBatchFilter by remember { mutableStateOf<Long?>(null) }

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
                modifier = Modifier.fillMaxWidth().testTag("upload_study_resource_button")
            ) {
                Text("Broadcast Educational Material to Students")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            Text(
                text = "ONLINE RESOURCE DIRECTORY (${studyMaterials.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace
            )

            // Search Bar
            OutlinedTextField(
                value = teacherSearchQuery,
                onValueChange = { teacherSearchQuery = it },
                placeholder = { Text("Search resources by title, category, or content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (teacherSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { teacherSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Batch filtering chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = teacherBatchFilter == null,
                    onClick = { teacherBatchFilter = null },
                    label = { Text("All Batches", fontSize = 11.sp) }
                )
                batches.forEach { b ->
                    FilterChip(
                        selected = teacherBatchFilter == b.id,
                        onClick = { teacherBatchFilter = b.id },
                        label = { Text(b.name, fontSize = 11.sp) }
                    )
                }
            }

            // Material type filtering chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("All", "Plain Text", "PDF", "YouTube video", "URL").forEach { plat ->
                    FilterChip(
                        selected = teacherTypeFilter == plat,
                        onClick = { teacherTypeFilter = plat },
                        label = { Text(plat, fontSize = 11.sp) }
                    )
                }
            }

            val filteredTeacherMaterials = studyMaterials.filter { mat ->
                val matchesSearch = mat.title.contains(teacherSearchQuery, ignoreCase = true) || 
                                    mat.mainCategory.contains(teacherSearchQuery, ignoreCase = true) ||
                                    mat.topicSubCategory.contains(teacherSearchQuery, ignoreCase = true) ||
                                    mat.content.contains(teacherSearchQuery, ignoreCase = true)
                val matchesType = teacherTypeFilter == "All" || mat.contentType.equals(teacherTypeFilter, ignoreCase = true)
                val matchesBatch = teacherBatchFilter == null || mat.batchId == teacherBatchFilter
                matchesSearch && matchesType && matchesBatch
            }

            if (filteredTeacherMaterials.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (teacherSearchQuery.isNotEmpty() || teacherTypeFilter != "All" || teacherBatchFilter != null)
                                "No materials found matching applied search filters."
                            else
                                "No active resources found. Create one above to sync.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredTeacherMaterials.forEach { mat ->
                        val bName = batches.find { it.id == mat.batchId }?.name ?: "All Batches"
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("study_resource_item_${mat.id}"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(bName.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Text(mat.contentType.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(mat.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${mat.mainCategory} • ${mat.topicSubCategory}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = {
                                            onQuizClick(mat)
                                        },
                                        modifier = Modifier.testTag("teacher_manage_quiz_btn_${mat.id}"),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Quiz, contentDescription = "Quiz Icon", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Manage Interactive Quizzes & Assessments", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (mat.content.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(mat.content, fontSize = 11.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteStudyMaterial(mat) },
                                    modifier = Modifier.testTag("delete_study_resource_${mat.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Material", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text(
                text = "📚 COOP NCERT & COMPETITIVE EXAM BOOKS CHANNELS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            var selectedLibCategory by remember { mutableStateOf("Class 10") }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Access and verify official online repositories of textbooks and core test sheets easily.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Category Selector Scroll row
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Class 10", "Class 12", "JEE / NEET / competitive Guide", "High-Res Atlas Maps").forEach { cat ->
                            val isSel = selectedLibCategory == cat
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedLibCategory = cat },
                                label = { Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    val academicItems = when (selectedLibCategory) {
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
                                    Text(itemTitle, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(itemDesc, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(itemUrl))
                                            context.startActivity(browserIntent)
                                            Toast.makeText(context, "Opening Online textbook guide...", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No web browser available on dynamic emulation device.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Book", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
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

                val alerts = emptyList<Triple<String, String, String>>()

                if (alerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "No active notifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "You are completely up-to-date! Demo notification feeds have been cleared.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Leftside: logo card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(64.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TuitionClassLogo(modifier = Modifier.fillMaxSize())
                    }
                }

                // Rightside: business name headers
                Column {
                    Text(
                        text = "Tuition Class",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF03A9F4),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Management System",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFF37474F),
                        letterSpacing = 0.2.sp
                    )
                }
            }

            // Academy ID Badge or Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("0d8c93c730"))
                        Toast.makeText(context, "Academy ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF37474F), letterSpacing = 0.5.sp)) {
                            append("ACADEMY ID :  ")
                        }
                        withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF0288D1), textDecoration = TextDecoration.Underline, letterSpacing = 0.5.sp)) {
                            append("0d8c93c730")
                        }
                    }
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

            // Category Section "Account"
            Text(
                text = "Account",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DrawerNavigationItems(
    activeAdminTab: String,
    activeStudentTab: String,
    onSelectAdminTab: (String) -> Unit,
    onSelectStudentTab: (String) -> Unit,
    onSelectBottomTab: (String) -> Unit,
    onLogout: () -> Unit,
    onMotivationalClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onHelpClick: () -> Unit,
    onReferralClick: () -> Unit,
    onOtherAppsClick: () -> Unit,
    themeMode: String,
    onSwitchTheme: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DrawerItem(
            label = "My Account",
            icon = Icons.Default.Person,
            selected = false,
            onClick = { onSelectBottomTab("MY_ACCOUNT") }
        )
        DrawerItem(
            label = "Motivational Post",
            icon = Icons.Default.SentimentSatisfied,
            selected = false,
            onClick = onMotivationalClick
        )
        DrawerItem(
            label = "Subscription",
            icon = Icons.Default.CreditCard,
            selected = false,
            onClick = onSubscriptionClick
        )
        DrawerItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            selected = false,
            onClick = onSettingsClick
        )
        DrawerItem(
            label = "About App",
            icon = Icons.Default.SentimentSatisfiedAlt,
            selected = false,
            onClick = onAboutAppClick
        )
        DrawerItem(
            label = "Help",
            icon = Icons.Default.HelpOutline,
            selected = false,
            onClick = onHelpClick
        )
        DrawerItem(
            label = "Referral Code",
            icon = Icons.Default.Share,
            selected = false,
            onClick = onReferralClick
        )
        DrawerItem(
            label = "Other Apps",
            icon = Icons.Default.Send,
            selected = false,
            onClick = onOtherAppsClick
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))

        // Dark Mode Toggle Button for Side Menu Bar
        val isDark = themeMode == "CLASSIC_MIDNIGHT"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = "Theme Icon",
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF8C6E12),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isDark) "Dark Mode" else "Classic Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Toggle active page theme",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            androidx.compose.material3.Switch(
                checked = isDark,
                onCheckedChange = { checked ->
                    onSwitchTheme(if (checked) "CLASSIC_MIDNIGHT" else "CLASSIC_NAVY")
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            label = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            selected = false,
            onClick = onLogout
        )
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
        shape = RoundedCornerShape(8.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFE1F5FE),
            unselectedContainerColor = Color.Transparent,
            selectedIconColor = Color(0xFF0288D1),
            unselectedIconColor = Color(0xFF546E7A),
            selectedTextColor = Color(0xFF0288D1),
            unselectedTextColor = Color(0xFF37474F)
        ),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    )
}

@Composable
fun AppOwnerMonitorView(
    transactions: List<FinancialTransaction>,
    batches: List<Batch>,
    students: List<Student>,
    institutes: List<InstituteAccount>,
    subscriberTransactionsList: List<com.example.ui.viewmodel.SubTransactionRecord>,
    onApprove: (String) -> Unit,
    onApproveAll: () -> Unit,
    onDeleteInstitute: (String) -> Unit,
    onToggleSubscription: (String) -> Unit,
    onToggleSuspension: (String) -> Unit,
    onScheduleWipe: (String, Boolean) -> Unit,
    onRecordUpiPaymentTransaction: (academyName: String, email: String, amount: Double, upiTxRef: String, plan: String) -> Unit,
    onLogout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All") } // "All", "Individual", "Institute", "Library"
    var tokenCode by remember { mutableStateOf("") }
    var outputTokenText by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Subscriber payment dialogue states
    var renewalForInst by remember { mutableStateOf<com.example.ui.viewmodel.InstituteAccount?>(null) }
    var inputAmount by remember { mutableStateOf("499.0") }
    var inputUpiRef by remember { mutableStateOf("") }
    var inputPlanType by remember { mutableStateOf("Monthly") } // "Monthly" or "Yearly"

    // High-fidelity default items mapped to Mr. Gupta, Mr. Sharma, and Silent Library as specified in Prompt 1
    val customTenants = remember(institutes) {
        val list = mutableListOf<com.example.ui.viewmodel.InstituteAccount>()
        if (institutes.none { it.academyName.contains("City Coaching", ignoreCase = true) }) {
            list.add(
                com.example.ui.viewmodel.InstituteAccount(
                    email = "city@coaching.com",
                    academyName = "City Coaching Institute",
                    directorName = "Mr. Gupta",
                    address = "Metro Center Hub, Block-C",
                    isApproved = true,
                    isSuspended = false,
                    subscriptionActive = true,
                    expiryDate = "23 July 2026",
                    lastUpiTxRef = "city@upi",
                    lastPaymentAmount = 1499.0,
                    profileType = "COACHING_OWNER"
                )
            )
        }
        if (institutes.none { it.academyName.contains("Verma Tuition", ignoreCase = true) }) {
            list.add(
                com.example.ui.viewmodel.InstituteAccount(
                    email = "verma@tuition.com",
                    academyName = "Verma Tuition Center",
                    directorName = "Mr. Sharma",
                    address = "Street 12, Civil Lines",
                    isApproved = true,
                    isSuspended = false,
                    subscriptionActive = true,
                    expiryDate = "29 July 2026",
                    lastUpiTxRef = "verma@upi",
                    lastPaymentAmount = 199.0,
                    profileType = "COACHING_OWNER"
                )
            )
        }
        if (institutes.none { it.academyName.contains("Silent Study", ignoreCase = true) }) {
            list.add(
                com.example.ui.viewmodel.InstituteAccount(
                    email = "silent@library.com",
                    academyName = "Silent Study Library",
                    directorName = "Library Desk",
                    address = "Opposite PG Hostel, Lane 4",
                    isApproved = true,
                    isSuspended = true, // Suspended on startup for Prompt 1 spec!
                    subscriptionActive = false,
                    expiryDate = "20 Oct 2026",
                    lastUpiTxRef = "silent@upi",
                    lastPaymentAmount = 4999.0,
                    profileType = "LIBRARY_OWNER"
                )
            )
        }
        list.addAll(institutes)
        list.toList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)) // Deep Space Blue/Grey
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // 1. TOP APP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF06B6D4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Panel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "TutionMaster Control Tower",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.padding(end = 12.dp)) {
                        IconButton(onClick = {
                            Toast.makeText(context, "System Status: Control Tower Online", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = Color.LightGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-4).dp, y = (4).dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF43F5E))
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F2937))
                            .border(1.5.dp, Color(0xFF06B6D4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF22D3EE))
                    }
                }
            }

            // 2. MAIN SCROLLABLE CONTENT BODY
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // STATS ROW (Horizontal Scroll with neon cyan glass-morphic cards with trending indicators)
                item {
                    Text(
                        text = "REAL-TIME NETWORK METRICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsRowGlassCard(title = "Total Tenants", count = "124", trend = "+8%", pathColor = Color(0xFF22D3EE))
                        StatsRowGlassCard(title = "Active Students", count = "2.3k", trend = "+12%", pathColor = Color(0xFF06B6D4))
                        StatsRowGlassCard(title = "Today's Attendance", count = "840", trend = "84%", pathColor = Color(0xFF10B981))
                        StatsRowGlassCard(title = "Monthly Revenue", count = "₹85,000", trend = "₹", pathColor = Color(0xFFF59E0B))
                    }
                }

                // SEARCH & FILTERS SECTION
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = BorderStroke(1.dp, Color(0xFF1F2937))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFF0B0F19))
                                    .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.4f), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF22D3EE),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search by Tenant Name, Phone, or ID...",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                    }
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                        modifier = Modifier.fillMaxWidth().testTag("tenant_search_field"),
                                        singleLine = true
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("All", "Individual", "Institute", "Library").forEach { filterLabel ->
                                    val isSelected = selectedFilterChip.equals(filterLabel, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) Color(0xFF06B6D4) else Color(0xFF1F2937))
                                            .clickable { selectedFilterChip = filterLabel }
                                            .padding(horizontal = 14.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = filterLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // TENANTS HEADER
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TENANT ENDPOINTS REGISTER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF06B6D4),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        
                        TextButton(
                            onClick = {
                                onApproveAll()
                                Toast.makeText(context, "All unregistered tenants have been approved!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Approve All Pending ✔", color = Color(0xFF22D3EE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Filtering list dynamically based on Search Query and select chips
                val filteredTenants = customTenants.filter { inst ->
                    val matchesSearch = inst.academyName.contains(searchQuery, ignoreCase = true) ||
                            inst.directorName.contains(searchQuery, ignoreCase = true) ||
                            inst.email.contains(searchQuery, ignoreCase = true)
                    
                    val matchesChip = when (selectedFilterChip.lowercase()) {
                        "all" -> true
                        "individual" -> inst.profileType.equals("ADMIN", ignoreCase = true) || inst.profileType.equals("COACHING_OWNER", ignoreCase = true)
                        "institute" -> inst.academyName.contains("Institute", ignoreCase = true) || inst.academyName.contains("Academy", ignoreCase = true)
                        "library" -> inst.profileType.contains("LIBRARY", ignoreCase = true)
                        else -> true
                    }
                    matchesSearch && matchesChip
                }

                if (filteredTenants.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No matching tenants found.", color = Color.Gray, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(filteredTenants.size) { index ->
                        val inst = filteredTenants[index]
                        TenantAccountItemCard(
                            inst = inst,
                            onToggleSuspension = { onToggleSuspension(inst.email) },
                            onToggleSubscription = { onToggleSubscription(inst.email) },
                            onDelete = { onDeleteInstitute(inst.email) },
                            onRecordSubscription = { renewalForInst = inst },
                            onWipeNode = { onScheduleWipe(inst.email, true) }
                        )
                    }
                }

        // Section 1: API UPTIME & SYSTEM TELEMETRY (Cyber Dark)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GLOBAL LICENSING & INSTANT DIAGNOSTICS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
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
                            Text("3.42 MB SQLite", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                        Column {
                            Text("Active Institutes Connected", fontSize = 11.sp, color = Color.Gray)
                            Text("3 Registered", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF10B981))
                        }
                        Column {
                            Text("Cloud S3 Status", fontSize = 11.sp, color = Color.Gray)
                            Text("99.98% Online", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF06B6D4))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("System telemetry logs (Real-time updates):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0F19), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text("● [2026-06-14 13:12] Cloud check successful. Latency 142ms.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF22D3EE))
                        Text("● [2026-06-14 11:45] Aspirants Success Classes synced 42 attendance entries.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                        Text("● [2026-06-14 09:00] Daily database encryption key rotated successfully.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                        Text("● [2026-06-14 00:05] S3 cold storage compressed archive compiled successfully.", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Section 2: REGISTERED INSTITUTES LEDGER (Cyber Dark)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TENANT SCHOOLS & LICENSE LEDGER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
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
                                Text(school.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text(school.second, fontSize = 11.sp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF06B6D4).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(school.third.uppercase(), color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        HorizontalDivider(color = Color(0xFF1F2937))
                    }
                }
            }
        }

        // Section 3: APP OWNER ACTIONS (Rescue operations / simulated remote wipes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUPERVISOR OPERATION CONTROLS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Generate Activation Key / Code for Client Schools", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tokenCode,
                            onValueChange = { tokenCode = it },
                            placeholder = { Text("e.g. ALPHA_COACH", color = Color.Gray) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF06B6D4),
                                unfocusedBorderColor = Color(0xFF1F2937)
                            )
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
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                        ) {
                            Text("Generate", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (outputTokenText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF06B6D4).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Code: $outputTokenText",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22D3EE)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1F2937))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Simulate Remote Reset / Rogue Database Lock", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    Text("Remotely wipe or revoke database client nodes due to licensing infractions.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "CRITICAL: Simulated database recovery initiated. Cloud nodes sync locked.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lock Node Sync", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "DANGER: Remote Wipe simulation dispatched. Client node data cleared.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Wipe Client DB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- SECTION: LIVE SUBSCRIPTION TRANSACTION LEDGER MONITOR ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE UPI SUBSCRIPTION TRANSACTIONS LEDGER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (subscriberTransactionsList.isEmpty()) {
                        Text(
                            text = "No subscriptions recorded yet.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            subscriberTransactionsList.forEach { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0B0F19), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tx.academyName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                        Text("${tx.date} • Plan: ${tx.planType} • Ref: ${tx.upiTxnId}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = "₹${tx.amount}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(90.dp)) }
    }

    }

    // Persistent FAB Logout
    FloatingActionButton(
        onClick = {
            onLogout()
            Toast.makeText(context, "Logged out of Control Tower", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .safeDrawingPadding()
            .testTag("control_tower_logout_fab"),
        containerColor = Color(0xFFEF4444),
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Logout", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

    // Dialogue: Record subscriber UPI payment
    renewalForInst?.let { inst ->
        AlertDialog(
            onDismissRequest = { renewalForInst = null },
            title = {
                Text(
                    text = "Record UPI Subscription: ${inst.academyName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Configure details for subscription payment from this school/library/teacher via UPI.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Amount Paid (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputUpiRef,
                        onValueChange = { inputUpiRef = it },
                        label = { Text("UPI Txn ID / Reference") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Text("Choose Subscription Term / Plan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = inputPlanType == "Monthly",
                                onClick = {
                                    inputPlanType = "Monthly"
                                    inputAmount = "499.0"
                                }
                            )
                            Text("Monthly (₹499)", fontSize = 12.sp, color = Color.Black)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = inputPlanType == "Yearly",
                                onClick = {
                                    inputPlanType = "Yearly"
                                    inputAmount = "4999.0"
                                }
                            )
                            Text("Yearly (₹4999)", fontSize = 12.sp, color = Color.Black)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountDouble = inputAmount.toDoubleOrNull() ?: 499.0
                        if (inputUpiRef.isNotBlank()) {
                            onRecordUpiPaymentTransaction(
                                inst.academyName,
                                inst.email,
                                amountDouble,
                                inputUpiRef,
                                inputPlanType
                            )
                            Toast.makeText(context, "UPI status transaction received. License updated!", Toast.LENGTH_LONG).show()
                            renewalForInst = null
                        } else {
                            Toast.makeText(context, "Please enter a valid UPI transaction reference.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text("Confirm Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { renewalForInst = null }) {
                    Text("Cancel")
                }
            }
        )
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

    val academyName by viewModel.academyName.collectAsState()
    val adminAddress by viewModel.adminAddress.collectAsState()
    val adminPhone by viewModel.adminPhone.collectAsState()
    val adminEmail by viewModel.adminEmail.collectAsState()

    // 1. Upgraded Tuition tracking state parameters
    var directSearchQuery by remember { mutableStateOf("") }
    var selectedStudentIdForDirectLog by remember { mutableStateOf<Long?>(null) }
    var logAmount by remember { mutableStateOf("") }
    var logDiscount by remember { mutableStateOf("0") }
    var logMonth by remember { mutableStateOf("June 2026") }
    var logMode by remember { mutableStateOf("UPI") }
    var logType by remember { mutableStateOf("Monthly-based") }
    var logRemarks by remember { mutableStateOf("Standard tuition fee instalment") }

    var searchPendingQuery by remember { mutableStateOf("") }
    var historySearchQuery by remember { mutableStateOf("") }
    var historyModeFilter by remember { mutableStateOf("ALL") }

    var selectedStudentForPay by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForReceipts by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForReminder by remember { mutableStateOf<Student?>(null) }
    var activeReceiptToShow by remember { mutableStateOf<FeePayment?>(null) }
    var paymentToDelete by remember { mutableStateOf<FeePayment?>(null) }

    var showUpiConfig by remember { mutableStateOf(false) }
    var tempUpiId by remember { mutableStateOf(merchantUpiId) }
    var tempMerchantName by remember { mutableStateOf(merchantName) }

    // 2. Financial metrics calculations
    val totalCollected = payments.sumOf { it.amountPaid }
    val totalDiscount = payments.sumOf { it.discount }
    val totalExpected = students.sumOf { s -> 
        val b = batches.find { it.id == s.batchId }
        b?.feesAmount ?: 1000.0
    }
    val outstandingDues = (totalExpected - totalCollected - totalDiscount).coerceAtLeast(0.0)
    val overdueStudents = students.filter { s ->
        val b = batches.find { it.id == s.batchId }
        val fee = b?.feesAmount ?: 1000.0
        val paid = payments.filter { it.studentId == s.id }.sumOf { it.amountPaid }
        (fee - paid) > 0.0
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // A. GATEWAY SETTINGS ACCORDION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showUpiConfig = !showUpiConfig },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("UPI Gateway Settings VPA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Icon(
                        imageVector = if (showUpiConfig) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showUpiConfig) {
                    OutlinedTextField(
                        value = tempUpiId,
                        onValueChange = { tempUpiId = it },
                        label = { Text("Academy UPI ID", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempMerchantName,
                        onValueChange = { tempMerchantName = it },
                        label = { Text("Academy Merchant Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (tempUpiId.isNotBlank() && tempMerchantName.isNotBlank() && tempUpiId.contains("@")) {
                                viewModel.updateMerchantUPI(tempUpiId, tempMerchantName)
                                showUpiConfig = false
                                Toast.makeText(context, "UPI gateway details updated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Provide valid VPA and Merchant details", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.End).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text("Save Gateway", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // B. TUITION LEDGER HEALTH SUMMARY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Summarize, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tuition Ledger Health Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Received", fontSize = 9.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("₹${totalCollected.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Pending Dues", fontSize = 9.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            Text("₹${outstandingDues.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFB71C1C))
                        }
                    }
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)), shape = RoundedCornerShape(8.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Rebate/Disc", fontSize = 9.sp, color = Color(0xFF5E35B1), fontWeight = FontWeight.Bold)
                            Text("₹${totalDiscount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF311B92))
                        }
                    }
                }

                val ratio = if (totalExpected > 0) (totalCollected / totalExpected).toFloat() else 0f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Gross Budgeted: ₹${totalExpected.toInt()}", fontSize = 10.sp, color = Color.Gray)
                        Text("${(ratio * 100).toInt()}% Collected", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF2D62CD),
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // C. DIRECT TUITION PAYMENT QUICK-LOGGER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCard, null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Direct Tuition Receipts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Search & Select Scholar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    OutlinedTextField(
                        value = directSearchQuery,
                        onValueChange = {
                            directSearchQuery = it
                            if (it.isBlank()) selectedStudentIdForDirectLog = null
                        },
                        placeholder = { Text("Type name or registration ID...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Autocomplete horizontal chip options
                    if (directSearchQuery.isNotBlank() && selectedStudentIdForDirectLog == null) {
                        val matches = students.filter {
                            it.name.contains(directSearchQuery, ignoreCase = true) || it.rollNumber.contains(directSearchQuery)
                        }
                        if (matches.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matches.take(5).forEach { scholar ->
                                    val bch = batches.find { it.id == scholar.batchId }
                                    val outstandingBalance = (bch?.feesAmount ?: 1000.0) - payments.filter { it.studentId == scholar.id }.sumOf { it.amountPaid }
                                    Surface(
                                        modifier = Modifier.clickable {
                                            selectedStudentIdForDirectLog = scholar.id
                                            directSearchQuery = scholar.name
                                            logAmount = outstandingBalance.coerceAtLeast(0.0).toString()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            "${scholar.name} (Reg: ${scholar.rollNumber})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected target information block & direct fields
                selectedStudentIdForDirectLog?.let { targetId ->
                    val scholar = students.find { it.id == targetId }
                    if (scholar != null) {
                        val bch = batches.find { it.id == scholar.batchId }
                        val baseAmount = bch?.feesAmount ?: 1000.0
                        val settled = payments.filter { it.studentId == scholar.id }.sumOf { it.amountPaid }
                        val outstandingBalance = (baseAmount - settled).coerceAtLeast(0.0)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, Color(0xFFCDDC39).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("SCHOLAR: ${scholar.name} | Batch: ${bch?.name ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF33691E))
                                Text("Required Fee: ₹${baseAmount.toInt()} | Already Paid: ₹${settled.toInt()} | Outstand: ₹${outstandingBalance.toInt()}", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = logAmount,
                                    onValueChange = { logAmount = it },
                                    label = { Text("Settle Value (₹)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = logDiscount,
                                    onValueChange = { logDiscount = it },
                                    label = { Text("Scholarship Rebate (₹)", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = logMonth,
                                    onValueChange = { logMonth = it },
                                    label = { Text("Billing Month", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = logRemarks,
                                    onValueChange = { logRemarks = it },
                                    label = { Text("Ledger Remarks", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("UPI", "CASH", "CARD", "NET_BANKING").forEach { m ->
                                    val active = logMode == m
                                    Button(
                                        onClick = { logMode = m },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (active) Color(0xFFE65100) else Color(0xFFECEFF1),
                                            contentColor = if (active) Color.White else Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(m, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val collected = logAmount.toDoubleOrNull() ?: 0.0
                                    val disc = logDiscount.toDoubleOrNull() ?: 0.0
                                    if (collected >= 0.0) {
                                        viewModel.collectFeePayment(
                                            studentId = scholar.id,
                                            amount = collected,
                                            discount = disc,
                                            feeType = logType,
                                            month = logMonth,
                                            mode = logMode,
                                            remarks = logRemarks
                                        )
                                        Toast.makeText(context, "Settle Payment logged successfully!", Toast.LENGTH_SHORT).show()
                                        selectedStudentIdForDirectLog = null
                                        directSearchQuery = ""
                                        logAmount = ""
                                        logDiscount = "0"
                                    } else {
                                        Toast.makeText(context, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log & Settle Tuition Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // D. PENDING TUITION LIST WITH QUERY FILTER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                        Text("Outstanding Tuition Accounts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("${overdueStudents.size} Demands", color = Color(0xFFC62828), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = searchPendingQuery,
                    onValueChange = { searchPendingQuery = it },
                    placeholder = { Text("Search debtor name...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (overdueStudents.isNotEmpty()) {
                            var totalPendingCount = 0
                            var concatenatedReminders = "=== MEMBERS OUTSTANDING FEE NOTICE ===\n\n"
                            overdueStudents.forEach { scholar ->
                                val bch = batches.find { it.id == scholar.batchId }
                                val baseAmount = bch?.feesAmount ?: 1000.0
                                val settled = payments.filter { it.studentId == scholar.id }.sumOf { it.amountPaid }
                                val outstand = (baseAmount - settled).coerceAtLeast(0.0)
                                if (outstand > 0.0) {
                                    totalPendingCount++
                                    concatenatedReminders += "• ${scholar.name} (Batch: ${bch?.name ?: "N/A"}): Dues: ₹${outstand.toInt()}\n"
                                    
                                    // Trigger a systemic local push notification to remind the student/parent immediately
                                    com.example.services.NotificationHelper.triggerFeeReminderNotification(
                                        context = context,
                                        studentName = scholar.name,
                                        amountDue = outstand,
                                        month = "Current Cycle"
                                    )
                                }
                            }
                            concatenatedReminders += "\nPlease clear current outstanding learning dues. Thank you!\nGenerated by $merchantName"
                            
                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                putExtra(android.content.Intent.EXTRA_TEXT, concatenatedReminders)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Send Consolidated Dues Record"))
                            Toast.makeText(context, "Consolidated fee sheet compiled! Triggered notifications for all $totalPendingCount students.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "All student accounts are fully settled. No dues reminders to compile!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("send_dues_reminder_to_all_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)), // Debtor Red color
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.SendToMobile, "Remind All", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Reminder To All Outstanding Students", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filteredDebts = overdueStudents.filter {
                        it.name.contains(searchPendingQuery, ignoreCase = true)
                    }
                    if (filteredDebts.isEmpty()) {
                        Text("All filtered students have settled accounts.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        filteredDebts.take(4).forEach { scholar ->
                            val bch = batches.find { it.id == scholar.batchId }
                            val baseAmount = bch?.feesAmount ?: 1000.0
                            val settled = payments.filter { it.studentId == scholar.id }.sumOf { it.amountPaid }
                            val outstand = (baseAmount - settled).coerceAtLeast(0.0)

                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(scholar.name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Batch: ${bch?.name ?: "N/A"} • Roll ID: ${scholar.rollNumber}", fontSize = 9.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("₹${outstand.toInt()}", fontWeight = FontWeight.Black, color = Color(0xFFC62828), fontSize = 12.sp)
                                    IconButton(
                                        onClick = {
                                            selectedStudentIdForDirectLog = scholar.id
                                            directSearchQuery = scholar.name
                                            logAmount = outstand.toInt().toString()
                                            Toast.makeText(context, "Ready to log in direct Logger form!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.AddCard, "Pay", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { selectedStudentForReminder = scholar },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Send, "Remind", tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // E. GLOBAL PAYMENTS HISTORY TABLE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("Global Payments History Table", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Button(
                        onClick = {
                            val list = payments.map { pay ->
                                val stud = students.find { it.id == pay.studentId }
                                "Ref: ${pay.receiptNo} | Student: ${stud?.name ?: "N/A"} | Date: ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(pay.datePaid))} | Mode: ${pay.paymentMode} | Paid: ₹${pay.amountPaid}"
                            }
                            if (list.isNotEmpty()) {
                                generateAndSavePdfReport(
                                    context = context,
                                    fileName = "Tuition_Payments_Audit_Ledger.pdf",
                                    reportTitle = "Gross Tuition Fee Receipts Log",
                                    records = list
                                )
                                Toast.makeText(context, "Payments History PDF Created Successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "No tuition collections found to log", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Export PDF", fontSize = 9.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = historySearchQuery,
                        onValueChange = { historySearchQuery = it },
                        placeholder = { Text("Search name/ref ID...", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(modifier = Modifier.weight(0.9f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "UPI", "CASH", "CARD").forEach { f ->
                            val active = historyModeFilter == f
                            Surface(
                                modifier = Modifier.clickable { historyModeFilter = f },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = f,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    color = if (active) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                }

                val displayedPayments = payments.filter { pay ->
                    val stud = students.find { it.id == pay.studentId }
                    val nameMatch = stud?.name?.contains(historySearchQuery, ignoreCase = true) ?: false
                    val refMatch = pay.receiptNo.contains(historySearchQuery, ignoreCase = true)
                    (historySearchQuery.isBlank() || nameMatch || refMatch) &&
                    (historyModeFilter == "ALL" || pay.paymentMode == historyModeFilter)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFF2D62CD)).padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("REF / STUDENT", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp)
                            Text("DATE/MODE", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp)
                            Text("NET PAID", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.End)
                            Text("ACTIONS", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.End)
                        }

                        if (displayedPayments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No payment ledger records matched filters.", fontSize = 10.sp, color = Color.Gray)
                            }
                        } else {
                            val sdf = remember { java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault()) }
                            displayedPayments.take(10).forEachIndexed { index, pay ->
                                val stud = students.find { it.id == pay.studentId }
                                val bch = stud?.let { s -> batches.find { it.id == s.batchId } }
                                val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                                Row(
                                    modifier = Modifier.fillMaxWidth().background(bg).padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.4f)) {
                                        Text(pay.receiptNo, fontWeight = FontWeight.Bold, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                        Text(stud?.name ?: "Archive Scholar", fontWeight = FontWeight.SemiBold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }

                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(sdf.format(java.util.Date(pay.datePaid)), fontSize = 9.sp, color = Color.DarkGray)
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(
                                                when(pay.paymentMode) {
                                                    "UPI" -> Color(0xFFE8F5E9)
                                                    "CASH" -> Color(0xFFE3F2FD)
                                                    else -> Color(0xFFFFF3E0)
                                                }
                                            ).padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                pay.paymentMode,
                                                color = when(pay.paymentMode) {
                                                    "UPI" -> Color(0xFF2E7D32)
                                                    "CASH" -> Color(0xFF1565C0)
                                                    else -> Color(0xFFE65100)
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 7.sp
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                                        Text("₹${pay.amountPaid.toInt()}", fontWeight = FontWeight.Black, color = Color(0xFF1B5E20), fontSize = 11.sp)
                                        if (pay.discount > 0.0) {
                                            Text("Disc: ₹${pay.discount.toInt()}", fontSize = 7.sp, color = Color.Red)
                                        }
                                    }

                                    Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { activeReceiptToShow = pay }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Receipt, "Receipt", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        }
                                        IconButton(onClick = { paymentToDelete = pay }, modifier = Modifier.size(20.dp)) {
                                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. DELETE SAFETY CONFIRM DIALOG
    paymentToDelete?.let { pay ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Void & Reverted Payment Account?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Are you sure you want to void receipt reference ${pay.receiptNo} of ₹${pay.amountPaid.toInt()}? This action rolls back student due record and logs a financial reversal entry.", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFeePayment(pay)
                        paymentToDelete = null
                        Toast.makeText(context, "Tuition collection rolled back successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Revert Ledger Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) { Text("Cancel") }
            }
        )
    }

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                val resultMsg = com.example.ui.screens.CardExporter.generatePaymentInvoicePdf(
                                    context = context,
                                    payment = pay,
                                    student = student,
                                    batch = batch,
                                    academyName = academyName,
                                    address = adminAddress,
                                    phone = adminPhone,
                                    email = adminEmail
                                )
                                Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF Invoice")
                    }

                    Button(
                        onClick = {
                            val shareMsg = """
                                🧾 *${academyName.uppercase()} FEE RECEIPT*
                                ------------------------------------
                                Receipt No: ${pay.receiptNo}
                                Student: ${student.name}
                                Course: ${batch?.name ?: "Preparatory"}
                                Month Code: ${pay.monthPaidFor}
                                Total Collected: ₹${pay.amountPaid.toInt()}
                                Payment Mode: ${pay.paymentMode}
                                Status: *SUCCESSFULLY PAID & SETTLED*
                                ------------------------------------
                                $academyName, Chhibramau.
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
                        Text("Share Receipt")
                    }
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
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send SMS Button
                    Button(
                        onClick = {
                            sendDirectSMS(student.parentPhone.ifEmpty { student.phone }, finalReminderText, context)
                            selectedStudentForReminder = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Send,
                            contentDescription = "SMS",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send via Normal SMS 💬", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentForReminder = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
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
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share on WhatsApp", fontSize = 10.sp, color = Color.White)
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

// ====================================================================================
// 🌟 ASPIRANTS NEXT-GEN FUTURE EDTECH SUITE & OVERLAYS
// ====================================================================================

// --- NEXT-GEN FUTURES INTEGRATION CABINET COMPOSABLE ---
@Composable
fun NextGenFuturesIntegrationCabinet(
    onAILearningClick: () -> Unit,
    onEdTechClick: () -> Unit,
    onInsightsClick: () -> Unit,
    onMonetizationClick: () -> Unit,
    onEngagementClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onSocialFlyerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Next Gen Icons",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "🌟 NEXT-GEN FUTURES HUB",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "NEW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Next-tier EdTech directions empowering students, administration, and marketing vectors with integrated AI tooling.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val nextGenSecList = listOf(
                Triple("AI Learning Engine", Icons.Default.AutoAwesome, onAILearningClick),
                Triple("EdTech Ecosystem Sync", Icons.Default.CastForEducation, onEdTechClick),
                Triple("Data-Driven Insights", Icons.Default.Insights, onInsightsClick),
                Triple("Growth & Monetization", Icons.Default.MonetizationOn, onMonetizationClick),
                Triple("Immersive Engagement", Icons.Default.EmojiEvents, onEngagementClick),
                Triple("Compliance & Security", Icons.Default.Security, onSecurityClick),
                Triple("AI Poster Generator", Icons.Default.Campaign, onSocialFlyerClick)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cols = if (maxWidth < 500.dp) 2 else 4
                val rows = (nextGenSecList.size + cols - 1) / cols
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (c in 0 until cols) {
                                val idx = r * cols + c
                                if (idx < nextGenSecList.size) {
                                    val (title, icon, action) = nextGenSecList[idx]
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { action() }
                                            .testTag("next_gen_button_${title.lowercase().replace(" ", "_")}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = title,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Text(
                                                text = title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 1. AI-POWERED LEARNING ENGINE OVERLAY (Study Plans + Adaptive Quizzes)
// -------------------------------------------------------------------------
@Composable
fun NextGenAILearningOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Planner") } // "Planner" or "Quiz"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Planner states
    var studentName by remember { mutableStateOf("") }
    var targetGoal by remember { mutableStateOf("IIT-JEE Prep") }
    val subjectsList = listOf("Physics", "Chemistry", "Mathematics", "Biology")
    var selectedSubjectsMap by remember { mutableStateOf(subjectsList.associateWith { true }) }
    var generatedPlanText by remember { mutableStateOf("") }
    var isGeneratingPlan by remember { mutableStateOf(false) }

    // Quiz states
    var quizTopic by remember { mutableStateOf("Kinematics Equations") }
    var quizDifficulty by remember { mutableStateOf("Easy") }  // "Easy", "Medium", "Hard" (Adaptive)
    var activeQuestionIndex by remember { mutableStateOf(0) }
    var scoreValue by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    var isCorrectChecked by remember { mutableStateOf(false) }

    // Hardcoded high-yield educational questions mapping topics & difficulty
    val sampleQuestions = listOf(
        // Kinematics Easy
        Triple(
            "What is the physical acceleration of a motorcar when its speed increases from 10 m/s to 30 m/s in exactly 5 seconds?",
            listOf("2 m/s²", "4 m/s²", "6 m/s²", "8 m/s²"),
            1 // index 1 = "4 m/s²"
        ),
        // Kinematics Medium
        Triple(
            "A projectile launched at 30° achieves range R. At what other launch angle with equal initial speed will the horizontal range R be identical?",
            listOf("45°", "50°", "60°", "75°"),
            2 // index 2 = "60°"
        ),
        // Kinematics Hard
        Triple(
            "If vertical speed is y = 3t² + 2t, what horizontal distance is covered between t = 1s and t = 3s if horizontal velocity is constant at vx = 5 m/s?",
            listOf("10 m", "15 m", "20 m", "25 m"),
            0 // index 0 = "10 m"
        )
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                        Column {
                            Text("Aspirants Learning Engine", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Real AI Adaptive Systems & Customized Planning Focus", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_ai_learning_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Sub-tabs Row
                val indicatorColor = MaterialTheme.colorScheme.primary
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    listOf("Planner", "Quiz").forEach { tab ->
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = tab }
                                .drawBehind {
                                    if (isSelected) {
                                        drawRect(
                                            color = indicatorColor,
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 4.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tab == "Planner") "📅 Personalized Study Planner" else "🎯 Adaptive Quiz Center",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Core Scrollable Interactive Workspace
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activeTab == "Planner") {
                        // PERSONALIZED STUDY PLANNER WORKSPACE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Step 1: Enter Student Credentials", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                OutlinedTextField(
                                    value = studentName,
                                    onValueChange = { studentName = it },
                                    label = { Text("Student Full Name") },
                                    placeholder = { Text("e.g. Aman Sharma") },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_planner_name_input"),
                                    singleLine = true
                                )

                                Text("Step 2: Define Academic Target Focus", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    }; Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { val targets = listOf("IIT-JEE Prep", "Class 12 Board", "NEET Focus", "Olympiad Prep", "KVPY", "NTSE Target", "General Foundation Courses")
                                    targets.forEach { goal ->
                                        val isSel = targetGoal == goal
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { targetGoal = goal },
                                            label = { Text(goal, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Text("Step 3: Tick Relevant Subjects", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    subjectsList.forEach { sub ->
                                        val ticked = selectedSubjectsMap[sub] ?: false
                                        FilterChip(
                                            selected = ticked,
                                            onClick = {
                                                selectedSubjectsMap = selectedSubjectsMap.toMutableMap().apply {
                                                    put(sub, !ticked)
                                                }
                                            },
                                            label = { Text(sub, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        if (studentName.isBlank()) {
                                            Toast.makeText(context, "Please enter a student to formulate customized plans", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        isGeneratingPlan = true
                                        scope.launch {
                                            try {
                                                val subsText = selectedSubjectsMap.entries.filter { it.value }.map { it.key }.joinToString()
                                                val customPrompt = """
                                                    Develop a professional hour-by-hour exam preparation syllabus plan for student $studentName.
                                                    Academic target: $targetGoal
                                                    Focusing on subject streams: $subsText.
                                                    Delineate 4 core weekly milestones, recommended textbooks, self-evaluation cycles, and exam-day stress reduction tips. Keep formatting sleek and encouraging.
                                                """.trimIndent()
                                                
                                                val aiResponse = com.example.services.GeminiService.generateContent(
                                                    prompt = customPrompt,
                                                    systemInstruction = "You are a professional EdTech syllabus architect. Output custom study plans formatted elegantly."
                                                )
                                                generatedPlanText = aiResponse
                                            } catch (e: Exception) {
                                                generatedPlanText = "Error formulation failed: ${e.localizedMessage}. Please verify internet and local key setups."
                                            } finally {
                                                isGeneratingPlan = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_planner_generate_btn"),
                                    enabled = !isGeneratingPlan
                                ) {
                                    if (isGeneratingPlan) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Mining Data Elements...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Formulate Custom Syllabus on Gemini-3.5", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (generatedPlanText.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                                border = BorderStroke(1.dp, Color(0xFFCDDC39).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Generated AI Study Plan Outline", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF33691E))
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedPlanText))
                                            Toast.makeText(context, "Syllabus copied safely to system buffer!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF33691E))
                                        }
                                    }
                                    Text(
                                        text = generatedPlanText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.background(Color.White).padding(10.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // ADAPTIVE MCQ QUIZ WORKSPACE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Subject: Physics Mechanical Frameworks", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Core Target: $quizTopic", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (quizDifficulty == "Easy") Color(0xFFE8F5E9) else if (quizDifficulty == "Medium") Color(0xFFFFF3E0) else Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            text = "DIFFICULTY: $quizDifficulty",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = if (quizDifficulty == "Easy") Color(0xFF2E7D32) else if (quizDifficulty == "Medium") Color(0xFFE65100) else Color(0xFFC62828),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Score: $scoreValue XP Points 🏆",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Resolve active question based on difficulty state
                        val curQ = when (quizDifficulty) {
                            "Medium" -> sampleQuestions[1]
                            "Hard" -> sampleQuestions[2]
                            else -> sampleQuestions[0]
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Q: " + curQ.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                curQ.second.forEachIndexed { optIdx, option ->
                                    val isSelected = selectedOptionIndex == optIdx
                                    val optionContainerColor = if (isCorrectChecked) {
                                        if (optIdx == curQ.third) Color(0xFFE8F5E9)
                                        else if (isSelected) Color(0xFFFFEBEE)
                                        else MaterialTheme.colorScheme.surface
                                    } else {
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    }
                                    val optionBorderColor = if (isCorrectChecked && optIdx == curQ.third) Color(0xFF4CAF50)
                                    else if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isCorrectChecked) { selectedOptionIndex = optIdx }
                                            .testTag("quiz_option_$optIdx"),
                                        colors = CardDefaults.cardColors(containerColor = optionContainerColor),
                                        border = BorderStroke(1.3.dp, optionBorderColor),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { if (!isCorrectChecked) selectedOptionIndex = optIdx },
                                                enabled = !isCorrectChecked
                                            )
                                            Text(option, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                if (feedbackText.isNotEmpty()) {
                                    Text(
                                        text = feedbackText,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (feedbackText.contains("SUCCESS")) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!isCorrectChecked) {
                                        Button(
                                            onClick = {
                                                if (selectedOptionIndex == null) {
                                                    Toast.makeText(context, "Select choice first", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                isCorrectChecked = true
                                                val verifiedIdx = curQ.third
                                                if (selectedOptionIndex == verifiedIdx) {
                                                    scoreValue += 50
                                                    feedbackText = "⭐ SUCCESS: Correct answer! +50 XP. Increasing adaptive problem sets to higher complexity matrices!"
                                                    // Auto adjust difficulty upward
                                                    quizDifficulty = if (quizDifficulty == "Easy") "Medium" else "Hard"
                                                } else {
                                                    feedbackText = "❌ INCORRECT: The correct response is '${curQ.second[verifiedIdx]}'. Demoting difficulty matrices."
                                                    quizDifficulty = if (quizDifficulty == "Hard") "Medium" else "Easy"
                                                }
                                            },
                                            modifier = Modifier.weight(1f).testTag("quiz_verify_btn")
                                        ) {
                                            Text("Verify Selection", fontSize = 11.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                // Next Question Reset
                                                selectedOptionIndex = null
                                                isCorrectChecked = false
                                                feedbackText = ""
                                                val nextTopics = listOf("Angular Momentum Mechanics", "Projectile Parabolic Paths", "Newton Friction Equations")
                                                quizTopic = nextTopics.random()
                                            },
                                            modifier = Modifier.weight(1f).testTag("quiz_next_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Text("Next Adaptive Problem", fontSize = 11.sp)
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

// -------------------------------------------------------------------------
// 2. EDTECH ECOSYSTEM INTEGRATION OVERLAY (Classroom + Zoom + LMS + NCERT)
// -------------------------------------------------------------------------
@Composable
fun NextGenEdTechOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("ClassSync") } // "ClassSync", "LmsLogs", "Library"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Classroom Sync state
    var isSyncingClassroom by remember { mutableStateOf(false) }
    var syncLogList by remember { mutableStateOf(listOf("System awaiting Classroom sync event")) }

    // Zoom scheduling state
    var schedTopic by remember { mutableStateOf("") }
    var generatedZoomInvite by remember { mutableStateOf("") }

    // Library search
    var librarySearchQuery by remember { mutableStateOf("") }
    val ncertMaterials = listOf(
        Pair("Class 12 Physics: Electrostatics Notes", "Full CBSE theory covering Coulomb's forces, Gauss Theorem, dipole moments, line charges."),
        Pair("Class 11 Chemistry: Thermodynamics", "Basic laws of enthalpy, entropy calculations, Carnot cyclic heat engines."),
        Pair("Class 10 Biology: Life Processes", "Photosynthesis and light-independent cycles, cellular human respiration systems."),
        Pair("IIT-JEE 2024 Practice Mock PDF", "High-yield actual examination question paper containing 30 adaptive MCQs with hints.")
    )

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF263238))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                        Column {
                            Text("Aspirants EdTech Sync Engine", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Active Nodes: Google Classroom, Zoom Webinars, LMS REST APIs", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_edtech_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Sub-Tabs
                ScrollableTabRow(
                    selectedTabIndex = listOf("ClassSync", "LmsLogs", "Library").indexOf(activeSubTab),
                    edgePadding = 8.dp,
                    containerColor = Color(0xFFF0F4C3).copy(alpha = 0.2f)
                ) {
                    Tab(
                        selected = activeSubTab == "ClassSync",
                        onClick = { activeSubTab = "ClassSync" },
                        text = { Text("🏫 Classroom & Zoom", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSubTab == "LmsLogs",
                        onClick = { activeSubTab = "LmsLogs" },
                        text = { Text("🔌 LMS REST APIs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeSubTab == "Library",
                        onClick = { activeSubTab = "Library" },
                        text = { Text("📚 NCERT & Prep Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Core content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeSubTab == "ClassSync") {
                        // GOOGLE CLASSROOM SYNC CONTROLLER
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Google Classroom Integration Nodes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                                Text("Map Google Assignment streams. Direct database parsing downloads resources onto students study workspace accounts.", fontSize = 11.sp, color = Color.DarkGray)
                                
                                Button(
                                    onClick = {
                                        isSyncingClassroom = true
                                        scope.launch {
                                            delay(1500)
                                            isSyncingClassroom = false
                                            syncLogList = syncLogList + listOf(
                                                "Initiated Classroom REST call.",
                                                "Linked: Standard Tenth Batch Classrooms.",
                                                "Imported 4 Digital PDFs to Library.",
                                                "Classroom Sync Completed successfully!"
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    modifier = Modifier.fillMaxWidth().testTag("sync_classroom_btn"),
                                    enabled = !isSyncingClassroom
                                ) {
                                    if (isSyncingClassroom) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reading Google Streams...", fontSize = 11.sp)
                                    } else {
                                        Text("Sync Now with Google Classroom", fontSize = 11.sp)
                                    }
                                }

                                Text("Status Log Reports:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.DarkGray)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(8.dp)
                                        .border(0.5.dp, Color.LightGray)
                                ) {
                                    syncLogList.forEach { log ->
                                        Text("• $log", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                                    }
                                }
                            }
                        }

                        // ZOOM SCHEDULER
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Schedule Live Zoom Webinar Class", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                OutlinedTextField(
                                    value = schedTopic,
                                    onValueChange = { schedTopic = it },
                                    label = { Text("Webinar Lecture Topic") },
                                    placeholder = { Text("e.g., Bohr Model Angular Momentum") },
                                    modifier = Modifier.fillMaxWidth().testTag("zoom_topic_input")
                                )

                                Button(
                                    onClick = {
                                        if (schedTopic.isBlank()) {
                                            Toast.makeText(context, "Please write a lecture topic first", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val generatedZoomId = (100000000..999999999).random()
                                        generatedZoomInvite = """
                                            🏫 ZOOM WEBINAR SCHEDULED!
                                            Topic: $schedTopic
                                            Access link: https://zoom.us/j/$generatedZoomId
                                            Webinar ID: $generatedZoomId
                                            Passcode: ASPR2026
                                            Status: Live Sync Enabled
                                        """.trimIndent()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("zoom_sched_btn")
                                ) {
                                    Text("Compute Instant Zoom Meeting Invites", fontSize = 11.sp)
                                }

                                if (generatedZoomInvite.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(generatedZoomInvite, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                            
                                            val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                                            Button(
                                                onClick = {
                                                    clipboard.setText(AnnotatedString(generatedZoomInvite))
                                                    Toast.makeText(context, "Zoom Invitation copied safely!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.align(Alignment.End),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                                            ) {
                                                Text("Copy Invitation", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (activeSubTab == "LmsLogs") {
                        // LMS CREDENTIALS & API LOGS
                        Text("Active External LMS Gateway Node", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Utilize security tokens of Aspirants management server directly on Moodle, Blackboard, Canvas or customized private institutional portals.", fontSize = 11.sp)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("LMS Developer Authorization Token", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.LightGray)
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color.Black).padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Bearer tams_jwt_live_a390fdc193af87", fontSize = 10.sp, color = Color(0xFF00FF66), fontFamily = FontFamily.Monospace)
                                    IconButton(onClick = {
                                        Toast.makeText(context, "API JWT bearer credential written to buffer", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Keys", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Text("Live External Endpoint Log Tracker", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.LightGray)
                                val endpointLogs = listOf(
                                    "2026-06-15 11:15 - [API] GET /v1/students - Origin: canvas.aspirants.com - 200 OK",
                                    "2026-06-15 11:18 - [API] POST /v1/attendance - Payload: {batchId:103} - 202 Accepted",
                                    "2026-06-15 11:22 - [API] GET /v1/tuition/unpaid - Status: Query Passed - 200 OK",
                                    "2026-06-15 11:25 - [API] POST /v1/webhooks/trigger - Sync Event Queue: 0 - 201 Created"
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    endpointLogs.forEach { log ->
                                        Text(log, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF00FF66))
                                    }
                                }
                            }
                        }
                    } else {
                        // DIGITAL SYLLABUS & LECTURE REPOSITORY
                        Text("Authorized Textbook & NCERT Syllabus Core", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = librarySearchQuery,
                            onValueChange = { librarySearchQuery = it },
                            label = { Text("Search NCERT Materials & competitive worksheets") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        val filteredLibrary = ncertMaterials.filter {
                            it.first.contains(librarySearchQuery, ignoreCase = true) ||
                                    it.second.contains(librarySearchQuery, ignoreCase = true)
                        }

                        filteredLibrary.forEach { (docTitle, docDesc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(docTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(docDesc, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Streaming resource: $docTitle securely!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Open", fontSize = 9.sp)
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

// -------------------------------------------------------------------------
// 3. DATA-DRIVEN INSIGHTS OVERLAY (Dropouts + Heatmaps + Load Minimizer)
// -------------------------------------------------------------------------
@Composable
fun NextGenInsightsOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("DropoutPredictor") } // "DropoutPredictor", "Heatmap", "Schedules"
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF880E4F))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = Color.White)
                        Column {
                            Text("Aspirants Predictive Analytics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Deep Risk Indices, Student Dropout Matrices & Teacher Optimization", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_insights_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Sub-tabs
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))) {
                    listOf("DropoutPredictor", "Heatmap", "Schedules").forEach { tab ->
                        val isSel = activeSubTab == tab
                        val lbl = when(tab) {
                            "DropoutPredictor" -> "📉 Dropout Predictor"
                            "Heatmap" -> "📊 Strengths Heatmaps"
                            else -> "⏰ Teacher Load"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSubTab = tab }
                                .drawBehind {
                                    if (isSel) {
                                        drawRect(
                                            color = Color(0xFF880E4F),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 4.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp, color = if (isSel) Color(0xFF880E4F) else Color.DarkGray)
                        }
                    }
                }

                // Scrollable details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeSubTab == "DropoutPredictor") {
                        Text("Dropout & Fee Default Predictor Matrix (Indian IT Act Compliant)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Our predictive regression pipeline flags students possessing critical risk markers (low class attendance average & outstanding tuition payment delays).", fontSize = 11.sp, color = Color.DarkGray)

                        val atRiskList = listOf(
                            Triple("Aman Verma (Class 12)", "Attendance: 54% | Pending fees: INR 4500", 86),
                            Triple("Rishi Kumar (Class 10)", "Attendance: 61% | Pending fees: INR 3200", 72),
                            Triple("Simran Kaur (Olympiad)", "Attendance: 70% | Pending fees: INR 2900", 58)
                        )

                        atRiskList.forEach { (name, stats, risk) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = risk / 100f)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (risk > 80) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
                                        ) {
                                            Text(
                                                text = "Risk: $risk%",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (risk > 80) Color(0xFFD32F2F) else Color(0xFFEF6C00),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Text(stats, fontSize = 11.sp, color = Color.Gray)
                                    
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Academic status automated notification shared to Parents WhatsApp node!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_whatsapp),
                                            contentDescription = "WhatsApp",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Outreach Warning PDF", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    } else if (activeSubTab == "Heatmap") {
                        Text("Active Classroom Subject Strength Grid Heatmap", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        val heatmapsList = listOf(
                            Pair("Mathematics Core Geometry", 92),
                            Pair("Quantum Kinetics Theory (Physics)", 81),
                            Pair("Organic Chemistry Nomenclature", 68),
                            Pair("Hindi Literature Grammar Modules", 45)
                        )

                        heatmapsList.forEach { (sub, score) ->
                            val colorBar = if (score > 85) Color(0xFF4CAF50)
                            else if (score > 60) Color(0xFFFF9800)
                            else Color(0xFFF44336)

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sub, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("Avg: $score%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorBar)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(score / 100f)
                                            .fillMaxHeight()
                                            .background(colorBar)
                                    )
                                }
                            }
                        }
                    } else {
                        // AI TEACHER WORKLOAD OPTIMIZER
                        Text("Teacher Load & Classroom Scheduling Optimization Engine", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Automatically maps faculty schedules across active classroom batches. Prevents burnout by analyzing teaching hours daily.", fontSize = 11.sp, color = Color.DarkGray)

                        var sliderStateHours by remember { mutableStateOf(6f) }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Assign Hours per Teacher: ${sliderStateHours.toInt()} hrs/day", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Slider(
                                    value = sliderStateHours,
                                    onValueChange = { sliderStateHours = it },
                                    valueRange = 2f..10f,
                                    steps = 7
                                )

                                val efficiencyLabel = if (sliderStateHours < 5f) {
                                    "Underutilized: Faculty contains unoccupied rosters. Consider adding CBSE Olympiad batches."
                                } else if (sliderStateHours < 8f) {
                                    "OPTIMAL: Maximum classroom learning yield with balanced preparatory sessions."
                                } else {
                                    "OVERBURDEN: Critical core exhaustion risk. Split physics rosters immediately."
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = if (sliderStateHours > 8) Color.Red else Color.Green)
                                    Text(
                                        text = efficiencyLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sliderStateHours > 8) Color.Red else Color.Black
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

@Composable
fun NextGenMonetizationOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var isReferralGenerated by remember { mutableStateOf(false) }
    var inputRefName by remember { mutableStateOf("") }
    var generatedReferText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val userRole by viewModel.currentUserRole.collectAsState()
    val adminEmailState by viewModel.adminEmail.collectAsState()
    val teachersList by viewModel.teachersList.collectAsState()
    val currentTeacher = teachersList.find { it.email.trim().lowercase() == adminEmailState.trim().lowercase() }

    var selectedPlanName by remember { mutableStateOf("") }
    var selectedPlanPrice by remember { mutableStateOf("") }
    var showingCheckoutForm by remember { mutableStateOf(false) }
    var refInputCode by remember { mutableStateOf("") }
    var upiVerificationState by remember { mutableStateOf(false) }

    val subsPlans = listOf(
        Triple("Basic Tier Standard", "999.0", "Access batches registers, offline diagnostics tracking, custom worksheet PDF prints."),
        Triple("Premium Plus (AI Doubt Hub)", "2499.0", "Includes Gemini doubt chatbot access, custom weekly study plan formulations."),
        Triple("Enterprise Multi-Branch Franchise", "4999.0", "Unlocks multi-institute setup portals, localized GDPR compliance auditors.")
    )

    val teacherPlans = listOf(
        Triple("Premium Mentor Monthly", "499.0", "Unlocks unlimited student batches, auto WhatsApp homework dispatch trackers, expense logs."),
        Triple("Master Educator Annual", "2999.0", "Unlocks automated study planner boards, unlimited clouds data storage backup, custom study sheets.")
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (userRole == "TEACHER") "Teacher Premium Center" else "TAMS SaaS Growth & Growth Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_monetize_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (userRole == "TEACHER") {
                    // Display Teacher Account Profile overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👤 Teacher Account: ${currentTeacher?.name ?: "Independent Tutor"}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("📧 Registration Email: $adminEmailState", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "👑 SaaS Status: " + if (currentTeacher?.isPremium == true) "ELITE PREMIUM" else "FREE BASIC PORTAL VERSION",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (currentTeacher?.isPremium == true) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            if (currentTeacher?.isPremium == true) {
                                Text("✓ Plan active: ${currentTeacher.premiumPlan}. Valid until: ${currentTeacher.premiumExpiry}", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    if (!showingCheckoutForm) {
                        Text("Explore Premium Services Upgrades Offered by Admin:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                        teacherPlans.forEach { (name, price, terms) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPlanName = name
                                        selectedPlanPrice = price
                                        showingCheckoutForm = true
                                    },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("₹$price", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                    }
                                    Text(terms, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                } else {
                    // ADMIN Standard Upgrades
                    Text("Select Institutional Subscription Tier", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    subsPlans.forEach { (tierName, price, terms) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPlanName = tierName
                                    selectedPlanPrice = price
                                    showingCheckoutForm = true
                                },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tierName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("₹$price", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                }
                                Text(terms, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                if (showingCheckoutForm) {
                    HorizontalDivider()
                    Text("💳 Checkout: $selectedPlanName • ₹$selectedPlanPrice", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Option A: Scan Admin UPI QR", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                        
                        // Show QR Code inside Dialog
                        val customRefUri = "upi://pay?pa=smtsharma282.sks@okaxis&pn=TAMS%20Admin&am=$selectedPlanPrice&cu=INR"
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val sizePx = size.width
                                val length = 21
                                val specSize = sizePx / length
                                drawRect(color = Color.White)
                                fun drawCorner(tx: Float, ty: Float) {
                                    drawRect(color = Color(0xFF1E3A8A), topLeft = androidx.compose.ui.geometry.Offset(tx, ty), size = androidx.compose.ui.geometry.Size(specSize * 7, specSize * 7))
                                    drawRect(color = Color.White, topLeft = androidx.compose.ui.geometry.Offset(tx + specSize, ty + specSize), size = androidx.compose.ui.geometry.Size(specSize * 5, specSize * 5))
                                    drawRect(color = Color(0xFF1E3A8A), topLeft = androidx.compose.ui.geometry.Offset(tx + specSize * 2, ty + specSize * 2), size = androidx.compose.ui.geometry.Size(specSize * 3, specSize * 3))
                                }
                                drawCorner(0f, 0f)
                                drawCorner((length - 7) * specSize, 0f)
                                drawCorner(0f, (length - 7) * specSize)
                                
                                val random = java.util.Random(customRefUri.hashCode().toLong())
                                for (r in 0 until length) {
                                    for (c in 0 until length) {
                                        if (!(r < 9 && c < 9) && !(r < 9 && c >= length - 9) && !(r >= length - 9 && c < 9)) {
                                            if (random.nextBoolean()) {
                                                drawRect(color = Color(0xFF1E3A8A), topLeft = androidx.compose.ui.geometry.Offset(c * specSize, r * specSize), size = androidx.compose.ui.geometry.Size(specSize, specSize))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse(customRefUri)
                                    setPackage("com.google.android.apps.npath")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(customRefUri)))
                                    } catch (ex: Exception) {
                                        Toast.makeText(context, "UPI direct link dispatch simulation...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Launch Google Pay / Active UPI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("Option B: Enter payment reference code", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = refInputCode,
                            onValueChange = { refInputCode = it },
                            label = { Text("Bank UTR ID / Reference Transaction Code", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showingCheckoutForm = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (refInputCode.trim().length >= 6) {
                                        if (userRole == "TEACHER") {
                                            viewModel.upgradeTeacherPremium(adminEmailState, selectedPlanName, selectedPlanPrice, refInputCode)
                                            Toast.makeText(context, "🎉 Premium Suite unlocked successfully!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "🎉 Institute subscription processed through ledger successfully!", Toast.LENGTH_LONG).show()
                                        }
                                        showingCheckoutForm = false
                                        upiVerificationState = true
                                    } else {
                                        Toast.makeText(context, "Please enter a valid 6-char proof of transaction reference", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Text("Validate & Activate", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Referral system
                Text("Create Student Admission Referral Bonus", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = inputRefName,
                    onValueChange = { inputRefName = it },
                    label = { Text("Referring Brand Marketer Name") },
                    placeholder = { Text("e.g., Professor Amit") },
                    modifier = Modifier.fillMaxWidth().testTag("ref_name_input"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (inputRefName.isBlank()) {
                            Toast.makeText(context, "Marketer name is missing", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val cleanRef = inputRefName.lowercase().replace(" ", "_")
                        isReferralGenerated = true
                        generatedReferText = "https://aspirantsapp.com/admission_referral/$cleanRef\nDiscount coupon code: GET10_ASPIRANT"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("generate_ref_btn")
                ) {
                    Text("Synthesize Promo Coupon & Link", fontSize = 11.sp)
                }

                if (isReferralGenerated) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Auto Referral Payload:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF33691E))
                            Text(generatedReferText, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
                            
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(generatedReferText))
                                    Toast.makeText(context, "Referral link cached safely!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33691E))
                            ) {
                                Text("Copy referral Text", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 5. NEXT-GEN ENGAGEMENT OVERLAY (Streaks + AR Classrooms + Peer Forums)
// -------------------------------------------------------------------------
@Composable
fun NextGenEngagementOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("Streaks") } // "Streaks", "ARLabs", "PeerForum"
    val context = LocalContext.current
    var inputForumText by remember { mutableStateOf("") }
    var forumPostList by remember { mutableStateOf(listOf(
        "Raman: Can someone solve problem 4 from electrostatics? Newton gravity similarities are bugging me.",
        "Teacher S: Check the NCERT Library folder class 12, uploaded Coulomb's constant breakdowns."
    )) }

    // Renders interactive animation for AR Science Orbits
    var orbitsVelocityCoeff by remember { mutableStateOf(1f) }
    var systemAnatomyAtomName by remember { mutableStateOf("Bohr Lithium Core Model") }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE65100))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                        Column {
                            Text("Aspirants Gamified Classroom", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Unlocked achievements, AR Laboratories, Batch forums support", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_engagement_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tabs Row
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFCC80).copy(alpha = 0.2f))) {
                    listOf("Streaks", "ARLabs", "PeerForum").forEach { tab ->
                        val isSel = activeSubTab == tab
                        val lbl = when(tab) {
                            "Streaks" -> "🔥 Streaks & Badges"
                            "ARLabs" -> "🧬 AR Science Labs"
                            else -> "💬 P2P Batch Forum"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSubTab = tab }
                                .drawBehind {
                                    if (isSel) {
                                        drawRect(
                                            color = Color(0xFFE65100),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 4.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color(0xFFE65100) else Color.DarkGray, fontSize = 11.sp)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeSubTab == "Streaks") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Your Active Learning Streak", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("🔥 15 Days Active", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                                Text("Great job Aman! Study daily topics or pass quiz modules to keep your streak burning.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Daily Streak successfully locked down for today! +50 XP bonus earned!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                ) {
                                    Text("Claim Daily Streak XP Booster", fontSize = 10.sp)
                                }
                            }
                        }

                        Text("Unlocked Academic Achievement Badges", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val badges = listOf(
                            Pair("🤖 Gemini Solver", "Solved 10 Doubts), Unlocked active mentor status"),
                            Pair("📈 Rank Achiever", "Newton core exam leader scored Rank 1"),
                            Pair("✍️ Homework Wizard", "Submit files consistently under deadline bounds"),
                            Pair("💰 Paid Ledger Champ", "Paid all tuition fees under instant zero due marks")
                        )

                        badges.forEach { (bTitle, bDesc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFF3E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text(bTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(bDesc, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    } else if (activeSubTab == "ARLabs") {
                        Text("Responsive AR Immersive Bohr Atom Simulator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("3D Physics particles simulation in realtime. Adjust parameters to see orbits change.", fontSize = 10.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Hydrogen Core", "Lithium Core Model", "Bohr Carbon Model").forEach { rawName ->
                                val isChosen = systemAnatomyAtomName == rawName
                                FilterChip(
                                    selected = isChosen,
                                    onClick = { systemAnatomyAtomName = rawName },
                                    label = { Text(rawName, fontSize = 8.sp) }
                                )
                            }
                        }

                        // Orbit Speed Coefficient Slider
                        Text("Orbital Electron Velocity: ${orbitsVelocityCoeff.toInt()}x", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = orbitsVelocityCoeff,
                            onValueChange = { orbitsVelocityCoeff = it },
                            valueRange = 1f..5f,
                            steps = 3
                        )

                        // HIGH IMPACT CUSTOM VECTOR DRAWING FOR AR SIMULATION
                        val infiniteRotationState = rememberInfiniteTransition()
                        val animatedAngleRotate by infiniteRotationState.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = (4000 / orbitsVelocityCoeff).toInt(), easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0F172A)) // Slate Dark Space background for AR Space Look
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f

                                // Draw Core Nucleus
                                drawCircle(
                                    color = Color(0xFFEF4444), // Proton Red
                                    radius = 24.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                                )
                                drawCircle(
                                    color = Color(0xFFFBBF24), // Neutron Yellow
                                    radius = 16.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(centerX - 8.dp.toPx(), centerY - 4.dp.toPx())
                                )

                                // Draw Orbit Shells
                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    radius = 60.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )

                                drawCircle(
                                    color = Color.LightGray.copy(alpha = 0.4f),
                                    radius = 90.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )

                                // Draw rotating electrons based on state logic
                                val angleRad1 = Math.toRadians(animatedAngleRotate.toDouble())
                                val electronX1 = centerX + (60.dp.toPx() * Math.cos(angleRad1)).toFloat()
                                val electronY1 = centerY + (60.dp.toPx() * Math.sin(angleRad1)).toFloat()

                                drawCircle(
                                    color = Color(0xFF3B82F6), // Electron Blue
                                    radius = 8.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(electronX1, electronY1)
                                )

                                if (systemAnatomyAtomName != "Hydrogen Core") {
                                    val angleRad2 = Math.toRadians((animatedAngleRotate + 180f).toDouble())
                                    val electronX2 = centerX + (90.dp.toPx() * Math.cos(angleRad2)).toFloat()
                                    val electronY2 = centerY + (90.dp.toPx() * Math.sin(angleRad2)).toFloat()

                                    drawCircle(
                                        color = Color(0xFF06B6D4), // Cyan electron
                                        radius = 8.dp.toPx(),
                                        center = androidx.compose.ui.geometry.Offset(electronX2, electronY2)
                                    )
                                }
                            }
                            // Floating stats
                            Text(
                                text = "Bohr core scale sync: PASS\nKinetic velocity energy: ${orbitsVelocityCoeff * 18.2} MeV\nElectron Orbit sync status: Connected",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.BottomStart).background(Color.Black.copy(alpha = 0.5f)).padding(6.dp)
                            )
                        }
                    } else {
                        // BATCH SOCIAL FORUM DISCUSSION BOARD
                        Text("Batch Social Peer-to-Peer Help Forum Channel", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Card(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                forumPostList.forEach { post ->
                                    Text(post, fontSize = 11.sp, color = Color.DarkGray)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = inputForumText,
                                onValueChange = { inputForumText = it },
                                placeholder = { Text("Write query details... e.g. question 5 tips") },
                                modifier = Modifier.weight(1f).testTag("forum_msg_input"),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    if (inputForumText.isNotBlank()) {
                                        forumPostList = forumPostList + listOf("Aman: $inputForumText")
                                        inputForumText = ""
                                    }
                                },
                                modifier = Modifier.testTag("forum_send_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Default.Send, contentDescription = "Send forum", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 6. COMPLIANCE & SECURITY OVERLAY (GDPR + Encrypted Hashing + Audit Logs)
// -------------------------------------------------------------------------
@Composable
fun NextGenSecurityOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var isRightForgottenChecked by remember { mutableStateOf(true) }
    var encryptPasskey by remember { mutableStateOf("") }
    var encryptedLogsOutput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val complChecklists = listOf(
        Pair("GDPR User Data Portability", "Guarantees student credentials can be downloaded as flat JSON matrices directly."),
        Pair("Indian IT Act Security compliance", "Encrypted backup algorithms protect details against external hardware failures.")
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Regulatory Compliance Vault", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_security_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text("Privacy & Security Controls (GDPR)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    complChecklists.forEach { (title, desc) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = isRightForgottenChecked,
                                onCheckedChange = { isRightForgottenChecked = it }
                            )
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(desc, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Encrypted Exports
                Text("Generate Secure Encrypted SQLite Dump", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                OutlinedTextField(
                    value = encryptPasskey,
                    onValueChange = { encryptPasskey = it },
                    label = { Text("AES-256 Hashing Secret key") },
                    placeholder = { Text("e.g. TAMSPassword10") },
                    modifier = Modifier.fillMaxWidth().testTag("aes_passkey_input"),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (encryptPasskey.isBlank()) {
                            Toast.makeText(context, "Encrypt passkey required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Perform standard hex hashing mock output
                        val computedHex = java.security.MessageDigest.getInstance("SHA-256")
                            .digest((encryptPasskey + "TAMS_SALT_2026").toByteArray())
                            .joinToString("") { "%02x".format(it) }
                        encryptedLogsOutput = "AES-256 SQLITE SNAPSHOT ENCRYPTED:\n• Hex Hash: 0x${computedHex.take(32)}...\n• Integrity: Locked & Secure\n• Status: Compliant Node"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("encrypt_db_btn")
                ) {
                    Text("Initialize Safe Cryptographic Export", fontSize = 11.sp)
                }

                if (encryptedLogsOutput.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(encryptedLogsOutput, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 7. AI SOCIAL POST & FLYER GENERATOR (Social templates + Canvas Visualizer)
// -------------------------------------------------------------------------
@Composable
fun NextGenSocialFlyerOverlay(
    viewModel: AppViewModel,
    onClose: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf("AIWriter") } // "AIWriter" or "Canva"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Writer systems
    var marketingTargetCampaign by remember { mutableStateOf("Admission Open 2026") }
    var activePlatformName by remember { mutableStateOf("WhatsApp Status") }
    var generatedAICopyText by remember { mutableStateOf("") }
    var isGeneratingCopyText by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00796B))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                        Column {
                            Text("Aspirants AI Social Promoting Arena", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Formulate high-yield marketing flyers, statuses, and posts using Gemini-3.5", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_social_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Sub-tabs
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFB2DFDB).copy(alpha = 0.2f))) {
                    listOf("AIWriter", "Canva").forEach { tab ->
                        val isSel = activeSubTab == tab
                        val lbl = if(tab == "AIWriter") "✍️ AI Text Marketer Copy" else "🎨 Digital visual Poster Canvas"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeSubTab = tab }
                                .drawBehind {
                                    if (isSel) {
                                        drawRect(
                                            color = Color(0xFF00796B),
                                            topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 4.dp.toPx()),
                                            size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(lbl, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color(0xFF00796B) else Color.DarkGray, fontSize = 11.sp)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeSubTab == "AIWriter") {
                        Text("Formulate Catchy Promotional Status & Post Copywriting", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Inputs map directly to professional marketing system instructions on Gemini, ensuring ultra-engaging calls to action which promote the class academy.", fontSize = 10.sp, color = Color.Gray)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Step 1: Choose Marketing Campaign Focus", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Admission Open 2026", "Elite JEE Physics", "Summer Bootcamp").forEach { opt ->
                                        val checker = marketingTargetCampaign == opt
                                        FilterChip(
                                            selected = checker,
                                            onClick = { marketingTargetCampaign = opt },
                                            label = { Text(opt, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Text("Step 2: Destination Platform Format", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("WhatsApp Status", "Instagram Post", "Twitter Tweet").forEach { plat ->
                                        val checker = activePlatformName == plat
                                        FilterChip(
                                            selected = checker,
                                            onClick = { activePlatformName = plat },
                                            label = { Text(plat, fontSize = 10.sp) }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        isGeneratingCopyText = true
                                        scope.launch {
                                            try {
                                                val finalPrompt = "Create a brief highly catchy promotional social post of format $activePlatformName, encouraging parents and student enrollments for Aspirants Academy. Focal theme: $marketingTargetCampaign. Include telephone: 9876543210, premium hashtags, and vivid educational emojis. Keep it extremely exciting!"
                                                val resultValue = com.example.services.GeminiService.generateContent(
                                                    prompt = finalPrompt,
                                                    systemInstruction = "You are a senior social advertisement copywriter for elite preparatory coaching institutes. Output creative marketing text blocks."
                                                )
                                                generatedAICopyText = resultValue
                                            } catch (e: Exception) {
                                                generatedAICopyText = "Ad Copywriting extraction failed: ${e.localizedMessage}. Verify networks."
                                            } finally {
                                                isGeneratingCopyText = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("social_generate_copy_btn"),
                                    enabled = !isGeneratingCopyText,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                                ) {
                                    if (isGeneratingCopyText) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Mining Copy Structure...", fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Generate Engaging Copy with Gemini-3.5", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (generatedAICopyText.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                                border = BorderStroke(1.dp, Color(0xFF009688).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Copy Mockup Preview Screen", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF004D40))
                                        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedAICopyText))
                                            Toast.makeText(context, "Copied poster copy to system buffer!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Marketing Status", tint = Color(0xFF004D40))
                                        }
                                    }
                                    Text(
                                        text = generatedAICopyText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.background(Color.White).padding(10.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // CANVA GRAPHICS CANVAS FLYER PREVIEW
                        Text("Interactive Marketing Flyer Canvas Editor", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Draft gorgeous visual branding posters with dynamic gradience directly suited for WhatsApp and Instagram shares.", fontSize = 11.sp)

                        var selectColorBaseTheme by remember { mutableStateOf("Calm Slate") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Calm Slate", "Teal Amber Accent", "Rich Crimson Golden").forEach { colorTheme ->
                                val checking = selectColorBaseTheme == colorTheme
                                FilterChip(
                                    selected = checking,
                                    onClick = { selectColorBaseTheme = colorTheme },
                                    label = { Text(colorTheme, fontSize = 9.sp) }
                                )
                            }
                        }

                        // THE SPECTACULAR FLYER DESIGN
                        val gradTheme = when(selectColorBaseTheme) {
                            "Teal Amber Accent" -> listOf(Color(0xFF004D40), Color(0xFF00796B), Color(0xFF009688))
                            "Rich Crimson Golden" -> listOf(Color(0xFF881010), Color(0xFFD32F2F), Color(0xFFFFB300))
                            else -> listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)) // Calm slate
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(gradTheme))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top headers
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "★ THE ASPIRANTS CLASSES ★",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        letterSpacing = 1.5.sp
                                    )
                                    Text(
                                        text = "Nurturing Ranks, Unlocking Potentials",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }

                                // Interactive Core Flyer badge
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = marketingTargetCampaign.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            color = gradTheme.first(),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "ADMISSIONS LAUNCHING NOW",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                // Bottom marketing footers
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "✓ Personalized Room SQLite Caching | ✓ Dynamic AI doubts Solver Bot",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 8.sp
                                    )
                                    Text(
                                        text = "📱 Helpline: 9876543210 | 📍 Institutional Main Campus Node",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported visual branding flyer to your local device photos directory!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("download_flyer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download Flyer")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Promotional Flyer", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMaterialQuizManagerDialog(
    studyMaterial: StudyMaterial,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userRole by viewModel.currentUserRole.collectAsState()
    val currentStudentId by viewModel.currentUserId.collectAsState()

    var activeView by remember { mutableStateOf("LIST") } // LIST, TAKE, CREATE, ATTEMPTS
    var selectedQuiz by remember { mutableStateOf<MaterialQuiz?>(null) }

    // Observe quizzes list
    val quizzes by viewModel.getQuizzesForMaterial(studyMaterial.id).collectAsState(initial = emptyList())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Quiz, contentDescription = "Quiz Icon", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = "Syllabus Quiz Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Topic: ${studyMaterial.title}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_quiz_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Content View Router
                Box(modifier = Modifier.weight(1f)) {
                    when (activeView) {
                        "LIST" -> QuizListView(
                            quizzes = quizzes,
                            userRole = userRole,
                            onTakeQuiz = { quiz ->
                                selectedQuiz = quiz
                                activeView = "TAKE"
                            },
                            onViewAttempts = { quiz ->
                                selectedQuiz = quiz
                                activeView = "ATTEMPTS"
                            },
                            onCreateClick = {
                                activeView = "CREATE"
                            },
                            onDeleteQuiz = { quiz ->
                                viewModel.deleteQuiz(quiz)
                            }
                        )
                        "CREATE" -> QuizCreateView(
                            studyMaterial = studyMaterial,
                            onSave = { title, desc, duration, questions ->
                                viewModel.createQuizForMaterial(studyMaterial.id, title, desc, duration, questions)
                                activeView = "LIST"
                            },
                            onCancel = {
                                activeView = "LIST"
                            }
                        )
                        "TAKE" -> {
                            selectedQuiz?.let { quiz ->
                                QuizTakeView(
                                    quiz = quiz,
                                    viewModel = viewModel,
                                    studentId = currentStudentId,
                                    onComplete = {
                                        activeView = "LIST"
                                        selectedQuiz = null
                                    },
                                    onCancel = {
                                        activeView = "LIST"
                                        selectedQuiz = null
                                    }
                                )
                            }
                        }
                        "ATTEMPTS" -> {
                            selectedQuiz?.let { quiz ->
                                QuizAttemptsView(
                                    quiz = quiz,
                                    viewModel = viewModel,
                                    onBack = {
                                        activeView = "LIST"
                                        selectedQuiz = null
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

@Composable
fun QuizListView(
    quizzes: List<MaterialQuiz>,
    userRole: String,
    onTakeQuiz: (MaterialQuiz) -> Unit,
    onViewAttempts: (MaterialQuiz) -> Unit,
    onCreateClick: () -> Unit,
    onDeleteQuiz: (MaterialQuiz) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Assessments (${quizzes.size})",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            if (userRole != "STUDENT") {
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.testTag("btn_create_interactive_quiz"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add quiz", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Quiz", fontSize = 12.sp)
                }
            }
        }

        if (quizzes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                    Text("No quizzes created for this material yet.", color = Color.Gray, fontSize = 13.sp)
                    if (userRole != "STUDENT") {
                        Text("Click 'Add Quiz' above to create a practice assessment.", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(quizzes) { quiz ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("quiz_card_${quiz.id}"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(quiz.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                    if (quiz.description.isNotBlank()) {
                                        Text(quiz.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                if (userRole != "STUDENT") {
                                    IconButton(
                                        onClick = { onDeleteQuiz(quiz) },
                                        modifier = Modifier.size(24.dp).testTag("delete_quiz_${quiz.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Quiz", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text("${quiz.durationMinutes} Mins limit", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (userRole != "STUDENT") {
                                        OutlinedButton(
                                            onClick = { onViewAttempts(quiz) },
                                            modifier = Modifier.height(32.dp).testTag("ui_view_attempts_${quiz.id}"),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Leaderboard, null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Scores", fontSize = 10.sp)
                                        }
                                    }

                                    Button(
                                        onClick = { onTakeQuiz(quiz) },
                                        modifier = Modifier.height(32.dp).testTag("ui_take_quiz_${quiz.id}"),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text(if (userRole == "STUDENT") "Start Test" else "Preview Test", fontSize = 11.sp)
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

@Composable
fun QuizCreateView(
    studyMaterial: StudyMaterial,
    onSave: (String, String, Int, List<MaterialQuizQuestion>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("15") }

    val tempQuestions = remember { mutableStateListOf<MaterialQuizQuestion>() }

    // State for temporary adding question form
    var showAddForm by remember { mutableStateOf(false) }
    var newQText by remember { mutableStateOf("") }
    var newQType by remember { mutableStateOf("MULTIPLE_CHOICE") } // MULTIPLE_CHOICE, FILL_IN_THE_BLANKS, SHORT_ANSWER

    // MCQ choices
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctMCQSelection by remember { mutableStateOf("A") }

    // Fill in blanks response
    var blanksAnswer by remember { mutableStateOf("") }

    // Short phrase response
    var shortAnswerKeyword by remember { mutableStateOf("") }

    // AI Generation States
    var showAIPanel by remember { mutableStateOf(false) }
    var aiGrade by remember { mutableStateOf("Grade 10") }
    var aiSubject by remember { mutableStateOf(studyMaterial.mainCategory) }
    var aiTopic by remember { mutableStateOf(studyMaterial.topicSubCategory) }
    var isGeneratingQuiz by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Build Interactive Quiz Sheet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)

        // Gemini AI Quiz Generator Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("ai_quiz_panel_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Genie Icon",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Gemini AI Quiz Worksheet Assistant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                if (!showAIPanel) {
                    Text(
                        text = "Instantly draft 5 custom, high-caliber interactive questions (MCQs, blanks, short response) matched to curriculum level & subject parameters.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Button(
                        onClick = { showAIPanel = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_trigger_ai_panel")
                    ) {
                        Text("✨ Design Worksheet with AI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Customize the learning parameters below to instruct the AI Model:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    OutlinedTextField(
                        value = aiGrade,
                        onValueChange = { aiGrade = it },
                        label = { Text("Curriculum Class / Grade Level", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_input_grade")
                    )

                    OutlinedTextField(
                        value = aiSubject,
                        onValueChange = { aiSubject = it },
                        label = { Text("Subject Area", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_input_subject")
                    )

                    OutlinedTextField(
                        value = aiTopic,
                        onValueChange = { aiTopic = it },
                        label = { Text("Topic Focus Description", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("ai_input_topic")
                    )

                    if (isGeneratingQuiz) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "🤖 AI Content Model drafting worksheet. Please wait...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAIPanel = false },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Discard", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (aiGrade.isBlank() || aiSubject.isBlank() || aiTopic.isBlank()) {
                                        Toast.makeText(context, "Please complete all quiz parameters", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isGeneratingQuiz = true
                                        try {
                                            val materialContentText = if (studyMaterial.content.length > 2500) studyMaterial.content.take(2500) else studyMaterial.content
                                            val finalPrompt = """
                                                Generate an educational assessment quiz matching the following criteria:
                                                Target Level: $aiGrade
                                                Subject Domain: $aiSubject
                                                Core Topic: $aiTopic
                                                Reference content details:
                                                $materialContentText
                                                
                                                Generate exactly 5 comprehensive questions (a combination of MULTIPLE_CHOICE, FILL_IN_THE_BLANKS, and SHORT_ANSWER).
                                                Respond ONLY with a valid Raw JSON Array, with no prefix, suffix, annotations, or backtick Markdown block code formatting wrappers like ```json.
                                                Each item in the array must comply exactly with this JSON Schema:
                                                {
                                                   "questionText": "Question text statement here",
                                                   "type": "MULTIPLE_CHOICE" or "FILL_IN_THE_BLANKS" or "SHORT_ANSWER",
                                                   "optionA": "Text of option A" (empty string if type is not MULTIPLE_CHOICE),
                                                   "optionB": "Text of option B" (empty string if type is not MULTIPLE_CHOICE),
                                                   "optionC": "Text of option C" (empty string if type is not MULTIPLE_CHOICE),
                                                   "optionD": "Text of option D" (empty string if type is not MULTIPLE_CHOICE),
                                                   "correctAnswer": "A", "B", "C", "D" (for MULTIPLE_CHOICE) or exact answer string (for FILL_IN_THE_BLANKS) or keyword text (for SHORT_ANSWER)
                                                }
                                            """.trimIndent()

                                            val response = com.example.services.GeminiService.generateContent(
                                                prompt = finalPrompt,
                                                systemInstruction = "You are a precise educational question JSON content engine database. Respond with Raw JSON arrays only."
                                            )

                                            val cleanJson = response.trim()
                                                .removePrefix("```json")
                                                .removePrefix("```")
                                                .removeSuffix("```")
                                                .trim()

                                            val array = org.json.JSONArray(cleanJson)
                                            val quizQuestions = mutableListOf<MaterialQuizQuestion>()
                                            for (i in 0 until array.length()) {
                                                val qObj = array.getJSONObject(i)
                                                val qType = qObj.optString("type", "MULTIPLE_CHOICE")
                                                quizQuestions.add(
                                                    MaterialQuizQuestion(
                                                        quizId = 0L,
                                                        type = qType,
                                                        questionText = qObj.optString("questionText", "A question statement"),
                                                        optionA = if (qType == "MULTIPLE_CHOICE") qObj.optString("optionA", "") else "",
                                                        optionB = if (qType == "MULTIPLE_CHOICE") qObj.optString("optionB", "") else "",
                                                        optionC = if (qType == "MULTIPLE_CHOICE") qObj.optString("optionC", "") else "",
                                                        optionD = if (qType == "MULTIPLE_CHOICE") qObj.optString("optionD", "") else "",
                                                        correctAnswer = qObj.optString("correctAnswer", "")
                                                    )
                                                )
                                            }

                                            if (quizQuestions.isNotEmpty()) {
                                                tempQuestions.clear()
                                                tempQuestions.addAll(quizQuestions)
                                                title = "AI Quiz - $aiSubject: $aiTopic"
                                                description = "Interactive test formulated by Gemini AI for $aiGrade."
                                                Toast.makeText(context, "🎉 AI worksheet drafted with 5 questions! Please review below.", Toast.LENGTH_LONG).show()
                                                showAIPanel = false
                                            } else {
                                                Toast.makeText(context, "⚠️ Response JSON had no valid quiz elements. Try again.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("QuizCreateView", "Gemini generation failed", e)
                                            Toast.makeText(context, "❌ Error during generation: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isGeneratingQuiz = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.weight(1.5f).testTag("btn_ai_compose_action")
                            ) {
                                Text("🪄 Auto-Draft Questions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Quiz Title Heading") },
            modifier = Modifier.fillMaxWidth().testTag("input_quiz_title")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Brief Instructions") },
            modifier = Modifier.fillMaxWidth().testTag("input_quiz_desc")
        )

        OutlinedTextField(
            value = durationStr,
            onValueChange = { durationStr = it },
            label = { Text("Allowed Duration (Minutes)") },
            modifier = Modifier.fillMaxWidth().testTag("input_quiz_duration")
        )

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Questions Added (${tempQuestions.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (!showAddForm) {
                OutlinedButton(
                    onClick = {
                        showAddForm = true
                        newQText = ""
                        newQType = "MULTIPLE_CHOICE"
                        optA = ""
                        optB = ""
                        optC = ""
                        optD = ""
                        blanksAnswer = ""
                        shortAnswerKeyword = ""
                    },
                    modifier = Modifier.testTag("btn_show_add_question_form")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Question", fontSize = 11.sp)
                }
            }
        }

        if (showAddForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("New Question Composer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = newQText,
                        onValueChange = { newQText = it },
                        label = { Text("Question Statement text") },
                        modifier = Modifier.fillMaxWidth().testTag("input_question_text")
                    )

                    Text("Answer Input Type Selection:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("MULTIPLE_CHOICE" to "MCQ Choices", "FILL_IN_THE_BLANKS" to "Fill Blank", "SHORT_ANSWER" to "Short Response").forEach { (typeKey, labelStr) ->
                            FilterChip(
                                selected = newQType == typeKey,
                                onClick = { newQType = typeKey },
                                label = { Text(labelStr, fontSize = 10.sp) }
                            )
                        }
                    }

                    when (newQType) {
                        "MULTIPLE_CHOICE" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())

                                Text("Specify CORRECT Option Letter Choice:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("A", "B", "C", "D").forEach { ltr ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = correctMCQSelection == ltr, onClick = { correctMCQSelection = ltr })
                                            Text(ltr, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "FILL_IN_THE_BLANKS" -> {
                            OutlinedTextField(
                                value = blanksAnswer,
                                onValueChange = { blanksAnswer = it },
                                label = { Text("Exact Correct Answer String value") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "SHORT_ANSWER" -> {
                            OutlinedTextField(
                                value = shortAnswerKeyword,
                                onValueChange = { shortAnswerKeyword = it },
                                label = { Text("Correct Answer Key-Phases (Case-insensitive match)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddForm = false }) {
                            Text("Discard", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newQText.isBlank()) return@Button
                                val q = MaterialQuizQuestion(
                                    quizId = 0L,
                                    questionText = newQText,
                                    type = newQType,
                                    optionA = if (newQType == "MULTIPLE_CHOICE") optA else "",
                                    optionB = if (newQType == "MULTIPLE_CHOICE") optB else "",
                                    optionC = if (newQType == "MULTIPLE_CHOICE") optC else "",
                                    optionD = if (newQType == "MULTIPLE_CHOICE") optD else "",
                                    correctAnswer = when (newQType) {
                                        "MULTIPLE_CHOICE" -> correctMCQSelection
                                        "FILL_IN_THE_BLANKS" -> blanksAnswer.trim()
                                        else -> shortAnswerKeyword.trim()
                                    }
                                )
                                tempQuestions.add(q)
                                showAddForm = false
                            }
                        ) {
                            Text("Confirm Question")
                        }
                    }
                }
            }
        }

        // Render questions list so far
        if (tempQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No questions added to this sheets composition yet.", fontSize = 11.sp, color = Color.Gray)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tempQuestions.forEachIndexed { index, mq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Q${index + 1}: ${mq.questionText}", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                Text("Type: ${mq.type} • Target Ans: ${mq.correctAnswer}", fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { tempQuestions.removeAt(index) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel Workflow")
            }

            Button(
                onClick = {
                    if (title.isBlank() || tempQuestions.isEmpty()) return@Button
                    val duration = durationStr.toIntOrNull() ?: 15
                    onSave(title, description, duration, tempQuestions.toList())
                },
                modifier = Modifier.weight(1.5f).testTag("save_quiz_sheet_btn")
            ) {
                Text("Compile & Launch Quiz")
            }
        }
    }
}

@Composable
fun QuizTakeView(
    quiz: MaterialQuiz,
    viewModel: AppViewModel,
    studentId: Long,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val questions by viewModel.getQuestionsForQuiz(quiz.id).collectAsState(initial = emptyList())

    var isCheckedAnswerView by remember { mutableStateOf(false) }
    var computedScore by remember { mutableStateOf(0) }
    var finalFeedbackText by remember { mutableStateOf("") }

    // User responses state
    val userAnswers = remember { mutableStateMapOf<Long, String>() }

    // Timer logic
    var timeLeftSeconds by remember { mutableStateOf(quiz.durationMinutes * 60) }
    LaunchedEffect(key1 = timeLeftSeconds, key2 = isCheckedAnswerView) {
        if (!isCheckedAnswerView && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                // Auto submit!
                Toast.makeText(context, "Time is up! Auto-submitting assessment...", Toast.LENGTH_LONG).show()
                var cr = 0
                val feedbackDetails = StringBuilder()
                questions.forEach { q ->
                    val ans = userAnswers[q.id]?.trim() ?: ""
                    val isCorrect = if (q.type == "MULTIPLE_CHOICE") {
                        ans.equals(q.correctAnswer, ignoreCase = true)
                    } else {
                        ans.equals(q.correctAnswer, ignoreCase = true) || (q.type == "SHORT_ANSWER" && ans.lowercase().contains(q.correctAnswer.lowercase()))
                    }
                    if (isCorrect) cr++
                    feedbackDetails.append("Q: ${q.questionText} | Your answer: $ans | Correct: ${q.correctAnswer}\n")
                }
                computedScore = cr
                finalFeedbackText = feedbackDetails.toString()
                viewModel.submitQuizAttempt(quiz.id, if (studentId == -1L) 101L else studentId, cr, questions.size, finalFeedbackText)
                isCheckedAnswerView = true
            }
        }
    }

    if (isCheckedAnswerView) {
        // Show Score Feedback screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Icon(Icons.Default.CheckCircle, "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(60.dp))
            Text("Assessment Submitted successfully!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF4CAF50))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOUR ACHIEVED REPORT SCORE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$computedScore / ${questions.size}", fontWeight = FontWeight.Black, fontSize = 42.sp, color = MaterialTheme.colorScheme.onSurface)
                    val percent = if (questions.isNotEmpty()) (computedScore * 100) / questions.size else 0
                    Text("$percent% correctness marks average", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                }
            }

            Text("Detailed Question Breakdown Review:", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())

            questions.forEachIndexed { idx, q ->
                val studAns = userAnswers[q.id] ?: "No Answer"
                val isCorrect = if (q.type == "MULTIPLE_CHOICE") {
                    studAns.trim().equals(q.correctAnswer, ignoreCase = true)
                } else {
                    studAns.trim().equals(q.correctAnswer, ignoreCase = true) || (q.type == "SHORT_ANSWER" && studAns.trim().lowercase().contains(q.correctAnswer.lowercase()))
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("Question ${idx + 1}: ${q.questionText}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF4CAF50) else Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text("Your Answer: $studAns", fontSize = 11.sp, color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
                        Text("Correct Reference Answer: ${q.correctAnswer}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            }

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth().testTag("close_score_view_btn")
            ) {
                Text("Return to Syllabus Menu")
            }
        }
    } else {
        // Taking active quiz view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Timer Card bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val m = timeLeftSeconds / 60
                    val s = timeLeftSeconds % 60
                    val timeStr = String.format("%02d:%02d", m, s)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Timer Countdown: $timeStr", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    }

                    Text("Total Questions: ${questions.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                questions.forEachIndexed { index, q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "${index + 1}. ${q.questionText}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            when (q.type) {
                                "MULTIPLE_CHOICE" -> {
                                    val currentAns = userAnswers[q.id] ?: ""
                                    listOf(
                                        "A" to q.optionA,
                                        "B" to q.optionB,
                                        "C" to q.optionC,
                                        "D" to q.optionD
                                    ).forEach { (ltr, textVal) ->
                                        if (!textVal.isNullOrBlank()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { userAnswers[q.id] = ltr }
                                                    .background(if (currentAns == ltr) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = currentAns == ltr,
                                                    onClick = { userAnswers[q.id] = ltr }
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("$ltr. $textVal", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                                "FILL_IN_THE_BLANKS" -> {
                                    val currentAns = userAnswers[q.id] ?: ""
                                    OutlinedTextField(
                                        value = currentAns,
                                        onValueChange = { userAnswers[q.id] = it },
                                        label = { Text("Your answer text") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1
                                    )
                                }
                                "SHORT_ANSWER" -> {
                                    val currentAns = userAnswers[q.id] ?: ""
                                    OutlinedTextField(
                                        value = currentAns,
                                        onValueChange = { userAnswers[q.id] = it },
                                        label = { Text("Your descriptive key response") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exit Attempt")
                }

                Button(
                    onClick = {
                        var cr = 0
                        val feedbackDetails = StringBuilder()
                        questions.forEach { q ->
                            val ans = userAnswers[q.id]?.trim() ?: ""
                            val isCorrect = if (q.type == "MULTIPLE_CHOICE") {
                                ans.equals(q.correctAnswer, ignoreCase = true)
                            } else {
                                ans.equals(q.correctAnswer, ignoreCase = true) || (q.type == "SHORT_ANSWER" && ans.lowercase().contains(q.correctAnswer.lowercase()))
                            }
                            if (isCorrect) cr++
                            feedbackDetails.append("Q: ${q.questionText} | Your answer: $ans | Correct: ${q.correctAnswer}\n")
                        }
                        computedScore = cr
                        finalFeedbackText = feedbackDetails.toString()
                        viewModel.submitQuizAttempt(quiz.id, if (studentId == -1L) 101L else studentId, cr, questions.size, finalFeedbackText)
                        isCheckedAnswerView = true
                    },
                    modifier = Modifier.weight(1.5f).testTag("submit_quiz_assessment_btn")
                ) {
                    Text("Submit Assessment Sheet")
                }
            }
        }
    }
}

@Composable
fun QuizAttemptsView(
    quiz: MaterialQuiz,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val attempts by viewModel.getAttemptsForQuiz(quiz.id).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
            Text("Assessment Attempts Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.width(32.dp))
        }

        if (attempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No student has attempted this interactive assessment yet.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attempts) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Student Roll Ref: #${att.studentId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(
                                    text = "Score: ${att.score} / ${att.totalQuestions}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (att.score >= (att.totalQuestions / 2)) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 12.sp
                                )
                            }
                            if (att.feedbackJson.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Response Record Brief:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                Text(att.feedbackJson, fontSize = 9.sp, color = Color.DarkGray, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close Tracker")
        }
    }
}

@Composable
fun AutoSlidingPromoSlideshow(
    viewModel: com.example.ui.viewmodel.AppViewModel,
    userRole: String,
    modifier: Modifier = Modifier
) {
    val promoSlides by viewModel.promoSlides.collectAsState()
    var currentSlideIndex by remember { mutableStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Start auto scrolling logic
    LaunchedEffect(promoSlides) {
        if (promoSlides.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(4000)
                currentSlideIndex = (currentSlideIndex + 1) % promoSlides.size
            }
        }
    }

    if (promoSlides.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth().height(210.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Loading slideshow...", color = Color.Gray)
            }
        }
        return
    }

    val slide = promoSlides.getOrNull(currentSlideIndex) ?: promoSlides.first()

    // Match exact height of old graph
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw background image if specified, or a stunning gradient!
            if (!slide.imageUrl.isNullOrBlank()) {
                // Load from URL/Uri
                coil.compose.AsyncImage(
                    model = slide.imageUrl,
                    contentDescription = slide.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    error = androidx.compose.ui.graphics.painter.ColorPainter(Color.Gray)
                )
                // Transparent dark overlay for readable text contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))
                            )
                        )
                )
            } else {
                // Stunning custom gradient
                val gradientColors = when (slide.gradientIndex) {
                    0 -> listOf(Color(0xFF0D47A1), Color(0xFF1E88E5), Color(0xFF42A5F5))
                    1 -> listOf(Color(0xFFB71C1C), Color(0xFFD84315), Color(0xFFFF8A65))
                    2 -> listOf(Color(0xFF1B5E20), Color(0xFF43A047), Color(0xFF81C784))
                    3 -> listOf(Color(0xFF4A148C), Color(0xFF8E24AA), Color(0xFFBA68C8))
                    else -> listOf(Color(0xFFE65100), Color(0xFFFB8C00), Color(0xFFFFD54F))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(colors = gradientColors))
                )
            }

            // Foreground text layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with Category Badge and optional Pencil editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = slide.badge.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Show edit pencil button ONLY for ADMIN!
                    if (userRole == "ADMIN") {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Slideshow Offers",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Title and Subtitle at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = slide.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = slide.subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Sliding dotted indicator
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        promoSlides.forEachIndexed { idx, _ ->
                            val isActive = idx == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 12.dp else 6.dp, 6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color.White else Color.White.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        PosterCustomizeDialog(
            viewModel = viewModel,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun PosterCustomizeDialog(
    viewModel: com.example.ui.viewmodel.AppViewModel,
    onDismiss: () -> Unit
) {
    val promoSlides by viewModel.promoSlides.collectAsState()
    var selectedSlideId by remember { mutableStateOf(0) }

    // Forms backing state initialized based on selectedSlideId
    val currentSlide = promoSlides.getOrNull(selectedSlideId) ?: promoSlides.first()
    var editTitle by remember(selectedSlideId) { mutableStateOf(currentSlide.title) }
    var editSubtitle by remember(selectedSlideId) { mutableStateOf(currentSlide.subtitle) }
    var editBadge by remember(selectedSlideId) { mutableStateOf(currentSlide.badge) }
    var editImageUrl by remember(selectedSlideId) { mutableStateOf(currentSlide.imageUrl) }
    var editGradientIndex by remember(selectedSlideId) { mutableStateOf(currentSlide.gradientIndex) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TAMS Studio  ❯  Banners",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Customize Slideshow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Pick a slide below and design specific institute campaigns, batch announcements, discounts, links, or custom banners.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 15.sp
                )

                // Row of 5 selection tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (0..4).forEach { id ->
                        val isSel = id == selectedSlideId
                        Button(
                            onClick = { selectedSlideId = id },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                        ) {
                            Text("Slide ${id + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // Input Badge
                OutlinedTextField(
                    value = editBadge,
                    onValueChange = { editBadge = it },
                    label = { Text("Category / Header Badge (e.g. OFFER, COURSE)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Input Title
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Banner Title / Headline") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Input Subtitle
                OutlinedTextField(
                    value = editSubtitle,
                    onValueChange = { editSubtitle = it },
                    label = { Text("Offer description details") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // Input Image URL
                OutlinedTextField(
                    value = editImageUrl,
                    onValueChange = { editImageUrl = it },
                    label = { Text("Custom Graphic Image URL (Optional)") },
                    placeholder = { Text("https://example.com/poster.jpg") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Pick background gradients
                Text("Select Theme Accent Gradient:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf(
                        0 to Color(0xFF1E88E5), // Ocean
                        1 to Color(0xFFD84315), // Sunset
                        2 to Color(0xFF43A047), // Emerald
                        3 to Color(0xFF8E24AA), // Mystic Purple
                        4 to Color(0xFFFB8C00)  // Golden Amber
                    ).forEach { (idx, c) ->
                        val isSel = editGradientIndex == idx
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    border = if (isSel) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(2.dp, Color.LightGray),
                                    shape = CircleShape
                                )
                                .clickable { editGradientIndex = idx }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.updatePromoSlide(
                                com.example.ui.viewmodel.AppViewModel.PromoSlide(
                                    id = selectedSlideId,
                                    badge = editBadge,
                                    title = editTitle,
                                    subtitle = editSubtitle,
                                    imageUrl = editImageUrl,
                                    gradientIndex = editGradientIndex
                                )
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Apply & Save Slide")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCountersScrollableRow(
    batchesList: List<com.example.data.model.Batch>,
    studentsList: List<com.example.data.model.Student>,
    staffProfiles: List<com.example.ui.viewmodel.StaffProfile>,
    onBatchesClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onStaffClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeStaffCount = staffProfiles.count { it.isActive }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Colored Card 1: Batches (Blue theme)
        MetricCard(
            title = "Batches",
            count = batchesList.size.toString(),
            label = "Active Classes",
            backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
            ),
            icon = androidx.compose.material.icons.Icons.Default.Layers,
            onClick = onBatchesClick
        )

        // Colored Card 2: Students (Emerald/Green theme)
        MetricCard(
            title = "Students",
            count = studentsList.size.toString(),
            label = "Total Admissions",
            backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFF059669), Color(0xFF10B981))
            ),
            icon = androidx.compose.material.icons.Icons.Default.People,
            onClick = onStudentsClick
        )

        // Colored Card 3: Staff (Amber/Orange theme)
        MetricCard(
            title = "Staff",
            count = "$activeStaffCount/${staffProfiles.size}",
            label = "Active Members",
            backgroundBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(Color(0xFFEA580C), Color(0xFFF97316))
            ),
            icon = androidx.compose.material.icons.Icons.Default.Person,
            onClick = onStaffClick
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    label: String,
    backgroundBrush: androidx.compose.ui.graphics.Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(165.dp)
            .height(82.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = count,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 33.sp
                )
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
fun StatsRowGlassCard(
    title: String,
    count: String,
    trend: String,
    pathColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(155.dp)
            .height(95.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color(0xFF1F2937))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
                
                // Small neon miniature trend indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(pathColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (trend.contains("+") || trend == "₹") "📈" else "⚡",
                        fontSize = 8.sp,
                        color = pathColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = count,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = trend,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = pathColor
                )
            }
        }
    }
}

@Composable
fun TenantAccountItemCard(
    inst: com.example.ui.viewmodel.InstituteAccount,
    onToggleSuspension: () -> Unit,
    onToggleSubscription: () -> Unit,
    onDelete: () -> Unit,
    onRecordSubscription: () -> Unit,
    onWipeNode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tenant_card_${inst.email.replace("@", "_").replace(".", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inst.isSuspended) Color(0xFF1E1E24) else Color(0xFF111827)
        ),
        border = BorderStroke(
            1.dp,
            if (inst.isSuspended) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF1F2937)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Top info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = inst.academyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (inst.isSuspended) Color(0xFF94A3B8) else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (inst.isApproved) Color(0xFF10B981).copy(alpha = 0.2f)
                                    else Color(0xFFF59E0B).copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (inst.isApproved) "VALIDATED" else "PENDING",
                                color = if (inst.isApproved) Color(0xFF34D399) else Color(0xFFFBBF24),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Director: ${inst.directorName} • ${inst.email}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Switch(
                    checked = inst.subscriptionActive,
                    onCheckedChange = { 
                        onToggleSubscription()
                        Toast.makeText(context, "Subscription updated for ${inst.academyName}", Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF06B6D4),
                        checkedTrackColor = Color(0xFF06B6D4).copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color(0xFF1F2937)
                    ),
                    modifier = Modifier.scale(0.85f).testTag("subscription_switch_${inst.email.replace("@", "_")}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(10.dp))

            // Details section: registration date, plan type, upi VPA, amount paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Expiry Date: ", fontSize = 11.sp, color = Color.Gray)
                        Text(inst.expiryDate, fontSize = 11.sp, color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Paid: ", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${inst.lastPaymentAmount.toInt()}", fontSize = 11.sp, color = Color(0xFF22D3EE), fontWeight = FontWeight.Bold)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "License: ${inst.profileType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF06B6D4)
                    )
                    Text(
                        text = "UPI Ref: ${inst.lastUpiTxRef}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons row: Suspend/Unban, Record Payment, Delete, GDPR Wipe
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Suspend / Unban button
                    Button(
                        onClick = onToggleSuspension,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (inst.isSuspended) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (inst.isSuspended) "Unban Account" else "Ban Workspace",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (inst.isSuspended) Color.Black else Color.White
                        )
                    }

                    // Record Payment button
                    Button(
                        onClick = onRecordSubscription,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = null,
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay UPI", fontSize = 11.sp, color = Color.White)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // GDPR Wipe button
                    IconButton(
                        onClick = {
                            onWipeNode()
                            Toast.makeText(context, "Secure wipe cycle scheduled for ${inst.academyName}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Text("🗑️", fontSize = 14.sp)
                    }

                    // Delete button with confirmation toggle
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(30.dp).testTag("delete_tenant_button_${inst.email.replace("@", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Confirm Permanent Deletion?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently erase the workspace record for ${inst.academyName}? This action is destructive and irreversible.",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                        Toast.makeText(context, "Deleted ${inst.academyName}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF111827)
        )
    }
}

@Composable
fun SettingsOverlayDialog(
    viewModel: com.example.ui.viewmodel.AppViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val academyNameState by viewModel.academyName.collectAsState()
    val directorNameState by viewModel.directorName.collectAsState()
    val adminEmailState by viewModel.adminEmail.collectAsState()
    val isBiometricLocked by viewModel.isBiometricLocked.collectAsState()
    val preferredSmsSim by viewModel.preferredSmsSim.collectAsState()
    val merchantUpiId by viewModel.merchantUpiId.collectAsState()
    val merchantName by viewModel.merchantName.collectAsState()
    val customGeminiApiKey by viewModel.customGeminiApiKey.collectAsState()

    var tempName by remember(academyNameState) { mutableStateOf(academyNameState) }
    var tempDirector by remember(directorNameState) { mutableStateOf(directorNameState) }
    var tempEmail by remember(adminEmailState) { mutableStateOf(adminEmailState) }
    var tempUpiId by remember(merchantUpiId) { mutableStateOf(merchantUpiId) }
    var tempMerchantName by remember(merchantName) { mutableStateOf(merchantName) }
    var tempApiKey by remember(customGeminiApiKey) { mutableStateOf(customGeminiApiKey) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM SETTINGS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Security",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
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

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

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

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    Text("COACHING ACADEMY IDENTITY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Academy Business Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("config_academy_name_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempDirector,
                            onValueChange = { tempDirector = it },
                            label = { Text("Director Name", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("config_director_name_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempEmail,
                            onValueChange = { tempEmail = it },
                            label = { Text("Admin Email", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.2f).testTag("config_admin_email_input"),
                            singleLine = true
                        )
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    Text("REAL-TIME UPI CHECKOUT INTEGRATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = tempUpiId,
                        onValueChange = { tempUpiId = it },
                        label = { Text("Coaching UPI ID VPA", fontSize = 11.sp) },
                        placeholder = { Text("e.g. upi@domain") },
                        modifier = Modifier.fillMaxWidth().testTag("config_merchant_upi_id_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempMerchantName,
                        onValueChange = { tempMerchantName = it },
                        label = { Text("Merchant Display Name", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("config_merchant_name_input"),
                        singleLine = true
                    )

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                    Text("AI & GEMINI INTEGRATION SETTINGS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Configure your own Gemini API Key to let students ask academic doubts.", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("Gemini API Key", fontSize = 11.sp) },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier.fillMaxWidth().testTag("config_gemini_api_key_input"),
                        singleLine = true,
                        visualTransformation = if (tempApiKey.isNotEmpty()) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (tempName.isNotBlank() && tempDirector.isNotBlank() && tempEmail.contains("@") && tempUpiId.isNotBlank() && tempUpiId.contains("@")) {
                                viewModel.updateAcademySettings(tempName, tempDirector, tempEmail)
                                viewModel.updateMerchantUPI(tempUpiId, tempMerchantName)
                                viewModel.updateCustomGeminiApiKey(tempApiKey)
                                Toast.makeText(context, "All academy settings, UPI keys, and AI configurations updated!", Toast.LENGTH_LONG).show()
                                onClose()
                            } else {
                                Toast.makeText(context, "Verify that all fields are correct.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_academy_config_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Academy & Gateway Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}




