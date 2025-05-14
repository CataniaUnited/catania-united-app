package com.example.cataniaunited.model

import com.example.cataniaunited.logic.game.DevelopmentCard
import com.example.cataniaunited.logic.game.DevelopmentCardType
import org.junit.Assert.*
import org.junit.Test

class DevelopmentCardTest {

    @Test
    fun testDevelopmentCardCreation() {
        val card = DevelopmentCard(DevelopmentCardType.KNIGHT)
        assertEquals(DevelopmentCardType.KNIGHT, card.type)
        assertFalse(card.used)
    }

    @Test
    fun testCardUsage() {
        val card = DevelopmentCard(DevelopmentCardType.MONOPOLY)
        card.used = true
        assertTrue(card.used)
    }
}
