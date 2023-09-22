package com.android.services.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoipCallRecord(
    var voipMessenger: String = "",
    var voipNumber: String = "",
    var voipName: String = "",
    var voipDirection: String = "",
    var voipType: String = "",
    var voipDateTime: String = ""
) : Parcelable