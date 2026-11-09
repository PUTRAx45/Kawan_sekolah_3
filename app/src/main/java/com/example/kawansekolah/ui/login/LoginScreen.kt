package com.example.kawansekolah.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.R
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var selectedRole by remember { mutableStateOf(initialRole) }
    var identifierInput by remember {
        mutableStateOf(
            when (initialRole) {
                UserRole.SISWA -> "12345"
                UserRole.GURU -> "guru@kawansekolah.sch.id"
                UserRole.WALI_KELAS -> "guru.wali@kawansekolah.sch.id"
                UserRole.ORANG_TUA -> "ortu.ahmad@kawansekolah.local"
            }
        )
    }
    var passwordInput by remember { mutableStateOf("123456") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val roleTitle = when (selectedRole) {
        UserRole.GURU -> "Guru"
        UserRole.WALI_KELAS -> "Wali kelas"
        UserRole.SISWA -> "Siswa"
        UserRole.ORANG_TUA -> "Orang tua"
    }

    val fieldLabel = when (selectedRole) {
        UserRole.SISWA -> "NISN atau email"
        UserRole.ORANG_TUA -> "Email atau nomor HP"
        else -> "Email"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Blue Scolarsia Badge Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.logo_scolarsia),
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
                color = Color(0xFFEFF6FF)
            ) {
                Text(
                    text = roleTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
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
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
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
                Text(
                    text = activeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Blue Button "Masuk"
            Button(
                onClick = {
                    errorMessage = ""
                    authViewModel.login(identifierInput, passwordInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
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
                color = Color(0xFF2563EB),
                modifier = Modifier
                    .clickable { onBackToRoleSelection() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    val demoName = when (selectedRole) {
                        UserRole.SISWA -> "Ahmad Budi"
                        UserRole.GURU -> "Budi Santoso"
                        UserRole.WALI_KELAS -> "Siti Aminah"
                        UserRole.ORANG_TUA -> "Bapak Ahmad"
                    }
                    onDemoLoginSuccess(demoName, selectedRole)
                }
            ) {
                Text(
                    text = "Gunakan Demo ${selectedRole.label} Tanpa Login ->",
                    fontSize = 12.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
