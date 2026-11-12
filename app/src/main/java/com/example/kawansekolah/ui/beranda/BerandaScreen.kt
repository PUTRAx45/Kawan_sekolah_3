package com.example.kawansekolah.ui.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
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
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AuthRepository
import com.example.kawansekolah.ui.components.*
import com.example.kawansekolah.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
                    // GURU BERANDA (Zero State & Responsive)
                    item {
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
                                        text = "PROGRES PENILAIAN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorWhite.copy(alpha = 0.85f)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = ColorWhite.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "0%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorWhite,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "0 / 0 siswa sudah dinilai",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorWhite
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                LinearProgressIndicator(
                                    progress = { 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = ColorWhite,
                                    trackColor = ColorWhite.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }

                    item {
                        EmptyStateView(
                            title = "Belum ada data nilai",
                            description = "Belum ada data nilai untuk dianalisis. Silakan lakukan input nilai terlebih dahulu.",
                            icon = Icons.Default.Assessment
                        )
                    }

                    item {
                        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SecondaryButton(
                                text = "Unggah nilai CSV",
                                onClick = { navController.navigate("unggah_csv") },
                                icon = Icons.Default.UploadFile
                            )

                            if (isDark) {
                                SecondaryButton(
                                    text = "Input nilai",
                                    onClick = { navController.navigate("guru_input") },
                                    icon = Icons.Default.Edit
                                )
                            } else {
                                PrimaryButton(
                                    text = "Input nilai",
                                    onClick = { navController.navigate("guru_input") },
                                    icon = Icons.Default.Edit
                                )
                            }
                        }
                    }
                }

                UserRole.WALI_KELAS -> {
                    // WALI KELAS BERANDA (Clean Zero State)
                    item {
                        var teacherUser by remember { mutableStateOf<User?>(null) }
                        var teacherProfile by remember { mutableStateOf<TeacherProfile?>(null) }

                        LaunchedEffect(Unit) {
                            val uid = AuthRepository.getCurrentUserUid() ?: Firebase.auth.currentUser?.uid
                            if (uid != null) {
                                teacherUser = AuthRepository.getUserProfile(uid)
                                teacherProfile = AcademicRepository.getTeacherProfile(uid)
                            }
                        }

                        val className = remember(teacherProfile, teacherUser) {
                            val cls = teacherProfile?.classesTaught?.firstOrNull { it != "-" && it.isNotBlank() }
                                ?: teacherUser?.className.orEmpty()
                            if (cls.isNotBlank()) "KELAS WALI $cls" else "KELAS WALI"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C64F2))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = className,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Badge(containerColor = Color.White.copy(alpha = 0.2f)) {
                                        Text(
                                            text = "0 siswa",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "0,00",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Badge(containerColor = Color.White.copy(alpha = 0.2f)) {
                                        Text(
                                            text = "+0,0",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "rata-rata semua mapel",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada data nilai kelas untuk diringkas",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    item {
                        PrimaryButton(
                            text = "Lihat rekap kelas",
                            onClick = { navController.navigate("rekap_wali") }
                        )
                    }
                }

                UserRole.ORANG_TUA -> {
                    // ORANG TUA BERANDA
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
                                    Text("Siswa Scolarsia", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                Text("0,00", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    item {
                        PrimaryButton(
                            text = "Lihat insight anak",
                            onClick = { navController.navigate("ai") }
                        )
                    }
                }

                else -> {
                    // SISWA BERANDA
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
                                        Text("0,0", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("0,00", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Mata pelajaran • Semester Ganjil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                        }
                    }

                    item {
                        PrimaryButton(
                            text = "Lihat insight AI",
                            onClick = { navController.navigate("insight_page") }
                        )
                    }
                }
            }
        }
    }
}
