package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.BlockedApp

@Dao
interface BlockedAppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(blockedApp: BlockedApp)

    @Update
    fun update(blockedApp: BlockedApp)

    @Query("Select package_name from blocked_app_table where is_blocked=:is_blocked")
    fun selectAllBlockedApps(is_blocked: String): List<String>

    @Query("delete from blocked_app_table where package_name=:package_name")
    fun delete(package_name: String)

    @Query("Select package_name from blocked_app_table where package_name=:package_name")
    fun checkAlreadyExists(package_name: String): String?

    @Query("Select package_name from blocked_app_table")
    fun getAllPackageNamesList():List<String>
}