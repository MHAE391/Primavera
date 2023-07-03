package com.m391.primavera

import android.app.Application
import android.content.Intent

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val intent = Intent(this, SensorsListenerService::class.java)
        startService(intent)
    }
}