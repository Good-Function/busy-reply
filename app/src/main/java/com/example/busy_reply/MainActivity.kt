package com.example.busy_reply

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.busy_reply.ui.theme.BusyreplyTheme
import kotlinx.coroutines.launch

private const val PREFS_NAME = "busy_reply_prefs"
private const val KEY_SAVED_TEXT = "saved_text"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusyreplyTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
                ) { innerPadding ->
                    PersistentTextScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun PersistentTextScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var text by remember {
        mutableStateOf(prefs.getString(KEY_SAVED_TEXT, "") ?: "")
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { testTag = "note_text_field" },
            placeholder = { Text("Enter your text…") },
            minLines = 5,
            maxLines = Int.MAX_VALUE
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                prefs.edit { putString(KEY_SAVED_TEXT, text) }
                scope.launch {
                    snackbarHostState.showSnackbar("Saved")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
