package com.example.kawansekolah.ui.feature_wali

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaliProfilScreen(
    onLogoutClick: () -> Unit = {},
    onUbahKataSandiClick: () -> Unit = {},
    onPrivasiNotifikasiClick: () -> Unit = {},
    onPerangkatSesiClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    var userSession by remember { mutableStateOf<User?>(null) }
    var teacherProfile by remember { mutableStateOf<TeacherProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = AuthRepository.getCurrentUserUid() ?: Firebase.auth.currentUser?.uid
        if (uid != null) {
            userSession = AuthRepository.getUserProfile(uid)
            teacherProfile = AcademicRepository.getTeacherProfile(uid)
        }
        isLoading = false
    }

    // Identitas dinamis dari sesi login
    val fullName = remember(teacherProfile, userSession) {
        val profName = teacherProfile?.fullName.orEmpty()
        val userName = userSession?.name.orEmpty()
        val authName = Firebase.auth.currentUser?.displayName.orEmpty()

        when {
            profName.isNotBlank() -> profName
            userName.isNotBlank() -> userName
            authName.isNotBlank() -> authName
            else -> "Wali Kelas"
        }
    }

    val email = remember(userSession) {
        val candidate = userSession?.email?.ifBlank { Firebase.auth.currentUser?.email.orEmpty() }
            ?: Firebase.auth.currentUser?.email.orEmpty()
        if (candidate.isNotBlank()) candidate else "walikelas@scolarsia.sch.id"
    }

    val roleLabel = "Wali Kelas"

    // Informasi Mengajar / Perwalian
    val schoolLevel = remember(teacherProfile, userSession) {
        val lvl = teacherProfile?.schoolLevel?.ifBlank { userSession?.schoolLevel }
            ?: userSession?.schoolLevel.orEmpty()
        if (lvl.isNotBlank()) lvl else "-"
    }

    val homeroomClass = remember(teacherProfile, userSession) {
        val cls = teacherProfile?.classesTaught?.firstOrNull { it != "-" && it.isNotBlank() }
            ?: userSession?.className.orEmpty()
        if (cls.isNotBlank()) "Kelas $cls" else "-"
    }

    val subjectsTaught = remember(teacherProfile) {
        val list = teacherProfile?.classesTaught?.filter { it != "-" && it.isNotBlank() } ?: emptyList()
        if (list.isNotEmpty()) list.joinToString(", ") else "-"
    }

    val initials = remember(fullName) {
        if (fullName.isNotBlank()) {
            val parts = fullName.trim().split(" ")
            if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase() else parts[0].take(2).uppercase()
        } else "WK"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopAppBar(
            title = { Text("Profil Saya", fontWeight = FontWeight.Bold) }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kartu Navy Profil
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A), // Dark Navy
                    contentColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E3A8A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = roleLabel,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = email,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Informasi Mengajar / Perwalian
            Text(
                text = "Informasi mengajar / perwalian",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Jenjang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(schoolLevel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kelas Wali", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(homeroomClass, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mata Pelajaran yang Diampu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(subjectsTaught, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Keamanan & Privasi Akun
            Text(
                text = "Keamanan & privasi akun",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    WaliProfileMenuRow(
                        icon = Icons.Default.Lock,
                        title = "Ubah kata sandi",
                        subtitle = "Jaga keamanan akun pribadi",
                        onClick = onUbahKataSandiClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    WaliProfileMenuRow(
                        icon = Icons.Default.Security,
                        title = "Privasi & notifikasi",
                        subtitle = "Atur izin dan notifikasi",
                        onClick = onPrivasiNotifikasiClick
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )

                    WaliProfileMenuRow(
                        icon = Icons.Default.Phonelink,
                        title = "Perangkat & sesi",
                        subtitle = "Kelola perangkat yang masuk",
                        badgeText = "1 aktif",
                        onClick = onPerangkatSesiClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Keluar
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari akun", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    // Dialog Konfirmasi Keluar
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Keluar dari akun?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Kamu perlu masuk lagi untuk melihat rekapitulasi kelas bimbingan.",
                    fontSize = 13.sp,
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun WaliProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (badgeText != null) {
            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
