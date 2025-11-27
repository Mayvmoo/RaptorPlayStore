package com.example.raptor.models

import com.google.gson.annotations.SerializedName

/**
 * Customer Partner Model
 * Komt overeen met iOS CustomerPartner
 */
data class CustomerPartner(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("customerEmail")
    val customerEmail: String,
    
    @SerializedName("partnerEmail")
    val partnerEmail: String,
    
    @SerializedName("partnerName")
    val partnerName: String,
    
    @SerializedName("partnerCompany")
    val partnerCompany: String,
    
    @SerializedName("partnerAddress")
    val partnerAddress: String? = null
)

