package com.example.raptor.models

import com.google.gson.annotations.SerializedName

/**
 * Customer Account Model - komt overeen met iOS CustomerAccount
 */
data class CustomerAccount(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("passwordHash")
    val passwordHash: String? = null, // Alleen voor lokale opslag, niet naar backend
    
    @SerializedName("customerType")
    val customerType: String, // "business" of "individual"
    
    @SerializedName("companyName")
    val companyName: String? = null,
    
    @SerializedName("contactName")
    val contactName: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    
    @SerializedName("address")
    val address: String? = null,
    
    @SerializedName("profileImageData")
    val profileImageData: String? = null, // Base64 encoded image
    
    @SerializedName("isActive")
    val isActive: Boolean = true,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("lastLoginAt")
    val lastLoginAt: String? = null
) {
    /**
     * Computed property voor CustomerType enum
     */
    val type: CustomerType
        get() = when (customerType) {
            "business" -> CustomerType.BUSINESS
            "individual" -> CustomerType.INDIVIDUAL
            else -> CustomerType.BUSINESS
        }
    
    /**
     * Computed property voor weergavenaam (bedrijfsnaam voor zakelijk, contactnaam voor particulier)
     */
    val displayName: String
        get() = if (type == CustomerType.BUSINESS && !companyName.isNullOrEmpty()) {
            companyName
        } else {
            contactName
        }
}

