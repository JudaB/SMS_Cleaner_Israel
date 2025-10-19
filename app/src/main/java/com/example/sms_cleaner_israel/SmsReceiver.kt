package com.example.sms_cleaner_israel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
                // Handle incoming SMS
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (message in messages) {
                    Log.d("SmsReceiver", "Received SMS: ${message.messageBody}")
                    // Here you can process the incoming SMS
                    // For now, we just log it
                }
            }
        }
    }
}