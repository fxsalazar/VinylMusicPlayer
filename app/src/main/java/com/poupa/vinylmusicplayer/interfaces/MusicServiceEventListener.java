package com.poupa.vinylmusicplayer.interfaces;

import android.support.annotation.CallSuper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface MusicServiceEventListener {
//    void onServiceConnected();
//
//    void onServiceDisconnected();

    void onQueueChanged(List<MediaSessionCompat.QueueItem> queue);

    void onPlayingMetaChanged(MediaMetadataCompat metadata);

    void onPlayStateChanged(PlaybackStateCompat state);

    void onRepeatModeChanged(int repeatMode);

    void onShuffleModeChanged(int shuffleMode);

//    void onMediaStoreChanged();
}
