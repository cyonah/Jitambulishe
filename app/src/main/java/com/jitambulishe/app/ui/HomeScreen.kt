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
import com.jitambulishe.app.data.SpamNumber
import com.jitambulishe.app.util.PhoneNumberUtils
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    isDefaultCallerIdApp: Boolean,
    onRequestDefault: () -> Unit,
    database: AppDatabase
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val dao = database.spamDao()

    // Simple way to trigger a refresh after adds/deletes without a ViewModel.
    var refreshTrigger by remember { mutableStateOf(0) }
    val entriesFlow = remember(refreshTrigger) { dao.observeAll() }
    val entries by entriesFlow.collectAsState(initial = emptyList())

    var inputNumber by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Jitambulishe",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Identify yourself — before you pick up.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        if (!isDefaultCallerIdApp) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Jitambulishe is not your default caller ID app.",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Enable it so calls are screened automatically.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRequestDefault) {
                        Text("Set as default caller ID app")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Add a number manually
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Add to blocklist", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputNumber,
                    onValueChange = { inputNumber = it },
                    label = { Text("Phone number (e.g. 0712345678)") },
                    singleLine = true,
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
                    Button(
                        onClick = {
                            if (inputNumber.isBlank()) return@Button
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
                                refreshTrigger++
                            }
                        }
                    ) { Text("Save") }

                    OutlinedButton(
                        onClick = {
                            if (inputNumber.isBlank()) return@OutlinedButton
                            val normalized = PhoneNumberUtils.normalize(inputNumber)
                            val url = "https://www.truecaller.com/search/ke/${
                                normalized.removePrefix("+")
                            }"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) { Text("Look up on Truecaller") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Saved numbers", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text(
                "No numbers saved yet. Add one above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries, key = { it.number }) { entry ->
                    EntryRow(
                        entry = entry,
                        onDelete = {
                            scope.launch {
                                dao.delete(entry.number)
                                refreshTrigger++
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: SpamNumber, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name ?: PhoneNumberUtils.formatForDisplay(entry.number),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = entry.number,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onDelete) { Text("Remove") }
        }
    }
}
