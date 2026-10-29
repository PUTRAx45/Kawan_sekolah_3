package com.example.kawansekolah.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kawansekolah.data.model.User
import com.example.kawansekolah.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> get() = _uiState

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email/NIS dan kata sandi tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = AuthRepository.loginWithEmailOrNis(identifier, pass)
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Gagal masuk. Periksa kembali akun Anda.")
            }
        }
    }

    fun registerTeacher(name: String, email: String, pass: String, inviteCode: String) {
        if (name.isBlank() || email.isBlank() || pass.isBlank() || inviteCode.isBlank()) {
            _uiState.value = AuthUiState.Error("Semua bidang dan Kode Undangan wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val teacher = AuthRepository.registerTeacher(name, email, pass, inviteCode)
                _uiState.value = AuthUiState.Success(teacher)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Gagal mendaftarkan akun Guru")
            }
        }
    }

    fun createStudentAccount(
        context: Context,
        name: String,
        nis: String,
        pass: String,
        schoolLevel: String,
        grade: Int,
        className: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val student = AuthRepository.createStudentAccount(context, name, nis, pass, schoolLevel, grade, className)
                onSuccess(student)
            } catch (e: Exception) {
                onError(e.message ?: "Gagal membuat akun Siswa")
            }
        }
    }

    fun createParentAccount(
        context: Context,
        name: String,
        email: String,
        pass: String,
        linkedStudentIds: List<String>,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val parent = AuthRepository.createParentAccount(context, name, email, pass, linkedStudentIds)
                onSuccess(parent)
            } catch (e: Exception) {
                onError(e.message ?: "Gagal membuat akun Orang Tua")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
