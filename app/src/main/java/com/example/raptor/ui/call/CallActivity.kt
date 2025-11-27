package com.example.raptor.ui.call

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.raptor.R
import java.util.Timer
import java.util.TimerTask

/**
 * Call Activity
 * Simple implementation with UI buttons (matching iOS CallView)
 * Full Agora.io integration not implemented - shows UI only
 */
class CallActivity : AppCompatActivity() {

    // Call states
    enum class CallState {
        CONNECTING,
        RINGING,
        CONNECTED,
        ENDED
    }

    private var callState: CallState = CallState.CONNECTING
    private var isMuted: Boolean = false
    private var isSpeakerOn: Boolean = false
    private var callDuration: Int = 0
    private var durationTimer: Timer? = null

    // UI Components
    private lateinit var receiverInitialText: TextView
    private lateinit var receiverNameText: TextView
    private lateinit var callStateText: TextView
    private lateinit var durationText: TextView
    private lateinit var connectedControls: LinearLayout

    private lateinit var muteButton: ImageButton
    private lateinit var endCallButton: ImageButton
    private lateinit var speakerButton: ImageButton

    private lateinit var muteText: TextView
    private lateinit var speakerText: TextView

    private var receiverName: String = ""
    private var receiverId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        // Get data from intent
        receiverName = intent.getStringExtra("receiver_name") ?: "Onbekend"
        receiverId = intent.getStringExtra("receiver_id") ?: ""

        initializeViews()
        setupClickListeners()
        simulateCall()
    }

    private fun initializeViews() {
        receiverInitialText = findViewById(R.id.receiverInitialText)
        receiverNameText = findViewById(R.id.receiverNameText)
        callStateText = findViewById(R.id.callStateText)
        durationText = findViewById(R.id.durationText)
        connectedControls = findViewById(R.id.connectedControls)

        muteButton = findViewById(R.id.muteButton)
        endCallButton = findViewById(R.id.endCallButton)
        speakerButton = findViewById(R.id.speakerButton)

        muteText = findViewById(R.id.muteText)
        speakerText = findViewById(R.id.speakerText)

        // Set receiver info
        receiverNameText.text = receiverName
        receiverInitialText.text = if (receiverName.isNotEmpty()) {
            receiverName.first().uppercase()
        } else {
            "?"
        }

        updateUI()
    }

    private fun setupClickListeners() {
        muteButton.setOnClickListener {
            toggleMute()
        }

        endCallButton.setOnClickListener {
            endCall()
        }

        speakerButton.setOnClickListener {
            toggleSpeaker()
        }
    }

    private fun simulateCall() {
        // Simulate connecting -> ringing -> connected
        callState = CallState.CONNECTING
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            callState = CallState.RINGING
            updateUI()
        }, 1500)

        Handler(Looper.getMainLooper()).postDelayed({
            callState = CallState.CONNECTED
            updateUI()
            startDurationTimer()
        }, 4000)
    }

    private fun startDurationTimer() {
        durationTimer = Timer()
        durationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                callDuration++
                runOnUiThread {
                    updateDurationText()
                }
            }
        }, 1000, 1000)
    }

    private fun stopDurationTimer() {
        durationTimer?.cancel()
        durationTimer = null
    }

    private fun updateDurationText() {
        val minutes = callDuration / 60
        val seconds = callDuration % 60
        durationText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateUI() {
        when (callState) {
            CallState.CONNECTING -> {
                callStateText.text = "Verbinden..."
                durationText.visibility = View.GONE
                connectedControls.visibility = View.VISIBLE
            }
            CallState.RINGING -> {
                callStateText.text = "Bellen..."
                durationText.visibility = View.GONE
                connectedControls.visibility = View.VISIBLE
            }
            CallState.CONNECTED -> {
                callStateText.text = "Verbonden"
                durationText.visibility = View.VISIBLE
                connectedControls.visibility = View.VISIBLE
            }
            CallState.ENDED -> {
                callStateText.text = "Gesprek beëindigd"
                durationText.visibility = View.GONE
                connectedControls.visibility = View.GONE
            }
        }

        // Update mute button
        if (isMuted) {
            muteButton.setImageResource(android.R.drawable.ic_lock_silent_mode)
            muteText.text = "Unmute"
        } else {
            muteButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            muteText.text = "Mute"
        }

        // Update speaker button
        if (isSpeakerOn) {
            speakerButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off)
            speakerText.text = "Speaker uit"
        } else {
            speakerButton.setImageResource(android.R.drawable.ic_btn_speak_now)
            speakerText.text = "Speaker"
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        updateUI()
        // TODO: Implement actual mute functionality with Agora
    }

    private fun toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn
        updateUI()
        // TODO: Implement actual speaker functionality with Agora
    }

    private fun endCall() {
        stopDurationTimer()
        callState = CallState.ENDED
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDurationTimer()
    }
}

