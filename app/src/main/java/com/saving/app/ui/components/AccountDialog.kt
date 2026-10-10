package com.saving.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.TextMuted

@Composable
fun AccountDialog(
    accountEmail: String?,
    isSyncing: Boolean,
    isSwitchingAccount: Boolean,
    errorMessage: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onSwitchAccount: () -> Unit,
    onDismiss: () -> Unit
) {
    // Disable every action while either a normal sync or the pre-switch backup is running,
    // so the user can't trigger a second sync (or sign out) mid-upload.
    val busy = isSyncing || isSwitchingAccount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud Backup") },
        text = {
            Column {
                if (accountEmail != null) {
                    Text("Signed in as $accountEmail", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            isSwitchingAccount -> "Backing up your data before switching account…"
                            isSyncing -> "Syncing…"
                            else -> "Data syncs automatically whenever you add, edit, or delete an entry. " +
                                "Tap Sync Now to pull in changes made on another device, or Switch Account " +
                                "to back up this account's data and sign in with a different Google account."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    Text(
                        text = "Sign in with Google to back up your data to Drive and keep it in sync across devices.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Sync error: $errorMessage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpenseRed
                    )
                }
            }
        },
        confirmButton = {
            if (accountEmail != null) {
                Row {
                    TextButton(onClick = onSwitchAccount, enabled = !busy) { Text("Switch Account") }
                    TextButton(onClick = onSyncNow, enabled = !busy) { Text("Sync Now") }
                }
            } else {
                TextButton(onClick = onSignIn) { Text("Sign In") }
            }
        },
        dismissButton = {
            if (accountEmail != null) {
                TextButton(onClick = onSignOut, enabled = !busy) { Text("Sign Out") }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
