package com.poupa.vinylmusicplayer.service.salazar

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.poupa.vinylmusicplayer.model.Album
import com.poupa.vinylmusicplayer.model.Artist
import com.poupa.vinylmusicplayer.model.Genre
import com.poupa.vinylmusicplayer.model.Song

/**
 * Created by fxsalazar
 * 05/05/2018.
 */
class ExoMediaManager: MediaManager {
    override var mediaControllerCompat: MediaControllerCompat? = null

    override fun playAll(songsSingle: MutableList<Song>, onEmpty: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playAll(songs: MutableList<Song>, position: Int, canClearShuffle: Boolean, onEmpty: (String) -> Unit) {
        songs.first().apply {
            val desc = MediaDescriptionCompat.Builder()
                    .setMediaId(this.id.toString())
                    .setTitle(this.title)
                    .setSubtitle("$this.albumArtistName $this.albumName")
                    .setMediaUri(Uri.parse(this.data))
                    .build()

            val item = MediaSessionCompat.QueueItem(desc, this.artistId.toLong())
            mediaControllerCompat?.addQueueItem(item.description)
            mediaControllerCompat?.transportControls?.play()
        }
    }

    override fun unregisterCallback(callback: MediaControllerCompat.Callback) {
        mediaControllerCompat?.unregisterCallback(callback)
    }
    override fun registerCallback(callback: MediaControllerCompat.Callback) {
        mediaControllerCompat?.registerCallback(callback)
    }

    override fun shuffleAll(songsSingle: List<Song>, onEmpty: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playFile(uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFilePath(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPlaying(): Boolean {
        return false
    }

    override fun getShuffleMode(): Int {
        return 0
    }

    override fun setShuffleMode(mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRepeatMode(): Int {
        return 0
    }

    override fun next() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun previous(allowTrackRestart: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playOrPause() {
        if (isPlaying()){
            mediaControllerCompat?.transportControls?.pause()
        } else {
            mediaControllerCompat?.transportControls?.play()
        }
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlbumArtist(): Artist? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlbum(): Album? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSong(): Song? {
        with (mediaControllerCompat?.metadata?.description){
            return Song(0,this?.title.toString(),0,0,0,"",0,0,
                "",0,this?.subtitle.toString())
        }
    }

    override fun getGenre(): Genre {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPosition(): Long {
        return 0
    }

    override fun getDuration(): Long {
        return 0
    }

    override fun seekTo(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveQueueItem(from: Int, to: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleShuffleMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cycleRepeat() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addToQueue(songs: MutableList<Song>, onAdded: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playNext(songs: MutableList<Song>, onAdded: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setQueuePosition(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearQueue() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getQueue(): MutableList<Song> {
        return mutableListOf(Song.EMPTY_SONG)
    }

    override fun getQueuePosition(): Int {
        return 0
    }

    override fun removeFromQueue(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFromQueue(songs: MutableList<Song>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleFavorite() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun closeEqualizerSessions(internal: Boolean, audioSessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openEqualizerSession(internal: Boolean, audioSessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEqualizer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}