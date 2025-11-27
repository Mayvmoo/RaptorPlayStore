package com.example.raptor.ui.payment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.raptor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Payment Activity
 * Matching iOS PaymentView exactly
 * Supports: iDEAL, PayPal, Google Pay (instead of Apple Pay), Credit Card
 */
class PaymentActivity : AppCompatActivity() {

    // Payment methods
    enum class PaymentMethod(val displayName: String, val description: String) {
        IDEAL("iDEAL", "Betaal direct via je bank"),
        PAYPAL("PayPal", "Betaal met je PayPal account"),
        GOOGLE_PAY("Google Pay", "Betaal snel en veilig"),
        CREDIT_CARD("Creditcard", "Visa, Mastercard, Amex")
    }

    private var selectedPaymentMethod: PaymentMethod = PaymentMethod.IDEAL
    private var orderTotal: Double = 0.0
    private var deliveryMode: String = "standard"
    private var isProcessing: Boolean = false

    // UI Components
    private lateinit var totalAmountText: TextView
    private lateinit var deliveryModeText: TextView
    private lateinit var deliveryModeIcon: ImageView

    // Payment method cards
    private lateinit var idealCard: LinearLayout
    private lateinit var paypalCard: LinearLayout
    private lateinit var googlePayCard: LinearLayout
    private lateinit var creditCardCard: LinearLayout

    // Credit card form
    private lateinit var creditCardFormContainer: LinearLayout
    private lateinit var cardNumberInput: EditText
    private lateinit var cardHolderNameInput: EditText
    private lateinit var expiryDateInput: EditText
    private lateinit var cvvInput: EditText

    // Pay button
    private lateinit var payButton: Button
    private lateinit var payButtonText: TextView
    private lateinit var payButtonProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Get data from intent
        orderTotal = intent.getDoubleExtra("order_total", 0.0)
        deliveryMode = intent.getStringExtra("delivery_mode") ?: "standard"

        initializeViews()
        setupPaymentMethodCards()
        setupCreditCardForm()
        setupPayButton()
        updateUI()
    }

    private fun initializeViews() {
        // Header
        totalAmountText = findViewById(R.id.totalAmountText)
        deliveryModeText = findViewById(R.id.deliveryModeText)
        deliveryModeIcon = findViewById(R.id.deliveryModeIcon)

        // Payment method cards
        idealCard = findViewById(R.id.idealCard)
        paypalCard = findViewById(R.id.paypalCard)
        googlePayCard = findViewById(R.id.googlePayCard)
        creditCardCard = findViewById(R.id.creditCardCard)

        // Credit card form
        creditCardFormContainer = findViewById(R.id.creditCardFormContainer)
        cardNumberInput = findViewById(R.id.cardNumberInput)
        cardHolderNameInput = findViewById(R.id.cardHolderNameInput)
        expiryDateInput = findViewById(R.id.expiryDateInput)
        cvvInput = findViewById(R.id.cvvInput)

        // Pay button
        payButton = findViewById(R.id.payButton)
        payButtonText = findViewById(R.id.payButtonText)
        payButtonProgress = findViewById(R.id.payButtonProgress)

        // Close button
        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }

        // Set header values
        totalAmountText.text = String.format("€ %.2f", orderTotal)
        deliveryModeText.text = if (deliveryMode == "express") "Express Delivery" else "Normal Delivery"
        deliveryModeIcon.setImageResource(
            if (deliveryMode == "express") android.R.drawable.ic_menu_send
            else android.R.drawable.ic_menu_share
        )
    }

    private fun setupPaymentMethodCards() {
        idealCard.setOnClickListener { selectPaymentMethod(PaymentMethod.IDEAL) }
        paypalCard.setOnClickListener { selectPaymentMethod(PaymentMethod.PAYPAL) }
        googlePayCard.setOnClickListener { selectPaymentMethod(PaymentMethod.GOOGLE_PAY) }
        creditCardCard.setOnClickListener { selectPaymentMethod(PaymentMethod.CREDIT_CARD) }

        // Initial selection
        selectPaymentMethod(PaymentMethod.IDEAL)
    }

    private fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method
        updatePaymentMethodUI()
        updatePayButtonText()

        // Show/hide credit card form
        creditCardFormContainer.visibility = if (method == PaymentMethod.CREDIT_CARD) View.VISIBLE else View.GONE
    }

    private fun updatePaymentMethodUI() {
        // Reset all cards
        listOf(idealCard, paypalCard, googlePayCard, creditCardCard).forEach { card ->
            card.background = ContextCompat.getDrawable(this, R.drawable.payment_method_unselected)
            card.findViewById<View>(R.id.checkIndicator)?.visibility = View.GONE
        }

        // Highlight selected card
        val selectedCard = when (selectedPaymentMethod) {
            PaymentMethod.IDEAL -> idealCard
            PaymentMethod.PAYPAL -> paypalCard
            PaymentMethod.GOOGLE_PAY -> googlePayCard
            PaymentMethod.CREDIT_CARD -> creditCardCard
        }

        selectedCard.background = ContextCompat.getDrawable(this, R.drawable.payment_method_selected)
        selectedCard.findViewById<View>(R.id.checkIndicator)?.visibility = View.VISIBLE
    }

    private fun setupCreditCardForm() {
        // Card number formatting
        cardNumberInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val formatted = formatCardNumber(s?.toString() ?: "")
                cardNumberInput.setText(formatted)
                cardNumberInput.setSelection(formatted.length)
                isFormatting = false
                updatePayButtonState()
            }
        })

        // Expiry date formatting (MM/YY)
        expiryDateInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val formatted = formatExpiryDate(s?.toString() ?: "")
                expiryDateInput.setText(formatted)
                expiryDateInput.setSelection(formatted.length)
                isFormatting = false
                updatePayButtonState()
            }
        })

        // CVV limit
        cvvInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > 4) {
                    cvvInput.setText(s?.substring(0, 4))
                    cvvInput.setSelection(4)
                }
                updatePayButtonState()
            }
        })

        cardHolderNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePayButtonState()
            }
        })
    }

    private fun formatCardNumber(input: String): String {
        val cleaned = input.replace(" ", "").take(16)
        return cleaned.chunked(4).joinToString(" ")
    }

    private fun formatExpiryDate(input: String): String {
        val cleaned = input.replace("/", "").take(4)
        return if (cleaned.length >= 2) {
            "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
        } else {
            cleaned
        }
    }

    private fun setupPayButton() {
        payButton.setOnClickListener {
            processPayment()
        }
    }

    private fun updatePayButtonText() {
        payButtonText.text = when (selectedPaymentMethod) {
            PaymentMethod.IDEAL -> "Betaal met iDEAL"
            PaymentMethod.PAYPAL -> "Betaal met PayPal"
            PaymentMethod.GOOGLE_PAY -> "Betaal met Google Pay"
            PaymentMethod.CREDIT_CARD -> String.format("Betaal € %.2f", orderTotal)
        }
    }

    private fun updatePayButtonState() {
        val isValid = isFormValid()
        payButton.isEnabled = isValid && !isProcessing
        payButton.alpha = if (isValid && !isProcessing) 1.0f else 0.6f
    }

    private fun isFormValid(): Boolean {
        return when (selectedPaymentMethod) {
            PaymentMethod.CREDIT_CARD -> {
                val cardNumber = cardNumberInput.text.toString().replace(" ", "")
                val cardHolder = cardHolderNameInput.text.toString().trim()
                val expiry = expiryDateInput.text.toString()
                val cvv = cvvInput.text.toString()

                cardNumber.length >= 13 &&
                        cardHolder.isNotEmpty() &&
                        expiry.length == 5 &&
                        cvv.length >= 3
            }
            else -> true // Other payment methods don't require validation
        }
    }

    private fun processPayment() {
        if (isProcessing) return

        if (selectedPaymentMethod == PaymentMethod.CREDIT_CARD && !isFormValid()) {
            Toast.makeText(this, "Vul alle creditcardgegevens correct in", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            // Simulate payment processing
            delay(500)

            withContext(Dispatchers.Main) {
                isProcessing = false
                setLoading(false)

                // Return success result
                setResult(RESULT_OK)
                Toast.makeText(this@PaymentActivity, "Betaling succesvol!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        payButtonProgress.visibility = if (loading) View.VISIBLE else View.GONE
        payButtonText.visibility = if (loading) View.GONE else View.VISIBLE
        payButton.isEnabled = !loading
    }

    private fun updateUI() {
        updatePaymentMethodUI()
        updatePayButtonText()
        updatePayButtonState()
    }
}

