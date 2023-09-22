package com.android.services.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_app_table")
class BlockedApp(
    @field:ColumnInfo(name = "package_name") @field:PrimaryKey val packageName: String,
    @field:ColumnInfo(
        name = "is_blocked") val isBlocked: String?
) {

    class BlockedAppBuilder {
        private var packageName: String? = null
        private var isBlocked: String? = null
        fun setPackageName(packageName: String?): BlockedAppBuilder {
            this.packageName = packageName
            return this
        }

        fun setIsBlocked(isBlocked: String?): BlockedAppBuilder {
            this.isBlocked = isBlocked
            return this
        }

        fun create(): BlockedApp {
            return BlockedApp(packageName!!, isBlocked)
        }
    }

    override fun toString(): String {
        return "BlockedApp{" +
                "packageName='" + packageName + '\'' +
                ", isBlocked=" + isBlocked +
                '}'
    }
}