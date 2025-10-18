package com.example.sms_cleaner_israel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SmsComposeActivity : AppCompatActivity() {
    
    private lateinit var recipientEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: Button
    
    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 200
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_compose)
        
        initializeViews()
        handleIntent()
        checkSmsPermission()
    }
    
    private fun initializeViews() {
        recipientEditText = findViewById(R.id.etRecipient)
        messageEditText = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend)
        backButton = findViewById(R.id.btnBack)
        
        sendButton.setOnClickListener {
            sendSms()
        }
        
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun handleIntent() {
        val intent = intent
        val data = intent.data
        
        if (data != null) {
            when (data.scheme) {
                "sms", "smsto" -> {
                    val phoneNumber = data.schemeSpecificPart
                    if (phoneNumber.isNotEmpty()) {
                        recipientEditText.setText(phoneNumber)
                    }
                }
            }
        }
    }
    
    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun sendSms() {
        val recipient = recipientEditText.text.toString().trim()
        val message = messageEditText.text.toString().trim()
        
        if (recipient.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(recipient, null, message, null, null)
                Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show()
        }
    }
}
