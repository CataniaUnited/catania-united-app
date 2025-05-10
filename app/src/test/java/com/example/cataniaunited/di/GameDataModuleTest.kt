package com.example.cataniaunited.di

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class GameDataModuleTest {

    @Test
    fun provideGameDataHandler_returnsGameDataHandlerInstance() {
        val gameDataHandler = GameDataModule.provideGameDataHandler()
        assertNotNull(gameDataHandler)
    }
}