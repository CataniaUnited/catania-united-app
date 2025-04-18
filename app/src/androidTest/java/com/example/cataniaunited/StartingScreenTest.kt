package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StartingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun startingScreenShowsAllElements() {
        composeRule.setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                StartingScreen(
                    onLearnClick = {},
                    onStartClick = {},
                    onTestClick = {},
                )
            }
        }
        composeRule.onNodeWithText("CATAN UNIVERSE").assertIsDisplayed()
        composeRule.onNodeWithText("LEARN").assertIsDisplayed()
        composeRule.onNodeWithText("OR").assertIsDisplayed()
        composeRule.onNodeWithText("START GAME").assertIsDisplayed()
    }

    @Test
    fun startingScreenButtonsClickable() {
        var learnClicked = false
        var startClicked = false

        composeRule.setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                StartingScreen(
                    onLearnClick = { learnClicked = true },
                    onStartClick = { startClicked = true },
                    onTestClick = { }
                )
            }
        }
        composeRule.onNodeWithText("LEARN").performClick()
        assertTrue(learnClicked)

        composeRule.onNodeWithText("START GAME").performClick()
        assertTrue(startClicked)
    }

}
