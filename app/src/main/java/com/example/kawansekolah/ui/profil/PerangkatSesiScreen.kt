package com.example.kawansekolah.ui.profil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.SesiPerangkatModel
import com.example.kawansekolah.ui.components.EmptyStateView
import com.example.kawansekolah.ui.components.StatusBadge
import com.example.kawansekolah.ui.components.StatusBadgeType
import com.example.kawansekolah.utils.DeviceSessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerangkatSesiScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activeUserId = remember { Firebase.auth.currentUser?.uid ?: "" }

    val currentDeviceName = remember { DeviceSessionManager.getDeviceName() }
    val currentOsVersion = remember { DeviceSessionManager.getOsVersion() }

    var otherSessions by remember { mutableStateOf<List<SesiPerangkatModel>>(emptyList()) }
    var isLoggingOutOtherDevices by remember { mutableStateOf(false) }

    // Real-Time Session Tracking & Listener
    LaunchedEffect(activeUserId) {
        if (activeUserId.isNotBlank()) {
            DeviceSessionManager.saveCurrentSession(context, activeUserId)
        }
    }

    DisposableEffect(activeUserId) {
        val listener = DeviceSessionManager.listenOtherSessions(context, activeUserId) { list ->
            otherSessions = list
        }
        onDispose {
            listener?.remove()
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
            title = { Text("Perangkat & sesi", fontWeight = FontWeight.Bold) },
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
            // Section 1: Perangkat Ini
            Text("Perangkat ini", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$currentDeviceName • $currentOsVersion", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Aktif sekarang", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusBadge(type = StatusBadgeType.AKTIF)
                }
            }

            // Section 2: Perangkat Lain
            Text("Perangkat lain", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (otherSessions.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    EmptyStateView(
                        title = "Belum ada koneksi perangkat lain",
                        description = "Akun Anda saat ini hanya aktif di perangkat ini.",
                        icon = Icons.Default.Phonelink,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        otherSessions.forEachIndexed { idx, session ->
                            val timeStr = if (session.aktif) "Sedang aktif" else {
                                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
                                "Terakhir aktif ${sdf.format(Date(session.terakhirAktif))}"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = session.namaPerangkat.ifBlank { "Perangkat Android" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (session.aktif) {
                                    StatusBadge(type = StatusBadgeType.AKTIF)
                                } else {
                                    StatusBadge(type = StatusBadgeType.BELUM_LENGKAP, customText = "Tidak aktif")
                                }
                            }

                            if (idx < otherSessions.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Button Keluar dari Perangkat Lain
        OutlinedButton(
            onClick = {
                if (otherSessions.isNotEmpty() && activeUserId.isNotBlank()) {
                    isLoggingOutOtherDevices = true
                    DeviceSessionManager.logoutOtherDevices(context, activeUserId) { success ->
                        isLoggingOutOtherDevices = false
                        if (success) {
                            Toast.makeText(context, "✅ Semua perangkat lain telah berhasil dikeluarkan.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "⚠️ Gagal mengeluarkan perangkat lain. Coba beberapa saat lagi.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = otherSessions.isNotEmpty() && !isLoggingOutOtherDevices,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(50.dp)
                .border(1.dp, if (otherSessions.isNotEmpty()) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            if (isLoggingOutOtherDevices) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Keluar dari perangkat lain", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
