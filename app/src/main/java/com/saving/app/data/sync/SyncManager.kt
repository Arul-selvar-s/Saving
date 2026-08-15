package com.saving.app.data.sync

import android.content.Context
import com.saving.app.auth.DriveAuth
import com.saving.app.data.repository.SavingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class SyncManager(
    private val context: Context,
    private val repository: SavingRepository
) {
    private val tombstoneStore = TombstoneStore(context)

    fun recordTransactionDeleted(cloudId: String?) {
        if (cloudId == null) return
        tombstoneStore.addDeletedId("txn:$cloudId")
    }

    fun recordCategoryDeleted(name: String) {
        tombstoneStore.addDeletedId("cat:$name")
    }

    private suspend fun currentAccessToken(): String? {
        val account = DriveAuth.getLastSignedInAccount(context) ?: return null
        return DriveAuth.getAccessToken(context, account)
    }

    /** Uploads the full current local state (best-effort, silent on failure — a background
     *  sync shouldn't interrupt the person adding a transaction). Call this after every
     *  add/edit/delete so changes reach the cloud immediately, per the app's primary rule. */
    suspend fun pushSnapshot() = withContext(Dispatchers.IO) {
        val token = currentAccessToken() ?: return@withContext
        try {
            // Backfill a cloudId for any older local transaction that predates sync being enabled,
            // so it participates correctly in merging instead of being invisible to it.
            val transactions = repository.transactions.first().map { tx ->
                if (tx.cloudId == null) {
                    val withCloudId = tx.copy(cloudId = UUID.randomUUID().toString())
                    repository.updateTransaction(withCloudId)
                    withCloudId
                } else tx
            }
            val categories = repository.categories.first()
            val json = BackupSerializer.serialize(transactions, categories, tombstoneStore.getDeletedIds())
            DriveApiClient(token).uploadOrUpdate(json)
        } catch (e: Exception) {
            // Best-effort background sync — a failed push here doesn't lose local data,
            // it just gets retried on the next save or manual "Sync Now".
        }
    }

    /** Downloads the latest snapshot from Drive, merges it into the local database
     *  (newer edits win, remote deletions are applied locally), then re-uploads the
     *  merged result so both devices converge. */
    suspend fun pullAndMerge() = withContext(Dispatchers.IO) {
        val token = currentAccessToken() ?: return@withContext
        val remoteJson = try {
            DriveApiClient(token).download()
        } catch (e: Exception) {
            null
        } ?: return@withContext

        val parsed = try {
            BackupSerializer.deserialize(remoteJson)
        } catch (e: Exception) {
            return@withContext
        }

        tombstoneStore.mergeDeletedIds(parsed.deletedIds)
        val allDeleted = tombstoneStore.getDeletedIds()

        // Merge categories: add remote-only ones, remove any that are tombstoned
        val localCategories = repository.categories.first()
        val localCategoryNames = localCategories.map { it.name }.toSet()
        parsed.categoryNames.forEach { name ->
            if ("cat:$name" !in allDeleted && name !in localCategoryNames) {
                repository.addCategory(name)
            }
        }
        localCategories.forEach { category ->
            if ("cat:${category.name}" in allDeleted) {
                repository.deleteCategory(category)
            }
        }

        // Merge transactions: add remote-only, update if remote is newer, skip tombstoned
        val localTransactions = repository.transactions.first()
        val localByCloudId = localTransactions.filter { it.cloudId != null }.associateBy { it.cloudId }
        parsed.transactions.forEach { remoteTx ->
            if (remoteTx.cloudId == null || "txn:${remoteTx.cloudId}" in allDeleted) return@forEach
            val local = localByCloudId[remoteTx.cloudId]
            if (local == null) {
                repository.addTransaction(remoteTx)
            } else if (remoteTx.updatedAtMillis > local.updatedAtMillis) {
                repository.updateTransaction(
                    local.copy(
                        type = remoteTx.type,
                        amount = remoteTx.amount,
                        note = remoteTx.note,
                        dateTimeMillis = remoteTx.dateTimeMillis,
                        updatedAtMillis = remoteTx.updatedAtMillis
                    )
                )
            }
        }
        localTransactions.forEach { tx ->
            if (tx.cloudId != null && "txn:${tx.cloudId}" in allDeleted) {
                repository.deleteTransaction(tx)
            }
        }

        // Push the merged result back so Drive reflects the converged state too
        pushSnapshot()
    }
}
