package com.android.services.models

data class ScreenTimeModel(
    var uniqueId: String,
    var appName: String,
    var packageName: String,
    var timeOnApp: String,
    var dateTime: String,
    var endTime: String
) {
    constructor() : this("", "", "", "", "", "")
}