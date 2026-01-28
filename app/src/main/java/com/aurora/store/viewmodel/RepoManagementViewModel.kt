package com.aurora.store.viewmodel

import android.content.Context
import androidx.lifecycle.Observer
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
    
    private val workInfoLiveData = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(FDroidSyncWorker.WORK_NAME)
    
    private val workInfoObserver = Observer<List<WorkInfo>> { workInfos ->
        val workInfo = workInfos?.firstOrNull()
        val newState = when (workInfo?.state) {
            WorkInfo.State.RUNNING -> {
                // Use the global sync status which has detailed progress info
                FDroidSyncStatus.syncState.value.let { currentState ->
                    if (currentState is SyncState.Syncing) currentState else SyncState.Syncing()
                }
            }
            WorkInfo.State.SUCCEEDED -> {
                loadSyncInfo()
                SyncState.Success
            }
            WorkInfo.State.FAILED -> SyncState.Error("Sync failed")
            else -> SyncState.Idle
        }
        _syncState.value = newState
        // Don't update global status here as it's managed by FDroidSyncWorker with detailed progress
        if (newState !is SyncState.Syncing) {
            FDroidSyncStatus.updateState(newState)
        }
    }
    
    init {
        loadSyncInfo()
        observeSyncWork()
        observeGlobalSyncStatus()
    }
    
    private fun loadSyncInfo() {
        viewModelScope.launch {
            try {
                _lastSyncTime.value = fdroidAppDao.getLastSyncTime()
                _appCount.value = fdroidAppDao.getAppCount()
                FDroidSyncStatus.updateAppCount(_appCount.value)
                _lastSyncTime.value?.let { FDroidSyncStatus.updateLastSyncTime(it) }
            } catch (e: Exception) {
                // Log database errors for debugging
                android.util.Log.e("RepoManagementViewModel", "Failed to load sync info", e)
                _lastSyncTime.value = null
                _appCount.value = 0
            }
        }
    }
    
    private fun observeSyncWork() {
        workInfoLiveData.observeForever(workInfoObserver)
    }
    
    private fun observeGlobalSyncStatus() {
        // Collect from global sync status to get detailed progress
        viewModelScope.launch {
            FDroidSyncStatus.syncState.collect { globalState ->
                if (globalState is SyncState.Syncing) {
                    _syncState.value = globalState
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Remove observer to prevent memory leaks
        workInfoLiveData.removeObserver(workInfoObserver)
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
    data class Syncing(
        val currentRepo: String = "",
        val currentRepoIndex: Int = 0,
        val totalRepos: Int = 0,
        val appsProcessed: Int = 0
    ) : SyncState()
    data object Success : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Global sync status object that can be observed from any screen
 * to show sync status in the status bar.
 * Thread-safe: All updates are synchronized to ensure consistency.
 */
object FDroidSyncStatus {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val _appCount = MutableStateFlow(0)
    val appCount: StateFlow<Int> = _appCount.asStateFlow()
    
    private val lock = Any()
    
    fun updateState(state: SyncState) {
        synchronized(lock) {
            _syncState.value = state
        }
    }
    
    fun updateLastSyncTime(time: Long) {
        synchronized(lock) {
            _lastSyncTime.value = time
        }
    }
    
    fun updateAppCount(count: Int) {
        synchronized(lock) {
            _appCount.value = count
        }
    }
    
    /**
     * Update the syncing state with repo progress details.
     */
    fun updateSyncingProgress(
        currentRepo: String,
        currentRepoIndex: Int,
        totalRepos: Int,
        appsProcessed: Int = 0
    ) {
        synchronized(lock) {
            _syncState.value = SyncState.Syncing(
                currentRepo = currentRepo,
                currentRepoIndex = currentRepoIndex,
                totalRepos = totalRepos,
                appsProcessed = appsProcessed
            )
        }
    }
    
    /**
     * Atomically update all sync status fields at once.
     * Use this when updating multiple fields together to ensure consistency.
     */
    fun updateSyncResult(state: SyncState, appCount: Int, lastSyncTime: Long) {
        synchronized(lock) {
            _appCount.value = appCount
            _lastSyncTime.value = lastSyncTime
            _syncState.value = state
        }
    }
}
