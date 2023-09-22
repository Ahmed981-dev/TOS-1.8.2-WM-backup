package com.android.services.repository

import android.content.Context
import com.android.services.db.dao.GeoFenceDao
import com.android.services.db.entities.GeoFence
import javax.inject.Inject

class GeoFenceRepository @Inject constructor(private val geoFenceDao: GeoFenceDao) {

    fun insertGeoFence(context: Context, geoFence: GeoFence?) {
        geoFenceDao.insert(geoFence)
    }

    fun delete(id: String?) {
        geoFenceDao.delete(id)
    }

    fun selectGeoFences(isEnable: Boolean): List<GeoFence> {
        return geoFenceDao.selectAllGeoFences(isEnable)
    }
}