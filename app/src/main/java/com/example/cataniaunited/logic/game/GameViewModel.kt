package com.example.cataniaunited.viewmodel

import com.example.cataniaunited.logic.game.DevelopmentCard

class GameViewModel {

    private val devCards = mutableListOf<DevelopmentCard>()

    fun addDevelopmentCard(card: DevelopmentCard) {
        devCards.add(card)
    }

    fun getDevelopmentCards(): List<DevelopmentCard> = devCards
}
