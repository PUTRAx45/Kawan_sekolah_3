package com.example.kawansekolah.ui.feature_insight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.AiInsight
import com.example.kawansekolah.data.repository.InsightRepository
import com.example.kawansekolah.data.repository.ScoreRepository
import com.example.kawansekolah.domain.AccessPolicy
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(
    userRole: String,
    userId: String
) {
    val coroutineScope = rememberCoroutineScope()

    var aiInsight by remember { mutableStateOf<AiInsight?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isWali = AccessPolicy.isWaliKelas(userRole)

    fun fetchInsightData() {
        if (isWali) {
            isLoading = false
            errorMessage = "Akses Ditolak: Halaman AI Auto-Insight tidak tersedia untuk Wali Kelas. Silakan manfaatkan menu Rekap Kelas."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            val dummyClass = ScoreRepository.getDummyKelasList().first()
            val penugasan = ScoreRepository.getPenugasanForTeacher(userId)
            val assignedTeacherId = penugasan?.teacherId ?: userId
            val subjectId = penugasan?.subjectId ?: "subj_matematika_9"

            val scores = ScoreRepository.getScoresForClass(userRole, userId, dummyClass.id, dummyClass.homeroomTeacherId)

            val res = InsightRepository.generateClassInsight(
                userRole = userRole,
                userId = userId,
                classHomeroomTeacherId = dummyClass.homeroomTeacherId,
                assignedTeacherId = assignedTeacherId,
                studentId = userId,
                classId = dummyClass.id,
                subjectId = subjectId,
                subjectName = "Matematika",
                scores = scores
            )

            if (res.isSuccess) {
                aiInsight = res.getOrNull()
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Gagal memuat rekomendasi AI."
            }
            isLoading = false
        }
    }

    LaunchedEffect(userId, userRole) {
        fetchInsightData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("AI Auto-Insight Pembelajaran", fontWeight = FontWeight.Bold) }
        )

        if (isWali) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
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
                            text = "Akses Ditolak (Khusus Guru Mapel)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Fitur AI Auto-Insight ini dikhususkan untuk Guru Mata Pelajaran. Sebagai Wali Kelas, Anda dapat memantau rekapitulasi nilai melalui menu Rekap Kelas.",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rekomendasi Otomatis AI Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Menghubungi AI Analytics...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ Kendala Layanan AI Analytics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Terjadi kesalahan.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { fetchInsightData() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Coba Lagi")
                                }
                            }
                        }
                    }
                } else if (aiInsight != null) {
                    val ins = aiInsight!!

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Data Dasar Pembelajaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Rata-Rata", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format(Locale.getDefault(), "%.1f", ins.averageScore), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Terendah", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format(Locale.getDefault(), "%.1f", ins.lowestScore), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tertinggi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format(Locale.getDefault(), "%.1f", ins.highestScore), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Hasil Analisis & Rekomendasi AI:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ins.recommendation,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
