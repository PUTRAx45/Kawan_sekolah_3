package com.example.kawansekolah.ui.nilai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kawansekolah.data.model.AiInsightEntity
import com.example.kawansekolah.data.model.AiInsightRequest
import com.example.kawansekolah.data.model.GradeEntity
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.data.model.SubjectWithDetails
import com.example.kawansekolah.data.model.TopicEntity
import com.example.kawansekolah.data.model.TopicScoreDto
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AiRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

object SubjectSyncManager {
    val selectedSubjectName = MutableStateFlow("Matematika")
    val selectedSubjectId = MutableStateFlow("subj_matematika_9")
}

data class AcademicUiState(
    val isLoading: Boolean = false,
    val subjects: List<SubjectEntity> = emptyList(),
    val subjectDetailsList: List<SubjectWithDetails> = emptyList(),
    val currentSubject: SubjectEntity? = null,
    val selectedSubjectId: String? = "subj_matematika_9",
    val selectedSubjectName: String = "Matematika",
    val topics: List<TopicEntity> = emptyList(),
    val grades: List<GradeEntity> = emptyList(),
    val currentInsight: AiInsightEntity? = null,
    val isAnalyzingAi: Boolean = false,
    val errorMessage: String? = null
)

class AcademicViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicUiState())
    val uiState: StateFlow<AcademicUiState> get() = _uiState

    init {
        val authUid = Firebase.auth.currentUser?.uid ?: "siswa_12345"
        loadAllAcademicData(authUid)
    }

    fun loadAllAcademicData(studentId: String = "siswa_12345") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val authUid = Firebase.auth.currentUser?.uid
            val targetStudentId = if (studentId == "siswa_12345" && !authUid.isNullOrBlank()) authUid else studentId

            try {
                // Timeout 8 detik per pengambilan data
                withTimeout(8_000L) {
                    val subjectEntities = AcademicRepository.getAllSubjects()

                    val detailsList = coroutineScope {
                        val deferreds = subjectEntities.map { subject ->
                            async {
                                val topicsDeferred = async { AcademicRepository.getTopicsForSubject(subject.id) }
                                val gradesDeferred = async {
                                    if (targetStudentId.isNotBlank()) AcademicRepository.getGradesForStudentAndSubject(targetStudentId, subject.id)
                                    else emptyList()
                                }
                                val insightDeferred = async {
                                    if (targetStudentId.isNotBlank()) AcademicRepository.getAiInsight(targetStudentId, subject.id)
                                    else null
                                }

                                val topics = topicsDeferred.await()
                                val grades = gradesDeferred.await()
                                val insight = insightDeferred.await()

                                val (finalScore, weakest, _) = AcademicRepository.calculateWeightedFinalScore(topics, grades)
                                val hasGrades = grades.isNotEmpty()

                                SubjectWithDetails(
                                    subject = subject,
                                    topics = topics,
                                    grades = grades,
                                    weightedFinalScore = if (hasGrades) finalScore else null,
                                    weakestTopic = if (hasGrades && weakest != "-") weakest else null,
                                    aiInsight = insight
                                )
                            }
                        }
                        deferreds.awaitAll()
                    }

                    val currentSelectedId = SubjectSyncManager.selectedSubjectId.value
                    val selectedDetail = detailsList.firstOrNull { it.subject.id == currentSelectedId } ?: detailsList.firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subjects = subjectEntities,
                        subjectDetailsList = detailsList,
                        selectedSubjectId = selectedDetail?.subject?.id,
                        selectedSubjectName = selectedDetail?.subject?.name ?: "Matematika",
                        currentSubject = selectedDetail?.subject ?: subjectEntities.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                val fallbackList = _uiState.value.subjectDetailsList
                val errorMsg = when (e) {
                    is TimeoutCancellationException -> "Koneksi ke Firestore / server AI melebihi batas waktu 8 detik (timeout)."
                    is FirebaseFirestoreException -> {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            "Izin ditolak (Rules Firebase). Akun Anda tidak berhak membaca dokumen ini."
                        } else {
                            "Gagal membaca Firestore: ${e.message}"
                        }
                    }
                    else -> e.message ?: "Gagal memuat data akademik. Silakan periksa koneksi."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (fallbackList.isEmpty()) errorMsg else null
                )
            }
        }
    }

    fun selectSubjectById(subjectId: String) {
        val detail = _uiState.value.subjectDetailsList.firstOrNull { it.subject.id == subjectId }
        if (detail != null) {
            SubjectSyncManager.selectedSubjectId.value = subjectId
            SubjectSyncManager.selectedSubjectName.value = detail.subject.name
            _uiState.value = _uiState.value.copy(
                selectedSubjectId = subjectId,
                selectedSubjectName = detail.subject.name,
                currentSubject = detail.subject
            )
        }
    }

    fun selectSubjectByName(subjectName: String) {
        val detail = _uiState.value.subjectDetailsList.firstOrNull { it.subject.name.equals(subjectName, ignoreCase = true) }
        if (detail != null) {
            selectSubjectById(detail.subject.id)
        } else {
            SubjectSyncManager.selectedSubjectName.value = subjectName
            _uiState.value = _uiState.value.copy(selectedSubjectName = subjectName)
        }
    }

    fun loadSubjectsForTeacher(teacherId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val list = AcademicRepository.getSubjectsForTeacher(teacherId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                subjects = list,
                currentSubject = list.firstOrNull()
            )
            list.firstOrNull()?.let { loadSubjectDetails(it, "") }
        }
    }

    fun selectSubject(subject: SubjectEntity, studentId: String) {
        selectSubjectById(subject.id)
        loadSubjectDetails(subject, studentId)
    }

    fun loadSubjectDetails(subject: SubjectEntity, studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val authUid = Firebase.auth.currentUser?.uid
            val targetStudentId = if (studentId == "siswa_12345" && !authUid.isNullOrBlank()) authUid else studentId

            val topics = AcademicRepository.getTopicsForSubject(subject.id)
            val grades = if (targetStudentId.isNotBlank()) {
                AcademicRepository.getGradesForStudentAndSubject(targetStudentId, subject.id)
            } else emptyList()
            val insight = if (targetStudentId.isNotBlank()) {
                AcademicRepository.getAiInsight(targetStudentId, subject.id)
            } else null

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                topics = topics,
                grades = grades,
                currentInsight = insight
            )
        }
    }

    fun addSubject(name: String, schoolLevel: String, grade: Int, teacherId: String) {
        viewModelScope.launch {
            try {
                AcademicRepository.addSubject(name, schoolLevel, grade)
                val authUid = Firebase.auth.currentUser?.uid ?: "siswa_12345"
                loadAllAcademicData(authUid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menambah mapel")
            }
        }
    }

    fun addTopic(subjectId: String, name: String, weight: Double) {
        viewModelScope.launch {
            try {
                AcademicRepository.addTopic(subjectId, name, weight)
                val authUid = Firebase.auth.currentUser?.uid ?: "siswa_12345"
                loadAllAcademicData(authUid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Gagal menambah sub-materi")
            }
        }
    }

    fun uploadGradesAndGenerateInsight(
        student: User,
        subject: SubjectEntity,
        topicGrades: List<Pair<TopicEntity, Double>>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isAnalyzingAi = true, errorMessage = null)
            try {
                AcademicRepository.saveStudentGradesBatch(
                    studentId = student.uid,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    gradesInput = topicGrades
                )

                val topics = AcademicRepository.getTopicsForSubject(subject.id)
                val updatedGrades = AcademicRepository.getGradesForStudentAndSubject(student.uid, subject.id)

                val (finalScore, weakestTopic, strongestTopic) = AcademicRepository.calculateWeightedFinalScore(topics, updatedGrades)

                val topicDtos = topicGrades.map { (t, s) -> TopicScoreDto(t.name, s, t.weight) }
                val aiReq = AiInsightRequest(
                    mode = "AUTO_INSIGHT",
                    studentId = student.uid,
                    studentName = student.name,
                    schoolLevel = student.schoolLevel,
                    grade = student.grade,
                    subject = subject.name,
                    finalScore = finalScore,
                    topics = topicDtos
                )

                val aiRes = AiRepository.fetchAiInsight(aiReq)
                val recommendation = aiRes.data?.aiRecommendation ?: "Siswa ${student.name} perlu lebih konsisten mempelajari sub-materi terlemah $weakestTopic."

                val newInsight = AiInsightEntity(
                    studentId = student.uid,
                    subjectId = subject.id,
                    subjectName = subject.name,
                    finalScore = finalScore,
                    weakestTopic = aiRes.data?.lowestTopic ?: weakestTopic,
                    strongestTopic = aiRes.data?.highestTopic ?: strongestTopic,
                    recommendation = recommendation,
                    generatedAt = System.currentTimeMillis()
                )

                AcademicRepository.saveAiInsight(newInsight)

                loadAllAcademicData(student.uid)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAnalyzingAi = false,
                    errorMessage = "Gagal menyimpan nilai / analisis AI: ${e.message}"
                )
            }
        }
    }
}
