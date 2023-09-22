package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_apps")
data class InstalledApp(

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") @Transient
    var id: Int,

    @ColumnInfo(name = "package_name")
    var packageName: String,

    @ColumnInfo(name = "app_name")
    var name: String,

    @ColumnInfo(name = "app_version")
    var version: String,

    @ColumnInfo(name = "install_time")
    var installTime: String,

    @ColumnInfo(name = "is_deleted")
    var isDeleted: Int,

    @ColumnInfo(name = "status")
    var status: Int
) {
    constructor() : this(0, "", "", "", "", 0, 0)
}