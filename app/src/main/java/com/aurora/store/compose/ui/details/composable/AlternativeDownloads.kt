/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details.composable

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.aurora.store.R
import com.aurora.store.compose.preview.PreviewTemplate

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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
    ) {
        // APKMirror Button
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { openAPKMirror(context, packageName) }
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
            onClick = { openAPKPure(context, packageName) }
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

private fun openAPKMirror(context: Context, packageName: String) {
    // Direct link to APKMirror search for the package
    val searchUrl = "https://www.apkmirror.com/?post_type=app_release&searchtype=apk&s=$packageName"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
    context.startActivity(intent)
}

private fun openAPKPure(context: Context, packageName: String) {
    // Direct link to APKPure app page
    val appUrl = "https://apkpure.com/search?q=$packageName"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl))
    context.startActivity(intent)
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
