package com.example.raptor.repositories

import com.example.raptor.models.ChatMessage
import com.example.raptor.network.NetworkModule
import com.example.raptor.network.RaptorApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * Chat Repository
 * Komt overeen met iOS chat functionaliteit
 */
class ChatRepository {
    
    private val apiService: RaptorApiService = NetworkModule.apiService
    
    /**
     * Get chat messages for an order
     */
    suspend fun getMessages(
        orderId: String,
        customerEmail: String? = null,
        driverEmail: String? = null
    ): Result<List<ChatMessage>> {
        return try {
            val response = apiService.getChatMessages(orderId, customerEmail, driverEmail)
            
            if (response.isSuccessful && response.body() != null) {
                val messages = response.body()!!
                Result.success(messages)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(ChatException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(ChatException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(ChatException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ChatException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Send chat message
     */
    suspend fun sendMessage(
        orderId: String,
        senderEmail: String,
        body: String
    ): Result<ChatMessage> {
        return try {
            val response = apiService.sendChatMessage(
                com.example.raptor.network.SendChatMessageRequest(orderId, senderEmail, body)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    Result.success(apiResponse.data!!)
                } else {
                    Result.failure(ChatException(apiResponse.error ?: "Bericht verzenden mislukt"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(ChatException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(ChatException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(ChatException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ChatException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Mark messages as read
     */
    suspend fun markAsRead(orderId: String, customerEmail: String? = null, driverEmail: String? = null): Result<Unit> {
        return try {
            val response = apiService.markChatMessagesAsRead(
                com.example.raptor.network.MarkMessagesReadRequest(orderId, customerEmail, driverEmail)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    Result.success(Unit)
                } else {
                    Result.failure(ChatException(apiResponse.error ?: "Markeren als gelezen mislukt"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(ChatException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(ChatException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(ChatException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(ChatException("Onbekende fout: ${e.message}"))
        }
    }
}

class ChatException(message: String) : Exception(message)

