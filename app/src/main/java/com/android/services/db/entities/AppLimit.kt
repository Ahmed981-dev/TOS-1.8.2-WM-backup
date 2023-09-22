package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limit_table")
class AppLimit(
    @field:ColumnInfo(name = "package_name") @field:PrimaryKey val packageName: String,
    @field:ColumnInfo(
        name = "app_name") val appName: String?,
    @field:ColumnInfo(name = "usage_time") val usageTime: String?,
) {

    class AppLimitBuilder {
        private var packageName: String? = null
        private var appName: String? = null
        private var usageTime: String? = null

        fun setPackageName(packageName: String?): AppLimitBuilder {
            this.packageName = packageName
            return this
        }

        fun setAppName(appName: String?): AppLimitBuilder {
            this.appName = appName
            return this
        }

        fun setUsageTime(usageTime: String?): AppLimitBuilder {
            this.usageTime = usageTime
            return this
        }

        fun create(): AppLimit {
            return AppLimit(packageName!!, appName, usageTime)
        }
    }
}