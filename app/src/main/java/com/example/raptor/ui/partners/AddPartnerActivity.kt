package com.example.raptor.ui.partners

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Add Partner Activity
 * Matching iOS AddPartnerView exactly
 * Allows user to add a new delivery partner (contact)
 */
class AddPartnerActivity : AppCompatActivity() {

    private lateinit var partnerEmailInput: EditText
    private lateinit var partnerNameInput: EditText
    private lateinit var partnerCompanyInput: EditText
    private lateinit var partnerAddressInput: EditText
    private lateinit var addPartnerButton: Button
    private lateinit var buttonText: TextView
    private lateinit var buttonProgress: ProgressBar

    private var customerEmail: String = ""
    private var isLoading: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_partner)

        customerEmail = intent.getStringExtra("customer_email") ?: ""

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        partnerEmailInput = findViewById(R.id.partnerEmailInput)
        partnerNameInput = findViewById(R.id.partnerNameInput)
        partnerCompanyInput = findViewById(R.id.partnerCompanyInput)
        partnerAddressInput = findViewById(R.id.partnerAddressInput)
        addPartnerButton = findViewById(R.id.addPartnerButton)
        buttonText = findViewById(R.id.buttonText)
        buttonProgress = findViewById(R.id.buttonProgress)

        // Close button
        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        addPartnerButton.setOnClickListener {
            addPartner()
        }
    }

    private fun addPartner() {
        if (isLoading) return

        val email = partnerEmailInput.text.toString().trim()
        val name = partnerNameInput.text.toString().trim()
        val company = partnerCompanyInput.text.toString().trim()
        val address = partnerAddressInput.text.toString().trim()

        // Validation
        if (email.isEmpty()) {
            Toast.makeText(this, "E-mailadres is verplicht", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Voer een geldig e-mailadres in", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Naam contactpersoon is verplicht", Toast.LENGTH_SHORT).show()
            return
        }

        if (company.isEmpty()) {
            Toast.makeText(this, "Bedrijfsnaam is verplicht", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Simulate API call
                delay(500)

                // TODO: Implement actual API call to save partner
                // val response = ApiService.addPartner(customerEmail, email, name, company, address)

                withContext(Dispatchers.Main) {
                    isLoading = false
                    setLoading(false)
                    Toast.makeText(this@AddPartnerActivity, "Partner toegevoegd!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    setLoading(false)
                    Toast.makeText(this@AddPartnerActivity, "Fout: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun setLoading(loading: Boolean) {
        buttonProgress.visibility = if (loading) View.VISIBLE else View.GONE
        buttonText.visibility = if (loading) View.GONE else View.VISIBLE
        addPartnerButton.isEnabled = !loading
        addPartnerButton.alpha = if (loading) 0.6f else 1.0f
    }
}

