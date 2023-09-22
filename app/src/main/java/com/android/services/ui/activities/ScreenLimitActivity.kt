package com.android.services.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.dialogs.AppStoppedDialog

class ScreenLimitActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE
        AccessibilityUtils.screenLimitPackage = packageName
        val appStoppedDialog = AppStoppedDialog(this@ScreenLimitActivity) {
            if (!isFinishing) {
                AccessibilityUtils.screenLimitPackage = ""
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent("com.android.services.accessibility.ACTION_BACK")
                        .putExtra("ACTION_TYPE", 0))
                finish()
            }
        }
        appStoppedDialog.setCancelable(false)
        appStoppedDialog.setCanceledOnTouchOutside(false)
        appStoppedDialog.show()
    }

    override fun onStop() {
        super.onStop()
        AccessibilityUtils.screenLimitPackage = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        AccessibilityUtils.screenLimitPackage = ""
    }
}
