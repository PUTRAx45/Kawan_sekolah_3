package com.example.kawansekolah.data.repository

import android.content.Context
import android.util.Log
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper
import com.example.kawansekolah.data.model.FirestoreCollections
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.utils.DeviceSessionManager
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

object AuthRepository {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val firestore: FirebaseFirestore get() = Firebase.firestore

    private var secondaryAuth: FirebaseAuth? = null

    // In-Memory User Data Cache
    private val userMemoryCache = mutableMapOf<String, User>()
    private var activeUserUid: String? = null

    fun getCachedUser(uid: String): User? = userMemoryCache[uid]

    fun setCachedUser(user: User) {
        if (user.uid.isNotBlank()) {
            userMemoryCache[user.uid] = user
            activeUserUid = user.uid
        }
    }

    fun clearCache() {
        userMemoryCache.clear()
        activeUserUid = null
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
                    secAuth.useEmulator(DevHost.getHost().ifBlank { "10.0.2.2" }, 9099)
                } catch (_: Exception) {}
            }
            secondaryAuth = secAuth
        }
        return secondaryAuth!!
    }

    fun getCurrentUserUid(): String? = auth.currentUser?.uid ?: activeUserUid

    suspend fun getUserProfile(uid: String): User? {
        userMemoryCache[uid]?.let {
            if (BuildConfig.DEBUG) Log.d("LoginTiming", "[getUserProfile] Mengambil data user dari Cache Memori (0ms)")
            return it
        }

        return try {
            var userDoc: User? = null

            // Try pengguna collection first
            val doc1 = firestore.collection(FirestoreCollections.PENGGUNA).document(uid).get().await()
            if (doc1.exists()) {
                val parsed = doc1.toObject(User::class.java)
                val namaVal = doc1.getString("namaLengkap") ?: doc1.getString("nama") ?: parsed?.name ?: ""
                val emailVal = doc1.getString("email") ?: parsed?.email ?: ""
                val roleVal = doc1.getString("role") ?: parsed?.role ?: "SISWA"
                val isAktif = doc1.getBoolean("aktif") ?: doc1.getBoolean("isActive") ?: parsed?.aktif ?: true

                userDoc = User(
                    uid = uid,
                    name = namaVal,
                    email = emailVal,
                    role = roleVal,
                    aktif = isAktif
                )
            } else {
                // Fallback to users collection
                val doc2 = firestore.collection("users").document(uid).get().await()
                if (doc2.exists()) {
                    val parsed = doc2.toObject(User::class.java)
                    val namaVal = doc2.getString("namaLengkap") ?: doc2.getString("nama") ?: parsed?.name ?: ""
                    val emailVal = doc2.getString("email") ?: parsed?.email ?: ""
                    val roleVal = doc2.getString("role") ?: parsed?.role ?: "SISWA"
                    val isAktif = doc2.getBoolean("aktif") ?: doc2.getBoolean("isActive") ?: parsed?.aktif ?: true

                    userDoc = User(
                        uid = uid,
                        name = namaVal,
                        email = emailVal,
                        role = roleVal,
                        aktif = isAktif
                    )
                }
            }

            userDoc?.let { setCachedUser(it) }
            userDoc
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user profile: ${e.message}")
            throw e
        }
    }

    fun normalizeRole(roleStr: String): String {
        return when (roleStr.lowercase().trim()) {
            "admin", "administrator" -> "ADMIN"
            "guru", "teacher", "guru_mapel" -> "GURU"
            "wali_kelas", "walikelas", "homeroom_teacher" -> "WALI_KELAS"
            "siswa", "student" -> "SISWA"
            "orang_tua", "orangtua", "parent" -> "ORANG_TUA"
            else -> roleStr.uppercase()
        }
    }

    /**
     * Diagnostic function to directly test Auth Emulator REST API over HTTP from HP
     */
    suspend fun testRestAuthDirect(targetHost: String, email: String, pass: String): Pair<Int, String> {
        return withContext(Dispatchers.IO) {
            val hostStr = targetHost.ifBlank { "10.0.2.2" }
            val urlStr = "http://$hostStr:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake"
            try {
                val url = java.net.URL(urlStr)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.doOutput = true

                val jsonBody = "{\"email\":\"$email\",\"password\":\"$pass\",\"returnSecureToken\":true}"
                conn.outputStream.use { os -> os.write(jsonBody.toByteArray()) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val reply = stream?.bufferedReader()?.use { it.readText() } ?: ""
                val snippet = reply.take(200).replace("\n", " ")

                Log.i("ScolarsiaNet", "🔬 [REST Auth Direct Test] Host: $hostStr:9099 | Email: $email | Status: $code | Reply (200 char): \"$snippet\"")
                Pair(code, snippet)
            } catch (e: Exception) {
                Log.e("ScolarsiaNet", "🔬 [REST Auth Direct Test Error] Host: $hostStr:9099 | Exception: ${e.javaClass.simpleName} (${e.message})")
                Pair(0, "Gagal koneksi REST: ${e.message}")
            }
        }
    }

    suspend fun loginWithEmailOrNis(
        identifier: String,
        pass: String,
        expectedRole: UserRole = UserRole.SISWA,
        context: Context? = null
    ): User {
        return withTimeout(10_000L) {
            val email = if (identifier.contains("@")) {
                identifier.trim()
            } else {
                "${identifier.trim()}@scolarsia.ac.id"
            }

            val appOptions = try { FirebaseApp.getInstance().options } catch (_: Exception) { null }
            val activeProjectId = appOptions?.projectId ?: "Unknown"
            val apiKeyPrefix = (appOptions?.apiKey ?: "").take(6)
            val currentHost = DevHost.getHost(context)
            val isApplied = FirebaseEmulatorHelper.isEmulatorConfigured

            Log.i("ScolarsiaAuth", "======================================================")
            Log.i("ScolarsiaAuth", "🔑 MEMULAI SIGN IN FIREBASE AUTH:")
            Log.i("ScolarsiaAuth", "   Email Target : $email")
            Log.i("ScolarsiaAuth", "   Project ID   : '$activeProjectId'")
            Log.i("ScolarsiaAuth", "   API Key 6-char: '$apiKeyPrefix...'")
            Log.i("ScolarsiaAuth", "   Host Target  : '$currentHost:9099'")
            Log.i("ScolarsiaAuth", "   useEmulator  : $isApplied")
            Log.i("ScolarsiaAuth", "======================================================")

            // Block login immediately if emulator is NOT configured on Debug physical device
            if (BuildConfig.DEBUG && BuildConfig.USE_EMULATOR && !isApplied) {
                if (DevHost.isPhysicalDevice() && currentHost.isBlank()) {
                    Log.w("ScolarsiaAuth", "Login DIBLOKIR: Alamat server belum diatur pada HP Fisik.")
                    throw IllegalStateException("Alamat server belum diatur. Buka ikon roda gigi ⚙️ di pojok kanan atas lalu isi IP Wi-Fi laptop Anda.")
                }
            }

            // 1. Firebase Auth signInWithEmailAndPassword
            val signInResult = try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: FirebaseAuthException) {
                val excName = e.javaClass.simpleName
                val errorCode = e.errorCode
                val msg = e.message ?: ""
                val stackSnippet = e.stackTrace.take(3).joinToString("\n   at ")

                Log.e("ScolarsiaAuth", "❌ [FirebaseAuthException] Class: $excName | Code: $errorCode | Message: $msg")
                Log.e("ScolarsiaAuth", "   ProjectID: '$activeProjectId' | APIKey: '$apiKeyPrefix...' | Host: '$currentHost:9099' | emulatorApplied: $isApplied")
                Log.e("ScolarsiaAuth", "   Stack: at $stackSnippet")

                if (errorCode == "ERROR_CONFIGURATION_NOT_FOUND" || msg.contains("CONFIGURATION_NOT_FOUND")) {
                    throw IllegalStateException("Aplikasi terhubung ke server yang salah (bukan emulator). Periksa Alamat Server pada ikon ⚙️.")
                }

                throw IllegalStateException("Gagal masuk Auth ($errorCode): $msg")
            } catch (e: FirebaseAuthInvalidUserException) {
                Log.w("ScolarsiaAuth", "Login gagal: Email/NISN '$email' tidak ditemukan pada Auth Emulator.")
                throw IllegalStateException("Akun / NISN tidak ditemukan. Silakan periksa kembali.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.w("ScolarsiaAuth", "Login gagal: Kata sandi untuk '$email' salah.")
                throw IllegalStateException("Kata sandi yang Anda masukkan salah. Silakan periksa kembali.")
            } catch (e: TimeoutCancellationException) {
                val targetHost = DevHost.getHost(context).ifBlank { "10.0.2.2" }
                Log.e("ScolarsiaAuth", "Login timeout: Tidak bisa menjangkau $targetHost:9099")
                throw IllegalStateException("Tidak bisa menjangkau $targetHost:9099. Pastikan laptop menyala, satu Wi-Fi, dan firewall terbuka (buka-firewall.bat).")
            } catch (e: Exception) {
                Log.e("ScolarsiaAuth", "❌ [Login Exception] Class: ${e.javaClass.simpleName} | Message: ${e.message}")
                throw IllegalStateException("Gagal masuk Auth: ${e.message}")
            }

            val uid = signInResult.user?.uid ?: throw IllegalStateException("User ID tidak ditemukan pada Firebase Auth.")

            // 2. Fetch profile from Firestore
            val userProfile = try {
                getUserProfile(uid)
            } catch (e: FirebaseFirestoreException) {
                auth.signOut()
                val targetHost = DevHost.getHost(context).ifBlank { "10.0.2.2" }
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw IllegalStateException("Tidak bisa menjangkau Firestore emulator di $targetHost:8081. Pastikan laptop menyala, satu Wi-Fi, dan port 8081 terbuka (buka-firewall.bat).")
                } else {
                    throw IllegalStateException("Gagal membaca Firestore: ${e.message} (Kode: ${e.code})")
                }
            }

            if (userProfile == null) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "UID: $uid | Dokumen profil tidak ditemukan pada koleksi pengguna/users.")
                throw IllegalStateException("Dokumen profil tidak ditemukan (UID: $uid). Akun belum didaftarkan oleh admin sekolah.")
            }

            // 3. Verify Active Status
            if (!userProfile.aktif) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "UID: $uid | Status akun nonaktif (aktif=false).")
                throw IllegalStateException("Akun dinonaktifkan. Silakan hubungi administrator sekolah.")
            }

            // 4. Verify Expected Role
            val actualRoleNorm = normalizeRole(userProfile.role)
            val expectedRoleNorm = normalizeRole(expectedRole.name)

            if (actualRoleNorm != expectedRoleNorm) {
                auth.signOut()
                Log.w("ScolarsiaAuth", "UID: $uid | Role Firestore: $actualRoleNorm | Role Kartu: $expectedRoleNorm | Keputusan: REJECTED (Role Mismatch)")
                throw IllegalStateException("Peran tidak cocok (Akun ini mendaftar sebagai ${userProfile.userRole.label}, bukan ${expectedRole.label}).")
            }

            // 5. Track device session in Firestore if context is available
            context?.let { ctx ->
                DeviceSessionManager.saveCurrentSession(ctx, uid)
            }

            Log.i("ScolarsiaAuth", "UID: $uid | Role Firestore: $actualRoleNorm | Role Kartu: $expectedRoleNorm | Keputusan: SUCCESS")
            setCachedUser(userProfile)
            userProfile
        }
    }

    suspend fun registerTeacher(
        name: String,
        email: String,
        pass: String,
        schoolLevel: String,
        grade: Int,
        className: String,
        context: Context
    ): User {
        val secAuth = getSecondaryAuth(context)
        val authRes = secAuth.createUserWithEmailAndPassword(email, pass).await()
        val uid = authRes.user?.uid ?: throw IllegalStateException("Gagal mendaftarkan akun guru di Firebase Auth")

        val newUser = User(
            uid = uid,
            name = name,
            email = email,
            role = "GURU",
            schoolLevel = schoolLevel,
            grade = grade,
            className = className,
            teacherId = uid,
            aktif = true
        )

        firestore.collection("pengguna").document(uid).set(newUser).await()
        firestore.collection("users").document(uid).set(newUser).await()
        secAuth.signOut()
        return newUser
    }

    suspend fun createParentAccount(
        context: Context,
        name: String,
        email: String,
        pass: String,
        linkedStudentIds: List<String>
    ): User {
        val secAuth = getSecondaryAuth(context)
        val authRes = secAuth.createUserWithEmailAndPassword(email, pass).await()
        val uid = authRes.user?.uid ?: throw IllegalStateException("Gagal mendaftarkan akun Orang Tua di Firebase Auth")

        val newUser = User(
            uid = uid,
            name = name,
            email = email,
            role = "ORANG_TUA",
            studentIds = linkedStudentIds,
            aktif = true
        )

        firestore.collection("pengguna").document(uid).set(newUser).await()
        firestore.collection("users").document(uid).set(newUser).await()
        secAuth.signOut()
        return newUser
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
        val secAuth = getSecondaryAuth(context)
        val syntheticEmail = "${nis.trim()}@scolarsia.ac.id"
        val authRes = secAuth.createUserWithEmailAndPassword(syntheticEmail, pass).await()
        val uid = authRes.user?.uid ?: throw IllegalStateException("Gagal mendaftarkan akun Siswa di Firebase Auth")

        val newUser = User(
            uid = uid,
            name = name,
            email = syntheticEmail,
            role = "SISWA",
            schoolLevel = schoolLevel,
            grade = grade,
            className = className,
            aktif = true
        )

        firestore.collection("pengguna").document(uid).set(newUser).await()
        firestore.collection("users").document(uid).set(newUser).await()
        secAuth.signOut()
        return newUser
    }
}
