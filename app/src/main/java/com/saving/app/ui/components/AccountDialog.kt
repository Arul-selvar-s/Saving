package com.saving.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.TextMuted

@Composable
fun AccountDialog(
    accountEmail: String?,
    isSyncing: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud Backup") },
        text = {
            Column {
                if (accountEmail != null) {
                    Text("Signed in as $accountEmail", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isSyncing) "Syncing…" else "Data syncs automatically whenever you add, edit, or delete an entry. Tap Sync Now to pull in changes made on another device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "Sign in with Google to back up your data to Drive and keep it in sync across devices.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            if (accountEmail != null) {
                TextButton(onClick = onSyncNow, enabled = !isSyncing) { Text("Sync Now") }
            } else {
                TextButton(onClick = onSignIn) { Text("Sign In") }
            }
        },
        dismissButton = {
            if (accountEmail != null) {
                TextButton(onClick = onSignOut) { Text("Sign Out") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
