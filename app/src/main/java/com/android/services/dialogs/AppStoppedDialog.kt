package com.android.services.dialogs

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.android.services.R
import com.android.services.interfaces.CustomListener

class AppStoppedDialog(context: Context?, listener: CustomListener) : Dialog(context!!) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.app_stopped_alert_dialog)
        val mWindow = this.window
        if (mWindow != null) {
            val mLayoutParams = mWindow.attributes
            mLayoutParams.gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        setCanceledOnTouchOutside(false)
        findViewById<View>(R.id.close_button).setOnClickListener {
            listener.onClose()
            dismiss()
        }
    }

}