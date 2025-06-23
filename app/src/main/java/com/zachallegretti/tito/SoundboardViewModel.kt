package com.zachallegretti.tito

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import com.zachallegretti.tito.AppAudioFiles.defaultSoundboardFiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SoundboardViewModel(application: Application) : AndroidViewModel(application) {

    private val USER_AUDIO_URIS_KEY = "user_audio_uris"


    private val audioFilesFlow=  MutableStateFlow<List<AudioFile>>(emptyList())
    val audioFiles = audioFilesFlow.asStateFlow()

    private val sharedPreferences = application.getSharedPreferences("user_audio_files", Context.MODE_PRIVATE)

    init {
        // Load pre-recorded sounds
        audioFilesFlow.value = AppAudioFiles.defaultSoundboardFiles
        loadUserAudioFiles()
    }

    fun addUserAudioFile(fileName: String, uri: Uri) {
        val newAudioFile = AudioFile(id = fileName,
            name = fileName,
            type = AudioSourceType.FILE_URI,
            uri = uri
            )
        audioFilesFlow.value += newAudioFile
    }

    private fun loadUserAudioFiles() {
        val savedUriStrings = sharedPreferences.getStringSet(USER_AUDIO_URIS_KEY, emptySet()) ?: emptySet()
        val userFiles = savedUriStrings.mapNotNull { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val name = getFileName(getApplication(), uri) ?: "User audio file"
                AudioFile(UUID.randomUUID().toString(), name, AudioSourceType.FILE_URI, uri = uri)
            } catch (e: Exception) {
                null
            }
        }
        audioFilesFlow.value = defaultSoundboardFiles + userFiles
    }
    // Helper function to get file name from URI
    private fun getFileName(context: Context, uri: Uri): String? {
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
        // Remove file extension for cleaner display
        return result?.substringBeforeLast('.') ?: result
    }
}