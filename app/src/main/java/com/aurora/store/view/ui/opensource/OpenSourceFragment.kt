/*
 * OmniAPK
 * Copyright (C) 2024
 *
 * Open-Source tab for F-Droid apps
 */

package com.aurora.store.view.ui.opensource

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurora.extensions.navigate
import com.aurora.store.MobileNavigationDirections
import com.aurora.store.R
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.databinding.FragmentOpenSourceBinding
import com.aurora.store.view.ui.commons.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OpenSourceFragment : BaseFragment<FragmentOpenSourceBinding>() {

    private val repoAdapter by lazy { RepoListAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adjust FAB margins for edgeToEdge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchFab) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            binding.searchFab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + resources.getDimensionPixelSize(R.dimen.margin_large)
            }
            WindowInsetsCompat.CONSUMED
        }

        // Toolbar
        binding.toolbar.apply {
            title = getString(R.string.title_open_source)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_download_manager -> {
                        requireContext().navigate(Screen.Downloads)
                    }
                    R.id.menu_more -> {
                        findNavController().navigate(
                            MobileNavigationDirections.actionGlobalMoreDialogFragment()
                        )
                    }
                }
                true
            }
        }

        // Setup RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = repoAdapter
        }

        // Load repos
        loadRepos()

        // Search FAB
        binding.searchFab.setOnClickListener {
            requireContext().navigate(Screen.Search)
        }
    }

    private fun loadRepos() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get enabled repos
            val enabledRepos = FDroidRepo.DEFAULT_REPOS.filter { it.enabled }
            repoAdapter.submitList(enabledRepos)
            
            // Update empty state
            if (enabledRepos.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        super.onDestroyView()
    }
}
