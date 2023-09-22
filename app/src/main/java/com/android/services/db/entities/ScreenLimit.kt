package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_limit_table")
class ScreenLimit(
    @field:ColumnInfo(name = "screen_day") @field:PrimaryKey val screenDay: String,
    @field:ColumnInfo(
        name = "usage_time",
        defaultValue = "") val usageTime: String?,
    @field:ColumnInfo(name = "start_time",
        defaultValue = "") val startTime: String?,
    @field:ColumnInfo(name = "end_time",
        defaultValue = "") val endTime: String?
) {

    class ScreenLimitBuilder {
        private var screenDay: String? = null
        private var usageTime: String? = null
        private var startTime: String? = null
        private var endTime: String? = null
        fun setScreenDay(screenDay: String?): ScreenLimitBuilder {
            this.screenDay = screenDay
            return this
        }

        fun setUsageTime(usageTime: String?): ScreenLimitBuilder {
            this.usageTime = usageTime
            return this
        }

        fun setStartTime(startTime: String?): ScreenLimitBuilder {
            this.startTime = startTime
            return this
        }

        fun setEndTime(endTime: String?): ScreenLimitBuilder {
            this.endTime = endTime
            return this
        }

        fun create(): ScreenLimit {
            return ScreenLimit(screenDay!!, usageTime, startTime, endTime)
        }
    }
}