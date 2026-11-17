package com.jitambulishe.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jitambulishe.app.data.AppDatabase
import com.jitambulishe.app.data.CallRecord
import com.jitambulishe.app.data.SpamNumber
import com.jitambulishe.app.util.PhoneNumberUtils
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen(
    database: AppDatabase,
    hasCallLogPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestPermissions: () -> Unit
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val dao = database.spamDao()
    val entries by dao.observeAll().collectAsState(initial = emptyList())
    val recentCalls by database.callRecordDao().observeRecent().collectAsState(initial = emptyList())

    var inputNumber by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Jitambulishe", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Caller identification after your call.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        if (hasCallLogPermission) "Post-call access is enabled"
                        else "Allow call-log access",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (hasCallLogPermission)
                            "Jitambulishe checks completed calls in the background. It does not screen, answer, reject or silence calls."
                        else
                            "Android call-log permission lets Jitambulishe review calls after they finish. It does not control live calls.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!hasCallLogPermission || !hasNotificationPermission) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onRequestPermissions) {
                            Text("Grant required permissions")
                        }
                    }
                    if (hasCallLogPermission && !hasNotificationPermission) {
                        Text(
                            "Notifications are disabled. You can still review calls in this app.",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            Text("Recent calls", style = MaterialTheme.typography.titleLarge)
            Text(
                "Call-log checks run when you open the app and periodically in the background. Android may delay background work.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!hasCallLogPermission) {
            item {
                Text(
                    "Grant call-log access to populate this list. If Android refuses the restricted permission on your device, use the manual Truecaller lookup below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (recentCalls.isEmpty()) {
            item {
                Text(
                    "No completed calls have been imported yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(recentCalls, key = { it.callLogId }) { call ->
                CallRecordRow(call = call, onLookup = {
                    val uri = Uri.parse(
                        "https://www.truecaller.com/search/ke/${call.number.removePrefix("+")}"
                    )
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }, onBlock = {
                    scope.launch {
                        dao.upsert(
                            SpamNumber(
                                number = call.number,
                                name = call.identifiedName ?: call.cachedName,
                                isSpam = true
                            )
                        )
                    }
                })
            }
        }

        item {
            Text("Local spam list", style = MaterialTheme.typography.titleLarge)
            Text(
                "These are labels for your reference. This passive version never automatically blocks or rejects calls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Mark a number as spam", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputNumber,
                        onValueChange = { inputNumber = it; error = null },
                        label = { Text("Phone number (e.g. 0712345678)") },
                        singleLine = true,
                        isError = error != null,
                        supportingText = { if (error != null) Text(error!!) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Name (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            if (!PhoneNumberUtils.isValidKenyanNumber(inputNumber)) {
                                error = "Enter a valid Kenyan number."
                            } else {
                                val normalized = PhoneNumberUtils.normalize(inputNumber)
                                scope.launch {
                                    dao.upsert(
                                        SpamNumber(
                                            number = normalized,
                                            name = inputName.ifBlank { null },
                                            isSpam = true
                                        )
                                    )
                                    inputNumber = ""
                                    inputName = ""
                                    error = null
                                }
                            }
                        }) { Text("Save") }

                        OutlinedButton(onClick = {
                            if (!PhoneNumberUtils.isValidKenyanNumber(inputNumber)) {
                                error = "Enter a valid number first."
                            } else {
                                val normalized = PhoneNumberUtils.normalize(inputNumber)
                                val uri = Uri.parse(
                                    "https://www.truecaller.com/search/ke/${normalized.removePrefix("+")}"
                                )
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }) { Text("Look up on Truecaller") }
                    }
                }
            }
        }

        if (entries.isNotEmpty()) {
            items(entries, key = { "saved_${it.number}" }) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.name ?: PhoneNumberUtils.formatForDisplay(entry.number))
                            Text(
                                entry.number,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = {
                            scope.launch { dao.delete(entry.number) }
                        }) { Text("Remove") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallRecordRow(
    call: CallRecord,
    onLookup: () -> Unit,
    onBlock: () -> Unit
) {
    val contextLabel = when (call.callType) {
        android.provider.CallLog.Calls.INCOMING_TYPE -> "Incoming"
        android.provider.CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        android.provider.CallLog.Calls.MISSED_TYPE -> "Missed"
        android.provider.CallLog.Calls.REJECTED_TYPE -> "Rejected"
        else -> "Call"
    }
    val displayName = call.identifiedName ?: call.cachedName ?: call.number
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(displayName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "$contextLabel · ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(call.callDate))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (call.isSpam) {
                    Text("On blocklist", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onLookup) { Text("Truecaller lookup") }
                OutlinedButton(onClick = onBlock) { Text("Block number") }
            }
        }
    }
}
