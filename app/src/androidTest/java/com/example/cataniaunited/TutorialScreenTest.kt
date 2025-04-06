package com.example.cataniaunited

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import org.junit.Rule
import org.junit.Test

class TutorialScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tutorialScreenDisplaysCenterText() {
        composeRule.setContent {
            CataniaUnitedTheme {
                TutorialScreen()
            }
        }
        composeRule.onNodeWithText("Tutorial Screen").assertIsDisplayed()
    }
}