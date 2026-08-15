package com.saving.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.SavingsGreen
import java.util.Locale

@Composable
fun SavingSummaryDialog(
    totalSaving: Double,
    totalBalance: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Saving Summary") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Saving", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "₹" + String.format(Locale.getDefault(), "%.2f", totalSaving),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SavingsGreen
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Balance", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "₹" + String.format(Locale.getDefault(), "%.2f", totalBalance),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (totalBalance < 0) ExpenseRed else SavingsGreen
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
