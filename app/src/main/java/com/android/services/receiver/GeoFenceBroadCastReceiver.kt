package com.android.services.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.android.services.db.entities.GeoFenceEvent
import com.android.services.util.*
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.Executors

class GeoFenceBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        logVerbose("$${AppConstants.GEO_FENCES_EVENTS_TYPE} OnReceive Event")
        val watchDogAlarmReceiver=WatchDogAlarmReceiver()
        watchDogAlarmReceiver.setAlarm(context)
        FirebasePushUtils.restartRemoteDataSyncService(context)
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent.hasError()) {
                val errorMsg = getErrorString(geofencingEvent.errorCode)
                logVerbose("GeoFence Event Error = $errorMsg")
                return
            }
            val geoFenceTransition = geofencingEvent.geofenceTransition
            if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
            ) {
                val executeService = Executors.newSingleThreadExecutor()
                executeService.execute {
                    val triggeringGeoFences = geofencingEvent.triggeringGeofences
                    val geoFenceTransitionDetails = getGeoFenceTransitionDetails(context, geoFenceTransition, triggeringGeoFences)
                    logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} Event details = $geoFenceTransitionDetails")
                    if (geoFenceTransitionDetails.isNotEmpty() && AppUtils.isNetworkAvailable(context)) {
                        EventBus.getDefault().post("uploadGeoFence")
                    }
                }
            }
        } catch (e: Exception) {
            logVerbose(AppConstants.GEO_FENCES_EVENTS_TYPE + " OnReceive Event Exception:- " + e.message)
        }
    }

    private fun getGeoFenceTransitionDetails(
        context: Context,
        geoFenceTransition: Int,
        triggeringGeoFences: List<Geofence>,
    ): String {
        val triggeringGeoFencesList = ArrayList<String?>()
        var status = ""
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            status = "Entering"
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "Exiting"
        }

        for (geofence in triggeringGeoFences) {
            val fenceId = geofence.requestId
            triggeringGeoFencesList.add(fenceId)
            try {
                val geoFenceEvent = GeoFenceEvent()
                geoFenceEvent.geoFenceId = fenceId
                geoFenceEvent.event = status
                geoFenceEvent.eventDatetime =
                    AppUtils.formatDate(System.currentTimeMillis().toString())
                geoFenceEvent.status = 0
                InjectorUtils.provideGeoFenceEventRepository(context)
                    .insertGeoFenceEvent(geoFenceEvent)
            } catch (e: Exception) {
                logVerbose(AppConstants.GEO_FENCES_EVENTS_TYPE + " Error Inserting GeoFence Event:- " + e.message)
            }
        }
        return status + " " + TextUtils.join(", ", triggeringGeoFencesList)
    }

    companion object {

        private fun getErrorString(errorCode: Int): String {
            return when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "GeoFence not available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too many GeoFences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
                else -> "Unknown error."
            }
        }
    }
}