package com.example.busy_reply

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.busy_reply.ui.theme.BusyreplyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PersistentTextScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setPersistentTextContent() {
        composeTestRule.setContent {
            val context = RuntimeEnvironment.getApplication()
            CompositionLocalProvider(LocalContext provides context) {
                BusyreplyTheme {
                    Scaffold(modifier = Modifier.padding(16.dp)) { innerPadding ->
                        PersistentTextScreen(
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun initialLoad_showsSaveButtonAndTextField() {
        setPersistentTextContent()
        composeTestRule.onNodeWithText("Save").assertExists()
        composeTestRule.onNodeWithTag("note_text_field").assertExists()
    }

    @Test
    fun saveButton_showsSavedBanner() {
        setPersistentTextContent()
        composeTestRule.onNodeWithTag("note_text_field").performTextInput("Important note")
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.onNodeWithTag("saved_banner").assertExists()
        composeTestRule.onNodeWithText("Saved").assertExists()
    }

    @Test
    fun prePopulatedPrefs_showsSavedTextOnLoad() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(BusyReplyPrefs.PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(BusyReplyPrefs.KEY_SAVED_TEXT, "Persistence works")
        }

        setPersistentTextContent()
        composeTestRule.onNodeWithTag("note_text_field").assert(hasText("Persistence works", substring = true))
    }
}
