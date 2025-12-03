package com.example.raptor.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.raptor.MainActivity
import com.example.raptor.R
import com.example.raptor.network.WebSocketService
import com.google.android.material.navigation.NavigationView
import android.util.Log

/**
 * Customer Dashboard Activity
 * Main screen for customers with navigation drawer (hamburger menu)
 * Matches iOS CustomerRootView functionality
 */
class CustomerDashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val TAG = "CustomerDashboard"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // Profile header views
    private lateinit var profileName: TextView
    private lateinit var profileInitials: TextView
    private lateinit var profileImage: ImageView
    private lateinit var profileRating: TextView
    
    // WebSocket service
    private val websocketService = WebSocketService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        // Initialize views
        initializeViews()
        setupToolbar()
        setupNavigationDrawer()
        setupProfileHeader()
        setupBottomNavigationBar()

        // Load dashboard content
        loadDashboardContent()
        
        // Connect WebSocket voor real-time updates en data fetching
        connectWebSocket()
    }
    
    private fun connectWebSocket() {
        // Get customer email from intent or session
        val customerEmail = intent.getStringExtra("customer_email") ?: ""
        
        // Connect WebSocket voor real-time updates en data fetching
        if (websocketService.connectionState.value !is WebSocketService.ConnectionState.Connected) {
            websocketService.connect(customerEmail)
            Log.d(TAG, "WebSocket connecting for customer: $customerEmail")
        }
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        // Get profile header views
        val headerView = navigationView.getHeaderView(0)
        profileName = headerView.findViewById(R.id.profileName)
        profileInitials = headerView.findViewById(R.id.profileInitials)
        profileImage = headerView.findViewById(R.id.profileImage)
        profileRating = headerView.findViewById(R.id.profileRating)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Raptor"
        
        // Set toolbar text color to white
        toolbar.setTitleTextColor(getColor(R.color.white))
        
        // Create custom hamburger button with gold gradient circle (matching iOS)
        createCustomHamburgerButton()
    }
    
    private fun createCustomHamburgerButton() {
        // Create a custom view for the hamburger button with gold gradient background
        val hamburgerButton = layoutInflater.inflate(R.layout.hamburger_menu_button, null)
        
        // Set click listener to open drawer
        hamburgerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        
        // Add to toolbar
        toolbar.addView(hamburgerButton)
    }

    private fun setupNavigationDrawer() {
        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this)

        // Customize drawer appearance
        navigationView.itemIconTintList = getColorStateList(R.color.gold)
        
        // Style logout button differently (red)
        val logoutItem = navigationView.menu.findItem(R.id.nav_logout)
        logoutItem?.setIconTintList(getColorStateList(R.color.red))
        
        // Add drawer listener for slide-in animation
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Smooth slide-in animation (handled automatically by DrawerLayout)
            }

            override fun onDrawerOpened(drawerView: View) {
                // Drawer opened
            }

            override fun onDrawerClosed(drawerView: View) {
                // Drawer closed
            }

            override fun onDrawerStateChanged(newState: Int) {
                // Drawer state changed
            }
        })
    }

    private fun setupProfileHeader() {
        // Get customer info from intent or session
        val customerName = intent.getStringExtra("customer_name") ?: "John Doe"
        val customerEmail = intent.getStringExtra("customer_email") ?: ""

        // Set profile name
        profileName.text = customerName

        // Calculate and set initials (automatisch berekend)
        val initials = calculateInitials(customerName)
        profileInitials.text = initials

        // Set rating display (5.0 met ster) - matching iOS
        profileRating.text = "5,0"

        // TODO: Load profile image if available
        // For now, show initials
        profileInitials.visibility = View.VISIBLE
        profileImage.visibility = View.GONE
        
        // Make profile header clickable to open profile
        val headerView = navigationView.getHeaderView(0)
        headerView.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            openPersonalInfo()
        }
    }

    private fun calculateInitials(name: String): String {
        val components = name.trim().split("\\s+".toRegex())
        return when {
            components.size >= 2 -> {
                components[0].take(1).uppercase() + components[1].take(1).uppercase()
            }
            components.size == 1 -> {
                components[0].take(2).uppercase()
            }
            else -> "JD"
        }
    }

    private fun loadDashboardContent() {
        // Load dashboard content fragment
        val fragment = DashboardContentFragment().apply {
            arguments = Bundle().apply {
                putString("customer_email", intent.getStringExtra("customer_email") ?: "")
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContentContainer, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_personal_info -> {
                // Open personal info / profile
                openPersonalInfo()
            }
            R.id.nav_login_security -> {
                // Open login & security
                openLoginSecurity()
            }
            R.id.nav_privacy -> {
                // Open privacy settings
                openPrivacy()
            }
            R.id.nav_order_history -> {
                // Open order history (Bekijk alle)
                openOrderHistory()
            }
            R.id.nav_help -> {
                // Open help
                openHelp()
            }
            R.id.nav_about -> {
                // Open about
                openAbout()
            }
            R.id.nav_logout -> {
                // Logout
                logout()
            }
        }

        // Close drawer
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openPersonalInfo() {
        val customerEmail = intent.getStringExtra("customer_email") ?: ""
        val customerName = intent.getStringExtra("customer_name") ?: ""
        val intent = Intent(this, com.example.raptor.ui.profile.CustomerProfileActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
            putExtra("contact_name", customerName)
            putExtra("customer_type", intent.getStringExtra("customer_type") ?: "INDIVIDUAL")
            putExtra("company_name", intent.getStringExtra("company_name"))
            putExtra("phone_number", intent.getStringExtra("phone_number"))
            putExtra("address", intent.getStringExtra("address"))
        }
        startActivity(intent)
    }

    private fun openLoginSecurity() {
        val customerEmail = intent.getStringExtra("customer_email") ?: ""
        val intent = Intent(this, com.example.raptor.ui.settings.LoginSecurityActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
        }
        startActivity(intent)
    }

    private fun openPrivacy() {
        val intent = Intent(this, com.example.raptor.ui.settings.PrivacyActivity::class.java)
        startActivity(intent)
    }

    private fun openOrderHistory() {
        val customerEmail = intent.getStringExtra("customer_email") ?: ""
        val intent = Intent(this, com.example.raptor.ui.orders.OrderHistoryActivity::class.java).apply {
            putExtra("customer_email", customerEmail)
        }
        startActivity(intent)
    }

    private fun openHelp() {
        val intent = Intent(this, com.example.raptor.ui.settings.HelpActivity::class.java)
        startActivity(intent)
    }

    private fun openAbout() {
        val intent = Intent(this, com.example.raptor.ui.settings.AboutActivity::class.java)
        startActivity(intent)
    }

    private fun logout() {
        // Clear session and return to login
        // TODO: Clear actual session data
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigationBar() {
        val homeTab = findViewById<View>(R.id.homeTab)
        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val homeText = homeTab.findViewById<TextView>(R.id.homeText)
        
        // Home tab is always selected (only tab for now)
        homeTab.setOnClickListener {
            // Already on home, do nothing
        }
        
        // Set gold gradient for selected state (matching iOS)
        // For icons, we use a gradient drawable as tint
        val gradientDrawable = ContextCompat.getDrawable(this, R.drawable.gold_gradient_horizontal)
        if (gradientDrawable != null) {
            // Apply gradient to icon using color filter (simplified - full gradient requires custom view)
            homeIcon.setColorFilter(getColor(R.color.light_gold))
        }
        
        // For text, we'll use light_gold color (gradient on text requires custom TextView)
        homeText?.setTextColor(getColor(R.color.light_gold))
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

