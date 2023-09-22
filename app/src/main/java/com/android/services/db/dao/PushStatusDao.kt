package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.PushStatus
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface PushStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(PushStatus: PushStatus)

    @Update
    fun update(PushStatus: PushStatus)

    @Query("Update push_status set status=:status, push_status=:push_status where push_id=:push_id")
    fun update(push_id: String, status: String, push_status: Int): Int

    @Query("Update push_status set push_status =:push_status  where date BETWEEN :startDate AND :endDate")
    fun update(startDate: Date, endDate: Date, push_status: Int): Int

    @Query("Select * from push_status where push_status = :push_status order by date DESC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllPushStatuses(push_status: Int): List<PushStatus>

    @Query("Select push_id from push_status where push_id = :push_id")
    fun checkIfAlreadyExist(push_id: String): String?
}