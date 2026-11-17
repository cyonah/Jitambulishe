package com.jitambulishe.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single entry in the local spam / caller-ID database.
 *
 * [number] is stored in E.164 form (e.g. "+254712345678"). Normalize on write.
 */
@Entity(tableName = "spam_numbers")
data class SpamNumber(
    @PrimaryKey val number: String,
    val name: String? = null,
    val isSpam: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
