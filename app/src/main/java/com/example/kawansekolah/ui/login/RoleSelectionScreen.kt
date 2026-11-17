package com.example.kawansekolah.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Process
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.R
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.theme.PrimaryBlue
import com.example.kawansekolah.utils.DevHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    onAdminClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        (context as? Activity)?.finish()
    }

    var showServerConfigDialog by remember { mutableStateOf(false) }
    var serverHostInput by remember { mutableStateOf(DevHost.getHost(context)) }
    var testResultMsg by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Scolarsia Blue Badge Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_scolarsia),
                    contentDescription = "Logo Scolarsia",
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scolarsia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Masuk sebagai",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Silakan pilih status akun Anda",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Role Card Items (Guru, Wali kelas, Siswa, Orang tua)
            RoleCardItem(
                role = UserRole.GURU,
                title = "Guru",
                subtitle = "Kelola penilaian mata pelajaran",
                badgeLetter = "G",
                badgeColor = Color(0xFFE0F2FE),
                badgeTextColor = Color(0xFF0284C7),
                onClick = { onRoleSelected(UserRole.GURU) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleCardItem(
                role = UserRole.WALI_KELAS,
                title = "Wali kelas",
                subtitle = "Rekapitulasi nilai kelas wali",
                badgeLetter = "W",
                badgeColor = Color(0xFFDBEAFE),
                badgeTextColor = Color(0xFF1D4ED8),
                onClick = { onRoleSelected(UserRole.WALI_KELAS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleCardItem(
                role = UserRole.SISWA,
                title = "Siswa",
                subtitle = "Lihat nilai & rekomendasi belajar",
                badgeLetter = "S",
                badgeColor = Color(0xFFDEF7EC),
                badgeTextColor = Color(0xFF0E9F6E),
                onClick = { onRoleSelected(UserRole.SISWA) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleCardItem(
                role = UserRole.ORANG_TUA,
                title = "Orang tua",
                subtitle = "Pantau hasil belajar anak",
                badgeLetter = "O",
                badgeColor = Color(0xFFFAF5FF),
                badgeTextColor = Color(0xFF9333EA),
                onClick = { onRoleSelected(UserRole.ORANG_TUA) }
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Admin sekolah? Daftarkan sekolah",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onAdminClick() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanHost = serverHostInput.trim()
                        val oldHost = DevHost.getHost(context)
                        DevHost.saveDynamicHost(context, cleanHost)

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
}

@Composable
private fun RoleCardItem(
    role: UserRole,
    title: String,
    subtitle: String,
    badgeLetter: String,
    badgeColor: Color,
    badgeTextColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeLetter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
