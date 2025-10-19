package com.example.sms_cleaner_israel

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SmsConversationActivity : AppCompatActivity() {
    
    private lateinit var conversationTextView: TextView
    private lateinit var backButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_conversation)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        conversationTextView = findViewById(R.id.tvConversation)
        backButton = findViewById(R.id.btnBack)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }
}