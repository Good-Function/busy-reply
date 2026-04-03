package com.example.busy_reply

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.busy_reply.ui.theme.BusyreplyTheme
import kotlinx.coroutines.delay

private fun busyReplyPermissions(): Array<String> = buildList {
    add(android.Manifest.permission.READ_PHONE_STATE)
    add(android.Manifest.permission.READ_CONTACTS)
    add(android.Manifest.permission.SEND_SMS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(android.Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

class MainActivity : ComponentActivity() {

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startMissedCallMonitorIfPermitted()
    }

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

    override fun onResume() {
        super.onResume()
        startMissedCallMonitorIfPermitted()
    }

    private fun startMissedCallMonitorIfPermitted() {
        if (!hasPermission(android.Manifest.permission.READ_PHONE_STATE)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        ) {
            return
        }
        ContextCompat.startForegroundService(
            this,
            Intent(this, MissedCallMonitorService::class.java)
        )
    }

    private fun requestPermissionsIfNeeded() {
        val missing = busyReplyPermissions().filter { !hasPermission(it) }
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
    var replyWhenBusy by remember {
        mutableStateOf(prefs.getBoolean(BusyReplyPrefs.KEY_REPLY_WHEN_BUSY, true))
    }
    var replyMissedCall by remember {
        mutableStateOf(prefs.getBoolean(BusyReplyPrefs.KEY_REPLY_MISSED_CALL, true))
    }
    var showSavedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(showSavedBanner) {
        if (!showSavedBanner) return@LaunchedEffect
        delay(1_000)
        showSavedBanner = false
    }

    Column(modifier = modifier) {
        if (showSavedBanner) {
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
        Spacer(modifier = Modifier.height(12.dp))
        CheckboxPreferenceRow(
            label = "Wyślij przy zajętej linii",
            checked = replyWhenBusy,
            onCheckedChange = { checked ->
                replyWhenBusy = checked
                prefs.edit { putBoolean(BusyReplyPrefs.KEY_REPLY_WHEN_BUSY, checked) }
            },
            rowTestTag = "checkbox_busy_row",
            boxTestTag = "checkbox_busy"
        )
        CheckboxPreferenceRow(
            label = "Wyślij dla nieodebranej rozmowy",
            checked = replyMissedCall,
            onCheckedChange = { checked ->
                replyMissedCall = checked
                prefs.edit { putBoolean(BusyReplyPrefs.KEY_REPLY_MISSED_CALL, checked) }
            },
            rowTestTag = "checkbox_missed_row",
            boxTestTag = "checkbox_missed"
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

@Composable
private fun CheckboxPreferenceRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    rowTestTag: String,
    boxTestTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .semantics { testTag = rowTestTag },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.semantics { testTag = boxTestTag }
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
