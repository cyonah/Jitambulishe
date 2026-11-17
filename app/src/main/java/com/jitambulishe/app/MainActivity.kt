package com.jitambulishe.app

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jitambulishe.app.data.AppDatabase
import com.jitambulishe.app.ui.HomeScreen
import com.jitambulishe.app.ui.theme.JitambulisheTheme
import com.jitambulishe.app.workers.BlocklistSyncWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* Result ignored: the system tells us if we became default. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleSync()

        setContent {
            JitambulisheTheme {
                val context = this
                var isDefault by remember { mutableStateOf(isDefaultCallerIdApp()) }

                HomeScreen(
                    isDefaultCallerIdApp = isDefault,
                    onRequestDefault = {
                        requestDefaultCallerIdRole()
                    },
                    database = AppDatabase.get(context)
                )
            }
        }
    }

    private fun isDefaultCallerIdApp(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestDefaultCallerIdRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val roleManager = getSystemService(RoleManager::class.java) ?: return
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) return
        val intent: Intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
        roleLauncher.launch(intent)
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()
        val request = PeriodicWorkRequestBuilder<BlocklistSyncWorker>(30, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "blocklist_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
