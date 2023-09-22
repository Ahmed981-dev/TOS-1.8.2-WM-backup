package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_alert")
data class TextAlert(
    @PrimaryKey @ColumnInfo(name = "alert_id") @Transient var alertId: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "caller_id") var callerId: String,
    @ColumnInfo(name = "keyword") var keyword: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "event_through") var eventThrough: String,
    @ColumnInfo(name = "email") var email: String
) {
    constructor() : this("", "", "", "", "", "", "")
}
