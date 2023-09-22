package com.android.services.ui.navigator

import com.android.services.enums.PermissionScreens

/**
 * Navigator Interface to Navigate to Screens
 */
interface AppNavigator {
    fun navigateTo(permissionScreens: PermissionScreens)
    fun launchFragment(permissionScreens: PermissionScreens)
}