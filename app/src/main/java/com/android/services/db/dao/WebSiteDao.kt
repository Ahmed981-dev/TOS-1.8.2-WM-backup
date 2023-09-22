package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.WebSite

@Dao
interface WebSiteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(webSite: WebSite)

    @Update
    fun update(webSite: WebSite): Int

    @Query("Select url from website_table where url =:url")
    fun checkIfAlreadyExist(url: String): String?

    @Query("Select * from website_table where isBlocked=:isBlocked")
    fun selectAllWebSites(isBlocked: String): List<WebSite>

    @Query("Update website_table set isBlocked=:isBlocked where url=:url")
    fun update(url: String, isBlocked: String)
}