package com.example.sms_cleaner_israel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Handle incoming MMS messages
        // This is required for default SMS app capability
        android.util.Log.d("SMS_Cleaner", "MMS received")
    }
}
