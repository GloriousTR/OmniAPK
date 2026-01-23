package com.omniapk.ui.opensource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omniapk.data.model.AppInfo
import com.omniapk.data.sources.FDroidProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenSourceViewModel @Inject constructor(
    private val fdroidProvider: FDroidProvider
) : ViewModel() {

    private val _apps = MutableLiveData<List<AppInfo>>()
    val apps: LiveData<List<AppInfo>> = _apps

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadPopularApps()
    }

    private fun loadPopularApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get popular apps from F-Droid homepage
                val popularApps = fdroidProvider.getPopularApps()
                
                if (popularApps.isNotEmpty()) {
                    _apps.value = popularApps
                } else {
                    // Fallback: search for known popular apps
                    val fallbackApps = mutableListOf<AppInfo>()
                    val queries = listOf("firefox", "signal", "vlc", "termux", "newpipe")
                    
                    for (query in queries) {
                        val results = fdroidProvider.searchApp(query)
                        if (results.isNotEmpty()) {
                            fallbackApps.add(results.first())
                        }
                    }
                    _apps.value = fallbackApps
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _apps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadPopularApps()
    }
    
    fun searchFDroid(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = fdroidProvider.searchApp(query)
                _apps.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _apps.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
