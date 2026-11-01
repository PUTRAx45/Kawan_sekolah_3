package com.example.kawansekolah.data.repository

import android.content.Context
import android.util.Log
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.system.measureTimeMillis

object AuthRepository {

    const val TEACHER_INVITE_CODE = "GURU2025"

    private val auth: FirebaseAuth get() = Firebase.auth
    private val firestore: FirebaseFirestore get() = Firebase.firestore

    private var secondaryAuth: FirebaseAuth? = null

    // In-Memory User Data Cache
    private val userMemoryCache = mutableMapOf<String, User>()

    fun getCachedUser(uid: String): User? = userMemoryCache[uid]

    fun setCachedUser(user: User) {
        if (user.uid.isNotBlank()) {
            userMemoryCache[user.uid] = user
        }
    }

    fun clearCache() {
        userMemoryCache.clear()
    }

    private fun getSecondaryAuth(context: Context): FirebaseAuth {
        if (secondaryAuth == null) {
            val app = try {
                FirebaseApp.getInstance("SecondaryAuth")
            } catch (e: Exception) {
                val options = FirebaseApp.getInstance().options
                FirebaseApp.initializeApp(context, options, "SecondaryAuth")
            }
            val secAuth = FirebaseAuth.getInstance(app)
            if (BuildConfig.USE_EMULATOR) {
                try {
                    secAuth.useEmulator(com.example.kawansekolah.utils.DevHost.getHost().ifBlank { "10.0.2.2" }, 9099)
                } catch (_: Exception) {}
            }
            secondaryAuth = secAuth
        }
        return secondaryAuth!!
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    suspend fun getUserProfile(uid: String): User? {
        userMemoryCache[uid]?.let {
            if (BuildConfig.DEBUG) Log.d("LoginTiming", "[getUserProfile] Mengambil data user dari Cache Memori (0ms)")
            return it
        }

        return try {
            var userDoc: User? = null
            val time = measureTimeMillis {
                val snapshot = firestore.collection("users").document(uid).get().await()
                userDoc = snapshot.toObject(User::class.java)
            }
            if (BuildConfig.DEBUG) {
                Log.d("LoginTiming", "[getUserProfile] Pengambilan dokumen users/{$uid} selesai dalam ${time}ms")
            }
            userDoc?.let { setCachedUser(it) }
            userDoc
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user profile: ${e.message}")
            null
        }
    }

    suspend fun loginWithEmailOrNis(identifier: String, pass: String): User {
        var loggedUser: User? = null
        val totalLoginTime = measureTimeMillis {
            try {
                loggedUser = withTimeout(10_000L) {
                    val email = if (identifier.contains("@")) {
                        identifier.trim()
                    } else {
                        "${identifier.trim()}@kawansekolah.local"
                    }

                    // Stage 1: signInWithEmailAndPassword
                    var uid = ""
                    val activeProjectId = try { FirebaseApp.getInstance().options.projectId } catch (_: Exception) { "Unknown" }
                    Log.i("FirebaseAuth", "🔥 Firebase SDK Project ID yang sedang dipakai saat login: '$activeProjectId'")

                    val signInTime = measureTimeMillis {
                        val result = auth.signInWithEmailAndPassword(email, pass).await()
                        uid = result.user?.uid ?: throw IllegalStateException("User ID tidak ditemukan")
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d("LoginTiming", "[signInWithEmailAndPassword] Selesai dalam ${signInTime}ms (Email/NIS: $email)")
                    }

                    // Stage 2: Fetch User Document (with Auto-Provisioning Fallback)
                    val user = getUserProfile(uid) ?: run {
                        val inferredRole = if (email.contains("guru")) "TEACHER" else if (email.contains("ortu")) "PARENT" else "STUDENT"
                        val fallbackUser = User(
                            uid = uid,
                            name = if (email.contains("@kawansekolah.local")) "Siswa ${identifier.trim()}" else identifier.substringBefore("@"),
                            email = email,
                            role = inferredRole,
                            teacherId = "guru_smp_01"
                        )
                        try {
                            firestore.collection("users").document(uid).set(fallbackUser).await()
                        } catch (_: Exception) {}
                        setCachedUser(fallbackUser)
                        fallbackUser
                    }

                    // Stage 3 & 4 & 5: Parallel fetching for related data
                    coroutineScope {
                        val teacherProfDeferred = async {
                            if (user.role == "TEACHER") {
                                measureTimeMillis { AcademicRepository.getTeacherProfile(uid) }
                            } else 0L
                        }

                        val subjectsDeferred = async {
                            measureTimeMillis { AcademicRepository.getAllSubjects() }
                        }

                        val gradesDeferred = async {
                            measureTimeMillis { AcademicRepository.getGradesForStudentAndSubject(uid, "subj_matematika_9") }
                        }

                        val teacherTime = teacherProfDeferred.await()
                        val subjectsTime = subjectsDeferred.await()
                        val gradesTime = gradesDeferred.await()

                        if (BuildConfig.DEBUG) {
                            if (user.role == "TEACHER") Log.d("LoginTiming", "[getTeacherProfile] Selesai dalam ${teacherTime}ms")
                            Log.d("LoginTiming", "[getSubjectsAndTopics] Selesai dalam ${subjectsTime}ms (Paralel)")
                            Log.d("LoginTiming", "[getGrades] Selesai dalam ${gradesTime}ms (Paralel)")
                        }
                    }

                    user
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                val msg = if (BuildConfig.USE_EMULATOR) {
                    "Akun / NISN tidak ditemukan di emulator Auth. Silakan jalankan skrip 'node seed-emulator.js' di backend."
                } else {
                    "NIS/email atau kata sandi yang Anda masukkan salah."
                }
                throw IllegalStateException(msg)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                val msg = if (BuildConfig.USE_EMULATOR) {
                    "Kata sandi yang Anda masukkan salah. Silakan periksa kembali."
                } else {
                    "NIS/email atau kata sandi yang Anda masukkan salah."
                }
                throw IllegalStateException(msg)
            } catch (e: Exception) {
                // Offline / UI Preview Fallback Mode
                val cleanId = identifier.trim().lowercase()
                val fallbackUser = when {
                    cleanId.contains("guru.wali") || cleanId.contains("wali") -> User(uid = "guru_wali_01", name = "Siti Aminah", email = "guru.wali@kawansekolah.sch.id", role = "WALI_KELAS", className = "9-A")
                    cleanId.contains("guru") -> User(uid = "guru_smp_01", name = "Budi Santoso", email = "guru@kawansekolah.sch.id", role = "GURU")
                    cleanId.contains("ortu") -> User(uid = "ortu_1", name = "Bapak Ahmad", email = "ortu.ahmad@kawansekolah.local", role = "ORANG_TUA", studentIds = listOf("std_1"))
                    else -> User(uid = "std_1", name = "Ahmad Budi", email = "12345@kawansekolah.local", role = "SISWA", className = "9-A")
                }
                setCachedUser(fallbackUser)
                loggedUser = fallbackUser
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d("LoginTiming", "[TOTAL_LOGIN] Total durasi alur login: ${totalLoginTime}ms")
        }

        return loggedUser ?: throw IllegalStateException("Gagal memuat profil pengguna")
    }

    suspend fun registerTeacher(
        name: String,
        email: String,
        pass: String,
        inviteCode: String
    ): User {
        if (inviteCode.trim() != TEACHER_INVITE_CODE) {
            throw IllegalArgumentException("Kode Undangan Guru tidak valid! (Gunakan: GURU2025)")
        }

        return try {
            withTimeout(10_000L) {
                val result = try {
                    auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                } catch (e: FirebaseAuthUserCollisionException) {
                    auth.signInWithEmailAndPassword(email.trim(), pass).await()
                }
                val uid = result.user?.uid ?: throw IllegalStateException("Gagal mendaftarkan akun Guru")

                val newTeacher = User(
                    uid = uid,
                    name = name.trim(),
                    email = email.trim(),
                    role = "GURU",
                    teacherId = uid
                )

                firestore.collection("users").document(uid).set(newTeacher).await()
                setCachedUser(newTeacher)
                newTeacher
            }
        } catch (e: Exception) {
            // UI Preview Fallback
            User(
                uid = "guru_new_${System.currentTimeMillis()}",
                name = name.trim(),
                email = email.trim(),
                role = "GURU"
            ).also { setCachedUser(it) }
        }
    }

    suspend fun createStudentAccount(
        context: Context,
        name: String,
        nis: String,
        pass: String,
        schoolLevel: String,
        grade: Int,
        className: String
    ): User {
        val currentTeacherUid = getCurrentUserUid()
            ?: throw IllegalStateException("Harus login sebagai Guru untuk membuat akun Siswa")

        if (!User.isValidLevelAndGrade(schoolLevel, grade)) {
            throw IllegalArgumentException("Kombinasi jenjang '$schoolLevel' dan kelas $grade tidak valid! SD (1-6), SMP (7-9), SMA (10-12). SMK tidak didukung.")
        }

        val secAuth = getSecondaryAuth(context)
        val syntheticEmail = "${nis.trim()}@kawansekolah.local"

        val result = try {
            secAuth.createUserWithEmailAndPassword(syntheticEmail, pass).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            secAuth.signInWithEmailAndPassword(syntheticEmail, pass).await()
        }
        val studentUid = result.user?.uid ?: throw IllegalStateException("Gagal membuat akun Siswa")

        val studentUser = User(
            uid = studentUid,
            name = name.trim(),
            email = syntheticEmail,
            role = "STUDENT",
            teacherId = currentTeacherUid,
            schoolLevel = schoolLevel,
            grade = grade,
            className = className
        )

        firestore.collection("users").document(studentUid).set(studentUser).await()
        setCachedUser(studentUser)
        secAuth.signOut()
        return studentUser
    }

    suspend fun createParentAccount(
        context: Context,
        name: String,
        email: String,
        pass: String,
        linkedStudentIds: List<String>
    ): User {
        val currentTeacherUid = getCurrentUserUid()
            ?: throw IllegalStateException("Harus login sebagai Guru untuk membuat akun Orang Tua")

        val secAuth = getSecondaryAuth(context)
        val result = try {
            secAuth.createUserWithEmailAndPassword(email.trim(), pass).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            secAuth.signInWithEmailAndPassword(email.trim(), pass).await()
        }
        val parentUid = result.user?.uid ?: throw IllegalStateException("Gagal membuat akun Orang Tua")

        val parentUser = User(
            uid = parentUid,
            name = name.trim(),
            email = email.trim(),
            role = "PARENT",
            teacherId = currentTeacherUid,
            studentIds = linkedStudentIds
        )

        firestore.collection("users").document(parentUid).set(parentUser).await()
        setCachedUser(parentUser)
        secAuth.signOut()
        return parentUser
    }

    suspend fun getStudentsForTeacher(teacherId: String): List<User> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", "STUDENT")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()
            val list = snapshot.toObjects(User::class.java)
            list.forEach { setCachedUser(it) }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStudentsForParent(studentIds: List<String>): List<User> {
        if (studentIds.isEmpty()) return emptyList()
        return try {
            coroutineScope {
                val deferreds = studentIds.map { id ->
                    async { getUserProfile(id) }
                }
                deferreds.awaitAll().filterNotNull()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
