package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.AppLimit

@Dao
interface AppLimitDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(appLimit: AppLimit?)

    @Update
    fun update(appLimit: AppLimit?): Int

    @Query("delete from app_limit_table where package_name=:package_name")
    fun delete(package_name: String?)

    @Query("Select * from app_limit_table")
    fun selectAllAppLimit(): List<AppLimit>

    @Query("Select * from app_limit_table where package_name=:package_name")
    fun checkIfAlreadyExists(package_name: String?): AppLimit?
}