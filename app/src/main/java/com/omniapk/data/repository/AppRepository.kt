package com.omniapk.data.repository

import com.omniapk.data.model.AppInfo
import com.omniapk.utils.PackageManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val packageManagerHelper: PackageManagerHelper
) {
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        // Filter out system apps by default or provide a toggle?
        // For now, let's just return all non-system apps or maybe all apps and let UI filter.
        // Let's return all for now and let the ViewModel filter if needed.
        packageManagerHelper.getInstalledApps().filter { !it.isSystemApp }
    }
}
