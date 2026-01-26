package com.aurora.store.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.data.providers.FDroidProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoManagementViewModel @Inject constructor(
    private val provider: FDroidProvider
) : ViewModel() {
    
    val repos: StateFlow<List<FDroidRepo>> = provider.repos
    
    fun addRepo(name: String, address: String) {
        // Basic validation/generation
        val id = address.hashCode().toString()
        val repo = FDroidRepo(id = id, name = name, address = address, enabled = true)
        provider.addRepo(repo)
    }
    
    fun removeRepo(repo: FDroidRepo) {
        provider.removeRepo(repo)
    }
    
    fun toggleRepo(repo: FDroidRepo, enabled: Boolean) {
        provider.toggleRepo(repo, enabled)
    }
    
    fun resetDefaults() {
        provider.resetToDefaults()
    }
}
