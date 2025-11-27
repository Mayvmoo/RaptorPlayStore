package com.example.raptor.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Change Password Activity
 * Komt overeen met iOS ChangePasswordView
 */
class ChangePasswordActivity : AppCompatActivity() {
    
    private lateinit var currentPasswordInput: TextInputEditText
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    
    private var customerEmail: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        
        customerEmail = intent.getStringExtra("customer_email")
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        currentPasswordInput = findViewById(R.id.currentPasswordInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        saveButton = findViewById(R.id.saveButton)
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            changePassword()
        }
    }
    
    private fun changePassword() {
        val currentPassword = currentPasswordInput.text?.toString() ?: ""
        val newPassword = newPasswordInput.text?.toString() ?: ""
        val confirmPassword = confirmPasswordInput.text?.toString() ?: ""
        
        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Huidig wachtwoord is verplicht", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (newPassword.length < 6) {
            Toast.makeText(this, "Nieuw wachtwoord moet minimaal 6 tekens bevatten", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Wachtwoorden komen niet overeen", Toast.LENGTH_SHORT).show()
            return
        }
        
        // TODO: Implement password change via API
        Toast.makeText(this, "Wachtwoord gewijzigd", Toast.LENGTH_SHORT).show()
        finish()
    }
}

