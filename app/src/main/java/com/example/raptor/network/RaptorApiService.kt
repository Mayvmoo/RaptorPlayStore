package com.example.raptor.network

import com.example.raptor.models.ChatMessage
import com.example.raptor.models.CustomerAccount
import com.example.raptor.models.CustomerSession
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.models.DriverLocation
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Interface - definieert alle backend endpoints
 * Komt overeen met iOS APIService endpoints
 */
interface RaptorApiService {
    
    // ========== Customer Authentication ==========
    
    /**
     * Authenticate customer (POST /customers.php)
     */
    @POST("customers.php")
    suspend fun authenticateCustomer(
        @Body request: CustomerAuthRequest
    ): Response<ApiResponse<CustomerSessionResponse>>
    
    /**
     * Register customer (POST /customers.php?action=register)
     */
    @POST("customers.php")
    suspend fun registerCustomer(
        @Body request: CustomerRegisterRequest
    ): Response<ApiResponse<CustomerSessionResponse>>
    
    /**
     * Get customer by email (GET /customers.php?email=...)
     */
    @GET("customers.php")
    suspend fun getCustomer(
        @Query("email") email: String
    ): Response<ApiResponse<CustomerAccount>>
    
    /**
     * Update customer (PATCH /customers.php)
     */
    @PATCH("customers.php")
    suspend fun updateCustomer(
        @Body request: CustomerUpdateRequest
    ): Response<ApiResponse<CustomerAccount>>
    
    /**
     * Request password reset (POST /password_reset.php)
     */
    @POST("password_reset.php")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequest
    ): Response<ApiResponse<Unit>>
    
    // ========== Orders ==========
    
    /**
     * Create order (POST /orders.php)
     */
    @POST("orders.php")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<ApiResponse<String>> // Returns orderId
    
    /**
     * Get all orders (GET /orders.php)
     */
    @GET("orders.php")
    suspend fun getAllOrders(): Response<List<DeliveryOrder>>
    
    /**
     * Get orders by customer email (GET /orders.php?customer_email=...)
     */
    @GET("orders.php")
    suspend fun getOrdersByCustomer(
        @Query("customer_email") customerEmail: String
    ): Response<List<DeliveryOrder>>
    
    /**
     * Get orders by driver email (GET /orders.php?driver_email=...)
     */
    @GET("orders.php")
    suspend fun getOrdersByDriver(
        @Query("driver_email") driverEmail: String
    ): Response<List<DeliveryOrder>>
    
    /**
     * Get available (pending) orders (GET /orders.php?status=pending)
     */
    @GET("orders.php")
    suspend fun getAvailableOrders(
        @Query("status") status: String = "pending"
    ): Response<List<DeliveryOrder>>
    
    /**
     * Update order status (PATCH /orders.php)
     */
    @PATCH("orders.php")
    suspend fun updateOrderStatus(
        @Body request: UpdateOrderStatusRequest
    ): Response<ApiResponse<Unit>>
    
    /**
     * Assign order to driver (PATCH /orders.php)
     */
    @PATCH("orders.php")
    suspend fun assignOrder(
        @Body request: AssignOrderRequest
    ): Response<ApiResponse<Unit>>
    
    // ========== Chat ==========
    
    /**
     * Get chat messages (GET /order_chat.php?order_id=...)
     */
    @GET("order_chat.php")
    suspend fun getChatMessages(
        @Query("order_id") orderId: String,
        @Query("customer_email") customerEmail: String? = null,
        @Query("driver_email") driverEmail: String? = null
    ): Response<List<ChatMessage>>
    
    /**
     * Send chat message (POST /order_chat.php)
     */
    @POST("order_chat.php")
    suspend fun sendChatMessage(
        @Body request: SendChatMessageRequest
    ): Response<ApiResponse<ChatMessage>>
    
    /**
     * Mark messages as read (PATCH /order_chat.php)
     */
    @PATCH("order_chat.php")
    suspend fun markChatMessagesAsRead(
        @Body request: MarkMessagesReadRequest
    ): Response<ApiResponse<Unit>>
    
    // ========== Driver Locations ==========
    
    /**
     * Get driver locations (GET /driver_locations.php)
     */
    @GET("driver_locations.php")
    suspend fun getDriverLocations(): Response<List<DriverLocation>>
}

// ========== Request Models ==========

data class CustomerAuthRequest(
    val email: String,
    val password: String
)

data class CustomerRegisterRequest(
    val action: String = "register",
    val email: String,
    val password: String,
    val customerType: String,
    val companyName: String? = null,
    val contactName: String,
    val phoneNumber: String? = null,
    val address: String? = null
)

data class CustomerUpdateRequest(
    val email: String,
    val companyName: String? = null,
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null
)

data class PasswordResetRequest(
    val email: String
)

data class CreateOrderRequest(
    val senderName: String,
    val senderAddress: String,
    val destinationName: String? = null,
    val destinationAddress: String,
    val deliveryMode: String = "standard",
    val isUrgent: Boolean = false,
    val notes: String? = null,
    val attachmentImageData: String? = null, // Base64
    val customerEmail: String? = null
)

data class UpdateOrderStatusRequest(
    val orderId: String,
    val status: String
)

data class AssignOrderRequest(
    val orderId: String,
    val driverEmail: String
)

data class SendChatMessageRequest(
    val orderId: String,
    val senderEmail: String,
    val body: String
)

data class MarkMessagesReadRequest(
    val orderId: String,
    val customerEmail: String? = null,
    val driverEmail: String? = null
)

// ========== Response Models ==========

data class CustomerSessionResponse(
    val email: String,
    val customerType: String,
    val companyName: String? = null,
    val contactName: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val profileImageData: String? = null
) {
    fun toCustomerSession(): CustomerSession {
        val type = when (customerType) {
            "business" -> com.example.raptor.models.CustomerType.BUSINESS
            "individual" -> com.example.raptor.models.CustomerType.INDIVIDUAL
            else -> com.example.raptor.models.CustomerType.BUSINESS
        }
        return CustomerSession(
            email = email,
            customerType = type,
            companyName = companyName,
            contactName = contactName,
            phoneNumber = phoneNumber,
            address = address,
            profileImageData = profileImageData
        )
    }
}

