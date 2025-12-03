package com.example.raptor.network

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CompletableDeferred
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * WebSocket Service voor Customer app - real-time communicatie
 * Verbindt met wss://57.131.28.13/ws/
 */
class WebSocketService private constructor() {
    
    companion object {
        private const val TAG = "WebSocketService"
        private const val WS_URL = "wss://57.131.28.13/ws/"
        private const val PING_INTERVAL_SECONDS = 30L
        private const val RECONNECT_DELAY_SECONDS = 5L
        
        @Volatile
        private var INSTANCE: WebSocketService? = null
        
        fun getInstance(): WebSocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketService().also { INSTANCE = it }
            }
        }
    }
    
    private val client = OkHttpClient.Builder()
        .pingInterval(PING_INTERVAL_SECONDS, TimeUnit.SECONDS)
        // SSL configuration voor self-signed certificates (zelfde als NetworkModule)
        .sslSocketFactory(
            SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<TrustManager>(SelfSignedTrustManager()), java.security.SecureRandom())
            }.socketFactory,
            SelfSignedTrustManager()
        )
        .hostnameVerifier { hostname, session ->
            // Accepteer development server hostname (zelfde als NetworkModule)
            if (hostname == "57.131.28.13") {
                true
            } else {
                HostnameVerifier.DEFAULT.verify(hostname, session)
            }
        }
        .build()
    
    private var webSocket: WebSocket? = null
    private var clientId: String = UUID.randomUUID().toString()
    private var clientType: String = "customer"
    private var customerEmail: String? = null
    
    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Message publishers (real-time updates)
    private val _chatMessages = MutableStateFlow<Map<String, Any>>(emptyMap())
    val chatMessages = _chatMessages.asStateFlow()
    
    private val _orderUpdates = MutableStateFlow<Map<String, Any>>(emptyMap())
    val orderUpdates = _orderUpdates.asStateFlow()
    
    private val _driverLocationUpdates = MutableStateFlow<Map<String, Any>>(emptyMap())
    val driverLocationUpdates = _driverLocationUpdates.asStateFlow()
    
    // Data fetching publishers (request/response) - Customer
    private val _customerOrdersData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val customerOrdersData = _customerOrdersData.asStateFlow()
    
    private val _customerProfileData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val customerProfileData = _customerProfileData.asStateFlow()
    
    private val _activeDriversData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val activeDriversData = _activeDriversData.asStateFlow()
    
    private val _orderDetailsData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val orderDetailsData = _orderDetailsData.asStateFlow()
    
    private val _orderHistoryData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val orderHistoryData = _orderHistoryData.asStateFlow()
    
    // Request tracking for async responses
    private val pendingRequests = mutableMapOf<String, CompletableDeferred<Map<String, Any>?>>()
    private var requestCounter = 0
    
    private val subscriptions = mutableSetOf<String>()
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null
    
    /**
     * Connect to WebSocket server
     */
    fun connect(email: String? = null) {
        if (_connectionState.value == ConnectionState.Connecting || 
            _connectionState.value == ConnectionState.Connected) {
            Log.w(TAG, "Already connecting or connected")
            return
        }
        
        this.customerEmail = email
        
        // Build URL with query parameters
        val httpUrl = HttpUrl.parse(WS_URL) ?: return
        val urlBuilder = httpUrl.newBuilder()
        
        urlBuilder.addQueryParameter("clientId", clientId)
        urlBuilder.addQueryParameter("type", clientType)
        email?.let { urlBuilder.addQueryParameter("email", it) }
        
        val url = urlBuilder.build()
        
        Log.d(TAG, "Connecting to: $url")
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        webSocket = client.newWebSocket(request, createWebSocketListener())
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting...")
        
        reconnectJob?.cancel()
        reconnectJob = null
        
        pingJob?.cancel()
        pingJob = null
        
        // Cancel all pending requests
        for ((_, deferred) in pendingRequests) {
            deferred.complete(mapOf("error" to "Connection closed"))
        }
        pendingRequests.clear()
        
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
        
        _connectionState.value = ConnectionState.Disconnected
        subscriptions.clear()
        
        Log.d(TAG, "Disconnected")
    }
    
    /**
     * Subscribe to channels
     */
    fun subscribe(channels: List<String>) {
        channels.forEach { subscriptions.add(it) }
        
        val message = JSONObject().apply {
            put("type", "subscribe")
            put("channels", channels)
        }
        
        sendMessage(message.toString())
    }
    
    /**
     * Unsubscribe from channels
     */
    fun unsubscribe(channels: List<String>) {
        channels.forEach { subscriptions.remove(it) }
        
        val message = JSONObject().apply {
            put("type", "unsubscribe")
            put("channels", channels)
        }
        
        sendMessage(message.toString())
    }
    
    /**
     * Send chat message
     */
    fun sendChatMessage(orderId: String, message: String, senderEmail: String) {
        val messageData = JSONObject().apply {
            put("type", "chat_message")
            put("orderId", orderId)
            put("message", message)
            put("senderEmail", senderEmail)
            put("timestamp", System.currentTimeMillis())
        }
        
        sendMessage(messageData.toString())
    }
    
    /**
     * Send message to server
     */
    private fun sendMessage(message: String) {
        val ws = webSocket
        if (ws == null || _connectionState.value != ConnectionState.Connected) {
            Log.w(TAG, "Cannot send message - not connected")
            return
        }
        
        val sent = ws.send(message)
        if (!sent) {
            Log.e(TAG, "Failed to send message")
        } else {
            Log.d(TAG, "Message sent: ${message.take(100)}")
        }
    }
    
    /**
     * Create WebSocket listener
     */
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket opened")
                _connectionState.value = ConnectionState.Connected
                
                // Start ping timer
                startPingTimer()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: ${text.take(100)}")
                handleMessage(text)
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val text = bytes.utf8()
                Log.d(TAG, "Message received (bytes): ${text.take(100)}")
                handleMessage(text)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                webSocket.close(1000, null)
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                _connectionState.value = ConnectionState.Disconnected
                webSocket = null
                
                // Try to reconnect
                scheduleReconnect()
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}", t)
                _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
                webSocket = null
                
                // Try to reconnect
                scheduleReconnect()
            }
        }
    }
    
    /**
     * Handle incoming message
     */
    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.optString("type", "")
            
            when (type) {
                "connected" -> {
                    val clientId = json.optString("clientId", "")
                    Log.d(TAG, "Connected: $clientId")
                    _connectionState.value = ConnectionState.Connected
                }
                
                "subscribed" -> {
                    Log.d(TAG, "Subscribed to channels")
                }
                
                "chat_message" -> {
                    val data = json.optJSONObject("data")?.toMap() ?: emptyMap()
                    _chatMessages.value = data
                }
                
                "order_update" -> {
                    val data = json.optJSONObject("data")?.toMap() ?: emptyMap()
                    _orderUpdates.value = data
                }
                
                "location_update" -> {
                    val data = json.optJSONObject("data")?.toMap() ?: emptyMap()
                    _driverLocationUpdates.value = data
                }
                
                // Customer app data fetching responses
                "customer_orders_response" -> {
                    handleDataResponse("customer_orders", json)
                }
                
                "customer_profile_response" -> {
                    handleDataResponse("customer_profile", json)
                }
                
                "active_drivers_response" -> {
                    handleDataResponse("active_drivers", json)
                }
                
                "order_details_response" -> {
                    handleDataResponse("order_details", json)
                }
                
                "order_history_response" -> {
                    handleDataResponse("order_history", json)
                }
                
                "pong" -> {
                    // Heartbeat response
                }
                
                "error" -> {
                    val errorMsg = json.optString("message", "Unknown error")
                    val requestId = json.optString("requestId", "")
                    if (requestId.isNotEmpty()) {
                        val deferred = pendingRequests.remove(requestId)
                        deferred?.complete(mapOf("error" to errorMsg))
                    } else {
                        Log.e(TAG, "Server error: $errorMsg")
                    }
                }
                
                else -> {
                    Log.w(TAG, "Unknown message type: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: ${e.message}", e)
        }
    }
    
    /**
     * Handle data response from server
     */
    private fun handleDataResponse(type: String, json: JSONObject) {
        val requestId = json.optString("requestId", "")
        val data = json.optJSONObject("data")?.toMap() ?: emptyMap()
        
        // Resolve pending request if exists
        if (requestId.isNotEmpty()) {
            val deferred = pendingRequests.remove(requestId)
            deferred?.complete(data)
        }
        
        // Also publish to subscribers
        when (type) {
            "customer_orders" -> _customerOrdersData.value = data
            "customer_profile" -> _customerProfileData.value = data
            "active_drivers" -> _activeDriversData.value = data
            "order_details" -> _orderDetailsData.value = data
            "order_history" -> _orderHistoryData.value = data
        }
    }
    
    /**
     * Generate unique request ID
     */
    private fun generateRequestId(): String {
        requestCounter++
        return "$clientId-$requestCounter-${System.currentTimeMillis()}"
    }
    
    /**
     * Fetch customer orders (active and history)
     */
    suspend fun fetchCustomerOrders(customerEmail: String, includeHistory: Boolean = false): Map<String, Any>? = withContext(Dispatchers.IO) {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<Map<String, Any>?>()
        
        pendingRequests[requestId] = deferred
        
        val messageData = JSONObject().apply {
            put("type", "fetch_customer_orders")
            put("requestId", requestId)
            put("customerEmail", customerEmail)
            put("includeHistory", includeHistory)
        }
        
        sendMessage(messageData.toString())
        
        withTimeoutOrNull(10000) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(requestId)
            mapOf("error" to "Request timeout")
        }
    }
    
    /**
     * Fetch customer profile
     */
    suspend fun fetchCustomerProfile(customerEmail: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<Map<String, Any>?>()
        
        pendingRequests[requestId] = deferred
        
        val messageData = JSONObject().apply {
            put("type", "fetch_customer_profile")
            put("requestId", requestId)
            put("customerEmail", customerEmail)
        }
        
        sendMessage(messageData.toString())
        
        withTimeoutOrNull(10000) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(requestId)
            mapOf("error" to "Request timeout")
        }
    }
    
    /**
     * Fetch active drivers (for customer to see available drivers)
     */
    suspend fun fetchActiveDrivers(): Map<String, Any>? = withContext(Dispatchers.IO) {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<Map<String, Any>?>()
        
        pendingRequests[requestId] = deferred
        
        val messageData = JSONObject().apply {
            put("type", "fetch_active_drivers")
            put("requestId", requestId)
        }
        
        sendMessage(messageData.toString())
        
        withTimeoutOrNull(10000) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(requestId)
            mapOf("error" to "Request timeout")
        }
    }
    
    /**
     * Fetch order details
     */
    suspend fun fetchOrderDetails(orderId: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<Map<String, Any>?>()
        
        pendingRequests[requestId] = deferred
        
        val messageData = JSONObject().apply {
            put("type", "fetch_order_details")
            put("requestId", requestId)
            put("orderId", orderId)
        }
        
        sendMessage(messageData.toString())
        
        withTimeoutOrNull(10000) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(requestId)
            mapOf("error" to "Request timeout")
        }
    }
    
    /**
     * Fetch order history for customer
     */
    suspend fun fetchOrderHistory(customerEmail: String, limit: Int? = null): Map<String, Any>? = withContext(Dispatchers.IO) {
        val requestId = generateRequestId()
        val deferred = CompletableDeferred<Map<String, Any>?>()
        
        pendingRequests[requestId] = deferred
        
        val messageData = JSONObject().apply {
            put("type", "fetch_order_history")
            put("requestId", requestId)
            put("customerEmail", customerEmail)
            limit?.let { put("limit", it) }
        }
        
        sendMessage(messageData.toString())
        
        withTimeoutOrNull(10000) {
            deferred.await()
        } ?: run {
            pendingRequests.remove(requestId)
            mapOf("error" to "Request timeout")
        }
    }
    
    /**
     * Start ping timer
     */
    private fun startPingTimer() {
        pingJob?.cancel()
        pingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == ConnectionState.Connected) {
                delay(PING_INTERVAL_SECONDS * 1000)
                
                val message = JSONObject().apply {
                    put("type", "ping")
                }
                sendMessage(message.toString())
            }
        }
    }
    
    /**
     * Schedule reconnection
     */
    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY_SECONDS * 1000)
            
            if (_connectionState.value != ConnectionState.Connected) {
                Log.d(TAG, "Attempting to reconnect...")
                connect(customerEmail)
            }
        }
    }
    
    /**
     * Connection state enum
     */
    enum class ConnectionState {
        Disconnected,
        Connecting,
        Connected,
        Error(String)
    }
}

/**
 * Extension to convert JSONObject to Map
 */
private fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val value = this.get(key)
        map[key] = when (value) {
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        }
    }
    return map
}

/**
 * Extension to convert JSONArray to List
 */
private fun org.json.JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        val value = this.get(i)
        list.add(when (value) {
            is JSONObject -> value.toMap()
            is org.json.JSONArray -> value.toList()
            else -> value
        })
    }
    return list
}

