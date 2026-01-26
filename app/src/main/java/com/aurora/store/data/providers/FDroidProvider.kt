package com.aurora.store.data.providers

import android.content.Context
import android.content.SharedPreferences
import com.aurora.store.data.model.FDroidRepo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FDroidProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fdroid_repos", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _repos = MutableStateFlow<List<FDroidRepo>>(emptyList())
    val repos: StateFlow<List<FDroidRepo>> = _repos.asStateFlow()
    
    init {
        loadRepos()
    }
    
    private fun loadRepos() {
        val json = prefs.getString("repos_list", null)
        if (json == null) {
            // First run, load defaults
            _repos.value = FDroidRepo.DEFAULT_REPOS
            saveRepos()
        } else {
            val type = object : TypeToken<List<FDroidRepo>>() {}.type
            _repos.value = gson.fromJson(json, type)
        }
    }
    
    private fun saveRepos() {
        val json = gson.toJson(_repos.value)
        prefs.edit().putString("repos_list", json).apply()
    }
    
    fun addRepo(repo: FDroidRepo) {
        val current = _repos.value.toMutableList()
        if (current.none { it.address == repo.address }) {
            current.add(repo)
            _repos.value = current
            saveRepos()
        }
    }
    
    fun removeRepo(repo: FDroidRepo) {
        val current = _repos.value.toMutableList()
        current.removeIf { it.id == repo.id }
        _repos.value = current
        saveRepos()
    }
    
    fun toggleRepo(repo: FDroidRepo, enabled: Boolean) {
        val current = _repos.value.map { 
            if (it.id == repo.id) it.copy(enabled = enabled) else it
        }
        _repos.value = current
        saveRepos()
    }
    
    fun resetToDefaults() {
        _repos.value = FDroidRepo.DEFAULT_REPOS
        saveRepos()
    }
}
