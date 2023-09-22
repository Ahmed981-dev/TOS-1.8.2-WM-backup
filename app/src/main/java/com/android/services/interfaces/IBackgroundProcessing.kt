package com.android.services.interfaces

import android.content.Intent

interface IBackgroundProcessing {
    fun onCreate()
    fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    fun onDestroy()
}