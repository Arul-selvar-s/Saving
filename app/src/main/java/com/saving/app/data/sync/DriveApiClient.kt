package com.saving.app.data.sync

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Talks directly to the Drive v3 REST API using the app's hidden "appDataFolder" — a space
 * only this app can see or write to (the user never sees these files in their normal Drive).
 * All data lives in a single file: saving_backup.json
 */
class DriveApiClient(private val accessToken: String) {

    private val client = OkHttpClient()
    private val fileName = "saving_backup.json"

    fun findFileId(): String? {
        val query = URLEncoder.encode("name='$fileName' and trashed=false", "UTF-8")
        val url = "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id,name)"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val files = JSONObject(body).optJSONArray("files") ?: return null
            return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
        }
    }

    fun uploadOrUpdate(json: String) {
        val existingId = findFileId()
        if (existingId != null) {
            updateExisting(existingId, json)
        } else {
            createNew(json)
        }
    }

    private fun updateExisting(fileId: String, json: String) {
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
            .addHeader("Authorization", "Bearer $accessToken")
            .patch(body)
            .build()
        client.newCall(request).execute().close()
    }

    private fun createNew(json: String) {
        val metadata = JSONObject().apply {
            put("name", fileName)
            put("parents", JSONArray().put("appDataFolder"))
        }
        val boundary = "saving_backup_boundary"
        val multipartContent = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata.toString())
            append("\r\n--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
            append(json)
            append("\r\n--$boundary--")
        }
        val body = multipartContent.toRequestBody("multipart/related; boundary=$boundary".toMediaType())
        val request = Request.Builder()
            .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
            .addHeader("Authorization", "Bearer $accessToken")
            .post(body)
            .build()
        client.newCall(request).execute().close()
    }

    fun download(): String? {
        val fileId = findFileId() ?: return null
        val request = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }
}
