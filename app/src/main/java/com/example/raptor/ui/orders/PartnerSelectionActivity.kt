package com.example.raptor.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raptor.R
import com.example.raptor.models.CustomerPartner
import com.example.raptor.models.CustomerSession
import com.example.raptor.network.NetworkModule
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Partner Selection Activity
 * Komt overeen met iOS PartnerSelectionView
 */
class PartnerSelectionActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private lateinit var partnersRecyclerView: RecyclerView
    private var emptyStateContainer: LinearLayout? = null
    private var loadingContainer: View? = null

    private var customerEmail: String? = null
    private val partners = mutableListOf<CustomerPartner>()
    private lateinit var adapter: PartnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner_selection)

        customerEmail = intent.getStringExtra("customer_email")

        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadPartners()
    }

    private fun initializeViews() {
        // toolbar = findViewById(R.id.toolbar)
        partnersRecyclerView = findViewById(R.id.partnersRecyclerView)
        emptyStateContainer = findViewById(R.id.emptyStateLayout)
        // loadingContainer = findViewById(R.id.loadingContainer)
        
        // Setup close button
        findViewById<View>(R.id.closeButton)?.setOnClickListener { finish() }
    }

    private fun setupToolbar() {
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            it.setNavigationOnClickListener { finish() }
        }
    }

    private fun setupRecyclerView() {
        adapter = PartnerAdapter(partners) { partner ->
            onPartnerSelected(partner)
        }
        partnersRecyclerView.layoutManager = LinearLayoutManager(this)
        partnersRecyclerView.adapter = adapter
    }

    private fun loadPartners() {
        // TODO: Load partners from API or local storage
        // For now, show empty state
        showEmptyState()
        
        // In production, load from API:
        /*
        lifecycleScope.launch {
            try {
                val response = NetworkModule.apiService.getPartners(customerEmail ?: "")
                if (response.isSuccessful && response.body() != null) {
                    partners.clear()
                    partners.addAll(response.body()!!)
                    adapter.notifyDataSetChanged()
                    
                    if (partners.isEmpty()) {
                        showEmptyState()
                    } else {
                        showPartners()
                    }
                }
            } catch (e: HttpException) {
                // Handle error
            } catch (e: IOException) {
                // Handle error
            }
        }
        */
    }

    private fun onPartnerSelected(partner: CustomerPartner) {
        val intent = Intent().apply {
            putExtra("partner_name", partner.partnerName)
            putExtra("partner_company", partner.partnerCompany)
            putExtra("partner_email", partner.partnerEmail)
            putExtra("partner_address", partner.partnerAddress)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun showEmptyState() {
        emptyStateContainer?.visibility = View.VISIBLE
        partnersRecyclerView.visibility = View.GONE
        loadingContainer?.visibility = View.GONE
    }

    private fun showPartners() {
        emptyStateContainer?.visibility = View.GONE
        partnersRecyclerView.visibility = View.VISIBLE
        loadingContainer?.visibility = View.GONE
    }

    private class PartnerAdapter(
        private val partners: List<CustomerPartner>,
        private val onPartnerClick: (CustomerPartner) -> Unit
    ) : RecyclerView.Adapter<PartnerAdapter.PartnerViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PartnerViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_partner, parent, false)
            return PartnerViewHolder(view)
        }

        override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
            holder.bind(partners[position], onPartnerClick)
        }

        override fun getItemCount() = partners.size

        class PartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val partnerInitial: TextView? = itemView.findViewById(R.id.partnerInitialText)
            private val partnerName: TextView? = itemView.findViewById(R.id.partnerNameText)
            private val partnerCompany: TextView? = itemView.findViewById(R.id.partnerCompanyText)
            private val partnerAddress: TextView? = itemView.findViewById(R.id.partnerAddressText)

            fun bind(partner: CustomerPartner, onClick: (CustomerPartner) -> Unit) {
                partnerName?.text = partner.partnerName
                partnerCompany?.text = partner.partnerCompany
                partnerInitial?.text = partner.partnerName.firstOrNull()?.uppercase() ?: "?"
                
                if (!partner.partnerAddress.isNullOrEmpty()) {
                    partnerAddress?.text = partner.partnerAddress
                    partnerAddress?.visibility = View.VISIBLE
                } else {
                    partnerAddress?.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    onClick(partner)
                }
            }
        }
    }
}

