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
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aurora.extensions.navigate
import com.aurora.store.MobileNavigationDirections
import com.aurora.store.R
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.databinding.FragmentAppsGamesBinding
import com.aurora.store.view.ui.commons.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpenSourceFragment : BaseFragment<FragmentAppsGamesBinding>() {

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
