package com.example.cataniaunited.di

import android.app.Application
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnDiceRolling
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    @Provides
    @Singleton
    fun provideOnConnectionSuccess(application: Application): OnConnectionSuccess {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnLobbyCreated(application: Application): OnLobbyCreated {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnGameBoardReceived(application: Application): OnGameBoardReceived {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnDiceResult(application: Application): OnDiceResult {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnDiceRolling(application: Application): OnDiceRolling {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnWebSocketError(application: Application): OnWebSocketError {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnWebSocketClosed(application: Application): OnWebSocketClosed {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideOnPlayerResourcesReceived(application: Application): OnPlayerResourcesReceived {
        return application as MainApplication
    }

    @Provides
    @Singleton
    fun provideWebSocketListener(
        onConnectionSuccess: OnConnectionSuccess,
        onLobbyCreated: OnLobbyCreated,
        onGameBoardReceived: OnGameBoardReceived,
        onError: OnWebSocketError,
        onClosed: OnWebSocketClosed,
        onDiceResult: OnDiceResult,
        onDiceRolling: OnDiceRolling,
        onPlayerResourcesReceived: OnPlayerResourcesReceived,
        gameDataHandler: GameDataHandler
    ): WebSocketListenerImpl {
        return WebSocketListenerImpl(
            onConnectionSuccess = onConnectionSuccess,
            onLobbyCreated = onLobbyCreated,
            onGameBoardReceived = onGameBoardReceived,
            onError = onError,
            onClosed = onClosed,
            onDiceResult = onDiceResult,
            onDiceRolling = onDiceRolling,
            gameDataHandler = gameDataHandler,
            onPlayerResourcesReceived = onPlayerResourcesReceived
        )
    }
}