package com.example.kawansekolah.utils

import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper

object DevHost {

    fun isAdbMode(): Boolean {
        return BuildConfig.DEV_MODE.lowercase() == "adb"
    }

    fun isPhysicalDevice(): Boolean {
        return FirebaseEmulatorHelper.isPhysicalDevice()
    }

    fun isPlaceholderBaseUrl(): Boolean {
        return BuildConfig.BASE_URL.contains("GANTI-DENGAN-URL-BACKEND")
    }

    /**
     * Resolves host IP address:
     * - In ADB mode: "127.0.0.1"
     * - On Physical Device with dev.host set: BuildConfig.DEV_HOST
     * - On Android Emulator: "10.0.2.2"
     * - Fallback on Physical Device without dev.host: "10.0.2.2"
     */
    fun getHost(): String {
        if (!BuildConfig.USE_EMULATOR) {
            return ""
        }

        if (isAdbMode()) {
            return "127.0.0.1"
        }

        if (isPhysicalDevice()) {
            val configuredHost = BuildConfig.DEV_HOST.trim()
            if (configuredHost.isNotBlank()) {
                return configuredHost
            }
            return "10.0.2.2"
        }

        return "10.0.2.2"
    }

    fun isHostConfiguredForPhysicalDevice(): Boolean {
        if (!isPhysicalDevice()) return true
        if (isAdbMode()) return true
        return BuildConfig.DEV_HOST.trim().isNotBlank()
    }

    fun getBackendBaseUrl(): String {
        if (!BuildConfig.USE_EMULATOR) {
            return BuildConfig.BASE_URL
        }
        val host = getHost()
        return "http://$host:3000/"
    }

    fun getDiagnosticsWarningMessage(): String {
        if (!BuildConfig.USE_EMULATOR) {
            return if (isPlaceholderBaseUrl()) {
                "⚠️ BASE_URL MASIH PLACEHOLDER!\nURL backend belum diisi di 'app/build.gradle.kts'. Harap ganti 'https://GANTI-DENGAN-URL-BACKEND/' dengan URL server backend online Anda."
            } else {
                "🌐 MODE PRODUKSI ONLINE:\nAplikasi terhubung langsung ke Firebase Cloud dan Backend AI Online (${BuildConfig.BASE_URL})."
            }
        }

        val currentHost = getHost()
        val configured = isHostConfiguredForPhysicalDevice()

        return if (isPhysicalDevice() && !configured) {
            "⚠️ PERANGKAT FISIK DETECTED!\n" +
            "Alamat '10.0.2.2' gagal karena '10.0.2.2' khusus Android Emulator.\n\n" +
            "Silakan isi IP Wi-Fi laptop Anda di file 'local.properties':\n" +
            "  dev.host=192.168.1.xxx\n\n" +
            "Atau jika memakai kabel USB ADB:\n" +
            "  dev.mode=adb\n" +
            "  (Lalu jalankan 'adb reverse tcp:3000 tcp:3000', dst)"
        } else if (isAdbMode()) {
            "📱 MODE ADB REVERSE ($currentHost):\n" +
            "Pastikan port forwarding ADB sudah diaktifkan di komputer:\n" +
            "• adb reverse tcp:3000 tcp:3000\n" +
            "• adb reverse tcp:8080 tcp:8080\n" +
            "• adb reverse tcp:9099 tcp:9099"
        } else if (isPhysicalDevice()) {
            "🌐 MODE LAN WI-FI ($currentHost):\n" +
            "Aplikasi terhubung ke laptop via IP '$currentHost'. Pastikan HP dan laptop berada dalam satu jaringan Wi-Fi."
        } else {
            "💻 ANDROID EMULATOR ($currentHost):\n" +
            "Terhubung ke localhost via alias '10.0.2.2'."
        }
    }
}
