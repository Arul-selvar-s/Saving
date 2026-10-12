package com.saving.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.TextMuted

@Composable
fun SavingsGoalDialog(
    currentName: String?,
    currentAmount: Double?,
    onSave: (name: String, amount: Double) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName ?: "") }
    var amountText by remember {
        mutableStateOf(
            currentAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Savings Goal") },
        text = {
            Column {
                Text(
                    text = "Set a target amount to track progress toward. This is stored on " +
                        "this device only and isn't synced to Drive.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal name (optional)") },
                    placeholder = { Text("Savings Goal") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Target amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0.0) {
                    onSave(name.ifBlank { "Savings Goal" }, amount)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            if (currentAmount != null) {
                TextButton(onClick = onClear) { Text("Clear Goal", color = ExpenseRed) }
            } else {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
