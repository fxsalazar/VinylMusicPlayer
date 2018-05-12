package com.poupa.vinylmusicplayer.ui.activities.base;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.service.salazar.ExoMediaLifecycleManager;
import com.poupa.vinylmusicplayer.service.salazar.ExoMusicService;
import com.simplecity.amp_library.playback.MediaManagerLifecycle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMusicServiceActivity extends AbsBaseActivity implements MediaManagerLifecycle.Callback, MusicServiceEventListener {
    public static final String TAG = AbsMusicServiceActivity.class.getSimpleName();

    private final ArrayList<MediaManagerLifecycle.Callback> mMusicServiceEventListeners = new ArrayList<>();

    private ExoMediaLifecycleManager exoMediaLifecycleManager;
    protected MediaControllerCompat mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exoMediaLifecycleManager = new ExoMediaLifecycleManager(
                this,
                this,
                ExoMusicService.class
        );

        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied));
    }

    public void addMusicServiceEventListener(final MediaManagerLifecycle.Callback listener) {
        if (listener != null) {
            mMusicServiceEventListeners.add(listener);
        }
    }

    public void removeMusicServiceEventListener(final MediaManagerLifecycle.Callback listener) {
        if (listener != null) {
            mMusicServiceEventListeners.remove(listener);
        }
    }

    @Override
    public void onServiceConnected() {
        exoMediaLifecycleManager.registerMediaControllerCallback(this.mediaControllerCallback);
        this.mediaController = MediaControllerCompat.getMediaController(this);
        for (MediaManagerLifecycle.Callback listener : mMusicServiceEventListeners) {
            if (listener != null) {
                listener.onServiceConnected();
            }
        }
    }

    @Override
    public void onServiceConnectionSuspended() {
        for (MediaManagerLifecycle.Callback listener : mMusicServiceEventListeners) {
            if (listener != null) {
                listener.onServiceConnectionSuspended();
            }
        }
    }

    @Override
    public void onServiceConnectionError(@NotNull Exception exception) {
        for (MediaManagerLifecycle.Callback listener : mMusicServiceEventListeners) {
            if (listener != null) {
                listener.onServiceConnectionError(exception);
            }
        }
    }

    @Override
    protected void onHasPermissionsChanged(boolean hasPermissions) {
        super.onHasPermissionsChanged(hasPermissions);
        Intent intent = new Intent(MusicService.MEDIA_STORE_CHANGED);
        intent.putExtra("from_permissions_changed", true); // just in case we need to know this at some point
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    protected String[] getPermissionsToRequest() {
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            AbsMusicServiceActivity.this.onPlayStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            AbsMusicServiceActivity.this.onPlayingMetaChanged(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
             AbsMusicServiceActivity.this.onQueueChanged(queue);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            AbsMusicServiceActivity.this.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            AbsMusicServiceActivity.this.onShuffleModeChanged(shuffleMode);
        }
    };

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {

    }

    @Override
    public void onPlayingMetaChanged(MediaMetadataCompat metadata) {

    }

    @Override
    public void onPlayStateChanged(PlaybackStateCompat state) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeChanged(int shuffleMode) {

    }

    public void onMediaStoreChanged() {
        // TODO: 12/05/2018 impl and remove
    }
}
