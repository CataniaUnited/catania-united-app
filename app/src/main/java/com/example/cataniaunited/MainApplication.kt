package com.example.cataniaunited

import android.app.Application
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.WebSocketManager

class MainApplication : Application() {

    var webSocketManager: WebSocketManager = WebSocketManager()

    override fun onCreate() {
        super.onCreate()
        //Initialize web socket manager
        webSocketManager.connect(BuildConfig.SERVER_URL, WebSocketListenerImpl())
    }
}