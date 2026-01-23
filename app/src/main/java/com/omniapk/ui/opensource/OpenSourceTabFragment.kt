package com.omniapk.ui.opensource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omniapk.databinding.FragmentTabContentBinding
import com.omniapk.ui.home.AppsAdapter
import dagger.hilt.android.AndroidEntryPoint

/**
 * Tab content fragment for Open Source section.
 * Shows F-Droid apps for each tab.
 */
@AndroidEntryPoint
class OpenSourceTabFragment : Fragment() {

    private var _binding: FragmentTabContentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: OpenSourceViewModel by viewModels()
    private val adapter = AppsAdapter()
    
    private var tabType: Int = TAB_FOR_YOU

    companion object {
        const val TAB_FOR_YOU = 0
        const val TAB_TOP_CHARTS = 1
        const val TAB_CATEGORIES = 2
        
        private const val ARG_TAB_TYPE = "tab_type"
        
        fun newInstance(tabType: Int): OpenSourceTabFragment {
            return OpenSourceTabFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_TYPE, tabType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tabType = it.getInt(ARG_TAB_TYPE, TAB_FOR_YOU)
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
        viewModel.apps.observe(viewLifecycleOwner) { apps ->
            val displayApps = when (tabType) {
                TAB_FOR_YOU -> apps.shuffled().take(10)
                TAB_TOP_CHARTS -> apps.take(10)
                TAB_CATEGORIES -> apps
                else -> apps
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
