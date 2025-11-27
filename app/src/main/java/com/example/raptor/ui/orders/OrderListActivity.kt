package com.example.raptor.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.viewmodels.OrderViewModel
import com.example.raptor.viewmodels.OrdersState
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.raptor.ui.profile.CustomerProfileActivity
import kotlinx.coroutines.launch

/**
 * Order List Activity
 * Komt overeen met iOS CustomerRootView orders tab
 */
class OrderListActivity : AppCompatActivity() {
    
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private lateinit var fab: FloatingActionButton
    
    private var customerEmail: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)
        
        customerEmail = intent.getStringExtra("customer_email")
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initializeViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Load orders
        if (customerEmail != null) {
            viewModel.fetchOrdersByCustomer(customerEmail!!)
        } else {
            viewModel.fetchAllOrders()
        }
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.ordersRecyclerView)
        fab = findViewById(R.id.fabCreateOrder)
    }
    
    private fun setupRecyclerView() {
        adapter = OrderAdapter { order ->
            // Open order detail
            val intent = Intent(this, OrderDetailActivity::class.java).apply {
                putExtra("order_id", order.orderId)
                putExtra("customer_email", customerEmail)
            }
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.ordersState.observe(this) { state ->
            when (state) {
                is OrdersState.Idle -> {}
                is OrdersState.Loading -> {
                    // Show loading indicator
                }
                is OrdersState.Success -> {
                    adapter.submitList(state.orders)
                }
                is OrdersState.Error -> {
                    Toast.makeText(this, "Fout: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
        
        // Observe customer orders specifically
        viewModel.customerOrders.observe(this) { orders ->
            adapter.submitList(orders)
        }
    }
    
    private fun setupClickListeners() {
        fab.setOnClickListener {
            val intent = Intent(this, CreateOrderActivity::class.java).apply {
                putExtra("customer_email", customerEmail)
            }
            startActivity(intent)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_order_list, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_profile -> {
                customerEmail?.let {
                    val intent = Intent(this, CustomerProfileActivity::class.java).apply {
                        putExtra("customer_email", it)
                    }
                    startActivity(intent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

/**
 * Order Adapter for RecyclerView
 */
class OrderAdapter(
    private val onOrderClick: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<OrderViewHolder>() {
    
    private var orders: List<DeliveryOrder> = emptyList()
    
    fun submitList(newOrders: List<DeliveryOrder>) {
        orders = newOrders
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OrderViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view, onOrderClick)
    }
    
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }
    
    override fun getItemCount(): Int = orders.size
}

/**
 * Order ViewHolder
 */
class OrderViewHolder(
    itemView: View,
    private val onOrderClick: (DeliveryOrder) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    
    private val orderIdText: android.widget.TextView = itemView.findViewById(R.id.orderIdText)
    private val statusText: android.widget.TextView = itemView.findViewById(R.id.statusText)
    private val senderAddressText: android.widget.TextView = itemView.findViewById(R.id.senderAddressText)
    private val destinationAddressText: android.widget.TextView = itemView.findViewById(R.id.destinationAddressText)
    
    fun bind(order: DeliveryOrder) {
        orderIdText.text = "Order #${order.orderId.take(8)}"
        statusText.text = order.status
        senderAddressText.text = order.senderAddress
        destinationAddressText.text = order.destinationAddress
        
        itemView.setOnClickListener {
            onOrderClick(order)
        }
    }
}

