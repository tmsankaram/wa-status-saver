package com.deva.statussaver.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusRepository(private val context: Context) {

    // Cache to avoid re-reading files unnecessarily
    private var cachedStatuses: List<Status>? = null
    private var cacheUri: Uri? = null

    suspend fun getStatuses(treeUri: Uri, forceRefresh: Boolean = false): List<Status> = withContext(Dispatchers.IO) {
        // Return cache if available and not forcing refresh
        if (!forceRefresh && cacheUri == treeUri && cachedStatuses != null) {
            return@withContext cachedStatuses!!
        }

        val statuses = mutableListOf<Status>()

        try {
            // Use ContentResolver query directly for better performance
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex) ?: continue
                    val mimeType = cursor.getString(mimeIndex) ?: ""
                    val lastModified = cursor.getLong(modifiedIndex)

                    // Skip .nomedia and hidden files
                    if (name.startsWith(".")) continue

                    val isVideo = mimeType.startsWith("video/") || name.endsWith(".mp4", ignoreCase = true)
                    val isImage = mimeType.startsWith("image/") ||
                                  name.endsWith(".jpg", ignoreCase = true) ||
                                  name.endsWith(".jpeg", ignoreCase = true) ||
                                  name.endsWith(".png", ignoreCase = true)

                    if (isVideo || isImage) {
                        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                        statuses.add(
                            Status(
                                uri = documentUri,
                                name = name,
                                isVideo = isVideo,
                                timestamp = lastModified
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to DocumentFile if cursor method fails
            return@withContext getStatusesLegacy(treeUri)
        }

        // Sort by newest first
        val sorted = statuses.sortedByDescending { it.timestamp }

        // Update cache
        cachedStatuses = sorted
        cacheUri = treeUri

        sorted
    }

    // Fallback method using DocumentFile
    private fun getStatusesLegacy(treeUri: Uri): List<Status> {
        val statuses = mutableListOf<Status>()
        val documentFile = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()

        documentFile.listFiles().forEach { file ->
            val name = file.name ?: return@forEach
            if (name.startsWith(".")) return@forEach

            val isVideo = name.endsWith(".mp4", ignoreCase = true)
            val isImage = name.endsWith(".jpg", ignoreCase = true) ||
                          name.endsWith(".jpeg", ignoreCase = true) ||
                          name.endsWith(".png", ignoreCase = true)

            if (isVideo || isImage) {
                statuses.add(
                    Status(
                        uri = file.uri,
                        name = name,
                        isVideo = isVideo,
                        timestamp = file.lastModified()
                    )
                )
            }
        }

        return statuses.sortedByDescending { it.timestamp }
    }

    fun clearCache() {
        cachedStatuses = null
        cacheUri = null
    }

    fun getPersistedUri(): Uri? {
        val persistedPermissions = context.contentResolver.persistedUriPermissions
        return persistedPermissions.firstOrNull()?.uri
    }
}
