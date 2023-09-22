package com.android.services.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.services.ui.activities.MainLaunchActivity
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose
import com.android.services.util.startActivityWithData

/**
 * This is the component that is responsible for actual device administration.
 * It becomes the receiver when a policy is applied. It is important that we
 * subclass DeviceAdminReceiver class here and to implement its only required
 * method onEnabled().
 */
class TOSDeviceAdminReceiver : DeviceAdminReceiver() {
    /**
     * Called when this application is approved to be a device administrator.
     */
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        logVerbose(TAG, "onEnabled")
//        if (!AppConstants.isAppIconCreated) {
//            AppUtils.createAppShortCut(context)
//            AppConstants.isAppIconCreated = true
//            Toast.makeText(context,"App Shortcut Created",Toast.LENGTH_LONG).show()
//            context.startActivityWithData<MainLaunchActivity>(
//                listOf(Intent.FLAG_ACTIVITY_NEW_TASK)
//            )
//        }
    }

    /**
     * Called when this application is no longer the device administrator.
     */
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        logVerbose(TAG, "onDisabled")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        logVerbose(TAG, "onLockTaskEntering")
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        super.onPasswordChanged(context, intent)
        logVerbose(TAG, "onPasswordChanged")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        logVerbose(TAG, "onPasswordFailed")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        logVerbose(TAG, "onPasswordSucceeded")
    }

    companion object {
        const val TAG = "DemoDeviceAdminReceiver"
    }
}