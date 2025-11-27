package com.example.raptor.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raptor.R
import com.example.raptor.models.ChatMessage
import com.example.raptor.viewmodels.ChatState
import com.example.raptor.viewmodels.ChatViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * Order Chat Activity
 * Komt overeen met iOS CustomerOrderChatView
 */
class OrderChatActivity : AppCompatActivity() {
    
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_chat)
        
        val orderId = intent.getStringExtra("order_id") ?: return
        val customerEmail = intent.getStringExtra("customer_email")
        val driverEmail = intent.getStringExtra("driver_email")
        val driverName = intent.getStringExtra("driver_name") ?: "Bezorger"
        
        supportActionBar?.title = "Chat met $driverName"
        
        initializeViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        
        // Initialize chat
        viewModel.initializeChat(orderId, customerEmail, driverEmail)
        
        // Mark as read when opening
        viewModel.markAsRead()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopPolling()
    }
    
    private fun initializeViews() {
        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
    }
    
    private fun setupRecyclerView() {
        adapter = ChatAdapter { }
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Start from bottom
        }
        recyclerView.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            // Scroll to bottom
            if (messages.isNotEmpty()) {
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
        
        viewModel.chatState.observe(this) { state ->
            when (state) {
                is ChatState.Idle -> {}
                is ChatState.Loading -> {
                    sendButton.isEnabled = false
                }
                is ChatState.Sending -> {
                    sendButton.isEnabled = false
                    messageInput.isEnabled = false
                }
                is ChatState.Success -> {
                    sendButton.isEnabled = true
                    messageInput.isEnabled = true
                    messageInput.text?.clear()
                }
                is ChatState.Error -> {
                    sendButton.isEnabled = true
                    messageInput.isEnabled = true
                    Toast.makeText(this, "Fout: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = messageInput.text?.toString()?.trim() ?: ""
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
            }
        }
    }
}

/**
 * Chat Adapter
 */
class ChatAdapter(
    private val onMessageClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<ChatViewHolder>() {
    
    private var messages: List<ChatMessage> = emptyList()
    private var customerEmail: String? = null
    
    fun setCustomerEmail(email: String?) {
        customerEmail = email
    }
    
    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ChatViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        val isFromCustomer = message.senderEmail == customerEmail
        holder.bind(message, isFromCustomer)
    }
    
    override fun getItemCount(): Int = messages.size
}

/**
 * Chat ViewHolder
 */
class ChatViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
    
    private val messageText: android.widget.TextView = itemView.findViewById(R.id.messageText)
    private val messageTime: android.widget.TextView = itemView.findViewById(R.id.messageTime)
    private val messageContainer: android.view.View = itemView.findViewById(R.id.messageContainer)
    
    fun bind(message: ChatMessage, isFromCustomer: Boolean) {
        messageText.text = message.body
        messageTime.text = formatTime(message.createdAt)
        
        // Set alignment based on sender
        val layoutParams = messageContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        if (isFromCustomer) {
            layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            messageContainer.setBackgroundResource(R.drawable.chat_bubble_sent)
        } else {
            layoutParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            messageContainer.setBackgroundResource(R.drawable.chat_bubble_received)
        }
        messageContainer.layoutParams = layoutParams
    }
    
    private fun formatTime(timeString: String): String {
        // Simple time formatting - can be improved
        return try {
            timeString.takeLast(8) // Show last 8 chars (HH:MM:SS)
        } catch (e: Exception) {
            timeString
        }
    }
}

