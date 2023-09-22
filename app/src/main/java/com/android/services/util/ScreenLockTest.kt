package com.android.services.util

import android.annotation.TargetApi
import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings

object ScreenLockTest {
    /**
     *
     * Checks to see if the lock screen is set up with either a PIN / PASS / PATTERN
     *
     *
     *
     * For Api 16+
     *
     * @return true if PIN, PASS or PATTERN set, false otherwise.
     */
    fun doesDeviceHaveSecuritySetup(context: Context): Boolean {
        return isPatternSet(context) || isPassOrPinSet(context)
    }

    /**
     * @param context context of the Application
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    private fun isPatternSet(context: Context): Boolean {
        val cr = context.contentResolver
        return try {
            val lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED)
            lockPatternEnable == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * @param context context of the Application
     * @return true if pass or pin set
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun isPassOrPinSet(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager? //api 16+
        return keyguardManager?.isKeyguardSecure ?: false
    }
}