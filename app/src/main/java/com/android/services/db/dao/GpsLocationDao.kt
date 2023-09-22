package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.GpsLocation
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface GpsLocationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(gpsLocation: GpsLocation)

    @Query("Update gps_location set location_status =:updated_location_status where date BETWEEN :startDate AND :endDate")
    fun updateGpsLocation(updated_location_status: Int, startDate: Date, endDate: Date): Int

    @Query("Select * from gps_location where location_status = :status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllGpsLocation(status: Int): List<GpsLocation>
}