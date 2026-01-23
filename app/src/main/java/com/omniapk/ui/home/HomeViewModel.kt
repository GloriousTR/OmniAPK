package com.omniapk.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omniapk.data.model.AppCategory
import com.omniapk.data.model.AppInfo
import com.omniapk.data.model.FeaturedSection
import com.omniapk.data.model.TopChart
import com.omniapk.data.repository.AppDiscoveryRepository
import com.omniapk.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val appDiscoveryRepository: AppDiscoveryRepository
) : ViewModel() {

    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps

    private val _featuredSections = MutableLiveData<List<FeaturedSection>>()
    val featuredSections: LiveData<List<FeaturedSection>> = _featuredSections
    
    private val _topCharts = MutableLiveData<TopChart>()
    val topCharts: LiveData<TopChart> = _topCharts
    
    private val _categories = MutableLiveData<List<AppCategory>>()
    val categories: LiveData<List<AppCategory>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        // Default load, though fragments request specific data
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _installedApps.value = appRepository.getInstalledApps()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFeaturedSections(isGame: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _featuredSections.value = appDiscoveryRepository.getFeaturedSections(isGame)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadTopCharts(filter: String, isGame: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _topCharts.value = appDiscoveryRepository.getTopCharts(filter, isGame)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCategories(isGame: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _categories.value = appDiscoveryRepository.getCategories(isGame)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
