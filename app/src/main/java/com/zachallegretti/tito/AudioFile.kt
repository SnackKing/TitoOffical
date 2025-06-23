package com.zachallegretti.tito

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Sealed class to clearly define the source type of an [AudioFile].
 */
sealed class AudioSourceType {
    /** Indicates the audio file is a built-in resource (from res/raw). */
    object RAW_RESOURCE : AudioSourceType()
    /** Indicates the audio file is from device local storage (user-uploaded). */
    object FILE_URI : AudioSourceType()
}

data class AudioFile(
    val id: String,
    val name: String,
    val type: AudioSourceType,
    val resourceId: Int? = null,
    val uri: Uri? = null
)

// Helper function to get file name from URI
fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result
}


object AppAudioFiles {
    val defaultSoundboardFiles = listOf(
        AudioFile("thats_so_daddy","That's so daddy", AudioSourceType.RAW_RESOURCE, R.raw.thats_so_daddy),
        AudioFile("bum_bum_tito", "Bum Bum Tito", AudioSourceType.RAW_RESOURCE, R.raw.bum_bum_tito)
    )
}