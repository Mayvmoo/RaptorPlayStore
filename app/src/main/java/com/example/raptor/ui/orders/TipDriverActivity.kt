package com.example.raptor.ui.orders

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

/**
 * Tip Driver Activity
 * Komt overeen met iOS TipDriverView
 */
class TipDriverActivity : AppCompatActivity() {
    
    private lateinit var driverNameText: MaterialTextView
    private lateinit var orderIdText: MaterialTextView
    private lateinit var customAmountInput: TextInputEditText
    private lateinit var messageInput: TextInputEditText
    private lateinit var tip2Button: MaterialButton
    private lateinit var tip3Button: MaterialButton
    private lateinit var tip5Button: MaterialButton
    private lateinit var tip10Button: MaterialButton
    private lateinit var submitButton: MaterialButton
    
    private var selectedAmount: Double? = null
    private var orderId: String? = null
    private var driverEmail: String? = null
    private var customerEmail: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_driver)
        
        orderId = intent.getStringExtra("order_id")
        driverEmail = intent.getStringExtra("driver_email")
        val driverName = intent.getStringExtra("driver_name") ?: "Bezorger"
        customerEmail = intent.getStringExtra("customer_email")
        
        initializeViews()
        setupClickListeners()
        
        driverNameText.text = driverName
        orderIdText.text = "Order #${orderId?.take(8) ?: ""}"
    }
    
    private fun initializeViews() {
        driverNameText = findViewById(R.id.driverNameText)
        orderIdText = findViewById(R.id.orderIdText)
        customAmountInput = findViewById(R.id.customAmountInput)
        messageInput = findViewById(R.id.messageInput)
        tip2Button = findViewById(R.id.tip2Button)
        tip3Button = findViewById(R.id.tip3Button)
        tip5Button = findViewById(R.id.tip5Button)
        tip10Button = findViewById(R.id.tip10Button)
        submitButton = findViewById(R.id.submitButton)
    }
    
    private fun setupClickListeners() {
        tip2Button.setOnClickListener { selectAmount(2.0) }
        tip3Button.setOnClickListener { selectAmount(3.0) }
        tip5Button.setOnClickListener { selectAmount(5.0) }
        tip10Button.setOnClickListener { selectAmount(10.0) }
        
        submitButton.setOnClickListener {
            submitTip()
        }
    }
    
    private fun selectAmount(amount: Double) {
        selectedAmount = amount
        customAmountInput.text?.clear()
        updateButtonStates()
    }
    
    private fun updateButtonStates() {
        tip2Button.isSelected = selectedAmount == 2.0
        tip3Button.isSelected = selectedAmount == 3.0
        tip5Button.isSelected = selectedAmount == 5.0
        tip10Button.isSelected = selectedAmount == 10.0
    }
    
    private fun submitTip() {
        val amount = selectedAmount ?: run {
            val customText = customAmountInput.text?.toString()?.trim()
            if (!customText.isNullOrEmpty()) {
                customText.replace(",", ".").toDoubleOrNull()
            } else {
                null
            }
        }
        
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Selecteer een bedrag", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = messageInput.text?.toString()?.trim()
        
        // TODO: Implement tip submission via API
        Toast.makeText(this, "Tip van €${String.format("%.2f", amount)} verzonden!", Toast.LENGTH_LONG).show()
        finish()
    }
}

