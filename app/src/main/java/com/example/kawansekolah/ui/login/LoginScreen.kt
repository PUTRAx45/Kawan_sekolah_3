package com.example.kawansekolah.ui.login

import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.R
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.theme.PrimaryBlue
import com.example.kawansekolah.ui.theme.TextMuted
import com.example.kawansekolah.utils.DevHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    initialRole: UserRole = UserRole.SISWA,
    onBackToRoleSelection: () -> Unit = {},
    onLoginSuccessUser: (User) -> Unit,
    onDemoLoginSuccess: (String, UserRole) -> Unit = { _, _ -> }
) {
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    BackHandler {
        onBackToRoleSelection()
    }

    var selectedRole by remember { mutableStateOf(initialRole) }
    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Dialog Konfigurasi Alamat Server (Debug Mode Only)
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var serverHostInput by remember { mutableStateOf(DevHost.getHost(context)) }
    var testResultMsg by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }

    val roleTitle = when (selectedRole) {
        UserRole.GURU -> "Guru"
        UserRole.WALI_KELAS -> "Wali kelas"
        UserRole.SISWA -> "Siswa"
        UserRole.ORANG_TUA -> "Orang tua"
        UserRole.ADMIN -> "Admin sekolah"
    }

    val fieldLabel = when (selectedRole) {
        UserRole.SISWA -> "NISN atau email"
        UserRole.ORANG_TUA -> "Email atau nomor HP"
        else -> "Email"
    }

    val emailPlaceholder = when (selectedRole) {
        UserRole.GURU -> "guru@sekolah.sch.id"
        UserRole.WALI_KELAS -> "walikelas@sekolah.sch.id"
        UserRole.SISWA -> "siswa@sekolah.sch.id"
        UserRole.ORANG_TUA -> "orangtua@sekolah.sch.id"
        UserRole.ADMIN -> "admin@sekolah.sch.id"
    }

    val passwordPlaceholder = "1234567"

    val defaultDemoEmail = when (selectedRole) {
        UserRole.GURU -> "guru12@scolarsia.ac.id"
        UserRole.WALI_KELAS -> "walikelas12@scolarsia.ac.id"
        UserRole.SISWA -> "siswa12@scolarsia.ac.id"
        UserRole.ORANG_TUA -> "orangtua12@scolarsia.ac.id"
        UserRole.ADMIN -> "admin12@scolarsia.ac.id"
    }

    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                val user = state.user
                val roleName = when (selectedRole) {
                    UserRole.SISWA -> "SISWA"
                    UserRole.GURU -> "GURU"
                    UserRole.WALI_KELAS -> "WALI_KELAS"
                    UserRole.ORANG_TUA -> "ORANG_TUA"
                    UserRole.ADMIN -> "ADMIN"
                }
                val finalUser = user.copy(
                    role = roleName,
                    name = if (user.name.isBlank()) "Pengguna ${selectedRole.label}" else user.name
                )
                onLoginSuccessUser(finalUser)
            }
            is AuthUiState.Error -> {
                errorMessage = state.message
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Main Scrollable Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Scolarsia Badge Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_scolarsia),
                    contentDescription = "Logo Scolarsia",
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scolarsia",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Role Badge Pill
            Surface(
                modifier = Modifier.padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = roleTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Masuk sebagai $roleTitle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Gunakan akun yang diberikan sekolah",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Field 1: Email / NISN / HP
            OutlinedTextField(
                value = identifierInput,
                onValueChange = { identifierInput = it },
                label = { Text(fieldLabel) },
                placeholder = { Text(emailPlaceholder, fontSize = 12.sp, color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 2: Password
            OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    coroutineScope.launch {
                        delay(100L)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                label = { Text("Kata sandi") },
                placeholder = { Text(passwordPlaceholder, fontSize = 12.sp, color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(200L)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            val activeError = if (errorMessage.isNotBlank()) errorMessage else if (authUiState is AuthUiState.Error) (authUiState as AuthUiState.Error).message else ""

            if (activeError.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = activeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Button "Masuk"
            Button(
                onClick = {
                    errorMessage = ""
                    val targetHost = DevHost.getHost(context)
                    if (DevHost.isPhysicalDevice() && targetHost.isBlank()) {
                        errorMessage = "Alamat server belum diatur. Klik ikon roda gigi ⚙️ di pojok kanan atas untuk memasukkan IP Wi-Fi laptop Anda."
                        return@Button
                    }
                    val inputEmail = identifierInput.ifBlank { defaultDemoEmail }
                    val inputPass = passwordInput.ifBlank { "1g2u3r4u" }
                    authViewModel.login(inputEmail, inputPass, selectedRole, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = authUiState !is AuthUiState.Loading
            ) {
                if (authUiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lupa kata sandi? Hubungi administrator sekolah",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "← Ganti peran",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onBackToRoleSelection() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Gear Icon Button at Top Right (Debug Only) - Placed AFTER Column with zIndex(10f) so it receives touch events
        if (BuildConfig.DEBUG) {
            IconButton(
                onClick = {
                    serverHostInput = DevHost.getHost(context)
                    testResultMsg = ""
                    showServerConfigDialog = true
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(10f)
                    .padding(12.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pengaturan Alamat Server",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialog Konfigurasi & Tes Alamat Server 3 Layanan
    if (showServerConfigDialog) {
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = {
                Text("Konfigurasi Alamat Server", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Masukkan IP Wi-Fi laptop (hanya angka & titik, misal: 192.168.1.4) atau 10.0.2.2 untuk Android Emulator:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = serverHostInput,
                        onValueChange = { input ->
                            serverHostInput = input.filter { c -> c.isDigit() || c == '.' }
                        },
                        label = { Text("IP Laptop / Host") },
                        placeholder = { Text("192.168.1.4") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (testResultMsg.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (testResultMsg.contains("✅")) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testResultMsg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (testResultMsg.contains("✅")) Color(0xFF166534) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isTestingConnection = true
                            testResultMsg = "⏳ Memeriksa 3 layanan (Backend 3000, Auth 9099, Firestore 8081)..."
                            coroutineScope.launch {
                                val targetHost = serverHostInput.trim().ifBlank { "10.0.2.2" }

                                val resMsg = withContext(Dispatchers.IO) {
                                    fun testSocket(host: String, port: Int, timeoutMs: Int = 3000): String {
                                        val start = System.currentTimeMillis()
                                        return try {
                                            val s = java.net.Socket()
                                            s.connect(java.net.InetSocketAddress(host, port), timeoutMs)
                                            s.close()
                                            val duration = System.currentTimeMillis() - start
                                            "OK (${duration}ms)"
                                        } catch (e: java.net.SocketTimeoutException) {
                                            "Timeout (${timeoutMs / 1000}s)"
                                        } catch (e: java.net.ConnectException) {
                                            "Ditolak (Port $port)"
                                        } catch (e: java.net.UnknownHostException) {
                                            "Host tidak ditemukan ($host)"
                                        } catch (e: Exception) {
                                            "Gagal (${e.message})"
                                        }
                                    }

                                    fun testBackendHttp(host: String): String {
                                        val timeoutMs = 8000
                                        for (attempt in 1..2) {
                                            val start = System.currentTimeMillis()
                                            try {
                                                val url = java.net.URL("http://$host:3000/health")
                                                val conn = url.openConnection() as java.net.HttpURLConnection
                                                conn.connectTimeout = timeoutMs
                                                conn.readTimeout = timeoutMs
                                                conn.connect()
                                                val code = conn.responseCode
                                                val duration = System.currentTimeMillis() - start
                                                return if (code == 200) "OK (${duration}ms)" else "HTTP $code (${duration}ms)"
                                            } catch (e: java.net.SocketTimeoutException) {
                                                if (attempt == 2) return "Timeout (8s)"
                                            } catch (e: java.net.ConnectException) {
                                                if (attempt == 2) return "Ditolak (Port 3000)"
                                            } catch (e: java.net.UnknownHostException) {
                                                return "Host tidak ditemukan ($host)"
                                            } catch (e: Exception) {
                                                if (attempt == 2) return "Gagal (${e.message})"
                                            }
                                        }
                                        return "Timeout (8s)"
                                    }

                                    val backendRes = testBackendHttp(targetHost)
                                    val authRes = testSocket(targetHost, 9099, 3000)
                                    val firestoreRes = testSocket(targetHost, 8081, 3000)

                                    val isAllOk = backendRes.startsWith("OK") && authRes.startsWith("OK") && firestoreRes.startsWith("OK")

                                    if (isAllOk) {
                                        "✅ SEMUA LAYANAN TERKONEKSI (OK):\n• Backend (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                    } else if (!backendRes.startsWith("OK") && !authRes.startsWith("OK") && !firestoreRes.startsWith("OK")) {
                                        "⚠️ SEMUA LAYANAN GAGAL:\nIP laptop kemungkinan berubah (saat ini: $targetHost).\nJalankan 'tampil-ip.bat' di laptop lalu samakan alamat IP di sini.\n\n• Backend (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                    } else {
                                        "❌ STATUS KONEKSI LAYANAN:\n• Backend API (3000): $backendRes\n• Auth (9099): $authRes\n• Firestore (8081): $firestoreRes"
                                    }
                                }

                                testResultMsg = resMsg
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isTestingConnection) "Memeriksa..." else "Tes Koneksi (3 Layanan)", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showDiagnosticsDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Diagnostik Lengkap (8 Cek HP)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanHost = serverHostInput.trim()
                        val oldHost = DevHost.getHost(context)
                        DevHost.saveDynamicHost(context, cleanHost)
                        errorMessage = ""

                        if (oldHost.isNotBlank() && oldHost != cleanHost) {
                            Toast.makeText(context, "✅ Alamat server disimpan ($cleanHost). Aplikasi akan restart...", Toast.LENGTH_LONG).show()
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            context.startActivity(intent)
                            Process.killProcess(Process.myPid())
                        } else {
                            FirebaseEmulatorHelper.setupEmulators(context, true, cleanHost)
                            showServerConfigDialog = false
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Simpan & Terapkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDiagnosticsDialog) {
        ConnectionDiagnosticsDialog(onDismiss = { showDiagnosticsDialog = false })
    }
}
