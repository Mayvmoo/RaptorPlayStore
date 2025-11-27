package com.example.raptor.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.repositories.OrderException
import com.example.raptor.repositories.OrderRepository
import kotlinx.coroutines.launch

/**
 * Order ViewModel
 * Komt overeen met iOS OrderService functionaliteit
 */
class OrderViewModel : ViewModel() {
    
    private val orderRepository = OrderRepository()
    
    // LiveData voor UI updates
    private val _ordersState = MutableLiveData<OrdersState>()
    val ordersState: LiveData<OrdersState> = _ordersState
    
    private val _allOrders = MutableLiveData<List<DeliveryOrder>>()
    val allOrders: LiveData<List<DeliveryOrder>> = _allOrders
    
    private val _availableOrders = MutableLiveData<List<DeliveryOrder>>()
    val availableOrders: LiveData<List<DeliveryOrder>> = _availableOrders
    
    private val _customerOrders = MutableLiveData<List<DeliveryOrder>>()
    val customerOrders: LiveData<List<DeliveryOrder>> = _customerOrders
    
    private val _selectedOrder = MutableLiveData<DeliveryOrder?>()
    val selectedOrder: LiveData<DeliveryOrder?> = _selectedOrder
    
    /**
     * Create new order
     * Komt overeen met iOS: OrderService.createOrder
     */
    fun createOrder(
        senderName: String,
        senderAddress: String,
        destinationName: String?,
        destinationAddress: String,
        deliveryMode: String = "standard",
        isUrgent: Boolean = false,
        notes: String? = null,
        attachmentImageData: String? = null, // Base64
        customerEmail: String?
    ) {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            
            val result = orderRepository.createOrder(
                senderName, senderAddress, destinationName, destinationAddress,
                deliveryMode, isUrgent, notes, attachmentImageData, customerEmail
            )
            if (result.isSuccess) {
                val orderId = result.getOrNull() ?: ""
                _ordersState.value = OrdersState.OrderCreated(orderId)
                // Refresh orders after creation
                fetchAllOrders()
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is OrderException -> error.message
                    else -> "Order aanmaken mislukt: ${error?.message}"
                }
                _ordersState.value = OrdersState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Fetch all orders
     * Komt overeen met iOS: OrderService.fetchAllOrders
     */
    fun fetchAllOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            
            val result = orderRepository.getAllOrders()
            if (result.isSuccess) {
                val orders = result.getOrNull() ?: emptyList()
                _allOrders.value = orders
                _ordersState.value = OrdersState.Success(orders)
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is OrderException -> error.message
                    else -> "Orders ophalen mislukt: ${error?.message}"
                }
                _ordersState.value = OrdersState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Fetch orders by customer email
     */
    fun fetchOrdersByCustomer(customerEmail: String) {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            
            val result = orderRepository.getOrdersByCustomer(customerEmail)
            if (result.isSuccess) {
                val orders = result.getOrNull() ?: emptyList()
                _customerOrders.value = orders
                _ordersState.value = OrdersState.Success(orders)
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is OrderException -> error.message
                    else -> "Orders ophalen mislukt: ${error?.message}"
                }
                _ordersState.value = OrdersState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Fetch available (pending) orders
     * Komt overeen met iOS: OrderService.fetchAvailableOrders
     */
    fun fetchAvailableOrders() {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            
            val result = orderRepository.getAvailableOrders()
            if (result.isSuccess) {
                val orders = result.getOrNull() ?: emptyList()
                _availableOrders.value = orders
                _ordersState.value = OrdersState.Success(orders)
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is OrderException -> error.message
                    else -> "Orders ophalen mislukt: ${error?.message}"
                }
                _ordersState.value = OrdersState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Update order status
     * Komt overeen met iOS: OrderService.updateOrderStatus
     */
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            
            val result = orderRepository.updateOrderStatus(orderId, status)
            if (result.isSuccess) {
                _ordersState.value = OrdersState.OrderUpdated
                // Refresh orders after update
                fetchAllOrders()
            } else {
                val error = result.exceptionOrNull()
                val errorMessage = when (error) {
                    is OrderException -> error.message
                    else -> "Status update mislukt: ${error?.message}"
                }
                _ordersState.value = OrdersState.Error(errorMessage ?: "Onbekende fout")
            }
        }
    }
    
    /**
     * Cancel order
     */
    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, "cancelled")
    }
    
    /**
     * Complete order
     */
    fun completeOrder(orderId: String) {
        updateOrderStatus(orderId, "completed")
    }
    
    /**
     * Select order for detail view
     */
    fun selectOrder(order: DeliveryOrder) {
        _selectedOrder.value = order
    }
    
    /**
     * Clear selected order
     */
    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }
    
    /**
     * Get active orders (pending status)
     */
    fun getActiveOrders(): List<DeliveryOrder> {
        return _allOrders.value?.filter { 
            it.status.lowercase() == "pending" && 
            it.status.lowercase() != "cancelled" && 
            it.status.lowercase() != "completed"
        } ?: emptyList()
    }
    
    /**
     * Get accepted orders (assigned or inProgress)
     */
    fun getAcceptedOrders(): List<DeliveryOrder> {
        return _allOrders.value?.filter { 
            val status = it.status.lowercase()
            status == "assigned" || status == "inprogress"
        } ?: emptyList()
    }
}

/**
 * Orders State
 */
sealed class OrdersState {
    object Idle : OrdersState()
    object Loading : OrdersState()
    data class Success(val orders: List<DeliveryOrder>) : OrdersState()
    data class Error(val message: String) : OrdersState()
    data class OrderCreated(val orderId: String) : OrdersState()
    object OrderUpdated : OrdersState()
}

