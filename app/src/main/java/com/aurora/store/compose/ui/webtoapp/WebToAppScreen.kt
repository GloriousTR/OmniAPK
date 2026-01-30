/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.webtoapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aurora.store.R
import com.aurora.store.compose.composable.TopAppBar
import com.aurora.store.compose.preview.PreviewTemplate
import com.aurora.store.data.model.webtoapp.WebAppBuildState
import com.aurora.store.viewmodel.webtoapp.WebToAppViewModel

@Composable
fun WebToAppScreen(
    onNavigateUp: (() -> Unit)? = null,
    viewModel: WebToAppViewModel = hiltViewModel()
) {
    val buildState by viewModel.buildState.collectAsStateWithLifecycle()
    val selectedIconUri by viewModel.selectedIconUri.collectAsStateWithLifecycle()

    ScreenContent(
        appName = viewModel.appName.value,
        onAppNameChange = { viewModel.appName.value = it },
        websiteUrl = viewModel.websiteUrl.value,
        onWebsiteUrlChange = { viewModel.websiteUrl.value = it },
        enableJavaScript = viewModel.enableJavaScript.value,
        onEnableJavaScriptChange = { viewModel.enableJavaScript.value = it },
        enableDesktopMode = viewModel.enableDesktopMode.value,
        onEnableDesktopModeChange = { viewModel.enableDesktopMode.value = it },
        enableDarkMode = viewModel.enableDarkMode.value,
        onEnableDarkModeChange = { viewModel.enableDarkMode.value = it },
        enableFullscreen = viewModel.enableFullscreen.value,
        onEnableFullscreenChange = { viewModel.enableFullscreen.value = it },
        enableAdBlock = viewModel.enableAdBlock.value,
        onEnableAdBlockChange = { viewModel.enableAdBlock.value = it },
        selectedIconUri = selectedIconUri,
        onIconSelected = { viewModel.setIconUri(it) },
        buildState = buildState,
        isFormValid = viewModel.isFormValid,
        onBuildClick = { viewModel.buildWebApp() },
        onResetBuildState = { viewModel.resetBuildState() },
        onNavigateUp = onNavigateUp
    )
}

@Composable
private fun ScreenContent(
    appName: String = "",
    onAppNameChange: (String) -> Unit = {},
    websiteUrl: String = "",
    onWebsiteUrlChange: (String) -> Unit = {},
    enableJavaScript: Boolean = true,
    onEnableJavaScriptChange: (Boolean) -> Unit = {},
    enableDesktopMode: Boolean = false,
    onEnableDesktopModeChange: (Boolean) -> Unit = {},
    enableDarkMode: Boolean = false,
    onEnableDarkModeChange: (Boolean) -> Unit = {},
    enableFullscreen: Boolean = false,
    onEnableFullscreenChange: (Boolean) -> Unit = {},
    enableAdBlock: Boolean = true,
    onEnableAdBlockChange: (Boolean) -> Unit = {},
    selectedIconUri: Uri? = null,
    onIconSelected: (Uri?) -> Unit = {},
    buildState: WebAppBuildState = WebAppBuildState.Idle,
    isFormValid: Boolean = false,
    onBuildClick: () -> Unit = {},
    onResetBuildState: () -> Unit = {},
    onNavigateUp: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onIconSelected(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(R.string.title_webtoapp),
                onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🌐 " + stringResource(R.string.webtoapp_header_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.webtoapp_header_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // Icon Selection
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.webtoapp_app_icon),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { iconPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedIconUri != null) {
                                AsyncImage(
                                    model = selectedIconUri,
                                    contentDescription = "App Icon",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Icon",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.webtoapp_tap_to_select_icon),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // App Details
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.webtoapp_app_details),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        OutlinedTextField(
                            value = appName,
                            onValueChange = onAppNameChange,
                            label = { Text(stringResource(R.string.webtoapp_app_name)) },
                            placeholder = { Text(stringResource(R.string.webtoapp_app_name_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = websiteUrl,
                            onValueChange = onWebsiteUrlChange,
                            label = { Text(stringResource(R.string.webtoapp_website_url)) },
                            placeholder = { Text(stringResource(R.string.webtoapp_website_url_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Settings
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.webtoapp_settings),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        SettingsSwitchItem(
                            title = stringResource(R.string.webtoapp_enable_javascript),
                            description = stringResource(R.string.webtoapp_enable_javascript_desc),
                            checked = enableJavaScript,
                            onCheckedChange = onEnableJavaScriptChange
                        )

                        SettingsSwitchItem(
                            title = stringResource(R.string.webtoapp_enable_adblock),
                            description = stringResource(R.string.webtoapp_enable_adblock_desc),
                            checked = enableAdBlock,
                            onCheckedChange = onEnableAdBlockChange
                        )

                        SettingsSwitchItem(
                            title = stringResource(R.string.webtoapp_enable_desktop_mode),
                            description = stringResource(R.string.webtoapp_enable_desktop_mode_desc),
                            checked = enableDesktopMode,
                            onCheckedChange = onEnableDesktopModeChange
                        )

                        SettingsSwitchItem(
                            title = stringResource(R.string.webtoapp_enable_dark_mode),
                            description = stringResource(R.string.webtoapp_enable_dark_mode_desc),
                            checked = enableDarkMode,
                            onCheckedChange = onEnableDarkModeChange
                        )

                        SettingsSwitchItem(
                            title = stringResource(R.string.webtoapp_enable_fullscreen),
                            description = stringResource(R.string.webtoapp_enable_fullscreen_desc),
                            checked = enableFullscreen,
                            onCheckedChange = onEnableFullscreenChange
                        )
                    }
                }

                // Build Button
                Button(
                    onClick = onBuildClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && buildState is WebAppBuildState.Idle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apps),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.webtoapp_create_app),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Build Progress Overlay
            AnimatedVisibility(
                visible = buildState !is WebAppBuildState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BuildProgressOverlay(
                    buildState = buildState,
                    onDismiss = onResetBuildState
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun BuildProgressOverlay(
    buildState: WebAppBuildState,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (buildState) {
                    is WebAppBuildState.Preparing -> {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.webtoapp_preparing),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    is WebAppBuildState.Building -> {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.webtoapp_building),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        LinearProgressIndicator(
                            progress = { buildState.progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${buildState.progress}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is WebAppBuildState.Success -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.webtoapp_success),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.webtoapp_success_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.action_ok))
                        }
                    }

                    is WebAppBuildState.Error -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.webtoapp_error),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = buildState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.action_ok))
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Preview
@Composable
private fun WebToAppScreenPreview() {
    PreviewTemplate {
        ScreenContent()
    }
}
