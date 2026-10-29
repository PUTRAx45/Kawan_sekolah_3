package com.example.kawansekolah

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
        useEmulator: Boolean = BuildConfig.USE_EMULATOR,
        host: String = DevHost.getHost()
    ) {
        if (!useEmulator || isEmulatorConfigured) return

        try {
            val targetHost = host.ifBlank { "10.0.2.2" }
            val activeProjectId = try { FirebaseApp.getInstance().options.projectId } catch (_: Exception) { "Unknown" }
            Log.i("FirebaseEmulator", "🔥 Firebase SDK Project ID aktif: '$activeProjectId'")

            if (isPhysicalDevice()) {
                Log.w(
                    "FirebaseEmulator",
                    "⚠️ Aplikasi berjalan di PERANGKAT FISIK (${Build.MANUFACTURER} ${Build.MODEL}). Target Host: $targetHost"
                )
            } else {
                Log.d("FirebaseEmulator", "📱 Aplikasi berjalan di Android Emulator (${Build.MODEL}). Target Host: $targetHost")
            }

            Firebase.firestore.firestoreSettings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings { })
            }
            Firebase.firestore.useEmulator(targetHost, 8080)
            Log.d("FirebaseEmulator", "✅ Firestore emulator terhubung ke $targetHost:8080")

            Firebase.auth.useEmulator(targetHost, 9099)
            Log.d("FirebaseEmulator", "✅ Auth emulator terhubung ke $targetHost:9099")

            isEmulatorConfigured = true
        } catch (e: Exception) {
            Log.e("FirebaseEmulator", "❌ Gagal terhubung ke emulator: ${e.message}")
        }
    }
}
