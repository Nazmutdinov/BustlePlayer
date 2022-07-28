package com.example.bustleplayer.di

import android.content.Context
import com.example.bustleplayer.data.local.BustleDatabase
import com.example.bustleplayer.fragments.DialogFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context) = ExoPlayer.Builder(context).build()

    @Provides
    @Singleton
    fun provideDialogFactory() = DialogFactory()

    @Provides
    @Singleton
    fun provideBottomSheetDialogFragment() = BottomSheetDialogFragment()

    @Provides
    @Singleton
    fun provideBustleDatabase(@ApplicationContext context: Context) = BustleDatabase.getDatabase(context)

    @DispatcherDb
    @Provides
    @Singleton
    fun provideDispatcherDb():CoroutineDispatcher = Dispatchers.IO


    @DispatcherVM
    @Provides
    @Singleton
    fun provideDispatcherVM():CoroutineDispatcher = Dispatchers.Default
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDb

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherVM


