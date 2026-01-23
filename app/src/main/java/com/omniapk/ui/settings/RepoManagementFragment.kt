package com.omniapk.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.omniapk.data.model.FDroidRepo
import com.omniapk.databinding.FragmentRepoManagementBinding

class RepoManagementFragment : DialogFragment() {

    private var _binding: FragmentRepoManagementBinding? = null
    private val binding get() = _binding!!
    
    private val repos = FDroidRepo.DEFAULT_REPOS.toMutableList()
    private lateinit var adapter: FDroidRepoAdapter

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
        
        adapter = FDroidRepoAdapter(repos) { repo, enabled ->
             val index = repos.indexOfFirst { it.id == repo.id }
             if (index >= 0) {
                 repos[index] = repo.copy(enabled = enabled)
                 // TODO: Save to SharedPreferences
             }
        }
        
        binding.rvRepos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRepos.adapter = adapter
        
        // Add Repo button
        binding.btnAddRepo.setOnClickListener {
            showAddRepoDialog()
        }
    }
    
    private fun showAddRepoDialog() {
        val context = requireContext()
        
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
        }
        
        val nameInput = EditText(context).apply {
            hint = "Repo Adı (örn: IzzyOnDroid)"
        }
        
        val urlInput = EditText(context).apply {
            hint = "Repo URL (örn: https://apt.izzysoft.de/fdroid/repo)"
        }
        
        layout.addView(nameInput)
        layout.addView(urlInput)
        
        AlertDialog.Builder(context)
            .setTitle("F-Droid Repo Ekle")
            .setView(layout)
            .setPositiveButton("Ekle") { _, _ ->
                val name = nameInput.text.toString().trim()
                val url = urlInput.text.toString().trim()
                
                if (name.isNotEmpty() && url.isNotEmpty()) {
                    val newRepo = FDroidRepo(
                        id = "custom_${System.currentTimeMillis()}",
                        name = name,
                        address = url,
                        description = "Custom repository",
                        enabled = true
                    )
                    repos.add(newRepo)
                    adapter.notifyItemInserted(repos.size - 1)
                    Toast.makeText(context, "$name eklendi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
