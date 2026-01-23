package com.omniapk.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.omniapk.data.model.Categories
import com.omniapk.databinding.FragmentTabContentBinding
import com.omniapk.ui.home.AppsAdapter
import com.omniapk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Generic tab content fragment that displays different content based on tab type.
 * Used for "Sizin için" (Featured), "Üst sıralar" (Top Charts), and "Kategoriler" (Categories).
 */
@AndroidEntryPoint
class TabContentFragment : Fragment() {

    private var _binding: FragmentTabContentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    
    private var tabType: Int = TAB_FOR_YOU
    private var isGameTab: Boolean = false
    private var activeFilter: String = "En iyi ücretsiz"

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
        setupFilters()
        loadData()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // Set adapter based on tab type
        when (tabType) {
            TAB_FOR_YOU -> {
                binding.recyclerView.adapter = FeaturedSectionAdapter()
            }
            TAB_TOP_CHARTS -> {
                binding.recyclerView.adapter = AppsAdapter { app ->
                     Toast.makeText(requireContext(), "${app.name} clicked", Toast.LENGTH_SHORT).show()
                }
            }
            TAB_CATEGORIES -> {
                binding.recyclerView.adapter = CategoriesAdapter(emptyList()) { category ->
                     Toast.makeText(requireContext(), "${category.name} clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupFilters() {
        if (tabType == TAB_TOP_CHARTS) {
            binding.scrollFilters.visibility = View.VISIBLE
            
            // Add chips
            Categories.TOP_CHART_FILTERS.forEachIndexed { index, filter ->
                val chip = Chip(requireContext()).apply {
                    text = filter
                    isCheckable = true
                    isChecked = index == 0
                    setOnClickListener {
                        if (activeFilter != filter) {
                            activeFilter = filter
                            viewModel.loadTopCharts(filter, isGameTab)
                        }
                    }
                }
                binding.chipGroupFilters.addView(chip)
            }
        } else {
            binding.scrollFilters.visibility = View.GONE
        }
    }
    
    private fun loadData() {
        when (tabType) {
            TAB_FOR_YOU -> viewModel.loadFeaturedSections(isGameTab)
            TAB_TOP_CHARTS -> viewModel.loadTopCharts(activeFilter, isGameTab)
            TAB_CATEGORIES -> viewModel.loadCategories(isGameTab)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        when (tabType) {
            TAB_FOR_YOU -> {
                viewModel.featuredSections.observe(viewLifecycleOwner) { sections ->
                    (binding.recyclerView.adapter as? FeaturedSectionAdapter)?.submitList(sections)
                    updateEmptyState(sections.isEmpty())
                }
            }
            TAB_TOP_CHARTS -> {
                viewModel.topCharts.observe(viewLifecycleOwner) { chart ->
                    (binding.recyclerView.adapter as? AppsAdapter)?.submitList(chart.apps)
                    updateEmptyState(chart.apps.isEmpty())
                }
            }
            TAB_CATEGORIES -> {
                viewModel.categories.observe(viewLifecycleOwner) { categories ->
                    (binding.recyclerView.adapter as? CategoriesAdapter)?.submitList(categories)
                    updateEmptyState(categories.isEmpty())
                }
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
