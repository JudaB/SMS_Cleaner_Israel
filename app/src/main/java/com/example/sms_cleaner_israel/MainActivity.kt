package com.example.sms_cleaner_israel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var readSmsButton: Button
    private lateinit var readSmsButton2: Button
    private lateinit var setDefaultButton: Button
    private lateinit var openSettingsButton: Button
    private lateinit var autoDeleteButton: Button
    private lateinit var autoDeleteLoanButton: Button
    private lateinit var autoDeleteMedicalButton: Button
    private lateinit var autoDeleteDealButton: Button
    private lateinit var smsRecyclerView: RecyclerView
    private lateinit var statusText: TextView
    private lateinit var smsAdapter: SmsAdapter
    
    private val targetKeywords = listOf(
        "הלוואה",
        "יד שרה",
        "נמצאת זכאי לקרן הלוואה",
        "שקיות רפואי",
        "מבצע"
    )
    
    private val targetKeywords2 = listOf(
        "חשבונית",
        "קבלה"
    )
    
    private val autoDeleteKeyword = "בעל עסק הודעה חשובה"
    private val autoDeleteLoanKeyword = "הלוואה"
    private val autoDeleteMedicalKeyword = "שקיות רפואי"
    private val autoDeleteDealKeyword = "מבצע"
    private val smsList = mutableListOf<SmsMessage>()
    private val smsIdList = mutableListOf<Long>() // Store SMS IDs for deletion
    
    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupRecyclerView()
        checkSmsPermission()
    }
    
    private fun initializeViews() {
        readSmsButton = findViewById(R.id.btnReadSms)
        readSmsButton2 = findViewById(R.id.btnReadSms2)
        setDefaultButton = findViewById(R.id.btnSetDefault)
        openSettingsButton = findViewById(R.id.btnOpenSettings)
        autoDeleteButton = findViewById(R.id.btnAutoDelete)
        autoDeleteLoanButton = findViewById(R.id.btnAutoDeleteLoan)
        autoDeleteMedicalButton = findViewById(R.id.btnAutoDeleteMedical)
        autoDeleteDealButton = findViewById(R.id.btnAutoDeleteDeal)
        smsRecyclerView = findViewById(R.id.recyclerViewSms)
        statusText = findViewById(R.id.tvStatus)
        
        // Debug: Check if button is found
        if (setDefaultButton == null) {
            Toast.makeText(this, "Button not found!", Toast.LENGTH_LONG).show()
            return
        }
        
        // Ensure buttons are enabled and clickable
        readSmsButton.isEnabled = true
        readSmsButton2.isEnabled = true
        readSmsButton2.isClickable = true
        setDefaultButton.isEnabled = true
        setDefaultButton.isClickable = true
        openSettingsButton.isEnabled = true
        openSettingsButton.isClickable = true
        autoDeleteButton.isEnabled = true
        autoDeleteButton.isClickable = true
        autoDeleteLoanButton.isEnabled = true
        autoDeleteLoanButton.isClickable = true
        autoDeleteMedicalButton.isEnabled = true
        autoDeleteMedicalButton.isClickable = true
        autoDeleteDealButton.isEnabled = true
        autoDeleteDealButton.isClickable = true
        
        readSmsButton.setOnClickListener {
            if (hasSmsPermission()) {
                readSmsMessages()
            } else {
                requestSmsPermission()
            }
        }
        
        readSmsButton2.setOnClickListener {
            if (hasSmsPermission()) {
                readSmsMessages2()
            } else {
                requestSmsPermission()
            }
        }
        
        setDefaultButton.setOnClickListener {
            Toast.makeText(this, "Set Default button clicked", Toast.LENGTH_SHORT).show()
            requestSetAsDefaultSmsApp()
        }
        
        openSettingsButton.setOnClickListener {
            Toast.makeText(this, "Opening default apps settings...", Toast.LENGTH_SHORT).show()
            openDefaultAppsSettings()
        }
        
        autoDeleteButton.setOnClickListener {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteMessages()
        }
        
        autoDeleteLoanButton.setOnClickListener {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteLoanKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteLoanMessages()
        }
        
        autoDeleteMedicalButton.setOnClickListener {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteMedicalKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteMedicalMessages()
        }
        
        autoDeleteDealButton.setOnClickListener {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteDealKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteDealMessages()
        }
        
        // Debug: Test if button is working
        Toast.makeText(this, "Button initialized successfully", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupRecyclerView() {
        smsAdapter = SmsAdapter(
            smsList,
            onDeleteClick = { smsMessage ->
                deleteSmsMessage(smsMessage)
            },
            onHideClick = { smsMessage ->
                hideSmsMessage(smsMessage)
            }
        )
        smsRecyclerView.layoutManager = LinearLayoutManager(this)
        smsRecyclerView.adapter = smsAdapter
    }
    
    private fun checkSmsPermission() {
        if (!hasSmsPermission()) {
            statusText.text = "SMS permission required to read messages"
            requestSmsPermission()
        } else {
            statusText.text = "Ready to read SMS messages"
        }
    }
    
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
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
                    statusText.text = "Permission granted! Ready to read SMS messages"
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    statusText.text = "SMS permission denied. Cannot read messages."
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun readSmsMessages() {
        smsList.clear()
        smsIdList.clear()
        statusText.text = "Reading SMS messages..."
        
        try {
            val uri = Uri.parse("content://sms/inbox")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
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
                    
                    // Filter messages containing target keywords
                    if (containsTargetKeywords(body)) {
                        val smsMessage = SmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            date = date,
                            type = type
                        )
                        smsList.add(smsMessage)
                        smsIdList.add(id)
                    }
                }
            }
            
            smsAdapter.notifyDataSetChanged()
            statusText.text = "Found ${smsList.size} messages containing target keywords"
            
        } catch (e: Exception) {
            statusText.text = "Error reading SMS: ${e.message}"
            Toast.makeText(this, "Error reading SMS messages", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun readSmsMessages2() {
        smsList.clear()
        smsIdList.clear()
        statusText.text = "Reading SMS messages for invoices and receipts..."
        
        try {
            val uri = Uri.parse("content://sms/inbox")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
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
                    
                    // Filter messages containing target keywords for invoices and receipts
                    if (containsTargetKeywords2(body)) {
                        val smsMessage = SmsMessage(
                            id = id,
                            address = address,
                            body = body,
                            date = date,
                            type = type
                        )
                        smsList.add(smsMessage)
                        smsIdList.add(id)
                    }
                }
            }
            
            smsAdapter.notifyDataSetChanged()
            statusText.text = "Found ${smsList.size} messages containing invoice/receipt keywords"
            
        } catch (e: Exception) {
            statusText.text = "Error reading SMS: ${e.message}"
            Toast.makeText(this, "Error reading SMS messages", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun containsTargetKeywords(text: String): Boolean {
        val lowerText = text.lowercase()
        return targetKeywords.any { keyword ->
            lowerText.contains(keyword.lowercase())
        }
    }
    
    private fun containsTargetKeywords2(text: String): Boolean {
        val lowerText = text.lowercase()
        return targetKeywords2.any { keyword ->
            lowerText.contains(keyword.lowercase())
        }
    }
    
    private fun requestSetAsDefaultSmsApp() {
        try {
            val currentDefault = Telephony.Sms.getDefaultSmsPackage(this)
            Toast.makeText(this, "Current default SMS app: $currentDefault", Toast.LENGTH_SHORT).show()
            
            if (currentDefault != packageName) {
                // Method 1: Try the standard SMS change intent
                try {
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    startActivity(intent)
                    Toast.makeText(this, "Opening SMS app selection...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Method 1 failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    
                    // Method 2: Try opening app settings directly
                    try {
                        val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        settingsIntent.data = Uri.parse("package:$packageName")
                        startActivity(settingsIntent)
                        Toast.makeText(this, "Opening app settings...", Toast.LENGTH_SHORT).show()
                    } catch (e2: Exception) {
                        Toast.makeText(this, "Method 2 failed: ${e2.message}", Toast.LENGTH_SHORT).show()
                        
                        // Method 3: Try opening default apps settings
                        try {
                            val defaultAppsIntent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                            startActivity(defaultAppsIntent)
                            Toast.makeText(this, "Opening default apps settings...", Toast.LENGTH_SHORT).show()
                        } catch (e3: Exception) {
                            Toast.makeText(this, "All methods failed. Please manually set as default SMS app in Settings > Apps > Default apps > SMS app", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "App is already the default SMS app", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun isDefaultSmsApp(): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(this) == packageName
    }
    
    private fun openDefaultAppsSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Navigate to 'SMS app' and select 'SMS Cleaner Israel'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun deleteSmsMessage(smsMessage: SmsMessage) {
        try {
            // Try multiple deletion methods
            var deletedRows = 0
            
            // Method 1: Try deleting from inbox
            try {
                val inboxUri = Uri.parse("content://sms/inbox")
                deletedRows = contentResolver.delete(
                    inboxUri,
                    "${Telephony.Sms._ID} = ?",
                    arrayOf(smsMessage.id.toString())
                )
                Toast.makeText(this, "Inbox deletion result: $deletedRows", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Inbox deletion failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            
            // Method 2: Try general SMS URI if inbox failed
            if (deletedRows == 0) {
                try {
                    val smsUri = Uri.parse("content://sms")
                    deletedRows = contentResolver.delete(
                        smsUri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(smsMessage.id.toString())
                    )
                    Toast.makeText(this, "General SMS deletion result: $deletedRows", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "General SMS deletion failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Method 3: Try with conversation URI
            if (deletedRows == 0) {
                try {
                    val conversationUri = Uri.parse("content://sms/conversations")
                    deletedRows = contentResolver.delete(
                        conversationUri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(smsMessage.id.toString())
                    )
                    Toast.makeText(this, "Conversation deletion result: $deletedRows", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Conversation deletion failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Update UI regardless of deletion result
            smsList.remove(smsMessage)
            smsIdList.remove(smsMessage.id)
            smsAdapter.notifyDataSetChanged()
            
            if (deletedRows > 0) {
                statusText.text = "Message deleted. ${smsList.size} messages remaining."
                Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                statusText.text = "Message hidden from view. ${smsList.size} messages remaining."
                Toast.makeText(this, "Message hidden (deletion may have failed)", Toast.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hideSmsMessage(smsMessage: SmsMessage) {
        // Simply remove from local lists without trying to delete from system
        smsList.remove(smsMessage)
        smsIdList.remove(smsMessage.id)
        
        // Update UI
        smsAdapter.notifyDataSetChanged()
        statusText.text = "Message hidden. ${smsList.size} messages remaining."
        Toast.makeText(this, "Message hidden from view", Toast.LENGTH_SHORT).show()
    }
    
    private fun autoDeleteMessages() {
        statusText.text = "Searching for messages containing: $autoDeleteKeyword"
        
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
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
            var foundCount = 0
            val messagesToDelete = mutableListOf<Pair<Long, String>>() // ID and sender
            
            // First pass: Count and collect messages to delete
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
                    
                    // Check if message contains the auto delete keyword
                    if (body.lowercase().contains(autoDeleteKeyword.lowercase())) {
                        foundCount++
                        messagesToDelete.add(Pair(id, address))
                    }
                }
            }
            
            // Show count of found messages
            statusText.text = "Found $foundCount messages with keyword: $autoDeleteKeyword"
            Toast.makeText(this, "Found $foundCount messages containing '$autoDeleteKeyword'. Starting deletion...", Toast.LENGTH_LONG).show()
            
            // Second pass: Delete the messages
            var deletedCount = 0
            for ((id, address) in messagesToDelete) {
                try {
                    val deletedRows = contentResolver.delete(
                        uri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(id.toString())
                    )
                    
                    if (deletedRows > 0) {
                        deletedCount++
                        Toast.makeText(this, "Deleted message from: $address", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to delete message from $address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            statusText.text = "Auto delete complete. Found: $foundCount, Deleted: $deletedCount"
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            statusText.text = "Auto delete error: ${e.message}"
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun autoDeleteLoanMessages() {
        statusText.text = "Searching for messages containing: $autoDeleteLoanKeyword"
        
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
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
            var foundCount = 0
            val messagesToDelete = mutableListOf<Pair<Long, String>>() // ID and sender
            
            // First pass: Count and collect messages to delete
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
                    
                    // Check if message contains the auto delete keyword
                    if (body.lowercase().contains(autoDeleteLoanKeyword.lowercase())) {
                        foundCount++
                        messagesToDelete.add(Pair(id, address))
                    }
                }
            }
            
            // Show count of found messages
            statusText.text = "Found $foundCount messages with keyword: $autoDeleteLoanKeyword"
            Toast.makeText(this, "Found $foundCount messages containing '$autoDeleteLoanKeyword'. Starting deletion...", Toast.LENGTH_LONG).show()
            
            // Second pass: Delete the messages
            var deletedCount = 0
            for ((id, address) in messagesToDelete) {
                try {
                    val deletedRows = contentResolver.delete(
                        uri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(id.toString())
                    )
                    
                    if (deletedRows > 0) {
                        deletedCount++
                        Toast.makeText(this, "Deleted message from: $address", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to delete message from $address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            statusText.text = "Auto delete complete. Found: $foundCount, Deleted: $deletedCount"
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            statusText.text = "Auto delete error: ${e.message}"
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun autoDeleteMedicalMessages() {
        statusText.text = "Searching for messages containing: $autoDeleteMedicalKeyword"
        
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
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
            var foundCount = 0
            val messagesToDelete = mutableListOf<Pair<Long, String>>() // ID and sender
            
            // First pass: Count and collect messages to delete
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
                    
                    // Check if message contains the auto delete keyword
                    if (body.lowercase().contains(autoDeleteMedicalKeyword.lowercase())) {
                        foundCount++
                        messagesToDelete.add(Pair(id, address))
                    }
                }
            }
            
            // Show count of found messages
            statusText.text = "Found $foundCount messages with keyword: $autoDeleteMedicalKeyword"
            Toast.makeText(this, "Found $foundCount messages containing '$autoDeleteMedicalKeyword'. Starting deletion...", Toast.LENGTH_LONG).show()
            
            // Second pass: Delete the messages
            var deletedCount = 0
            for ((id, address) in messagesToDelete) {
                try {
                    val deletedRows = contentResolver.delete(
                        uri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(id.toString())
                    )
                    
                    if (deletedRows > 0) {
                        deletedCount++
                        Toast.makeText(this, "Deleted message from: $address", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to delete message from $address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            statusText.text = "Auto delete complete. Found: $foundCount, Deleted: $deletedCount"
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            statusText.text = "Auto delete error: ${e.message}"
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun autoDeleteDealMessages() {
        statusText.text = "Searching for messages containing: $autoDeleteDealKeyword"
        
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
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )
            
            var foundCount = 0
            val messagesToDelete = mutableListOf<Pair<Long, String>>() // ID and sender
            
            // First pass: Count and collect messages to delete
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
                    
                    // Check if message contains the auto delete keyword
                    if (body.lowercase().contains(autoDeleteDealKeyword.lowercase())) {
                        foundCount++
                        messagesToDelete.add(Pair(id, address))
                    }
                }
            }
            
            // Show count of found messages
            statusText.text = "Found $foundCount messages with keyword: $autoDeleteDealKeyword"
            Toast.makeText(this, "Found $foundCount messages containing '$autoDeleteDealKeyword'. Starting deletion...", Toast.LENGTH_LONG).show()
            
            // Second pass: Delete the messages
            var deletedCount = 0
            for ((id, address) in messagesToDelete) {
                try {
                    val deletedRows = contentResolver.delete(
                        uri,
                        "${Telephony.Sms._ID} = ?",
                        arrayOf(id.toString())
                    )
                    
                    if (deletedRows > 0) {
                        deletedCount++
                        Toast.makeText(this, "Deleted message from: $address", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to delete message from $address: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            statusText.text = "Auto delete complete. Found: $foundCount, Deleted: $deletedCount"
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            statusText.text = "Auto delete error: ${e.message}"
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(date))
    }
    
    fun getTypeString(): String {
        return when (type) {
            Telephony.Sms.MESSAGE_TYPE_INBOX -> "Received"
            Telephony.Sms.MESSAGE_TYPE_SENT -> "Sent"
            else -> "Other"
        }
    }
}
