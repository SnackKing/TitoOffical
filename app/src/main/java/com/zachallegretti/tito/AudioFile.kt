package com.zachallegretti.tito

data class AudioFile(
    val name: String,
    val resourceId: Int
)

object AppAudioFiles {
    val soundboardFiles = listOf(
        AudioFile("That's so daddy", R.raw.thats_so_daddy),
        AudioFile("Bum Bum Tito", R.raw.bum_bum_tito)
    )
}