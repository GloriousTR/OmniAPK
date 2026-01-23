package com.omniapk.ui.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(
    fragment: Fragment,
    private val fragments: List<Fragment>,
    private val titles: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
    
    fun getTabTitle(position: Int): String = titles[position]
}
