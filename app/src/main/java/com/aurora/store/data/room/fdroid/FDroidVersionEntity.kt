package com.aurora.store.data.room.fdroid

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "fdroid_versions",
    primaryKeys = ["packageName", "versionCode"],
    foreignKeys = [
        ForeignKey(
            entity = FDroidAppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packageName"])]
)
data class FDroidVersionEntity(
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val size: Long,
    val downloadUrl: String,
    val added: Long,
    val minSdkVersion: Int = 0,
    val targetSdkVersion: Int = 0,
    val hash: String = "",
    val hashType: String = "",
    val repoName: String = "",
    val releaseNotes: String = "" // What's New for this specific version
)
