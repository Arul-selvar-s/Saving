package com.saving.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.SavingsGreen
import com.saving.app.ui.theme.TextMuted
import com.saving.app.viewmodel.FilteredTotals
import java.util.Locale

/**
 * Shows the combined (all months together) Saving / Expense / Balance for whatever
 * filter and/or search is currently active, e.g. the all-time total for a chosen category.
 * This complements MonthHeader, which only shows per-month totals.
 */
@Composable
fun OverallFilterSummary(totals: FilteredTotals) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Overall (all results)",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saving: ₹" + String.format(Locale.getDefault(), "%.2f", totals.saving),
                    style = MaterialTheme.typography.labelMedium,
                    color = SavingsGreen
                )
                Text(
                    text = "Expense: ₹" + String.format(Locale.getDefault(), "%.2f", totals.expense),
                    style = MaterialTheme.typography.labelMedium,
                    color = ExpenseRed
                )
                Text(
                    text = "Balance: ₹" + String.format(Locale.getDefault(), "%.2f", totals.balance),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (totals.balance < 0) ExpenseRed else SavingsGreen
                )
            }
        }
        Divider(thickness = 2.dp)
    }
}
