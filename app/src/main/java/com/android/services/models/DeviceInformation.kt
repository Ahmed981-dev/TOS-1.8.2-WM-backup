package com.android.services.models

data class DeviceInformation(
    var batteryLevel: String,
    var ipAddress: String,
    var simNumber: String,
    var isWifiOn: Boolean,
    var isDeviceRooted: Boolean,
    var isGpsOn: Boolean,
    var deviceStorage: String,
    var photosStorage: String,
    var videosStorage: String,
    var audioStorage: String,
    var otherStorage: String,
    var imei: String,
    var appVersion: String,
    var carrierName: String,
    var deviceName: String,
    var apkStorage: String,
    var documentStorage: String,
    var archivesStorage: String,
    var syncTime: String,
    var userId: String,
    var phoneServiceId: String
) {
    constructor() : this(
        "", "", "", false, false, false, "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", ""
    )
}
    