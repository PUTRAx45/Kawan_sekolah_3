package com.example.kawansekolah.ui.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.R
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit,
    onAdminClick: () -> Unit = {}
) {
    val context = LocalContext.current

    BackHandler {
        (context as? Activity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMuted)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
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
                    .background(PrimaryBlue),
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
                color = DarkText
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Masuk sebagai",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )

            Text(
                text = "Silakan pilih status akun Anda",
                fontSize = 12.sp,
                color = TextMuted,
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
                badgeColor = PrimarySoftBlue,
                badgeTextColor = PrimaryBlue,
                onClick = { onRoleSelected(UserRole.WALI_KELAS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleCardItem(
                role = UserRole.SISWA,
                title = "Siswa",
                subtitle = "Lihat nilai & rekomendasi belajar",
                badgeLetter = "S",
                badgeColor = SuccessGreenBg,
                badgeTextColor = SuccessGreen,
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
                color = PrimaryBlue,
                modifier = Modifier
                    .clickable { onAdminClick() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
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
        color = ColorWhite,
        border = BorderStroke(1.dp, CardBorderStroke),
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
                    color = DarkText
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
