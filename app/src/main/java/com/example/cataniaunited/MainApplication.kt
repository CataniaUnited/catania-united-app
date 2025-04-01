package com.example.cataniaunited

import android.app.Application
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.WebSocketClient

open class MainApplication : Application() {

    var webSocketClient: WebSocketClient = WebSocketClient(BuildConfig.SERVER_URL)

    override fun onCreate() {
        super.onCreate()
        //Initialize web socket manager
        webSocketClient.connect(WebSocketListenerImpl())
    }
}