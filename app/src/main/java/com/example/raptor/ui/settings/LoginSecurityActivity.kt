package com.example.raptor.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.example.raptor.R
import com.example.raptor.ui.profile.ChangePasswordActivity

/**
 * Login Security Activity
 * Komt overeen met iOS LoginSecurityView
 */
class LoginSecurityActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var changePasswordRow: View
    private lateinit var autoLogoutSwitch: Switch
    private lateinit var hidePasswordSwitch: Switch

    private lateinit var prefs: SharedPreferences
    private var customerEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_security)

        customerEmail = intent.getStringExtra("customer_email")
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        initializeViews()
        setupToolbar()
        setupClickListeners()
        loadPreferences()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        changePasswordRow = findViewById(R.id.changePasswordRow)
        autoLogoutSwitch = findViewById(R.id.autoLogoutSwitch)
        hidePasswordSwitch = findViewById(R.id.hidePasswordSwitch)
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
        changePasswordRow.setOnClickListener {
            customerEmail?.let { email ->
                val intent = Intent(this, ChangePasswordActivity::class.java).apply {
                    putExtra("customer_email", email)
                }
                startActivity(intent)
            }
        }

        autoLogoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("autoLogoutEnabled", isChecked).apply()
        }

        hidePasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("hidePasswordEnabled", isChecked).apply()
        }
    }

    private fun loadPreferences() {
        autoLogoutSwitch.isChecked = prefs.getBoolean("autoLogoutEnabled", false)
        hidePasswordSwitch.isChecked = prefs.getBoolean("hidePasswordEnabled", false)
    }
}

