package com.poupa.vinylmusicplayer.service.salazar.utils

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.poupa.vinylmusicplayer.R.string.song
import com.poupa.vinylmusicplayer.model.Song
import com.poupa.vinylmusicplayer.util.MusicUtil

/**
 * Created by fxsalazar
 * 12/05/2018.
 */

fun MediaSessionCompat.QueueItem.toSong(): Song {
    val desc = this.description
    return Song(queueId.toInt(),desc.title.toString(),0,0,0,"",
            0,0,desc.subtitle.toString(),0,desc.subtitle.toString())
}

fun List<MediaSessionCompat.QueueItem>.toSong(): Collection<Song> {
    return fold(mutableListOf()) { acc, queueItem ->
        acc.add(queueItem.toSong())
        return acc
    }
}

fun MediaMetadataCompat.toSong(): Song {
    return this.description.toSong()
}

fun MediaDescriptionCompat.toSong(): Song {
    return Song(0,title.toString(),0,0,0,"",
            0,0,subtitle.toString(),0,subtitle.toString())
}

fun Song.toMediaDescriptionCompat(): MediaDescriptionCompat {
    val toString = MusicUtil.getSongFileUri(id).toString()
    return MediaDescriptionCompat.Builder()
            .setMediaId(id.toString())
            .setTitle(title)
            .setSubtitle("$artistName - $albumName")
            .setMediaUri(Uri.parse(toString))
            .build()
}