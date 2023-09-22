package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.PhoneServices

@Dao
interface PhoneServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneService(phoneServices:List<PhoneServices>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoneService(phoneService:PhoneServices)

    @Query("Select * from phone_services")
    fun selectAllPhoneServices():List<PhoneServices>
}