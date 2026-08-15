package com.saving.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.data.model.TransactionEntity
import com.saving.app.data.model.TransactionType
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.SavingsGreen
import com.saving.app.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionRow(transaction: TransactionEntity, onClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isSaving = transaction.type == TransactionType.SAVING.name

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = transaction.note, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = formatter.format(Date(transaction.dateTimeMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Text(
                text = (if (isSaving) "+ ₹" else "- ₹") + String.format(Locale.getDefault(), "%.2f", transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSaving) SavingsGreen else ExpenseRed
            )
        }
        Divider()
    }
}
