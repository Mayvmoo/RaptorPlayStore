package com.example.raptor

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.raptor.models.CustomerType
import com.example.raptor.viewmodels.AuthState
import com.example.raptor.viewmodels.CustomerAuthViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: CustomerAuthViewModel
    
    // UI Components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private var contactNameEditText: EditText? = null
    private var companyNameEditText: EditText? = null
    private var phoneNumberEditText: EditText? = null
    private var addressEditText: EditText? = null
    private var customerTypeRadioGroup: RadioGroup? = null
    private var registerButton: Button? = null
    private lateinit var loginButton: View
    private lateinit var loginButtonText: TextView
    private lateinit var loginButtonIcon: android.widget.ImageView
    private lateinit var loginProgressBar: ProgressBar
    private lateinit var forgotPasswordButton: TextView
    private lateinit var registerLink: View
    private lateinit var rememberMeCheckBox: android.widget.ImageView
    private var rememberMeChecked = false
    private lateinit var statusTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Pre-inflate layout on background thread for faster startup
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CustomerAuthViewModel::class.java]
        
        // Initialize UI (defer heavy operations)
        initializeViews()
        setupObservers()
        setupClickListeners()
        
        // Defer non-critical initialization
        window.decorView.post {
            // Initialize hidden fields only when needed (lazy loading)
            // This improves initial startup time
        }
    }
    
    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButtonContainer)
        loginButtonText = findViewById(R.id.loginButtonText)
        loginButtonIcon = findViewById(R.id.loginButtonIcon)
        loginProgressBar = findViewById(R.id.loginProgressBar)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        registerLink = findViewById(R.id.registerLink)
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)
        statusTextView = findViewById(R.id.statusTextView)
        
        // Hidden fields will be loaded lazily when needed
    }
    
    private fun ensureHiddenFieldsLoaded() {
        if (contactNameEditText == null) {
            try {
                val stub = findViewById<android.view.ViewStub>(R.id.hiddenFieldsStub)
                stub?.inflate()
                contactNameEditText = findViewById(R.id.contactNameEditText)
                companyNameEditText = findViewById(R.id.companyNameEditText)
                phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
                addressEditText = findViewById(R.id.addressEditText)
                customerTypeRadioGroup = findViewById(R.id.customerTypeRadioGroup)
                registerButton = findViewById(R.id.registerButton)
            } catch (e: Exception) {
                // ViewStub already inflated or not found
                contactNameEditText = findViewById(R.id.contactNameEditText)
                companyNameEditText = findViewById(R.id.companyNameEditText)
                phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
                addressEditText = findViewById(R.id.addressEditText)
                customerTypeRadioGroup = findViewById(R.id.customerTypeRadioGroup)
                registerButton = findViewById(R.id.registerButton)
            }
        }
        
        // Setup focus listeners for input fields
        setupFocusListeners()
        
        // Enable hardware acceleration for better performance
        emailEditText.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        passwordEditText.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        loginButton.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Remember me checkbox click listener
        rememberMeCheckBox.setOnClickListener {
            rememberMeChecked = !rememberMeChecked
            updateRememberMeCheckbox()
        }
        
        // Auto-focus email field (defer to avoid blocking UI)
        emailEditText.post {
            emailEditText.requestFocus()
        }
    }
    
    private fun updateRememberMeCheckbox() {
        rememberMeCheckBox.setImageResource(
            if (rememberMeChecked) R.drawable.ic_checkmark_square_fill else R.drawable.ic_square
        )
        rememberMeCheckBox.setColorFilter(
            ContextCompat.getColor(
                this,
                if (rememberMeChecked) R.color.light_gold else R.color.white_opacity_60
            )
        )
    }
    
    private fun setupFocusListeners() {
        // Email field focus listener - matching iOS focus states
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            emailEditText.background = if (hasFocus) {
                resources.getDrawable(R.drawable.input_field_focused, theme)
            } else {
                resources.getDrawable(R.drawable.input_field_background, theme)
            }
            // Animate the change smoothly
            emailEditText.animate().scaleX(if (hasFocus) 1.01f else 1.0f)
                .scaleY(if (hasFocus) 1.01f else 1.0f)
                .setDuration(200)
                .start()
        }
        
        // Password field focus listener - matching iOS focus states
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            passwordEditText.background = if (hasFocus) {
                resources.getDrawable(R.drawable.input_field_focused, theme)
            } else {
                resources.getDrawable(R.drawable.input_field_background, theme)
            }
            // Animate the change smoothly
            passwordEditText.animate().scaleX(if (hasFocus) 1.01f else 1.0f)
                .scaleY(if (hasFocus) 1.01f else 1.0f)
                .setDuration(200)
                .start()
        }
        
        // Update button state when text changes
        emailEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        passwordEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    private fun updateLoginButtonState() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val isEnabled = email.isNotEmpty() && password.isNotEmpty()
        
        loginButton.isEnabled = isEnabled
        loginButton.isClickable = isEnabled
        loginButton.alpha = if (isEnabled) 1.0f else 0.6f
        loginButton.background = if (isEnabled) {
            resources.getDrawable(R.drawable.button_primary_enabled, theme)
        } else {
            resources.getDrawable(R.drawable.button_primary_disabled, theme)
        }
    }
    
    private fun setupObservers() {
        // Observe authentication state
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Idle -> {
                    statusTextView.visibility = View.GONE
                    setLoading(false)
                }
                is AuthState.Loading -> {
                    statusTextView.visibility = View.GONE
                    setLoading(true)
                }
                is AuthState.Success -> {
                    val session = state.session
                    setLoading(false)
                    
                    // Show success message
                    val welcomeMessage = "Welkom terug, ${session?.displayName ?: "gebruiker"}!"
                    Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
                    
                    // Navigate to Order List after successful login/register
                    session?.let {
                        navigateToOrderList(it.email)
                    }
                }
                is AuthState.Error -> {
                    setLoading(false)
                    // Show error in status text view
                    statusTextView.text = "❌ ${state.message}"
                    statusTextView.visibility = View.VISIBLE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is AuthState.PasswordResetSent -> {
                    setLoading(false)
                    Toast.makeText(this, "Password reset email verzonden", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Observe current session
        viewModel.currentSession.observe(this) { session ->
            if (session != null) {
                // User is logged in - navigate to order list
                navigateToOrderList(session.email)
            }
        }
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            loginUser()
        }
        
        forgotPasswordButton.setOnClickListener {
            forgotPassword()
        }
        
        registerLink.setOnClickListener {
            val intent = android.content.Intent(this, com.example.raptor.ui.auth.CustomerRegisterActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun registerUser() {
        // Lazy load hidden fields if not already loaded
        ensureHiddenFieldsLoaded()
        
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val contactName = contactNameEditText?.text?.toString()?.trim() ?: ""
        val companyName = companyNameEditText?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val phoneNumber = phoneNumberEditText?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val address = addressEditText?.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        
        // Get selected customer type
        val selectedRadioId = customerTypeRadioGroup?.checkedRadioButtonId ?: R.id.radioIndividual
        val customerType = if (selectedRadioId == R.id.radioBusiness) {
            CustomerType.BUSINESS
        } else {
            CustomerType.INDIVIDUAL
        }
        
        // Validation
        if (email.isEmpty() || password.isEmpty() || contactName.isEmpty()) {
            Toast.makeText(this, "Vul minimaal email, wachtwoord en contactnaam in", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (customerType == CustomerType.BUSINESS && companyName.isNullOrEmpty()) {
            Toast.makeText(this, "Bedrijfsnaam is verplicht voor zakelijke accounts", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Register
        viewModel.register(
            email = email,
            password = password,
            customerType = customerType,
            companyName = companyName,
            contactName = contactName,
            phoneNumber = phoneNumber,
            address = address
        )
    }
    
    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vul email en wachtwoord in", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.authenticate(email, password)
    }
    
    private fun forgotPassword() {
        val email = emailEditText.text.toString().trim()
        
        // Open ForgotPasswordActivity with pre-filled email
        val intent = android.content.Intent(this, com.example.raptor.ui.auth.ForgotPasswordActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(intent)
    }
    
    private fun setLoading(loading: Boolean) {
        loginButton.isEnabled = !loading
        loginButton.isClickable = !loading
        
        if (loading) {
            loginProgressBar.visibility = View.VISIBLE
            loginButtonIcon.visibility = View.GONE
            loginButtonText.text = ""
        } else {
            loginProgressBar.visibility = View.GONE
            loginButtonIcon.visibility = View.VISIBLE
            loginButtonText.text = "Inloggen"
        }
        
        loginButton.background = if (loading || !loginButton.isEnabled) {
            resources.getDrawable(R.drawable.button_primary_disabled, theme)
        } else {
            resources.getDrawable(R.drawable.button_primary_enabled, theme)
        }
        
        emailEditText.isEnabled = !loading
        passwordEditText.isEnabled = !loading
        forgotPasswordButton.isEnabled = !loading
        registerLink.isEnabled = !loading
    }
    
    private fun navigateToOrderList(customerEmail: String) {
        val intent = android.content.Intent(this, com.example.raptor.ui.orders.OrderListActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
        }
        startActivity(intent)
    }
}
