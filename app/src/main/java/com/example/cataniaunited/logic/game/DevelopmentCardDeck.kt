package com.example.cataniaunited.logic.game

class DevelopmentCardDeck {

    private val deck: MutableList<DevelopmentCard> = mutableListOf()

    init {
        addCards(DevelopmentCardType.KNIGHT, 14)
        addCards(DevelopmentCardType.VICTORY_POINT, 5)
        addCards(DevelopmentCardType.ROAD_BUILDING, 2)
        addCards(DevelopmentCardType.YEAR_OF_PLENTY, 2)
        addCards(DevelopmentCardType.MONOPOLY, 2)

        deck.shuffle()
    }

    private fun addCards(type: DevelopmentCardType, count: Int) {
        repeat(count) {
            deck.add(DevelopmentCard(type))
        }
    }

    fun drawCard(): DevelopmentCard? {
        return if (deck.isNotEmpty()) deck.removeAt(0) else null
    }
}
