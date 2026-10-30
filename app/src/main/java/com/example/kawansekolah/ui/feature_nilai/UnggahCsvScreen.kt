package com.example.kawansekolah.ui.feature_nilai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.repository.ScoreRepository
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.ScoreValidator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnggahCsvScreen(
    userRole: String,
    userId: String,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val kelasList = remember { ScoreRepository.getDummyKelasList() }
    val subjectsList = remember { ScoreRepository.getDummySubjectsList() }

    var selectedClass by remember { mutableStateOf(kelasList.first()) }
    var selectedSubject by remember { mutableStateOf(subjectsList.first()) }

    val assignedTeacherId = selectedSubject.teacherId
    val canUpload = AccessPolicy.canUploadCsv(userRole, userId, assignedTeacherId)

    var csvInputText by remember {
        mutableStateOf(
            """
            studentId, studentName, score, scoreType
            std_1, Ahmad Budi, 88.5, KUIS_HARIAN
            std_2, Siti Rahma, 95.0, KUIS_HARIAN
            std_3, Dewi Lestari, 150.0, KUIS_HARIAN
            std_4, Fajar Pratama, 80.0, KARAKTER
            """.trimIndent()
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    csvInputText = content
                }
            } catch (e: Exception) {
                // Ignore file read error
            }
        }
    }

    val parseResults = remember(csvInputText) {
        ScoreValidator.validateCsvContent(csvInputText)
    }

    val validRows = parseResults.filter { it.isValid }
    val invalidRows = parseResults.filter { !it.isValid }

    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Unggah Nilai via CSV", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pilih Berkas CSV / Tempel Teks Nilai:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Kelas Target: ${selectedClass.name} | Mapel: ${selectedSubject.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Format: studentId, studentName, score, scoreType (KUIS_HARIAN / KARAKTER)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedButton(
                            onClick = { filePickerLauncher.launch(arrayOf("text/*", "text/csv", "*/*")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pilih File .CSV dari HP")
                        }

                        OutlinedTextField(
                            value = csvInputText,
                            onValueChange = { csvInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip(
                        onClick = {
                            csvInputText = """
                            std_1, Ahmad Budi, 90.0, KUIS_HARIAN
                            std_2, Siti Rahma, 85.0, KARAKTER
                            """.trimIndent()
                        },
                        label = { Text("Contoh CSV Valid") },
                        modifier = Modifier.weight(1f)
                    )

                    AssistChip(
                        onClick = {
                            csvInputText = """
                            std_1, Ahmad Budi, 120.0, KUIS_HARIAN
                            std_2, Siti Rahma, -10.0, SALAH
                            """.trimIndent()
                        },
                        label = { Text("Contoh CSV Error") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hasil Validasi Baris:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Valid: ${validRows.size} | Salah: ${invalidRows.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            items(parseResults) { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isValid) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (res.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (res.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Baris ${res.lineNumber}: ${res.studentName} (${res.studentId})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Nilai: ${res.score} | Jenis: ${res.scoreType}", fontSize = 11.sp)
                            if (!res.isValid) {
                                Text(" Error: ${res.errorMessage}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        statusMessage = ""
                        isError = false

                        if (!canUpload) {
                            isError = true
                            statusMessage = "❌ Akses ditolak: Hanya Guru Mapel bersangkutan yang dapat mengunggah file nilai."
                            return@Button
                        }

                        if (validRows.isEmpty()) {
                            isError = true
                            statusMessage = "❌ Tidak ada baris CSV yang valid untuk diunggah."
                            return@Button
                        }

                        val scoresToSave = validRows.map { row ->
                            Score(
                                studentId = row.studentId,
                                studentName = row.studentName,
                                teacherId = userId,
                                subjectId = selectedSubject.id,
                                classId = selectedClass.id,
                                competenceId = "komp_1",
                                periodId = "per_1",
                                scoreType = row.scoreType,
                                scoreValue = row.score
                            )
                        }

                        coroutineScope.launch {
                            val res = ScoreRepository.saveScoresBatch(userRole, userId, assignedTeacherId, scoresToSave)
                            if (res.isSuccess) {
                                statusMessage = "✅ Berhasil mengunggah ${res.getOrNull()} baris nilai valid!"
                            } else {
                                isError = true
                                statusMessage = "❌ ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    enabled = validRows.isNotEmpty() && canUpload
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proses & Unggah Baris Valid (${validRows.size})", fontWeight = FontWeight.Bold)
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
