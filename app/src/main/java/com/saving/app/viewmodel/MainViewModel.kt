package com.saving.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.FilterState
import com.saving.app.data.model.TransactionEntity
import com.saving.app.data.model.TransactionType
import com.saving.app.data.repository.SavingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val repository: SavingRepository) : ViewModel() {

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

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _filterState) { list, filter ->
            list.filter { transaction -> matchesFilter(transaction, filter) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun addTransaction(type: TransactionType, amount: Double, note: String, dateTimeMillis: Long) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    type = type.name,
                    amount = amount,
                    note = note,
                    dateTimeMillis = dateTimeMillis
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch { repository.addCategory(name) }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.updateCategory(category) }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }
}

class MainViewModelFactory(private val repository: SavingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
