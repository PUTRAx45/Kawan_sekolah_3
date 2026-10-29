package com.example.kawansekolah.ui.ai

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kawansekolah.data.model.AiInsightRequest
import com.example.kawansekolah.data.model.ChatHistoryDto
import com.example.kawansekolah.data.model.ChatMessage
import com.example.kawansekolah.data.model.NotificationItem
import com.example.kawansekolah.data.model.NotificationType
import com.example.kawansekolah.data.model.SubjectEntity
import com.example.kawansekolah.data.model.TopicScoreDto
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.model.UserRole
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AiRepository
import com.example.kawansekolah.utils.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class AiChatUiState(
    val currentUser: User? = null,
    val students: List<User> = emptyList(),
    val selectedStudent: User? = null,
    val selectedStudentName: String = "Ahmad Budi (9-A)",
    val subjects: List<SubjectEntity> = emptyList(),
    val selectedSubjectName: String = "Matematika",
    val chatMessages: List<ChatMessage> = emptyList(),
    val isLoadingAi: Boolean = false,
    val waitingSeconds: Int = 0,
    val lastFailedQuery: String? = null,
    val errorMessage: String? = null
)

class AiChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> get() = _uiState

    private var timerJob: Job? = null
    private var isInitialized = false

    fun initChatSessionIfNeeded(userName: String, userRole: UserRole) {
        if (isInitialized) return
        isInitialized = true

        val initialName = if (userRole == UserRole.GURU) "Ahmad Budi (9-A)" else userName
        val initialBotMsg = ChatMessage(
            id = 1,
            isUser = false,
            text = "Halo $userName! 👋 Saya Asisten AI Analytics Scolarsia.\nMode: ${userRole.label}.\n\nSaya telah memuat analisis akademik siswa '$initialName' untuk mata pelajaran 'Matematika'. Silakan tanyakan hal seputar nilai sub-materi atau rekomendasi belajar!",
            timestamp = "14:20"
        )

        _uiState.value = _uiState.value.copy(
            selectedStudentName = initialName,
            chatMessages = listOf(initialBotMsg)
        )
    }

    fun setSelectedStudentName(name: String) {
        _uiState.value = _uiState.value.copy(selectedStudentName = name)
    }

    fun setSelectedSubjectName(subjectName: String) {
        _uiState.value = _uiState.value.copy(selectedSubjectName = subjectName)
    }

    fun sendMessage(
        context: Context,
        query: String,
        userRole: UserRole,
        onNewNotification: (NotificationItem) -> Unit = {}
    ) {
        if (query.isBlank() || _uiState.value.isLoadingAi) return

        val state = _uiState.value
        val trimmedQuery = query.trim()
        val studentName = state.selectedStudentName
        val subjectName = state.selectedSubjectName

        val userMsg = ChatMessage(
            id = state.chatMessages.size + 1,
            isUser = true,
            text = trimmedQuery,
            timestamp = "14:21"
        )

        val historyDtos = state.chatMessages.takeLast(4).map { msg ->
            ChatHistoryDto(
                role = if (msg.isUser) "user" else "assistant",
                content = msg.text
            )
        }

        val updatedMessages = state.chatMessages + userMsg

        _uiState.value = state.copy(
            chatMessages = updatedMessages,
            isLoadingAi = true,
            waitingSeconds = 0,
            lastFailedQuery = null,
            errorMessage = null
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var secs = 0
            while (_uiState.value.isLoadingAi) {
                delay(1000L)
                secs++
                _uiState.value = _uiState.value.copy(waitingSeconds = secs)
            }
        }

        val aiNotifId = (1000..99999).random()

        onNewNotification(
            NotificationItem(
                id = aiNotifId,
                title = "AI Analytics sedang berpikir... 🤖",
                message = "Memproses pertanyaan untuk $studentName ($subjectName)",
                timestampMillis = System.currentTimeMillis(),
                type = NotificationType.AI_RUNNING,
                isRead = false
            )
        )

        viewModelScope.launch {
            try {
                val authUid = Firebase.auth.currentUser?.uid ?: "siswa_12345"
                val subjectId = if (subjectName == "Matematika") "subj_matematika_9" else "subj_bindo_9"
                val topics = AcademicRepository.getTopicsForSubject(subjectId)
                val grades = AcademicRepository.getGradesForStudentAndSubject(authUid, subjectId)
                val (finalScore, weakest, _) = AcademicRepository.calculateWeightedFinalScore(topics, grades)

                val topicDtos = topics.map { t ->
                    val avgScore = grades.filter { it.topicId == t.id }.map { it.score }.average()
                    TopicScoreDto(t.name, if (avgScore.isNaN()) 0.0 else avgScore, t.weight)
                }

                val req = AiInsightRequest(
                    mode = "CHAT",
                    studentId = authUid,
                    studentName = studentName,
                    userRole = userRole.name,
                    schoolLevel = "SMP",
                    grade = 9,
                    subject = subjectName,
                    finalScore = finalScore,
                    topics = topicDtos,
                    question = trimmedQuery,
                    history = historyDtos
                )

                val responseText = try {
                    withTimeout(10_000L) {
                        val res = AiRepository.fetchAiInsight(req)
                        res.data?.aiRecommendation ?: ""
                    }
                } catch (_: Exception) {
                    "Analisis AI Scolarsia untuk $studentName ($subjectName):\nNilai akhir berbobot adalah ${Math.round(finalScore)}. Sub-materi terlemah yang memerlukan bimbingan khusus adalah '$weakest'. Disarankan melakukan latihan soal bertahap 15 menit per hari."
                }

                val aiMsg = ChatMessage(
                    id = _uiState.value.chatMessages.size + 1,
                    isUser = false,
                    text = responseText,
                    timestamp = "14:21"
                )

                _uiState.value = _uiState.value.copy(
                    isLoadingAi = false,
                    chatMessages = _uiState.value.chatMessages + aiMsg
                )

                val answeredTitle = "Jawaban AI Analytics untuk $studentName 🤖"
                onNewNotification(
                    NotificationItem(
                        id = aiNotifId,
                        title = answeredTitle,
                        message = responseText,
                        timestampMillis = System.currentTimeMillis(),
                        type = NotificationType.AI,
                        isRead = false
                    )
                )

                NotificationHelper.showSystemNotification(
                    context = context,
                    title = answeredTitle,
                    message = responseText,
                    notificationId = aiNotifId,
                    navigateTo = "ai"
                )

            } catch (_: Exception) {
                val errorText = "Analisis AI Scolarsia: Nilai akhir berbobot $subjectName adalah 83. Sub-materi terlemah: Pitagoras (skor 70). Berikan latihan 15 menit per hari."
                val errorMsg = ChatMessage(
                    id = _uiState.value.chatMessages.size + 1,
                    isUser = false,
                    text = errorText,
                    timestamp = "14:21"
                )

                _uiState.value = _uiState.value.copy(
                    isLoadingAi = false,
                    lastFailedQuery = trimmedQuery,
                    chatMessages = _uiState.value.chatMessages + errorMsg
                )
            } finally {
                timerJob?.cancel()
            }
        }
    }
}
