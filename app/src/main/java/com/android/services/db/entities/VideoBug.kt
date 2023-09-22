package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_bug")
data class VideoBug(
    @PrimaryKey @ColumnInfo(name = "file_path")
    var file: String,
    @ColumnInfo(name = "name")
    var name: String?,

    @ColumnInfo(name = "camera_type")
    var cameraType: String?,

    @ColumnInfo(name = "start_datetime")
    var startDatetime: String?,

    @ColumnInfo(name = "push_id")
    var pushId: String?,

    @ColumnInfo(name = "push_status")
    var pushStatus: String?,

    @ColumnInfo(name = "status")
    var status: Int
) {
    constructor() : this("", "", "", "", "", "", 0)
}