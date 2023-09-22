package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.ScreenLimit

@Dao
interface ScreenLimitDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(screenLimit: ScreenLimit)

    @Update
    fun update(screenLimit: ScreenLimit): Int

    @Query("delete from screen_limit_table where screen_day=:screen_day")
    fun delete(screen_day: String)

    @Query("Update screen_limit_table set usage_time = :usage_time where screen_day=:screen_day")
    fun removeScreenUsageLimit(screen_day: String, usage_time: String): Int

    @Query("Update screen_limit_table set start_time =:start_time,end_time=:end_time where screen_day=:screen_day")
    fun removeScreenRangeLimit(screen_day: String, start_time: String, end_time: String): Int

    @Query("Select * from screen_limit_table")
    fun selectAllScreenLimit(): List<ScreenLimit>

    @Query("Select * from screen_limit_table where screen_day=:screen_day")
    fun checkIfAlreadyExists(screen_day: String): ScreenLimit?
}