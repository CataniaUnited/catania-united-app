package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText("TUTORIAL").assertIsDisplayed()
        composeRule.onNodeWithText("BASIC CONCEPTS").assertIsDisplayed()
        composeRule.onNodeWithText("1.\tSettlements & Cities").assertIsDisplayed()
        composeRule.onNodeWithText("2.\tRoads").assertIsDisplayed()
        composeRule.onNodeWithText("3.\tDistance Rule").assertIsDisplayed()
        composeRule.onNodeWithText("4.\tResource Cards").assertIsDisplayed()
        composeRule.onNodeWithText("5.\tNumber Tokens & Dice").assertIsDisplayed()
        composeRule.onNodeWithText("6.\tThe Robber & Rolling a 7").assertIsDisplayed()
        composeRule.onNodeWithText("7.\tDevelopment Cards").assertIsDisplayed()
        composeRule.onNodeWithText("TURN SEQUENCE").assertIsDisplayed()
        composeRule.onNodeWithText("WINNING THE GAME").assertIsDisplayed()
        composeRule.onNodeWithText("KEY TIPS & NOTES").assertIsDisplayed()
        composeRule.onNodeWithText("Each player’s turn has 3 main phases:").assertIsDisplayed()
        composeRule.onNodeWithText("Balancing Resources: If you have too many of one resource").assertIsDisplayed()
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
