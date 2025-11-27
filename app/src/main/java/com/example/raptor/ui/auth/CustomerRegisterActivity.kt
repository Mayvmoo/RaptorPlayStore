package com.example.raptor.ui.auth

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.raptor.R
import com.example.raptor.models.CustomerType
import com.example.raptor.ui.orders.OrderListActivity
import com.example.raptor.viewmodels.AuthState
import com.example.raptor.viewmodels.CustomerAuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

/**
 * Customer Registration Activity
 * Multi-step registration form matching iOS CustomerRegisterView exactly
 * 
 * For Business: Account Type -> (Personal Info Subview) -> (Company Info Subview) -> (Terms Subview)
 * For Individual: Account Type -> Personal Info -> Terms
 */
enum class RegistrationStep(val title: String, val icon: String) {
    ACCOUNT_TYPE("Type", "👥"),
    PERSONAL_INFO("Persoonlijk", "👤"),
    COMPANY_INFO("Bedrijf", "🏢"),
    ADDRESS_INFO("Adres", "📍"),
    TERMS("Voorwaarden", "📄");
    
    companion object {
        fun visibleSteps(customerType: CustomerType?): List<RegistrationStep> {
            return when (customerType) {
                CustomerType.BUSINESS -> listOf(ACCOUNT_TYPE, COMPANY_INFO, TERMS)
                CustomerType.INDIVIDUAL -> listOf(ACCOUNT_TYPE, PERSONAL_INFO, TERMS)
                null -> listOf(ACCOUNT_TYPE)
            }
        }
    }
}

class CustomerRegisterActivity : AppCompatActivity() {
    
    private lateinit var viewModel: CustomerAuthViewModel
    
    // Step management
    private var currentStep: RegistrationStep = RegistrationStep.ACCOUNT_TYPE
    private var visibleSteps: List<RegistrationStep> = listOf(RegistrationStep.ACCOUNT_TYPE)
    
    // Step 0: Account Type
    private var selectedCustomerType: CustomerType? = null
    private var showPersonalInfoSubview = false
    private var showCompanyInfoSubview = false
    private var showTermsSubview = false
    
    // Step 1: Personal Info (voor particulier en subview voor zakelijk)
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var contactNameEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    
    // Thuisadres (voor particulier)
    private var homeStreetAddressEditText: EditText? = null
    private var homeHouseNumberEditText: EditText? = null
    private var homePostalCodeEditText: EditText? = null
    private var homeCityEditText: EditText? = null
    
    // Werkadres (voor particulier)
    private var workStreetAddressEditText: EditText? = null
    private var workHouseNumberEditText: EditText? = null
    private var workPostalCodeEditText: EditText? = null
    private var workCityEditText: EditText? = null
    
    // Step 2: Company Info (alleen voor zakelijk)
    private lateinit var companyNameEditText: EditText
    private var companyStreetAddressEditText: EditText? = null
    private var companyHouseNumberEditText: EditText? = null
    private var kvkNumberEditText: EditText? = null
    private var btwNumberEditText: EditText? = null
    
    // Step 3: Terms (subview)
    private lateinit var termsCheckBox: CheckBox
    private var termsPhoneNumberEditText: EditText? = null
    private lateinit var verificationCodeEditText: EditText
    private var isCodeSent = false
    private var isSendingCode = false
    private var isVerifyingCode = false
    private var verificationCode = ""
    
    // UI Components
    private lateinit var progressContainer: LinearLayout
    private lateinit var stepContentContainer: LinearLayout
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var loadingIndicator: ProgressBar
    
    // Step containers
    private lateinit var step0Container: View
    private lateinit var step1Container: View
    private lateinit var step2Container: View
    private lateinit var step3Container: View
    
    // Subview containers (overlay op step0)
    private lateinit var personalInfoSubviewContainer: View
    private lateinit var companyInfoSubviewContainer: View
    private lateinit var termsSubviewContainer: View
    
    // Password strength
    private var passwordStrengthIndicator: TextView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_register)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[CustomerAuthViewModel::class.java]
        
        // Initialize UI
        initializeViews()
        setupObservers()
        setupClickListeners()
        
        // Show first step
        showStep(RegistrationStep.ACCOUNT_TYPE)
    }
    
    private fun goToNextStep() {
        val currentIndex = visibleSteps.indexOf(currentStep)
        if (currentIndex < visibleSteps.size - 1) {
            val nextStep = visibleSteps[currentIndex + 1]
            
            // Special handling for business flow
            if (selectedCustomerType == CustomerType.BUSINESS) {
                when (currentStep) {
                    RegistrationStep.ACCOUNT_TYPE -> {
                        if (showPersonalInfoSubview) {
                            // Move from personal info subview to company info subview
                            showPersonalInfoSubview = false
                            showCompanyInfoSubview = true
                            showSubviews()
                        } else if (showCompanyInfoSubview) {
                            // Move from company info subview to terms subview
                            showCompanyInfoSubview = false
                            showTermsSubview = true
                            showSubviews()
                        }
                    }
                    else -> showStep(nextStep)
                }
            } else {
                // Individual flow
                when (currentStep) {
                    RegistrationStep.ACCOUNT_TYPE -> {
                        showStep(RegistrationStep.PERSONAL_INFO)
                    }
                    RegistrationStep.PERSONAL_INFO -> {
                        showTermsSubview = true
                        showSubviews()
                    }
                    else -> showStep(nextStep)
                }
            }
        }
    }
    
    private fun goToPreviousStep() {
        // Handle subview navigation
        if (showTermsSubview) {
            if (selectedCustomerType == CustomerType.BUSINESS) {
                showTermsSubview = false
                showCompanyInfoSubview = true
                showSubviews()
            } else {
                showTermsSubview = false
                showStep(RegistrationStep.PERSONAL_INFO)
            }
        } else if (showCompanyInfoSubview) {
            showCompanyInfoSubview = false
            showPersonalInfoSubview = true
            showSubviews()
        } else if (showPersonalInfoSubview) {
            showPersonalInfoSubview = false
            showSubviews()
        } else {
            val currentIndex = visibleSteps.indexOf(currentStep)
            if (currentIndex > 0) {
                showStep(visibleSteps[currentIndex - 1])
            }
        }
    }
    
    private fun isLastStep(): Boolean {
        if (showTermsSubview) return true
        val currentIndex = visibleSteps.indexOf(currentStep)
        return currentIndex == visibleSteps.size - 1
    }
    
    private fun initializeViews() {
        progressContainer = findViewById(R.id.progressContainer)
        stepContentContainer = findViewById(R.id.stepContentContainer)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        
        // Step containers
        step0Container = findViewById(R.id.step0Container)
        step1Container = findViewById(R.id.step1Container)
        step2Container = findViewById(R.id.step2Container)
        step3Container = findViewById(R.id.step3Container)
        
        // Subview containers
        personalInfoSubviewContainer = findViewById(R.id.personalInfoSubviewContainer)
        companyInfoSubviewContainer = findViewById(R.id.companyInfoSubviewContainer)
        termsSubviewContainer = findViewById(R.id.termsSubviewContainer)
        
        // Step 1 fields (Personal Info)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        contactNameEditText = findViewById(R.id.contactNameEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        
        // Thuisadres fields - optional, may not exist in layout
        // homeStreetAddressEditText = findViewById(R.id.homeStreetAddressEditText)
        // homeHouseNumberEditText = findViewById(R.id.homeHouseNumberEditText)
        // homePostalCodeEditText = findViewById(R.id.homePostalCodeEditText)
        // homeCityEditText = findViewById(R.id.homeCityEditText)
        
        // Werkadres fields - optional, may not exist in layout
        // workStreetAddressEditText = findViewById(R.id.workStreetAddressEditText)
        // workHouseNumberEditText = findViewById(R.id.workHouseNumberEditText)
        // workPostalCodeEditText = findViewById(R.id.workPostalCodeEditText)
        // workCityEditText = findViewById(R.id.workCityEditText)
        
        // Step 2 fields (Company Info - alleen voor zakelijk)
        companyNameEditText = findViewById(R.id.companyNameEditText)
        // companyStreetAddressEditText = findViewById(R.id.companyStreetAddressEditText)
        // companyHouseNumberEditText = findViewById(R.id.companyHouseNumberEditText)
        // kvkNumberEditText = findViewById(R.id.kvkNumberEditText)
        // btwNumberEditText = findViewById(R.id.btwNumberEditText)
        
        // Step 3 fields (Terms)
        termsCheckBox = findViewById(R.id.termsCheckBox)
        // termsPhoneNumberEditText = findViewById(R.id.termsPhoneNumberEditText)
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText)
        
        // Password strength indicator - optional
        // passwordStrengthIndicator = findViewById(R.id.passwordStrengthIndicator)
        
        // Bind subview fields (these are in the included layouts)
        // Personal info subview fields are accessed via findViewById in the subview container
        // Company info subview fields are accessed via findViewById in the subview container
        // Terms subview fields are accessed via findViewById in the subview container
        
        // Setup text watchers for password strength
        setupPasswordStrengthIndicator()
        
        // Setup postcode lookup
        setupPostcodeLookup()
        
        // Setup phone verification
        setupPhoneVerification()
        
        // Setup subview password strength
        findViewById<EditText>(R.id.subviewPasswordEditText)?.let {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updatePasswordStrength(s?.toString() ?: "", findViewById(R.id.subviewPasswordStrengthIndicator))
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
        
        // Enable hardware acceleration
        stepContentContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        personalInfoSubviewContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        companyInfoSubviewContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        termsSubviewContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
    
    private fun updatePasswordStrength(password: String, indicator: TextView?) {
        if (indicator == null) return
        val strength = calculatePasswordStrength(password)
        indicator.text = strength.first
        indicator.setTextColor(ContextCompat.getColor(this, strength.second))
        indicator.visibility = if (password.isNotEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun setupPasswordStrengthIndicator() {
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrength(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun updatePasswordStrength(password: String) {
        updatePasswordStrength(password, passwordStrengthIndicator)
    }
    
    private fun calculatePasswordStrength(password: String): Pair<String, Int> {
        return when {
            password.length < 6 -> Pair("Zwak", R.color.red)
            password.length < 8 -> Pair("Gemiddeld", R.color.orange)
            password.matches(Regex(".*[A-Za-z].*")) && password.matches(Regex(".*[0-9].*")) -> 
                Pair("Sterk", R.color.green)
            else -> Pair("Redelijk", R.color.yellow)
        }
    }
    
    private fun setupPostcodeLookup() {
        // Home postal code lookup
        homePostalCodeEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val postalCode = formatPostalCode(s?.toString() ?: "")
                if (postalCode != s?.toString()) {
                    homePostalCodeEditText?.setText(postalCode)
                    homePostalCodeEditText?.setSelection(postalCode.length)
                }
                
                val cleaned = postalCode.replace(" ", "").uppercase()
                if (cleaned.length == 6 && cleaned.matches(Regex("\\d{4}[A-Z]{2}"))) {
                    lookupPostcode(cleaned, isHome = true)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // Work postal code lookup
        workPostalCodeEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val postalCode = formatPostalCode(s?.toString() ?: "")
                if (postalCode != s?.toString()) {
                    workPostalCodeEditText?.setText(postalCode)
                    workPostalCodeEditText?.setSelection(postalCode.length)
                }
                
                val cleaned = postalCode.replace(" ", "").uppercase()
                if (cleaned.length == 6 && cleaned.matches(Regex("\\d{4}[A-Z]{2}"))) {
                    lookupPostcode(cleaned, isHome = false)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun formatPostalCode(input: String): String {
        val cleaned = input.replace(" ", "").uppercase()
        return when {
            cleaned.length <= 4 -> cleaned
            cleaned.length <= 6 -> {
                val digits = cleaned.substring(0, 4)
                val letters = cleaned.substring(4)
                "$digits $letters"
            }
            else -> cleaned.substring(0, 6)
        }
    }
    
    private fun lookupPostcode(postalCode: String, isHome: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: Implement postcode lookup API call
                // For now, just a placeholder
                withContext(Dispatchers.Main) {
                    // Update city field if found
                    // This would be implemented with actual API call
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun setupPhoneVerification() {
        // Send code button (in terms subview)
        Handler(Looper.getMainLooper()).postDelayed({
            findViewById<Button>(R.id.sendVerificationCodeButton)?.setOnClickListener {
                sendVerificationCode()
            }
            
            // Verification code input (in terms subview)
            findViewById<EditText>(R.id.verificationCodeEditText)?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val code = s?.toString()?.filter { it.isDigit() } ?: ""
                    val editText = findViewById<EditText>(R.id.verificationCodeEditText)
                    if (code.length > 6) {
                        editText?.setText(code.substring(0, 6))
                        editText?.setSelection(6)
                    } else {
                        editText?.setText(code)
                        editText?.setSelection(code.length)
                    }
                    
                    // Show register button when code is verified
                    if (code.length == 6 && code == verificationCode) {
                        findViewById<Button>(R.id.registerFromTermsButton)?.visibility = View.VISIBLE
                    } else {
                        findViewById<Button>(R.id.registerFromTermsButton)?.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            
            // Register button in terms subview
            findViewById<Button>(R.id.registerFromTermsButton)?.setOnClickListener {
                if (validateTermsSubview()) {
                    submitRegistration()
                }
            }
        }, 100)
    }
    
    private fun sendVerificationCode() {
        val phoneNumber = findViewById<EditText>(R.id.subviewTermsPhoneNumberEditText)?.text?.toString()?.trim() ?: ""
        if (phoneNumber.isEmpty() || phoneNumber == "+31") {
            Toast.makeText(this, "Voer een geldig telefoonnummer in", Toast.LENGTH_SHORT).show()
            return
        }
        
        isSendingCode = true
        findViewById<Button>(R.id.sendVerificationCodeButton)?.isEnabled = false
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Generate 6-digit code
                verificationCode = String.format("%06d", (100000..999999).random())
                
                // TODO: In production, send via SMS API (Twilio, MessageBird, etc.)
                // For now, log it (in development)
                android.util.Log.d("Raptor", "SMS verificatiecode voor $phoneNumber: $verificationCode")
                
                withContext(Dispatchers.Main) {
                    isSendingCode = false
                    isCodeSent = true
                    updatePhoneVerificationUI()
                    Toast.makeText(this@CustomerRegisterActivity, "Verificatiecode verzonden naar $phoneNumber", Toast.LENGTH_SHORT).show()
                    
                    // DEVELOPMENT ONLY: Auto-fill code
                    if (android.os.Build.TYPE == "eng" || android.os.Build.TYPE == "userdebug") {
                        verificationCodeEditText.setText(verificationCode)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSendingCode = false
                    findViewById<Button>(R.id.sendVerificationCodeButton)?.isEnabled = true
                    Toast.makeText(this@CustomerRegisterActivity, "Kon code niet versturen. Probeer het opnieuw.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updatePhoneVerificationUI() {
        Handler(Looper.getMainLooper()).post {
            val sendButton = findViewById<Button>(R.id.sendVerificationCodeButton)
            val codeInputContainer = findViewById<View>(R.id.verificationCodeContainer)
            val registerButton = findViewById<Button>(R.id.registerFromTermsButton)
            
            if (isCodeSent) {
                sendButton?.visibility = View.GONE
                codeInputContainer?.visibility = View.VISIBLE
                // Show register button when code is entered and verified
                val code = findViewById<EditText>(R.id.verificationCodeEditText)?.text?.toString()?.trim() ?: ""
                if (code.length == 6 && code == verificationCode) {
                    registerButton?.visibility = View.VISIBLE
                } else {
                    registerButton?.visibility = View.GONE
                }
            } else {
                sendButton?.visibility = View.VISIBLE
                codeInputContainer?.visibility = View.GONE
                registerButton?.visibility = View.GONE
            }
        }
    }
    
    fun resendVerificationCode(view: View) {
        isCodeSent = false
        verificationCode = ""
        findViewById<EditText>(R.id.verificationCodeEditText)?.setText("")
        updatePhoneVerificationUI()
        sendVerificationCode()
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
                is AuthState.Success -> {
                    setLoading(false)
                    val session = state.session
                    Toast.makeText(this, "Registratie succesvol!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to Order List
                    session?.let {
                        val intent = android.content.Intent(this, OrderListActivity::class.java).apply {
                            putExtra("customer_email", it.email)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
                is AuthState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, "Fout: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            if (currentStep == RegistrationStep.ACCOUNT_TYPE && !showPersonalInfoSubview && !showCompanyInfoSubview && !showTermsSubview) {
                finish()
            } else {
                goToPreviousStep()
            }
        }
        
        nextButton.setOnClickListener {
            if (validateCurrentStep()) {
                if (isLastStep()) {
                    submitRegistration()
                } else {
                    goToNextStep()
                }
            }
        }
        
        // Account type selection
        findViewById<View>(R.id.businessOption).setOnClickListener {
            selectedCustomerType = CustomerType.BUSINESS
            updateAccountTypeUI()
            // Update visible steps
            visibleSteps = RegistrationStep.visibleSteps(selectedCustomerType)
            updateProgressIndicator()
            // For business: show personal info subview first with animation
            if (!showPersonalInfoSubview && !showCompanyInfoSubview && !showTermsSubview) {
                animateSubviewTransition(
                    from = null,
                    to = personalInfoSubviewContainer,
                    forward = true
                ) {
                    showPersonalInfoSubview = true
                    showSubviews()
                }
            }
        }
        
        findViewById<View>(R.id.individualOption).setOnClickListener {
            selectedCustomerType = CustomerType.INDIVIDUAL
            updateAccountTypeUI()
            // Update visible steps
            visibleSteps = RegistrationStep.visibleSteps(selectedCustomerType)
            updateProgressIndicator()
            // Hide all subviews for individual with animation
            val currentSubview = when {
                personalInfoSubviewContainer.visibility == View.VISIBLE -> personalInfoSubviewContainer
                companyInfoSubviewContainer.visibility == View.VISIBLE -> companyInfoSubviewContainer
                termsSubviewContainer.visibility == View.VISIBLE -> termsSubviewContainer
                else -> null
            }
            
            currentSubview?.let {
                animateSubviewTransition(
                    from = it,
                    to = null,
                    forward = false
                ) {
                    showPersonalInfoSubview = false
                    showCompanyInfoSubview = false
                    showTermsSubview = false
                    showSubviews()
                }
            } ?: run {
                showPersonalInfoSubview = false
                showCompanyInfoSubview = false
                showTermsSubview = false
                showSubviews()
            }
        }
        
        // Subview navigation buttons with smooth animations
        findViewById<Button>(R.id.backFromPersonalInfoButton)?.setOnClickListener {
            animateSubviewTransition(
                from = personalInfoSubviewContainer,
                to = null,
                forward = false
            ) {
                showPersonalInfoSubview = false
                showSubviews()
            }
        }
        
        findViewById<Button>(R.id.continueFromPersonalInfoButton)?.setOnClickListener {
            if (validatePersonalInfoSubview()) {
                animateSubviewTransition(
                    from = personalInfoSubviewContainer,
                    to = companyInfoSubviewContainer,
                    forward = true
                ) {
                    showPersonalInfoSubview = false
                    showCompanyInfoSubview = true
                    showSubviews()
                }
            }
        }
        
        findViewById<Button>(R.id.backFromCompanyInfoButton)?.setOnClickListener {
            animateSubviewTransition(
                from = companyInfoSubviewContainer,
                to = personalInfoSubviewContainer,
                forward = false
            ) {
                showCompanyInfoSubview = false
                showPersonalInfoSubview = true
                showSubviews()
            }
        }
        
        findViewById<Button>(R.id.continueFromCompanyInfoButton)?.setOnClickListener {
            if (validateCompanyInfoSubview()) {
                animateSubviewTransition(
                    from = companyInfoSubviewContainer,
                    to = termsSubviewContainer,
                    forward = true
                ) {
                    showCompanyInfoSubview = false
                    showTermsSubview = true
                    showSubviews()
                }
            }
        }
        
        findViewById<Button>(R.id.backFromTermsButton)?.setOnClickListener {
            if (selectedCustomerType == CustomerType.BUSINESS) {
                animateSubviewTransition(
                    from = termsSubviewContainer,
                    to = companyInfoSubviewContainer,
                    forward = false
                ) {
                    showTermsSubview = false
                    showCompanyInfoSubview = true
                    showSubviews()
                }
            } else {
                showTermsSubview = false
                showStep(RegistrationStep.PERSONAL_INFO)
            }
        }
    }
    
    private fun showStep(step: RegistrationStep) {
        currentStep = step
        
        // Hide all steps
        step0Container.visibility = View.GONE
        step1Container.visibility = View.GONE
        step2Container.visibility = View.GONE
        step3Container.visibility = View.GONE
        
        // Hide all subviews
        showPersonalInfoSubview = false
        showCompanyInfoSubview = false
        showTermsSubview = false
        
        // Show current step
        when (step) {
            RegistrationStep.ACCOUNT_TYPE -> {
                step0Container.visibility = View.VISIBLE
            }
            RegistrationStep.PERSONAL_INFO -> {
                step1Container.visibility = View.VISIBLE
                updatePersonalInfoUI()
            }
            RegistrationStep.COMPANY_INFO -> {
                step2Container.visibility = View.VISIBLE
                updateAddressInfoUI()
            }
            RegistrationStep.TERMS -> {
                step3Container.visibility = View.VISIBLE
            }
            RegistrationStep.ADDRESS_INFO -> {
                // Not used in current flow
            }
        }
        
        // Show subviews if needed (for business flow)
        showSubviews()
        
        // Update progress indicator
        updateProgressIndicator()
        
        // Update buttons
        val isFirstStep = step == RegistrationStep.ACCOUNT_TYPE
        backButton.text = if (isFirstStep) "Annuleren" else "Terug"
        
        val isLastStep = step == visibleSteps.lastOrNull()
        nextButton.text = if (isLastStep) "Registreren" else "Volgende"
    }
    
    private fun showSubviews() {
        // Determine which subview should be shown
        val targetSubview = when {
            showPersonalInfoSubview -> personalInfoSubviewContainer
            showCompanyInfoSubview -> companyInfoSubviewContainer
            showTermsSubview -> termsSubviewContainer
            else -> null
        }
        
        // Get the currently visible subview
        val currentSubview = when {
            personalInfoSubviewContainer.visibility == View.VISIBLE -> personalInfoSubviewContainer
            companyInfoSubviewContainer.visibility == View.VISIBLE -> companyInfoSubviewContainer
            termsSubviewContainer.visibility == View.VISIBLE -> termsSubviewContainer
            else -> null
        }
        
        // Animate transition
        if (targetSubview != null && targetSubview != currentSubview) {
            // Hide current subview with slide out animation
            currentSubview?.let { animateSubviewOut(it) }
            
            // Show new subview with slide in animation
            animateSubviewIn(targetSubview)
        } else if (targetSubview == null && currentSubview != null) {
            // Hide all subviews
            animateSubviewOut(currentSubview) {
                personalInfoSubviewContainer.visibility = View.GONE
                companyInfoSubviewContainer.visibility = View.GONE
                termsSubviewContainer.visibility = View.GONE
            }
        } else if (targetSubview == null) {
            // No subview to show, hide all
            personalInfoSubviewContainer.visibility = View.GONE
            companyInfoSubviewContainer.visibility = View.GONE
            termsSubviewContainer.visibility = View.GONE
        }
    }
    
    private fun animateSubviewIn(targetView: View) {
        // Set initial state
        targetView.alpha = 0f
        targetView.translationX = 400f
        targetView.scaleX = 0.95f
        targetView.scaleY = 0.95f
        targetView.visibility = View.VISIBLE
        
        // Animate in with spring-like effect
        targetView.animate()
            .alpha(1f)
            .translationX(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(350)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withLayer()
            .start()
        
        // Also fade out the account type container slightly
        step0Container.animate()
            .alpha(0.3f)
            .setDuration(300)
            .start()
    }
    
    private fun animateSubviewOut(targetView: View, onComplete: (() -> Unit)? = null) {
        val slideDirection = if (showPersonalInfoSubview || showCompanyInfoSubview || showTermsSubview) {
            // If moving forward, slide out to left
            -400f
        } else {
            // If going back, slide out to right
            400f
        }
        
        targetView.animate()
            .alpha(0f)
            .translationX(slideDirection)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withLayer()
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    targetView.visibility = View.GONE
                    targetView.alpha = 1f
                    targetView.translationX = 0f
                    targetView.scaleX = 1f
                    targetView.scaleY = 1f
                    onComplete?.invoke()
                }
                override fun onAnimationCancel(animation: Animator) {
                    targetView.visibility = View.GONE
                    targetView.alpha = 1f
                    targetView.translationX = 0f
                    targetView.scaleX = 1f
                    targetView.scaleY = 1f
                    onComplete?.invoke()
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
        
        // Restore account type container opacity
        step0Container.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }
    
    /**
     * Smooth transition between subviews with slide and fade animations
     * @param from The subview to hide (null if showing first subview)
     * @param to The subview to show (null if hiding all subviews)
     * @param forward True if moving forward in the flow, false if going back
     * @param onComplete Callback to execute after animation completes
     */
    private fun animateSubviewTransition(
        from: View?,
        to: View?,
        forward: Boolean,
        onComplete: () -> Unit
    ) {
        val slideDistance = 400f
        val slideOutDirection = if (forward) -slideDistance else slideDistance
        val slideInDirection = if (forward) slideDistance else -slideDistance
        
        // Animate out the current subview
        from?.let { fromView ->
            fromView.animate()
                .alpha(0f)
                .translationX(slideOutDirection)
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withLayer()
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        fromView.visibility = View.GONE
                        fromView.alpha = 1f
                        fromView.translationX = 0f
                        fromView.scaleX = 1f
                        fromView.scaleY = 1f
                        
                        // Animate in the new subview
                        to?.let { toView ->
                            animateSubviewIn(toView)
                        } ?: run {
                            // Restore account type container if hiding all
                            step0Container.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .start()
                        }
                        
                        onComplete()
                    }
                    override fun onAnimationCancel(animation: Animator) {
                        fromView.visibility = View.GONE
                        fromView.alpha = 1f
                        fromView.translationX = 0f
                        fromView.scaleX = 1f
                        fromView.scaleY = 1f
                        onComplete()
                    }
                    override fun onAnimationRepeat(animation: Animator) {}
                })
                .start()
        } ?: run {
            // No subview to hide, just show the new one
            to?.let { toView ->
                animateSubviewIn(toView)
                onComplete()
            } ?: run {
                onComplete()
            }
        }
    }
    
    private fun updateProgressIndicator() {
        // Update visible steps based on customer type
        visibleSteps = RegistrationStep.visibleSteps(selectedCustomerType)
        
        // Hide all step indicators first
        val allIndicators = arrayOf(
            findViewById<View>(R.id.step0Indicator),
            findViewById<View>(R.id.step1Indicator),
            findViewById<View>(R.id.step2Indicator),
            findViewById<View>(R.id.step3Indicator)
        )
        allIndicators.forEach { it?.visibility = View.GONE }
        
        // Show and update only visible steps
        visibleSteps.forEachIndexed { index, step ->
            val stepView = when (index) {
                0 -> findViewById<View>(R.id.step0Indicator)
                1 -> findViewById<View>(R.id.step1Indicator)
                2 -> findViewById<View>(R.id.step2Indicator)
                3 -> findViewById<View>(R.id.step3Indicator)
                else -> null
            }
            
            stepView?.let {
                it.visibility = View.VISIBLE
                
                val isActive = currentStep == step || 
                    (step == RegistrationStep.ACCOUNT_TYPE && (showPersonalInfoSubview || showCompanyInfoSubview || showTermsSubview))
                val currentIndex = visibleSteps.indexOf(currentStep)
                val stepIndex = visibleSteps.indexOf(step)
                val isCompleted = currentIndex > stepIndex || 
                    (step == RegistrationStep.ACCOUNT_TYPE && selectedCustomerType == CustomerType.BUSINESS && showCompanyInfoSubview)
                
                val circle = it.findViewById<View>(R.id.stepCircle)
                val icon = it.findViewById<TextView>(R.id.stepIcon)
                val title = it.findViewById<TextView>(R.id.stepTitle)
                
                // Set title and icon
                title.text = step.title
                icon.text = step.icon
                
                if (isCompleted) {
                    circle.setBackgroundResource(R.drawable.step_indicator_completed)
                    icon.text = "✓"
                    icon.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else if (isActive) {
                    circle.setBackgroundResource(R.drawable.step_indicator_active)
                    icon.setTextColor(ContextCompat.getColor(this, R.color.white))
                } else {
                    circle.setBackgroundResource(R.drawable.step_indicator_inactive)
                    icon.setTextColor(ContextCompat.getColor(this, R.color.white_opacity_60))
                }
                
                title.setTextColor(
                    ContextCompat.getColor(
                        this,
                        if (isActive || isCompleted) R.color.white else R.color.white_opacity_60
                    )
                )
            }
        }
    }
    
    private fun updateAccountTypeUI() {
        val businessOption = findViewById<View>(R.id.businessOption)
        val individualOption = findViewById<View>(R.id.individualOption)
        val businessCheck = findViewById<TextView>(R.id.businessCheck)
        val individualCheck = findViewById<TextView>(R.id.individualCheck)
        
        when (selectedCustomerType) {
            CustomerType.BUSINESS -> {
                businessOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_selected)
                individualOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_unselected)
                businessCheck.visibility = View.VISIBLE
                individualCheck.visibility = View.GONE
            }
            CustomerType.INDIVIDUAL -> {
                businessOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_unselected)
                individualOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_selected)
                businessCheck.visibility = View.GONE
                individualCheck.visibility = View.VISIBLE
            }
            null -> {
                businessOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_unselected)
                individualOption.background = ContextCompat.getDrawable(this, R.drawable.account_type_button_unselected)
                businessCheck.visibility = View.GONE
                individualCheck.visibility = View.GONE
            }
        }
    }
    
    private fun updatePersonalInfoUI() {
        // Update labels based on customer type
        val contactLabel = findViewById<TextView>(R.id.contactNameLabel)
        contactLabel.text = if (selectedCustomerType == CustomerType.INDIVIDUAL) {
            "Volledige naam"
        } else {
            "Contactpersoon"
        }
    }
    
    private fun updateAddressInfoUI() {
        // Show/hide company name field based on customer type
        val companyNameContainer = findViewById<View>(R.id.companyNameContainer)
        companyNameContainer.visibility = if (selectedCustomerType == CustomerType.BUSINESS) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    
    private fun validateCurrentStep(): Boolean {
        // Handle subview validation
        if (showPersonalInfoSubview) {
            return validatePersonalInfoSubview()
        }
        if (showCompanyInfoSubview) {
            return validateCompanyInfoSubview()
        }
        if (showTermsSubview) {
            return validateTermsSubview()
        }
        
        // Handle main step validation
        return when (currentStep) {
            RegistrationStep.ACCOUNT_TYPE -> {
                if (selectedCustomerType == null) {
                    Toast.makeText(this, "Selecteer een account type", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }
            RegistrationStep.PERSONAL_INFO -> {
                validatePersonalInfoStep()
            }
            RegistrationStep.COMPANY_INFO -> {
                validateCompanyInfoStep()
            }
            RegistrationStep.TERMS -> {
                if (!termsCheckBox.isChecked) {
                    Toast.makeText(this, "Accepteer de voorwaarden om door te gaan", Toast.LENGTH_SHORT).show()
                    false
                } else {
                    true
                }
            }
            RegistrationStep.ADDRESS_INFO -> true
        }
    }
    
    private fun validatePersonalInfoStep(): Boolean {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val contactName = contactNameEditText.text.toString().trim()
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Voer een geldig e-mailadres in", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Wachtwoord moet minimaal 6 tekens bevatten", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Wachtwoorden komen niet overeen", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contactName.isEmpty()) {
            Toast.makeText(this, "Vul je naam in", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validatePersonalInfoSubview(): Boolean {
        // Get subview fields
        val email = findViewById<EditText>(R.id.subviewEmailEditText)?.text?.toString()?.trim() ?: ""
        val password = findViewById<EditText>(R.id.subviewPasswordEditText)?.text?.toString()?.trim() ?: ""
        val confirmPassword = findViewById<EditText>(R.id.subviewConfirmPasswordEditText)?.text?.toString()?.trim() ?: ""
        val contactName = findViewById<EditText>(R.id.subviewContactNameEditText)?.text?.toString()?.trim() ?: ""
        
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Voer een geldig e-mailadres in", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Wachtwoord moet minimaal 6 tekens bevatten", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Wachtwoorden komen niet overeen", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contactName.isEmpty()) {
            Toast.makeText(this, "Vul de naam van de contactpersoon in", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validateCompanyInfoStep(): Boolean {
        val companyName = companyNameEditText.text.toString().trim()
        val street = companyStreetAddressEditText?.text?.toString()?.trim() ?: ""
        val houseNumber = companyHouseNumberEditText?.text?.toString()?.trim() ?: ""
        
        if (companyName.isEmpty()) {
            Toast.makeText(this, "Vul de bedrijfsnaam in", Toast.LENGTH_SHORT).show()
            return false
        }
        if (street.isEmpty() || houseNumber.isEmpty()) {
            Toast.makeText(this, "Vul het bedrijfsadres in", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validateCompanyInfoSubview(): Boolean {
        val companyName = findViewById<EditText>(R.id.subviewCompanyNameEditText)?.text?.toString()?.trim() ?: ""
        val street = findViewById<EditText>(R.id.subviewCompanyStreetAddressEditText)?.text?.toString()?.trim() ?: ""
        val houseNumber = findViewById<EditText>(R.id.subviewCompanyHouseNumberEditText)?.text?.toString()?.trim() ?: ""
        
        if (companyName.isEmpty()) {
            Toast.makeText(this, "Vul de bedrijfsnaam in", Toast.LENGTH_SHORT).show()
            return false
        }
        if (street.isEmpty() || houseNumber.isEmpty()) {
            Toast.makeText(this, "Vul het bedrijfsadres in", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun validateTermsSubview(): Boolean {
        val termsCheck = findViewById<CheckBox>(R.id.subviewTermsCheckBox)
        if (termsCheck?.isChecked != true) {
            Toast.makeText(this, "Accepteer de voorwaarden om door te gaan", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // If code is sent, verify code
        if (isCodeSent) {
            val code = findViewById<EditText>(R.id.verificationCodeEditText)?.text?.toString()?.trim() ?: ""
            if (code.length != 6) {
                Toast.makeText(this, "Voer de 6-cijferige code in", Toast.LENGTH_SHORT).show()
                return false
            }
            if (code != verificationCode) {
                Toast.makeText(this, "Verificatiecode is onjuist", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        
        return true
    }
    
    private fun submitRegistration() {
        // Get data based on customer type and current flow
        val email: String
        val password: String
        val contactName: String
        val phoneNumber: String?
        val companyName: String?
        val address: String
        
        if (selectedCustomerType == CustomerType.BUSINESS) {
            // Business flow: use subview fields
            email = findViewById<EditText>(R.id.subviewEmailEditText)?.text?.toString()?.trim() ?: ""
            password = findViewById<EditText>(R.id.subviewPasswordEditText)?.text?.toString()?.trim() ?: ""
            contactName = findViewById<EditText>(R.id.subviewContactNameEditText)?.text?.toString()?.trim() ?: ""
            
            val companyStreet = findViewById<EditText>(R.id.subviewCompanyStreetAddressEditText)?.text?.toString()?.trim() ?: ""
            val companyHouse = findViewById<EditText>(R.id.subviewCompanyHouseNumberEditText)?.text?.toString()?.trim() ?: ""
            val companyPostal = findViewById<EditText>(R.id.subviewCompanyPostalCodeEditText)?.text?.toString()?.trim() ?: ""
            val companyCity = findViewById<EditText>(R.id.subviewCompanyCityEditText)?.text?.toString()?.trim() ?: ""
            
            companyName = findViewById<EditText>(R.id.subviewCompanyNameEditText)?.text?.toString()?.trim()
            phoneNumber = findViewById<EditText>(R.id.subviewTermsPhoneNumberEditText)?.text?.toString()?.trim().takeIf { it?.isNotEmpty() == true && it != "+31" }
            
            address = if (companyStreet.isNotEmpty() && companyHouse.isNotEmpty()) {
                "$companyStreet $companyHouse, $companyPostal $companyCity"
            } else {
                ""
            }
        } else {
            // Individual flow: use main step fields
            email = emailEditText.text.toString().trim()
            password = passwordEditText.text.toString().trim()
            contactName = contactNameEditText.text.toString().trim()
            phoneNumber = phoneNumberEditText.text.toString().trim().takeIf { it.isNotEmpty() && it != "+31" }
            companyName = null
            
            // Use home address for individual
            val homeStreet = homeStreetAddressEditText?.text?.toString()?.trim() ?: ""
            val homeHouse = homeHouseNumberEditText?.text?.toString()?.trim() ?: ""
            val homePostal = homePostalCodeEditText?.text?.toString()?.trim() ?: ""
            val homeCity = homeCityEditText?.text?.toString()?.trim() ?: ""
            
            address = if (homeStreet.isNotEmpty() && homeHouse.isNotEmpty()) {
                "$homeStreet $homeHouse, $homePostal $homeCity"
            } else {
                ""
            }
        }
        
        viewModel.register(
            email = email,
            password = password,
            customerType = selectedCustomerType ?: CustomerType.INDIVIDUAL,
            companyName = companyName,
            contactName = contactName,
            phoneNumber = phoneNumber,
            address = address
        )
    }
    
    private fun setLoading(loading: Boolean) {
        loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        nextButton.isEnabled = !loading
        backButton.isEnabled = !loading
    }
}

