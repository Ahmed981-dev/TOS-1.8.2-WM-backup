package com.android.services.workers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.services.jobScheduler.ObserverJobScheduler
import com.android.services.network.DataSyncTask
import com.android.services.observer.SmsObserver
import com.android.services.util.AppConstants
import com.android.services.util.logVerbose
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DataUploadingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parameters: WorkerParameters,
    val dataSyncTask: DataSyncTask
) : CoroutineWorker(context, parameters) {
    companion object {
        const val TAG_DATA_UPLOADING_WORKER = "DataUploadingWorker"
    }

    private var mSmsObserver: SmsObserver? = null

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                startObservers()
                logVerbose(
                    "DataUploadingWorkerInfo= ",
                    "Getting into uploadingWorker with instance=$dataSyncTask"
                )
//                val dataUploadingTask =
//                    async(Dispatchers.IO) {
                dataSyncTask.executeDataJobTask()
//                    }
//                dataUploadingTask.await()
                logVerbose("DataUploadingWorkerInfo= ", "Worker task ends")
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    @SuppressLint("NewApi")
    private fun startObservers(): Unit {
        if (AppConstants.osGreaterThanEqualToNougat) {
            ObserverJobScheduler.registerObserverJob(applicationContext)
        } else {
            addSmsContentChangeObserver()
        }
    }

    private fun addSmsContentChangeObserver() {
        if (mSmsObserver == null) {
            mSmsObserver = SmsObserver(applicationContext, Handler())
            applicationContext.contentResolver.registerContentObserver(
                Uri.parse(AppConstants.SMS_URI), true,
                mSmsObserver!!
            )
        }
    }
}