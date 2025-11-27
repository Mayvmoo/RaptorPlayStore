package com.example.raptor.models

/**
 * Lokaal model voor actieve klant sessie (niet gesynchroniseerd)
 * Komt overeen met iOS CustomerSession
 */
data class CustomerSession(
    val email: String,
    val customerType: CustomerType,
    val companyName: String? = null,
    val contactName: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profileImageData: String? = null // Base64 encoded image
) {
    /**
     * Computed property voor weergavenaam
     */
    val displayName: String
        get() = if (customerType == CustomerType.BUSINESS && !companyName.isNullOrEmpty()) {
            companyName
        } else {
            contactName
        }
}

