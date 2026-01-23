package com.omniapk.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.omniapk.data.model.FDroidRepo
import com.omniapk.databinding.FragmentRepoManagementBinding

class RepoManagementFragment : DialogFragment() {

    private var _binding: FragmentRepoManagementBinding? = null
    private val binding get() = _binding!!
    
    // In a real app we would use ViewModel sharing, here using static/singleton or simple local list for demo
    private val repos = FDroidRepo.DEFAULT_REPOS.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepoManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvTitle.text = "F-Droid Repos"
        binding.btnClose.setOnClickListener { dismiss() }
        
        val adapter = FDroidRepoAdapter(repos) { repo, enabled ->
             val index = repos.indexOfFirst { it.id == repo.id }
             if (index >= 0) {
                 repos[index] = repo.copy(enabled = enabled)
                 // Save preference logic here
             }
        }
        
        binding.rvRepos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRepos.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
