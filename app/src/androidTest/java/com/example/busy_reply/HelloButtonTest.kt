package com.example.busy_reply

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HelloButtonTest {
    
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
