package com.stockflow.app.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copies a picked photo into the app's private storage so the image
 * keeps working after the app restarts (picker links can expire).
 */
object ImageStorage {

    private const val FOLDER = "product_images"

    /** Returns the saved file's URI as a String, or null if the copy failed. */
    suspend fun copyToAppStorage(context: Context, source: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val folder = File(context.filesDir, FOLDER).apply { mkdirs() }
                val target = File(folder, "${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(source)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null
                Uri.fromFile(target).toString()
            } catch (e: Exception) {
                null
            }
        }

    /** Deletes an image that this app saved earlier. Other URIs are ignored. */
    fun deleteImage(context: Context, imageUri: String?) {
        if (imageUri == null) return
        val path = Uri.parse(imageUri).path ?: return
        val file = File(path)
        val folder = File(context.filesDir, FOLDER)
        if (file.parentFile == folder) file.delete()
    }
}
