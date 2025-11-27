package com.example.raptor.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raptor.models.CustomerSession
import com.example.raptor.models.CustomerType
import com.example.raptor.repositories.AuthException
import com.example.raptor.repositories.CustomerAuthRepository
import kotlinx.coroutines.launch

/**
 * Customer Authentication ViewModel
 * Komt overeen met iOS CustomerAuthService functionaliteit
 */
class CustomerAuthViewModel : ViewModel() {
    
    private val authRepository = CustomerAuthRepository()
    
    // LiveData voor UI updates
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState
    
    private val _currentSession = MutableLiveData<CustomerSession?>()
    val currentSession: LiveData<CustomerSession?> = _currentSession
    
    /**
     * Authenticate customer (login)
     */
    fun authenticate(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.authenticate(email, password)
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _authState.value = AuthState.Success(result.getOrNull())
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is AuthException -> error.message
                    else -> "Authenticatie mislukt: ${error?.message}"
                }
                _authState.value = AuthState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Register new customer
     */
    fun register(
        email: String,
        password: String,
        customerType: CustomerType,
        companyName: String?,
        contactName: String,
        phoneNumber: String?,
        address: String?
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.register(
                email, password, customerType, companyName, contactName, phoneNumber, address
            )
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _authState.value = AuthState.Success(result.getOrNull())
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is AuthException -> error.message
                    else -> "Registratie mislukt: ${error?.message}"
                }
                _authState.value = AuthState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Request password reset
     */
    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.requestPasswordReset(email)
            if (result.isSuccess) {
                _authState.value = AuthState.PasswordResetSent
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is AuthException -> error.message
                    else -> "Password reset mislukt: ${error?.message}"
                }
                _authState.value = AuthState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Logout
     */
    fun logout() {
        _currentSession.value = null
        _authState.value = AuthState.Idle
    }
}

/**
 * Authentication State
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val session: CustomerSession?) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}

