package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.GeoFence

@Dao
interface GeoFenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(geoFence: GeoFence?)

    @Query("Update geo_fence_table set enable = :isEnable where geo_fence_id=:fenceId")
    fun updateGeoFence(fenceId: String?, isEnable: Boolean): Int

    @Query("Select * from geo_fence_table where enable = :isEnable")
    fun selectAllGeoFences(isEnable: Boolean): List<GeoFence>

    @Query("delete from geo_fence_table where geo_fence_id =:id")
    fun delete(id: String?)
}