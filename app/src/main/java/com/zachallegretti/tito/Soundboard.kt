package com.zachallegretti.tito

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.zachallegretti.tito.ui.theme.TitoTheme
import java.io.IOException


@Composable
    fun SoundboardScreen(navController: NavController,
                         soundboardViewModel: SoundboardViewModel = viewModel() // Get ViewModel instance
) {
    val context = LocalContext.current
    val startAudioFileSelection = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flag)
                val fileName = getFileName(context, it) ?: "User Audio"
                soundboardViewModel.addUserAudioFile(fileName, uri)
            }
        }
    )
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is the Soundboard!")
            Button(onClick = { navController.popBackStack() }) { Text("Back to Main Menu") }
            Button(onClick = { startAudioFileSelection.launch(arrayOf("audio/*")) }) { Text("Add your own sound!") }

            PopulateAudioFiles(soundboardViewModel)
        }
    }

    @Composable
    fun PopulateAudioFiles(soundboardViewModel: SoundboardViewModel) {
        val context = LocalContext.current
        val mediaPlayer = remember { MediaPlayer() }
        val audioFiles by soundboardViewModel.audioFiles.collectAsState()
        for (audioFile in audioFiles) {
            Button(onClick = { playAudio(context, mediaPlayer, audioFile) }) {
                Text(audioFile.name)
            }

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SoundboardScreenPreview() {
        TitoTheme {
            val previewNavController = rememberNavController()
            SoundboardScreen(navController = previewNavController)
        }
    }


fun playAudio(context: Context, mediaPlayer: MediaPlayer, audioFile: AudioFile) {
    try {
        mediaPlayer.reset()

        when (audioFile.type) {
            AudioSourceType.RAW_RESOURCE -> {
                audioFile.resourceId?.let { resId ->
                    context.resources.openRawResourceFd(resId)?.use { afd ->
                        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    } ?: throw IOException("Raw resource file descriptor is null for ID: $resId")
                } ?: throw IllegalArgumentException("Missing resourceId for RAW_RESOURCE type")
            }
            AudioSourceType.FILE_URI -> {
                audioFile.uri?.let { fileUri ->
                    mediaPlayer.setDataSource(context, fileUri)
                } ?: throw IllegalArgumentException("Missing URI for FILE_URI type")
            }
        }

        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error
    }
}
