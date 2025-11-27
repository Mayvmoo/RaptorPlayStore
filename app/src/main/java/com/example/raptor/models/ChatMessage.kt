package com.example.raptor.models

import com.google.gson.annotations.SerializedName

/**
 * Chat Message Model
 * Komt overeen met iOS ChatMessage
 */
data class ChatMessage(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("senderEmail")
    val senderEmail: String,
    
    @SerializedName("body")
    val body: String,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("isRead")
    val isRead: Boolean = false
)

