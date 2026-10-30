package com.example.kawansekolah.ui.feature_nilai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.repository.ScoreRepository
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.ScoreValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputNilaiScreen(
    userRole: String,
    userId: String,
    onNavigateToCsvUpload: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val kelasList = remember { ScoreRepository.getDummyKelasList() }
    val subjectsList = remember { ScoreRepository.getDummySubjectsList() }
    val periodeList = remember { ScoreRepository.getDummyPeriodeList() }

    var selectedClass by remember { mutableStateOf(kelasList.first()) }
    var selectedSubject by remember { mutableStateOf(subjectsList.first()) }
    var selectedPeriode by remember { mutableStateOf(periodeList.first()) }

    val kompetensiList = remember(selectedSubject.id) { ScoreRepository.getDummyKompetensiList(selectedSubject.id) }
    var selectedKompetensi by remember { mutableStateOf(kompetensiList.first()) }

    var selectedScoreType by remember { mutableStateOf("KUIS_HARIAN") }

    // Dropdown Expanded States
    var expandedClass by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }
    var expandedPeriode by remember { mutableStateOf(false) }
    var expandedKompetensi by remember { mutableStateOf(false) }

    val students = remember(selectedClass.id) { ScoreRepository.getDummyStudentsList(selectedClass.id) }

    val scoreInputs = remember(selectedClass.id) {
        mutableStateMapOf<String, String>().apply {
            students.forEach { put(it.id, "85") }
        }
    }

    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val assignedTeacherId = selectedSubject.teacherId
    val canInput = AccessPolicy.canInputScore(userRole, userId, assignedTeacherId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Input Nilai Siswa", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            },
            actions = {
                if (AccessPolicy.canUploadCsv(userRole, userId, assignedTeacherId)) {
                    IconButton(onClick = onNavigateToCsvUpload) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Unggah CSV")
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Parameter Penilaian", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        // Dropdown Kelas
                        ExposedDropdownMenuBox(
                            expanded = expandedClass,
                            onExpandedChange = { expandedClass = !expandedClass }
                        ) {
                            OutlinedTextField(
                                value = selectedClass.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kelas") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedClass,
                                onDismissRequest = { expandedClass = false }
                            ) {
                                kelasList.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            selectedClass = item
                                            expandedClass = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Mapel
                        ExposedDropdownMenuBox(
                            expanded = expandedSubject,
                            onExpandedChange = { expandedSubject = !expandedSubject }
                        ) {
                            OutlinedTextField(
                                value = selectedSubject.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mata Pelajaran") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSubject,
                                onDismissRequest = { expandedSubject = false }
                            ) {
                                subjectsList.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            selectedSubject = item
                                            expandedSubject = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Periode
                        ExposedDropdownMenuBox(
                            expanded = expandedPeriode,
                            onExpandedChange = { expandedPeriode = !expandedPeriode }
                        ) {
                            OutlinedTextField(
                                value = selectedPeriode.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Periode / Semester") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriode) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPeriode,
                                onDismissRequest = { expandedPeriode = false }
                            ) {
                                periodeList.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = {
                                            selectedPeriode = item
                                            expandedPeriode = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dropdown Kompetensi
                        ExposedDropdownMenuBox(
                            expanded = expandedKompetensi,
                            onExpandedChange = { expandedKompetensi = !expandedKompetensi }
                        ) {
                            OutlinedTextField(
                                value = "${selectedKompetensi.code} - ${selectedKompetensi.name}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kompetensi Dasar") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKompetensi) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedKompetensi,
                                onDismissRequest = { expandedKompetensi = false }
                            ) {
                                kompetensiList.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text("${item.code} - ${item.name}") },
                                        onClick = {
                                            selectedKompetensi = item
                                            expandedKompetensi = false
                                        }
                                    )
                                }
                            }
                        }

                        // Jenis Penilaian
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedScoreType == "KUIS_HARIAN",
                                onClick = { selectedScoreType = "KUIS_HARIAN" },
                                label = { Text("Kuis Harian") }
                            )
                            FilterChip(
                                selected = selectedScoreType == "KARAKTER",
                                onClick = { selectedScoreType = "KARAKTER" },
                                label = { Text("Penilaian Karakter") }
                            )
                        }
                    }
                }
            }

            if (!canInput) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Akses Ditolak: Anda tidak memiliki wewenang menginput nilai untuk mata pelajaran ini.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                items(students) { student ->
                    val scoreText = scoreInputs[student.id] ?: ""
                    val (isValid, _) = ScoreValidator.parseScoreInput(scoreText)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("NIS: ${student.nis}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            OutlinedTextField(
                                value = scoreText,
                                onValueChange = { scoreInputs[student.id] = it },
                                label = { Text("Nilai (0-100)") },
                                isError = !isValid && scoreText.isNotBlank(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(130.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            statusMessage = ""
                            isError = false
                            val scoresToSave = mutableListOf<Score>()
                            var hasInvalid = false

                            students.forEach { std ->
                                val txt = scoreInputs[std.id] ?: "0"
                                val (valid, valNum) = ScoreValidator.parseScoreInput(txt)
                                if (!valid || valNum == null) {
                                    hasInvalid = true
                                } else {
                                    scoresToSave.add(
                                        Score(
                                            studentId = std.id,
                                            studentName = std.name,
                                            teacherId = userId,
                                            subjectId = selectedSubject.id,
                                            classId = selectedClass.id,
                                            competenceId = selectedKompetensi.id,
                                            periodId = selectedPeriode.id,
                                            scoreType = selectedScoreType,
                                            scoreValue = valNum
                                        )
                                    )
                                }
                            }

                            if (hasInvalid) {
                                isError = true
                                statusMessage = "❌ Terdapat nilai yang tidak valid (harus angka 0.0 - 100.0)."
                            } else {
                                coroutineScope.launch {
                                    val res = ScoreRepository.saveScoresBatch(userRole, userId, assignedTeacherId, scoresToSave)
                                    if (res.isSuccess) {
                                        statusMessage = "✅ Berhasil menyimpan ${res.getOrNull()} data nilai siswa!"
                                    } else {
                                        isError = true
                                        statusMessage = "❌ ${res.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Batch Nilai", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (statusMessage.isNotBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage,
                            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
