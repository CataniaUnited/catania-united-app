package com.example.cataniaunited.di

import android.content.Context
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.ws.provider.WebSocketErrorProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Singleton
    @Binds
    abstract fun bindWebSocketErrorProvider(
        application: MainApplication
    ): WebSocketErrorProvider

    companion object {
        @Provides
        @Singleton
        fun provideMainApplication(@ApplicationContext context: Context): MainApplication {
            // Cast the ApplicationContext (which is the Application instance) to your specific Application class
            return context as MainApplication
        }
    }
}