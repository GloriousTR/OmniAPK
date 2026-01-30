/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.view.ui.webtoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.aurora.store.compose.theme.AuroraTheme
import com.aurora.store.compose.ui.webtoapp.WebToAppScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment wrapper for WebToApp Compose screen.
 * This allows WebToApp to be used in the bottom navigation bar.
 */
@AndroidEntryPoint
class WebToAppFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AuroraTheme {
                    WebToAppScreen(
                        onNavigateUp = null // Top-level destination, no back navigation
                    )
                }
            }
        }
    }
}
