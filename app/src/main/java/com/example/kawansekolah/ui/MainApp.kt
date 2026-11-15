package com.example.kawansekolah.ui

import android.util.Log
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
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.data.SeedDataHelper
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AuthRepository
import com.example.kawansekolah.ui.login.LoginScreen
import com.example.kawansekolah.ui.login.RoleSelectionScreen
import com.example.kawansekolah.ui.splash.SplashScreen
import com.example.kawansekolah.ui.theme.ThemeMode
import com.example.kawansekolah.utils.DeviceSessionManager
import com.example.kawansekolah.utils.NetworkObserver
import com.example.kawansekolah.utils.NotificationHelper
import com.example.kawansekolah.utils.UserSessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

    // State Screens
    var showSplash by remember { mutableStateOf(true) }
    var selectedRoleForLogin by remember { mutableStateOf<UserRole?>(null) }

    var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn() || Firebase.auth.currentUser != null) }
    var userDisplayName by remember { mutableStateOf(sessionManager.getUserName()) }
    var userRole by remember { mutableStateOf(sessionManager.getUserRole()) }
    var showLoginSuccessDialog by remember { mutableStateOf(false) }

    var isRestoringSession by remember { mutableStateOf(false) }
    var sessionRestoreError by remember { mutableStateOf<String?>(null) }

    // Session restoration on startup from auth.currentUser + Firestore
    val restoreSession: () -> Unit = {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            isRestoringSession = true
            sessionRestoreError = null
            coroutineScope.launch {
                try {
                    val profile = AuthRepository.getUserProfile(currentUser.uid)
                    if (profile != null) {
                        val name = profile.name.ifBlank { "Pengguna" }
                        val role = profile.userRole
                        userDisplayName = name
                        userRole = role
                        sessionManager.saveSession(name, role)
                        isLoggedIn = true
                        Log.i("ScolarsiaAuth", "Sesi dipulihkan dari Firestore. UID: ${profile.uid}, Role: ${profile.role}, Name: $name")
                    } else {
                        Log.w("ScolarsiaAuth", "Dokumen profil tidak ditemukan pada server untuk UID: ${currentUser.uid}")
                        sessionRestoreError = "Dokumen profil tidak ditemukan pada server."
                    }
                } catch (e: Exception) {
                    Log.e("ScolarsiaAuth", "Gagal membaca profil dari Firestore: ${e.message}")
                    sessionRestoreError = "Gagal memuat profil pengguna dari server. Periksa koneksi internet."
                } finally {
                    isRestoringSession = false
                }
            }
        }
    }

    // Inisialisasi Emulator & Restore Session
    LaunchedEffect(Unit) {
        FirebaseEmulatorHelper.setupEmulators(context)
        coroutineScope.launch {
            SeedDataHelper.seedDatabase()
        }
        restoreSession()
    }

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

        // Banner Error Pemulihan Sesi (dengan Tombol Coba Lagi)
        if (sessionRestoreError != null && isLoggedIn) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sessionRestoreError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { restoreSession() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Coba Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (showSplash) {
                SplashScreen(
                    onTimeout = {
                        showSplash = false
                    }
                )
            } else if (!isLoggedIn) {
                if (selectedRoleForLogin == null) {
                    RoleSelectionScreen(
                        onRoleSelected = { role ->
                            selectedRoleForLogin = role
                        },
                        onAdminClick = {
                            selectedRoleForLogin = UserRole.ADMIN
                        }
                    )
                } else {
                    LoginScreen(
                        initialRole = selectedRoleForLogin ?: UserRole.SISWA,
                        onBackToRoleSelection = {
                            selectedRoleForLogin = null
                        },
                        onLoginSuccessUser = { user ->
                            val finalName = user.name.ifBlank { "Pengguna" }
                            val role = user.userRole

                            sessionManager.saveSession(finalName, role)

                            userDisplayName = finalName
                            userRole = role
                            isLoggedIn = true
                            showLoginSuccessDialog = true

                            NotificationHelper.showSystemNotification(
                                context = context,
                                title = "Login Berhasil 🔔",
                                message = "Selamat datang kembali, $finalName! Akun ${role.label} Anda terhubung."
                            )
                        }
                    )
                }
            } else {
                MainScreen(
                    userName = userDisplayName,
                    userRole = userRole,
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = {
                        coroutineScope.launch {
                            val uid = Firebase.auth.currentUser?.uid
                            uid?.let { DeviceSessionManager.markCurrentSessionInactive(context, it) }
                            Firebase.auth.signOut()
                            AuthRepository.clearCache()
                            sessionManager.clearSession()
                            isLoggedIn = false
                            selectedRoleForLogin = null
                            userDisplayName = "Pengguna"
                            userRole = UserRole.SISWA
                            Log.i("ScolarsiaAuth", "Sesi berhasil dibersihkan (Logout).")
                        }
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
