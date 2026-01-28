package com.aurora.store.compose.ui.repos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.viewmodel.RepoManagementViewModel
import com.aurora.store.viewmodel.SyncState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoManagementScreen(
    viewModel: RepoManagementViewModel,
    onNavigateUp: () -> Unit
) {
    val repos by viewModel.repos.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val appCount by viewModel.appCount.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    val enabledCount = repos.count { it.enabled }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("F-Droid Repositories") },
                actions = {
                    IconButton(onClick = { viewModel.resetDefaults() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Defaults")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Repo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sync Card
            SyncCard(
                syncState = syncState,
                lastSyncTime = lastSyncTime,
                appCount = appCount,
                enabledCount = enabledCount,
                onSyncClick = { viewModel.startSync() }
            )
            
            // Repos List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(repos, key = { it.id }) { repo ->
                    RepoItem(
                        repo = repo,
                        onToggle = { enabled -> viewModel.toggleRepo(repo, enabled) },
                        onDelete = { viewModel.removeRepo(repo) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddRepoDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, address ->
                    viewModel.addRepo(name, address)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun SyncCard(
    syncState: SyncState,
    lastSyncTime: Long?,
    appCount: Int,
    enabledCount: Int,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    when (syncState) {
                        is SyncState.Idle -> {
                            Text(
                                text = if (lastSyncTime != null) {
                                    "Last sync: ${formatTime(lastSyncTime)}"
                                } else {
                                    "Not synced yet"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is SyncState.Syncing -> {
                            val progressPercent = if (syncState.totalRepos > 0) {
                                ((syncState.currentRepoIndex.toFloat() / syncState.totalRepos.toFloat()) * 100).toInt()
                            } else 0
                            val syncingText = if (syncState.currentRepo.isNotEmpty()) {
                                "Syncing ${syncState.currentRepo} (${syncState.currentRepoIndex}/${syncState.totalRepos}) - %$progressPercent"
                            } else {
                                "Syncing..."
                            }
                            Column {
                                Text(
                                    text = syncingText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (syncState.totalRepos > 0) {
                                    LinearProgressIndicator(
                                        progress = { syncState.currentRepoIndex.toFloat() / syncState.totalRepos.toFloat() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        is SyncState.Success -> {
                            Text(
                                text = "Sync completed!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        is SyncState.Error -> {
                            Text(
                                text = syncState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "$enabledCount repos enabled • $appCount apps cached",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onSyncClick,
                    enabled = syncState !is SyncState.Syncing && enabledCount > 0
                ) {
                    if (syncState is SyncState.Syncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync")
                }
            }
        }
    }
}

@Composable
fun RepoItem(
    repo: FDroidRepo,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = repo.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = repo.address,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (repo.description.isNotEmpty()) {
                    Text(
                        text = repo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = repo.enabled,
                onCheckedChange = onToggle
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AddRepoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("URL") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && address.isNotBlank()) onAdd(name, address) }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
