package com.deva.statussaver.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class FileSaver(private val context: Context) {

    suspend fun saveStatus(status: Status): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // Prepare content values
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, status.name)
                put(MediaStore.MediaColumns.MIME_TYPE, if (status.isVideo) "video/mp4" else "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/StatusSaver")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                } else {
                    // Legacy for < Android 10 (though manifest says minSdk 24, target 34)
                    // We might rely on Scoped Storage being enforced so RELATIVE_PATH is good.
                }
            }

            val collection = if (status.isVideo) {
                 MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                 MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }

            // Check for duplicates (simple check by display name)
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(status.name)

            contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.count > 0) {
                    // Already exists
                    return@withContext false
                }
            }

            val destUri = contentResolver.insert(collection, values) ?: return@withContext false

            contentResolver.openOutputStream(destUri)?.use { outputStream ->
                contentResolver.openInputStream(status.uri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(destUri, values, null, null)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
