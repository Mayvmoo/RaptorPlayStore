package com.example.raptor.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.raptor.R

/**
 * About Activity
 * Komt overeen met iOS AboutView
 */
class AboutActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var versionText: TextView
    private lateinit var termsRow: View
    private lateinit var privacyPolicyRow: View
    private lateinit var licensesRow: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initializeViews()
        setupToolbar()
        setupClickListeners()
        loadVersionInfo()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        versionText = findViewById(R.id.versionText)
        termsRow = findViewById(R.id.termsRow)
        privacyPolicyRow = findViewById(R.id.privacyPolicyRow)
        licensesRow = findViewById(R.id.licensesRow)
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
        termsRow.setOnClickListener {
            openUrl("https://raptor.nl/terms")
        }

        privacyPolicyRow.setOnClickListener {
            openUrl("https://raptor.nl/privacy")
        }

        licensesRow.setOnClickListener {
            openUrl("https://raptor.nl/licenses")
        }
    }

    private fun loadVersionInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            versionText.text = "Versie $versionName"
        } catch (e: PackageManager.NameNotFoundException) {
            versionText.text = "Versie 1.0.0"
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

