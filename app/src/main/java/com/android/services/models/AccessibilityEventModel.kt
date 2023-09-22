package com.android.services.models

import android.os.Parcelable
import android.view.accessibility.AccessibilityNodeInfo

data class AccessibilityEventModel(
    var packageName: String,
    var eventClassName: String,
    var eventText: String,
    var eventSource: AccessibilityNodeInfo? = null,
    var rootInWindow: AccessibilityNodeInfo? = null,
    var parcelable: Parcelable? = null
) {
    constructor() : this("", "", "", null, null, null)
}