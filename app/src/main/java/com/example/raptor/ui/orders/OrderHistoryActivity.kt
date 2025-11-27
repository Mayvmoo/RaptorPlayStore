package com.example.raptor.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.repositories.OrderRepository
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Order History Activity
 * Komt overeen met iOS OrderHistoryView
 */
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var loadingContainer: CardView
    private lateinit var errorContainer: CardView
    private lateinit var emptyContainer: CardView
    private lateinit var ordersListContainer: LinearLayout
    private lateinit var errorTitle: TextView
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: Button

    private val orderRepository = OrderRepository()
    private var customerEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        customerEmail = intent.getStringExtra("customer_email")

        initializeViews()
        setupToolbar()
        loadOrders()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        loadingContainer = findViewById(R.id.loadingContainer)
        errorContainer = findViewById(R.id.errorContainer)
        emptyContainer = findViewById(R.id.emptyContainer)
        ordersListContainer = findViewById(R.id.ordersListContainer)
        errorTitle = findViewById(R.id.errorTitle)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)

        retryButton.setOnClickListener {
            loadOrders()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadOrders() {
        if (customerEmail == null) {
            showEmptyState()
            return
        }

        showLoadingState()

        lifecycleScope.launch {
            val result = orderRepository.getOrdersByCustomer(customerEmail!!)

            val orders = result.getOrNull()
            if (orders == null) {
                val exception = result.exceptionOrNull()
                showErrorState(exception?.message ?: "Onbekende fout")
                return@launch
            }

            if (orders.isEmpty()) {
                showEmptyState()
            } else {
                // Sort by createdAt (newest first)
                val sortedOrders = orders.sortedByDescending { it.createdAt }
                showOrders(sortedOrders)
            }
        }
    }

    private fun showLoadingState() {
        loadingContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.GONE
        ordersListContainer.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        emptyContainer.visibility = View.GONE
        ordersListContainer.visibility = View.GONE
        errorMessage.text = message
    }

    private fun showEmptyState() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
        ordersListContainer.visibility = View.GONE
    }

    private fun showOrders(orders: List<DeliveryOrder>) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        emptyContainer.visibility = View.GONE
        ordersListContainer.visibility = View.VISIBLE

        // Clear existing orders
        ordersListContainer.removeAllViews()

        // Add order cards
        orders.forEach { order ->
            val orderCard = createOrderCard(order)
            ordersListContainer.addView(orderCard)
        }
    }

    private fun createOrderCard(order: DeliveryOrder): MaterialCardView {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16.dpToPx())
            }
            radius = 20f
            cardElevation = 8f
            setCardBackgroundColor(getColor(R.color.white_opacity_10))
            radius = 20f
        }

        // Inflate order card layout (simplified version)
        val cardContent = layoutInflater.inflate(R.layout.item_order_history_card, null)
        
        // Bind order data
        cardContent.findViewById<TextView>(R.id.orderIdText)?.text = "Order #${order.orderId.take(8)}"
        cardContent.findViewById<TextView>(R.id.statusText)?.text = getStatusText(order.status)
        cardContent.findViewById<TextView>(R.id.destinationText)?.text = order.destinationAddress
        cardContent.findViewById<TextView>(R.id.dateText)?.text = formatDate(order.createdAt)

        // Set status color
        val statusBadge = cardContent.findViewById<View>(R.id.statusBadge)
        statusBadge?.setBackgroundResource(
            when (order.status.lowercase()) {
                "completed" -> R.drawable.status_badge_background_gold
                "cancelled" -> R.drawable.status_badge_background
                else -> R.drawable.status_badge_background_blue
            }
        )

        // Set click listener
        card.setOnClickListener {
            // Open order detail
            val intent = android.content.Intent(this, OrderDetailActivity::class.java).apply {
                putExtra("order_id", order.orderId)
                putExtra("customer_email", customerEmail)
            }
            startActivity(intent)
        }

        card.addView(cardContent)
        return card
    }

    private fun getStatusText(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "In afwachting"
            "assigned" -> "Toegewezen"
            "in_progress" -> "Onderweg"
            "completed" -> "Voltooid"
            "cancelled" -> "Geannuleerd"
            else -> status
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("nl", "NL"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: return "")
        } catch (e: Exception) {
            dateString
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}

