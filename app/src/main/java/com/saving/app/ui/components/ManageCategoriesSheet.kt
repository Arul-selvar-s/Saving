package com.saving.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import com.saving.app.data.model.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesSheet(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onUpdate: (CategoryEntity) -> Unit,
    onDelete: (CategoryEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newCategoryName by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var editingName by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .fillMaxWidth()
        ) {
            Text("Manage Categories", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            categories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { editingCategory = category; editingName = category.name }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit ${category.name}")
                    }
                    IconButton(onClick = { onDelete(category) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ${category.name}")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("New category") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        onAdd(newCategoryName)
                        newCategoryName = ""
                    }
                }) { Text("Add") }
            }
        }
    }

    editingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Rename Category") },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Category name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingName.isNotBlank()) {
                        onUpdate(category.copy(name = editingName))
                    }
                    editingCategory = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) { Text("Cancel") }
            }
        )
    }
}
