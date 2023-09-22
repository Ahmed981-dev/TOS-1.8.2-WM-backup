package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.InstalledApp
import com.android.services.models.UninstalledApp

@Dao
interface InstalledAppsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(installedAppList: List<InstalledApp>)

    @Query("Update installed_apps set status = :updated_status where id BETWEEN :startId AND :endId")
    fun update(startId: Int, endId: Int, updated_status: Int): Int

    @Query("Update installed_apps set is_deleted = :is_deleted where package_name=:package_name")
    fun update(package_name: String, is_deleted: Int): Int

    @Query("delete from installed_apps where package_name=:package_name")
    fun delete(package_name: String)

    @Query("Select package_name, app_name from installed_apps where is_deleted=:is_deleted")
    fun selectUninstalledApps(is_deleted: Int): List<UninstalledApp>

    @Query("Select * from installed_apps where status = :status")
    fun selectAllInstalledApps(status: Int): List<InstalledApp>

    @Query("Select package_name from installed_apps where package_name = :packageName")
    fun checkIfAlreadyExist(packageName: String): String?
}