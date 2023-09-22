package com.android.services.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.android.services.MyApplication.Companion.appContext
import com.android.services.db.entities.GeoFence
import com.android.services.receiver.GeoFenceBroadCastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.util.*

class GeoFenceApiClient private constructor() {

    private var mGeoFencingClient: GeofencingClient? = null
    private var mGeoFencePendingIntent: PendingIntent? = null

    private fun initializeGeoFenceClient(context: Context) {
        if (mGeoFencingClient == null)
            mGeoFencingClient = LocationServices.getGeofencingClient(context)
    }

    fun setGeoFencesList(context: Context, geoFenceList: List<GeoFence>) {
        synchronized(GeoFenceApiClient::class.java) {
            mGeoFences.clear()
            mGeoFences.addAll(geoFenceList)
            startGeoFenceMonitoring(context)
        }
    }

    fun removeGeoFences() {
        if (mGeoFencingClient != null && mGeoFencePendingIntent != null) {
            mGeoFencingClient?.removeGeofences(mGeoFencePendingIntent!!)?.run {
                addOnSuccessListener {
                    mGeoFencingClient = null
                    mGeoFencePendingIntent = null
                    logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} geoFences are removed")
                }
                addOnFailureListener {
                    logVerbose("${AppConstants.GEO_FENCES_EVENTS_TYPE} Failed to remove geoFences")
                }
            }
        }
    }

    private fun startGeoFenceMonitoring(context: Context) {
        initializeGeoFenceClient(context)
        try {
            val geoFencesList = createGeoFences()
            addGeoFence(context, geoFencesList)
        } catch (e: Exception) {
            logVerbose(TAG + " Error Start Monitoring Geo Fences: " + e.message)
        }
    }

    private fun createGeoFences(): List<Geofence> {
        val mGeoFencesList: MutableList<Geofence> = ArrayList()
        if (mGeoFences.size > 0) {
            for (geoFence in mGeoFences) {
                try {
                    val geoFenceId = geoFence.id
                    val latitude = geoFence.latitude
                    val longitude = geoFence.longitude
                    val radius = geoFence.radius
                    mGeoFencesList.add(Geofence.Builder()
                        .setRequestId(geoFenceId)
                        .setCircularRegion(latitude, longitude, radius.toString().toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build())
                } catch (e: Exception) {
                    logVerbose(TAG + " Error Creating GeoFences " + e.message)
                }
            }
        }
        return mGeoFencesList
    }

    private fun getGeoFenceRequest(geoFencesList: List<Geofence>): GeofencingRequest {
        logVerbose("$TAG Creating GeoFence Request")
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geoFencesList)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun addGeoFence(context: Context, mGeoFences: List<Geofence>) {
        mGeoFencingClient!!.addGeofences(getGeoFenceRequest(mGeoFences),
            getGeoFencingPendingIntent(context)!!)
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(p0: Void?) {
                    logVerbose("$TAG Geo Fences Added Successfully")
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    logVerbose(TAG + " Failed to add Geo Fences: " + e.message)
                }
            })
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getGeoFencingPendingIntent(context: Context): PendingIntent? {
        if (mGeoFencePendingIntent != null) {
            return mGeoFencePendingIntent
        }
        val intent = Intent(context, GeoFenceBroadCastReceiver::class.java)
        mGeoFencePendingIntent =
            PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeoFencePendingIntent
    }

    companion object {

        private const val TAG = "GeoFencing"

        @Volatile
        private var mInstance: GeoFenceApiClient? = null
        private val mGeoFences = Collections.synchronizedList(ArrayList<GeoFence>())
        val instance: GeoFenceApiClient?
            get() {
                if (mInstance == null) {
                    synchronized(GeoFenceApiClient::class.java) {
                        mInstance = GeoFenceApiClient()
                    }
                }
                return mInstance
            }
    }

    init {
        if (mInstance != null) {
            throw RuntimeException("Use getInstance() method to get the single instance of this class.")
        }
    }
}