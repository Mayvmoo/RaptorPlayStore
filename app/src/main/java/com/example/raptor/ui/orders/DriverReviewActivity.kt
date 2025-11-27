package com.example.raptor.ui.orders

import android.os.Bundle
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.raptor.R
import com.example.raptor.models.DeliveryOrder
import com.example.raptor.viewmodels.OrderViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Driver Review Activity
 * Komt overeen met iOS DriverReviewView
 */
class DriverReviewActivity : AppCompatActivity() {
    
    private val viewModel: OrderViewModel by lazy {
        ViewModelProvider(this)[OrderViewModel::class.java]
    }
    
    private lateinit var orderSpinner: Spinner
    private lateinit var ratingBar: RatingBar
    private lateinit var commentInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    
    private var customerEmail: String? = null
    private var reviewableOrders: List<DeliveryOrder> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_review)
        
        customerEmail = intent.getStringExtra("customer_email")
        
        initializeViews()
        setupObservers()
        setupClickListeners()
        
        // Load completed orders
        customerEmail?.let { viewModel.fetchOrdersByCustomer(it) }
    }
    
    private fun initializeViews() {
        orderSpinner = findViewById(R.id.orderSpinner)
        ratingBar = findViewById(R.id.ratingBar)
        commentInput = findViewById(R.id.commentInput)
        submitButton = findViewById(R.id.submitButton)
    }
    
    private fun setupObservers() {
        viewModel.customerOrders.observe(this) { orders ->
            // Filter completed orders with assigned driver
            reviewableOrders = orders.filter { 
                it.status.lowercase() == "completed" && it.assignedDriverEmail != null
            }
            
            // TODO: Populate spinner with orders
        }
    }
    
    private fun setupClickListeners() {
        submitButton.setOnClickListener {
            submitReview()
        }
    }
    
    private fun submitReview() {
        val rating = ratingBar.rating.toInt()
        val comment = commentInput.text?.toString()?.trim()
        
        if (rating == 0) {
            Toast.makeText(this, "Selecteer een beoordeling", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Implement review submission via API
        Toast.makeText(this, "Review verzonden!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

