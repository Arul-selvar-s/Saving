package com.saving.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    // so the user can't trigger a second sync (or sign out/switch) mid-upload.
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
                            else -> "Data syncs automatically whenever you add, edit, or delete an entry."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(16.dp))

                    // Each action gets its own full-width row so none of the tap targets
                    // are squeezed or overlapping on narrower phones.
                    TextButton(
                        onClick = onSyncNow,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sync Now", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    }
                    TextButton(
                        onClick = onSwitchAccount,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Switch Account",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    TextButton(
                        onClick = onSignOut,
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Sign Out",
                            color = if (busy) TextMuted else ExpenseRed,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
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
            if (accountEmail == null) {
                TextButton(onClick = onSignIn) { Text("Sign In") }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (accountEmail == null) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
