package com.example.kawansekolah

import com.example.kawansekolah.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGuardMatrixUnitTest {

    private val adminRoutes = setOf(
        "admin_beranda", "admin_pengguna", "admin_akademik",
        "admin_penugasan_guru", "admin_profil", "admin_detail_akun",
        "admin_daftar_akun", "admin_impor_csv"
    )

    private val sharedAccountRoutes = setOf(
        "ubah_kata_sandi", "privasi_notifikasi", "perangkat_sesi"
    )

    fun isRouteAllowedForRole(role: UserRole, route: String): Boolean {
        if (sharedAccountRoutes.contains(route)) return true

        val isAdminRoute = adminRoutes.contains(route)

        return when (role) {
            UserRole.ADMIN -> isAdminRoute
            UserRole.GURU -> !isAdminRoute && route in setOf("beranda", "nilai", "insight", "profil", "guru_input", "unggah_csv", "guru_dashboard")
            UserRole.WALI_KELAS -> !isAdminRoute && route in setOf("beranda", "nilai", "insight", "profil", "rekap_wali", "statistik", "insight_page")
            UserRole.SISWA -> !isAdminRoute && route in setOf("beranda", "nilai", "ai", "profil", "pelajaran")
            UserRole.ORANG_TUA -> !isAdminRoute && route in setOf("beranda", "nilai", "ai", "profil", "pelajaran")
        }
    }

    @Test
    fun `1 Matrix Test - ADMIN Access`() {
        assertTrue(isRouteAllowedForRole(UserRole.ADMIN, "admin_beranda"))
        assertTrue(isRouteAllowedForRole(UserRole.ADMIN, "admin_pengguna"))
        assertFalse(isRouteAllowedForRole(UserRole.ADMIN, "beranda"))
        assertFalse(isRouteAllowedForRole(UserRole.ADMIN, "guru_input"))
    }

    @Test
    fun `2 Matrix Test - GURU Access`() {
        assertTrue(isRouteAllowedForRole(UserRole.GURU, "guru_input"))
        assertTrue(isRouteAllowedForRole(UserRole.GURU, "unggah_csv"))
        assertFalse(isRouteAllowedForRole(UserRole.GURU, "admin_beranda"))
        assertFalse(isRouteAllowedForRole(UserRole.GURU, "rekap_wali"))
    }

    @Test
    fun `3 Matrix Test - WALI_KELAS Access`() {
        assertTrue(isRouteAllowedForRole(UserRole.WALI_KELAS, "rekap_wali"))
        assertTrue(isRouteAllowedForRole(UserRole.WALI_KELAS, "statistik"))
        assertFalse(isRouteAllowedForRole(UserRole.WALI_KELAS, "admin_beranda"))
        assertFalse(isRouteAllowedForRole(UserRole.WALI_KELAS, "guru_input"))
    }

    @Test
    fun `4 Matrix Test - SISWA Access`() {
        assertTrue(isRouteAllowedForRole(UserRole.SISWA, "beranda"))
        assertTrue(isRouteAllowedForRole(UserRole.SISWA, "ai"))
        assertFalse(isRouteAllowedForRole(UserRole.SISWA, "admin_beranda"))
        assertFalse(isRouteAllowedForRole(UserRole.SISWA, "guru_input"))
    }

    @Test
    fun `5 Matrix Test - ORANG_TUA Access`() {
        assertTrue(isRouteAllowedForRole(UserRole.ORANG_TUA, "beranda"))
        assertTrue(isRouteAllowedForRole(UserRole.ORANG_TUA, "ai"))
        assertFalse(isRouteAllowedForRole(UserRole.ORANG_TUA, "admin_beranda"))
        assertFalse(isRouteAllowedForRole(UserRole.ORANG_TUA, "rekap_wali"))
    }
}
