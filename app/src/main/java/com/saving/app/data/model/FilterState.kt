package com.saving.app.data.model

data class FilterState(
    val type: TransactionType? = null,
    val category: String? = null,
    val specificDateMillis: Long? = null,
    val month: Int? = null, // 0-11 (Calendar.MONTH style)
    val year: Int? = null
) {
    val isActive: Boolean
        get() = type != null || category != null || specificDateMillis != null || month != null || year != null
}
