package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "screen_shot")
data class ScreenShot(
    @PrimaryKey
    @ColumnInfo(name = "file_path") var file: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "date_taken") var dateTaken: String,
    @ColumnInfo(name = "push_id") var pushId: String,
    @ColumnInfo(name = "push_status") var pushStatus: String,
    @field:Transient @field:ColumnInfo(name = "date") var date: Date,
    @field:Transient @ColumnInfo(name = "status") var status: Int
) {
    constructor() : this("", "", "", "", "", Date(), 0)
}