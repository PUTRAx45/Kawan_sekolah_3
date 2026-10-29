package com.example.kawansekolah

import android.app.Application
import com.example.kawansekolah.utils.NotificationHelper

class ScolarsiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseEmulatorHelper.setupEmulators(useEmulator = BuildConfig.USE_EMULATOR)
        NotificationHelper.createNotificationChannel(this)
    }
}
