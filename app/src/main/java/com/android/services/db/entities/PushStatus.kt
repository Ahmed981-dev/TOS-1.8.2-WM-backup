package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "push_status")
data class PushStatus(
    @PrimaryKey @ColumnInfo(name = "push_id") var uniqueId: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "phone_service_id") var phoneServiceId: String,
    @field:Transient @field:ColumnInfo(name = "date") var date: Date,
    @ColumnInfo(name = "push_status") var pushStatus: Int
) {
    constructor() : this("", "", "", Date(), 0)
}