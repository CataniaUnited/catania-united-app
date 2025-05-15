package com.example.cataniaunited.di

import com.example.cataniaunited.logic.game.GameDataHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameDataModule {
    @Provides
    @Singleton
    fun provideGameDataHandler(): GameDataHandler {
        return GameDataHandler()
    }
}