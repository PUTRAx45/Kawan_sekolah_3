package com.example.kawansekolah.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.ui.theme.CardBorderStroke
import com.example.kawansekolah.ui.theme.PrimaryBlue
import com.example.kawansekolah.ui.theme.PrimarySoftBlue
import com.example.kawansekolah.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPenugasanGuruScreen(
    initialTeacherEmail: String = "",
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Registered teacher account email options
    val teacherEmailOptions = remember {
        mutableStateListOf(
            "guru12@scolarsia.ac.id",
            "walikelas12@scolarsia.ac.id"
        )
    }

    var selectedEmail by remember { mutableStateOf(initialTeacherEmail) }
    var expandedEmailDropdown by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("SMP") }

    // 4 Class Slots State Initialized Empty (-)
    var slot0 by remember { mutableStateOf("-") }
    var slot1 by remember { mutableStateOf("-") }
    var slot2 by remember { mutableStateOf("-") }
    var slot3 by remember { mutableStateOf("-") }

    // Expanded state per slot
    var expandedSlot by remember { mutableStateOf<Int?>(null) }

    // Custom Class Dialog
    var showCustomClassDialog by remember { mutableStateOf(false) }
    var customClassInput by remember { mutableStateOf("") }
    var activeSlotForCustomClass by remember { mutableIntStateOf(0) }

    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Dynamic Class Lists per Level
    val sdClasses = remember { mutableStateListOf("1-A", "1-B", "1-C", "1-D", "2-A", "2-B", "2-C", "2-D", "3-A", "3-B", "3-C", "3-D", "4-A", "4-B", "4-C", "4-D", "5-A", "5-B", "5-C", "5-D", "6-A", "6-B", "6-C", "6-D") }
    val smpClasses = remember { mutableStateListOf("7-A", "7-B", "7-C", "7-D", "8-A", "8-B", "8-C", "8-D", "9-A", "9-B", "9-C", "9-D") }
    val smaClasses = remember { mutableStateListOf("10-A", "10-B", "10-C", "10-D", "11-A", "11-B", "11-C", "11-D", "12-A", "12-B", "12-C", "12-D") }

    val currentOptions = when (selectedLevel) {
        "SD" -> sdClasses
        "SMA" -> smaClasses
        else -> smpClasses
    }

    // Auto-load profile data when selected teacher email changes
    LaunchedEffect(selectedEmail) {
        if (selectedEmail.isNotBlank()) {
            statusMessage = ""
            val targetUid = if (selectedEmail.contains("wali")) "wali_01" else "guru_01"
            val existingProfile = AcademicRepository.getTeacherProfile(targetUid)
            if (existingProfile != null) {
                fullName = existingProfile.fullName
                schoolName = existingProfile.schoolName
                selectedLevel = existingProfile.schoolLevel.ifBlank { selectedLevel }
                val classes = existingProfile.classesTaught.filter { it != "-" && it.isNotBlank() }
                slot0 = classes.getOrNull(0) ?: "-"
                slot1 = classes.getOrNull(1) ?: "-"
                slot2 = classes.getOrNull(2) ?: "-"
                slot3 = classes.getOrNull(3) ?: "-"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = { Text("Penugasan & Data Pengajar", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimarySoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Form Penugasan & Data Pengajar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Kelola akun guru, nama sekolah, jenjang, dan penugasan kelas yang diampu secara langsung melalui panel Admin.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Pilih Akun / Email Guru
            ExposedDropdownMenuBox(
                expanded = expandedEmailDropdown,
                onExpandedChange = { expandedEmailDropdown = !expandedEmailDropdown }
            ) {
                OutlinedTextField(
                    value = selectedEmail,
                    onValueChange = { selectedEmail = it },
                    label = { Text("Pilih Akun / Email Guru Terdaftar") },
                    placeholder = { Text("Pilih atau ketik email guru") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmailDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedEmailDropdown,
                    onDismissRequest = { expandedEmailDropdown = false }
                ) {
                    teacherEmailOptions.forEach { optEmail ->
                        DropdownMenuItem(
                            text = { Text(optEmail) },
                            onClick = {
                                selectedEmail = optEmail
                                expandedEmailDropdown = false
                            }
                        )
                    }
                }
            }

            // 2. Nama Lengkap Guru
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap & Gelar Guru") },
                placeholder = { Text("Contoh: Rani Puspitasari, S.Pd.") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 3. Nama Sekolah
            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("Nama Sekolah") },
                placeholder = { Text("Contoh: SMP Scolarsia 1") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 4. Pilih Jenjang Sekolah
            Text(
                text = "Pilih Jenjang Sekolah:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Start)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("SD", "SMP", "SMA").forEach { level ->
                    FilterChip(
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level },
                        label = { Text(level) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. Kelas yang Diampu (4 Slot)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kelas yang Diampu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "(Maksimal 4 slot)",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // 4 Slot Horizontal Dropdown Boxes
            val slots = listOf(slot0, slot1, slot2, slot3)
            val updateSlot: (Int, String) -> Unit = { idx, value ->
                when (idx) {
                    0 -> slot0 = value
                    1 -> slot1 = value
                    2 -> slot2 = value
                    3 -> slot3 = value
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slots.forEachIndexed { idx, value ->
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { expandedSlot = idx },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (value != "-") PrimaryBlue else CardBorderStroke)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Class,
                                        contentDescription = null,
                                        tint = if (value != "-") PrimaryBlue else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Slot ${idx + 1}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = value,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (value != "-") PrimaryBlue else TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Dropdown Menu per Slot
                        DropdownMenu(
                            expanded = expandedSlot == idx,
                            onDismissRequest = { expandedSlot = null },
                            modifier = Modifier
                                .width(140.dp)
                                .heightIn(max = 280.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("- (Kosong)", fontSize = 12.sp, color = TextMuted) },
                                onClick = {
                                    updateSlot(idx, "-")
                                    expandedSlot = null
                                }
                            )

                            currentOptions.forEach { classOpt ->
                                DropdownMenuItem(
                                    text = { Text(classOpt, fontSize = 13.sp, fontWeight = if (value == classOpt) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        updateSlot(idx, classOpt)
                                        expandedSlot = null
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Tambah kelas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    }
                                },
                                onClick = {
                                    activeSlotForCustomClass = idx
                                    expandedSlot = null
                                    customClassInput = ""
                                    showCustomClassDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (statusMessage.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isError) MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF166534),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            val isFormValid = selectedEmail.isNotBlank() && fullName.isNotBlank() && schoolName.isNotBlank() && listOf(slot0, slot1, slot2, slot3).any { it != "-" && it.isNotBlank() }

            Button(
                onClick = {
                    val classesList = listOf(slot0, slot1, slot2, slot3).filter { it != "-" && it.isNotBlank() }
                    if (!isFormValid) {
                        statusMessage = "⚠️ Mohon lengkapi email, nama, sekolah, dan pilih minimal 1 kelas!"
                        isError = true
                    } else {
                        isSaving = true
                        statusMessage = ""
                        isError = false

                        coroutineScope.launch {
                            try {
                                val targetUid = if (selectedEmail.contains("wali")) "wali_01" else "guru_01"
                                val profile = TeacherProfile(
                                    teacherId = targetUid,
                                    fullName = fullName,
                                    schoolName = schoolName,
                                    schoolLevel = selectedLevel,
                                    classesTaught = classesList,
                                    profileComplete = true
                                )

                                AcademicRepository.saveTeacherProfile(profile)
                                isSaving = false
                                isError = false
                                statusMessage = "✅ Penugasan & Data Pengajar '$fullName' berhasil disimpan ke Firestore!"
                            } catch (e: Exception) {
                                isSaving = false
                                isError = true
                                statusMessage = "❌ Gagal menyimpan data: ${e.message}"
                            }
                        }
                    }
                },
                enabled = isFormValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Simpan Penugasan & Data Pengajar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Custom Class Input Dialog
    if (showCustomClassDialog) {
        AlertDialog(
            onDismissRequest = { showCustomClassDialog = false },
            title = { Text("Tambah Kelas Kustom", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Ketik nama kelas khusus (misal: 9-E atau 12-IPA-1):", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customClassInput,
                        onValueChange = { customClassInput = it },
                        label = { Text("Nama Kelas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = customClassInput.trim()
                        if (cleanName.isNotBlank()) {
                            if (!currentOptions.contains(cleanName)) {
                                currentOptions.add(cleanName)
                            }
                            when (activeSlotForCustomClass) {
                                0 -> slot0 = cleanName
                                1 -> slot1 = cleanName
                                2 -> slot2 = cleanName
                                3 -> slot3 = cleanName
                            }
                        }
                        showCustomClassDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tambah & Pilih", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomClassDialog = false }) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
