package com.project.beam.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.beam.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val context: Context) : ViewModel() {

    private val repository = AuthRepository(context)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun googleLogin(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "idToken 전송: $idToken")
            repository.googleLogin(idToken).fold(
                onSuccess = {
                    Log.d("AuthViewModel", "로그인 성공")
                    _authState.value = AuthState.Success
                },
                onFailure = {
                    Log.e("AuthViewModel", "로그인 실패: ${it.message}")
                    _authState.value = AuthState.Error(it.message ?: "로그인 실패")
                }
            )
        }
    }
}