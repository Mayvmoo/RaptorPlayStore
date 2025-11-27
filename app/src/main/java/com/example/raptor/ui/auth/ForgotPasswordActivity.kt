package com.example.raptor.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.raptor.R
import com.example.raptor.viewmodels.AuthState
import com.example.raptor.viewmodels.CustomerAuthViewModel

/**
 * Forgot Password Activity
 * Matching iOS ForgotPasswordView exactly
 * Allows user to request password reset via email
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var viewModel: CustomerAuthViewModel

    // UI Components
    private lateinit var emailInput: EditText
    private lateinit var resetButton: Button
    private lateinit var resetButtonText: TextView
    private lateinit var resetButtonProgress: ProgressBar
    
    // Success view components
    private lateinit var formContainer: LinearLayout
    private lateinit var successContainer: LinearLayout
    private lateinit var successEmailText: TextView
    private lateinit var closeSuccessButton: Button

    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Get pre-filled email from intent
        val prefilledEmail = intent.getStringExtra("email") ?: ""

        viewModel = ViewModelProvider(this)[CustomerAuthViewModel::class.java]

        initializeViews()
        setupObservers()
        setupClickListeners()

        // Pre-fill email if provided
        if (prefilledEmail.isNotEmpty()) {
            emailInput.setText(prefilledEmail)
        }
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        resetButton = findViewById(R.id.resetButton)
        resetButtonText = findViewById(R.id.resetButtonText)
        resetButtonProgress = findViewById(R.id.resetButtonProgress)
        
        formContainer = findViewById(R.id.formContainer)
        successContainer = findViewById(R.id.successContainer)
        successEmailText = findViewById(R.id.successEmailText)
        closeSuccessButton = findViewById(R.id.closeSuccessButton)

        // Close button
        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    setLoading(false)
                }
                is AuthState.Loading -> {
                    setLoading(true)
                }
                is AuthState.PasswordResetSent -> {
                    setLoading(false)
                    showSuccessView()
                }
                is AuthState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        resetButton.setOnClickListener {
            requestPasswordReset()
        }

        closeSuccessButton.setOnClickListener {
            finish()
        }
    }

    private fun requestPasswordReset() {
        if (isLoading) return

        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Voer je e-mailadres in", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Ongeldig e-mailadres", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.requestPasswordReset(email)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        resetButtonProgress.visibility = if (loading) View.VISIBLE else View.GONE
        resetButtonText.visibility = if (loading) View.GONE else View.VISIBLE
        resetButton.isEnabled = !loading
        resetButton.alpha = if (loading) 0.6f else 1.0f
        emailInput.isEnabled = !loading
    }

    private fun showSuccessView() {
        val email = emailInput.text.toString().trim()
        successEmailText.text = "We hebben een wachtwoord reset link gestuurd naar:\n$email"
        
        formContainer.visibility = View.GONE
        successContainer.visibility = View.VISIBLE
    }
}

