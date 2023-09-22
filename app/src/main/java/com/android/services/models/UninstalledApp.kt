package com.android.services.models

import androidx.room.ColumnInfo
import androidx.room.Ignore

class UninstalledApp {
    @ColumnInfo(name = "package_name")
    var packageName: String? = null

    @ColumnInfo(name = "app_name")
    var name: String? = null

    constructor()

    @Ignore
    constructor(packageName: String?, name: String?) {
        this.packageName = packageName
        this.name = name
    }
}