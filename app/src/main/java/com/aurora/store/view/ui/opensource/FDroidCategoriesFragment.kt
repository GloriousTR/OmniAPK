/*
 * OmniAPK - F-Droid Categories Fragment
 * Copyright (C) 2024
 *
 * Shows F-Droid app categories
 */

package com.aurora.store.view.ui.opensource

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurora.store.R
import com.aurora.store.data.providers.FDroidApiProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FDroidCategoriesFragment : Fragment() {

    @Inject
    lateinit var fdroidApiProvider: FDroidApiProvider

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressView: View
    private lateinit var emptyView: View
    private val adapter = FDroidCategoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fdroid_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        progressView = view.findViewById(R.id.progress_view)
        emptyView = view.findViewById(R.id.empty_view)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        loadCategories()
    }

    private fun loadCategories() {
        progressView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val categories = fdroidApiProvider.getCategories()
                updateUI(categories)
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun updateUI(categories: List<String>) {
        progressView.visibility = View.GONE
        if (categories.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.submitList(categories)
        }
    }
}
