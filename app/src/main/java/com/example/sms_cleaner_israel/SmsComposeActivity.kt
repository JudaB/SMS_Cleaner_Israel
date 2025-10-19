package com.example.sms_cleaner_israel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SmsComposeActivity : AppCompatActivity() {
    
    private lateinit var recipientEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var cancelButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_compose)
        
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        recipientEditText = findViewById(R.id.etRecipient)
        messageEditText = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend)
        cancelButton = findViewById(R.id.btnCancel)
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            // Handle send SMS
            finish()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
}