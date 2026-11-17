package com.jitambulishe.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jitambulishe.app.data.AppDatabase

/**
 * Periodic sync for a blocklist you maintain elsewhere (e.g. a CSV you host).
 *
 * Right now this is a no-op: the app's core (local DB + STIR/SHAKEN + manual
 * Truecaller button) works without it. When you decide on a source, read it
 * here and call [AppDatabase.spamDao]'s upsert for each entry.
 */
class BlocklistSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).spamDao()

        // Example of what a real sync might look like:
        //
        // val rows = fetchRemoteCsv("https://example.com/kenya-spam.csv")
        // rows.forEach { row ->
        //     dao.upsert(
        //         SpamNumber(
        //             number = PhoneNumberUtils.normalize(row.number),
        //             name = row.name,
        //             isSpam = row.isSpam
        //         )
        //     )
        // }

        // No-op for now.
        return Result.success()
    }
}
