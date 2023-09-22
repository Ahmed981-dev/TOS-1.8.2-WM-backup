package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "sms_log")
data class SmsLog(
    @PrimaryKey @ColumnInfo(name = "sms_id") @Transient var smsId: String,
    @ColumnInfo(name = "sms_body") var smsBody: String,
    @ColumnInfo(name = "sms_number") var address: String,
    @ColumnInfo(name = "sms_type") var smsType: String,
    @ColumnInfo(name = "sms_time") var smsTime: String,
    @ColumnInfo(name = "sms_sender") var smsSender: String,
    @ColumnInfo(name = "sms_recipient") var smsRecipient: String,
    @ColumnInfo(name = "sms_status") @Transient var smsStatus: String,
    @ColumnInfo(name = "location_longitude") var locationLongitude: String,
    @ColumnInfo(name = "location_latitude") var locationLattitude: String,
    @ColumnInfo(name = "userId") var userId: String,
    @ColumnInfo(name = "phoneServiceId") var phoneServiceId: String,
    @ColumnInfo(name = "date") @Transient var date: Date,
    @ColumnInfo(name = "sent_status") @Transient var status: Int
) {
    constructor() : this(
        "", "", "", "", "", "",
        "", "", "", "", "", "", Date(), 0
    )
}