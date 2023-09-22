package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "gps_location")
data class GpsLocation(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uniqueId") @Transient
    var uniqueId: Int,
    @ColumnInfo(name = "geoLocationName")
    var geoLocationName: String,
    @ColumnInfo(name = "geoLocationLongitude")
    var geoLocationLongitude: String,
    @ColumnInfo(name = "geoLocationLattitude")
    var geoLocationLattitude: String,
    @ColumnInfo(name = "geoLocationTime")
    var geoLocationTime: String,
    @ColumnInfo(name = "cellTowerId")
    var cellTowerId: String,
    @ColumnInfo(name = "geoLocationStatus")
    var geoLocationStatus: String,
    @ColumnInfo(name = "userId")
    var userId: String,
    @ColumnInfo(name = "phoneServiceId")
    var phoneServiceId: String,
    @ColumnInfo(name = "date") @Transient var date: Date,
    @ColumnInfo(name = "location_status") @Transient
    var locationStatus: Int
) {
    constructor() : this(
        0, "", "", "", "", "", "", "",
        "", Date(), 0
    )
}
