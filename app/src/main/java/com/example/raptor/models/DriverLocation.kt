package com.example.raptor.models

import com.google.gson.annotations.SerializedName

/**
 * Driver Location Model
 */
data class DriverLocation(
    @SerializedName("driverEmail")
    val driverEmail: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

