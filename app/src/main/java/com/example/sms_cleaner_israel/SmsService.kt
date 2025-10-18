package com.example.sms_cleaner_israel

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SmsService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle SMS service operations
        return START_STICKY
    }
}
