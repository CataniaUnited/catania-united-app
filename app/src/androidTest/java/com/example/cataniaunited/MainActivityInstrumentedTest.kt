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
    fun appLaunchesWithStartingScreen(){
        composeTestRule.onNodeWithText("CATAN UNIVERSE").assertIsDisplayed()
    }
    @Test
    fun appNavigatesToTutorialScreenOnLearnClick() {
        composeTestRule.onNodeWithText("CATAN UNIVERSE").assertIsDisplayed()
        composeTestRule.onNodeWithText("LEARN").performClick()
        composeTestRule.onNodeWithText("Tutorial Screen").assertIsDisplayed()
    }

    @Test
    fun appDoesNotNavigateWhenStartGameIsClicked() { // change when StartGame page is implemented
        composeTestRule.onNodeWithText("CATAN UNIVERSE").assertIsDisplayed()
        composeTestRule.onNodeWithText("START GAME").performClick()
        composeTestRule.onNodeWithText("Tutorial Screen").assertDoesNotExist()
    }



}
