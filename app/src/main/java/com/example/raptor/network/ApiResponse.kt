package com.example.raptor.network

import com.google.gson.annotations.SerializedName

/**
 * Generic API Response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("message")
    val message: String? = null
)

/**
 * API Error Response
 */
data class ApiErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    
    @SerializedName("error")
    val error: String = ""
)

