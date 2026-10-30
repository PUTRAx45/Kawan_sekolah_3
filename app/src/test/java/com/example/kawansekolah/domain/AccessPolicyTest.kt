package com.example.kawansekolah.domain

import org.junit.Assert.*
import org.junit.Test

class AccessPolicyTest {

    @Test
    fun `canInputScore allows only assigned subject teacher`() {
        // Assigned Guru Mapel
        assertTrue(AccessPolicy.canInputScore("GURU_MAPEL", "guru_smp_01", "guru_smp_01"))

        // Unassigned / other subject teacher
        assertFalse(AccessPolicy.canInputScore("GURU_MAPEL", "guru_smp_02", "guru_smp_01"))

        // Wali Kelas (not allowed to input subject scores)
        assertFalse(AccessPolicy.canInputScore("WALI_KELAS", "guru_wali_01", "guru_smp_01"))

        // Role SISWA rejected
        assertFalse(AccessPolicy.canInputScore("SISWA", "std_1", "guru_smp_01"))

        // Role ORANG_TUA rejected
        assertFalse(AccessPolicy.canInputScore("ORANG_TUA", "ortu_1", "guru_smp_01"))
    }

    @Test
    fun `canViewClassScores allows Wali Kelas for their homeroom class`() {
        // Wali Kelas viewing their homeroom class
        assertTrue(AccessPolicy.canViewClassScores("WALI_KELAS", "guru_wali_01", "guru_wali_01", "guru_smp_01"))

        // Other Wali Kelas trying to view another homeroom class
        assertFalse(AccessPolicy.canViewClassScores("WALI_KELAS", "guru_wali_02", "guru_wali_01", "guru_smp_01"))

        // Guru Mapel viewing assigned subject scores
        assertTrue(AccessPolicy.canViewClassScores("GURU_MAPEL", "guru_smp_01", "guru_wali_01", "guru_smp_01"))

        // Other subject teacher trying to view unassigned subject
        assertFalse(AccessPolicy.canViewClassScores("GURU_MAPEL", "guru_smp_02", "guru_wali_01", "guru_smp_01"))

        // Role SISWA rejected for class scores
        assertFalse(AccessPolicy.canViewClassScores("SISWA", "std_1", "guru_wali_01", "guru_smp_01"))

        // Role ORANG_TUA rejected for class scores
        assertFalse(AccessPolicy.canViewClassScores("ORANG_TUA", "ortu_1", "guru_wali_01", "guru_smp_01"))
    }
}
