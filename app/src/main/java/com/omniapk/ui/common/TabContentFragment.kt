package com.omniapk.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omniapk.databinding.FragmentTabContentBinding
import com.omniapk.ui.home.AppsAdapter
import com.omniapk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Generic tab content fragment that displays a list of apps.
 * Used for "Sizin için", "Üst sıralar", and "Kategoriler" tabs.
 */
@AndroidEntryPoint
class TabContentFragment : Fragment() {

    private var _binding: FragmentTabContentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = AppsAdapter()
    
    private var tabType: Int = TAB_FOR_YOU
    private var isGameTab: Boolean = false

    companion object {
        const val TAB_FOR_YOU = 0
        const val TAB_TOP_CHARTS = 1
        const val TAB_CATEGORIES = 2
        
        private const val ARG_TAB_TYPE = "tab_type"
        private const val ARG_IS_GAME = "is_game"
        
        fun newInstance(tabType: Int, isGame: Boolean = false): TabContentFragment {
            return TabContentFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_TYPE, tabType)
                    putBoolean(ARG_IS_GAME, isGame)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tabType = it.getInt(ARG_TAB_TYPE, TAB_FOR_YOU)
            isGameTab = it.getBoolean(ARG_IS_GAME, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.installedApps.observe(viewLifecycleOwner) { apps ->
            // Filter based on isGame
            val filteredApps = if (isGameTab) {
                apps.filter { it.isGame }
            } else {
                apps.filter { !it.isGame }
            }
            
            // Further filter based on tab type
            val displayApps = when (tabType) {
                TAB_FOR_YOU -> filteredApps.shuffled().take(20) // Random selection for "Sizin için"
                TAB_TOP_CHARTS -> filteredApps.sortedByDescending { it.name.length }.take(20) // Placeholder sorting
                TAB_CATEGORIES -> filteredApps // Show all for categories
                else -> filteredApps
            }
            
            adapter.submitList(displayApps)
            binding.tvEmpty.visibility = if (displayApps.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (displayApps.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
