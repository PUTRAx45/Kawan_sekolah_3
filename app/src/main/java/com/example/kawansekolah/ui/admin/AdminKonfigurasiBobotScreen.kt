package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.ui.components.NavyPeriodeCard
import com.example.kawansekolah.ui.components.StatusBadge
import com.example.kawansekolah.ui.components.StatusBadgeType
import com.example.kawansekolah.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminKonfigurasiBobotScreen(
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
    var bobotTugasInput by remember { mutableStateOf("0") }
    var bobotUtsInput by remember { mutableStateOf("0") }
    var bobotUasInput by remember { mutableStateOf("0") }

    val tugasVal = bobotTugasInput.toIntOrNull() ?: 0
    val utsVal = bobotUtsInput.toIntOrNull() ?: 0
    val uasVal = bobotUasInput.toIntOrNull() ?: 0

    val totalBobot = tugasVal + utsVal + uasVal
    val isValid = totalBobot == 100

    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = { Text("Konfigurasi Bobot", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Navy Card Periode Penilaian
            NavyPeriodeCard(
                periodeName = "Semester Ganjil 2026/2027",
                badgeText = "AKTIF"
            )

            // Form Inputs for Bobot
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Atur Persentase Bobot Nilai (%)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = bobotTugasInput,
                        onValueChange = { if (it.length <= 3) bobotTugasInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Tugas (%)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = bobotUtsInput,
                        onValueChange = { if (it.length <= 3) bobotUtsInput = it.filter { c -> c.isDigit() } },
                        label = { Text("UTS (%)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = bobotUasInput,
                        onValueChange = { if (it.length <= 3) bobotUasInput = it.filter { c -> c.isDigit() } },
                        label = { Text("UAS (%)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Total Bobot Card Indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isValid) SuccessGreenBg else WarningYellowBg,
                border = BorderStroke(1.dp, if (isValid) SuccessGreen.copy(alpha = 0.4f) else WarningYellowStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BOBOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isValid) SuccessGreen else WarningYellow
                        )

                        StatusBadge(
                            type = if (isValid) StatusBadgeType.LENGKAP else StatusBadgeType.PERLU_DICEK,
                            customText = if (isValid) "100% Valid ✓" else "Belum valid ($totalBobot%)"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$totalBobot%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isValid) SuccessGreen else WarningYellow
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Nilai akhir = Σ(nilai × bobot). Total bobot harus berjumlah tepat 100%.",
                        fontSize = 11.sp,
                        color = if (isValid) SuccessGreen.copy(alpha = 0.8f) else WarningYellow
                    )
                }
            }

            if (statusMessage.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        color = Color(0xFF166534),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button (Disabled until totalBobot == 100)
            Button(
                onClick = {
                    if (isValid) {
                        isSaving = true
                        statusMessage = "✅ Konfigurasi bobot ($tugasVal% Tugas, $utsVal% UTS, $uasVal% UAS) berhasil disimpan!"
                        isSaving = false
                        onSaveSuccess()
                    }
                },
                enabled = isValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = DisabledGray
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Simpan konfigurasi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}
