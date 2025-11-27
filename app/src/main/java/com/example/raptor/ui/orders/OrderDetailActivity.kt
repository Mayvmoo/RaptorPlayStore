package com.example.raptor.ui.orders

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.viewmodels.OrderViewModel
import com.example.raptor.viewmodels.OrdersState
import android.content.Intent
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.example.raptor.ui.chat.OrderChatActivity
import com.example.raptor.ui.map.CustomerMapActivity
import com.example.raptor.ui.orders.TipDriverActivity

/**
 * Order Detail Activity
 * Komt overeen met iOS OrderDetailView
 */
class OrderDetailActivity : AppCompatActivity() {
    
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var orderIdText: MaterialTextView
    private lateinit var statusText: MaterialTextView
    private lateinit var senderNameText: MaterialTextView
    private lateinit var senderAddressText: MaterialTextView
    private lateinit var destinationNameText: MaterialTextView
    private lateinit var destinationAddressText: MaterialTextView
    private lateinit var notesText: MaterialTextView
    private lateinit var cancelButton: MaterialButton
    private lateinit var chatButton: MaterialButton
    private lateinit var mapButton: MaterialButton
    private lateinit var tipButton: MaterialButton
    
    private var orderId: String? = null
    private var customerEmail: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        
        orderId = intent.getStringExtra("order_id")
        customerEmail = intent.getStringExtra("customer_email")
        
        initializeViews()
        setupObservers()
        setupClickListeners()
        
        // Load order details
        if (orderId != null) {
            loadOrderDetails()
        }
    }
    
    private fun initializeViews() {
        orderIdText = findViewById(R.id.orderIdText)
        statusText = findViewById(R.id.statusText)
        senderNameText = findViewById(R.id.senderNameText)
        senderAddressText = findViewById(R.id.senderAddressText)
        destinationNameText = findViewById(R.id.destinationNameText)
        destinationAddressText = findViewById(R.id.destinationAddressText)
        notesText = findViewById(R.id.notesText)
        cancelButton = findViewById(R.id.cancelButton)
        chatButton = findViewById(R.id.chatButton)
        mapButton = findViewById(R.id.mapButton)
        tipButton = findViewById(R.id.tipButton)
    }
    
    private fun setupObservers() {
        viewModel.selectedOrder.observe(this) { order ->
            order?.let { displayOrder(it) }
        }
        
        viewModel.ordersState.observe(this) { state ->
            when (state) {
                is OrdersState.OrderUpdated -> {
                    Toast.makeText(this, "Order geannuleerd", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is OrdersState.Error -> {
                    Toast.makeText(this, "Fout: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    private fun setupClickListeners() {
        cancelButton.setOnClickListener {
            orderId?.let { viewModel.cancelOrder(it) }
        }
        
        chatButton.setOnClickListener {
            orderId?.let { id ->
                val order = viewModel.allOrders.value?.find { it.orderId == id }
                if (order != null && order.assignedDriverEmail != null) {
                    val intent = Intent(this, OrderChatActivity::class.java).apply {
                        putExtra("order_id", id)
                        putExtra("customer_email", customerEmail)
                        putExtra("driver_email", order.assignedDriverEmail)
                        putExtra("driver_name", "Bezorger") // TODO: Get driver name
                    }
                    startActivity(intent)
                }
            }
        }
        
        mapButton.setOnClickListener {
            orderId?.let { id ->
                val order = viewModel.allOrders.value?.find { it.orderId == id }
                if (order != null && order.assignedDriverEmail != null) {
                    val intent = Intent(this, CustomerMapActivity::class.java).apply {
                        putExtra("tracked_order_id", order.orderId)
                        putExtra("tracked_driver_email", order.assignedDriverEmail)
                    }
                    startActivity(intent)
                }
            }
        }
        
        tipButton.setOnClickListener {
            orderId?.let { id ->
                val order = viewModel.allOrders.value?.find { it.orderId == id }
                if (order != null && order.assignedDriverEmail != null) {
                    val intent = Intent(this, TipDriverActivity::class.java).apply {
                        putExtra("order_id", id)
                        putExtra("driver_email", order.assignedDriverEmail)
                        putExtra("driver_name", "Bezorger") // TODO: Get driver name
                        putExtra("customer_email", customerEmail)
                    }
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun loadOrderDetails() {
        // Find order in all orders
        viewModel.allOrders.observe(this) { orders ->
            val order = orders.find { it.orderId == orderId }
            order?.let { viewModel.selectOrder(it) }
        }
        
        // Refresh orders to get latest data
        viewModel.fetchAllOrders()
    }
    
    private fun displayOrder(order: DeliveryOrder) {
        orderIdText.text = "Order #${order.orderId.take(8)}"
        statusText.text = order.status
        senderNameText.text = order.senderName
        senderAddressText.text = order.senderAddress
        destinationNameText.text = order.destinationName ?: "Geen naam"
        destinationAddressText.text = order.destinationAddress
        notesText.text = order.notes ?: "Geen notities"
        
        // Show/hide buttons based on status
        val canCancel = order.status.lowercase() == "pending"
        val canChat = order.assignedDriverEmail != null && 
                     (order.status.lowercase() == "assigned" || order.status.lowercase() == "inprogress")
        val canViewMap = order.assignedDriverEmail != null && 
                        (order.status.lowercase() == "assigned" || order.status.lowercase() == "inprogress")
        val canTip = order.status.lowercase() == "completed" && order.assignedDriverEmail != null
        
        cancelButton.visibility = if (canCancel) android.view.View.VISIBLE else android.view.View.GONE
        chatButton.visibility = if (canChat) android.view.View.VISIBLE else android.view.View.GONE
        mapButton.visibility = if (canViewMap) android.view.View.VISIBLE else android.view.View.GONE
        tipButton.visibility = if (canTip) android.view.View.VISIBLE else android.view.View.GONE
    }
}

