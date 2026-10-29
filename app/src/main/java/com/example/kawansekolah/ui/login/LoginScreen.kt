package com.example.kawansekolah.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.R
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.utils.ConnectionDiagnostics
import com.example.kawansekolah.utils.ServiceCheckResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccessUser: (User) -> Unit,
    onDemoLoginSuccess: (String, UserRole) -> Unit = { _, _ -> }
) {
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isRegisterTab by remember { mutableStateOf(false) }

    // Form State
    var identifierInput by remember { mutableStateOf("12345") }
    var passwordInput by remember { mutableStateOf("123456") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.SISWA) }

    // Register State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var regInviteCode by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    // Rate limiting: 5 failed attempts -> 30s cooldown
    var failedCount by remember { mutableIntStateOf(0) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    // Diagnostics Dialog (Long-Press Gesture on Scolarsia Title)
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var isCheckingDiagnostics by remember { mutableStateOf(false) }
    var diagnosticsResults by remember { mutableStateOf<List<ServiceCheckResult>?>(null) }

    // Real-Time Polling Loop for Diagnostics when Dialog is Visible
    LaunchedEffect(showDiagnosticsDialog) {
        if (showDiagnosticsDialog) {
            isCheckingDiagnostics = true
            while (showDiagnosticsDialog) {
                diagnosticsResults = ConnectionDiagnostics.checkAllServices()
                isCheckingDiagnostics = false
                delay(2000L) // Real-Time Refresh Latency every 2 seconds
            }
        }
    }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                failedCount = 0
                val user = state.user

                if (selectedRole == UserRole.SISWA && user.userRole != UserRole.SISWA) {
                    errorMessage = "Akun ${user.userRole.label} tidak bisa masuk melalui mode Siswa. Silakan pilih tab Guru/Orang Tua."
                    authViewModel.resetState()
                } else if (selectedRole != UserRole.SISWA && user.userRole == UserRole.SISWA) {
                    errorMessage = "Akun Siswa tidak bisa masuk melalui mode Guru/Orang Tua. Silakan pilih tab Siswa."
                    authViewModel.resetState()
                } else {
                    onLoginSuccessUser(user)
                }
            }
            is AuthUiState.Error -> {
                failedCount++
                if (failedCount >= 5) {
                    cooldownSeconds = 30
                    failedCount = 0
                }
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.logo_scolarsia),
                    contentDescription = "Logo Scolarsia",
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Title (Long-Press on "Scolarsia" activates real-time diagnostics dialog in Debug)
            Text(
                text = "Scolarsia",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (BuildConfig.DEBUG) {
                            showDiagnosticsDialog = true
                        }
                    }
                )
            )

            Text(
                text = "AI Analytics for Better Learning",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Switcher
            TabRow(
                selectedTabIndex = if (isRegisterTab) 1 else 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = !isRegisterTab,
                    onClick = {
                        isRegisterTab = false
                        errorMessage = ""
                        authViewModel.resetState()
                    },
                    text = { Text("Masuk", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = isRegisterTab,
                    onClick = {
                        isRegisterTab = true
                        errorMessage = ""
                        authViewModel.resetState()
                    },
                    text = { Text("Daftar Guru", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!BuildConfig.USE_EMULATOR && com.example.kawansekolah.utils.DevHost.isPlaceholderBaseUrl()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️ Perhatian: BASE_URL backend online belum diisi di app/build.gradle.kts (masih placeholder). Fitur AI memerlukan URL backend, tetapi Login & Firebase Cloud tetap dapat diuji.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            if (!isRegisterTab) {
                // LOGIN TAB
                Text(
                    text = "Pilih Peran Akun:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf(
                        Triple(UserRole.SISWA, "Siswa (NISN)", Icons.Default.School),
                        Triple(UserRole.GURU, "Guru", Icons.Default.Psychology),
                        Triple(UserRole.ORANG_TUA, "Orang Tua", Icons.Default.People)
                    )

                    roles.forEach { (role, label, icon) ->
                        val isSelected = selectedRole == role
                        Surface(
                            onClick = {
                                selectedRole = role
                                identifierInput = when (role) {
                                    UserRole.SISWA -> "12345"
                                    UserRole.GURU -> "guru@kawansekolah.sch.id"
                                    UserRole.ORANG_TUA -> "ortu.ahmad@kawansekolah.local"
                                }
                                passwordInput = "123456"
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val fieldLabel = when (selectedRole) {
                    UserRole.SISWA -> "Nomor NISN Siswa"
                    UserRole.GURU -> "Email Guru"
                    UserRole.ORANG_TUA -> "Email Orang Tua"
                }

                OutlinedTextField(
                    value = identifierInput,
                    onValueChange = { identifierInput = it },
                    label = { Text(fieldLabel) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authUiState !is AuthUiState.Loading && cooldownSeconds == 0,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Kata Sandi") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (isPasswordVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi"
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = description)
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authUiState !is AuthUiState.Loading && cooldownSeconds == 0,
                    shape = RoundedCornerShape(12.dp)
                )

                val activeError = if (errorMessage.isNotBlank()) errorMessage else if (authUiState is AuthUiState.Error) (authUiState as AuthUiState.Error).message else ""

                if (activeError.isNotEmpty()) {
                    Text(
                        text = activeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = ""
                        authViewModel.login(identifierInput, passwordInput)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = authUiState !is AuthUiState.Loading && cooldownSeconds == 0
                ) {
                    if (authUiState is AuthUiState.Loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Memproses Login...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (cooldownSeconds > 0) {
                        Text("Tunggu $cooldownSeconds Detik...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { onDemoLoginSuccess("Ahmad Budi", UserRole.SISWA) }) {
                    Text("Gunakan Akun Demo Tanpa Login ->", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }

            } else {
                // REGISTER GURU TAB
                Text(
                    text = "Pendaftaran Khusus Guru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = "Siswa dan Orang Tua didaftarkan langsung oleh Guru melalui dashboard.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("Nama Lengkap Guru") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("Email Resmi Guru") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = { Text("Kata Sandi") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val icon = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (isRegPasswordVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi"
                        IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                            Icon(imageVector = icon, contentDescription = description)
                        }
                    },
                    visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = regInviteCode,
                    onValueChange = { regInviteCode = it },
                    label = { Text("Kode Undangan Guru (Kode: GURU2025)") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (authUiState is AuthUiState.Error) {
                    val err = (authUiState as AuthUiState.Error).message
                    Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { authViewModel.registerTeacher(regName, regEmail, regPassword, regInviteCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = authUiState !is AuthUiState.Loading
                ) {
                    if (authUiState is AuthUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Daftar Akun Guru", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Real-time Diagnostics Dialog (Debug Mode Gesture)
        if (showDiagnosticsDialog) {
            AlertDialog(
                onDismissRequest = { showDiagnosticsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Cek Koneksi Real-Time Scolarsia", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("LIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (diagnosticsResults == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Memeriksa status port & server...", fontSize = 12.sp)
                            }
                        } else {
                            diagnosticsResults?.forEach { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(res.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(res.endpoint + " (" + res.details + ")", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (res.isOk) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                            Text("BERHASIL (${res.latencyMs}ms)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    } else {
                                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                            Text("GAGAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ConnectionDiagnostics.getDiagnosticHint(),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showDiagnosticsDialog = false }) {
                        Text("Tutup")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isCheckingDiagnostics = true
                        coroutineScope.launch {
                            diagnosticsResults = ConnectionDiagnostics.checkAllServices()
                            isCheckingDiagnostics = false
                        }
                    }) {
                        Text("Refresh Sekarang")
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
