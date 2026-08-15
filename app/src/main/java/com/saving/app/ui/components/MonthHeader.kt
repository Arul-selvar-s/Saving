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
import com.saving.app.data.model.MonthGroup
import com.saving.app.ui.theme.ExpenseRed
import com.saving.app.ui.theme.SavingsGreen
import java.util.Locale

@Composable
fun MonthHeader(group: MonthGroup) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = group.label,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saving: ₹" + String.format(Locale.getDefault(), "%.2f", group.monthSaving),
                    style = MaterialTheme.typography.labelSmall,
                    color = SavingsGreen
                )
                Text(
                    text = "Balance: ₹" + String.format(Locale.getDefault(), "%.2f", group.monthBalance),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (group.monthBalance < 0) ExpenseRed else SavingsGreen
                )
            }
        }
        Divider(thickness = 1.dp)
    }
}
