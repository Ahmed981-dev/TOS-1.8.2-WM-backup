package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.TumblrPostRooted

@Dao
interface TumblrPostRootedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(tumblr_post: List<TumblrPostRooted>)

    @Query("Select * from tumblr_post_rooted where status =:status")
    fun selectAllTumblrPostMessages(status: Int): List<TumblrPostRooted>

    @Query("Update tumblr_post_rooted set status=:updated_status where status=:status")
    fun update(status: Int, updated_status: Int): Int

    @Query("select max(timeStamp) from tumblr_post_rooted")
    fun selectMaxTimeStamp(): Long?

    @Query("Select postUrl from tumblr_post_rooted where post_id=:id")
    fun checkIfAlreadyExists(id: String): String?
}