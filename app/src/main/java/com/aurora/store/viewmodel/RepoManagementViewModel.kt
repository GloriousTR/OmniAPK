package com.aurora.store.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.aurora.store.data.model.FDroidRepo
import com.aurora.store.data.providers.FDroidProvider
import com.aurora.store.data.room.fdroid.FDroidAppDao
import com.aurora.store.data.work.FDroidSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoManagementViewModel @Inject constructor(
    private val provider: FDroidProvider,
    private val fdroidAppDao: FDroidAppDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val repos: StateFlow<List<FDroidRepo>> = provider.repos
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val _appCount = MutableStateFlow(0)
    val appCount: StateFlow<Int> = _appCount.asStateFlow()
    
    init {
        loadSyncInfo()
        observeSyncWork()
    }
    
    private fun loadSyncInfo() {
        viewModelScope.launch {
            _lastSyncTime.value = fdroidAppDao.getLastSyncTime()
            _appCount.value = fdroidAppDao.getAppCount()
        }
    }
    
    private fun observeSyncWork() {
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(FDroidSyncWorker.WORK_NAME)
            .observeForever { workInfos ->
                val workInfo = workInfos?.firstOrNull()
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> _syncState.value = SyncState.Syncing
                    WorkInfo.State.SUCCEEDED -> {
                        _syncState.value = SyncState.Success
                        loadSyncInfo()
                    }
                    WorkInfo.State.FAILED -> _syncState.value = SyncState.Error("Sync failed")
                    else -> _syncState.value = SyncState.Idle
                }
            }
    }
    
    fun startSync() {
        val hasEnabledRepos = repos.value.any { it.enabled }
        if (!hasEnabledRepos) {
            _syncState.value = SyncState.Error("No enabled repositories")
            return
        }
        
        _syncState.value = SyncState.Syncing
        
        val syncRequest = OneTimeWorkRequestBuilder<FDroidSyncWorker>()
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                FDroidSyncWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
    }
    
    fun addRepo(name: String, address: String) {
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

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
}
