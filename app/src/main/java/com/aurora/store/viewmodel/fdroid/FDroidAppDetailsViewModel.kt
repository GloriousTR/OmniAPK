/*
 * OmniAPK - F-Droid App Details ViewModel
 * ViewModel for F-Droid app details screen
 */

package com.aurora.store.viewmodel.fdroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.store.data.room.fdroid.FDroidAppDao
import com.aurora.store.data.room.fdroid.FDroidAppEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FDroidAppDetailsViewModel @Inject constructor(
    private val fdroidAppDao: FDroidAppDao
) : ViewModel() {

    private val _app = MutableStateFlow<FDroidAppEntity?>(null)
    val app: StateFlow<FDroidAppEntity?> = _app.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadApp(packageName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _app.value = fdroidAppDao.getAppByPackageName(packageName)
            _isLoading.value = false
        }
    }
}
