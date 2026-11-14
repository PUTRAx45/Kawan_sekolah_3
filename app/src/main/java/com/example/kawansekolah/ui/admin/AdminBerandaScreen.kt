package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.ui.components.EmptyStateView
import com.example.kawansekolah.ui.components.LinkRowItem
import com.example.kawansekolah.ui.components.TopBarArea
import com.example.kawansekolah.ui.theme.*

@Composable
fun AdminBerandaScreen(
    userName: String = "Admin Sekolah",
    schoolName: String = "Scolarsia",
    unreadCount: Int = 0,
    totalUsers: Int = 0,
    totalGuru: Int = 0,
    totalSiswa: Int = 0,
    totalOrtu: Int = 0,
    totalKelas: Int = 0,
    activities: List<String> = emptyList(),
    onNotificationClick: () -> Unit = {},
    onNavigateToPengguna: () -> Unit = {},
    onNavigateToAkademik: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(
            userName = userName,
            unreadCount = unreadCount,
            onNotificationClick = onNotificationClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Blue Hero Summary Card (#1C64F2)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATISTIK PENGGUNA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorWhite.copy(alpha = 0.85f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = ColorWhite.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$totalGuru guru",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$totalUsers Total",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorWhite
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GURU", fontSize = 10.sp, color = ColorWhite.copy(alpha = 0.7f))
                            Text("$totalGuru", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorWhite)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SISWA", fontSize = 10.sp, color = ColorWhite.copy(alpha = 0.7f))
                            Text("$totalSiswa", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorWhite)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ORTU", fontSize = 10.sp, color = ColorWhite.copy(alpha = 0.7f))
                            Text("$totalOrtu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorWhite)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KELAS", fontSize = 10.sp, color = ColorWhite.copy(alpha = 0.7f))
                            Text("$totalKelas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorWhite)
                        }
                    }
                }
            }

            // Quick Access Section ("Akses Cepat")
            Text(
                text = "Akses cepat",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinkRowItem(
                title = "Kelola pengguna",
                subtitle = "Daftarkan dan atur akun sekolah",
                icon = Icons.Default.Group,
                onClick = onNavigateToPengguna
            )

            LinkRowItem(
                title = "Data akademik",
                subtitle = "Atur kelas, mapel, dan bobot",
                icon = Icons.Default.School,
                onClick = onNavigateToAkademik
            )

            // Recent Activity / System Log Section
            Text(
                text = "Aktivitas terbaru",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (activities.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorderStroke)
                ) {
                    EmptyStateView(
                        title = "Belum ada aktivitas",
                        description = "Belum ada aktivitas atau data pengguna terdaftar.",
                        icon = Icons.Default.Inbox,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorderStroke)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        activities.forEachIndexed { idx, act ->
                            Text(
                                text = act,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (idx < activities.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        }
                    }
                }
            }
        }
    }
}
