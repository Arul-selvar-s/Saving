package com.saving.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.ui.components.AddTransactionSheet
import com.saving.app.ui.components.TotalsRow
import com.saving.app.ui.components.TransactionRow
import com.saving.app.ui.theme.TextMuted
import com.saving.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TotalsRow(totalSavings = totalSavings, totalExpenses = totalExpenses)

            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No entries yet.\nTap + to add your first saving or expense.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionRow(transaction)
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = categories,
            onDismiss = { showAddSheet = false },
            onSave = { type, amount, note, dateTimeMillis ->
                viewModel.addTransaction(type, amount, note, dateTimeMillis)
                showAddSheet = false
            },
            onAddCategory = { name -> viewModel.addCategory(name) }
        )
    }
}
