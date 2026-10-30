package com.example.kawansekolah.ui.feature_wali

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.repository.ScoreRepository
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.StatisticsCalculator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapNilaiScreen(
    userRole: String,
    userId: String,
    onNavigateToStatistik: () -> Unit = {}
) {
    val kelasList = remember { ScoreRepository.getDummyKelasList() }
    val selectedClass = remember { kelasList.first() }

    var scoresList by remember { mutableStateOf<List<Score>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var assignedTeacherId by remember { mutableStateOf(userId) }

    LaunchedEffect(selectedClass.id, userId) {
        isLoading = true
        val penugasan = ScoreRepository.getPenugasanForTeacher(userId)
        assignedTeacherId = penugasan?.teacherId ?: userId
        scoresList = ScoreRepository.getScoresForClass(userRole, userId, selectedClass.id, selectedClass.homeroomTeacherId)
        isLoading = false
    }

    val canView = AccessPolicy.canViewClassScores(userRole, userId, selectedClass.homeroomTeacherId, assignedTeacherId)
    val scoreNumbers = scoresList.map { it.scoreValue }
    val stats = StatisticsCalculator.calculateSummary(scoreNumbers)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Rekap Nilai Kelas (Wali Kelas)", fontWeight = FontWeight.Bold) }
        )

        if (!canView) {
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
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
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
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "🔒 Mode Read-Only: Wali Kelas (${selectedClass.name}) hanya dapat memantau rekapitulasi tanpa mengubah nilai.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(selectedClass.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                TextButton(onClick = onNavigateToStatistik) {
                                    Text("Lihat Grafik 📊", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Jumlah Data", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${stats.count}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Rata-Rata", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format(Locale.getDefault(), "%.1f", stats.average), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Tertinggi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format(Locale.getDefault(), "%.1f", stats.highest), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Terendah", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(String.format(Locale.getDefault(), "%.1f", stats.lowest), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Daftar Rekap Nilai Siswa", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (scoresList.isEmpty()) {
                    item {
                        Text("Belum ada data nilai tersimpan untuk kelas ini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(scoresList) { score ->
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
                                    Text(score.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Jenis: ${score.scoreType} | ID: ${score.studentId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (score.comment.isNotBlank()) {
                                        Text("Catatan: ${score.comment}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f", score.scoreValue),
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
