package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.TextAlert

@Dao
interface TextAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(textAlert: TextAlert)

    @Update
    fun update(textAlert: TextAlert)

    @Query("Select * from text_alert")
    fun selectAllTextAlerts(): List<TextAlert>

    @Query("delete from text_alert where alert_id=:alertId")
    fun delete(alertId: String)
}