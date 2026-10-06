package com.example.kawansekolah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Define Data Classes
data class Module(
    val id: Int,
    val title: String,
    val duration: String,
    val isCompleted: Boolean
)

data class Subject(
    val name: String,
    val category: String,
    val modules: List<Module>,
    val icon: ImageVector,
    val color: Color
) {
    val modulesCount: Int get() = modules.size
    val completedModulesCount: Int get() = modules.count { it.isCompleted }
    val progress: Float get() = if (modulesCount > 0) completedModulesCount.toFloat() / modulesCount else 0f
}

data class Task(
    val id: Int,
    val title: String,
    val subject: String,
    val deadline: String,
    val isChecked: Boolean
)

// Main Root Composable
@Composable
fun MainApp() {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var userDisplayName by rememberSaveable { mutableStateOf("Budi Santoso") }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { name ->
                if (name.isNotBlank()) {
                    userDisplayName = name
                }
                isLoggedIn = true
            }
        )
    } else {
        MainScreen(
            userName = userDisplayName,
            onLogout = {
                isLoggedIn = false
            }
        )
    }
}

// Login Screen
@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo & Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KawanSekolah",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Masuk ke akun Anda untuk memulai belajar",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username / Email Field
            OutlinedTextField(
                value = usernameOrEmail,
                onValueChange = {
                    usernameOrEmail = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama Pengguna / Email / NIS") },
                placeholder = { Text("Contoh: Budi Santoso / 20251004") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kata Sandi") },
                placeholder = { Text("Masukkan kata sandi") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            Button(
                onClick = {
                    if (usernameOrEmail.isBlank()) {
                        errorMessage = "Silakan masukkan nama pengguna atau NIS"
                    } else if (password.isBlank()) {
                        errorMessage = "Silakan masukkan kata sandi"
                    } else {
                        onLoginSuccess(usernameOrEmail)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Masuk",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo Login Button
            TextButton(
                onClick = { onLoginSuccess("Budi Santoso") }
            ) {
                Text(
                    text = "Masuk sebagai Akun Demo (Budi Santoso)",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// MainScreen
@Composable
fun MainScreen(
    userName: String = "Budi Santoso",
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    // Sample State Data
    val initialSubjects = remember {
        listOf(
            Subject(
                name = "Matematika",
                category = "Eksakta",
                modules = listOf(
                    Module(1, "Modul 1: Aljabar Dasar & Persamaan", "45 Menit", true),
                    Module(2, "Modul 2: Fungsi Kuadrat & Grafik", "60 Menit", true),
                    Module(3, "Modul 3: Matriks & Penjumlahan", "50 Menit", true),
                    Module(4, "Modul 4: Trigonometri Dasar", "55 Menit", true),
                    Module(5, "Modul 5: Geometri Analitik", "40 Menit", true),
                    Module(6, "Modul 6: Limit & Kontinuitas", "60 Menit", true),
                    Module(7, "Modul 7: Turunan Fungsi", "65 Menit", false),
                    Module(8, "Modul 8: Integral Tak Tentu", "70 Menit", false),
                    Module(9, "Modul 9: Vektor R2 & R3", "50 Menit", false),
                    Module(10, "Modul 10: Peluang & Kombinatorika", "45 Menit", false),
                    Module(11, "Modul 11: Statistika Deskriptif", "55 Menit", false),
                    Module(12, "Modul 12: Barisan & Deret", "60 Menit", false)
                ),
                icon = Icons.Default.Calculate,
                color = Color(0xFFE57373)
            ),
            Subject(
                name = "Bahasa Indonesia",
                category = "Bahasa",
                modules = listOf(
                    Module(13, "Modul 1: Teks Laporan Hasil Observasi", "40 Menit", true),
                    Module(14, "Modul 2: Teks Eksposisi & Gagasan", "45 Menit", true),
                    Module(15, "Modul 3: Teks Anekdot & Humor Kritikal", "35 Menit", true),
                    Module(16, "Modul 4: Hikayat & Cerita Rakyat", "50 Menit", true),
                    Module(17, "Modul 5: Puisi & Majas", "40 Menit", true),
                    Module(18, "Modul 6: Teks Negosiasi", "45 Menit", true),
                    Module(19, "Modul 7: Biografi Tokoh", "50 Menit", false),
                    Module(20, "Modul 8: Karya Tulis Ilmiah", "60 Menit", false)
                ),
                icon = Icons.Default.Book,
                color = Color(0xFF81C784)
            ),
            Subject(
                name = "Fisika",
                category = "Eksakta",
                modules = listOf(
                    Module(21, "Modul 1: Besaran & Satuan Vektor", "50 Menit", true),
                    Module(22, "Modul 2: Kinematika Gerak Lurus", "55 Menit", true),
                    Module(23, "Modul 3: Hukum Newton tentang Gerak", "60 Menit", true),
                    Module(24, "Modul 4: Usaha & Energi", "45 Menit", false),
                    Module(25, "Modul 5: Momentum & Impuls", "50 Menit", false),
                    Module(26, "Modul 6: Gerak Harmonis Sederhana", "55 Menit", false),
                    Module(27, "Modul 7: Dinamika Rotasi", "65 Menit", false),
                    Module(28, "Modul 8: Fluida Statis & Dinamis", "60 Menit", false),
                    Module(29, "Modul 9: Suhu & Kalor", "45 Menit", false),
                    Module(30, "Modul 10: Gelombang Bunyi & Cahaya", "50 Menit", false)
                ),
                icon = Icons.Default.Science,
                color = Color(0xFF64B5F6)
            ),
            Subject(
                name = "Biologi",
                category = "Eksakta",
                modules = listOf(
                    Module(31, "Modul 1: Keanekaragaman Hayati", "40 Menit", true),
                    Module(32, "Modul 2: Virus & Bakteri", "45 Menit", true),
                    Module(33, "Modul 3: Struktur Sel & Jaringan", "50 Menit", true),
                    Module(34, "Modul 4: Sistem Pencernaan", "55 Menit", true),
                    Module(35, "Modul 5: Sistem Pernapasan", "45 Menit", true),
                    Module(36, "Modul 6: Sistem Peredaran Darah", "60 Menit", true),
                    Module(37, "Modul 7: Fotosintesis & Respirasi", "50 Menit", false),
                    Module(38, "Modul 8: Genetik & Hereditas", "65 Menit", false)
                ),
                icon = Icons.Default.Biotech,
                color = Color(0xFFFFB74D)
            ),
            Subject(
                name = "Sejarah",
                category = "Sosial",
                modules = listOf(
                    Module(39, "Modul 1: Berpikir Sejarah & Diakronik", "35 Menit", true),
                    Module(40, "Modul 2: Manusia Purba di Indonesia", "40 Menit", false),
                    Module(41, "Modul 3: Kerajaan Hindu-Buddha", "50 Menit", false),
                    Module(42, "Modul 4: Kerajaan Islam Nusantara", "55 Menit", false),
                    Module(43, "Modul 5: Kolonialisme Bangsa Eropa", "60 Menit", false),
                    Module(44, "Modul 6: Pergerakan Nasional", "50 Menit", false)
                ),
                icon = Icons.Default.History,
                color = Color(0xFFBA68C8)
            )
        )
    }

    val initialTasks = remember {
        listOf(
            Task(1, "Latihan Soal Aljabar Bab 3", "Matematika", "Besok, 23:59", false),
            Task(2, "Menulis Essay Lingkungan Hidup", "Bahasa Indonesia", "20 Okt 2025", false),
            Task(3, "Praktikum Fotosintesis", "Ilmu Pengetahuan Alam", "22 Okt 2025", true),
            Task(4, "Quiz Trigonometri & Vektor", "Matematika", "25 Okt 2025", false),
            Task(5, "Laporan Praktikum Hukum Newton", "Fisika", "28 Okt 2025", false)
        )
    }

    var subjects by remember { mutableStateOf(initialSubjects) }
    var tasks by remember { mutableStateOf(initialTasks) }
    var selectedSubjectForDetail by remember { mutableStateOf<Subject?>(null) }

    val toggleTask: (Int) -> Unit = { taskId ->
        tasks = tasks.map { if (it.id == taskId) it.copy(isChecked = !it.isChecked) else it }
    }

    val toggleModule: (Subject, Int) -> Unit = { targetSubject, moduleId ->
        subjects = subjects.map { subject ->
            if (subject.name == targetSubject.name) {
                val updatedModules = subject.modules.map { module ->
                    if (module.id == moduleId) module.copy(isCompleted = !module.isCompleted) else module
                }
                val updatedSubject = subject.copy(modules = updatedModules)
                if (selectedSubjectForDetail?.name == updatedSubject.name) {
                    selectedSubjectForDetail = updatedSubject
                }
                updatedSubject
            } else {
                subject
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "beranda",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("beranda") {
                BerandaScreen(
                    navController = navController,
                    userName = userName,
                    subjects = subjects,
                    tasks = tasks,
                    onToggleTask = toggleTask,
                    onSelectSubject = { selectedSubjectForDetail = it }
                )
            }
            composable("pelajaran") {
                PelajaranScreen(
                    userName = userName,
                    subjects = subjects,
                    onSelectSubject = { selectedSubjectForDetail = it }
                )
            }
            composable("ai") { AiScreen(userName = userName) }
            composable("tugas") {
                TugasScreen(
                    userName = userName,
                    tasks = tasks,
                    onToggleTask = toggleTask
                )
            }
            composable("profil") {
                ProfilScreen(
                    userName = userName,
                    onLogout = onLogout
                )
            }
        }
    }

    // Detail Bottom Sheet
    selectedSubjectForDetail?.let { subject ->
        SubjectDetailBottomSheet(
            subject = subject,
            onDismiss = { selectedSubjectForDetail = null },
            onToggleModule = { moduleId -> toggleModule(subject, moduleId) }
        )
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Triple("Beranda", Icons.Default.Home, "beranda"),
        Triple("Pelajaran", Icons.Default.MenuBook, "pelajaran"),
        Triple("AI", Icons.Default.AutoAwesome, "ai"),
        Triple("Tugas", Icons.Default.Assignment, "tugas"),
        Triple("Profil", Icons.Default.Person, "profil")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .fillMaxWidth()
            .clip(CircleShape)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = CircleShape
            )
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            windowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            tonalElevation = 0.dp
        ) {
            items.forEach { (title, icon, route) ->
                val isSelected = currentRoute == route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    },
                    selected = isSelected,
                    alwaysShowLabel = false,
                    onClick = {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun TopBarArea(userName: String = "Budi Santoso") {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("KawanSekolah", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Halo, $userName 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun BerandaScreen(
    navController: NavHostController,
    userName: String,
    subjects: List<Subject>,
    tasks: List<Task>,
    onToggleTask: (Int) -> Unit,
    onSelectSubject: (Subject) -> Unit
) {
    // Dynamic progress calculation across subjects and tasks
    val totalModules = subjects.sumOf { it.modulesCount }
    val completedModules = subjects.sumOf { it.completedModulesCount }
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isChecked }

    val totalItems = totalModules + totalTasks
    val completedItems = completedModules + completedTasks
    val overallProgress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    val overallPct = (overallProgress * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item { TopBarArea(userName = userName) }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Progres Belajar Minggu Ini", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$overallPct% Selesai 🎉", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$completedItems dari $totalItems materi & tugas telah diselesaikan", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
        item {
            SectionHeader(
                title = "Mata Pelajaran",
                onSeeAllClick = { navController.navigate("pelajaran") }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects.take(3)) { subject ->
                    SubjectCard(subject = subject, onClick = { onSelectSubject(subject) })
                }
            }
        }
        item {
            SectionHeader(
                title = "Tugas Mendatang",
                onSeeAllClick = { navController.navigate("tugas") }
            )
        }
        items(tasks.take(3)) { task ->
            TaskItem(task = task, onToggle = onToggleTask)
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onSeeAllClick) {
            Text("Lihat Semua", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 150.dp, height = 150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(subject.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(subject.icon, contentDescription = null, tint = subject.color)
            }
            Column {
                Text(subject.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${subject.completedModulesCount}/${subject.modulesCount} Modul Selesai", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { subject.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = subject.color,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, isDetailed: Boolean = false, onToggle: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(task.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isChecked,
            onCheckedChange = { onToggle(task.id) },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.isChecked) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isDetailed) {
                Text(task.subject, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(task.deadline, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
            } else {
                Text("${task.subject} • ${task.deadline}", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PelajaranScreen(
    userName: String,
    subjects: List<Subject>,
    onSelectSubject: (Subject) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(userName = userName)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Cari mata pelajaran...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(subjects.filter { it.name.contains(searchQuery, ignoreCase = true) }) { subject ->
                SubjectListItem(subject = subject, onClick = { onSelectSubject(subject) })
            }
        }
    }
}

@Composable
fun AiScreen(userName: String = "Budi Santoso") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(userName = userName)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Penilaian Keseluruhan (AI)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Berdasarkan progres Anda, Anda sangat baik di bidang Eksakta, namun perlu meningkatkan waktu belajar untuk mata pelajaran Sosial. Teruskan kerja keras Anda!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Lihat Detail Evaluasi", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun SubjectListItem(subject: Subject, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(subject.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(subject.icon, contentDescription = null, tint = subject.color)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(subject.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${subject.category} • ${subject.completedModulesCount}/${subject.modulesCount} Modul", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { subject.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = subject.color,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun TugasScreen(
    userName: String,
    tasks: List<Task>,
    onToggleTask: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarArea(userName = userName)
            Text(
                "Daftar Tugas & Ujian",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(task = task, isDetailed = true, onToggle = onToggleTask)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Silakan pasang klien PC terlebih dahulu", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
            }
        }
    }
}

@Composable
fun ProfilScreen(
    userName: String = "Budi Santoso",
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBarArea(userName = userName)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                userName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Kelas X IPA 2 • NIS: 20251004",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val formattedEmail = "${userName.lowercase().replace(" ", ".")}@kawansekolah.sch.id"
                    ProfileListItem(Icons.Default.Email, "Email", formattedEmail)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileListItem(Icons.Default.School, "Sekolah", "SMA Negeri 1 Jakarta")
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))
                    ProfileListItem(Icons.Default.Star, "Poin Prestasi", "450 Poin")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar dari Akun", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileListItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// Subject Detail Bottom Sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailBottomSheet(
    subject: Subject,
    onDismiss: () -> Unit,
    onToggleModule: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subject.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(subject.icon, contentDescription = null, tint = subject.color)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(subject.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(subject.category, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Card
            val progressPct = (subject.progress * 100).toInt()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progres Modul", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$progressPct% Selesai", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { subject.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = subject.color,
                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${subject.completedModulesCount} dari ${subject.modulesCount} modul telah diselesaikan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Daftar Modul / Materi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subject.modules) { module ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { onToggleModule(module.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = module.isCompleted,
                            onCheckedChange = { onToggleModule(module.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = module.title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (module.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = module.duration,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
