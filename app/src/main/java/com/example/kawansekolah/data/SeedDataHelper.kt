package com.example.kawansekolah.data

import android.util.Log
import com.example.kawansekolah.data.model.AiInsightEntity
import com.example.kawansekolah.data.model.GradeEntity
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.model.TopicEntity
import com.example.kawansekolah.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object SeedDataHelper {

    suspend fun seedDatabase() {
        val db = Firebase.firestore

        try {
            Log.d("SeedDataHelper", "Memulai Seed Data ke Firebase Emulator...")

            // 1. Users
            val teacherUid = "guru_smp_01"
            val studentUid = "siswa_12345"
            val parentUid = "orangtua_01"

            val teacherUser = User(
                uid = teacherUid,
                name = "Drs. Budi Raharjo",
                email = "guru@kawansekolah.sch.id",
                role = "TEACHER",
                teacherId = teacherUid
            )
            val studentUser = User(
                uid = studentUid,
                name = "Ahmad Budi",
                email = "12345@kawansekolah.local",
                role = "STUDENT",
                teacherId = teacherUid,
                schoolLevel = "SMP",
                grade = 9,
                className = "9-A"
            )
            val parentUser = User(
                uid = parentUid,
                name = "Siti Rahma (Orang Tua Ahmad)",
                email = "ortu.ahmad@gmail.com",
                role = "PARENT",
                teacherId = teacherUid,
                studentIds = listOf(studentUid)
            )

            db.collection("users").document(teacherUid).set(teacherUser).await()
            db.collection("users").document(studentUid).set(studentUser).await()
            db.collection("users").document(parentUid).set(parentUser).await()

            // 2. Teacher Profile
            val teacherProfile = TeacherProfile(
                teacherId = teacherUid,
                fullName = "Drs. Budi Raharjo",
                schoolName = "SMP Negeri 1 Jakarta",
                schoolLevel = "SMP",
                classesTaught = listOf("9-A", "9-B"),
                profileComplete = true
            )
            db.collection("teaching_profiles").document(teacherUid).set(teacherProfile).await()

            // 3. Subjects & Topics
            val mathSubjectId = "subj_matematika_9"
            val mathSubject = SubjectEntity(
                id = mathSubjectId,
                teacherId = teacherUid,
                name = "Matematika",
                schoolLevel = "SMP",
                grade = 9
            )
            db.collection("subjects").document(mathSubjectId).set(mathSubject).await()

            val topic1 = TopicEntity("top_aritmatika", teacherUid, mathSubjectId, "Aritmatika", 1.0)
            val topic2 = TopicEntity("top_geometri", teacherUid, mathSubjectId, "Geometri", 2.0)
            val topic3 = TopicEntity("top_pitagoras", teacherUid, mathSubjectId, "Pitagoras", 1.5)

            db.collection("topics").document(topic1.id).set(topic1).await()
            db.collection("topics").document(topic2.id).set(topic2).await()
            db.collection("topics").document(topic3.id).set(topic3).await()

            val indoSubjectId = "subj_bindo_9"
            val indoSubject = SubjectEntity(
                id = indoSubjectId,
                teacherId = teacherUid,
                name = "Bahasa Indonesia",
                schoolLevel = "SMP",
                grade = 9
            )
            db.collection("subjects").document(indoSubjectId).set(indoSubject).await()

            val topic4 = TopicEntity("top_lho", teacherUid, indoSubjectId, "Teks LHO", 1.0)
            val topic5 = TopicEntity("top_puisi", teacherUid, indoSubjectId, "Puisi & Majas", 1.0)

            db.collection("topics").document(topic4.id).set(topic4).await()
            db.collection("topics").document(topic5.id).set(topic5).await()

            // 4. Grades
            val g1 = GradeEntity("${studentUid}_${topic1.id}", studentUid, teacherUid, mathSubjectId, "Matematika", topic1.id, topic1.name, 90.0)
            val g2 = GradeEntity("${studentUid}_${topic2.id}", studentUid, teacherUid, mathSubjectId, "Matematika", topic2.id, topic2.name, 90.0)
            val g3 = GradeEntity("${studentUid}_${topic3.id}", studentUid, teacherUid, mathSubjectId, "Matematika", topic3.id, topic3.name, 70.0)

            val g4 = GradeEntity("${studentUid}_${topic4.id}", studentUid, teacherUid, indoSubjectId, "Bahasa Indonesia", topic4.id, topic4.name, 85.0)
            val g5 = GradeEntity("${studentUid}_${topic5.id}", studentUid, teacherUid, indoSubjectId, "Bahasa Indonesia", topic5.id, topic5.name, 95.0)

            db.collection("grades").document(g1.id).set(g1).await()
            db.collection("grades").document(g2.id).set(g2).await()
            db.collection("grades").document(g3.id).set(g3).await()
            db.collection("grades").document(g4.id).set(g4).await()
            db.collection("grades").document(g5.id).set(g5).await()

            // 5. AI Insight
            // Weighted calculation for Math: (90*1.0 + 90*2.0 + 70*1.5) / (1.0 + 2.0 + 1.5) = (90 + 180 + 105) / 4.5 = 375 / 4.5 = 83.33
            val insightId = "${studentUid}_${mathSubjectId}"
            val mathInsight = AiInsightEntity(
                id = insightId,
                studentId = studentUid,
                subjectId = mathSubjectId,
                subjectName = "Matematika",
                finalScore = 83.3,
                weakestTopic = "Pitagoras",
                strongestTopic = "Aritmatika & Geometri",
                recommendation = "Siswa Ahmad Budi menunjukkan penguasaan yang sangat baik pada Aritmatika dan Geometri (skor 90). Namun, pemahaman pada materi Pitagoras masih perlu ditingkatkan (skor 70). Disarankan melakukan latihan tambahan pada rumus segitiga siku-siku dan tripel Pitagoras."
            )
            db.collection("insights").document(insightId).set(mathInsight).await()

            Log.d("SeedDataHelper", "✅ Seed Data berhasil dibuat!")
        } catch (e: Exception) {
            Log.e("SeedDataHelper", "❌ Gagal membuat Seed Data: ${e.message}")
        }
    }
}
