package com.saving.app.data.sync

import android.content.Context

/**
 * Deleting a record locally removes it from Room, but that alone isn't enough for sync:
 * if we just upload "everything currently in my database", the other device's next upload
 * would silently bring the deleted record back (it doesn't know it was deleted).
 *
 * So every deletion is recorded here as a small permanent "tombstone" key (e.g. "txn:<cloudId>"
 * or "cat:<name>"). This set is included in every backup upload, and merged with whatever
 * tombstones come down from Drive, so both devices eventually agree on what's been deleted.
 */
class TombstoneStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("saving_sync", Context.MODE_PRIVATE)
    private val KEY = "deleted_ids"

    fun getDeletedIds(): Set<String> = prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    fun addDeletedId(id: String) {
        val updated = getDeletedIds().toMutableSet()
        updated.add(id)
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun mergeDeletedIds(remoteIds: Set<String>) {
        val updated = getDeletedIds().toMutableSet()
        updated.addAll(remoteIds)
        prefs.edit().putStringSet(KEY, updated).apply()
    }
}
