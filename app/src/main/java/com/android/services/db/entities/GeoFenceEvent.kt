package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "geo_fence_event_table")
class GeoFenceEvent {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @Transient
    var id = 0

    @ColumnInfo(name = "geoFenceId")
    var geoFenceId: String? = null

    @ColumnInfo(name = "event")
    var event: String? = null

    @ColumnInfo(name = "eventDatetime")
    var eventDatetime: String? = null

    @ColumnInfo(name = "status")
    @Transient
    var status = 0

    constructor()

    @Ignore
    constructor(id: Int, geoFenceId: String?, event: String?, eventDatetime: String?, status: Int) {
        this.id = id
        this.geoFenceId = geoFenceId
        this.event = event
        this.eventDatetime = eventDatetime
        this.status = status
    }
}