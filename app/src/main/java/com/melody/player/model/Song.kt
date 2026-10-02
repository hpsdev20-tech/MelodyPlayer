package com.melody.player.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val dateAdded: Long,
    val trackNumber: Int,
    val uri: Uri
)
