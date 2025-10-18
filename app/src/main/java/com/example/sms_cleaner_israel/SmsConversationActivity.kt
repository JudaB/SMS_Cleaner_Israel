package com.example.sms_cleaner_israel

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class SmsConversationActivity : AppCompatActivity() {
    
    private lateinit var conversationRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var backButton: Button
    private lateinit var contactTextView: TextView
    
    private var contactNumber: String = ""
    private val conversationList = mutableListOf<SmsMessage>()
    private lateinit var conversationAdapter: SmsAdapter
    
    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 300
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_conversation)
        
        initializeViews()
        handleIntent()
        loadConversation()
        checkSmsPermission()
    }
    
    private fun initializeViews() {
        conversationRecyclerView = findViewById(R.id.recyclerViewConversation)
        messageEditText = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend)
        backButton = findViewById(R.id.btnBack)
        contactTextView = findViewById(R.id.tvContact)
        
        sendButton.setOnClickListener {
            sendSms()
        }
        
        backButton.setOnClickListener {
            finish()
        }
        
        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = SmsAdapter(
            conversationList,
            onDeleteClick = { smsMessage ->
                // Handle delete in conversation
                Toast.makeText(this, "Delete not available in conversation view", Toast.LENGTH_SHORT).show()
            },
            onHideClick = { smsMessage ->
                // Handle hide in conversation
                Toast.makeText(this, "Hide not available in conversation view", Toast.LENGTH_SHORT).show()
            }
        )
        conversationRecyclerView.layoutManager = LinearLayoutManager(this)
        conversationRecyclerView.adapter = conversationAdapter
    }
    
    private fun handleIntent() {
        val intent = intent
        val data = intent.data
        
        if (data != null) {
            when (data.scheme) {
                "sms", "smsto" -> {
                    contactNumber = data.schemeSpecificPart
                    contactTextView.text = "Conversation with: $contactNumber"
                }
            }
        }
    }
    
    private fun loadConversation() {
        if (contactNumber.isEmpty()) {
            Toast.makeText(this, "No contact number provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(contactNumber),
                "${Telephony.Sms.DATE} ASC"
            )
            
            conversationList.clear()
            cursor?.use { c ->
                val idIndex = c.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = c.getColumnIndex(Telephony.Sms.TYPE)
                
                while (c.moveToNext()) {
                    val id = c.getLong(idIndex)
                    val address = c.getString(addressIndex) ?: "Unknown"
                    val body = c.getString(bodyIndex) ?: ""
                    val date = c.getLong(dateIndex)
                    val type = c.getInt(typeIndex)
                    
                    val smsMessage = SmsMessage(
                        id = id,
                        address = address,
                        body = body,
                        date = date,
                        type = type
                    )
                    conversationList.add(smsMessage)
                }
            }
            
            conversationAdapter.notifyDataSetChanged()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading conversation: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val message = messageEditText.text.toString().trim()
        
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
                val smsManager = android.telephony.SmsManager.getDefault()
                smsManager.sendTextMessage(contactNumber, null, message, null, null)
                Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show()
                messageEditText.text.clear()
                loadConversation() // Refresh conversation
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "SMS permission required", Toast.LENGTH_SHORT).show()
        }
    }
}
