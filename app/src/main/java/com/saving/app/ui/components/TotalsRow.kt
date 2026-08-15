package com.saving.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.SavingsGreen
import com.saving.app.ui.theme.TextMuted
import java.util.Locale

@Composable
fun TotalsRow(
    totalSavings: Double,
    totalBalance: Double,
    onSavingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.clickable { onSavingsClick() }) {
            Text(
                text = "Total Savings",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = "₹" + String.format(Locale.getDefault(), "%.2f", totalSavings),
                style = MaterialTheme.typography.headlineMedium,
                color = SavingsGreen
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Balance",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = "₹" + String.format(Locale.getDefault(), "%.2f", totalBalance),
                style = MaterialTheme.typography.headlineMedium,
                color = if (totalBalance < 0) ExpenseRed else SavingsGreen
            )
        }
    }
}
