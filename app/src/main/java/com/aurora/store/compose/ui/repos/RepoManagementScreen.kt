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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.viewmodel.RepoManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoManagementScreen(
    viewModel: RepoManagementViewModel,
    onNavigateUp: () -> Unit
) {
    val repos by viewModel.repos.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("F-Droid Repositories") },
                navigationIcon = {
                    // Back button handled by Fragment hosting this composition usually, 
                    // but we can add an IconButton here calling onNavigateUp
                },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
