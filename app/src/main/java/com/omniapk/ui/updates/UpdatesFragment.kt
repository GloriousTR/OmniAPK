package com.omniapk.ui.updates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.omniapk.databinding.FragmentUpdatesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdatesFragment : Fragment() {

    private var _binding: FragmentUpdatesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // For now, just show empty state
        // Future: Check for updates and show list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
