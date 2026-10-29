package com.example.kawansekolah.ui.profil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.components.TopBarArea
import com.example.kawansekolah.ui.profil.components.ProfileListItem
import com.example.kawansekolah.ui.tampilan.components.TampilanCard
import com.example.kawansekolah.ui.theme.ThemeMode

@Composable
fun ProfilScreen(
    userName: String = "Budi Santoso",
    userRole: UserRole = UserRole.SISWA,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val isDigitsOnly = remember(userName) { userName.all { it.isDigit() } }

    val displayName = remember(userName, isDigitsOnly) {
        if (isDigitsOnly && userRole == UserRole.SISWA) {
            if (userName == "0052025100" || userName == "20251004") userName
            else "Siswa NISN $userName"
        } else {
            userName
        }
    }

    val idNumber = remember(userName) {
        if (isDigitsOnly) userName
        else {
            val hash = kotlin.math.abs(userName.trim().lowercase().hashCode()) % 89999 + 10000
            "00520$hash"
        }
    }

    val roleSubtitle = when (userRole) {
        UserRole.SISWA -> "Kelas X IPA 2 • NISN: $idNumber"
        UserRole.GURU -> "Pengajar Utama • NIP: 198805$idNumber"
        UserRole.ORANG_TUA -> "Wali Murid • ID: ORTU-$idNumber"
    }

    val formattedEmail = remember(displayName, userRole) {
        val cleanName = displayName.lowercase().replace(" ", ".")
        val domain = when (userRole) {
            UserRole.SISWA -> "siswa.kawansekolah.sch.id"
            UserRole.GURU -> "guru.kawansekolah.sch.id"
            UserRole.ORANG_TUA -> "ortu.kawansekolah.sch.id"
        }
        "$cleanName@$domain"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(
            userName = displayName,
            unreadCount = unreadCount,
            onNotificationClick = onNotificationClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (userRole) {
                        UserRole.SISWA -> Icons.Default.School
                        UserRole.GURU -> Icons.Default.Psychology
                        UserRole.ORANG_TUA -> Icons.Default.People
                    },
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = roleSubtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileListItem(Icons.Default.Email, "Email Akun", formattedEmail)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileListItem(Icons.Default.Badge, "Peran / Jabatan", userRole.label)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileListItem(Icons.Default.School, "Sekolah", "SMA Negeri 1 Jakarta")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileListItem(Icons.Default.Star, "Poin Prestasi", "450 Poin")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TampilanCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                currentThemeMode = currentThemeMode,
                onThemeModeChange = onThemeModeChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
