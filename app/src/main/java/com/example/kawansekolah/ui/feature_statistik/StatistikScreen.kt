package com.example.kawansekolah.ui.feature_statistik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.repository.ScoreRepository
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.StatisticsCalculator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistikScreen(
    userRole: String,
    userId: String
) {
    val kelasList = remember { ScoreRepository.getDummyKelasList() }
    val selectedClass = remember { kelasList.first() }

    var scoresList by remember { mutableStateOf(emptyList<Double>()) }
    var isLoading by remember { mutableStateOf(true) }
    var assignedTeacherId by remember { mutableStateOf(userId) }

    LaunchedEffect(selectedClass.id, userId) {
        isLoading = true
        val penugasan = ScoreRepository.getPenugasanForTeacher(userId)
        assignedTeacherId = penugasan?.teacherId ?: userId
        val scores = ScoreRepository.getScoresForClass(userRole, userId, selectedClass.id, selectedClass.homeroomTeacherId)
        scoresList = scores.map { it.scoreValue }
        isLoading = false
    }

    val canView = AccessPolicy.canViewClassScores(userRole, userId, selectedClass.homeroomTeacherId, assignedTeacherId)
    val stats = StatisticsCalculator.calculateSummary(scoresList)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Statistik & Grafik Nilai", fontWeight = FontWeight.Bold) }
        )

        if (!canView) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Akses ditolak: Anda tidak memiliki wewenang melihat statistik kelas ini.", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
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
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ringkasan Statistik (${selectedClass.name})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    title = "Rata-Rata",
                                    value = String.format(Locale.getDefault(), "%.1f", stats.average),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Tertinggi",
                                    value = String.format(Locale.getDefault(), "%.1f", stats.highest),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Terendah",
                                    value = String.format(Locale.getDefault(), "%.1f", stats.lowest),
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
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
                            Text("Grafik Batang Distribusi Nilai", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (scoresList.isEmpty()) {
                                Text("Belum ada data nilai untuk ditampilkan pada grafik.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                val barPrimaryColor = MaterialTheme.colorScheme.primary
                                val barSecondaryColor = MaterialTheme.colorScheme.tertiary

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(vertical = 12.dp)
                                ) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height

                                    val maxVal = 100f
                                    val count = scoresList.size
                                    val barWidth = (canvasWidth / (count * 2f)).coerceAtMost(40f)
                                    val spacing = barWidth / 2f

                                    scoresList.forEachIndexed { index, scoreVal ->
                                        val barHeight = (scoreVal.toFloat() / maxVal) * canvasHeight
                                        val left = index * (barWidth + spacing) + spacing
                                        val top = canvasHeight - barHeight

                                        drawRect(
                                            color = if (scoreVal >= stats.average) barPrimaryColor else barSecondaryColor,
                                            topLeft = Offset(left, top),
                                            size = Size(barWidth, barHeight)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jumlah Sampel: ${stats.count} Siswa", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Biru = ≥ Rata-rata", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
