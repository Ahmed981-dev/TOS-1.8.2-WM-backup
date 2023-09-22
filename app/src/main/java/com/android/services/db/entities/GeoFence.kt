package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_fence_table")
class GeoFence(
    @field:ColumnInfo(name = "geo_fence_id") @field:PrimaryKey var id: String,
    var name: String,
    var latitude: Double,
    var longitude: Double,
    var radius: Double,
    var enable: Boolean
)