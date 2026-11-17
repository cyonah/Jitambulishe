package com.jitambulishe.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey val callLogId: Long,
    val number: String,
    val cachedName: String?,
    val callType: Int,
    val callDate: Long,
    val durationSeconds: Long,
    val identifiedName: String? = null,
    val isSpam: Boolean = false
)
