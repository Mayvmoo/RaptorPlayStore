package com.example.raptor.repositories

import com.example.raptor.models.DeliveryOrder
import com.example.raptor.network.NetworkModule
import com.example.raptor.network.RaptorApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * Order Repository
 * Komt overeen met iOS OrderService
 */
class OrderRepository {
    
    private val apiService: RaptorApiService = NetworkModule.apiService
    
    // Cache voor orders (30 seconden, zoals iOS)
    private var orderCache: MutableMap<String, Pair<List<DeliveryOrder>, Long>> = mutableMapOf()
    private val cacheDuration: Long = 30_000 // 30 seconden in millis
    
    /**
     * Create order
     * Komt overeen met iOS: OrderService.createOrder
     */
    suspend fun createOrder(
        senderName: String,
        senderAddress: String,
        destinationName: String?,
        destinationAddress: String,
        deliveryMode: String,
        isUrgent: Boolean,
        notes: String?,
        attachmentImageData: String?, // Base64
        customerEmail: String?
    ): Result<String> { // Returns orderId
        return try {
            val response = apiService.createOrder(
                com.example.raptor.network.CreateOrderRequest(
                    senderName = senderName,
                    senderAddress = senderAddress,
                    destinationName = destinationName,
                    destinationAddress = destinationAddress,
                    deliveryMode = deliveryMode,
                    isUrgent = isUrgent,
                    notes = notes,
                    attachmentImageData = attachmentImageData,
                    customerEmail = customerEmail
                )
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success && apiResponse.data != null) {
                    // Clear cache na nieuwe order
                    clearCache()
                    Result.success(apiResponse.data!!)
                } else {
                    Result.failure(
                        OrderException(apiResponse.error ?: "Order aanmaken mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Get all orders
     * Komt overeen met iOS: OrderService.fetchAllOrders
     */
    suspend fun getAllOrders(): Result<List<DeliveryOrder>> {
        return try {
            // Check cache eerst
            val cached = getCachedOrders("all")
            if (cached != null) {
                return Result.success(cached)
            }
            
            val response = apiService.getAllOrders()
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!
                // Update cache
                setCachedOrders(orders, "all")
                Result.success(orders)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Get orders by customer email
     */
    suspend fun getOrdersByCustomer(customerEmail: String): Result<List<DeliveryOrder>> {
        return try {
            val cacheKey = "customer:$customerEmail"
            val cached = getCachedOrders(cacheKey)
            if (cached != null) {
                return Result.success(cached)
            }
            
            val response = apiService.getOrdersByCustomer(customerEmail)
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!
                setCachedOrders(orders, cacheKey)
                Result.success(orders)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Get available (pending) orders
     * Komt overeen met iOS: OrderService.fetchAvailableOrders
     */
    suspend fun getAvailableOrders(): Result<List<DeliveryOrder>> {
        return try {
            val cached = getCachedOrders("available")
            if (cached != null && cached.isNotEmpty()) {
                return Result.success(cached)
            }
            
            val response = apiService.getAvailableOrders("pending")
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!
                setCachedOrders(orders, "available")
                Result.success(orders)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Update order status
     * Komt overeen met iOS: OrderService.updateOrderStatus
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            val response = apiService.updateOrderStatus(
                com.example.raptor.network.UpdateOrderStatusRequest(orderId, status)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    // Clear cache na update
                    clearCache()
                    Result.success(Unit)
                } else {
                    Result.failure(
                        OrderException(apiResponse.error ?: "Status update mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    /**
     * Assign order to driver
     * Komt overeen met iOS: OrderService.assignOrder
     */
    suspend fun assignOrder(orderId: String, driverEmail: String): Result<Unit> {
        return try {
            // Clear cache VOOR assignment
            clearCache()
            
            val response = apiService.assignOrder(
                com.example.raptor.network.AssignOrderRequest(orderId, driverEmail)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    // Clear cache opnieuw na assignment
                    clearCache()
                    Result.success(Unit)
                } else {
                    Result.failure(
                        OrderException(apiResponse.error ?: "Order toewijzen mislukt")
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Onbekende fout"
                Result.failure(OrderException("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: HttpException) {
            Result.failure(OrderException("Server fout: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(OrderException("Netwerk fout: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(OrderException("Onbekende fout: ${e.message}"))
        }
    }
    
    // ========== Cache Methods ==========
    
    private fun getCachedOrders(key: String): List<DeliveryOrder>? {
        val cached = orderCache[key] ?: return null
        val now = System.currentTimeMillis()
        
        if (now - cached.second < cacheDuration) {
            return cached.first
        }
        
        // Cache expired
        orderCache.remove(key)
        return null
    }
    
    private fun setCachedOrders(orders: List<DeliveryOrder>, key: String) {
        orderCache[key] = Pair(orders, System.currentTimeMillis())
    }
    
    fun clearCache() {
        orderCache.clear()
    }
}

/**
 * Custom exception voor order errors
 */
class OrderException(message: String) : Exception(message)

