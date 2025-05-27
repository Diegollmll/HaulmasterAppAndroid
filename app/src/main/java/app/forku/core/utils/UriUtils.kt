package app.forku.core.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun Uri.toFile(context: Context): File {
    return when (scheme) {
        "file" -> File(path!!)
        "content" -> {
            val inputStream: InputStream? = context.contentResolver.openInputStream(this)
            // Try to get the original file extension from the content resolver
            val extension = context.contentResolver.getType(this)?.let { mimeType ->
                android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            } ?: run {
                // Try to get extension from the display name
                val cursor = context.contentResolver.query(this, null, null, null, null)
                val ext = cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (it.moveToFirst() && nameIndex != -1) {
                        val name = it.getString(nameIndex)
                        name.substringAfterLast('.', "jpg")
                    } else "jpg"
                } ?: "jpg"
                ext
            }
            val file = File.createTempFile("upload_", ".${extension}", context.cacheDir)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        }
        else -> throw IllegalArgumentException("Unsupported URI scheme: $scheme")
    }
} 