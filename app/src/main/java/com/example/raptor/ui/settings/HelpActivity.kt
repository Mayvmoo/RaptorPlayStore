package com.example.raptor.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.raptor.R

/**
 * Help Activity
 * Komt overeen met iOS HelpView
 */
class HelpActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var faqRow: View
    private lateinit var contactRow: View
    private lateinit var tutorialsRow: View
    private lateinit var manualRow: View
    private lateinit var emailContactRow: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        initializeViews()
        setupToolbar()
        setupClickListeners()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        faqRow = findViewById(R.id.faqRow)
        contactRow = findViewById(R.id.contactRow)
        tutorialsRow = findViewById(R.id.tutorialsRow)
        manualRow = findViewById(R.id.manualRow)
        emailContactRow = findViewById(R.id.emailContactRow)
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
        faqRow.setOnClickListener {
            openUrl("https://raptor.nl/faq")
        }

        contactRow.setOnClickListener {
            openEmail("support@raptor.nl")
        }

        tutorialsRow.setOnClickListener {
            openUrl("https://raptor.nl/tutorials")
        }

        manualRow.setOnClickListener {
            openUrl("https://raptor.nl/manual")
        }

        emailContactRow.setOnClickListener {
            openEmail("support@raptor.nl")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}

