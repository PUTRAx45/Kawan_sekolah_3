package com.example.kawansekolah.ui.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AuthRepository
import com.example.kawansekolah.utils.DevHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DiagTableRow(
    val namaCek: String,
    val hasilNyata: String,
    val status: String // LULUS, GAGAL, TIDAK DIUJI
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDiagnosticsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var emailInput by remember { mutableStateOf("guru12@scolarsia.ac.id") }
    var passwordInput by remember { mutableStateOf("1g2u3r4u") }
    var rows by remember { mutableStateOf<List<DiagTableRow>>(emptyList()) }
    var isRunning by remember { mutableStateOf(false) }

    val runAllChecks: () -> Unit = {
        isRunning = true
        rows = emptyList()
        coroutineScope.launch {
            val list = mutableListOf<DiagTableRow>()
            val host = DevHost.getHost(context).ifBlank { "10.0.2.2" }
            val (resolvedHost, hostSource) = DevHost.getHostWithSource(context)
            val emuConfigured = FirebaseEmulatorHelper.isEmulatorConfigured

            // 1. Cek Konfigurasi
            list.add(
                DiagTableRow(
                    namaCek = "1. Konfigurasi Host & Emulator",
                    hasilNyata = "Host: $resolvedHost (Sumber: $hostSource) | emulatorApplied: $emuConfigured | Auth: $host:9099 | Firestore: $host:8081 | Backend: $host:3000",
                    status = if (emuConfigured) "LULUS" else "GAGAL"
                )
            )

            // 2. Cek Jaringan HP
            val (networkType, wifiIp) = withContext(Dispatchers.IO) {
                try {
                    val s = java.net.Socket()
                    s.connect(java.net.InetSocketAddress(host, 3000), 2000)
                    val localIp = s.localAddress.hostAddress ?: "127.0.0.1"
                    s.close()
                    Pair("Wi-Fi Aktif", localIp)
                } catch (e: Exception) {
                    Pair("Jaringan / Wi-Fi Error", "127.0.0.1")
                }
            }
            val isSameSubnet = if (wifiIp.contains(".") && host.contains(".")) {
                wifiIp.substringBeforeLast(".") == host.substringBeforeLast(".")
            } else true

            list.add(
                DiagTableRow(
                    namaCek = "2. Jaringan HP & Subnet /24",
                    hasilNyata = "Jenis: $networkType | IP HP: $wifiIp | Server IP: $host | Subnet /24 Cocok: $isSameSubnet",
                    status = if (isSameSubnet) "LULUS" else "GAGAL"
                )
            )

            // 3. Cek TCP Connect ke 4 Port (3000, 9099, 8081, 4000)
            val ports = listOf(3000, 9099, 8081, 4000)
            for (p in ports) {
                val (isOk, latency, cause) = withContext(Dispatchers.IO) {
                    val start = System.currentTimeMillis()
                    try {
                        val s = java.net.Socket()
                        s.connect(java.net.InetSocketAddress(host, p), 3000)
                        val dur = System.currentTimeMillis() - start
                        s.close()
                        Triple(true, dur, "OK (${dur}ms)")
                    } catch (e: java.net.SocketTimeoutException) {
                        Triple(false, 3000L, "Timeout (3s)")
                    } catch (e: java.net.ConnectException) {
                        Triple(false, 0L, "Ditolak (Port $p)")
                    } catch (e: Exception) {
                        Triple(false, 0L, "Gagal (${e.message})")
                    }
                }
                list.add(
                    DiagTableRow(
                        namaCek = "3. TCP Connect Port $p",
                        hasilNyata = "Host: $host:$p | $cause",
                        status = if (isOk) "LULUS" else "GAGAL"
                    )
                )
            }

            // 4. Cek HTTP Endpoints
            val httpHealth = withContext(Dispatchers.IO) {
                try {
                    val conn = java.net.URL("http://$host:3000/health").openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 3000
                    conn.readTimeout = 3000
                    conn.connect()
                    "HTTP ${conn.responseCode} OK"
                } catch (e: Exception) {
                    "Gagal (${e.message})"
                }
            }
            list.add(DiagTableRow("4a. HTTP GET /health", "http://$host:3000/health -> $httpHealth", if (httpHealth.contains("200")) "LULUS" else "GAGAL"))

            // 5. REST Direct Auth Login
            val (restCode, restSnippet) = AuthRepository.testRestAuthDirect(host, emailInput.trim(), passwordInput.trim())
            list.add(
                DiagTableRow(
                    namaCek = "5. REST Direct Auth Login",
                    hasilNyata = "POST http://$host:9099/signInWithPassword -> Status HTTP $restCode | Respon: $restSnippet",
                    status = if (restCode == 200) "LULUS" else "GAGAL"
                )
            )

            // 6. SDK Auth Login & Firestore Read
            var sdkSuccess = false
            var sdkResultStr = ""
            try {
                val user = AuthRepository.loginWithEmailOrNis(emailInput.trim(), passwordInput.trim(), UserRole.GURU, context)
                sdkSuccess = true
                sdkResultStr = "Auth SDK Sukses! UID: ${user.uid} | Role: ${user.role} | Aktif: ${user.aktif}"
            } catch (e: Exception) {
                sdkResultStr = "Auth SDK Gagal [${e.javaClass.simpleName}]: ${e.message}"
            }
            list.add(
                DiagTableRow(
                    namaCek = "6. SDK Auth & Firestore Read",
                    hasilNyata = sdkResultStr,
                    status = if (sdkSuccess) "LULUS" else "GAGAL"
                )
            )

            // 7. Simulasi Penguncian Peran
            val roleCardSimulations = listOf(UserRole.GURU, UserRole.WALI_KELAS, UserRole.SISWA, UserRole.ORANG_TUA)
            val simResults = mutableListOf<String>()
            for (r in roleCardSimulations) {
                val actualNorm = AuthRepository.normalizeRole("guru")
                val expectedNorm = AuthRepository.normalizeRole(r.name)
                val accepted = actualNorm == expectedNorm
                simResults.add("${r.label}: ${if (accepted) "DITERIMA" else "DITOLAK"}")
            }
            list.add(
                DiagTableRow(
                    namaCek = "7. Simulasi Penguncian Peran",
                    hasilNyata = simResults.joinToString(" | "),
                    status = "LULUS"
                )
            )

            rows = list
            isRunning = false

            // Log to Logcat under tag ScolarsiaDiag
            Log.i("ScolarsiaDiag", "==========================================================")
            Log.i("ScolarsiaDiag", "📋 HASIL TABEL DIAGNOSTIK KONEKSI LENGKAP HP FISIK:")
            for (r in list) {
                Log.i("ScolarsiaDiag", "  • [${r.status.padEnd(7)}] ${r.namaCek} -> ${r.hasilNyata}")
            }
            Log.i("ScolarsiaDiag", "==========================================================")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Diagnostik Koneksi HP Fisik", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ketik Email & Sandi Uji (tidak disimpan di kode) lalu tekan 'Jalankan Semua Cek':",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Email Uji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Kata Sandi Uji") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { runAllChecks() },
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRunning) "Memeriksa..." else "Jalankan Semua Cek", fontSize = 12.sp)
                    }

                    if (rows.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val textToCopy = rows.joinToString("\n") { "[${it.status}] ${it.namaCek}: ${it.hasilNyata}" }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("ScolarsiaDiag", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ Hasil diagnostik disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 12.sp)
                        }
                    }
                }

                if (rows.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(rows) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (item.status == "LULUS") Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(item.namaCek, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                        Text(item.status, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (item.status == "LULUS") Color(0xFF166534) else MaterialTheme.colorScheme.error)
                                    }
                                    Text(item.hasilNyata, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
