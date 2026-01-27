/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details.composable

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.aurora.gplayapi.data.models.App
import com.aurora.store.R
import com.aurora.store.compose.preview.PreviewTemplate
import com.aurora.store.data.providers.APKMirrorProvider
import com.aurora.store.data.providers.APKPureProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Composable to display alternative download options (APKMirror, APKPure)
 * @param packageName The package name of the app
 * @param appName The display name of the app
 */
@Composable
fun AlternativeDownloads(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // APKMirror Button
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                coroutineScope.launch {
                    openAPKMirror(context, packageName, appName)
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
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
                coroutineScope.launch {
                    openAPKPure(context, packageName, appName)
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
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
}

private suspend fun openAPKMirror(context: Context, packageName: String, appName: String) {
    withContext(Dispatchers.IO) {
        try {
            // Try to get download URL from APKMirror
            val provider = APKMirrorProvider()
            val versions = provider.getAppVersions(packageName, appName)
            
            if (versions.isNotEmpty()) {
                // Open the first available version's download page
                val downloadUrl = versions.first().downloadUrl
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                }
            } else {
                // Fallback to search page
                val searchUrl = "https://www.apkmirror.com/?post_type=app_release&searchtype=apk&s=$packageName"
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            // Fallback to direct search
            val searchUrl = "https://www.apkmirror.com/?post_type=app_release&searchtype=apk&s=$packageName"
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                context.startActivity(intent)
            }
        }
    }
}

private suspend fun openAPKPure(context: Context, packageName: String, appName: String) {
    withContext(Dispatchers.IO) {
        try {
            // Try to get download URL from APKPure
            val provider = APKPureProvider()
            val versions = provider.getAppVersions(packageName, appName)
            
            if (versions.isNotEmpty()) {
                // Open the first available version's download page
                val downloadUrl = versions.first().downloadUrl
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                }
            } else {
                // Fallback to search page
                val searchUrl = "https://apkpure.com/search?q=$packageName"
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            // Fallback to direct app page
            val appUrl = "https://apkpure.com/$packageName"
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl))
                context.startActivity(intent)
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
