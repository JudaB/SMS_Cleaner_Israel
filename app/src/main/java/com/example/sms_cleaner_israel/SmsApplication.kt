package com.example.sms_cleaner_israel

import android.app.Application
import android.util.Log

class SmsApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("SmsApplication", "SMS Cleaner Israel Application started")
    }
}
