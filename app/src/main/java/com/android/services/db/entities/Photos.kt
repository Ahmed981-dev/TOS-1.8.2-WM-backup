package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "photos_table")
data class Photos(
    @PrimaryKey @ColumnInfo(name = "photo_id")
    var photoId: String,
    @ColumnInfo(name = "file")
    var file: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "date_taken")
    var dateTaken: String,
    @ColumnInfo(name = "date")
    @Transient
    var date: Date,
    @ColumnInfo(name = "size")
    var size: Long,
    @ColumnInfo(name = "status")
    var status: Int
) {
    constructor() : this("", "", "", "", "", Date(), 0, 0)
}