package com.saving.app.data.model

data class MonthGroup(
    val year: Int,
    val month: Int, // 0-11, Calendar.MONTH style
    val label: String, // e.g. "August 2026"
    val transactions: List<TransactionEntity>,
    val monthSaving: Double,
    val monthBalance: Double // monthSaving - monthExpense
)
