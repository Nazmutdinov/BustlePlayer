package com.example.bustleplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bustleplayer.data.local.entities.TrackInfoEntity

@Database(
    entities = [TrackInfoEntity::class],
    version = 1,
    exportSchema = false
)

abstract class BustleDatabase: RoomDatabase() {
    abstract fun getDao(): BustleInfoDao

    companion object {
        @Volatile
        private var INSTANCE: BustleDatabase? = null

        fun getDatabase(context: Context): BustleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    BustleDatabase::class.java,
                    "bustle1107"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}