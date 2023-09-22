package com.android.services.repository

import android.content.Context
import android.os.AsyncTask
import com.android.services.db.dao.GeoFenceEventDao
import com.android.services.db.entities.GeoFenceEvent
import com.orhanobut.logger.Logger
import javax.inject.Inject

class GeoFenceEventRepository @Inject constructor(private val geoFenceEventDao: GeoFenceEventDao) {

    fun insertGeoFenceEvent(geoFenceEvent: GeoFenceEvent) {
        geoFenceEventDao.insert(geoFenceEvent)
    }

    fun selectGeoFenceEvents(): List<GeoFenceEvent> {
        return geoFenceEventDao.selectAllGeoFenceEvent(0)
    }

    fun updateGeoFenceEvent(startId: Int, endId: Int) {
        geoFenceEventDao.updateGeoFenceEvent(1, startId, endId)
    }
}