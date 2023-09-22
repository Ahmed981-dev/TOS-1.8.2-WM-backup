package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "connected_network")
data class ConnectedNetwork(
    @PrimaryKey @ColumnInfo(name = "unique_id")
    var uniqueId: String,
    var networkId: String,
    var networkName: String,
    var networkType: String,
    var ipAddress: String,
    @ColumnInfo(name = "date")
    @Transient
    var date: Date,
    @ColumnInfo(name = "status")
    @Transient
    var status: Int
) {
    constructor() : this("", "", "", "", "", Date(), 0)
}