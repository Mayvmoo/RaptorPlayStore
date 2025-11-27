package com.example.raptor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import com.example.raptor.models.CustomerSession
import com.example.raptor.ui.orders.OrderListActivity

/**
 * Customer Profile Activity
 * Komt overeen met iOS CustomerProfileView
 */
class CustomerProfileActivity : AppCompatActivity() {
    
    private lateinit var displayNameText: TextView
    private lateinit var emailText: TextView
    private lateinit var customerTypeText: TextView
    private lateinit var orderHistoryButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    
    private var session: CustomerSession? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)
        
        // Get session from intent (in production, use SharedPreferences or similar)
        val email = intent.getStringExtra("customer_email")
        val contactName = intent.getStringExtra("contact_name")
        val customerType = intent.getStringExtra("customer_type")
        
        if (email != null && contactName != null) {
            session = CustomerSession(
                email = email,
                customerType = com.example.raptor.models.CustomerType.valueOf(customerType ?: "INDIVIDUAL"),
                contactName = contactName,
                companyName = intent.getStringExtra("company_name"),
                phoneNumber = intent.getStringExtra("phone_number"),
                address = intent.getStringExtra("address")
            )
        }
        
        initializeViews()
        setupClickListeners()
        displayProfile()
    }
    
    private fun initializeViews() {
        displayNameText = findViewById(R.id.displayNameText)
        emailText = findViewById(R.id.emailText)
        customerTypeText = findViewById(R.id.customerTypeText)
        orderHistoryButton = findViewById(R.id.orderHistoryButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)
    }
    
    private fun setupClickListeners() {
        orderHistoryButton.setOnClickListener {
            session?.let {
                val intent = Intent(this, OrderListActivity::class.java).apply {
                    putExtra("customer_email", it.email)
                    putExtra("show_history", true)
                }
                startActivity(intent)
            }
        }
        
        editProfileButton.setOnClickListener {
            session?.let {
                val intent = Intent(this, EditProfileActivity::class.java).apply {
                    putExtra("customer_email", it.email)
                    putExtra("contact_name", it.contactName)
                    putExtra("company_name", it.companyName)
                    putExtra("phone_number", it.phoneNumber)
                    putExtra("address", it.address)
                }
                startActivity(intent)
            }
        }
        
        changePasswordButton.setOnClickListener {
            session?.let {
                val intent = Intent(this, ChangePasswordActivity::class.java).apply {
                    putExtra("customer_email", it.email)
                }
                startActivity(intent)
            }
        }
        
        logoutButton.setOnClickListener {
            // Clear session and return to login
            finish()
        }
    }
    
    private fun displayProfile() {
        session?.let {
            displayNameText.text = it.displayName
            emailText.text = it.email
            customerTypeText.text = it.customerType.displayName
        }
    }
}

