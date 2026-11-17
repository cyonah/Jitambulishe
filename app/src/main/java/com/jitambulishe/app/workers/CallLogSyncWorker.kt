package com.jitambulishe.app.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.CallLog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jitambulishe.app.R
import com.jitambulishe.app.data.AppDatabase
import com.jitambulishe.app.data.CallRecord
import com.jitambulishe.app.util.PhoneNumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads completed call-log entries after the phone has already handled the call.
 * It never participates in ringing, answering, rejecting, or ending a call.
 */
class CallLogSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) return@withContext Result.success()

        try {
            val prefs = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            // Start from the moment permission is first granted; do not import the
            // user's entire historical call log or generate a burst of old alerts.
            if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
                prefs.edit()
                    .putLong(KEY_LAST_DATE, System.currentTimeMillis())
                    .putBoolean(KEY_INITIALIZED, true)
                    .apply()
                return@withContext Result.success()
            }
            val lastSeenDate = prefs.getLong(KEY_LAST_DATE, 0L)
            val db = AppDatabase.get(applicationContext)
            var newestDate = lastSeenDate
            val resolver = applicationContext.contentResolver
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val selection = "${CallLog.Calls.DATE} > ?"
            val selectionArgs = arrayOf(lastSeenDate.toString())

            resolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CallLog.Calls.DATE} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                val numberCol = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val nameCol = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val typeCol = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val durationCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawNumber = cursor.getString(numberCol).orEmpty()
                    val normalized = PhoneNumberUtils.normalize(rawNumber)
                    val date = cursor.getLong(dateCol)
                    if (date > newestDate) newestDate = date
                    if (normalized.isBlank()) continue
                    if (db.callRecordDao().exists(id)) continue

                    val localMatch = db.spamDao().lookup(normalized)
                    val savedName = localMatch?.name ?: cursor.getString(nameCol)
                    val record = CallRecord(
                        callLogId = id,
                        number = normalized,
                        cachedName = cursor.getString(nameCol),
                        callType = cursor.getInt(typeCol),
                        callDate = date,
                        durationSeconds = cursor.getLong(durationCol),
                        identifiedName = savedName,
                        isSpam = localMatch?.isSpam == true
                    )
                    db.callRecordDao().upsert(record)
                    postCallNotification(id.toInt(), normalized, savedName, localMatch?.isSpam == true)
                }
            }
            if (newestDate > lastSeenDate) {
                prefs.edit().putLong(KEY_LAST_DATE, newestDate).apply()
            }
            Result.success()
        } catch (_: SecurityException) {
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun postCallNotification(id: Int, number: String, name: String?, isSpam: Boolean) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Post-call identification", NotificationManager.IMPORTANCE_LOW)
            )
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) return

        val title = when {
            isSpam -> "Number on your blocklist"
            !name.isNullOrBlank() -> name
            else -> "Call completed"
        }
        val text = when {
            isSpam -> "$number is in your local spam blocklist."
            !name.isNullOrBlank() -> "$number · Matched your saved caller list."
            else -> "Caller: $number · Tap Jitambulishe to review."
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "jitambulishe_call_log_sync"
        private const val PREFS = "call_log_sync"
        private const val KEY_LAST_DATE = "last_seen_call_date"
        private const val KEY_INITIALIZED = "initialized"
        private const val CHANNEL_ID = "post_call_identification"
    }
}
