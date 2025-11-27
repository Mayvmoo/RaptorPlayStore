package com.example.raptor.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.raptor.R
import com.example.raptor.models.CustomerPartner
import com.example.raptor.repositories.OrderRepository
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

/**
 * Quick Order Activity
 * Komt overeen met iOS QuickOrderView
 */
class QuickOrderActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var partnerInfoCard: MaterialCardView? = null
    private var partnerNameView: TextView? = null
    private var partnerCompanyView: TextView? = null
    private var partnerAddressView: TextView? = null
    private var standardModeButton: Button? = null
    private var expressModeButton: Button? = null
    private var sameDayModeButton: Button? = null
    private var urgentToggle: android.widget.Switch? = null
    private var notesInput: EditText? = null
    private var destinationInput: EditText? = null
    private lateinit var submitButton: Button
    private var loadingIndicator: ProgressBar? = null

    private var partner: CustomerPartner? = null
    private var customerEmail: String? = null
    private var customerName: String? = null
    private var customerAddress: String? = null
    private var customerType: String? = null
    private var companyName: String? = null

    private var selectedDeliveryMode: String = "standard"
    private var isUrgent: Boolean = false
    private var isLoading: Boolean = false

    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_order)

        // Get partner from intent
        partner = intent.getSerializableExtra("selected_partner") as? CustomerPartner
        customerEmail = intent.getStringExtra("customer_email")
        customerName = intent.getStringExtra("customer_name")
        customerAddress = intent.getStringExtra("customer_address")
        customerType = intent.getStringExtra("customer_type")
        companyName = intent.getStringExtra("company_name")

        if (partner == null) {
            Toast.makeText(this, "Geen partner geselecteerd", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupToolbar()
        setupClickListeners()
        displayPartnerInfo()
    }

    private fun initializeViews() {
        // toolbar = findViewById(R.id.toolbar)
        // partnerInfoCard = findViewById(R.id.partnerInfoCard)
        // partnerNameView = findViewById(R.id.partnerName)
        // partnerCompanyView = findViewById(R.id.partnerCompany)
        // partnerAddressView = findViewById(R.id.partnerAddress)
        // standardModeButton = findViewById(R.id.standardModeButton)
        // expressModeButton = findViewById(R.id.expressModeButton)
        // sameDayModeButton = findViewById(R.id.sameDayModeButton)
        // urgentToggle = findViewById(R.id.urgentToggle)
        notesInput = findViewById(R.id.notesInput)
        destinationInput = findViewById(R.id.destinationInput)
        submitButton = findViewById(R.id.createButton)
        // loadingIndicator = findViewById(R.id.loadingIndicator)
    }

    private fun setupToolbar() {
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            it.setNavigationOnClickListener { finish() }
        }
    }

    private fun setupClickListeners() {
        standardModeButton?.setOnClickListener {
            selectDeliveryMode("standard")
        }

        expressModeButton?.setOnClickListener {
            selectDeliveryMode("express")
        }

        sameDayModeButton?.setOnClickListener {
            selectDeliveryMode("same-day")
        }

        urgentToggle?.setOnCheckedChangeListener { _, isChecked ->
            isUrgent = isChecked
        }

        submitButton.setOnClickListener {
            submitQuickOrder()
        }
    }

    private fun selectDeliveryMode(mode: String) {
        selectedDeliveryMode = mode

        // Update button states
        standardModeButton?.isSelected = mode == "standard"
        expressModeButton?.isSelected = mode == "express"
        sameDayModeButton?.isSelected = mode == "same-day"

        // Update button backgrounds
        standardModeButton?.background = if (mode == "standard") {
            getDrawable(R.drawable.button_primary_enabled)
        } else {
            getDrawable(R.drawable.button_secondary)
        }

        expressModeButton?.background = if (mode == "express") {
            getDrawable(R.drawable.button_primary_enabled)
        } else {
            getDrawable(R.drawable.button_secondary)
        }

        sameDayModeButton?.background = if (mode == "same-day") {
            getDrawable(R.drawable.button_primary_enabled)
        } else {
            getDrawable(R.drawable.button_secondary)
        }
    }

    private fun displayPartnerInfo() {
        partner?.let {
            partnerNameView?.text = it.partnerName
            partnerCompanyView?.text = it.partnerCompany
            if (!it.partnerAddress.isNullOrEmpty()) {
                partnerAddressView?.text = it.partnerAddress
                partnerAddressView?.visibility = View.VISIBLE
            } else {
                partnerAddressView?.visibility = View.GONE
            }
        }
    }

    private fun submitQuickOrder() {
        if (isLoading) return

        val partner = this.partner ?: return
        val customerEmail = this.customerEmail ?: return
        val customerName = this.customerName ?: return
        val customerAddress = this.customerAddress ?: ""

        // Determine sender name based on customer type
        val senderName = if (customerType == "business" && companyName != null && companyName!!.isNotEmpty()) {
            if (customerName != companyName) {
                "$companyName - $customerName"
            } else {
                companyName!!
            }
        } else {
            customerName
        }

        isLoading = true
        loadingIndicator?.visibility = View.VISIBLE
        submitButton.isEnabled = false

        // Get destination from input or partner address
        val destinationAddress = destinationInput?.text?.toString()?.takeIf { it.isNotEmpty() }
            ?: partner.partnerAddress 
            ?: partner.partnerEmail

        lifecycleScope.launch {
            val result = orderRepository.createOrder(
                senderName = senderName,
                senderAddress = customerAddress,
                destinationName = partner.partnerName,
                destinationAddress = destinationAddress,
                deliveryMode = selectedDeliveryMode,
                isUrgent = isUrgent,
                notes = notesInput?.text?.toString()?.takeIf { it.isNotEmpty() },
                attachmentImageData = null,
                customerEmail = customerEmail
            )

            result.getOrNull()?.let { orderId ->
                Toast.makeText(this@QuickOrderActivity, "Bestelling geplaatst!", Toast.LENGTH_SHORT).show()
                finish()
            } ?: run {
                val error = result.exceptionOrNull()
                Toast.makeText(
                    this@QuickOrderActivity,
                    error?.message ?: "Bestelling plaatsen mislukt",
                    Toast.LENGTH_LONG
                ).show()
            }

            isLoading = false
            loadingIndicator?.visibility = View.GONE
            submitButton.isEnabled = true
        }
    }
}
