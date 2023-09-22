package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "text_alert_event")
data class TextAlertEvent(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") @Transient var id: Int,
    @ColumnInfo(name = "alert_id") @Transient var alertId: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "caller_id") var callerId: String,
    @ColumnInfo(name = "keyword") var keyword: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "event_through") @Transient var eventThrough: String,
    @ColumnInfo(name = "email") var email: String,
    @ColumnInfo(name = "alertContent") var alertContent: String,
    @ColumnInfo(name = "date") @Transient var date: Date,
    @ColumnInfo(name = "status") @Transient var status: Int
) {
    constructor() : this(0, "", "", "", "", "", "", "", "", Date(), 0)
}
