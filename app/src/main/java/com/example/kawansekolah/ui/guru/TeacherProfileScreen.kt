package com.example.kawansekolah.ui.guru

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kawansekolah.ui.theme.CardBorderStroke
import com.example.kawansekolah.ui.theme.PrimaryBlue
import com.example.kawansekolah.ui.theme.PrimarySoftBlue
import com.example.kawansekolah.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    onProfileSavedSuccess: () -> Unit
) {
    val viewModel: TeacherProfileViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("SMP") }

    // 4 Slots State
    var slot0 by remember { mutableStateOf("9-A") }
    var slot1 by remember { mutableStateOf("9-B") }
    var slot2 by remember { mutableStateOf("-") }
    var slot3 by remember { mutableStateOf("-") }

    // Expanded states per slot
    var expandedSlot by remember { mutableStateOf<Int?>(null) }

    // Custom Class Input Dialog
    var showCustomClassDialog by remember { mutableStateOf(false) }
    var customClassInput by remember { mutableStateOf("") }
    var activeSlotForCustomClass by remember { mutableIntStateOf(0) }

    // Dynamic Class Options List per Level
    val sdClasses = remember { mutableStateListOf("1-A", "1-B", "1-C", "1-D", "2-A", "2-B", "2-C", "2-D", "3-A", "3-B", "3-C", "3-D", "4-A", "4-B", "4-C", "4-D", "5-A", "5-B", "5-C", "5-D", "6-A", "6-B", "6-C", "6-D") }
    val smpClasses = remember { mutableStateListOf("7-A", "7-B", "7-C", "7-D", "8-A", "8-B", "8-C", "8-D", "9-A", "9-B", "9-C", "9-D") }
    val smaClasses = remember { mutableStateListOf("10-A", "10-B", "10-C", "10-D", "11-A", "11-B", "11-C", "11-D", "12-A", "12-B", "12-C", "12-D") }

    val currentOptions = when (selectedLevel) {
        "SD" -> sdClasses
        "SMA" -> smaClasses
        else -> smpClasses
    }

    // Reset slot options when level changes
    LaunchedEffect(selectedLevel) {
        if (selectedLevel == "SD") {
            slot0 = "5-A"
            slot1 = "5-B"
            slot2 = "-"
            slot3 = "-"
        } else if (selectedLevel == "SMA") {
            slot0 = "11-A"
            slot1 = "11-B"
            slot2 = "-"
            slot3 = "-"
        } else {
            slot0 = "9-A"
            slot1 = "9-B"
            slot2 = "-"
            slot3 = "-"
        }
    }

    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.SaveSuccess) {
            onProfileSavedSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrimarySoftBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lengkapi Profil Pengajar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Sebagai Guru, mohon lengkapi profil mengajar Anda terlebih dahulu sebelum dapat mengakses dashboard.",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap & Gelar") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = schoolName,
                onValueChange = { schoolName = it },
                label = { Text("Nama Sekolah") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    .padding(vertical = 8.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Label Kelas yang Diampu (Maksimal 4 Slot)
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

            Spacer(modifier = Modifier.height(8.dp))

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

            if (errorMsg.isNotBlank() || uiState is ProfileUiState.Error) {
                val errText = if (errorMsg.isNotBlank()) errorMsg else (uiState as ProfileUiState.Error).message
                Text(
                    text = errText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val classesList = listOf(slot0, slot1, slot2, slot3).filter { it != "-" && it.isNotBlank() }
                    if (fullName.isBlank() || schoolName.isBlank() || classesList.isEmpty()) {
                        errorMsg = "Mohon isi semua bidang dan pilih minimal 1 kelas yang diampu!"
                    } else {
                        errorMsg = ""
                        viewModel.saveProfile(fullName, schoolName, selectedLevel, classesList)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is ProfileUiState.Saving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState is ProfileUiState.Saving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Simpan & Lanjutkan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
