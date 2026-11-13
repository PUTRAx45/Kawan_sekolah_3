package com.example.kawansekolah.utils

import android.content.Context
import android.os.Build
import com.example.kawansekolah.data.model.FirestoreCollections
import com.example.kawansekolah.data.model.SesiPerangkatModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import java.util.UUID

object DeviceSessionManager {

    private const val PREF_NAME = "scolarsia_device_prefs"
    private const val KEY_DEVICE_ID = "unique_device_id"

    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId.isNullOrBlank()) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun getOsVersion(): String {
        return "Android ${Build.VERSION.RELEASE}"
    }

    fun saveCurrentSession(context: Context, userUid: String) {
        if (userUid.isBlank()) return
        val deviceId = getDeviceId(context)
        val deviceName = getDeviceName()
        val osVer = getOsVersion()
        val now = System.currentTimeMillis()

        val sessionData = hashMapOf(
            "id" to deviceId,
            "userUid" to userUid,
            "namaPerangkat" to "$deviceName • $osVer",
            "terakhirAktif" to now,
            "aktif" to true
        )

        try {
            Firebase.firestore.collection(FirestoreCollections.PENGGUNA)
                .document(userUid)
                .collection("sesiPerangkat")
                .document(deviceId)
                .set(sessionData)
        } catch (_: Exception) {}

        try {
            Firebase.firestore.collection("users")
                .document(userUid)
                .collection("sessions")
                .document(deviceId)
                .set(sessionData)
        } catch (_: Exception) {}
    }

    fun markCurrentSessionInactive(context: Context, userUid: String) {
        if (userUid.isBlank()) return
        val deviceId = getDeviceId(context)
        try {
            Firebase.firestore.collection(FirestoreCollections.PENGGUNA)
                .document(userUid)
                .collection("sesiPerangkat")
                .document(deviceId)
                .update("aktif", false, "terakhirAktif", System.currentTimeMillis())
        } catch (_: Exception) {}
    }

    fun listenOtherSessions(
        context: Context,
        userUid: String,
        onUpdate: (List<SesiPerangkatModel>) -> Unit
    ): ListenerRegistration? {
        if (userUid.isBlank()) {
            onUpdate(emptyList())
            return null
        }
        val currentDeviceId = getDeviceId(context)

        return try {
            Firebase.firestore.collection(FirestoreCollections.PENGGUNA)
                .document(userUid)
                .collection("sesiPerangkat")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        onUpdate(emptyList())
                        return@addSnapshotListener
                    }

                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(SesiPerangkatModel::class.java)
                    }.filter { it.id != currentDeviceId }

                    onUpdate(list)
                }
        } catch (_: Exception) {
            onUpdate(emptyList())
            null
        }
    }

    fun logoutOtherDevices(context: Context, userUid: String, onComplete: (Boolean) -> Unit) {
        if (userUid.isBlank()) {
            onComplete(false)
            return
        }
        val currentDeviceId = getDeviceId(context)

        try {
            Firebase.firestore.collection(FirestoreCollections.PENGGUNA)
                .document(userUid)
                .collection("sesiPerangkat")
                .get()
                .addOnSuccessListener { snapshot ->
                    val batch = Firebase.firestore.batch()
                    var count = 0

                    snapshot.documents.forEach { doc ->
                        if (doc.id != currentDeviceId) {
                            batch.update(doc.reference, "aktif", false, "terakhirAktif", System.currentTimeMillis())
                            count++
                        }
                    }

                    if (count > 0) {
                        batch.commit().addOnCompleteListener { task ->
                            onComplete(task.isSuccessful)
                        }
                    } else {
                        onComplete(true)
                    }
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } catch (_: Exception) {
            onComplete(false)
        }
    }
}
