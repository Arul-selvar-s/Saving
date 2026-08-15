package com.saving.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.saving.app.data.model.TransactionEntity
import com.saving.app.data.model.TransactionType
import com.saving.app.data.repository.SavingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: SavingRepository) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repository.transactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories = repository.categories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSavings: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpenses: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
}

class MainViewModelFactory(private val repository: SavingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}
