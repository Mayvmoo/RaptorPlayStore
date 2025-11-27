package com.example.raptor.models

import com.google.gson.annotations.SerializedName

/**
 * Delivery Order Model - komt overeen met iOS DeliveryOrder
 */
data class DeliveryOrder(
    @SerializedName("orderId")
    val orderId: String,
    
    @SerializedName("senderName")
    val senderName: String,
    
    @SerializedName("senderAddress")
    val senderAddress: String,
    
    @SerializedName("destinationName")
    val destinationName: String? = null,
    
    @SerializedName("destinationAddress")
    val destinationAddress: String,
    
    @SerializedName("deliveryMode")
    val deliveryMode: String = "standard",
    
    @SerializedName("isUrgent")
    val isUrgent: Boolean = false,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("status")
    val status: String = "pending", // pending, assigned, inProgress, completed, cancelled
    
    @SerializedName("assignedDriverEmail")
    val assignedDriverEmail: String? = null,
    
    @SerializedName("pickedUp")
    val pickedUp: Boolean? = false,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    
    @SerializedName("attachmentImageData")
    val attachmentImageData: String? = null, // Base64 encoded image
    
    @SerializedName("deliveryTimeMinutes")
    val deliveryTimeMinutes: Int? = null,
    
    @SerializedName("totalTimeMinutes")
    val totalTimeMinutes: Int? = null,
    
    @SerializedName("routeDistanceKilometers")
    val routeDistanceKilometers: Double? = null
) {
    /**
     * Helper property voor backward compatibility
     */
    val isPickedUp: Boolean
        get() = pickedUp ?: false
}

