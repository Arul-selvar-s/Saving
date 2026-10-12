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

    // For EXPENSE this holds the category name; for SAVING this holds the short free-text
    // note. This is the ONLY text field that filters/category-matching ever look at.
    val note: String,

    val dateTimeMillis: Long,

    val updatedAtMillis: Long = System.currentTimeMillis(),

    // Will be used in the Google Drive sync step to match local <-> cloud records
    val cloudId: String? = null,

    // A second, optional longer free-text field, available on both Saving and Expense
    // entries. Purely informational: never matched by filters or the category dropdown,
    // and only shown when an entry is opened to view/edit it (not in the main list).
    val message: String = ""
)
