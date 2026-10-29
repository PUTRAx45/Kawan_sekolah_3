package com.example.kawansekolah.ui.guru

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kawansekolah.data.model.TeacherProfile
import com.example.kawansekolah.data.repository.AcademicRepository
import com.example.kawansekolah.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val profile: TeacherProfile?) : ProfileUiState()
    object Saving : ProfileUiState()
    object SaveSuccess : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class TeacherProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> get() = _uiState

    fun loadProfile() {
        val uid = AuthRepository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = AcademicRepository.getTeacherProfile(uid)
                _uiState.value = ProfileUiState.Loaded(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Gagal memuat profil")
            }
        }
    }

    fun saveProfile(fullName: String, schoolName: String, schoolLevel: String, classesTaught: List<String>) {
        if (fullName.isBlank() || schoolName.isBlank() || classesTaught.isEmpty()) {
            _uiState.value = ProfileUiState.Error("Semua bidang dan minimal 1 kelas wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Saving
            try {
                val profile = TeacherProfile(
                    fullName = fullName,
                    schoolName = schoolName,
                    schoolLevel = schoolLevel,
                    classesTaught = classesTaught,
                    profileComplete = true
                )
                AcademicRepository.saveTeacherProfile(profile)
                _uiState.value = ProfileUiState.SaveSuccess
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Gagal menyimpan profil")
            }
        }
    }
}
