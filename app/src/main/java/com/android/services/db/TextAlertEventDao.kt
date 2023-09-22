package com.android.services.db

import androidx.room.*
import com.android.services.db.entities.TextAlertEvent
import java.util.*

@Dao
interface TextAlertEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(textAlertEvent: TextAlertEvent)

    @Update
    fun update(textAlertEvent: TextAlertEvent)

    @Query("Select * from text_alert_event where status=:status order by date DESC limit 50")
    fun selectAllTextAlertEvents(status: Int): List<TextAlertEvent>

    @Query("delete from text_alert_event where alert_id=:alertId")
    fun delete(alertId: String)

    @Query("Update text_alert_event set status = :updated_status where date BETWEEN :startDate AND :endDate")
    fun updateTextAlertEvents(updated_status: Int, startDate: Date, endDate: Date)
}