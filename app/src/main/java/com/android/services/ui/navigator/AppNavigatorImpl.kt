package com.android.services.ui.navigator

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.services.R
import com.android.services.enums.PermissionScreens
import com.android.services.ui.fragments.*
import javax.inject.Inject

/**
 * Navigator implementation.
 */
class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    override fun navigateTo(permissionScreens: PermissionScreens) {
//        activity.startActivityWithData<AppPermissionsActivity>(
//            listOf(
//                Intent.FLAG_ACTIVITY_NEW_TASK,
//                Intent.FLAG_ACTIVITY_CLEAR_TASK,
//                Intent.FLAG_ACTIVITY_CLEAR_TOP
//            ),
//            AppPermissionsActivity.KEY_SCREEN_PERMISSION to permissionScreens.toString()
//        )
    }

    override fun launchFragment(permissionScreens: PermissionScreens) {
        val fragment: Fragment? = when (permissionScreens) {
            PermissionScreens.LOCATION_PERMISSION -> LocationFragment.newInstance()
            PermissionScreens.MANAGEMENT_OF_ALL_FILES -> ManagementOfFilesFragment.newInstance()
            PermissionScreens.DEVICE_ADMIN_PERMISSION -> DeviceAdministratorFragment.newInstance()
            PermissionScreens.SCREEN_RECORD_PERMISSION -> ScreenRecordFragment.newInstance()
            PermissionScreens.DRAW_OVER_OTHER_APPS -> DisplayOverAppsFragment.newInstance()
            PermissionScreens.NOTIFICATION_ACCESS_PERMISSION -> NotificationAccessFragment.newInstance()
            PermissionScreens.ACCESSIBILITY_PERMISSION -> AccessibilityFragment.newInstance()
            PermissionScreens.DISABLE_NOTIFICATION_ACCESS -> DisableNotificationFragment.newInstance()
            PermissionScreens.USAGE_ACCESS_PERMISSION -> UsageAccessFragment.newInstance()
            PermissionScreens.HIDE_APP -> {
                HideAppFragment.newInstance()
            }
        }

        fragment?.let {
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(fragment::class.java.canonicalName)
                .commitAllowingStateLoss()
        }
    }
}