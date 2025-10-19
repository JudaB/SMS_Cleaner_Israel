package com.example.sms_cleaner_israel

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupSettingsContent()
    }
    
    private fun setupSettingsContent() {
        val tvSettingsContent = findViewById<TextView>(R.id.tvSettingsContent)
        
        val settingsText = """
            SMS Cleaner Israel - Settings
            
            READ BUTTONS:
            
            1. Read Spam
               Keywords: הלוואה, יד שרה, נמצאת זכאי לקרן הלוואה, שקיות רפואי, מבצע, סקר, לקוח
            
            2. Read Business  
               Keywords: חשבונית, קבלה
            
            3. הצג את כל ההודעות (Read All)
               Shows: All SMS messages without filtering
            
            AUTO DELETE BUTTONS:
            
            4. הלוואת (Auto Delete Loans)
               Keywords: בעל עסק הודעה חשובה, הלוואה
            
            5. גרס (Auto: Medical)
               Keywords: שקיות רפואי
            
            6. מבצעים (Promotions)
               Keywords: מבצע
            
            STATISTICS:
            
            • Spam Counter: Shows count of spam messages
            • Business Counter: Shows count of business messages  
            • Total Counter: Shows total SMS count
            • Loans Counter: Shows count of loan/business messages
            • Medical Counter: Shows count of medical messages
            • Promotions Counter: Shows count of promotional messages
            
            NAVIGATION:
            
            • Swipe Left: Go to messages screen
            • Swipe Right: Return to main screen
            • Tap buttons to filter or delete messages
            
            PERMISSIONS:
            
            • READ_SMS: Required to read and manage SMS messages
            • Set as default SMS app for full functionality
        """.trimIndent()
        
        tvSettingsContent.text = settingsText
    }
}
