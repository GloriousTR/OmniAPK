package com.omniapk.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.omniapk.R
import com.omniapk.databinding.FragmentTabbedSectionBinding
import com.omniapk.ui.common.TabContentFragment
import com.omniapk.ui.common.TabPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GamesFragment : Fragment() {

    private var _binding: FragmentTabbedSectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabbedSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
    }

    private fun setupTabs() {
        val tabTitles = listOf(
            getString(R.string.tab_for_you),
            getString(R.string.tab_top_charts),
            getString(R.string.tab_categories)
        )
        
        val fragments = listOf(
            TabContentFragment.newInstance(TabContentFragment.TAB_FOR_YOU, isGame = true),
            TabContentFragment.newInstance(TabContentFragment.TAB_TOP_CHARTS, isGame = true),
            TabContentFragment.newInstance(TabContentFragment.TAB_CATEGORIES, isGame = true)
        )
        
        val adapter = TabPagerAdapter(this, fragments, tabTitles)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
