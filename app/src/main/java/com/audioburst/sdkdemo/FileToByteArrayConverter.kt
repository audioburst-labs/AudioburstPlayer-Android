package com.audioburst.sdkdemo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

class FileToByteArrayConverter(private val context: Context) {

    suspend fun convert(file: File): ByteArray =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                context.contentResolver.openInputStream(Uri.fromFile(file))?.use { inputStream ->
                    continuation.resume(inputStream.readBytes())
                    file.deleteOnExit()
                }
            }
        }
}