package com.saving.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.saving.app.ui.theme.SavingsGreen
import com.saving.app.ui.theme.TextMuted
import java.util.Locale

/**
 * Tappable card showing progress toward the user's savings goal (target amount set in
 * SavingsGoalDialog). Goal is local-only per device — see GoalStore. Progress compares
 * against lifetime total Saving (not netted against expenses), since the goal is about how
 * much has been saved, not the current balance.
 */
@Composable
fun SavingsGoalCard(
    goalName: String?,
    goalAmount: Double?,
    currentSaving: Double,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (goalAmount == null || goalAmount <= 0.0) {
                Text(
                    text = "Set a savings goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            } else {
                val progress = (currentSaving / goalAmount).toFloat().coerceIn(0f, 1f)
                val percentText = String.format(Locale.getDefault(), "%.0f%%", progress * 100)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = goalName?.takeIf { it.isNotBlank() } ?: "Savings Goal",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.titleMedium,
                        color = SavingsGreen
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    // Using the plain Float overload (not the newer progress-lambda one) —
                    // the Compose BOM pinned in this project (2024.06.00, Material3 ~1.2.x)
                    // predates the lambda overload; see build-and-auth-learnings for why we
                    // stay conservative with Material3 API usage in this project.
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SavingsGreen
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹" + String.format(Locale.getDefault(), "%.2f", currentSaving) +
                        " of ₹" + String.format(Locale.getDefault(), "%.2f", goalAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
        Divider(thickness = 1.dp)
    }
}
