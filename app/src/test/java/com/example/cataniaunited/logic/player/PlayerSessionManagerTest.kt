package com.example.cataniaunited.logic.player

import android.content.Context
import android.util.Log
import com.example.cataniaunited.MainApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PlayerSessionManagerTest {

    private lateinit var playerSessionManager: PlayerSessionManager
    private lateinit var mockContext: Context
    private lateinit var mockMainApplication: MainApplication

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk<MainApplication>(relaxed = true)
        mockContext = mockk<Context>()

        every { mockContext.applicationContext } returns mockMainApplication

        playerSessionManager = PlayerSessionManager(mockContext)

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getPlayerId_returnsPlayerIdFromMainApplication_whenSuccessful() {
        val expectedPlayerId = "player123"
        every { mockMainApplication.getPlayerId() } returns expectedPlayerId

        val actualPlayerId = playerSessionManager.getPlayerId()

        assertEquals(expectedPlayerId, actualPlayerId)
        verify(exactly = 1) { mockContext.applicationContext }
        verify(exactly = 1) { mockMainApplication.getPlayerId() }
    }

    @Test
    fun getPlayerId_throwsIllegalStateException_whenContextCannotBeCastedToMainApplication() {
        val nonMainApplicationContext = mockk<Context>()
        every { nonMainApplicationContext.applicationContext } returns mockk<Context>()

        playerSessionManager = PlayerSessionManager(nonMainApplicationContext)

        assertThrows<IllegalStateException> {
            playerSessionManager.getPlayerId()
        }

        verify(exactly = 1) { nonMainApplicationContext.applicationContext }
        verify(exactly = 1) {
            Log.e(
                "PlayerSessionManager",
                "Could not fetch playerId from main application",
                any()
            )
        }
        verify(exactly = 0) { mockMainApplication.getPlayerId() }
    }

    @Test
    fun getPlayerId_throwsIllegalStateException_whenGetPlayerIdThrowsException() {
        val simulatedException = RuntimeException("Simulated error in MainApplication")
        every { mockMainApplication.getPlayerId() } throws simulatedException

        val exception = assertThrows<IllegalStateException> {
            playerSessionManager.getPlayerId()
        }

        assertEquals("No player Id set", exception.message)
        verify(exactly = 1) { mockContext.applicationContext }
        verify(exactly = 1) { mockMainApplication.getPlayerId() }
        verify(exactly = 1) {
            Log.e(
                "PlayerSessionManager",
                "Could not fetch playerId from main application",
                simulatedException
            )
        }
    }

    @Test
    fun getPlayerId_returnsEmptyString_whenMainApplicationReturnsEmptyString() {
        val expectedPlayerId = ""
        every { mockMainApplication.getPlayerId() } returns expectedPlayerId

        val actualPlayerId = playerSessionManager.getPlayerId()

        assertEquals(expectedPlayerId, actualPlayerId)
        verify(exactly = 1) { mockContext.applicationContext }
        verify(exactly = 1) { mockMainApplication.getPlayerId() }
        verify(exactly = 0) { Log.e(any(), any(), any()) }
    }
}