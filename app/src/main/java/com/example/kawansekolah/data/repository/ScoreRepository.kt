package com.example.kawansekolah.data.repository

import com.example.kawansekolah.data.model.Kelas
import com.example.kawansekolah.data.model.Kompetensi
import com.example.kawansekolah.data.model.Penugasan
import com.example.kawansekolah.data.model.Periode
import com.example.kawansekolah.data.model.Score
import com.example.kawansekolah.data.model.Student
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.domain.AccessPolicy
import com.example.kawansekolah.domain.ScoreValidator
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object ScoreRepository {

    private val firestore by lazy { Firebase.firestore }

    var isDemoMode: Boolean = true

    private val localScores = mutableListOf<Score>()
    private val localPenugasan = mutableListOf<Penugasan>()

    init {
        localScores.addAll(
            listOf(
                Score("sc_1", "std_1", "Ahmad Budi", "guru_smp_01", "subj_matematika_9", "cls_9a", "komp_1", "per_1", "KUIS_HARIAN", 85.0, "Bagus"),
                Score("sc_2", "std_2", "Siti Rahma", "guru_smp_01", "subj_matematika_9", "cls_9a", "komp_1", "per_1", "KUIS_HARIAN", 92.0, "Sangat baik"),
                Score("sc_3", "std_3", "Dewi Lestari", "guru_smp_01", "subj_matematika_9", "cls_9a", "komp_1", "per_1", "KUIS_HARIAN", 78.0, "Cukup"),
                Score("sc_4", "std_1", "Ahmad Budi", "guru_smp_01", "subj_matematika_9", "cls_9a", "komp_2", "per_1", "KARAKTER", 90.0, "Disiplin dan aktif"),
                Score("sc_5", "std_2", "Siti Rahma", "guru_smp_01", "subj_matematika_9", "cls_9a", "komp_2", "per_1", "KARAKTER", 88.0, "Kerja sama baik")
            )
        )

        // Dummy Penugasan: Satu guru HANYA satu mapel
        localPenugasan.addAll(
            listOf(
                Penugasan("guru_smp_01_subj_matematika_9_cls_9a", "guru_smp_01", "subj_matematika_9", "cls_9a"),
                Penugasan("guru_smp_03_subj_bindo_9_cls_9a", "guru_smp_03", "subj_bindo_9", "cls_9a"),
                Penugasan("guru_smp_02_subj_ipa_9_cls_9b", "guru_smp_02", "subj_ipa_9", "cls_9b")
            )
        )
    }

    suspend fun saveScore(
        userRole: String,
        userId: String,
        assignedTeacherId: String,
        score: Score
    ): Result<String> {
        if (!AccessPolicy.canInputScore(userRole, userId, assignedTeacherId)) {
            return Result.failure(IllegalStateException("Akses ditolak: Anda tidak memiliki wewenang menginput nilai pada mata pelajaran ini."))
        }

        if (!ScoreValidator.validateScore(score.scoreValue)) {
            return Result.failure(IllegalArgumentException("Format/Rentang nilai ${score.scoreValue} tidak valid (harus 0.0 - 100.0)."))
        }

        return try {
            val docRef = if (score.id.isBlank()) firestore.collection("scores").document() else firestore.collection("scores").document(score.id)
            val scoreToSave = score.copy(id = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(scoreToSave).await()
            localScores.removeAll { it.id == scoreToSave.id }
            localScores.add(scoreToSave)
            Result.success(docRef.id)
        } catch (e: Exception) {
            if (isDemoMode) {
                val scoreId = if (score.id.isBlank()) "sc_${System.currentTimeMillis()}" else score.id
                val scoreToSave = score.copy(id = scoreId, timestamp = System.currentTimeMillis())
                localScores.removeAll { it.id == scoreToSave.id }
                localScores.add(scoreToSave)
                Result.success(scoreId)
            } else {
                Result.failure(IllegalStateException("Gagal menyimpan nilai ke Firestore: ${e.message}", e))
            }
        }
    }

    suspend fun saveScoresBatch(
        userRole: String,
        userId: String,
        assignedTeacherId: String,
        scores: List<Score>
    ): Result<Int> {
        if (!AccessPolicy.canUploadCsv(userRole, userId, assignedTeacherId)) {
            return Result.failure(IllegalStateException("Akses ditolak: Hanya Guru Mapel bersangkutan yang dapat mengunggah file nilai."))
        }

        val invalidScores = scores.filter { !ScoreValidator.validateScore(it.scoreValue) }
        if (invalidScores.isNotEmpty()) {
            return Result.failure(IllegalArgumentException("Terdapat ${invalidScores.size} baris nilai di luar rentang 0.0 - 100.0."))
        }

        var savedCount = 0
        for (score in scores) {
            val res = saveScore(userRole, userId, assignedTeacherId, score)
            if (res.isSuccess) {
                savedCount++
            } else {
                return Result.failure(res.exceptionOrNull() ?: IllegalStateException("Gagal menyimpan batch nilai."))
            }
        }
        return Result.success(savedCount)
    }

    suspend fun getScoresForClassAndSubject(
        userRole: String,
        userId: String,
        classId: String,
        subjectId: String,
        classHomeroomTeacherId: String,
        assignedTeacherId: String
    ): List<Score> {
        if (!AccessPolicy.canViewClassScores(userRole, userId, classHomeroomTeacherId, assignedTeacherId)) {
            return emptyList()
        }

        return try {
            val snapshot = firestore.collection("scores")
                .whereEqualTo("classId", classId)
                .whereEqualTo("subjectId", subjectId)
                .get()
                .await()
            val list = snapshot.toObjects(Score::class.java)
            if (list.isNotEmpty()) list else if (isDemoMode) localScores.filter { it.classId == classId && it.subjectId == subjectId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localScores.filter { it.classId == classId && it.subjectId == subjectId } else emptyList()
        }
    }

    suspend fun getScoresForClass(
        userRole: String,
        userId: String,
        classId: String,
        classHomeroomTeacherId: String
    ): List<Score> {
        if (AccessPolicy.isWaliKelas(userRole)) {
            if (userId.isBlank() || userId != classHomeroomTeacherId) {
                return emptyList()
            }
        } else if (AccessPolicy.isGuruMapel(userRole)) {
            val assignedSubjectIds = getAssignedSubjectIdsForTeacherAndClass(userId, classId)
            if (assignedSubjectIds.isEmpty()) {
                return emptyList()
            }
            val allClassScores = fetchClassScoresInternal(classId)
            return allClassScores.filter { it.subjectId in assignedSubjectIds }
        } else {
            return emptyList()
        }

        return fetchClassScoresInternal(classId)
    }

    private suspend fun fetchClassScoresInternal(classId: String): List<Score> {
        return try {
            val snapshot = firestore.collection("scores")
                .whereEqualTo("classId", classId)
                .get()
                .await()
            val list = snapshot.toObjects(Score::class.java)
            if (list.isNotEmpty()) list else if (isDemoMode) localScores.filter { it.classId == classId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localScores.filter { it.classId == classId } else emptyList()
        }
    }

    suspend fun getAssignedSubjectIdsForTeacherAndClass(teacherId: String, classId: String): List<String> {
        return try {
            val snapshot = firestore.collection("penugasan")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("classId", classId)
                .get()
                .await()
            val list = snapshot.toObjects(Penugasan::class.java)
            val ids = list.map { it.subjectId }
            if (ids.isNotEmpty()) ids else if (isDemoMode) localPenugasan.filter { it.teacherId == teacherId && it.classId == classId }.map { it.subjectId } else emptyList()
        } catch (_: Exception) {
            if (isDemoMode) localPenugasan.filter { it.teacherId == teacherId && it.classId == classId }.map { it.subjectId } else emptyList()
        }
    }

    suspend fun getPenugasanForTeacher(teacherId: String): Penugasan? {
        if (teacherId.isBlank()) return null

        return try {
            val snapshot = firestore.collection("penugasan")
                .whereEqualTo("teacherId", teacherId)
                .limit(1)
                .get()
                .await()
            val list = snapshot.toObjects(Penugasan::class.java)
            if (list.isNotEmpty()) list.first() else if (isDemoMode) localPenugasan.firstOrNull { it.teacherId == teacherId } else null
        } catch (_: Exception) {
            if (isDemoMode) localPenugasan.firstOrNull { it.teacherId == teacherId } else null
        }
    }

    // Dummy Helpers
    fun getDummyKelasList(): List<Kelas> {
        return listOf(
            Kelas("cls_9a", "Kelas 9-A", "guru_wali_01", "SMP", 9),
            Kelas("cls_9b", "Kelas 9-B", "guru_wali_02", "SMP", 9)
        )
    }

    fun getDummyStudentsList(classId: String): List<Student> {
        return listOf(
            Student("std_1", "Ahmad Budi", "12345", classId),
            Student("std_2", "Siti Rahma", "12346", classId),
            Student("std_3", "Dewi Lestari", "12347", classId),
            Student("std_4", "Fajar Pratama", "12348", classId)
        )
    }

    fun getDummySubjectsList(): List<SubjectEntity> {
        return listOf(
            SubjectEntity("subj_matematika_9", "guru_smp_01", "Matematika", "SMP", 9),
            SubjectEntity("subj_bindo_9", "guru_smp_03", "Bahasa Indonesia", "SMP", 9)
        )
    }

    fun getDummyKompetensiList(subjectId: String): List<Kompetensi> {
        return listOf(
            Kompetensi("komp_1", subjectId, "KD 3.1", "Aritmatika & Aljabar Dasar"),
            Kompetensi("komp_2", subjectId, "KD 3.2", "Geometri & Pitagoras"),
            Kompetensi("komp_3", subjectId, "KD 4.1", "Sikap & Keaktifan Belajar")
        )
    }

    fun getDummyPeriodeList(): List<Periode> {
        return listOf(
            Periode("per_1", "Semester Ganjil 2025/2026", "Ganjil", "2025/2026"),
            Periode("per_2", "Semester Genap 2025/2026", "Genap", "2025/2026")
        )
    }
}
