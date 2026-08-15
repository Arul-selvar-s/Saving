package com.saving.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saving.app.data.model.TransactionEntity
import com.saving.app.ui.components.AddTransactionSheet
import com.saving.app.ui.components.EditTransactionSheet
import com.saving.app.ui.components.FilterSheet
import com.saving.app.ui.components.ManageCategoriesSheet
import com.saving.app.ui.components.MonthHeader
import com.saving.app.ui.components.SavingSummaryDialog
import com.saving.app.ui.components.TotalsRow
import com.saving.app.ui.components.TransactionRow
import com.saving.app.ui.theme.TextMuted
import com.saving.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showSavingSummary by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💮 Saving") },
                actions = {
                    IconButton(onClick = { showManageCategories = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Manage categories")
                    }
                    BadgedBox(badge = { if (filterState.isActive) Badge() }) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TotalsRow(
                totalSavings = totalSavings,
                totalBalance = totalBalance,
                onSavingsClick = { showSavingSummary = true }
            )

            if (filterState.isActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters applied",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    TextButton(onClick = { viewModel.clearFilter() }) { Text("Clear") }
                }
            }

            if (groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (filterState.isActive)
                            "No entries match these filters."
                        else
                            "No entries yet.\nTap + to add your first saving or expense.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedTransactions.forEach { group ->
                        item(key = "header-${group.year}-${group.month}") {
                            MonthHeader(group)
                        }
                        items(group.transactions, key = { it.id }) { transaction ->
                            TransactionRow(transaction, onClick = { editingTransaction = transaction })
                        }
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

    if (showFilterSheet) {
        FilterSheet(
            currentFilter = filterState,
            categories = categories,
            onApply = { newFilter ->
                viewModel.updateFilter(newFilter)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showManageCategories) {
        ManageCategoriesSheet(
            categories = categories,
            onDismiss = { showManageCategories = false },
            onAdd = { name -> viewModel.addCategory(name) },
            onUpdate = { category -> viewModel.updateCategory(category) },
            onDelete = { category -> viewModel.deleteCategory(category) }
        )
    }

    if (showSavingSummary) {
        SavingSummaryDialog(
            totalSaving = totalSavings,
            totalBalance = totalBalance,
            onDismiss = { showSavingSummary = false }
        )
    }

    editingTransaction?.let { transaction ->
        EditTransactionSheet(
            transaction = transaction,
            categories = categories,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                viewModel.updateTransaction(updated)
                editingTransaction = null
            },
            onDelete = { toDelete ->
                viewModel.deleteTransaction(toDelete)
                editingTransaction = null
            },
            onAddCategory = { name -> viewModel.addCategory(name) }
        )
    }
}
