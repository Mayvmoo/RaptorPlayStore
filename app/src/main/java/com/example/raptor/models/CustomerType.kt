package com.example.raptor.models

/**
 * Type van klant: zakelijk of particulier
 */
enum class CustomerType(val value: String, val displayName: String) {
    BUSINESS("business", "Zakelijk"),
    INDIVIDUAL("individual", "Particulier")
}

