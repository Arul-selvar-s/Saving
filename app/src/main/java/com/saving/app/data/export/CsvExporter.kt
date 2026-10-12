package com.saving.app.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.saving.app.data.model.TransactionEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    /**
     * Writes the given transactions to a CSV file under the app's cache dir and returns a
     * content:// Uri for it (via FileProvider) that can be shared/opened by other apps,
     * e.g. Excel, Google Sheets, or email. Includes both Note/Category and the optional
     * Message field, since a full export is useful even though Message isn't shown in the
     * main list or matched by filters.
     */
    fun exportToCsv(context: Context, transactions: List<TransactionEntity>): Uri {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("Date,Type,Category/Note,Message,Amount\n")

        transactions.sortedByDescending { it.dateTimeMillis }.forEach { tx ->
            sb.append(dateFormatter.format(Date(tx.dateTimeMillis))).append(',')
            sb.append(tx.type).append(',')
            sb.append(csvEscape(tx.note)).append(',')
            sb.append(csvEscape(tx.message)).append(',')
            sb.append(tx.amount)
            sb.append('\n')
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "saving_export_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun csvEscape(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
