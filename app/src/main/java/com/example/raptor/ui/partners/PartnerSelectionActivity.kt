package com.example.raptor.ui.partners

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raptor.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Partner Selection Activity
 * Matching iOS PartnerSelectionView
 * Allows user to select from saved partners or add new ones
 */
class PartnerSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addPartnerFab: FloatingActionButton

    private var customerEmail: String? = null
    private val partners = mutableListOf<Partner>()

    // Activity result launcher for AddPartnerActivity
    private val addPartnerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Reload partners
            loadPartners()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_selection)

        customerEmail = intent.getStringExtra("customer_email")

        initializeViews()
        setupClickListeners()
        loadPartners()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.partnersRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        addPartnerFab = findViewById(R.id.addPartnerFab)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Close button
        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        addPartnerFab.setOnClickListener {
            openAddPartner()
        }

        findViewById<LinearLayout>(R.id.addPartnerButton).setOnClickListener {
            openAddPartner()
        }
    }

    private fun openAddPartner() {
        val intent = Intent(this, AddPartnerActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
        }
        addPartnerLauncher.launch(intent)
    }

    private fun loadPartners() {
        // TODO: Load actual partners from API/database
        // For now, use mock data
        partners.clear()
        partners.addAll(
            listOf(
                Partner("1", "Jan Jansen", "Jansen BV", "Hoofdstraat 1, 1234 AB Amsterdam"),
                Partner("2", "Piet Pietersen", "Pietersen & Zn", "Kerkstraat 15, 5678 CD Rotterdam"),
                Partner("3", "Klaas Klaassen", "Klaassen Transport", "Industrieweg 42, 9012 EF Utrecht")
            )
        )

        updateUI()
    }

    private fun updateUI() {
        if (partners.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            recyclerView.adapter = PartnerAdapter(partners) { partner ->
                selectPartner(partner)
            }
        }
    }

    private fun selectPartner(partner: Partner) {
        val resultIntent = Intent().apply {
            putExtra("partner_id", partner.id)
            putExtra("partner_name", partner.name)
            putExtra("partner_company", partner.company)
            putExtra("partner_address", partner.address)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    // Data class for Partner
    data class Partner(
        val id: String,
        val name: String,
        val company: String,
        val address: String
    )

    // RecyclerView Adapter
    inner class PartnerAdapter(
        private val partners: List<Partner>,
        private val onPartnerClick: (Partner) -> Unit
    ) : RecyclerView.Adapter<PartnerAdapter.PartnerViewHolder>() {

        inner class PartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.partnerNameText)
            val companyText: TextView = itemView.findViewById(R.id.partnerCompanyText)
            val addressText: TextView = itemView.findViewById(R.id.partnerAddressText)
            val initialText: TextView = itemView.findViewById(R.id.partnerInitialText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_partner, parent, false)
            return PartnerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
            val partner = partners[position]
            holder.nameText.text = partner.name
            holder.companyText.text = partner.company
            holder.addressText.text = partner.address
            holder.initialText.text = partner.name.first().uppercase()

            holder.itemView.setOnClickListener {
                onPartnerClick(partner)
            }
        }

        override fun getItemCount(): Int = partners.size
    }
}

