package com.omniapk.data.repository

import com.omniapk.data.model.AppInfo
import com.omniapk.data.sources.SourceProvider
import javax.inject.Inject

class SourcesRepository @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards SourceProvider> 
) {
    suspend fun searchApps(query: String): List<AppInfo> {
        val allResults = mutableListOf<AppInfo>()
        sources.forEach { source ->
            try {
                allResults.addAll(source.searchApp(query))
            } catch (e: Exception) {
                // Log error but continue with other sources
                e.printStackTrace()
            }
        }
        return allResults
    }
    
    suspend fun checkUpdate(packageName: String, currentVersionCode: Long, currentVersionName: String): AppInfo? {
        // Strategy: Check all sources, return the one with the highest version
        var bestUpdate: AppInfo? = null

        sources.forEach { source ->
             val update = source.checkUpdate(packageName, currentVersionCode, currentVersionName)
             if (update != null) {
                 // Compare versions
                 val isNewer = com.omniapk.utils.VersionUtil.compareVersions(update.versionName, currentVersionName) > 0
                 
                 // If newer, and (no bestUpdate yet OR update is newer than bestUpdate)
                 if (isNewer) {
                     if (bestUpdate == null || com.omniapk.utils.VersionUtil.compareVersions(update.versionName, bestUpdate!!.versionName) > 0) {
                         bestUpdate = update
                     }
                 }
             }
        }
        return bestUpdate
    }
}
