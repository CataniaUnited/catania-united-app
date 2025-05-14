package com.example.cataniaunited.viewmodel

import com.example.cataniaunited.logic.game.DevelopmentCard
import com.example.cataniaunited.logic.game.DevelopmentCardType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun testAddDevelopmentCard() {
        val card = DevelopmentCard(DevelopmentCardType.YEAR_OF_PLENTY)
        viewModel.addDevelopmentCard(card)

        val cards = viewModel.getDevelopmentCards()
        assertTrue(cards.contains(card))
        assertEquals(DevelopmentCardType.YEAR_OF_PLENTY, cards[0].type)
    }
}
