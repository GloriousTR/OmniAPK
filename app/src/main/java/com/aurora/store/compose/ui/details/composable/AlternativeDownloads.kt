/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.store.R
import com.aurora.store.compose.preview.PreviewTemplate
import com.aurora.store.data.installer.InstallResult
import com.aurora.store.data.installer.XAPKInstaller
import com.aurora.store.data.providers.AppVersion
import com.aurora.store.viewmodel.alternative.AlternativeDownloadState
import com.aurora.store.viewmodel.alternative.AlternativeDownloadViewModel
import dagger.hilt.android.EntryPointAccessors

/**
 * Composable to display alternative download options (APKMirror, APKPure)
 * with in-app downloading support
 * @param packageName The package name of the app
 * @param appName The display name of the app
 */
@Composable
fun AlternativeDownloads(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    viewModel: AlternativeDownloadViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val versions by viewModel.versions.collectAsStateWithLifecycle()
    val progress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages by viewModel.totalPages.collectAsStateWithLifecycle()
    
    // Preload versions in background when component mounts
    LaunchedEffect(packageName) {
        viewModel.preloadVersions(packageName)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // APKMirror Button
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedSource = "APKMirror"
                showDialog = true
                viewModel.loadAPKMirrorVersions(packageName)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.download_via_apkmirror),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // APKPure Button
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedSource = "APKPure"
                showDialog = true
                viewModel.loadAPKPureVersions(packageName)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.padding_small)
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_download),
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.download_via_apkpure),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Alternative Download Dialog
    if (showDialog) {
        AlternativeDownloadDialog(
            source = selectedSource,
            appName = appName,
            state = state,
            versions = versions,
            progress = progress,
            currentPage = currentPage,
            totalPages = totalPages,
            onPageChange = { page -> viewModel.goToPage(page) },
            onVersionSelected = { version -> viewModel.downloadVersion(version) },
            onDismiss = {
                showDialog = false
                viewModel.reset()
            }
        )
    }
}

@Composable
private fun AlternativeDownloadDialog(
    source: String,
    appName: String,
    state: AlternativeDownloadState,
    versions: List<AppVersion>,
    progress: Int,
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onVersionSelected: (AppVersion) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (state !is AlternativeDownloadState.Downloading) {
                onDismiss()
            }
        },
        title = {
            Text(text = "$source - $appName")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is AlternativeDownloadState.Idle,
                    is AlternativeDownloadState.Loading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.alternative_loading_versions))
                    }

                    is AlternativeDownloadState.VersionsLoaded -> {
                        if (versions.isEmpty()) {
                            Text(text = stringResource(R.string.alternative_no_versions))
                        } else {
                            Text(
                                text = stringResource(R.string.alternative_select_version),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier.height(250.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(versions) { version ->
                                    VersionItem(
                                        version = version,
                                        onClick = { onVersionSelected(version) }
                                    )
                                }
                            }
                            
                            // Pagination controls
                            if (totalPages > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { onPageChange(currentPage - 1) },
                                        enabled = currentPage > 1
                                    ) {
                                        Text("<")
                                    }
                                    
                                    // Page numbers
                                    val pageRange = (maxOf(1, currentPage - 2)..minOf(totalPages, currentPage + 2))
                                    pageRange.forEach { page ->
                                        TextButton(
                                            onClick = { onPageChange(page) }
                                        ) {
                                            Text(
                                                text = page.toString(),
                                                fontWeight = if (page == currentPage) FontWeight.Bold else FontWeight.Normal,
                                                color = if (page == currentPage) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    
                                    TextButton(
                                        onClick = { onPageChange(currentPage + 1) },
                                        enabled = currentPage < totalPages
                                    ) {
                                        Text(">")
                                    }
                                }
                            }
                        }
                    }

                    is AlternativeDownloadState.Downloading -> {
                        Text(
                            text = stringResource(R.string.alternative_downloading),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "%$progress")
                    }

                    is AlternativeDownloadState.Downloaded -> {
                        val xapkInstaller = remember {
                            XAPKInstaller(context)
                        }
                        var installState by remember {
                            mutableStateOf<String?>(null)
                        }
                        var isInstalling by remember { mutableStateOf(true) }

                        LaunchedEffect(state.file) {
                            isInstalling = true
                            val result = xapkInstaller.installAuto(state.file)
                            isInstalling = false
                            installState = when (result) {
                                is InstallResult.Success -> null
                                is InstallResult.Error -> result.message
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.alternative_download_complete),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isInstalling) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.alternative_installing),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (installState != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = installState!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is AlternativeDownloadState.Error -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (state is AlternativeDownloadState.Downloaded ||
                state is AlternativeDownloadState.Error
            ) {
                Button(onClick = onDismiss) {
                    Text(text = stringResource(R.string.action_close))
                }
            }
        },
        dismissButton = {
            if (state !is AlternativeDownloadState.Downloading) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        }
    )
}

@Composable
private fun VersionItem(
    version: AppVersion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = version.versionName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (version.size.isNotEmpty()) {
                    Text(
                        text = version.size,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlternativeDownloadsPreview() {
    PreviewTemplate {
        AlternativeDownloads(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
        )
    }
}
