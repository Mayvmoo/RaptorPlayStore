package com.example.raptor.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raptor.models.ChatMessage
import com.example.raptor.repositories.ChatException
import com.example.raptor.repositories.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Chat ViewModel
 * Komt overeen met iOS CustomerOrderChatView functionaliteit
 */
class ChatViewModel : ViewModel() {
    
    private val chatRepository = ChatRepository()
    
    private var pollingJob: Job? = null
    private var orderId: String? = null
    private var customerEmail: String? = null
    private var driverEmail: String? = null
    
    // LiveData voor UI updates
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages
    
    private val _chatState = MutableLiveData<ChatState>()
    val chatState: LiveData<ChatState> = _chatState
    
    /**
     * Initialize chat for an order
     */
    fun initializeChat(
        orderId: String,
        customerEmail: String? = null,
        driverEmail: String? = null
    ) {
        this.orderId = orderId
        this.customerEmail = customerEmail
        this.driverEmail = driverEmail
        
        loadMessages()
        startPolling()
    }
    
    /**
     * Load messages
     */
    fun loadMessages() {
        val currentOrderId = orderId ?: return
        
        viewModelScope.launch {
            _chatState.value = ChatState.Loading
            
            val result = chatRepository.getMessages(currentOrderId, customerEmail, driverEmail)
            if (result.isSuccess) {
                val messagesList = result.getOrNull() ?: emptyList()
                _messages.value = messagesList
                _chatState.value = ChatState.Success
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is ChatException -> error.message
                    else -> "Berichten ophalen mislukt: ${error?.message}"
                }
                _chatState.value = ChatState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Send message
     */
    fun sendMessage(body: String) {
        val currentOrderId = orderId ?: return
        val senderEmail = customerEmail ?: driverEmail ?: return
        
        if (body.trim().isEmpty()) return
        
        viewModelScope.launch {
            _chatState.value = ChatState.Sending
            
            val result = chatRepository.sendMessage(currentOrderId, senderEmail, body.trim())
            if (result.isSuccess) {
                // Reload messages to get the new one
                loadMessages()
                _chatState.value = ChatState.Success
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is ChatException -> error.message
                    else -> "Bericht verzenden mislukt: ${error?.message}"
                }
                _chatState.value = ChatState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Mark messages as read
     */
    fun markAsRead() {
        val currentOrderId = orderId ?: return
        
        viewModelScope.launch {
            chatRepository.markAsRead(currentOrderId, customerEmail, driverEmail)
        }
    }
    
    /**
     * Start polling for new messages (every 3 seconds)
     */
    private fun startPolling() {
        stopPolling() // Stop existing polling
        
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3000) // 3 seconds
                loadMessages()
            }
        }
    }
    
    /**
     * Stop polling
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    object Sending : ChatState()
    object Success : ChatState()
    data class Error(val message: String) : ChatState()
}

