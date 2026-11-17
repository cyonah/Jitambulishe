package com.jitambulishe.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SpamNumber::class, CallRecord::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spamDao(): SpamDao
    abstract fun callRecordDao(): CallRecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS call_records (
                        callLogId INTEGER NOT NULL PRIMARY KEY,
                        number TEXT NOT NULL,
                        cachedName TEXT,
                        callType INTEGER NOT NULL,
                        callDate INTEGER NOT NULL,
                        durationSeconds INTEGER NOT NULL,
                        identifiedName TEXT,
                        isSpam INTEGER NOT NULL
                    )"""
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jitambulishe.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
