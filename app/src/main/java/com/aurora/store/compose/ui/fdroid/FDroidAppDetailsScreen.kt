/*
 * OmniAPK - F-Droid App Details Screen
 * Shows detailed information about an F-Droid app including
 * description, version info, and download options.
 */

package com.aurora.store.compose.ui.fdroid

import android.content.Intent
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.store.R
import com.aurora.store.compose.composable.TopAppBar
import com.aurora.store.data.room.fdroid.FDroidAppEntity
import com.aurora.store.viewmodel.fdroid.FDroidAppDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FDroidAppDetailsScreen(
    packageName: String,
    onNavigateUp: () -> Unit,
    viewModel: FDroidAppDetailsViewModel = hiltViewModel(key = packageName)
) {
    val context = LocalContext.current
    val app by viewModel.app.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) {
        viewModel.loadApp(packageName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = app?.name ?: stringResource(R.string.title_open_source),
                onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
            app == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apps_outage),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.toast_app_unavailable),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {
                val currentApp = app ?: return@Scaffold
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Header
                    AppHeader(app = currentApp)

                    // Action Buttons
                    ActionButtons(
                        downloadUrl = currentApp.downloadUrl,
                        webSite = currentApp.webSite,
                        sourceCode = currentApp.sourceCode,
                        onDownload = {
                            if (currentApp.downloadUrl.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentApp.downloadUrl))
                                context.startActivity(intent)
                            }
                        },
                        onOpenWebsite = {
                            if (currentApp.webSite.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentApp.webSite))
                                context.startActivity(intent)
                            }
                        },
                        onOpenSourceCode = {
                            if (currentApp.sourceCode.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentApp.sourceCode))
                                context.startActivity(intent)
                            }
                        }
                    )

                    // Categories
                    if (currentApp.categories.isNotEmpty()) {
                        CategoriesSection(categories = currentApp.categories)
                    }

                    // Description
                    if (currentApp.description.isNotEmpty()) {
                        DescriptionSection(
                            summary = currentApp.summary,
                            description = currentApp.description
                        )
                    } else if (currentApp.summary.isNotEmpty()) {
                        DescriptionSection(
                            summary = currentApp.summary,
                            description = ""
                        )
                    }

                    // App Info
                    AppInfoSection(app = currentApp)

                    // Repository Info
                    RepositoryInfoSection(
                        repoName = currentApp.repoName,
                        repoAddress = currentApp.repoAddress
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader(app: FDroidAppEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(app.iconUrl)
                .crossfade(true)
                .build(),
            contentDescription = app.name,
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp)),
            placeholder = painterResource(R.drawable.ic_app_placeholder),
            error = painterResource(R.drawable.ic_app_placeholder)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "v${app.versionName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActionButtons(
    downloadUrl: String,
    webSite: String,
    sourceCode: String,
    onDownload: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenSourceCode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onDownload,
            enabled = downloadUrl.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.action_get))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onOpenWebsite,
                enabled = webSite.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.details_website), maxLines = 1)
            }

            OutlinedButton(
                onClick = onOpenSourceCode,
                enabled = sourceCode.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.details_source_code), maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoriesSection(categories: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    SuggestionChip(
                        onClick = { /* Category filtering can be added in the future */ },
                        label = { Text(text = category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(summary: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_more_about_app),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppInfoSection(app: FDroidAppEntity) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_app_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Version
            InfoRow(
                label = stringResource(R.string.details_version),
                value = app.versionName
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Version Code
            InfoRow(
                label = stringResource(R.string.details_versionCode),
                value = app.versionCode.toString()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Size
            if (app.size > 0) {
                InfoRow(
                    label = stringResource(R.string.details_size),
                    value = Formatter.formatFileSize(context, app.size)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Min SDK
            if (app.minSdkVersion > 0) {
                InfoRow(
                    label = stringResource(R.string.details_min_sdk),
                    value = "Android ${app.minSdkVersion}+"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // License
            if (app.license.isNotEmpty()) {
                InfoRow(
                    label = stringResource(R.string.fdroid_license),
                    value = app.license
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Last Updated
            if (app.lastUpdated > 0) {
                InfoRow(
                    label = stringResource(R.string.fdroid_last_updated),
                    value = dateFormat.format(Date(app.lastUpdated))
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Added
            if (app.added > 0) {
                InfoRow(
                    label = stringResource(R.string.fdroid_added),
                    value = dateFormat.format(Date(app.added))
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RepositoryInfoSection(repoName: String, repoAddress: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.fdroid_repository),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = repoName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = repoAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
