package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TutorialScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testTutorialScreenShowingExpectedText() {

        composeRule.setContent {
            CataniaUnitedTheme {
                TutorialScreen(onBackClick = {})
            }
        }

        composeRule.onNodeWithText("TUTORIAL").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("BASIC CONCEPTS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("1.\tSettlements & Cities").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("2.\tRoads").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("3.\tDistance Rule").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("4.\tResource Cards").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("5.\tNumber Tokens & Dice").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("6.\tThe Robber & Rolling a 7").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("7.\tDevelopment Cards").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("TURN SEQUENCE").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("WINNING THE GAME").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("KEY TIPS & NOTES").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Each player’s turn has 3 main phases:").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("o\tBalancing Resources: If you have too many of one resource, try to trade or place a settlement on a special harbor.")

    }

    @Test
    fun testTutorialScreenBackButtonCallsOnBackClick() {
        var backButtonClicked = false

        composeRule.setContent {
            CataniaUnitedTheme {
                TutorialScreen(onBackClick = { backButtonClicked = true })
            }
        }

        composeRule.onNodeWithText("BACK").performClick()
        assertTrue(backButtonClicked)
    }
}
