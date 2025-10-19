package com.example.sms_cleaner_israel

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SmsService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SmsService", "SMS Service started")
        return START_STICKY
    }
}