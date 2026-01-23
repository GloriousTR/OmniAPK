package com.omniapk.di

import com.omniapk.data.sources.ApkMirrorProvider
import com.omniapk.data.sources.ApkPureProvider
import com.omniapk.data.sources.FDroidProvider
import com.omniapk.data.sources.PlayStoreProvider
import com.omniapk.data.sources.SourceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IntoSet
    @Singleton
    fun provideApkMirror(provider: ApkMirrorProvider): SourceProvider = provider

    @Provides
    @IntoSet
    @Singleton
    fun provideApkPure(provider: ApkPureProvider): SourceProvider = provider

    @Provides
    @IntoSet
    @Singleton
    fun providePlayStore(provider: PlayStoreProvider): SourceProvider = provider

    @Provides
    @IntoSet
    @Singleton
    fun provideFDroid(provider: FDroidProvider): SourceProvider = provider
}
