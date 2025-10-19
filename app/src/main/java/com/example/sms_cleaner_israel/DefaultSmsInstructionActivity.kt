package com.example.sms_cleaner_israel

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DefaultSmsInstructionActivity : AppCompatActivity() {
    
    private lateinit var instructionText: TextView
    private lateinit var smsSettingsButton: Button
    private lateinit var continueButton: Button
    private lateinit var checkCapabilityButton: Button
    private lateinit var testSmsAccessButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_sms_instruction)
        
        initializeViews()
        setupClickListeners()
        updateInstructionText()
    }
    
    private fun initializeViews() {
        instructionText = findViewById(R.id.tvInstruction)
        smsSettingsButton = findViewById(R.id.btnSmsSettings)
        continueButton = findViewById(R.id.btnContinue)
        checkCapabilityButton = findViewById(R.id.btnCheckCapability)
        testSmsAccessButton = findViewById(R.id.btnTestSmsAccess)
    }
    
    private fun setupClickListeners() {
        smsSettingsButton.setOnClickListener {
            openSmsSettings()
        }
        
        continueButton.setOnClickListener {
            checkDefaultSmsStatus()
        }
        
        checkCapabilityButton.setOnClickListener {
            checkAppSmsCapability()
        }
        
        testSmsAccessButton.setOnClickListener {
            testSmsAccess()
        }
    }
    
    private fun checkDefaultSmsStatus() {
        // Add a small delay to ensure the system has updated
        continueButton.postDelayed({
            // Try multiple detection methods
            val isDefault1 = isDefaultSmsApp()
            val isDefault2 = checkSmsAccess()
            val isDefault3 = checkDefaultSmsAlternative() != null
            
            val debugMessage = """
                Default SMS Detection Results:
                
                Method 1 (Telephony): $isDefault1
                Method 2 (SMS Access): $isDefault2
                Method 3 (Alternative): $isDefault3
                
                Any method shows default: ${isDefault1 || isDefault2 || isDefault3}
            """.trimIndent()
            
            if (isDefault1 || isDefault2 || isDefault3) {
                showDebugDialog("Success!", "Great! App is now set as default SMS app.\n\n$debugMessage")
                // Start MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                showDebugDialog("Setup Required", "Please set SMS Cleaner Israel as your default SMS app first.\n\n$debugMessage")
                // Update instruction text to be more specific
                updateInstructionTextWithRetry()
            }
        }, 500) // 500ms delay to allow system to update
    }
    
    private fun checkSmsAccess(): Boolean {
        return try {
            val resolver = contentResolver
            val uri = android.net.Uri.parse("content://sms")
            val cursor = resolver.query(uri, arrayOf("_id"), null, null, null)
            cursor?.use {
                it.count > 0 // If we can access SMS content, we're likely the default SMS app
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun testSmsAccess() {
        try {
            val resolver = contentResolver
            val uri = android.net.Uri.parse("content://sms")
            val cursor = resolver.query(uri, arrayOf("_id", "address", "body"), null, null, null)
            
            val debugMessage = """
                SMS Access Test Results:
                
                Can access SMS content: ${cursor != null}
                SMS messages found: ${cursor?.count ?: 0}
                
                Test Details:
                - Content URI: content://sms
                - Query successful: ${cursor != null}
                - Cursor count: ${cursor?.count ?: 0}
                
                If you can access SMS content, the app is likely set as default SMS app.
                If you get permission denied, the app is not the default SMS app.
            """.trimIndent()
            
            showDebugDialog("SMS Access Test", debugMessage)
            
            cursor?.close()
        } catch (e: Exception) {
            val debugMessage = """
                SMS Access Test Failed:
                
                Error: ${e.message}
                Error Type: ${e.javaClass.simpleName}
                
                This usually means:
                - App is not set as default SMS app
                - Missing SMS permissions
                - System security restrictions
            """.trimIndent()
            
            showDebugDialog("SMS Access Test Failed", debugMessage)
        }
    }
    
    private fun updateInstructionText() {
        val instruction = """
            SMS Cleaner Israel Setup Required
            
            ⚠️ NO DEFAULT SMS APP SET ⚠️
            
            Your device currently has no default SMS app configured.
            You need to set "SMS Cleaner Israel" as your default SMS app.
            
            WHY THIS IS NEEDED:
            • Android requires a default SMS app to handle messages
            • Without a default app, SMS functionality doesn't work
            • This app needs to be the default to read and manage your messages
            
            STEPS TO SETUP:
            1. Tap "Open SMS Settings" button below
            2. Look for "SMS app" or "Default SMS app" 
            3. You should see "None" or no selection currently
            4. Select "SMS Cleaner Israel" from the available list
            5. Confirm your selection when prompted
            6. Return to this app and tap "Continue"
            
            WHAT TO EXPECT:
            • You'll see a list of SMS-capable apps
            • Select "SMS Cleaner Israel" from the list
            • Android may ask for confirmation
            • The app will then be your default SMS handler
            
            BENEFITS:
            • Automatic SMS filtering and cleaning
            • Smart message categorization  
            • Bulk deletion of unwanted messages
            • Statistics and message management
            
            Tap "Open SMS Settings" to start setup.
        """.trimIndent()
        
        instructionText.text = instruction
    }
    
    private fun openSmsSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Navigate to 'SMS app' and select 'SMS Cleaner Israel'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showDebugDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun checkAppSmsCapability() {
        try {
            val packageManager = packageManager
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = android.net.Uri.parse("sms:")
            val activities = packageManager.queryIntentActivities(intent, 0)
            
            // Alternative detection methods
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            val alternativeCheck = checkDefaultSmsAlternative()
            val systemSettingsCheck = checkSystemSettings()
            
            val debugMessage = """
                SMS Capability Check:
                
                App Package: $packageName
                SMS Activities Found: ${activities.size}
                Current App in List: ${activities.any { it.activityInfo.packageName == packageName }}
                
                Found SMS Apps:
                ${activities.joinToString("\n") { "- ${it.activityInfo.packageName}" }}
                
                DEFAULT SMS DETECTION:
                Method 1 (Telephony): $defaultSmsPackage
                Method 2 (Alternative): $alternativeCheck
                Method 3 (Settings): $systemSettingsCheck
                
                Status: ${if (activities.any { it.activityInfo.packageName == packageName }) "✅ App is SMS-capable" else "❌ App not recognized as SMS-capable"}
                
                DEBUG INFO:
                - All methods return null: ${defaultSmsPackage == null && alternativeCheck == null && systemSettingsCheck == null}
                - App appears in SMS list: ${activities.any { it.activityInfo.packageName == packageName }}
                - System recognizes app: ${defaultSmsPackage == packageName}
            """.trimIndent()
            
            showDebugDialog("SMS Capability Check", debugMessage)
        } catch (e: Exception) {
            showDebugDialog("Error", "Error checking SMS capability: ${e.message}")
        }
    }
    
    private fun checkDefaultSmsAlternative(): String? {
        return try {
            // Alternative method using different approach
            val resolver = contentResolver
            val uri = android.net.Uri.parse("content://sms")
            val cursor = resolver.query(uri, arrayOf("_id"), null, null, null)
            cursor?.use {
                // This is just to test if we can access SMS content
                "SMS content accessible"
            }
            null
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    private fun checkSystemSettings(): String? {
        return try {
            // Check if we can access system settings
            val settings = android.provider.Settings.Secure.getString(
                contentResolver,
                "sms_default_application"
            )
            settings ?: "No default SMS app in settings"
        } catch (e: Exception) {
            "Error accessing settings: ${e.message}"
        }
    }
    
    private fun isDefaultSmsApp(): Boolean {
        try {
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            val currentPackage = packageName
            
            // Enhanced debug information
            val debugMessage = """
                Debug Info:
                Default SMS = $defaultSmsPackage
                Current App = $currentPackage
                Package Match = ${defaultSmsPackage == currentPackage}
                Is Null = ${defaultSmsPackage == null}
                
                Alternative Check:
                Can Access SMS = ${checkSmsAccess()}
                System Settings = ${checkSystemSettings()}
            """.trimIndent()
            
            showDebugDialog("Default SMS Detection", debugMessage)
            
            // If Telephony method returns null, try alternative methods
            if (defaultSmsPackage == null) {
                // Try alternative detection methods
                val canAccessSms = checkSmsAccess()
                val systemSettings = checkSystemSettings()
                
                if (canAccessSms) {
                    showDebugDialog("Alternative Detection", "App can access SMS content - likely default SMS app")
                    return true
                } else {
                    showDebugDialog("No Default SMS App", "No default SMS app detected. Please set one in Android Settings.")
                    return false
                }
            }
            
            return defaultSmsPackage == currentPackage
        } catch (e: Exception) {
            showDebugDialog("Error", "Error checking default SMS app: ${e.message}")
            return false
        }
    }
    
    private fun isDefaultSmsAppAlternative(): Boolean {
        try {
            // Alternative method using Settings
            val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
            return defaultSmsPackage != null && defaultSmsPackage == packageName
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun updateInstructionTextWithRetry() {
        val instruction = """
            SMS Cleaner Israel Setup Required
            
            ⚠️ NO DEFAULT SMS APP DETECTED ⚠️
            
            The system shows no default SMS app is currently set.
            This means you need to set a default SMS app first.
            
            IMPORTANT:
            • Android requires a default SMS app to be set
            • Without a default SMS app, no SMS functionality works
            • You must select "SMS Cleaner Israel" as the default
            
            STEPS TO FIX:
            1. Tap "Open SMS Settings" below
            2. Look for "SMS app" or "Default SMS app"
            3. If no app is selected, you'll see "None" or empty
            4. Select "SMS Cleaner Israel" from the list
            5. Confirm your selection
            6. Return to this screen and tap "Continue"
            
            TROUBLESHOOTING:
            • If you don't see "SMS Cleaner Israel" in the list:
              - Make sure the app is installed properly
              - Try restarting your device
              - Check if the app has SMS permissions
            
            • If the setting doesn't stick:
              - Try restarting your device
              - Clear app data and reinstall
              - Check for conflicting SMS apps
        """.trimIndent()
        
        instructionText.text = instruction
    }
    
    override fun onResume() {
        super.onResume()
        // Add delay to ensure system has updated the default SMS app
        instructionText.postDelayed({
            if (isDefaultSmsApp()) {
                showDebugDialog("Perfect!", "SMS Cleaner Israel is now your default SMS app.")
                // Start MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1000) // 1 second delay to allow system to update
    }
}
