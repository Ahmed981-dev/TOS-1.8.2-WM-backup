package com.android.services.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.android.services.R

class DummyBackgroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val notification = NotificationCompat.Builder(this)
            .setContentText("Running in background...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(100, notification)
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}