package com.android.services.models

data class SmsCallAlert(
    var phoneNumber: String = "",
    var contactName: String = "",
    var type: String = "",
    var body: String = "",
    var date: String = ""
)
