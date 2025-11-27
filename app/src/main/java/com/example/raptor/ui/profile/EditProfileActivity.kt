package com.example.raptor.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.raptor.R
import com.example.raptor.models.CustomerSession
import com.example.raptor.viewmodels.CustomerAuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Edit Profile Activity
 * Komt overeen met iOS EditProfileView
 */
class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var viewModel: CustomerAuthViewModel
    private lateinit var contactNameInput: TextInputEditText
    private lateinit var companyNameInput: TextInputEditText
    private lateinit var phoneNumberInput: TextInputEditText
    private lateinit var addressInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    
    private var customerEmail: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        customerEmail = intent.getStringExtra("customer_email")
        viewModel = ViewModelProvider(this)[CustomerAuthViewModel::class.java]
        
        initializeViews()
        loadProfileData()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        contactNameInput = findViewById(R.id.contactNameInput)
        companyNameInput = findViewById(R.id.companyNameInput)
        phoneNumberInput = findViewById(R.id.phoneNumberInput)
        addressInput = findViewById(R.id.addressInput)
        saveButton = findViewById(R.id.saveButton)
    }
    
    private fun loadProfileData() {
        // Load from intent (in production, fetch from API)
        contactNameInput.setText(intent.getStringExtra("contact_name") ?: "")
        companyNameInput.setText(intent.getStringExtra("company_name") ?: "")
        phoneNumberInput.setText(intent.getStringExtra("phone_number") ?: "")
        addressInput.setText(intent.getStringExtra("address") ?: "")
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveProfile()
        }
    }
    
    private fun saveProfile() {
        val contactName = contactNameInput.text?.toString()?.trim() ?: ""
        val companyName = companyNameInput.text?.toString()?.trim()
        val phoneNumber = phoneNumberInput.text?.toString()?.trim()
        val address = addressInput.text?.toString()?.trim()
        
        if (contactName.isEmpty()) {
            Toast.makeText(this, "Contactnaam is verplicht", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Implement update via API
        Toast.makeText(this, "Profiel bijgewerkt", Toast.LENGTH_SHORT).show()
        finish()
    }
}

