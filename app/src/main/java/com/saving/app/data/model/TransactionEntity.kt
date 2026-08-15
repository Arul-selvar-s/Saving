package com.saving.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val amount: Double,
    val note: String,
    val dateTimeMillis: Long,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val cloudId: String? = null
)
