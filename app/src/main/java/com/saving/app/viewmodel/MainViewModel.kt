package com.saving.app.viewmodel

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.saving.app.auth.DriveAccessResult
import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.FilterState
import com.saving.app.data.model.MonthGroup
import com.saving.app.data.model.TransactionEntity
import com.saving.app.data.model.TransactionType
import com.saving.app.data.repository.SavingRepository
import com.saving.app.data.sync.SyncManager
import com.saving.app.data.sync.SyncOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class MainViewModel(
    private val repository: SavingRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repository.transactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CategoryEntity>> = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Totals always reflect ALL transactions, regardless of active filters
    val totalSavings: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Balance = Saving - Expense. Negative when expenses outweigh savings for the period.
    val totalBalance: StateFlow<Double> = combine(totalSavings, totalExpenses) { saving, expense ->
        saving - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _filterState) { list, filter ->
            list.filter { transaction -> matchesFilter(transaction, filter) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions grouped by month (most recent month first), each with its own saving/balance totals
    val groupedTransactions: StateFlow<List<MonthGroup>> = filteredTransactions.map { list ->
        list.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.dateTimeMillis }
            cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
        }.map { (key, txsInMonth) ->
            val (year, month) = key
            val monthSaving = txsInMonth.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount }
            val monthExpense = txsInMonth.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            MonthGroup(
                year = year,
                month = month,
                label = monthLabel(year, month),
                transactions = txsInMonth,
                monthSaving = monthSaving,
                monthBalance = monthSaving - monthExpense
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun monthLabel(year: Int, month: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    private fun matchesFilter(transaction: TransactionEntity, filter: FilterState): Boolean {
        if (filter.type != null && transaction.type != filter.type.name) return false
        if (filter.category != null && transaction.note != filter.category) return false

        if (filter.specificDateMillis != null) {
            val txCal = Calendar.getInstance().apply { timeInMillis = transaction.dateTimeMillis }
            val filterCal = Calendar.getInstance().apply { timeInMillis = filter.specificDateMillis }
            if (txCal.get(Calendar.YEAR) != filterCal.get(Calendar.YEAR) ||
                txCal.get(Calendar.DAY_OF_YEAR) != filterCal.get(Calendar.DAY_OF_YEAR)
            ) return false
        }

        if (filter.month != null || filter.year != null) {
            val txCal = Calendar.getInstance().apply { timeInMillis = transaction.dateTimeMillis }
            if (filter.month != null && txCal.get(Calendar.MONTH) != filter.month) return false
            if (filter.year != null && txCal.get(Calendar.YEAR) != filter.year) return false
        }

        return true
    }

    fun updateFilter(filter: FilterState) {
        _filterState.value = filter
    }

    fun clearFilter() {
        _filterState.value = FilterState()
    }

    // ---- Cloud sync state ----

    private val _signedInAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val signedInAccount: StateFlow<GoogleSignInAccount?> = _signedInAccount

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError

    // Non-null when Google needs the user to approve Drive access via a system dialog.
    // MainActivity observes this and launches the resolution intent.
    private val _authorizationNeeded = MutableStateFlow<IntentSender?>(null)
    val authorizationNeeded: StateFlow<IntentSender?> = _authorizationNeeded

    fun setSignedInAccount(account: GoogleSignInAccount?) {
        _signedInAccount.value = account
        _syncError.value = null
        if (account != null) syncNow()
    }

    fun syncNow() {
        if (_signedInAccount.value == null) return
        viewModelScope.launch {
            _isSyncing.value = true
            handleOutcome(syncManager.pullAndMerge())
            _isSyncing.value = false
        }
    }

    /** Called by MainActivity after the user resolves (or cancels) a Drive consent dialog. */
    fun onAuthorizationResult(data: Intent?) {
        _authorizationNeeded.value = null
        viewModelScope.launch {
            when (val result = syncManager.handleAuthorizationResolution(data)) {
                is DriveAccessResult.Authorized -> syncNow()
                is DriveAccessResult.NeedsConsent -> _authorizationNeeded.value = result.intentSender
                is DriveAccessResult.Failed -> _syncError.value = result.message
            }
        }
    }

    private fun handleOutcome(outcome: SyncOutcome) {
        when (outcome) {
            is SyncOutcome.Success -> _syncError.value = null
            is SyncOutcome.NeedsConsent -> _authorizationNeeded.value = outcome.intentSender
            is SyncOutcome.Error -> _syncError.value = outcome.message
        }
    }

    private fun pushIfSignedIn() {
        if (_signedInAccount.value != null) {
            viewModelScope.launch { handleOutcome(syncManager.pushSnapshot()) }
        }
    }

    // ---- Transactions / Categories (now sync-aware) ----

    fun addTransaction(type: TransactionType, amount: Double, note: String, dateTimeMillis: Long) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = type.name,
                    amount = amount,
                    note = note,
                    dateTimeMillis = dateTimeMillis,
                    cloudId = UUID.randomUUID().toString()
                )
            )
            pushIfSignedIn()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            syncManager.recordTransactionDeleted(transaction.cloudId)
            pushIfSignedIn()
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val withCloudId = if (transaction.cloudId == null) {
                transaction.copy(cloudId = UUID.randomUUID().toString())
            } else transaction
            repository.updateTransaction(withCloudId)
            pushIfSignedIn()
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name)
            pushIfSignedIn()
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
            pushIfSignedIn()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            syncManager.recordCategoryDeleted(category.name)
            pushIfSignedIn()
        }
    }
}

class MainViewModelFactory(
    private val repository: SavingRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, syncManager) as T
    }
}
