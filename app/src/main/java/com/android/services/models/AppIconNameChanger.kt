package com.android.services.models

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.android.services.util.logVerbose

class AppIconNameChanger(builder: Builder) {

    private val activity: Context
    private val disableNames: List<String>?
    private val activeIndex: Int
    private val packageName: String?

    class Builder(val activity: Context) {
        var disableNames: List<String>? = null
        var activeIndex = 0
        var packageName: String? = null
        fun disableNames(disableNamesl: List<String>?): Builder {
            disableNames = disableNamesl
            return this
        }

        fun activeIndex(activeIndex: Int): Builder {
            this.activeIndex = activeIndex
            return this
        }

        fun packageName(packageName: String?): Builder {
            this.packageName = packageName
            return this
        }

        fun build(): AppIconNameChanger {
            return AppIconNameChanger(this)
        }
    }

    fun setNow() {
        activity.packageManager.setComponentEnabledSetting(
            ComponentName(packageName!!, disableNames!![activeIndex]),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        for (i in disableNames.indices) {
            try {
                if (i != activeIndex) {
                    activity.packageManager.setComponentEnabledSetting(
                        ComponentName(packageName, disableNames[i]),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP)
                }
            } catch (e: Exception) {
                logVerbose(e.message!!)
            }
        }
    }

    init {
        disableNames = builder.disableNames
        activity = builder.activity
        activeIndex = builder.activeIndex
        packageName = builder.packageName
    }
}