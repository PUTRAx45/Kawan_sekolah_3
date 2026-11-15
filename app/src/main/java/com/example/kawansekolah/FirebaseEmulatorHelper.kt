package com.example.kawansekolah

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.kawansekolah.utils.DevHost
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

object FirebaseEmulatorHelper {
    private var isEmulatorConfigured = false

    fun isPhysicalDevice(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val product = Build.PRODUCT
        val hardware = Build.HARDWARE
        val board = Build.BOARD

        val isEmulator = fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                fingerprint.contains("emu") ||
                fingerprint.contains("sdk") ||
                model.contains("google_sdk") ||
                model.contains("Emulator") ||
                model.contains("Android SDK built for x86") ||
                model.contains("sdk_gphone") ||
                manufacturer.contains("Genymotion") ||
                hardware.contains("goldfish") ||
                hardware.contains("ranchu") ||
                hardware.contains("vbox86") ||
                product.contains("sdk") ||
                product.contains("google_sdk") ||
                product.contains("emulator") ||
                product.contains("gphone") ||
                brand.startsWith("generic") ||
                board.contains("goldfish")

        return !isEmulator
    }

    fun setupEmulators(
        context: Context? = null,
        useEmulator: Boolean = BuildConfig.USE_EMULATOR,
        overrideHost: String? = null
    ) {
        if (!useEmulator || isEmulatorConfigured) return

        val host = overrideHost ?: DevHost.getHost(context)
        val targetHost = host.trim().ifBlank {
            if (isPhysicalDevice()) "" else "10.0.2.2"
        }

        if (isPhysicalDevice() && targetHost.isBlank()) {
            Log.w("ScolarsiaNet", "⚠️ HP FISIK DETECTED (${Build.MANUFACTURER} ${Build.MODEL}): Alamat server belum diatur. Gunakan dialog Alamat Server (ikon ⚙️ di layar login).")
            return
        }

        try {
            val activeProjectId = try { FirebaseApp.getInstance().options.projectId } catch (_: Exception) { "Unknown" }

            Log.i("ScolarsiaNet", "======================================================")
            Log.i("ScolarsiaNet", "📡 KONFIGURASI KONEKSI EMULATOR SCOLARSIA:")
            Log.i("ScolarsiaNet", "   Project ID: '$activeProjectId'")
            Log.i("ScolarsiaNet", "   Auth -> $targetHost:9099")
            Log.i("ScolarsiaNet", "   Firestore -> $targetHost:8081")
            Log.i("ScolarsiaNet", "   Backend -> $targetHost:3000")
            Log.i("ScolarsiaNet", "======================================================")

            Firebase.firestore.firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { })
                isSslEnabled = false
            }
            Firebase.firestore.useEmulator(targetHost, 8081)
            Log.d("ScolarsiaNet", "✅ Firestore emulator terhubung ke $targetHost:8081")

            Firebase.auth.useEmulator(targetHost, 9099)
            Log.d("ScolarsiaNet", "✅ Auth emulator terhubung ke $targetHost:9099")

            isEmulatorConfigured = true
        } catch (e: Exception) {
            Log.w("ScolarsiaNet", "⚠️ Firestore/Auth emulator sudah diinisialisasi atau mengalami kendala di host '$targetHost': ${e.message}")
        }
    }
}
