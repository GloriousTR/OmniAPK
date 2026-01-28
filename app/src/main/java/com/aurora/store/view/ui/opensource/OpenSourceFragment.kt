/*
 * OmniAPK - Open Source Container Fragment
 * Copyright (C) 2024
 *
 * Container fragment with ViewPager for F-Droid apps
 * Similar to AppsContainerFragment structure
 */

package com.aurora.store.view.ui.opensource

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aurora.extensions.navigate
import com.aurora.store.MobileNavigationDirections
import com.aurora.store.R
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.databinding.FragmentOpenSourceBinding
import com.aurora.store.view.ui.commons.BaseFragment
import com.aurora.store.viewmodel.FDroidSyncStatus
import com.aurora.store.viewmodel.SyncState
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class OpenSourceFragment : BaseFragment<FragmentOpenSourceBinding>() {

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

        // ViewPager with 3 tabs
        binding.pager.adapter = OpenSourcePagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle
        )

        binding.pager.isUserInputEnabled = false // Disable viewpager scroll to avoid scroll conflicts

        val tabTitles = listOf(
            getString(R.string.tab_for_you),
            getString(R.string.tab_top_charts),
            getString(R.string.tab_categories)
        )

        TabLayoutMediator(
            binding.tabLayout,
            binding.pager,
            true
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }.attach()

        binding.searchFab.setOnClickListener {
            requireContext().navigate(Screen.Search)
        }

        // Setup sync status bar dismiss button
        binding.syncDismissBtn.setOnClickListener {
            binding.syncStatusBar.visibility = View.GONE
        }

        // Observe sync status
        observeSyncStatus()
    }

    private fun observeSyncStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    FDroidSyncStatus.syncState.collectLatest { state ->
                        updateSyncStatusBar(state)
                    }
                }
            }
        }
    }

    private fun updateSyncStatusBar(state: SyncState) {
        when (state) {
            is SyncState.Syncing -> {
                binding.syncStatusBar.visibility = View.VISIBLE
                binding.syncProgress.visibility = View.VISIBLE
                binding.syncIcon.visibility = View.GONE
                binding.syncDismissBtn.visibility = View.GONE
                
                // Show detailed progress with repo name
                val statusText = if (state.currentRepo.isNotEmpty()) {
                    getString(R.string.fdroid_syncing_repo_status, 
                        state.currentRepo, 
                        state.currentRepoIndex, 
                        state.totalRepos)
                } else {
                    getString(R.string.fdroid_syncing_status)
                }
                binding.syncStatusText.text = statusText
            }
            is SyncState.Success -> {
                binding.syncStatusBar.visibility = View.VISIBLE
                binding.syncProgress.visibility = View.GONE
                binding.syncIcon.visibility = View.VISIBLE
                binding.syncIcon.setImageResource(R.drawable.ic_check)
                binding.syncDismissBtn.visibility = View.VISIBLE
                val appCount = FDroidSyncStatus.appCount.value
                val lastSync = FDroidSyncStatus.lastSyncTime.value?.let { formatTime(it) } ?: ""
                binding.syncStatusText.text = getString(R.string.fdroid_sync_success_status, appCount, lastSync)
            }
            is SyncState.Error -> {
                binding.syncStatusBar.visibility = View.VISIBLE
                binding.syncProgress.visibility = View.GONE
                binding.syncIcon.visibility = View.VISIBLE
                binding.syncIcon.setImageResource(R.drawable.ic_cancel)
                binding.syncDismissBtn.visibility = View.VISIBLE
                binding.syncStatusText.text = getString(R.string.fdroid_sync_error_status, state.message)
            }
            is SyncState.Idle -> {
                // Hide status bar when idle
                binding.syncStatusBar.visibility = View.GONE
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        return android.text.format.DateFormat.getTimeFormat(requireContext()).format(Date(timestamp))
    }

    override fun onDestroyView() {
        binding.pager.adapter = null
        super.onDestroyView()
    }

    internal class OpenSourcePagerAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        private val tabFragments = listOf(
            FDroidForYouFragment(),
            FDroidTopAppsFragment(),
            FDroidCategoriesFragment()
        )

        override fun createFragment(position: Int): Fragment = tabFragments[position]

        override fun getItemCount(): Int = tabFragments.size
    }
}
