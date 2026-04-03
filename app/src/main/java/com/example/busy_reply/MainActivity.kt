package com.example.busy_reply

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.busy_reply.ui.theme.BusyreplyTheme
import kotlinx.coroutines.delay

private val BUSY_REPLY_PERMISSIONS = arrayOf(
    android.Manifest.permission.READ_PHONE_STATE,
    android.Manifest.permission.READ_CONTACTS,
    android.Manifest.permission.SEND_SMS
)

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()
        setContent {
            BusyreplyTheme {
                Scaffold(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) { innerPadding ->
                    PersistentTextScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                    )
                }
            }
        }
        requestCallScreeningRole()
    }

    private fun requestPermissionsIfNeeded() {
        val missing = BUSY_REPLY_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) requestPermissionsLauncher.launch(missing.toTypedArray())
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager ?: return
        if (!roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) return
        if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) return
        startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
    }

}

@Composable
fun PersistentTextScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(BusyReplyPrefs.PREFS_NAME, Context.MODE_PRIVATE) }
    var text by remember {
        mutableStateOf(prefs.getString(BusyReplyPrefs.KEY_SAVED_TEXT, "") ?: "")
    }
    var showSavedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedBanner) {
        if (!showSavedBanner) return@LaunchedEffect
        delay(1_000)
        showSavedBanner = false
    }

    Column(modifier = modifier) {
        AnimatedVisibility(visible = showSavedBanner) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .semantics { testTag = "saved_banner" },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Zapisano",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .semantics { testTag = "note_text_field" },
            placeholder = { Text("Enter your text…") },
            minLines = 1,
            maxLines = Int.MAX_VALUE
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                prefs.edit { putString(BusyReplyPrefs.KEY_SAVED_TEXT, text) }
                showSavedBanner = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
