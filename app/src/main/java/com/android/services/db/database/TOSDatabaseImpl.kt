package com.android.services.db.database

import android.content.Context
import androidx.room.Room

object TOSDatabaseImpl {

    private var INSTANCE: TOSDatabase? = null

    fun getAppDatabase(context: Context): TOSDatabase {
        if (INSTANCE == null) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        TOSDatabase::class.java,
                        "tos.db"
                    ).fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
        return INSTANCE!!
    }
}