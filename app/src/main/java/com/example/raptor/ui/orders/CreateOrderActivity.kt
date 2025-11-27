package com.example.raptor.ui.orders

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.example.raptor.R
import com.example.raptor.ui.partners.PartnerSelectionActivity
import com.example.raptor.ui.payment.PaymentActivity
import com.example.raptor.viewmodels.OrderViewModel
import com.example.raptor.viewmodels.OrdersState
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Create Order Activity - Extended Version
 * Matching iOS CreateOrderView exactly
 * Includes: Delivery modes, urgent toggle, schedule picker, partner selection, photo attachment
 */
class CreateOrderActivity : AppCompatActivity() {

    private val viewModel: OrderViewModel by viewModels()

    // Delivery mode
    enum class DeliveryMode(val displayName: String, val priceMultiplier: Double) {
        STANDARD("Standaard", 1.0),
        EXPRESS("Express", 1.5),
        SAME_DAY("Vandaag", 2.0)
    }

    private var selectedDeliveryMode: DeliveryMode = DeliveryMode.STANDARD
    private var isUrgent: Boolean = false
    private var scheduledDate: Calendar? = null
    private var scheduledTime: Calendar? = null
    private var selectedPartner: Map<String, String>? = null
    private var attachedImageData: ByteArray? = null
    private var basePrice: Double = 12.50

    // UI Components
    private lateinit var backButton: ImageView
    private lateinit var standardDeliveryButton: LinearLayout
    private lateinit var expressDeliveryButton: LinearLayout
    private lateinit var sameDayDeliveryButton: LinearLayout
    private lateinit var urgentSwitch: SwitchCompat
    private lateinit var scheduleButton: LinearLayout
    private lateinit var scheduleText: TextView
    private lateinit var partnerButton: LinearLayout
    private lateinit var partnerText: TextView
    private lateinit var senderNameInput: EditText
    private lateinit var senderAddressInput: EditText
    private lateinit var destinationNameInput: EditText
    private lateinit var destinationAddressInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var attachPhotoButton: LinearLayout
    private lateinit var attachPhotoText: TextView
    private lateinit var priceEstimateText: TextView
    private lateinit var createButton: Button
    private lateinit var createButtonText: TextView
    private lateinit var createButtonProgress: ProgressBar

    // Delivery mode icons and texts
    private lateinit var standardIcon: ImageView
    private lateinit var standardText: TextView
    private lateinit var expressIcon: ImageView
    private lateinit var expressText: TextView
    private lateinit var sameDayIcon: ImageView
    private lateinit var sameDayText: TextView

    private var customerEmail: String? = null

    // Activity result launchers
    private val schedulePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                scheduledDate = Calendar.getInstance().apply {
                    timeInMillis = data.getLongExtra("selected_date", System.currentTimeMillis())
                }
                scheduledTime = Calendar.getInstance().apply {
                    timeInMillis = data.getLongExtra("selected_time", System.currentTimeMillis())
                }
                updateScheduleText()
            }
        }
    }

    private val partnerSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val partnerName = data.getStringExtra("partner_name") ?: ""
                val partnerCompany = data.getStringExtra("partner_company") ?: ""
                val partnerAddress = data.getStringExtra("partner_address") ?: ""

                selectedPartner = mapOf(
                    "name" to partnerName,
                    "company" to partnerCompany,
                    "address" to partnerAddress
                )

                destinationNameInput.setText(partnerName)
                destinationAddressInput.setText(partnerAddress)
                partnerText.text = partnerCompany.ifEmpty { partnerName }
            }
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                processSelectedImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.get("data")?.let { bitmap ->
                processSelectedBitmap(bitmap as Bitmap)
            }
        }
    }

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Payment successful, create the order
            submitOrder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)

        customerEmail = intent.getStringExtra("customer_email")

        initializeViews()
        setupObservers()
        setupClickListeners()
        updatePriceEstimate()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        standardDeliveryButton = findViewById(R.id.standardDeliveryButton)
        expressDeliveryButton = findViewById(R.id.expressDeliveryButton)
        sameDayDeliveryButton = findViewById(R.id.sameDayDeliveryButton)
        urgentSwitch = findViewById(R.id.urgentSwitch)
        scheduleButton = findViewById(R.id.scheduleButton)
        scheduleText = findViewById(R.id.scheduleText)
        partnerButton = findViewById(R.id.partnerButton)
        partnerText = findViewById(R.id.partnerText)
        senderNameInput = findViewById(R.id.senderNameInput)
        senderAddressInput = findViewById(R.id.senderAddressInput)
        destinationNameInput = findViewById(R.id.destinationNameInput)
        destinationAddressInput = findViewById(R.id.destinationAddressInput)
        notesInput = findViewById(R.id.notesInput)
        attachPhotoButton = findViewById(R.id.attachPhotoButton)
        attachPhotoText = findViewById(R.id.attachPhotoText)
        priceEstimateText = findViewById(R.id.priceEstimateText)
        createButton = findViewById(R.id.createButton)
        createButtonText = findViewById(R.id.createButtonText)
        createButtonProgress = findViewById(R.id.createButtonProgress)

        // Delivery mode icons and texts
        standardIcon = findViewById(R.id.standardIcon)
        standardText = findViewById(R.id.standardText)
        expressIcon = findViewById(R.id.expressIcon)
        expressText = findViewById(R.id.expressText)
        sameDayIcon = findViewById(R.id.sameDayIcon)
        sameDayText = findViewById(R.id.sameDayText)
    }

    private fun setupObservers() {
        viewModel.ordersState.observe(this) { state ->
            when (state) {
                is OrdersState.Idle -> {
                    setLoading(false)
                }
                is OrdersState.Loading -> {
                    setLoading(true)
                }
                is OrdersState.OrderCreated -> {
                    setLoading(false)
                    Toast.makeText(this, "Bezorging aangemaakt: ${state.orderId}", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is OrdersState.Error -> {
                    setLoading(false)
                    Toast.makeText(this, "Fout: ${state.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        // Delivery mode buttons
        standardDeliveryButton.setOnClickListener {
            selectDeliveryMode(DeliveryMode.STANDARD)
        }
        expressDeliveryButton.setOnClickListener {
            selectDeliveryMode(DeliveryMode.EXPRESS)
        }
        sameDayDeliveryButton.setOnClickListener {
            selectDeliveryMode(DeliveryMode.SAME_DAY)
        }

        // Urgent switch
        urgentSwitch.setOnCheckedChangeListener { _, isChecked ->
            isUrgent = isChecked
            updatePriceEstimate()
        }

        // Schedule picker
        scheduleButton.setOnClickListener {
            openSchedulePicker()
        }

        // Partner selection
        partnerButton.setOnClickListener {
            openPartnerSelection()
        }

        // Photo attachment
        attachPhotoButton.setOnClickListener {
            showPhotoOptions()
        }

        // Create button
        createButton.setOnClickListener {
            createOrder()
        }
    }

    private fun selectDeliveryMode(mode: DeliveryMode) {
        selectedDeliveryMode = mode

        // Update UI
        val unselectedBg = ContextCompat.getDrawable(this, R.drawable.delivery_mode_unselected)
        val selectedBg = ContextCompat.getDrawable(this, R.drawable.delivery_mode_selected)
        val unselectedColor = ContextCompat.getColor(this, R.color.white_opacity_60)
        val selectedColor = ContextCompat.getColor(this, R.color.primary_blue)

        // Reset all
        standardDeliveryButton.background = unselectedBg
        expressDeliveryButton.background = unselectedBg
        sameDayDeliveryButton.background = unselectedBg
        standardIcon.setColorFilter(unselectedColor)
        expressIcon.setColorFilter(unselectedColor)
        sameDayIcon.setColorFilter(unselectedColor)
        standardText.setTextColor(unselectedColor)
        expressText.setTextColor(unselectedColor)
        sameDayText.setTextColor(unselectedColor)

        // Set selected
        when (mode) {
            DeliveryMode.STANDARD -> {
                standardDeliveryButton.background = selectedBg
                standardIcon.setColorFilter(selectedColor)
                standardText.setTextColor(selectedColor)
            }
            DeliveryMode.EXPRESS -> {
                expressDeliveryButton.background = selectedBg
                expressIcon.setColorFilter(selectedColor)
                expressText.setTextColor(selectedColor)
            }
            DeliveryMode.SAME_DAY -> {
                sameDayDeliveryButton.background = selectedBg
                sameDayIcon.setColorFilter(selectedColor)
                sameDayText.setTextColor(selectedColor)
            }
        }

        updatePriceEstimate()
    }

    private fun updatePriceEstimate() {
        var price = basePrice * selectedDeliveryMode.priceMultiplier
        if (isUrgent) {
            price += 5.0
        }
        priceEstimateText.text = String.format("€ %.2f", price)
    }

    private fun updateScheduleText() {
        if (scheduledDate != null && scheduledTime != null) {
            val dateFormat = SimpleDateFormat("d MMM", Locale("nl", "NL"))
            val timeFormat = SimpleDateFormat("HH:mm", Locale("nl", "NL"))
            scheduleText.text = "${dateFormat.format(scheduledDate!!.time)} om ${timeFormat.format(scheduledTime!!.time)}"
        } else {
            scheduleText.text = "Zo snel mogelijk"
        }
    }

    private fun openSchedulePicker() {
        val intent = Intent(this, SchedulePickerActivity::class.java).apply {
            scheduledDate?.let { putExtra("selected_date", it.timeInMillis) }
            scheduledTime?.let { putExtra("selected_time", it.timeInMillis) }
        }
        schedulePickerLauncher.launch(intent)
    }

    private fun openPartnerSelection() {
        val intent = Intent(this, PartnerSelectionActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
        }
        partnerSelectionLauncher.launch(intent)
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Camera", "Galerij")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Foto toevoegen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun processSelectedImage(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            processSelectedBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Fout bij laden van afbeelding", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processSelectedBitmap(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        attachedImageData = stream.toByteArray()
        attachPhotoText.text = "Foto toegevoegd ✓"
    }

    private fun createOrder() {
        val senderName = senderNameInput.text?.toString()?.trim() ?: ""
        val senderAddress = senderAddressInput.text?.toString()?.trim() ?: ""
        val destinationName = destinationNameInput.text?.toString()?.trim()
        val destinationAddress = destinationAddressInput.text?.toString()?.trim() ?: ""
        val notes = notesInput.text?.toString()?.trim()

        // Validation
        if (senderAddress.isEmpty()) {
            Toast.makeText(this, "Ophaaladres is verplicht", Toast.LENGTH_SHORT).show()
            return
        }

        if (destinationAddress.isEmpty()) {
            Toast.makeText(this, "Bestemmingsadres is verplicht", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate total price
        var totalPrice = basePrice * selectedDeliveryMode.priceMultiplier
        if (isUrgent) {
            totalPrice += 5.0
        }

        // Open payment screen
        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("order_total", totalPrice)
            putExtra("delivery_mode", selectedDeliveryMode.name.lowercase())
        }
        paymentLauncher.launch(intent)
    }

    private fun submitOrder() {
        val senderName = senderNameInput.text?.toString()?.trim() ?: ""
        val senderAddress = senderAddressInput.text?.toString()?.trim() ?: ""
        val destinationName = destinationNameInput.text?.toString()?.trim()
        val destinationAddress = destinationAddressInput.text?.toString()?.trim() ?: ""
        val notes = notesInput.text?.toString()?.trim()
        
        // Convert ByteArray to Base64 string
        val attachmentBase64 = attachedImageData?.let { 
            Base64.encodeToString(it, Base64.DEFAULT) 
        }

        viewModel.createOrder(
            senderName = senderName,
            senderAddress = senderAddress,
            destinationName = destinationName?.takeIf { it.isNotEmpty() },
            destinationAddress = destinationAddress,
            deliveryMode = selectedDeliveryMode.name.lowercase(),
            isUrgent = isUrgent,
            notes = notes?.takeIf { it.isNotEmpty() },
            attachmentImageData = attachmentBase64,
            customerEmail = customerEmail
        )
    }

    private fun setLoading(loading: Boolean) {
        createButton.isEnabled = !loading
        createButtonProgress.visibility = if (loading) View.VISIBLE else View.GONE
        createButtonText.visibility = if (loading) View.GONE else View.VISIBLE
    }
}
