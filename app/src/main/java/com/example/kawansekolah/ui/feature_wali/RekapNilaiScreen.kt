package com.example.kawansekolah.ui.feature_wali

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AuthRepository
import com.example.kawansekolah.domain.AccessPolicy
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Locale

data class StudentSummaryItem(
    val id: String,
    val name: String,
    val nis: String,
    val averageScore: Double,
    val status: String // "Lengkap", "Perlu latihan", "Perlu perhatian"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapNilaiScreen(
    userRole: String,
    userId: String,
    onNavigateToStatistik: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var userSession by remember { mutableStateOf<User?>(null) }
    var teacherProfile by remember { mutableStateOf<TeacherProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var studentList by remember { mutableStateOf<List<StudentSummaryItem>>(emptyList()) }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Lengkap, 2: Perlu latihan, 3: Perlu perhatian
    var selectedStudentForRapor by remember { mutableStateOf<StudentSummaryItem?>(null) }

    if (selectedStudentForRapor != null) {
        WaliRaporSiswaScreen(
            studentName = selectedStudentForRapor!!.name,
            studentNis = selectedStudentForRapor!!.nis,
            subjectGrades = emptyList(),
            onBackClick = { selectedStudentForRapor = null }
        )
        return
    }

    LaunchedEffect(userId) {
        isLoading = true
        val uid = if (userId.isNotBlank()) userId else AuthRepository.getCurrentUserUid() ?: Firebase.auth.currentUser?.uid
        if (uid != null) {
            userSession = AuthRepository.getUserProfile(uid)
            teacherProfile = AcademicRepository.getTeacherProfile(uid)
        }
        // Inisialisasi daftar siswa kosong (empty state awal)
        studentList = emptyList()
        isLoading = false
    }

    val className = remember(teacherProfile, userSession) {
        val cls = teacherProfile?.classesTaught?.firstOrNull { it != "-" && it.isNotBlank() }
            ?: userSession?.className.orEmpty()
        if (cls.isNotBlank()) "Kelas $cls" else "Kelas Perwalian"
    }

    val homeroomTeacherName = remember(teacherProfile, userSession) {
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

    val totalStudents = studentList.size
    val completeCount = studentList.count { it.status == "Lengkap" }
    val needsAttentionCount = studentList.count { it.status == "Perlu perhatian" || it.status == "Perlu latihan" }

    val classAverage = if (studentList.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.2f", studentList.map { it.averageScore }.average()).replace('.', ',')
    } else {
        "0,00"
    }

    val filteredStudents = remember(studentList, selectedFilter) {
        when (selectedFilter) {
            1 -> studentList.filter { it.status == "Lengkap" }
            2 -> studentList.filter { it.status == "Perlu latihan" }
            3 -> studentList.filter { it.status == "Perlu perhatian" }
            else -> studentList
        }
    }

    val isWali = AccessPolicy.isWaliKelas(userRole) || userRole.contains("WALI", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Rekapitulasi Kelas Wali", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            }
        )

        if (!isWali) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Akses Ditolak (Khusus Wali Kelas)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Halaman ini khusus untuk Wali Kelas yang berwenang memantau rekapitulasi nilai siswa di kelas bimbingannya.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner Informasi Wali Kelas
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "🔒 Mode Read-Only: Wali Kelas ($homeroomTeacherName • $className) memantau rekapitulasi nilai.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Hero Card Rata-Rata Kelas Wali
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1C64F2), // Blue
                            contentColor = Color.White
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = className.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }
                                TextButton(onClick = onNavigateToStatistik) {
                                    Text("Grafik 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = classAverage,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Rata-Rata Kelas Wali",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Score Grid / Statistik Siswa (Total Siswa, Nilai Lengkap, Perlu Perhatian)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Total Siswa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$totalStudents", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Nilai Lengkap", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$completeCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Perlu Perhatian", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$needsAttentionCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Filter Chips (Semua, Lengkap, Perlu latihan, Perlu perhatian)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Filter Performa Siswa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Semua", "Lengkap", "Perlu latihan", "Perlu perhatian").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = selectedFilter == idx,
                                    onClick = { selectedFilter = idx },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // LazyColumn / Daftar Siswa
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredStudents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada data siswa di kelas ini",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredStudents) { student ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("NIS: ${student.nis}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f", student.averageScore),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
