package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "browser_history")
data class BrowserHistory(

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") @Transient
    var id: Int,

    @ColumnInfo(name = "unique_id")
    var uniqueId: String,

    @ColumnInfo(name = "urlTitle")
    var urlTitle: String,

    @ColumnInfo(name = "urlAddress")
    var urlAddress: String,

    @ColumnInfo(name = "urlDate")
    var urlDate: String,

    @ColumnInfo(name = "urlVisits")
    var urlVisits: String,

    @ColumnInfo(name = "isBookmarked")
    var isBookmarked: String,

    @ColumnInfo(name = "status")
    @Transient
    var status: Int
) {
    constructor() : this(0, "", "", "", "", "", "", 0)
}
