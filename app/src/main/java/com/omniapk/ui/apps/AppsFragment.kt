package com.omniapk.ui.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omniapk.databinding.FragmentHomeBinding
import com.omniapk.ui.home.AppsAdapter
import com.omniapk.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppsFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = AppsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvHeader.text = getString(com.omniapk.R.string.nav_apps)
        
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.installedApps.observe(viewLifecycleOwner) { apps ->
            // Filter to show only non-game apps using the isGame property
            val nonGameApps = apps.filter { !it.isGame }
            adapter.submitList(nonGameApps)
            binding.tvAppCount.text = getString(com.omniapk.R.string.app_count, nonGameApps.size)
            binding.tvEmpty.visibility = if (nonGameApps.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (nonGameApps.isEmpty()) View.GONE else View.VISIBLE
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
