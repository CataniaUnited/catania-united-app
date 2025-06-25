package com.example.cataniaunited.logic

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow


object CardReceiver {
    private val _cardFlow = MutableSharedFlow<String>()
    val cardFlow: SharedFlow<String> = _cardFlow

    suspend fun sendCard(cardType: String) {
        _cardFlow.emit(cardType)
    }
}

