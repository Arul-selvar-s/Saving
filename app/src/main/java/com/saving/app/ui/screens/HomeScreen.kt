package com.saving.app.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.saving.app.data.export.CsvExporter
import com.saving.app.data.model.TransactionEntity
import com.saving.app.ui.components.AccountDialog
import com.saving.app.ui.components.AddTransactionSheet
import com.saving.app.ui.components.EditTransactionSheet
import com.saving.app.ui.components.FilterSheet
import com.saving.app.ui.components.ManageCategoriesSheet
import com.saving.app.ui.components.MonthHeader
import com.saving.app.ui.components.OverallFilterSummary
import com.saving.app.ui.components.SavingSummaryDialog
import com.saving.app.ui.components.SavingsGoalCard
import com.saving.app.ui.components.SavingsGoalDialog
import com.saving.app.ui.components.TotalsRow
import com.saving.app.ui.components.TransactionRow
import com.saving.app.ui.theme.TextMuted
import com.saving.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    val context = LocalContext.current

    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val filteredTotals by viewModel.filteredTotals.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val signedInAccount by viewModel.signedInAccount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isSwitchingAccount by viewModel.isSwitchingAccount.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val goalName by viewModel.goalName.collectAsState()
    val goalAmount by viewModel.goalAmount.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showManageCategories by remember { mutableStateOf(false) }
    var showSavingSummary by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search by note or category") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("💮 Saving")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (showSearchBar) {
                            // Closing the search bar also clears it, so leaving search mode
                            // never leaves a stale filter silently applied to the list.
                            viewModel.clearSearch()
                        }
                        showSearchBar = !showSearchBar
                    }) {
                        Icon(
                            if (showSearchBar) Icons.Default.Clear else Icons.Default.Search,
                            contentDescription = if (showSearchBar) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { showAccountDialog = true }) {
                        BadgedBox(badge = { if (signedInAccount == null) Badge() }) {
                            Icon(Icons.Default.Cloud, contentDescription = "Cloud backup")
                        }
                    }
                    // Dedicated, always-visible switch button (separate from the Cloud
                    // Backup dialog) — only shown once signed in, since there's nothing to
                    // switch away from otherwise. Backs up this account's data first, then
                    // opens Google's account picker so a different login can be chosen.
                    if (signedInAccount != null) {
                        IconButton(onClick = onSwitchAccountClick, enabled = !isSwitchingAccount) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = "Switch account")
                        }
                    }
                    BadgedBox(badge = { if (filterState.isActive) Badge() }) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manage Categories") },
                                onClick = {
                                    showOverflowMenu = false
                                    showManageCategories = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export CSV") },
                                onClick = {
                                    showOverflowMenu = false
                                    // Exports whatever the current filter/search shows, so
                                    // "export this view" matches what's on screen.
                                    val uri = CsvExporter.exportToCsv(context, filteredTransactions)
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Export transactions"))
                                }
                            )
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

            SavingsGoalCard(
                goalName = goalName,
                goalAmount = goalAmount,
                currentSaving = totalSavings,
                onClick = { showGoalDialog = true }
            )

            if (filterState.isActive || searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            filterState.isActive && searchQuery.isNotBlank() -> "Filters applied + Searching"
                            filterState.isActive -> "Filters applied"
                            else -> "Searching"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    TextButton(onClick = {
                        viewModel.clearFilter()
                        viewModel.clearSearch()
                    }) { Text("Clear") }
                }
                OverallFilterSummary(filteredTotals)
            }

            if (groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (filterState.isActive || searchQuery.isNotBlank())
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
            onSave = { type, amount, note, dateTimeMillis, message ->
                viewModel.addTransaction(type, amount, note, dateTimeMillis, message)
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

    if (showGoalDialog) {
        SavingsGoalDialog(
            currentName = goalName,
            currentAmount = goalAmount,
            onSave = { name, amount ->
                viewModel.setSavingsGoal(name, amount)
                showGoalDialog = false
            },
            onClear = {
                viewModel.clearSavingsGoal()
                showGoalDialog = false
            },
            onDismiss = { showGoalDialog = false }
        )
    }

    if (showAccountDialog) {
        AccountDialog(
            accountEmail = signedInAccount?.email,
            isSyncing = isSyncing,
            isSwitchingAccount = isSwitchingAccount,
            errorMessage = syncError,
            onSignIn = {
                onSignInClick()
                showAccountDialog = false
            },
            onSignOut = {
                onSignOutClick()
                showAccountDialog = false
            },
            onSyncNow = { viewModel.syncNow() },
            onSwitchAccount = { viewModel.requestSwitchAccount() },
            onDismiss = { showAccountDialog = false }
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
