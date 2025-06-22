package com.zachallegretti.tito

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.zachallegretti.tito.ui.theme.TitoTheme


    @Composable
    fun SoundboardScreen(navController: NavController) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This is the Soundboard!")
            Button(onClick = { navController.popBackStack() }) { Text("Back to Main Menu") }
            PopulateAudioFiles()
        }
    }

    @Composable
    fun PopulateAudioFiles() {
        val context = LocalContext.current
        val mediaPlayer = remember { MediaPlayer() }
        for (audioFile in AppAudioFiles.soundboardFiles) {
            Button(onClick = { playAudio(context, mediaPlayer, audioFile.resourceId) }) {
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

fun playAudio(context: Context, mediaPlayer: MediaPlayer, resourceId: Int) {
    try {
        mediaPlayer.reset()
        val afd = context.resources.openRawResourceFd(resourceId) ?: return

        mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (e: Exception) {
        e.printStackTrace()
        // Handle error
    }
}
