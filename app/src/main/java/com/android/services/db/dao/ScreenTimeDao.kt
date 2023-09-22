package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ScreenTime
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface ScreenTimeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(screenTime: ScreenTime)

    @Query("Update screen_time set status = :updated_screenTime where id BETWEEN :startId AND :endId")
    fun updateScreenTime(updated_screenTime: Int, startId: Int, endId: Int): Int

    @Query("Select * from screen_time where status = :status order by id ASC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllScreenTime(status: Int): List<ScreenTime>

    @Query("Select SUM(time_milli_seconds) from screen_time where date BETWEEN :startDate AND :endDate")
    fun totalUsageTime(startDate: Date, endDate: Date): Long

    @Query("Select SUM(time_milli_seconds) from screen_time where package_name=:packageName AND date BETWEEN :startDate AND :endDate")
    fun totalAppUsageTime(packageName: String, startDate: Date, endDate: Date): Long
}