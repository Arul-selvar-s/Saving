package com.saving.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Stored as String ("SAVING" / "EXPENSE") so Room doesn't need a custom enum converter
    val type: String,

    val amount: Double,

    // For EXPENSE this holds the category name; for SAVING this holds the free-text note
    val note: String,

    val dateTimeMillis: Long,

    val updatedAtMillis: Long = System.currentTimeMillis(),

    // Will be used in the Google Drive sync step to match local <-> cloud records
    val cloudId: String? = null
)
