package com.android.services.db.dao

import androidx.room.*
import com.android.services.db.entities.RestrictedCall

@Dao
interface RestrictedCallDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(blockedApp: RestrictedCall?)

    @Update
    fun update(blockedApp: RestrictedCall?)

    @Query("Select number from restricted_call_table where isRestricted=:isRestricted")
    fun selectAllRestrictedCalls(isRestricted: String?): List<String>

    @Query("delete from restricted_call_table where number=:number")
    fun delete(number: String?)

    @Query("Select number from restricted_call_table where number=:number")
    fun checkAlreadyExists(number: String?): String?
}