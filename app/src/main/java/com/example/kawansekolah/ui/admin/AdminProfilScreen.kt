package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.ui.components.DangerButton
import com.example.kawansekolah.ui.components.DarkProfileHeaderCard
import com.example.kawansekolah.ui.components.SecondaryButton
import com.example.kawansekolah.ui.components.SettingRowItem
import com.example.kawansekolah.ui.theme.*
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfilScreen(
    adminName: String = "Arif Setiawan",
    adminEmail: String = "admin12@scolarsia.ac.id",
    onLogoutClick: () -> Unit = {},
    onUbahKataSandiClick: () -> Unit = {},
    onPrivasiNotifikasiClick: () -> Unit = {},
    onPerangkatSesiClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var seedStatusMsg by remember { mutableStateOf("") }
    var isSeeding by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val activeAuthEmail = remember { Firebase.auth.currentUser?.email ?: adminEmail }
    val activeName = if (adminName.isNotBlank()) adminName else "Arif Setiawan"
    val initials = if (activeName.isNotBlank()) activeName.split(" ").map { it.take(1) }.take(2).joinToString("") else "AS"

    val callSeedEndpoint: (String) -> Unit = { endpoint ->
        isSeeding = true
        seedStatusMsg = ""
        coroutineScope.launch {
            try {
                val resMsg = withContext(Dispatchers.IO) {
                    val baseUrl = DevHost.getBackendBaseUrl()
                    val url = java.net.URL(baseUrl + endpoint.removePrefix("/"))
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.connect()
                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        if (endpoint.contains("clear")) "✅ Data dummy berhasil dibersihkan dari emulator!" else "✅ Data dummy berhasil diisi ke Auth & Firestore!"
                    } else {
                        "❌ Gagal memproses data dummy (HTTP $responseCode)"
                    }
                }
                seedStatusMsg = resMsg
            } catch (e: Exception) {
                seedStatusMsg = "❌ Kendala server backend lokal: ${e.message}"
            } finally {
                isSeeding = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Profil saya",
                    style = MaterialTheme.typography.titleLarge
                )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Navy Profile Header Card
            DarkProfileHeaderCard(
                title = activeName,
                subtitle = "Administrator Sekolah • Scolarsia",
                email = activeAuthEmail,
                initials = initials
            )

            // Administrasi Sekolah Section
            Text(
                text = "Administrasi sekolah",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kelola pengguna & peran", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("Akun dan relasi keluarga", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kelola data akademik", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("Periode, kelas, mapel, penugasan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Keamanan & Privasi Section
            Text(
                text = "Keamanan & privasi akun",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderStroke)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingRowItem(
                        icon = Icons.Default.Lock,
                        title = "Ubah kata sandi",
                        subtitle = "Jaga keamanan akun pribadi",
                        onClick = onUbahKataSandiClick
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingRowItem(
                        icon = Icons.Default.Security,
                        title = "Privasi & notifikasi",
                        subtitle = "Atur izin dan notifikasi",
                        onClick = onPrivasiNotifikasiClick
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    SettingRowItem(
                        icon = Icons.Default.Phonelink,
                        title = "Perangkat & sesi",
                        subtitle = "Kelola perangkat yang masuk",
                        badgeText = "1 aktif",
                        onClick = onPerangkatSesiClick
                    )
                }
            }

            // Developer Seed Controls (Debug Only)
            if (BuildConfig.DEBUG) {
                Text(
                    text = "Pengembang & Data Dummy (Debug)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorderStroke)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryButton(
                            text = if (isSeeding) "Memproses..." else "Isi data dummy",
                            onClick = { callSeedEndpoint("dev/seed") },
                            enabled = !isSeeding,
                            icon = Icons.Default.Add
                        )

                        DangerButton(
                            text = "Hapus data dummy",
                            onClick = { showConfirmClearDialog = true },
                            enabled = !isSeeding,
                            icon = Icons.Default.Delete
                        )

                        if (seedStatusMsg.isNotBlank()) {
                            Text(
                                text = seedStatusMsg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "Akses pengelolaan sekolah mengikuti kewenangan akun administrator.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Danger Logout Button
            DangerButton(
                text = "Keluar dari akun",
                onClick = { showLogoutDialog = true },
                icon = Icons.AutoMirrored.Filled.ExitToApp
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = {
                Text(
                    text = "Hapus Data Dummy?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "HANYA data dummy (isDummy = true) yang akan dibersihkan dari Auth & Firestore emulator. Data akun asli yang dibuat admin tidak akan terhapus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClearDialog = false
                        callSeedEndpoint("dev/seed-clear")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = "Ya, Hapus Data Dummy",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmClearDialog = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Keluar dari akun?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Kamu perlu masuk lagi untuk mengelola akun sekolah.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Keluar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
