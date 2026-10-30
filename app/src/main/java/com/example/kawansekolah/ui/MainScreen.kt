package com.example.kawansekolah.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kawansekolah.data.model.NotificationItem
import com.example.kawansekolah.data.model.NotificationType
import com.example.kawansekolah.data.model.SchoolLevel
import com.example.kawansekolah.data.model.Subject
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.SchoolRepository
import com.example.kawansekolah.ui.guru.GuruScreen
import kotlinx.coroutines.delay
import com.example.kawansekolah.ui.ai.AiScreen
import com.example.kawansekolah.ui.beranda.BerandaScreen
import com.example.kawansekolah.ui.components.BottomNavigationBar
import com.example.kawansekolah.ui.feature_insight.InsightScreen
import com.example.kawansekolah.ui.feature_nilai.InputNilaiScreen
import com.example.kawansekolah.ui.feature_nilai.UnggahCsvScreen
import com.example.kawansekolah.ui.feature_notifikasi.NotificationBottomSheet
import com.example.kawansekolah.ui.feature_statistik.StatistikScreen
import com.example.kawansekolah.ui.feature_wali.RekapNilaiScreen
import com.example.kawansekolah.ui.nilai.NilaiScreen
import com.example.kawansekolah.ui.pelajaran.PelajaranScreen
import com.example.kawansekolah.ui.pelajaran.components.SubjectDetailBottomSheet
import com.example.kawansekolah.ui.profil.ProfilScreen
import com.example.kawansekolah.ui.tampilan.TampilanScreen
import com.example.kawansekolah.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    userName: String = "Budi Santoso",
    userRole: UserRole = UserRole.SISWA,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onLogout: () -> Unit = {},
    initialNavigateTo: String? = null,
    onNavigateHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val isKeyboardOpen = WindowInsets.isImeVisible

    LaunchedEffect(initialNavigateTo) {
        if (!initialNavigateTo.isNullOrBlank()) {
            navController.navigate(initialNavigateTo) {
                popUpTo("beranda") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onNavigateHandled()
        }
    }

    // Repository State
    val initialSubjects = remember { SchoolRepository.getInitialSubjects() }
    val initialTasks = remember { SchoolRepository.getInitialTasks() }
    val initialGrades = remember { SchoolRepository.getInitialGrades() }

    var subjects by remember { mutableStateOf(initialSubjects) }
    var tasks by remember { mutableStateOf(initialTasks) }
    var grades by remember { mutableStateOf(initialGrades) }
    var selectedSubjectForDetail by remember { mutableStateOf<Subject?>(null) }

    // Real-time ticker state
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    // Notifications State
    val startMillis = remember { System.currentTimeMillis() }
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = 1,
                    title = "Login Berhasil",
                    message = "Selamat datang kembali, $userName! Akun ${userRole.label} Anda berhasil terhubung.",
                    timestampMillis = startMillis,
                    type = NotificationType.LOGIN,
                    isRead = false
                ),
                NotificationItem(
                    id = 2,
                    title = "Tugas Mendatang",
                    message = "Latihan Soal Aljabar Bab 3 jatuh tempo besok jam 23:59.",
                    timestampMillis = startMillis,
                    type = NotificationType.TASK,
                    relatedTaskId = 1,
                    isRead = false
                ),
                NotificationItem(
                    id = 3,
                    title = "Fitur AI Tutor Aktif",
                    message = "Tanyakan materi pelajaran atau latihan soal kapan saja dengan AI Tutor.",
                    timestampMillis = startMillis,
                    type = NotificationType.AI,
                    isRead = false
                )
            )
        )
    }

    val completedTaskIds = remember(tasks) { tasks.filter { it.isChecked }.map { it.id }.toSet() }

    val activeNotifications = remember(notifications, nowMillis, completedTaskIds) {
        notifications.filter { item ->
            if (item.type == NotificationType.LOGIN && (nowMillis - item.timestampMillis) >= 3_600_000L) {
                return@filter false
            }
            if (item.type == NotificationType.TASK && item.relatedTaskId != null && completedTaskIds.contains(item.relatedTaskId)) {
                return@filter false
            }
            true
        }
    }

    val unreadCount = activeNotifications.count { !it.isRead }
    var showNotificationSheet by remember { mutableStateOf(false) }

    val handleNotificationClick = {
        showNotificationSheet = true
    }

    val markAllNotificationsAsRead = {
        notifications = notifications.map { item ->
            item.copy(isRead = true)
        }
    }

    val clearAllNotifications = {
        notifications = notifications.filter { item ->
            item.type == NotificationType.AI_RUNNING
        }
    }

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

    val updateGrade: (String, Double, Int, String, String, Int, Int, Int, Int) -> Unit = { subjectName, score, semester, conduct, notes, hadir, izin, sakit, alpa ->
        grades = grades.map { item ->
            if (item.subjectName.equals(subjectName, ignoreCase = true) && item.semester == semester && item.level == SchoolLevel.SMP) {
                item.copy(
                    score = score,
                    conductStatus = conduct,
                    teacherNotes = notes,
                    hadirCount = hadir,
                    izinCount = izin,
                    sakitCount = sakit,
                    alpaCount = alpa
                )
            } else {
                item
            }
        }
    }

    val startDest = when (userRole) {
        UserRole.GURU -> "guru_input"
        UserRole.WALI_KELAS -> "rekap_wali"
        UserRole.ORANG_TUA -> "nilai"
        UserRole.SISWA -> "beranda"
    }

    val roleString = when (userRole) {
        UserRole.GURU -> "GURU_MAPEL"
        UserRole.WALI_KELAS -> "WALI_KELAS"
        UserRole.SISWA -> "SISWA"
        UserRole.ORANG_TUA -> "ORANG_TUA"
    }

    Scaffold(
        bottomBar = {
            if (!isKeyboardOpen && selectedSubjectForDetail == null) {
                BottomNavigationBar(navController, userRole)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("guru_input") {
                InputNilaiScreen(
                    userRole = roleString,
                    userId = "guru_smp_01",
                    onNavigateToCsvUpload = { navController.navigate("unggah_csv") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("unggah_csv") {
                UnggahCsvScreen(
                    userRole = roleString,
                    userId = "guru_smp_01",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("rekap_wali") {
                RekapNilaiScreen(
                    userRole = roleString,
                    userId = "guru_wali_01",
                    onNavigateToStatistik = { navController.navigate("statistik") }
                )
            }
            composable("statistik") {
                StatistikScreen(
                    userRole = roleString,
                    userId = if (userRole == UserRole.WALI_KELAS) "guru_wali_01" else "guru_smp_01"
                )
            }
            composable("insight_page") {
                InsightScreen(
                    userRole = roleString,
                    userId = "guru_smp_01"
                )
            }
            composable("guru_dashboard") {
                GuruScreen(
                    userName = userName,
                    grades = grades,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick,
                    onUpdateGrade = updateGrade
                )
            }
            composable("beranda") {
                BerandaScreen(
                    navController = navController,
                    userName = userName,
                    userRole = userRole,
                    subjects = subjects,
                    tasks = tasks,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick,
                    onToggleTask = toggleTask,
                    onSelectSubject = { selectedSubjectForDetail = it }
                )
            }
            composable("pelajaran") {
                PelajaranScreen(
                    userName = userName,
                    subjects = subjects,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick,
                    onSelectSubject = { selectedSubjectForDetail = it }
                )
            }
            composable("ai") {
                AiScreen(
                    userName = userName,
                    userRole = userRole,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick,
                    onNewNotification = { newNotification ->
                        notifications = notifications.filterNot { it.id == newNotification.id } + newNotification
                    },
                    onUpdateGrade = updateGrade
                )
            }
            composable("nilai") {
                NilaiScreen(
                    userName = userName,
                    userRole = userRole,
                    grades = grades,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick
                )
            }
            composable("profil") {
                ProfilScreen(
                    userName = userName,
                    userRole = userRole,
                    currentThemeMode = currentThemeMode,
                    unreadCount = unreadCount,
                    onNotificationClick = handleNotificationClick,
                    onThemeModeChange = onThemeModeChange,
                    onLogout = onLogout
                )
            }
            composable("tampilan") {
                TampilanScreen(
                    userName = userName,
                    unreadCount = unreadCount,
                    currentThemeMode = currentThemeMode,
                    onNotificationClick = handleNotificationClick,
                    onThemeModeChange = onThemeModeChange
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

    // Notification Bottom Sheet
    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = activeNotifications,
            nowMillis = nowMillis,
            onDismiss = {
                markAllNotificationsAsRead()
                showNotificationSheet = false
            },
            onClearAll = {
                clearAllNotifications()
            },
            onNotificationClick = { item ->
                markAllNotificationsAsRead()
                showNotificationSheet = false
                if (item.type == NotificationType.AI || item.type == NotificationType.AI_RUNNING) {
                    navController.navigate("ai") {
                        popUpTo("beranda") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}
