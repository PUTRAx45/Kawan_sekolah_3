package com.example.kawansekolah.ui.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kawansekolah.data.model.Subject
import com.example.kawansekolah.data.model.Task
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.ui.components.TopBarArea

@Composable
fun BerandaScreen(
    navController: NavHostController,
    userName: String,
    userRole: UserRole = UserRole.SISWA,
    subjects: List<Subject>,
    tasks: List<Task>,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onToggleTask: (Int) -> Unit = {},
    onSelectSubject: (Subject) -> Unit = {}
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

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (userRole) {
                UserRole.GURU -> {
                    // GURU BERANDA (Image 5)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("KELAS IX A • MATEMATIKA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("80%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("24 / 30 siswa sudah dinilai", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("6 nilai belum lengkap", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                                    Text("Tugas, UTS, atau UAS belum masuk", fontSize = 11.sp, color = Color(0xFFB45309))
                                }
                                Badge(containerColor = Color(0xFFF59E0B)) {
                                    Text("PERLU DICEK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("TOPIK LEMAH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Matematika • Teorema Pythagoras", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rata-rata 68 • buka AI Analytics untuk saran", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Column {
                            Text("Aktivitas terbaru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Text("Nilai Andi Pratama diperbarui • 10.42", fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { navController.navigate("unggah_csv") },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unggah nilai CSV", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { navController.navigate("guru_input") },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Kelola nilai", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                UserRole.WALI_KELAS -> {
                    // WALI KELAS BERANDA (Image 6)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("KELAS WALI IX A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("30 siswa", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("82,41", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("rata-rata semua mapel", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("6 siswa nilai belum lengkap", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                                    Text("Dari 4 mata pelajaran di kelasmu", fontSize = 11.sp, color = Color(0xFFB45309))
                                }
                                Badge(containerColor = Color(0xFFF59E0B)) {
                                    Text("PERLU DICEK", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("TOPIK LEMAH KELAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("IPA • Sistem ekskresi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Rata-rata kelas 74 • lihat Insight Kelas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { navController.navigate("rekap_wali") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lihat rekap kelas", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                UserRole.ORANG_TUA -> {
                    // ORANG TUA BERANDA (Image 8)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Anak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Andi Pratama • Kelas IX A", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null)
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("RATA-RATA SEMESTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("Stabil", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("82,87", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("DUKUNGAN", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1D4ED8))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Matematika • Pythagoras • nilai 68", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E40AF))
                                Text("Ajak anak berlatih dengan contoh bertahap.", fontSize = 11.sp, color = Color(0xFF2563EB))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { navController.navigate("ai") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lihat insight anak", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                else -> {
                    // SISWA BERANDA (Image 7)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("RATA-RATA SEMESTER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("3,2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("82,87", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("4 mata pelajaran • Semester Ganjil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    item {
                        Text("Mata pelajaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Matematika", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(containerColor = Color(0xFFDCFCE7)) { Text("KUAT  86,40", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("IPA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(containerColor = Color(0xFFFEF3C7)) { Text("LATIHAN  74,00", color = Color(0xFFB45309), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bahasa Inggris", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(containerColor = Color(0xFFDCFCE7)) { Text("KUAT  88,20", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Bahasa Indonesia", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) { Text("BELUM LENGKAP", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("FOKUS BELAJAR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Matematika • Pythagoras perlu latihan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFB45309))
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { navController.navigate("insight_page") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lihat insight AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
