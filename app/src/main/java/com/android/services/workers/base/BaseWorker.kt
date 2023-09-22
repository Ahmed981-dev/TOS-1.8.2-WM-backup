package com.android.services.workers.base

import android.app.Service
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex

abstract class BaseWorker(
    private val context: Context,
    parameters: WorkerParameters,
    private val tagName: String
) : CoroutineWorker(context, parameters) {
    private val mutex = Mutex(true)
    override suspend fun doWork(): Result {
        try {
            val input = inputData.getString("InputData")
            Log.d(tagName, input.toString())
            onCreate()
            onStartCommand()
            lockThread()
        } catch (ex: CancellationException) {
            Log.d(tagName, "Cancel Exception with message ${ex.message} detailMessage= ${ex.localizedMessage}")
            unlockThread()
            onStopWorker()
        }
        return Result.success()
    }

    private fun unlockThread() {
        mutex.unlock()
    }

    private suspend fun lockThread() {
        mutex.lock()
    }

    private fun onStopWorker() {
        onStop()
    }

    abstract fun onCreate()
    abstract fun onStartCommand()
    abstract fun onStop()
}