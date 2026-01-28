package com.aurora.store.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aurora.store.data.room.download.Download
import com.aurora.store.data.room.download.DownloadConverter
import com.aurora.store.data.room.download.DownloadDao
import com.aurora.store.data.room.favourite.Favourite
import com.aurora.store.data.room.favourite.FavouriteDao
import com.aurora.store.data.room.fdroid.FDroidAppDao
import com.aurora.store.data.room.fdroid.FDroidAppEntity
import com.aurora.store.data.room.fdroid.FDroidConverters
import com.aurora.store.data.room.fdroid.FDroidVersionEntity
import com.aurora.store.data.room.update.Update
import com.aurora.store.data.room.update.UpdateDao

@Database(
    entities = [Download::class, Favourite::class, Update::class, FDroidAppEntity::class, FDroidVersionEntity::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(DownloadConverter::class, FDroidConverters::class)
abstract class AuroraDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun updateDao(): UpdateDao
    abstract fun fdroidAppDao(): FDroidAppDao
}
