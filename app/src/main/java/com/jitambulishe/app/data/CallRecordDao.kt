package com.jitambulishe.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: CallRecord)

    @Query("SELECT * FROM call_records ORDER BY callDate DESC LIMIT 50")
    fun observeRecent(): Flow<List<CallRecord>>

    @Query("SELECT EXISTS(SELECT 1 FROM call_records WHERE callLogId = :id)")
    suspend fun exists(id: Long): Boolean
}
