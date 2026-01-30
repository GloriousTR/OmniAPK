/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.webtoapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aurora.store.R
import com.aurora.store.compose.composable.TopAppBar
import com.aurora.store.compose.preview.PreviewTemplate
import com.aurora.store.data.model.webtoapp.AnnouncementTemplate
import com.aurora.store.data.model.webtoapp.ExtensionModule
import com.aurora.store.data.model.webtoapp.ScreenOrientation
import com.aurora.store.data.model.webtoapp.UserAgentType
import com.aurora.store.data.model.webtoapp.WebAppBuildState
import com.aurora.store.viewmodel.webtoapp.SettingsSection
import com.aurora.store.viewmodel.webtoapp.WebToAppViewModel

@Composable
fun WebToAppScreen(
    onNavigateUp: (() -> Unit)? = null,
    viewModel: WebToAppViewModel = hiltViewModel()
) {
    val buildState by viewModel.buildState.collectAsStateWithLifecycle()
    val selectedIconUri by viewModel.selectedIconUri.collectAsStateWithLifecycle()
    val splashImageUri by viewModel.splashImageUri.collectAsStateWithLifecycle()
    val bgmUri by viewModel.bgmUri.collectAsStateWithLifecycle()

    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setIconUri(uri) }

    val splashPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setSplashImageUri(uri) }

    val bgmPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.setBgmUri(uri) }

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
                HeaderCard()

                // Basic Info Section
                BasicInfoSection(
                    appName = viewModel.appName.value,
                    onAppNameChange = { viewModel.appName.value = it },
                    websiteUrl = viewModel.websiteUrl.value,
                    onWebsiteUrlChange = { viewModel.websiteUrl.value = it },
                    packageName = viewModel.packageName.value,
                    onPackageNameChange = { viewModel.packageName.value = it },
                    selectedIconUri = selectedIconUri,
                    onSelectIcon = { iconPickerLauncher.launch("image/*") }
                )

                // Display & Appearance Section
                DisplaySection(viewModel)

                // Privacy & Security Section
                PrivacySection(viewModel)

                // Navigation & Behavior Section
                NavigationSection(viewModel)

                // Media Section
                MediaSection(viewModel)

                // Splash Screen Section
                SplashSection(
                    viewModel = viewModel,
                    splashImageUri = splashImageUri,
                    onSelectSplash = { splashPickerLauncher.launch("image/*") }
                )

                // Background Music Section
                BgmSection(
                    viewModel = viewModel,
                    bgmUri = bgmUri,
                    onSelectBgm = { bgmPickerLauncher.launch("audio/*") }
                )

                // Activation Code Section
                ActivationSection(viewModel)

                // Announcement Section
                AnnouncementSection(viewModel)

                // Forced Run Section
                ForcedRunSection(viewModel)

                // Extension Modules Section
                ExtensionsSection(viewModel)

                // Advanced Section
                AdvancedSection(viewModel)

                // Build Button
                Button(
                    onClick = { viewModel.buildWebApp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = viewModel.isFormValid && buildState is WebAppBuildState.Idle,
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

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Build Progress Overlay
            AnimatedVisibility(
                visible = buildState !is WebAppBuildState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BuildProgressOverlay(
                    buildState = buildState,
                    onDismiss = { viewModel.resetBuildState() }
                )
            }
        }
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
}

@Composable
private fun BasicInfoSection(
    appName: String,
    onAppNameChange: (String) -> Unit,
    websiteUrl: String,
    onWebsiteUrlChange: (String) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    selectedIconUri: Uri?,
    onSelectIcon: () -> Unit
) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_basic),
        icon = "📱",
        initiallyExpanded = true
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onSelectIcon() },
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
            }
            Text(
                text = stringResource(R.string.webtoapp_tap_to_select_icon),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

            OutlinedTextField(
                value = packageName,
                onValueChange = onPackageNameChange,
                label = { Text(stringResource(R.string.webtoapp_package_name)) },
                placeholder = { Text(stringResource(R.string.webtoapp_package_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun DisplaySection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_display),
        icon = "🎨"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_fullscreen),
                description = stringResource(R.string.webtoapp_enable_fullscreen_desc),
                checked = viewModel.enableFullscreen.value,
                onCheckedChange = { viewModel.enableFullscreen.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_desktop_mode),
                description = stringResource(R.string.webtoapp_enable_desktop_mode_desc),
                checked = viewModel.enableDesktopMode.value,
                onCheckedChange = { viewModel.enableDesktopMode.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_dark_mode),
                description = stringResource(R.string.webtoapp_enable_dark_mode_desc),
                checked = viewModel.enableDarkMode.value,
                onCheckedChange = { viewModel.enableDarkMode.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_keep_screen_on),
                description = stringResource(R.string.webtoapp_keep_screen_on_desc),
                checked = viewModel.keepScreenOn.value,
                onCheckedChange = { viewModel.keepScreenOn.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_immersive_mode),
                description = stringResource(R.string.webtoapp_immersive_mode_desc),
                checked = viewModel.immersiveMode.value,
                onCheckedChange = { viewModel.immersiveMode.value = it }
            )

            // Screen Orientation Dropdown
            DropdownItem(
                title = stringResource(R.string.webtoapp_screen_orientation),
                selectedValue = viewModel.screenOrientation.value.name,
                options = ScreenOrientation.entries.map { it.name },
                onValueChange = { viewModel.screenOrientation.value = ScreenOrientation.valueOf(it) }
            )
        }
    }
}

@Composable
private fun PrivacySection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_privacy),
        icon = "🛡️"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_javascript),
                description = stringResource(R.string.webtoapp_enable_javascript_desc),
                checked = viewModel.enableJavaScript.value,
                onCheckedChange = { viewModel.enableJavaScript.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_adblock),
                description = stringResource(R.string.webtoapp_enable_adblock_desc),
                checked = viewModel.enableAdBlock.value,
                onCheckedChange = { viewModel.enableAdBlock.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_popup_block),
                description = stringResource(R.string.webtoapp_enable_popup_block_desc),
                checked = viewModel.enablePopupBlock.value,
                onCheckedChange = { viewModel.enablePopupBlock.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_anti_tracking),
                description = stringResource(R.string.webtoapp_enable_anti_tracking_desc),
                checked = viewModel.enableAntiTracking.value,
                onCheckedChange = { viewModel.enableAntiTracking.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_fingerprint_spoofing),
                description = stringResource(R.string.webtoapp_enable_fingerprint_spoofing_desc),
                checked = viewModel.enableFingerprintSpoofing.value,
                onCheckedChange = { viewModel.enableFingerprintSpoofing.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_incognito_mode),
                description = stringResource(R.string.webtoapp_incognito_mode_desc),
                checked = viewModel.enableIncognitoMode.value,
                onCheckedChange = { viewModel.enableIncognitoMode.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_clear_cache_on_exit),
                description = stringResource(R.string.webtoapp_clear_cache_on_exit_desc),
                checked = viewModel.clearCacheOnExit.value,
                onCheckedChange = { viewModel.clearCacheOnExit.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_clear_cookies_on_exit),
                description = stringResource(R.string.webtoapp_clear_cookies_on_exit_desc),
                checked = viewModel.clearCookiesOnExit.value,
                onCheckedChange = { viewModel.clearCookiesOnExit.value = it }
            )

            // User Agent Dropdown
            DropdownItem(
                title = stringResource(R.string.webtoapp_user_agent),
                selectedValue = viewModel.userAgentType.value.name,
                options = UserAgentType.entries.map { it.name },
                onValueChange = { viewModel.userAgentType.value = UserAgentType.valueOf(it) }
            )

            if (viewModel.userAgentType.value == UserAgentType.CUSTOM) {
                OutlinedTextField(
                    value = viewModel.customUserAgent.value,
                    onValueChange = { viewModel.customUserAgent.value = it },
                    label = { Text(stringResource(R.string.webtoapp_custom_user_agent)) },
                    placeholder = { Text(stringResource(R.string.webtoapp_custom_user_agent_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun NavigationSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_navigation),
        icon = "🧭"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_pull_to_refresh),
                description = stringResource(R.string.webtoapp_pull_to_refresh_desc),
                checked = viewModel.enablePullToRefresh.value,
                onCheckedChange = { viewModel.enablePullToRefresh.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_swipe_navigation),
                description = stringResource(R.string.webtoapp_swipe_navigation_desc),
                checked = viewModel.enableSwipeNavigation.value,
                onCheckedChange = { viewModel.enableSwipeNavigation.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_back_button_exit),
                description = stringResource(R.string.webtoapp_back_button_exit_desc),
                checked = viewModel.enableBackButtonExit.value,
                onCheckedChange = { viewModel.enableBackButtonExit.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_confirm_exit),
                description = stringResource(R.string.webtoapp_confirm_exit_desc),
                checked = viewModel.confirmExit.value,
                onCheckedChange = { viewModel.confirmExit.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_external_links),
                description = stringResource(R.string.webtoapp_external_links_desc),
                checked = viewModel.openExternalLinksInBrowser.value,
                onCheckedChange = { viewModel.openExternalLinksInBrowser.value = it }
            )
        }
    }
}

@Composable
private fun MediaSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_media),
        icon = "🎬"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_downloads),
                description = stringResource(R.string.webtoapp_enable_downloads_desc),
                checked = viewModel.enableDownloads.value,
                onCheckedChange = { viewModel.enableDownloads.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_media_playback),
                description = stringResource(R.string.webtoapp_enable_media_playback_desc),
                checked = viewModel.enableMediaPlayback.value,
                onCheckedChange = { viewModel.enableMediaPlayback.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_video_fullscreen),
                description = stringResource(R.string.webtoapp_video_fullscreen_desc),
                checked = viewModel.enableVideoFullscreen.value,
                onCheckedChange = { viewModel.enableVideoFullscreen.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_mute_audio),
                description = stringResource(R.string.webtoapp_mute_audio_desc),
                checked = viewModel.muteAudio.value,
                onCheckedChange = { viewModel.muteAudio.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_autoplay_media),
                description = stringResource(R.string.webtoapp_autoplay_media_desc),
                checked = viewModel.autoplayMedia.value,
                onCheckedChange = { viewModel.autoplayMedia.value = it }
            )
        }
    }
}

@Composable
private fun SplashSection(
    viewModel: WebToAppViewModel,
    splashImageUri: Uri?,
    onSelectSplash: () -> Unit
) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_splash),
        icon = "🎨"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_splash),
                description = stringResource(R.string.webtoapp_enable_splash_desc),
                checked = viewModel.enableSplashScreen.value,
                onCheckedChange = { viewModel.enableSplashScreen.value = it }
            )

            if (viewModel.enableSplashScreen.value) {
                Spacer(modifier = Modifier.height(12.dp))

                // Splash Image Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelectSplash() },
                    contentAlignment = Alignment.Center
                ) {
                    if (splashImageUri != null) {
                        AsyncImage(
                            model = splashImageUri,
                            contentDescription = "Splash Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text(stringResource(R.string.webtoapp_tap_to_select_splash))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Slider
                Text(
                    text = "${stringResource(R.string.webtoapp_splash_duration)}: ${viewModel.splashDurationMs.value}ms",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = viewModel.splashDurationMs.value.toFloat(),
                    onValueChange = { viewModel.splashDurationMs.value = it.toLong() },
                    valueRange = 1000f..10000f,
                    steps = 8
                )
            }
        }
    }
}

@Composable
private fun BgmSection(
    viewModel: WebToAppViewModel,
    bgmUri: Uri?,
    onSelectBgm: () -> Unit
) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_bgm),
        icon = "🎵"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_bgm),
                description = stringResource(R.string.webtoapp_enable_bgm_desc),
                checked = viewModel.enableBgm.value,
                onCheckedChange = { viewModel.enableBgm.value = it }
            )

            if (viewModel.enableBgm.value) {
                Spacer(modifier = Modifier.height(12.dp))

                // BGM File Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onSelectBgm() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (bgmUri != null) "🎵 Music file selected" else stringResource(R.string.webtoapp_tap_to_select_bgm),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Volume Slider
                Text(
                    text = "${stringResource(R.string.webtoapp_bgm_volume)}: ${(viewModel.bgmVolume.value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = viewModel.bgmVolume.value,
                    onValueChange = { viewModel.bgmVolume.value = it },
                    valueRange = 0f..1f
                )

                SwitchItem(
                    title = stringResource(R.string.webtoapp_bgm_loop),
                    description = stringResource(R.string.webtoapp_bgm_loop_desc),
                    checked = viewModel.bgmLoop.value,
                    onCheckedChange = { viewModel.bgmLoop.value = it }
                )
                SwitchItem(
                    title = stringResource(R.string.webtoapp_bgm_autoplay),
                    description = stringResource(R.string.webtoapp_bgm_autoplay_desc),
                    checked = viewModel.bgmAutoPlay.value,
                    onCheckedChange = { viewModel.bgmAutoPlay.value = it }
                )
            }
        }
    }
}

@Composable
private fun ActivationSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_activation),
        icon = "🔐"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_activation),
                description = stringResource(R.string.webtoapp_enable_activation_desc),
                checked = viewModel.enableActivation.value,
                onCheckedChange = { viewModel.enableActivation.value = it }
            )

            if (viewModel.enableActivation.value) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.activationCode.value,
                    onValueChange = { viewModel.activationCode.value = it },
                    label = { Text(stringResource(R.string.webtoapp_activation_code)) },
                    placeholder = { Text(stringResource(R.string.webtoapp_activation_code_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.activationMessage.value,
                    onValueChange = { viewModel.activationMessage.value = it },
                    label = { Text(stringResource(R.string.webtoapp_activation_message)) },
                    placeholder = { Text(stringResource(R.string.webtoapp_activation_message_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AnnouncementSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_announcement),
        icon = "📢"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_announcement),
                description = stringResource(R.string.webtoapp_enable_announcement_desc),
                checked = viewModel.enableAnnouncement.value,
                onCheckedChange = { viewModel.enableAnnouncement.value = it }
            )

            if (viewModel.enableAnnouncement.value) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = viewModel.announcementTitle.value,
                    onValueChange = { viewModel.announcementTitle.value = it },
                    label = { Text(stringResource(R.string.webtoapp_announcement_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.announcementContent.value,
                    onValueChange = { viewModel.announcementContent.value = it },
                    label = { Text(stringResource(R.string.webtoapp_announcement_content)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.announcementUrl.value,
                    onValueChange = { viewModel.announcementUrl.value = it },
                    label = { Text(stringResource(R.string.webtoapp_announcement_url)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Template Dropdown
                DropdownItem(
                    title = stringResource(R.string.webtoapp_announcement_template),
                    selectedValue = viewModel.announcementTemplate.value.name,
                    options = AnnouncementTemplate.entries.map { it.name },
                    onValueChange = { viewModel.announcementTemplate.value = AnnouncementTemplate.valueOf(it) }
                )

                SwitchItem(
                    title = stringResource(R.string.webtoapp_show_once),
                    description = stringResource(R.string.webtoapp_show_once_desc),
                    checked = viewModel.showAnnouncementOnce.value,
                    onCheckedChange = { viewModel.showAnnouncementOnce.value = it }
                )
            }
        }
    }
}

@Composable
private fun ForcedRunSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_forced),
        icon = "🔒"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_forced),
                description = stringResource(R.string.webtoapp_enable_forced_desc),
                checked = viewModel.enableForcedRun.value,
                onCheckedChange = { viewModel.enableForcedRun.value = it }
            )

            if (viewModel.enableForcedRun.value) {
                SwitchItem(
                    title = stringResource(R.string.webtoapp_block_back),
                    description = stringResource(R.string.webtoapp_block_back_desc),
                    checked = viewModel.blockBackButton.value,
                    onCheckedChange = { viewModel.blockBackButton.value = it }
                )
                SwitchItem(
                    title = stringResource(R.string.webtoapp_block_home),
                    description = stringResource(R.string.webtoapp_block_home_desc),
                    checked = viewModel.blockHomeButton.value,
                    onCheckedChange = { viewModel.blockHomeButton.value = it }
                )
                SwitchItem(
                    title = stringResource(R.string.webtoapp_block_recent),
                    description = stringResource(R.string.webtoapp_block_recent_desc),
                    checked = viewModel.blockRecentApps.value,
                    onCheckedChange = { viewModel.blockRecentApps.value = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stringResource(R.string.webtoapp_forced_duration)}: ${viewModel.forcedRunDurationMinutes.value} min",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.webtoapp_forced_duration_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = viewModel.forcedRunDurationMinutes.value.toFloat(),
                    onValueChange = { viewModel.forcedRunDurationMinutes.value = it.toInt() },
                    valueRange = 0f..120f,
                    steps = 11
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExtensionsSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_extensions),
        icon = "🧩"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.webtoapp_extensions_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.webtoapp_builtin_extensions),
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtensionModule.entries.forEach { module ->
                    FilterChip(
                        selected = module in viewModel.enabledExtensions.value,
                        onClick = { viewModel.toggleExtension(module) },
                        label = { Text(module.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = if (module in viewModel.enabledExtensions.value) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.customJavaScript.value,
                onValueChange = { viewModel.customJavaScript.value = it },
                label = { Text(stringResource(R.string.webtoapp_custom_javascript)) },
                placeholder = { Text(stringResource(R.string.webtoapp_custom_javascript_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.customCss.value,
                onValueChange = { viewModel.customCss.value = it },
                label = { Text(stringResource(R.string.webtoapp_custom_css)) },
                placeholder = { Text(stringResource(R.string.webtoapp_custom_css_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            SwitchItem(
                title = stringResource(R.string.webtoapp_inject_at_start),
                description = stringResource(R.string.webtoapp_inject_at_start_desc),
                checked = viewModel.injectScriptAtStart.value,
                onCheckedChange = { viewModel.injectScriptAtStart.value = it }
            )
        }
    }
}

@Composable
private fun AdvancedSection(viewModel: WebToAppViewModel) {
    ExpandableCard(
        title = stringResource(R.string.webtoapp_section_advanced),
        icon = "⚙️"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_isolation),
                description = stringResource(R.string.webtoapp_enable_isolation_desc),
                checked = viewModel.enableIsolation.value,
                onCheckedChange = { viewModel.enableIsolation.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_encryption),
                description = stringResource(R.string.webtoapp_enable_encryption_desc),
                checked = viewModel.enableEncryption.value,
                onCheckedChange = { viewModel.enableEncryption.value = it }
            )
            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_translate),
                description = stringResource(R.string.webtoapp_enable_translate_desc),
                checked = viewModel.enableAutoTranslate.value,
                onCheckedChange = { viewModel.enableAutoTranslate.value = it }
            )

            if (viewModel.enableAutoTranslate.value) {
                DropdownItem(
                    title = stringResource(R.string.webtoapp_translate_language),
                    selectedValue = viewModel.translateTargetLanguage.value,
                    options = listOf("en", "tr", "de", "fr", "es", "ja", "zh", "ar"),
                    onValueChange = { viewModel.translateTargetLanguage.value = it }
                )
            }

            SwitchItem(
                title = stringResource(R.string.webtoapp_enable_autostart),
                description = stringResource(R.string.webtoapp_enable_autostart_desc),
                checked = viewModel.enableAutoStart.value,
                onCheckedChange = { viewModel.enableAutoStart.value = it }
            )
        }
    }
}

// =====================================================================
// REUSABLE COMPONENTS
// =====================================================================

@Composable
private fun ExpandableCard(
    title: String,
    icon: String,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SwitchItem(
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
private fun DropdownItem(
    title: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
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
                text = selectedValue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Icon(Icons.Default.ExpandMore, contentDescription = null)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
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
                        if (buildState.currentStep.isNotBlank()) {
                            Text(
                                text = buildState.currentStep,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
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
                                .background(MaterialTheme.colorScheme.error, CircleShape),
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
        HeaderCard()
    }
}
