package com.jitambulishe.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamDao {

    @Query("SELECT * FROM spam_numbers WHERE number = :number LIMIT 1")
    suspend fun lookup(number: String): SpamNumber?

    @Query("SELECT EXISTS(SELECT 1 FROM spam_numbers WHERE number = :number AND isSpam = 1)")
    suspend fun isSpam(number: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: SpamNumber)

    @Query("DELETE FROM spam_numbers WHERE number = :number")
    suspend fun delete(number: String)

    @Query("SELECT * FROM spam_numbers ORDER BY lastUpdated DESC")
    fun observeAll(): Flow<List<SpamNumber>>
}
