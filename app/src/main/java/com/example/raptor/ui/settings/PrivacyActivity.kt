package com.example.raptor.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.example.raptor.R

/**
 * Privacy Activity
 * Komt overeen met iOS PrivacyView
 */
class PrivacyActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var privacyPolicyLink: View
    private lateinit var shareLocationSwitch: Switch
    private lateinit var shareUsageSwitch: Switch
    private lateinit var marketingEmailsSwitch: Switch

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        initializeViews()
        setupToolbar()
        setupClickListeners()
        loadPreferences()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        privacyPolicyLink = findViewById(R.id.privacyPolicyLink)
        shareLocationSwitch = findViewById(R.id.shareLocationSwitch)
        shareUsageSwitch = findViewById(R.id.shareUsageSwitch)
        marketingEmailsSwitch = findViewById(R.id.marketingEmailsSwitch)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        privacyPolicyLink.setOnClickListener {
            val url = "https://raptor.nl/privacy"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        shareLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("shareLocationData", isChecked).apply()
        }

        shareUsageSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("shareUsageData", isChecked).apply()
        }

        marketingEmailsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("allowMarketingEmails", isChecked).apply()
        }
    }

    private fun loadPreferences() {
        shareLocationSwitch.isChecked = prefs.getBoolean("shareLocationData", true)
        shareUsageSwitch.isChecked = prefs.getBoolean("shareUsageData", false)
        marketingEmailsSwitch.isChecked = prefs.getBoolean("allowMarketingEmails", false)
    }
}

