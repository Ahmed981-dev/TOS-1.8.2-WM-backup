package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.ZaloPostRooted

@Dao
interface ZaloPostRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(zalo_post: List<ZaloPostRooted>)

    @Query("Select * from zalo_post_rooted where status =:status")
    fun selectAllZaloPostMessages(status: Int): List<ZaloPostRooted>

    @Query("Update zalo_post_rooted set status=:updated_status where status=:status")
    fun update(status: Int, updated_status: Int): Int

    @Query("Select post_url from zalo_post_rooted where post_id=:id")
    fun checkIfAlreadyExists(id: String): String

    @Query("select max(timeStamp) from zalo_post_rooted")
    fun selectMaxTimeStamp(): Long?
}