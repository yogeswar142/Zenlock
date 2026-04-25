package com.zenlock.focusguard.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zenlock.focusguard.data.datastore.TokenManager
import com.zenlock.focusguard.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val isNewUser: Boolean, val isProfileComplete: Boolean) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _userProfile = MutableStateFlow<UserDto?>(null)
    val userProfile: StateFlow<UserDto?> = _userProfile

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading

    /**
     * Parse the error message from the API response body.
     * Expects JSON format: { "error": "..." }
     */
    private fun parseErrorMessage(errorBody: String?, fallback: String): String {
        return try {
            if (errorBody.isNullOrBlank()) fallback
            else JSONObject(errorBody).optString("error", fallback)
        } catch (e: Exception) {
            fallback
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager.saveToken(
                        authData.token,
                        authData.user.email,
                        authData.user.isProfileComplete
                    )
                    _userProfile.value = authData.user
                    _authState.value = AuthState.Success(
                        authData.isNewUser,
                        authData.user.isProfileComplete
                    )
                } else {
                    val msg = parseErrorMessage(
                        response.errorBody()?.string(),
                        "Login failed"
                    )
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _authState.value = AuthState.Error("Connection failed. Please check your internet.")
            }
        }
    }

    fun signup(request: SignupRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.signup(request)
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager.saveToken(
                        authData.token,
                        authData.user.email,
                        authData.user.isProfileComplete
                    )
                    _userProfile.value = authData.user
                    _authState.value = AuthState.Success(
                        authData.isNewUser,
                        authData.user.isProfileComplete
                    )
                } else {
                    val msg = parseErrorMessage(
                        response.errorBody()?.string(),
                        "Signup failed"
                    )
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Signup error", e)
                _authState.value = AuthState.Error("Connection failed. Please check your internet.")
            }
        }
    }

    fun googleAuth(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.authApi.googleAuth(GoogleAuthRequest(idToken))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!
                    tokenManager.saveToken(
                        authData.token,
                        authData.user.email,
                        authData.user.isProfileComplete
                    )
                    _userProfile.value = authData.user
                    _authState.value = AuthState.Success(
                        authData.isNewUser,
                        authData.user.isProfileComplete
                    )
                } else {
                    val msg = parseErrorMessage(
                        response.errorBody()?.string(),
                        "Google authentication failed"
                    )
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google auth error", e)
                _authState.value = AuthState.Error("Connection failed. Please check your internet.")
            }
        }
    }

    fun setupProfile(request: ProfileSetupRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val token = tokenManager.jwtToken.first()
                if (token == null) {
                    _authState.value = AuthState.Error("Session expired. Please login again.")
                    return@launch
                }

                val response = RetrofitClient.authApi.setupProfile("Bearer $token", request)
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.completeProfile()
                    _userProfile.value = response.body()!!.user
                    _authState.value = AuthState.Success(
                        isNewUser = false,
                        isProfileComplete = true
                    )
                    onComplete()
                } else {
                    val msg = parseErrorMessage(
                        response.errorBody()?.string(),
                        "Profile setup failed"
                    )
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Profile setup error", e)
                _authState.value = AuthState.Error("Connection failed. Please check your internet.")
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isProfileLoading.value = true
            try {
                val token = tokenManager.jwtToken.first()
                if (token == null) {
                    _isProfileLoading.value = false
                    return@launch
                }

                val response = RetrofitClient.authApi.getProfile("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    _userProfile.value = response.body()!!.user
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to load profile", e)
            } finally {
                _isProfileLoading.value = false
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            tokenManager.clearToken()
            _userProfile.value = null
            _authState.value = AuthState.Idle
            onLogout()
        }
    }
}
