package com.example.kawansekolah.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.data.SeedDataHelper
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.guru.TeacherProfileScreen
import com.example.kawansekolah.ui.login.LoginScreen
import com.example.kawansekolah.ui.splash.SplashScreen
import com.example.kawansekolah.ui.theme.ThemeMode
import com.example.kawansekolah.utils.NetworkObserver
import com.example.kawansekolah.utils.NotificationHelper
import com.example.kawansekolah.utils.UserSessionManager
import kotlinx.coroutines.launch

@Composable
fun MainApp(
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    initialNavigateTo: String? = null,
    onNavigateHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember(context) { UserSessionManager(context) }

    // Monitor Network Connectivity
    val networkObserver = remember(context) { NetworkObserver(context) }
    val isOnline by networkObserver.isOnline.collectAsState(initial = true)

    // State Splash Screen
    var showSplash by remember { mutableStateOf(true) }

    // Inisialisasi Emulator & Seed Data
    LaunchedEffect(Unit) {
        FirebaseEmulatorHelper.setupEmulators()
        coroutineScope.launch {
            SeedDataHelper.seedDatabase()
        }
    }

    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
    var userDisplayName by remember { mutableStateOf(sessionManager.getUserName()) }
    var userRole by remember { mutableStateOf(sessionManager.getUserRole()) }
    var needsTeacherProfile by remember { mutableStateOf(false) }
    var showLoginSuccessDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Banner Offline
        if (!isOnline) {
            Surface(
                color = Color(0xFFDC2626),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tidak ada koneksi internet. Periksa Wi-Fi atau data seluler Anda.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (showSplash) {
                SplashScreen(
                    onSplashFinished = {
                        showSplash = false
                    }
                )
            } else if (!isLoggedIn) {
                LoginScreen(
                    onLoginSuccessUser = { user ->
                        val finalName = user.name.ifBlank { "Pengguna" }
                        val role = user.userRole

                        sessionManager.saveSession(finalName, role)

                        userDisplayName = finalName
                        userRole = role
                        isLoggedIn = true

                        if (role == UserRole.GURU && user.teacherId.isBlank()) {
                            needsTeacherProfile = true
                        } else {
                            showLoginSuccessDialog = true
                        }

                        NotificationHelper.showSystemNotification(
                            context = context,
                            title = "Login Berhasil 🔔",
                            message = "Selamat datang kembali, $finalName! Akun ${role.label} Anda terhubung."
                        )
                    },
                    onDemoLoginSuccess = { demoName, demoRole ->
                        sessionManager.saveDemoSession(demoName, demoRole)
                        userDisplayName = demoName
                        userRole = demoRole
                        isLoggedIn = true
                        showLoginSuccessDialog = true
                    }
                )
            } else if (needsTeacherProfile) {
                TeacherProfileScreen(
                    onProfileSavedSuccess = {
                        needsTeacherProfile = false
                        showLoginSuccessDialog = true
                    }
                )
            } else {
                MainScreen(
                    userName = userDisplayName,
                    userRole = userRole,
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = {
                        sessionManager.clearSession()
                        isLoggedIn = false
                        needsTeacherProfile = false
                        userDisplayName = "Budi Santoso"
                        userRole = UserRole.SISWA
                    },
                    initialNavigateTo = initialNavigateTo,
                    onNavigateHandled = onNavigateHandled
                )

                if (showLoginSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showLoginSuccessDialog = false },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Login Berhasil!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        },
                        text = {
                            Text(
                                text = "Selamat datang di Scolarsia AI Analytics, $userDisplayName! 👋 Mode: ${userRole.label}.",
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { showLoginSuccessDialog = false },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Mulai Belajar")
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        }
    }
}
