package com.example.sms_cleaner_israel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.provider.Telephony.WAP_PUSH_DELIVER" -> {
                // Handle incoming MMS
                Log.d("MmsReceiver", "Received MMS")
                // Here you can process the incoming MMS
                // For now, we just log it
            }
        }
    }
}