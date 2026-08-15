package com.saving.app.data.sync

import com.saving.app.data.model.CategoryEntity
import com.saving.app.data.model.TransactionEntity
import org.json.JSONArray
import org.json.JSONObject

object BackupSerializer {

    data class ParsedBackup(
        val transactions: List<TransactionEntity>,
        val categoryNames: List<String>,
        val deletedIds: Set<String>
    )

    fun serialize(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        deletedIds: Set<String>
    ): String {
        val txArray = JSONArray()
        transactions.forEach { tx ->
            txArray.put(
                JSONObject().apply {
                    put("cloudId", tx.cloudId ?: "")
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("note", tx.note)
                    put("dateTimeMillis", tx.dateTimeMillis)
                    put("updatedAtMillis", tx.updatedAtMillis)
                }
            )
        }

        val catArray = JSONArray()
        categories.forEach { cat ->
            catArray.put(JSONObject().apply { put("name", cat.name) })
        }

        val deletedArray = JSONArray()
        deletedIds.forEach { deletedArray.put(it) }

        return JSONObject().apply {
            put("transactions", txArray)
            put("categories", catArray)
            put("deletedIds", deletedArray)
            put("syncedAtMillis", System.currentTimeMillis())
        }.toString()
    }

    fun deserialize(json: String): ParsedBackup {
        val root = JSONObject(json)

        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        val transactions = (0 until txArray.length()).map { i ->
            val obj = txArray.getJSONObject(i)
            TransactionEntity(
                id = 0, // local id is assigned fresh by Room on insert; cloudId is the real identity
                type = obj.getString("type"),
                amount = obj.getDouble("amount"),
                note = obj.getString("note"),
                dateTimeMillis = obj.getLong("dateTimeMillis"),
                updatedAtMillis = obj.optLong("updatedAtMillis", 0L),
                cloudId = obj.optString("cloudId").ifBlank { null }
            )
        }

        val catArray = root.optJSONArray("categories") ?: JSONArray()
        val categoryNames = (0 until catArray.length()).map { catArray.getJSONObject(it).getString("name") }

        val deletedArray = root.optJSONArray("deletedIds") ?: JSONArray()
        val deletedIds = (0 until deletedArray.length()).map { deletedArray.getString(it) }.toSet()

        return ParsedBackup(transactions, categoryNames, deletedIds)
    }
}
