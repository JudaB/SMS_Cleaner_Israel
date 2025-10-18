package com.example.sms_cleaner_israel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                handleIncomingSms(context, intent)
            }
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                handleIncomingSms(context, intent)
            }
        }
    }
    
    private fun handleIncomingSms(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in messages) {
            val sender = message.originatingAddress ?: "Unknown"
            val body = message.messageBody ?: ""
            
            // Check if this message matches our filter criteria
            val targetKeywords = listOf(
                "הלוואה",
                "יד שרה", 
                "נמצאת זכאי לקרן הלוואה",
                "שקיות רפואי"
            )
            
            val shouldFilter = targetKeywords.any { keyword ->
                body.lowercase().contains(keyword.lowercase())
            }
            
            if (shouldFilter) {
                // This is a filtered message
                android.util.Log.d("SMS_Cleaner", "Filtered SMS from $sender: $body")
                
                // You could add auto-deletion logic here if desired
                // For now, just log it
            }
        }
    }
}
