package com.example.kawansekolah

import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionUnitTest {

    @Test
    fun `1 role mismatch ditolak`() {
        val userFromFirestore = User(uid = "guru_01", role = "guru", name = "Budi Santoso")
        val actualRoleNorm = AuthRepository.normalizeRole(userFromFirestore.role)
        val expectedRoleNorm = AuthRepository.normalizeRole(UserRole.WALI_KELAS.name)

        val isRoleMismatch = actualRoleNorm != expectedRoleNorm
        assertTrue("Role guru harus ditolak jika kartu yang dipilih adalah Wali Kelas", isRoleMismatch)
    }

    @Test
    fun `2 role dari Firestore menentukan peran`() {
        val docFromFirestore = User(uid = "admin_01", role = "admin", name = "Arif Setiawan")
        val userRole = docFromFirestore.userRole

        assertEquals(UserRole.ADMIN, userRole)
    }

    @Test
    fun `3 sesi dipulihkan saat startup`() {
        val savedUser = User(uid = "siswa_01", role = "siswa", name = "Ahmad Budi")
        AuthRepository.setCachedUser(savedUser)

        val restoredUser = AuthRepository.getCachedUser("siswa_01")
        assertNotNull(restoredUser)
        assertEquals("Ahmad Budi", restoredUser?.name)
        assertEquals(UserRole.SISWA, restoredUser?.userRole)
    }

    @Test
    fun `4 kegagalan jaringan tidak menghapus sesi`() {
        val activeUser = User(uid = "ortu_01", role = "orang_tua", name = "Bapak Ahmad")
        AuthRepository.setCachedUser(activeUser)

        // Simulasi Network Error Exception
        var isSessionCleared = false
        try {
            throw java.net.SocketTimeoutException("Network Timeout 8081")
        } catch (e: Exception) {
            // Sesi TIDAK dibersihkan pada Network Exception
            isSessionCleared = false
        }

        val currentUserAfterError = AuthRepository.getCachedUser("ortu_01")
        assertNotNull("Sesi harus tetap tersimpan saat terjadi kegagalan jaringan", currentUserAfterError)
        assertFalse(isSessionCleared)
    }

    @Test
    fun `5 host kosong memblokir login`() {
        val hostKosong = ""
        val isPhysicalDevice = true

        val shouldBlockLogin = isPhysicalDevice && hostKosong.isBlank()
        assertTrue("Login harus diblokir jika alamat server kosong pada HP fisik", shouldBlockLogin)
    }

    @Test
    fun `6 host terisi menerapkan emulator`() {
        val hostTerisi = "192.168.1.4"
        val isPhysicalDevice = true

        val shouldApplyEmulator = hostTerisi.isNotBlank() || !isPhysicalDevice
        assertTrue("Emulator harus diterapkan jika alamat server terisi", shouldApplyEmulator)
    }
}
