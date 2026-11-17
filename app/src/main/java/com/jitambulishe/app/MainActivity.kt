package com.jitambulishe.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jitambulishe.app.data.AppDatabase
import com.jitambulishe.app.ui.HomeScreen
import com.jitambulishe.app.ui.theme.JitambulisheTheme
import com.jitambulishe.app.workers.CallLogSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private var hasCallLogPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(true)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissions()
        if (hasCallLogPermission) enqueueImmediateScan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshPermissions()
        scheduleCallLogSync()

        setContent {
            JitambulisheTheme {
                HomeScreen(
                    database = AppDatabase.get(this),
                    hasCallLogPermission = hasCallLogPermission,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestPermissions = ::requestPermissions
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
        if (hasCallLogPermission) enqueueImmediateScan()
    }

    private fun refreshPermissions() {
        hasCallLogPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        hasNotificationPermission = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val requested = buildList {
            add(Manifest.permission.READ_CALL_LOG)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(requested.toTypedArray())
    }

    private fun scheduleCallLogSync() {
        val periodic = PeriodicWorkRequestBuilder<CallLogSyncWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CallLogSyncWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
        if (hasCallLogPermission) enqueueImmediateScan()
    }

    private fun enqueueImmediateScan() {
        WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<CallLogSyncWorker>().build())
    }
}
