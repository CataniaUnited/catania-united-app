package com.example.cataniaunited.ws.provider

import kotlinx.coroutines.flow.Flow

// Interface to provide WebSocket error events
interface WebSocketErrorProvider {
    val errorFlow: Flow<String>
}