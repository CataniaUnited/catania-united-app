package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
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
                    onStartClick = {}
                )
            }
        }

        composeRule.onNodeWithText("CATAN UNIVERSE").assertIsDisplayed()
        composeRule.onNodeWithText("LEARN").assertIsDisplayed()
        composeRule.onNodeWithText("OR").assertIsDisplayed()
        composeRule.onNodeWithText("START GAME").assertIsDisplayed()
    }


}
