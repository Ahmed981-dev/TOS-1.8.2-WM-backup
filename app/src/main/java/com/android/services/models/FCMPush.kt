package com.android.services.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FCMPush(
    val pushCommand: String,
    val pushMethod: String,
    val pushId: String,
    val phoneServiceId: String
) : Parcelable

