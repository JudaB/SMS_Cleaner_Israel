package com.example.sms_cleaner_israel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var mainScreenFragment: MainScreenFragment
    private lateinit var messagesFragment: MessagesFragment
    
    private val targetKeywords = listOf(
        "הלוואה",
        "יד שרה",
        "נמצאת זכאי לקרן הלוואה",
        "שקיות רפואי",
        "מבצע",
        "סקר",
        "לקוח"
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
        
        // Check if app is default SMS app first
        if (!isDefaultSmsApp()) {
            openDefaultSmsInstruction()
            return
        }
        
        initializeMainApp()
    }
    
    private fun initializeMainApp() {
        setContentView(R.layout.activity_main)
        
        setupViewPager()
        
        // Wait for fragments to be created before setting up callbacks
        viewPager.post {
            setupFragmentCallbacks()
            checkAllPermissions()
            checkSmsPermission()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Double-check if app is still default SMS app when returning to MainActivity
        if (!isDefaultSmsApp()) {
            Toast.makeText(this, "SMS Cleaner Israel is no longer the default SMS app. Redirecting to setup...", Toast.LENGTH_LONG).show()
            openDefaultSmsInstruction()
        }
    }
    
    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        
        // Ensure we start on the main screen (page 0)
        viewPager.currentItem = 0
    }
    
    private fun setupFragmentCallbacks() {
        try {
            // Get fragment references after they're created
            mainScreenFragment = viewPagerAdapter.getMainScreenFragment()
            messagesFragment = viewPagerAdapter.getMessagesFragment()
            
            // Set up click listeners for messages fragment
            messagesFragment.setClickListeners(
                onDelete = { smsMessage -> deleteSmsMessage(smsMessage) },
                onHide = { smsMessage -> hideSmsMessage(smsMessage) }
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up fragments: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Set up main screen callbacks
        mainScreenFragment.onReadSmsClick = {
            if (hasSmsPermission()) {
                readSmsMessages()
            } else {
                requestSmsPermission()
            }
        }
        
        mainScreenFragment.onReadSms2Click = {
            if (hasSmsPermission()) {
                readSmsMessages2()
            } else {
                requestSmsPermission()
            }
        }
        
        mainScreenFragment.onReadAllClick = {
            if (hasSmsPermission()) {
                readAllSmsMessages()
            } else {
                requestSmsPermission()
            }
        }
        
        mainScreenFragment.onOpenSettingsClick = {
            Toast.makeText(this, "Opening settings...", Toast.LENGTH_SHORT).show()
            openSettingsActivity()
        }
        
        mainScreenFragment.onOpenSmsSettingsClick = {
            Toast.makeText(this, "Opening SMS app chooser...", Toast.LENGTH_SHORT).show()
            openDefaultAppsSettings()
        }
        
        mainScreenFragment.onAutoDeleteLoansClick = {
            Toast.makeText(this, "Starting auto delete for loans and business messages", Toast.LENGTH_SHORT).show()
            autoDeleteLoansMessages()
        }
        
        mainScreenFragment.onAutoDeleteMedicalClick = {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteMedicalKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteMedicalMessages()
        }
        
        mainScreenFragment.onAutoDeletePromotionsClick = {
            Toast.makeText(this, "Starting auto delete for: $autoDeleteDealKeyword", Toast.LENGTH_SHORT).show()
            autoDeleteDealMessages()
        }
    }
    
    private fun checkSmsPermission() {
        if (!hasSmsPermission()) {
            mainScreenFragment.updateStatusText("SMS permission required to read messages")
            requestSmsPermission()
        } else {
            mainScreenFragment.updateStatusText("Ready to read SMS messages")
            // Count all SMS statistics when permission is granted
            updateAllCounters()
        }
    }
    
    private fun checkAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
        )
        
        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            Toast.makeText(this, "Missing permissions: ${missingPermissions.joinToString()}", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), SMS_PERMISSION_REQUEST_CODE)
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
                    mainScreenFragment.updateStatusText("Permission granted! Ready to read SMS messages")
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show()
                    // Count all SMS statistics after permission is granted
                    updateAllCounters()
                } else {
                    mainScreenFragment.updateStatusText("SMS permission denied. Cannot read messages.")
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun readSmsMessages() {
        smsList.clear()
        smsIdList.clear()
        mainScreenFragment.updateStatusText("Reading SMS messages...")
        
        // Check permission first
        if (!hasSmsPermission()) {
            mainScreenFragment.updateStatusText("SMS permission required to read messages")
            Toast.makeText(this, "SMS permission required. Please grant permission.", Toast.LENGTH_LONG).show()
            requestSmsPermission()
            return
        }
        
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
            
            if (cursor == null) {
                mainScreenFragment.updateStatusText("Error: Cannot access SMS content. Check permissions.")
                Toast.makeText(this, "Cannot access SMS content. Check if app is default SMS app.", Toast.LENGTH_LONG).show()
                return
            }
            
            cursor.use { c ->
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
            
            messagesFragment.updateMessages(smsList)
            mainScreenFragment.updateStatusText("Found ${smsList.size} messages containing target keywords")
            
        } catch (e: Exception) {
            val errorMessage = "Error reading SMS: ${e.message}"
            mainScreenFragment.updateStatusText(errorMessage)
            Toast.makeText(this, "Error reading SMS messages: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Check if it's a permission issue
            if (e.message?.contains("permission", ignoreCase = true) == true) {
                Toast.makeText(this, "Permission denied. Please grant SMS permission in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun readSmsMessages2() {
        smsList.clear()
        smsIdList.clear()
        mainScreenFragment.updateStatusText("Reading SMS messages for invoices and receipts...")
        
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
            
            var totalMessages = 0
            var foundMessages = 0
            
            cursor?.use { c ->
                val idIndex = c.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = c.getColumnIndex(Telephony.Sms.TYPE)
                
                while (c.moveToNext()) {
                    totalMessages++
                    val id = c.getLong(idIndex)
                    val address = c.getString(addressIndex) ?: "Unknown"
                    val body = c.getString(bodyIndex) ?: ""
                    val date = c.getLong(dateIndex)
                    val type = c.getInt(typeIndex)
                    
                    // Filter messages containing target keywords for invoices and receipts
                    if (containsTargetKeywords2(body)) {
                        foundMessages++
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
            
            messagesFragment.updateMessages(smsList)
            mainScreenFragment.updateStatusText("Business Messages: Found $foundMessages out of $totalMessages total messages")
            Toast.makeText(this, "Business Messages: Found $foundMessages out of $totalMessages total messages", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            mainScreenFragment.updateStatusText("Error reading SMS: ${e.message}")
            Toast.makeText(this, "Error reading SMS messages", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun readAllSmsMessages() {
        smsList.clear()
        smsIdList.clear()
        mainScreenFragment.updateStatusText("Reading all SMS messages...")
        
        // Check permission first
        if (!hasSmsPermission()) {
            mainScreenFragment.updateStatusText("SMS permission required to read messages")
            Toast.makeText(this, "SMS permission required. Please grant permission.", Toast.LENGTH_LONG).show()
            requestSmsPermission()
            return
        }
        
        try {
            // Use a more efficient approach - limit to recent messages first
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
                "${Telephony.Sms.DATE} DESC LIMIT 1000" // Limit to 1000 most recent messages
            )
            
            if (cursor == null) {
                mainScreenFragment.updateStatusText("Error: Cannot access SMS content. Check permissions.")
                Toast.makeText(this, "Cannot access SMS content. Check if app is default SMS app.", Toast.LENGTH_LONG).show()
                return
            }
            
            var totalMessages = 0
            val tempSmsList = mutableListOf<SmsMessage>()
            val tempSmsIdList = mutableListOf<Long>()
            
            cursor.use { c ->
                val idIndex = c.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = c.getColumnIndex(Telephony.Sms.TYPE)
                
                while (c.moveToNext()) {
                    totalMessages++
                    val id = c.getLong(idIndex)
                    val address = c.getString(addressIndex) ?: "Unknown"
                    val body = c.getString(bodyIndex) ?: ""
                    val date = c.getLong(dateIndex)
                    val type = c.getInt(typeIndex)
                    
                    // Add all messages without filtering
                    val smsMessage = SmsMessage(
                        id = id,
                        address = address,
                        body = body,
                        date = date,
                        type = type
                    )
                    tempSmsList.add(smsMessage)
                    tempSmsIdList.add(id)
                    
                    // Update progress every 100 messages
                    if (totalMessages % 100 == 0) {
                        mainScreenFragment.updateStatusText("Reading messages... $totalMessages loaded")
                    }
                }
            }
            
            // Update the lists atomically
            smsList.addAll(tempSmsList)
            smsIdList.addAll(tempSmsIdList)
            
            messagesFragment.updateMessages(smsList)
            mainScreenFragment.updateStatusText("All Messages: Found $totalMessages total messages")
            Toast.makeText(this, "All Messages: Found $totalMessages total messages", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            val errorMessage = "Error reading SMS: ${e.message}"
            mainScreenFragment.updateStatusText(errorMessage)
            Toast.makeText(this, "Error reading SMS messages: ${e.message}", Toast.LENGTH_LONG).show()
            
            // Try fallback with smaller limit
            try {
                mainScreenFragment.updateStatusText("Trying with smaller batch...")
                readAllSmsMessagesFallback()
            } catch (fallbackException: Exception) {
                mainScreenFragment.updateStatusText("Failed to load messages: ${fallbackException.message}")
                Toast.makeText(this, "Failed to load messages. Try using filtered options instead.", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun readAllSmsMessagesFallback() {
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
                "${Telephony.Sms.DATE} DESC LIMIT 100" // Much smaller limit
            )
            
            if (cursor == null) {
                mainScreenFragment.updateStatusText("Error: Cannot access SMS content.")
                return
            }
            
            var totalMessages = 0
            val tempSmsList = mutableListOf<SmsMessage>()
            val tempSmsIdList = mutableListOf<Long>()
            
            cursor.use { c ->
                val idIndex = c.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)
                val typeIndex = c.getColumnIndex(Telephony.Sms.TYPE)
                
                while (c.moveToNext()) {
                    totalMessages++
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
                    tempSmsList.add(smsMessage)
                    tempSmsIdList.add(id)
                }
            }
            
            smsList.addAll(tempSmsList)
            smsIdList.addAll(tempSmsIdList)
            
            messagesFragment.updateMessages(smsList)
            mainScreenFragment.updateStatusText("Recent Messages: Found $totalMessages messages (limited to recent)")
            Toast.makeText(this, "Recent Messages: Found $totalMessages messages (limited to recent)", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            throw e // Re-throw to be caught by the main function
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
            // Use regex to find complete words, not parts of words
            val regex = "\\b${Regex.escape(keyword.lowercase())}\\b"
            regex.toRegex().containsMatchIn(lowerText)
        }
    }
    
    private fun isDefaultSmsApp(): Boolean {
        try {
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            
            // Handle null case - if null, no default SMS app is set
            if (defaultSmsPackage == null) {
                // Try alternative detection - if we can access SMS content, we're likely the default
                return checkSmsAccess()
            }
            
            return defaultSmsPackage == packageName
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun checkSmsAccess(): Boolean {
        return try {
            val resolver = contentResolver
            val uri = android.net.Uri.parse("content://sms")
            val cursor = resolver.query(uri, arrayOf("_id"), null, null, null)
            cursor?.use {
                it.count >= 0 // If we can access SMS content, we're likely the default SMS app
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun openDefaultSmsInstruction() {
        try {
            val intent = Intent(this, DefaultSmsInstructionActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity since user needs to set up default SMS app first
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening instruction activity: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openSettingsActivity() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
            messagesFragment.updateMessages(smsList)
            
            if (deletedRows > 0) {
                mainScreenFragment.updateStatusText("Message deleted. ${smsList.size} messages remaining.")
                Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                mainScreenFragment.updateStatusText("Message hidden from view. ${smsList.size} messages remaining.")
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
        messagesFragment.updateMessages(smsList)
        mainScreenFragment.updateStatusText("Message hidden. ${smsList.size} messages remaining.")
        Toast.makeText(this, "Message hidden from view", Toast.LENGTH_SHORT).show()
    }
    
    private fun autoDeleteLoansMessages() {
        mainScreenFragment.updateStatusText("Searching for loan and business messages...")
        
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
                    
                    // Check if message contains business or loan keywords
                    if (body.lowercase().contains(autoDeleteKeyword.lowercase()) || 
                        body.lowercase().contains(autoDeleteLoanKeyword.lowercase())) {
                        foundCount++
                        messagesToDelete.add(Pair(id, address))
                    }
                }
            }
            
            // Show count of found messages
            mainScreenFragment.updateStatusText("Found $foundCount loan and business messages")
            Toast.makeText(this, "Found $foundCount loan and business messages. Starting deletion...", Toast.LENGTH_LONG).show()
            
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
            
            mainScreenFragment.updateStatusText("Auto delete complete. Found: $foundCount, Deleted: $deletedCount")
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            mainScreenFragment.updateStatusText("Auto delete error: ${e.message}")
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun autoDeleteMedicalMessages() {
        mainScreenFragment.updateStatusText("Searching for messages containing: $autoDeleteMedicalKeyword")
        
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
            mainScreenFragment.updateStatusText("Found $foundCount messages with keyword: $autoDeleteMedicalKeyword")
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
            
            mainScreenFragment.updateStatusText("Auto delete complete. Found: $foundCount, Deleted: $deletedCount")
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            mainScreenFragment.updateStatusText("Auto delete error: ${e.message}")
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun autoDeleteDealMessages() {
        mainScreenFragment.updateStatusText("Searching for messages containing: $autoDeleteDealKeyword")
        
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
            mainScreenFragment.updateStatusText("Found $foundCount messages with keyword: $autoDeleteDealKeyword")
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
            
            mainScreenFragment.updateStatusText("Auto delete complete. Found: $foundCount, Deleted: $deletedCount")
            Toast.makeText(this, "Auto delete complete! Found: $foundCount, Deleted: $deletedCount", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            mainScreenFragment.updateStatusText("Auto delete error: ${e.message}")
            Toast.makeText(this, "Auto delete error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun countTotalSmsMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID),
                null,
                null,
                null
            )
            
            var totalCount = 0
            cursor?.use { c ->
                totalCount = c.count
            }
            
            mainScreenFragment.updateStatusText("Ready to read SMS messages (Total: $totalCount messages)")
            mainScreenFragment.updateTotalCount(totalCount)
            Toast.makeText(this, "Total SMS messages: $totalCount", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            mainScreenFragment.updateStatusText("Error counting SMS messages: ${e.message}")
            Toast.makeText(this, "Error counting SMS messages", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun countSpamMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
                null,
                null,
                null
            )
            
            var spamCount = 0
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: ""
                    if (containsTargetKeywords(body)) {
                        spamCount++
                    }
                }
            }
            
            mainScreenFragment.updateSpamCount(spamCount)
            
        } catch (e: Exception) {
            mainScreenFragment.updateSpamCount(-1) // Error indicator
        }
    }
    
    private fun countBusinessMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
                null,
                null,
                null
            )
            
            var businessCount = 0
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: ""
                    if (containsTargetKeywords2(body)) {
                        businessCount++
                    }
                }
            }
            
            mainScreenFragment.updateBusinessCount(businessCount)
            
        } catch (e: Exception) {
            mainScreenFragment.updateBusinessCount(-1) // Error indicator
        }
    }
    
    private fun countLoansMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
                null,
                null,
                null
            )
            
            var loansCount = 0
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: ""
                    // Check for both business and loan keywords
                    if (body.lowercase().contains(autoDeleteKeyword.lowercase()) || 
                        body.lowercase().contains(autoDeleteLoanKeyword.lowercase())) {
                        loansCount++
                    }
                }
            }
            
            mainScreenFragment.updateLoansCount(loansCount)
            
        } catch (e: Exception) {
            mainScreenFragment.updateLoansCount(-1) // Error indicator
        }
    }
    
    private fun countMedicalMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
                null,
                null,
                null
            )
            
            var medicalCount = 0
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: ""
                    if (body.lowercase().contains(autoDeleteMedicalKeyword.lowercase())) {
                        medicalCount++
                    }
                }
            }
            
            mainScreenFragment.updateMedicalCount(medicalCount)
            
        } catch (e: Exception) {
            mainScreenFragment.updateMedicalCount(-1) // Error indicator
        }
    }
    
    private fun countPromotionsMessages() {
        try {
            val uri = Uri.parse("content://sms")
            val cursor: Cursor? = contentResolver.query(
                uri,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.BODY),
                null,
                null,
                null
            )
            
            var promotionsCount = 0
            cursor?.use { c ->
                val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
                while (c.moveToNext()) {
                    val body = c.getString(bodyIndex) ?: ""
                    if (body.lowercase().contains(autoDeleteDealKeyword.lowercase())) {
                        promotionsCount++
                    }
                }
            }
            
            mainScreenFragment.updatePromotionsCount(promotionsCount)
            
        } catch (e: Exception) {
            mainScreenFragment.updatePromotionsCount(-1) // Error indicator
        }
    }
    
    private fun updateAllCounters() {
        try {
            countTotalSmsMessages()
            countSpamMessages()
            countBusinessMessages()
            countLoansMessages()
            countMedicalMessages()
            countPromotionsMessages()
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating counters: ${e.message}", Toast.LENGTH_SHORT).show()
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