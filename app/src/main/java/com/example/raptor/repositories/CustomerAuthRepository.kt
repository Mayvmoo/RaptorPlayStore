package com.example.raptor.repositories

import com.example.raptor.models.CustomerSession
import com.example.raptor.models.CustomerType
import com.example.raptor.network.NetworkModule
import com.example.raptor.network.RaptorApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * Customer Authentication Repository
 * Komt overeen met iOS CustomerAuthService
 */
class CustomerAuthRepository {
    
    private val apiService: RaptorApiService = NetworkModule.apiService
    
    /**
     * Authenticate customer
     * Komt overeen met iOS: CustomerAuthService.authenticate
     */
    suspend fun authenticate(email: String, password: String): Result<CustomerSession> {
        return try {
            val response = apiService.authenticateCustomer(
                com.example.raptor.network.CustomerAuthRequest(email, password)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    val session = apiResponse.data!!.toCustomerSession()
                    Result.success(session)
                } else {
                    Result.failure(
                        AuthException(apiResponse.error ?: "Authenticatie mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(AuthException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(AuthException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(AuthException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(AuthException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Register customer
     * Komt overeen met iOS: CustomerAuthService.register
     */
    suspend fun register(
        email: String,
        password: String,
        customerType: CustomerType,
        companyName: String?,
        contactName: String,
        phoneNumber: String?,
        address: String?
    ): Result<CustomerSession> {
        return try {
            val response = apiService.registerCustomer(
                com.example.raptor.network.CustomerRegisterRequest(
                    action = "register",
                    email = email,
                    password = password,
                    customerType = customerType.value,
                    companyName = companyName,
                    contactName = contactName,
                    phoneNumber = phoneNumber,
                    address = address
                )
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    val session = apiResponse.data!!.toCustomerSession()
                    Result.success(session)
                } else {
                    Result.failure(
                        AuthException(apiResponse.error ?: "Registratie mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(AuthException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(AuthException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(AuthException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(AuthException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Request password reset
     * Komt overeen met iOS: CustomerAuthService.requestPasswordReset
     */
    suspend fun requestPasswordReset(email: String): Result<Unit> {
        return try {
            val response = apiService.requestPasswordReset(
                com.example.raptor.network.PasswordResetRequest(email)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        AuthException(apiResponse.error ?: "Password reset mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(AuthException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(AuthException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(AuthException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(AuthException("Onbekende fout: ${e.message}"))
        }
    }
}

/**
 * Custom exception voor authentication errors
 */
class AuthException(message: String) : Exception(message)

