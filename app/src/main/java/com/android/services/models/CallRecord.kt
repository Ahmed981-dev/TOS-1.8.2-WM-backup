package com.android.services.models

import android.os.Parcelable
import com.android.services.enums.CallRecordType

@kotlinx.parcelize.Parcelize
class CallRecord(
    var callRecordType: CallRecordType,
    val phoneNumber: String,
    val callType: String
) : Parcelable {

    override fun toString(): String {
        return "$callRecordType, $phoneNumber, $callType"
    }
}