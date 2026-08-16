package com.saving.app.data.sync

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.saving.app.auth.DriveAccessResult
import com.saving.app.auth.DriveAuth
import com.saving.app.data.repository.SavingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

/** Outcome of a sync attempt, surfaced all the way up to the UI — previously these failures
 *  were silently swallowed, which made real problems (like the device-account issue) invisible. */
sealed class SyncOutcome {
    object Success : SyncOutcome()
    data class NeedsConsent(val intentSender: IntentSender) : SyncOutcome()
    data class Error(val message: String) : SyncOutcome()
}

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

    fun handleAuthorizationResolution(data: Intent?): DriveAccessResult =
        DriveAuth.finishAuthorization(context, data)

    suspend fun pushSnapshot(): SyncOutcome = withContext(Dispatchers.IO) {
        when (val access = DriveAuth.requestDriveAccess(context)) {
            is DriveAccessResult.Authorized -> uploadSnapshot(access.accessToken)
            is DriveAccessResult.NeedsConsent -> SyncOutcome.NeedsConsent(access.intentSender)
            is DriveAccessResult.Failed -> SyncOutcome.Error(access.message)
        }
    }

    suspend fun pullAndMerge(): SyncOutcome = withContext(Dispatchers.IO) {
        when (val access = DriveAuth.requestDriveAccess(context)) {
            is DriveAccessResult.Authorized -> pullAndMergeWithToken(access.accessToken)
            is DriveAccessResult.NeedsConsent -> SyncOutcome.NeedsConsent(access.intentSender)
            is DriveAccessResult.Failed -> SyncOutcome.Error(access.message)
        }
    }

    private suspend fun uploadSnapshot(accessToken: String): SyncOutcome {
        return try {
            // Backfill a cloudId for any older local transaction that predates sync being
            // enabled, so it participates correctly in merging instead of being invisible to it.
            val transactions = repository.transactions.first().map { tx ->
                if (tx.cloudId == null) {
                    val withCloudId = tx.copy(cloudId = UUID.randomUUID().toString())
                    repository.updateTransaction(withCloudId)
                    withCloudId
                } else tx
            }
            val categories = repository.categories.first()
            val json = BackupSerializer.serialize(transactions, categories, tombstoneStore.getDeletedIds())
            DriveApiClient(accessToken).uploadOrUpdate(json)
            SyncOutcome.Success
        } catch (e: Exception) {
            SyncOutcome.Error(e.message ?: "Upload to Drive failed")
        }
    }

    private suspend fun pullAndMergeWithToken(accessToken: String): SyncOutcome {
        return try {
            val remoteJson = DriveApiClient(accessToken).download()
                ?: return uploadSnapshot(accessToken) // nothing backed up yet — push our current state

            val parsed = BackupSerializer.deserialize(remoteJson)

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
            uploadSnapshot(accessToken)
        } catch (e: Exception) {
            SyncOutcome.Error(e.message ?: "Sync failed")
        }
    }
}
