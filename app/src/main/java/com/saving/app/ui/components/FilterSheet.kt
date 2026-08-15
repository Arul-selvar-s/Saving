package com.saving.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.FilterState
import com.saving.app.data.model.TransactionType
import com.saving.app.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    currentFilter: FilterState,
    categories: List<CategoryEntity>,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(currentFilter.type) }
    var category by remember { mutableStateOf(currentFilter.category) }
    var specificDate by remember { mutableStateOf(currentFilter.specificDateMillis) }
    var month by remember { mutableStateOf(currentFilter.month) }
    var year by remember { mutableStateOf(currentFilter.year) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearOptions = (currentYear - 5..currentYear).toList().reversed()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Text("Type", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Row {
                FilterChip(selected = type == null, onClick = { type = null }, label = { Text("All") })
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = type == TransactionType.SAVING,
                    onClick = { type = TransactionType.SAVING },
                    label = { Text("Saving") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { type = TransactionType.EXPENSE },
                    label = { Text("Expense") }
                )
            }
            Spacer(Modifier.height(16.dp))

            Text("Category", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = categoryMenuExpanded,
                onExpandedChange = { categoryMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = category ?: "All categories",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                DropdownMenu(
                    expanded = categoryMenuExpanded,
                    onDismissRequest = { categoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All categories") },
                        onClick = { category = null; categoryMenuExpanded = false }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { category = cat.name; categoryMenuExpanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Specific date", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { specificDate?.let { timeInMillis = it } }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val picked = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                specificDate = picked.timeInMillis
                            },
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(specificDate?.let { dateFormatter.format(Date(it)) } ?: "Any date")
                }
                if (specificDate != null) {
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { specificDate = null }) { Text("Clear") }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Month & Year", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Row {
                ExposedDropdownMenuBox(
                    expanded = monthMenuExpanded,
                    onExpandedChange = { monthMenuExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = month?.let { monthNames[it] } ?: "Any month",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = monthMenuExpanded,
                        onDismissRequest = { monthMenuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Any month") }, onClick = { month = null; monthMenuExpanded = false })
                        monthNames.forEachIndexed { index, name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { month = index; monthMenuExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBox(
                    expanded = yearMenuExpanded,
                    onExpandedChange = { yearMenuExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = year?.toString() ?: "Any year",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = yearMenuExpanded,
                        onDismissRequest = { yearMenuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Any year") }, onClick = { year = null; yearMenuExpanded = false })
                        yearOptions.forEach { y ->
                            DropdownMenuItem(text = { Text(y.toString()) }, onClick = { year = y; yearMenuExpanded = false })
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    type = null; category = null; specificDate = null; month = null; year = null
                }) { Text("Reset") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    onApply(
                        FilterState(
                            type = type,
                            category = category,
                            specificDateMillis = specificDate,
                            month = month,
                            year = year
                        )
                    )
                }) { Text("Apply") }
            }
        }
    }
}
