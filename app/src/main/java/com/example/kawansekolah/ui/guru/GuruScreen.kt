package com.example.kawansekolah.ui.guru

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.data.model.GradeItem
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.data.model.TopicEntity
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.ui.components.TopBarArea
import com.example.kawansekolah.ui.login.AuthViewModel
import com.example.kawansekolah.ui.nilai.AcademicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuruScreen(
    userName: String,
    grades: List<GradeItem>,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onUpdateGrade: (String, Double, Int, String, String, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val academicViewModel: AcademicViewModel = viewModel()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Account Creation Form States
    var createAccountType by remember { mutableStateOf("STUDENT") }
    var studentName by remember { mutableStateOf("") }
    var studentNis by remember { mutableStateOf("") }
    var studentPass by remember { mutableStateOf("123456") }
    var isStudentPassVisible by remember { mutableStateOf(false) }
    var studentClass by remember { mutableStateOf("9-A") }

    var parentName by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var parentPass by remember { mutableStateOf("123456") }
    var isParentPassVisible by remember { mutableStateOf(false) }

    // Grade Upload States
    var scoreAritmatika by remember { mutableStateOf("90") }
    var scoreGeometri by remember { mutableStateOf("90") }
    var scorePitagoras by remember { mutableStateOf("70") }

    // Subject/Topic States
    var newSubjectName by remember { mutableStateOf("") }
    var newTopicName by remember { mutableStateOf("") }
    var newTopicWeight by remember { mutableStateOf("1.5") }

    var statusMessage by remember { mutableStateOf("") }

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

        // Teacher Action Tabs
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; statusMessage = "" }, text = { Text("Upload Nilai", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; statusMessage = "" }, text = { Text("Mapel & Bobot", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2; statusMessage = "" }, text = { Text("Buat Akun", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (selectedTab == 0) {
                // TAB 0: UPLOAD NILAI
                item {
                    Text("Input Nilai Sub-Materi Berbobot", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Pilih siswa dan masukkan nilai 0-100 per sub-materi. AI akan menghitung nilai akhir berbobot secara otomatis.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Siswa: Ahmad Budi (Kelas 9-A, NIS: 12345)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Mapel: Matematika (SMP Kelas 9)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = scoreAritmatika,
                                onValueChange = { scoreAritmatika = it },
                                label = { Text("Aritmatika (Bobot 1.0)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = scoreGeometri,
                                onValueChange = { scoreGeometri = it },
                                label = { Text("Geometri (Bobot 2.0)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = scorePitagoras,
                                onValueChange = { scorePitagoras = it },
                                label = { Text("Pitagoras (Bobot 1.5)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val sA = scoreAritmatika.toDoubleOrNull() ?: 90.0
                                    val sG = scoreGeometri.toDoubleOrNull() ?: 90.0
                                    val sP = scorePitagoras.toDoubleOrNull() ?: 70.0

                                    val dummyStudent = User(
                                        uid = "3dvXsUYZhjFbXasyrMXSDGwNq2zN",
                                        name = "Ahmad Budi",
                                        email = "12345@kawansekolah.local",
                                        role = "STUDENT",
                                        schoolLevel = "SMP",
                                        grade = 9
                                    )
                                    val dummySubject = SubjectEntity(
                                        id = "subj_matematika_9",
                                        teacherId = "guru_smp_01",
                                        name = "Matematika",
                                        schoolLevel = "SMP",
                                        grade = 9
                                    )
                                    val topicList = listOf(
                                        Pair(TopicEntity("top_aritmatika", "guru_smp_01", "subj_matematika_9", "Aritmatika", 1.0), sA),
                                        Pair(TopicEntity("top_geometri", "guru_smp_01", "subj_matematika_9", "Geometri", 2.0), sG),
                                        Pair(TopicEntity("top_pitagoras", "guru_smp_01", "subj_matematika_9", "Pitagoras", 1.5), sP)
                                    )

                                    academicViewModel.uploadGradesAndGenerateInsight(dummyStudent, dummySubject, topicList)
                                    statusMessage = "✅ Nilai berbobot berhasil disimpan ke Firestore & dikirim ke AI Analytics!"
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Simpan Batch & Analisis AI", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // TAB 1: KELOLA MAPEL & SUB-MATERI
                item {
                    Text("Kelola Mata Pelajaran & Sub-Materi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tambah Mata Pelajaran Baru", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newSubjectName, onValueChange = { newSubjectName = it }, label = { Text("Nama Mapel (misal: Fisika)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newSubjectName.isNotBlank()) {
                                        academicViewModel.addSubject(newSubjectName.trim(), "SMP", 9, "guru_smp_01")
                                        statusMessage = "✅ Mapel '$newSubjectName' berhasil ditambahkan!"
                                        newSubjectName = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Mapel")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tambah Sub-Materi Berbobot (Mapel Matematika)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = newTopicName, onValueChange = { newTopicName = it }, label = { Text("Nama Sub-Materi (misal: Trigonometri)") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newTopicWeight,
                                onValueChange = { newTopicWeight = it },
                                label = { Text("Bobot Sub-Materi (misal: 1.5)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newTopicName.isNotBlank()) {
                                        val weight = newTopicWeight.toDoubleOrNull() ?: 1.0
                                        academicViewModel.addTopic("subj_matematika_9", newTopicName.trim(), weight)
                                        statusMessage = "✅ Sub-materi '$newTopicName' (Bobot $weight) berhasil disimpan!"
                                        newTopicName = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah Sub-Materi Berbobot")
                            }
                        }
                    }
                }
            } else {
                // TAB 2: BUAT AKUN SISWA / ORANG TUA
                item {
                    Text("Pendaftaran Akun Siswa & Orang Tua", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Dibuat menggunakan Secondary FirebaseApp agar sesi login Guru tidak terputus.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = createAccountType == "STUDENT",
                            onClick = { createAccountType = "STUDENT" },
                            label = { Text("Akun Siswa") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = createAccountType == "PARENT",
                            onClick = { createAccountType = "PARENT" },
                            label = { Text("Akun Orang Tua") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (createAccountType == "STUDENT") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Nama Lengkap Siswa") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = studentNis, onValueChange = { studentNis = it }, label = { Text("Nomor NIS Siswa (misal: 12345)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = studentPass,
                                    onValueChange = { studentPass = it },
                                    label = { Text("Kata Sandi") },
                                    trailingIcon = {
                                        val icon = if (isStudentPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                        val desc = if (isStudentPassVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi"
                                        IconButton(onClick = { isStudentPassVisible = !isStudentPassVisible }) {
                                            Icon(imageVector = icon, contentDescription = desc)
                                        }
                                    },
                                    visualTransformation = if (isStudentPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = studentClass, onValueChange = { studentClass = it }, label = { Text("Kelas (misal: 9-A)") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (studentName.isNotBlank() && studentNis.isNotBlank()) {
                                            authViewModel.createStudentAccount(
                                                context = context,
                                                name = studentName,
                                                nis = studentNis,
                                                pass = studentPass,
                                                schoolLevel = "SMP",
                                                grade = 9,
                                                className = studentClass,
                                                onSuccess = {
                                                    statusMessage = "✅ Akun Siswa '${it.name}' berhasil dibuat dengan NIS ${studentNis}!"
                                                    studentName = ""
                                                    studentNis = ""
                                                },
                                                onError = { err -> statusMessage = "❌ $err" }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buat Akun Siswa (Secondary Auth)")
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(value = parentName, onValueChange = { parentName = it }, label = { Text("Nama Lengkap Orang Tua") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = parentEmail, onValueChange = { parentEmail = it }, label = { Text("Email Orang Tua") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = parentPass,
                                    onValueChange = { parentPass = it },
                                    label = { Text("Kata Sandi") },
                                    trailingIcon = {
                                        val icon = if (isParentPassVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                        val desc = if (isParentPassVisible) "Sembunyikan Kata Sandi" else "Tampilkan Kata Sandi"
                                        IconButton(onClick = { isParentPassVisible = !isParentPassVisible }) {
                                            Icon(imageVector = icon, contentDescription = desc)
                                        }
                                    },
                                    visualTransformation = if (isParentPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (parentName.isNotBlank() && parentEmail.isNotBlank()) {
                                            authViewModel.createParentAccount(
                                                context = context,
                                                name = parentName,
                                                email = parentEmail,
                                                pass = parentPass,
                                                linkedStudentIds = listOf("siswa_12345", "3dvXsUYZhjFbXasyrMXSDGwNq2zN"),
                                                onSuccess = {
                                                    statusMessage = "✅ Akun Orang Tua '${it.name}' berhasil dibuat & ditautkan ke Siswa!"
                                                    parentName = ""
                                                    parentEmail = ""
                                                },
                                                onError = { err -> statusMessage = "❌ $err" }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buat Akun Orang Tua (Secondary Auth)")
                                }
                            }
                        }
                    }
                }
            }

            if (statusMessage.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
