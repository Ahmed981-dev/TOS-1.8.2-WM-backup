package com.android.services.interfaces

import android.content.Intent
import androidx.work.Data

interface IWorkerProcessing {
    fun onCreate()
    fun onStartCommand(data: Data)
    fun onDestroy()
}