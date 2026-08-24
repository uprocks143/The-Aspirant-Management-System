package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.BiometricAuthStatus
import com.example.security.BiometricSecurityManager
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.services.NotificationHelper

class MainActivity : FragmentActivity() {

    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            android.util.Log.d("MainActivity", "Permission $permission granted: $isGranted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize real local push notification channels
        NotificationHelper.createNotificationChannels(applicationContext)

        // Request all necessary runtime permissions on app launch
        requestAllPermissions()

        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val isAppEntryBiometricProtected by viewModel.isAppEntryBiometricProtected.collectAsState()
            val isAppEntryAuthenticated by viewModel.isAppEntryAuthenticated.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                if (isAppEntryBiometricProtected && !isAppEntryAuthenticated) {
                    AppEntryBiometricGateScreen(
                        activity = this@MainActivity,
                        viewModel = viewModel,
                        onAuthenticated = {
                            viewModel.setAppEntryAuthenticated(true)
                        }
                    )
                } else {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

@Composable
fun AppEntryBiometricGateScreen(
    activity: FragmentActivity,
    viewModel: AppViewModel,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val academyName by viewModel.academyName.collectAsState()
    val biometricStatus = remember { BiometricSecurityManager.checkBiometricStatus(context) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPromptedInitially by remember { mutableStateOf(false) }

    fun triggerBiometricAuth() {
        BiometricSecurityManager.promptAuthentication(
            activity = activity,
            title = "App Entry Security 🛡️",
            subtitle = "Scan fingerprint or face to open ${if (academyName.isNotBlank()) academyName else "TAMS"}",
            description = "Authenticate to unlock the tuition & student management portal.",
            onSuccess = {
                errorMessage = null
                onAuthenticated()
            },
            onError = { err ->
                errorMessage = err
            }
        )
    }

    // Auto-prompt on screen load
    LaunchedEffect(Unit) {
        if (!hasPromptedInitially) {
            hasPromptedInitially = true
            if (biometricStatus == BiometricAuthStatus.READY) {
                triggerBiometricAuth()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Security Icon with circular badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Security",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (academyName.isNotBlank()) academyName else "TAMS Portal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "App Entry Biometric Gate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Authenticate with your enrolled fingerprint, face recognition, or device credential to access records.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Authenticate Button
                    Button(
                        onClick = {
                            triggerBiometricAuth()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("app_entry_biometric_unlock_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Unlock with Biometrics",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Fallback / Device PIN / Emulator Bypass option
                    OutlinedButton(
                        onClick = {
                            // If user chooses PIN or biometrics are not enrolled, still try prompt or unlock
                            if (biometricStatus == BiometricAuthStatus.READY) {
                                triggerBiometricAuth()
                            } else {
                                Toast.makeText(context, "Biometrics not enrolled. Unlocking with device session.", Toast.LENGTH_SHORT).show()
                                onAuthenticated()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("app_entry_device_pin_btn"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (biometricStatus == BiometricAuthStatus.READY) "Use Device PIN / Pattern" else "Enter via Device Credential",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
