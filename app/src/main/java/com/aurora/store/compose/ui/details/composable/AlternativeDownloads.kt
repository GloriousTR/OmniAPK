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
 * with WebView-based browsing and in-app downloading support
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
    var showWebViewDialog by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val versions by viewModel.versions.collectAsStateWithLifecycle()
    val progress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages by viewModel.totalPages.collectAsStateWithLifecycle()
    
    // Preload versions in background when component mounts (for fallback)
    LaunchedEffect(packageName) {
        viewModel.preloadVersions(packageName)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // APKMirror Button - WebView based (primary)
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedSource = "APKMirror"
                showWebViewDialog = true
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
                    text = stringResource(R.string.browse_apkmirror),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // APKPure Button - WebView based (primary)
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedSource = "APKPure"
                showWebViewDialog = true
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
                    text = stringResource(R.string.browse_apkpure),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // WebView Download Dialog - New approach
    WebViewDownloadDialog(
        source = selectedSource,
        packageName = packageName,
        appName = appName,
        isVisible = showWebViewDialog,
        onDismiss = {
            showWebViewDialog = false
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
