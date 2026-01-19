package com.omniapk.data.sources

import com.omniapk.data.model.AppInfo

interface SourceProvider {
    val name: String
    
    suspend fun searchApp(query: String): List<AppInfo>
    
    suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo?
    
    // Potentially needed for downloading
    suspend fun getDownloadUrl(appInfo: AppInfo): String?
}
