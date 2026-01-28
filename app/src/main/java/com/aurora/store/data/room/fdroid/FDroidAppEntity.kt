/*
 * OmniAPK - F-Droid App Entity
 * Room database entity for caching F-Droid apps
 */

package com.aurora.store.data.room.fdroid

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "fdroid_apps")
@TypeConverters(FDroidConverters::class)
data class FDroidAppEntity(
    @PrimaryKey
    val packageName: String,
    val name: String,
    val summary: String,
    val description: String = "",
    val versionName: String,
    val versionCode: Int,
    val iconUrl: String,
    val downloadUrl: String,
    val license: String = "",
    val webSite: String = "",
    val sourceCode: String = "",
    val categories: List<String> = emptyList(),
    val size: Long = 0,
    val minSdkVersion: Int = 0,
    val lastUpdated: Long = 0,
    val added: Long = 0,
    val suggestedVersionCode: Int = 0,
    val repoName: String = "",
    val repoAddress: String = "",
    val syncTimestamp: Long = System.currentTimeMillis(),
    val authorName: String = "",
    val screenshots: List<String> = emptyList(),
    val antiFeatures: List<String> = emptyList(),
    val whatsNew: String = ""
)

/**
 * Type converters for Room
 */
class FDroidConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }
}
