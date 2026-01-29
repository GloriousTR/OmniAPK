/*
 * OmniAPK - F-Droid App DAO
 * Data Access Object for F-Droid apps cache
 */

package com.aurora.store.data.room.fdroid

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FDroidAppDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<FDroidAppEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: FDroidAppEntity)
    
    @Query("SELECT * FROM fdroid_apps ORDER BY lastUpdated DESC")
    fun getAllApps(): Flow<List<FDroidAppEntity>>
    
    @Query("SELECT * FROM fdroid_apps ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getRecentApps(limit: Int = 30): List<FDroidAppEntity>
    
    @Query("SELECT * FROM fdroid_apps ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getTopApps(limit: Int = 50): List<FDroidAppEntity>
    
    @Query("SELECT * FROM fdroid_apps WHERE categories LIKE '%' || :category || '%'")
    suspend fun getAppsByCategory(category: String): List<FDroidAppEntity>
    
    @Query("SELECT DISTINCT categories FROM fdroid_apps")
    suspend fun getAllCategoriesRaw(): List<String>
    
    @Query("SELECT * FROM fdroid_apps WHERE name LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    suspend fun searchApps(query: String): List<FDroidAppEntity>
    
    @Query("SELECT * FROM fdroid_apps WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): FDroidAppEntity?
    
    @Query("SELECT COUNT(*) FROM fdroid_apps")
    suspend fun getAppCount(): Int
    
    @Query("SELECT MAX(syncTimestamp) FROM fdroid_apps")
    suspend fun getLastSyncTime(): Long?
    
    @Query("DELETE FROM fdroid_apps")
    suspend fun deleteAllApps()
    
    @Query("DELETE FROM fdroid_apps WHERE repoAddress = :repoAddress")
    suspend fun deleteAppsByRepo(repoAddress: String)

    // Version Management
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: FDroidVersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<FDroidVersionEntity>)

    @Query("SELECT * FROM fdroid_versions WHERE packageName = :packageName ORDER BY versionCode DESC")
    suspend fun getAppVersions(packageName: String): List<FDroidVersionEntity>
    
    @Query("DELETE FROM fdroid_versions WHERE packageName = :packageName")
    suspend fun deleteVersionsByPackage(packageName: String)
}
