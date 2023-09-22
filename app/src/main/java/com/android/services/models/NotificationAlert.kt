package com.android.services.models

data class NotificationAlert(
    var title: String = "",
    var packageName: String = "",
    var appName: String = "",
    var body: String = "",
    var date: String = ""
)
