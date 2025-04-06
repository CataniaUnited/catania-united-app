package com.example.cataniaunited

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.filters.SdkSuppress
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import org.junit.Rule
import org.junit.Test

class CataniaUnitedThemeTest {

    @get:Rule
    val composeRule = createComposeRule()


    @Composable
    private fun DummyContent() {
        Box(modifier = Modifier.fillMaxSize()) {

        }
    }

    @Test
    fun lightThemeNoDynamicColor() {
        composeRule.setContent {
            CataniaUnitedTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                DummyContent()
            }
        }

        composeRule.onRoot().printToLog("CataniaUnitedThemeTest")
    }


    @Test
    fun darkThemeNoDynamicColor() {
        composeRule.setContent {
            CataniaUnitedTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                DummyContent()
            }
        }
        composeRule.onRoot().printToLog("CataniaUnitedThemeTest")
    }

    @Test
    fun lightThemeWithDynamicColor() {
        composeRule.setContent {
            CataniaUnitedTheme(
                darkTheme = false,
                dynamicColor = true
            ) {
                DummyContent()
            }
        }
        composeRule.onRoot().printToLog("CataniaUnitedThemeTest")
    }

    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    @Test
    fun darkThemeWithDynamicColor() {
        composeRule.setContent {
            CataniaUnitedTheme(
                darkTheme = true,
                dynamicColor = true
            ) {
                DummyContent()
            }
        }
        composeRule.onRoot().printToLog("CataniaUnitedThemeTest")
    }
}