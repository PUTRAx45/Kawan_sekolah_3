package com.example.kawansekolah.utils

import android.content.Context
import com.example.kawansekolah.BuildConfig
import com.example.kawansekolah.FirebaseEmulatorHelper

object DevHost {

    private const val PREF_NAME = "scolarsia_dev_host_prefs"
    private const val KEY_DYNAMIC_HOST = "dynamic_server_host"

    @Volatile
    private var cachedDynamicHost: String? = null

    fun getSavedDynamicHost(context: Context?): String {
        if (cachedDynamicHost != null) return cachedDynamicHost!!
        if (context == null) return ""
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val host = prefs.getString(KEY_DYNAMIC_HOST, "") ?: ""
        cachedDynamicHost = host
        return host
    }

    fun saveDynamicHost(context: Context, host: String) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").substringBefore(":")
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DYNAMIC_HOST, cleanHost).commit() // Synchronous commit
        cachedDynamicHost = cleanHost
    }

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
     * Resolves host IP address and identifies its source:
     * Returns Pair(host, sourceString)
     */
    fun getHostWithSource(context: Context? = null): Pair<String, String> {
        if (!BuildConfig.USE_EMULATOR) {
            return Pair("", "Production Online")
        }

        // Priority 1: SharedPreferences / DataStore
        val dynamicHost = getSavedDynamicHost(context)
        if (dynamicHost.isNotBlank()) {
            return Pair(dynamicHost, "DataStore / SharedPreferences")
        }

        // Priority 2: ADB Mode
        if (isAdbMode()) {
            return Pair("127.0.0.1", "ADB Mode")
        }

        // Priority 3: BuildConfig DEV_SERVER_HOST / DEV_HOST
        val buildConfigHost = try {
            val h = BuildConfig.DEV_SERVER_HOST.trim()
            if (h.isNotBlank()) h else BuildConfig.DEV_HOST.trim()
        } catch (_: Throwable) {
            BuildConfig.DEV_HOST.trim()
        }

        if (buildConfigHost.isNotBlank()) {
            return Pair(buildConfigHost, "BuildConfig / local.properties")
        }

        // Priority 4: Default for Emulator vs Physical Device
        if (isPhysicalDevice()) {
            return Pair("", "Kosong (HP Fisik Belum Dikonfigurasi)")
        }

        return Pair("10.0.2.2", "Android Emulator Default (10.0.2.2)")
    }

    fun getHost(context: Context? = null): String {
        return getHostWithSource(context).first
    }

    fun getBackendBaseUrl(context: Context? = null): String {
        if (!BuildConfig.USE_EMULATOR) {
            return BuildConfig.BASE_URL
        }
        val host = getHost(context)
        if (host.isBlank()) {
            return "http://10.0.2.2:3000/"
        }
        return "http://$host:3000/"
    }

    fun getDiagnosticsWarningMessage(context: Context? = null): String {
        if (!BuildConfig.USE_EMULATOR) {
            return if (isPlaceholderBaseUrl()) {
                "⚠️ BASE_URL MASIH PLACEHOLDER!\nURL backend belum diisi di 'app/build.gradle.kts'."
            } else {
                "🌐 MODE PRODUKSI ONLINE:\nAplikasi terhubung langsung ke Firebase Cloud dan Backend AI Online."
            }
        }

        val (currentHost, source) = getHostWithSource(context)

        return if (isPhysicalDevice() && currentHost.isBlank()) {
            "⚠️ ALAMAT SERVER BELUM DIATUR!\n" +
            "Silakan isi IP Wi-Fi laptop Anda (misal: 192.168.1.xxx) pada dialog Alamat Server (ikon ⚙️ di layar login)."
        } else if (isAdbMode()) {
            "📱 MODE ADB REVERSE ($currentHost):\n" +
            "Pastikan port forwarding ADB sudah diaktifkan di komputer:\n" +
            "• adb reverse tcp:3000 tcp:3000 (Backend API)\n" +
            "• adb reverse tcp:8081 tcp:8081 (Firestore Emulator)\n" +
            "• adb reverse tcp:9099 tcp:9099 (Auth Emulator)"
        } else if (isPhysicalDevice()) {
            "🌐 MODE LAN WI-FI ($currentHost) [Sumber: $source]:\n" +
            "Aplikasi terhubung ke laptop via IP '$currentHost'. Pastikan HP dan laptop berada dalam satu jaringan Wi-Fi."
        } else {
            "💻 ANDROID EMULATOR ($currentHost) [Sumber: $source]:\n" +
            "Terhubung ke localhost via alias '10.0.2.2'."
        }
    }
}
