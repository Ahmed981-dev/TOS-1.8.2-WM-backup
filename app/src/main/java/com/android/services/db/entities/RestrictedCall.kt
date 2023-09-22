package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restricted_call_table")
class RestrictedCall(
    @field:ColumnInfo(name = "number") @field:PrimaryKey var number: String,
    @field:ColumnInfo(
        name = "isRestricted") var isRestricted: String
)