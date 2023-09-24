package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "mic_bug_table")
data class MicBug(
    @PrimaryKey @ColumnInfo(name = "file") var file: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "duration") var duration: String,
    @ColumnInfo(name = "startDatetime") var startDatetime: String,
    @ColumnInfo(name = "pushId") var pushId: String,
    @ColumnInfo(name = "pushStatus") var pushStatus: String,
    @field:Transient @field:ColumnInfo(name = "totalDuration") var totalDuration: Int,
    @field:Transient @field:ColumnInfo(name = "date") var date: Date,
    @ColumnInfo(name = "is_compressed") var isCompressed: Int,
    @ColumnInfo(name = "mic_bug_status") var status: Int
) {
    constructor() : this("", "", "", "", "", "",0, Date(), 0, 0)
}