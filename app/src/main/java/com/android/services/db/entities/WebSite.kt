package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "website_table")
data class WebSite(
    @field:ColumnInfo(name = "url")
    @field:PrimaryKey var url: String = "",
    var category: String = "",
    var isWebSite: Boolean = false,
    var isBlocked: String,
)