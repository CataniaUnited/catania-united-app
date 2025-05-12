package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNavigatesToTutorialScreenOnLearnClick() {
        composeTestRule.onNodeWithText("LEARN").performClick()
        composeTestRule.onNodeWithText("TUTORIAL").assertIsDisplayed()
    }

    @Test
    fun appDoesNotNavigateWhenStartGameIsClicked() { // change when StartGame page is implemented
        composeTestRule.onNodeWithText("START GAME").performClick()
        composeTestRule.onNodeWithText("TUTORIAL").assertDoesNotExist()
    }

    @Test
    fun appNavigatesBackToStartingScreenFromTutorialScreen(){
        composeTestRule.onNodeWithText("LEARN").performClick()
        composeTestRule.onNodeWithText("TUTORIAL").assertIsDisplayed()
        composeTestRule.onNodeWithText("BACK").performClick()
    }



}
