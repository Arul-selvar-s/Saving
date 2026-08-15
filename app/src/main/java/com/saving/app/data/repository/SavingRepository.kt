package com.saving.app.data.repository

import com.saving.app.data.db.AppDatabase
import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class SavingRepository(private val db: AppDatabase) {

    val transactions: Flow<List<TransactionEntity>> = db.transactionDao().getAll()
    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAll()

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        db.transactionDao().insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        db.transactionDao().update(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        db.transactionDao().delete(transaction)

    suspend fun addCategory(name: String): Long =
        db.categoryDao().insert(CategoryEntity(name = name))

    suspend fun updateCategory(category: CategoryEntity) =
        db.categoryDao().update(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        db.categoryDao().delete(category)
}
