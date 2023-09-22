package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts_table")
data class Contacts(
    
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "unique_id") @Transient
    var uniqueId: Int,

    @ColumnInfo(name = "phoneContactId")
    var phoneContactId: String,

    @ColumnInfo(name = "contactFirstName")
    var contactFirstName: String,

    @ColumnInfo(name = "contactLastName")
    var contactLastName: String,

    @ColumnInfo(name = "contactMobileNo")
    var contactMobileNo: String,

    @ColumnInfo(name = "contactHomeNo")
    var contactHomeNo: String,

    @ColumnInfo(name = "contactOfficeNo")
    var contactOfficeNo: String,

    @ColumnInfo(name = "userId")
    var userId: String,

    @ColumnInfo(name = "phoneServiceId")
    var phoneServiceId: String,

    @ColumnInfo(name = "contactStatus") @Transient
    var contactStatus: Int
) {
    constructor() : this(0, "", "", "", "", "", "", "", "", 0)
}