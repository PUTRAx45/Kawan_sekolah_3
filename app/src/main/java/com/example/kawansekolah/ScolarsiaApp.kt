package com.example.kawansekolah

import android.app.Application
import android.util.Log
import com.example.kawansekolah.utils.NotificationHelper

class ScolarsiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("ScolarsiaApp", "🚀 ScolarsiaApp.onCreate() dipanggil. Menginisialisasi Firebase Emulator...")
        FirebaseEmulatorHelper.setupEmulators(context = this, useEmulator = BuildConfig.USE_EMULATOR)
        NotificationHelper.createNotificationChannel(this)
    }
}
