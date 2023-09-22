package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.GeoFenceEvent

@Dao
interface GeoFenceEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(geoFenceEvent: GeoFenceEvent?)

    @Query("Update geo_fence_event_table set status = :updated_geoFenceEvent where id BETWEEN :startId AND :endId")
    fun updateGeoFenceEvent(updated_geoFenceEvent: Int, startId: Int, endId: Int): Int

    @Query("Select * from geo_fence_event_table where status = :status order by id ASC limit 5")
    fun selectAllGeoFenceEvent(status: Int): List<GeoFenceEvent>
}