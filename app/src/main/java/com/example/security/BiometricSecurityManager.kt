package com.example.security

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAuthStatus {
    READY,
    NONE_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    UNSUPPORTED
}

object BiometricSecurityManager {

    /**
     * Checks if biometric authentication or device credentials (PIN/Pattern) are available on this device.
     */
    fun checkBiometricStatus(context: Context): BiometricAuthStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAuthStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAuthStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAuthStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAuthStatus.HW_UNAVAILABLE
            else -> BiometricAuthStatus.UNSUPPORTED
        }
    }

    /**
     * Launches the native BiometricPrompt API for Fingerprint, Face, or Device Lock authentication.
     */
    fun promptAuthentication(
        activity: FragmentActivity,
        title: String = "Biometric Verification 🛡️",
        subtitle: String = "Scan fingerprint, face recognition, or enter PIN",
        description: String = "Authenticate to access confidential records and transaction logs.",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_CANCELED -> {
                            onError("Authentication cancelled by user.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT -> {
                            onError("Too many failed attempts. Try again in 30 seconds.")
                        }
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Biometrics locked permanently until device PIN unlock.")
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                            onError("No fingerprints or face data enrolled on this device.")
                        }
                        else -> {
                            onError(errString.toString())
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Prompt remains open; user can retry scanning
                }
            }
        )

        // Configure PromptInfo with biometric + device credential fallback
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Unable to launch BiometricPrompt.")
        }
    }
}

/**
 * Convenient Composable helper to launch BiometricPrompt from Compose UI.
 */
@Composable
fun rememberBiometricLauncher(
    onSuccess: () -> Unit,
    onError: (String) -> Unit = {}
): (title: String, subtitle: String) -> Unit {
    val context = LocalContext.current
    return remember(context, onSuccess, onError) {
        { title, subtitle ->
            val activity = context as? FragmentActivity
            if (activity != null) {
                BiometricSecurityManager.promptAuthentication(
                    activity = activity,
                    title = title,
                    subtitle = subtitle,
                    onSuccess = onSuccess,
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        onError(err)
                    }
                )
            } else {
                Toast.makeText(context, "Biometric security: Activity not available", Toast.LENGTH_SHORT).show()
                onError("Activity unavailable")
            }
        }
    }
}

/**
 * Material 3 Security Banner displayed above sensitive sections (Payment logs or Student records)
 */
@Composable
fun BiometricSecurityHeaderBanner(
    isProtected: Boolean,
    isUnlocked: Boolean,
    itemLabel: String, // e.g. "Student Records & Contacts" or "Payment Ledger & Fee Receipts"
    onUnlockRequest: () -> Unit,
    onLockRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isProtected) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFF81C784) else Color(0xFFFFB74D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) Color(0xFFC8E6C9) else Color(0xFFFFE0B2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Fingerprint,
                        contentDescription = "Biometric Status",
                        tint = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isUnlocked) "🔓 $itemLabel Unlocked" else "🔒 $itemLabel Protected",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                    Text(
                        text = if (isUnlocked)
                            "Authenticated with BiometricPrompt (Fingerprint/Face/PIN)."
                        else
                            "Biometric or device PIN required to reveal details & controls.",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isUnlocked) {
                OutlinedButton(
                    onClick = onLockRequest,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            } else {
                Button(
                    onClick = onUnlockRequest,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Scan",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Locked Barrier Component shown when sensitive data (student directory or payment history) is locked.
 */
@Composable
fun BiometricLockedBarrierCard(
    title: String,
    description: String,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometrics",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onUnlockClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                modifier = Modifier.fillMaxWidth(0.85f).height(46.dp)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Authenticate with Biometrics / PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}
