package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDaftarAkunScreen(
    onBackClick: () -> Unit = {},
    onImportCsvClick: () -> Unit = {},
    onNavigateToPenugasanGuru: () -> Unit = {},
    onAccountCreated: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tempPass by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Guru") }
    var expandedRole by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && email.isNotBlank() && tempPass.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = { Text("Daftarkan akun", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama lengkap") },
                placeholder = { Text("Contoh: Budi Santoso") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email / username") },
                placeholder = { Text("nama@sekolah.sch.id") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = tempPass,
                onValueChange = { tempPass = it },
                label = { Text("Kata sandi awal") },
                placeholder = { Text("Buat kata sandi sementara") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Dropdown Peran
            ExposedDropdownMenuBox(
                expanded = expandedRole,
                onExpandedChange = { expandedRole = !expandedRole }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Peran pengguna") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedRole,
                    onDismissRequest = { expandedRole = false }
                ) {
                    listOf("Guru", "Wali kelas", "Siswa", "Orang tua", "Admin").forEach { roleOption ->
                        DropdownMenuItem(
                            text = { Text(roleOption) },
                            onClick = {
                                selectedRole = roleOption
                                expandedRole = false
                            }
                        )
                    }
                }
            }

            Text("Pengguna wajib mengubah kata sandi saat pertama masuk.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (selectedRole == "Guru" || selectedRole == "Wali kelas") {
                TextButton(onClick = onNavigateToPenugasanGuru) {
                    Text("Atur Penugasan & Data Pengajar Guru ->", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(onClick = onImportCsvClick) {
                Text("Impor banyak akun lewat CSV ->", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onAccountCreated,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Daftarkan akun", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}
