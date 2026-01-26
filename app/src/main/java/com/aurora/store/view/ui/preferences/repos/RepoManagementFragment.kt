package com.aurora.store.view.ui.preferences.repos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aurora.store.compose.theme.AuroraTheme
import com.aurora.store.compose.ui.repos.RepoManagementScreen
import com.aurora.store.viewmodel.RepoManagementViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RepoManagementFragment : Fragment() {

    private val viewModel: RepoManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AuroraTheme {
                    RepoManagementScreen(
                        viewModel = viewModel,
                        onNavigateUp = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }
}
