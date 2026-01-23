package com.example.busy_reply

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HelloButtonRobolectricTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun whenButtonIsClicked_itDisplaysHelloText() {
        composeTestRule.setContent {
            HelloButtonScreen()
        }
        
        composeTestRule.onNodeWithText("Click me").performClick()
        composeTestRule.onNodeWithText("Hello").assertExists()
    }
}
