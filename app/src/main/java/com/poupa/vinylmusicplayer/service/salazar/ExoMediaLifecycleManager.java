package com.poupa.vinylmusicplayer.service.salazar;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ComponentName;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import com.simplecity.amp_library.playback.MediaManagerLifecycle;
import org.jetbrains.annotations.NotNull;


/**
 * Created by fxsalazar
 * 27/01/2018.
 */
public final class ExoMediaLifecycleManager implements LifecycleObserver, MediaManagerLifecycle {
    private static final String TAG = ExoMediaLifecycleManager.class.getSimpleName();

    private MediaManager mediaManager = new ExoMediaManager();
    @NonNull
    private MediaBrowserCompat mediaBrowser;
    @NonNull
    private FragmentActivity activity;
    @NonNull
    private final Callback mediaManagerCallback;
    @Nullable
    private MediaControllerCompat.Callback mediaControllerCallback;
    private Class<? extends MediaBrowserServiceCompat> mediaServiceClass;

    private final MediaBrowserCompat.ConnectionCallback connectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mediaBrowser.getSessionToken());
                        mediaManager.setMediaControllerCompat(getMediaController());
                        mediaManagerCallback.onServiceConnected();
                    } catch (RemoteException e) {
                        Log.e(TAG, "could not connect media controller", e);
                        mediaManagerCallback.onServiceConnectionError(new Exception("could not connect media controller", e));
                    }
                }

                @Override
                public void onConnectionSuspended() {
                    mediaManagerCallback.onServiceConnectionSuspended();
                }

                @Override
                public void onConnectionFailed() {
                    mediaManagerCallback.onServiceConnectionError(new Exception("MediaBrowser connection FAILED"));
                }
            };

    public ExoMediaLifecycleManager(@NonNull FragmentActivity activity,
                                  @NonNull Callback mediaManagerCallback,
                                  @NonNull Class<? extends MediaBrowserServiceCompat> mediaServiceClass) {
        this.activity = activity;
        this.mediaManagerCallback = mediaManagerCallback;
        this.mediaServiceClass = mediaServiceClass;
        activity.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void onCreate() {
        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mediaBrowser = new MediaBrowserCompat(
                this.activity,
                new ComponentName(this.activity, mediaServiceClass), connectionCallback, null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onStart() {
        mediaBrowser.connect();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onStop() {
        mediaBrowser.disconnect();
        MediaControllerCompat mediaController = getMediaController();
        if (mediaController != null && mediaControllerCallback != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat.setMediaController(this.activity, new MediaControllerCompat(this.activity, token));
    }

    @Nullable
    private MediaControllerCompat getMediaController() {
        return MediaControllerCompat.getMediaController(this.activity);
    }

    public void registerMediaControllerCallback(@NonNull MediaControllerCompat.Callback mediaControllerCallback) {
        this.mediaControllerCallback = mediaControllerCallback;
        MediaControllerCompat mediaController = getMediaController();
        if (mediaController != null && this.mediaBrowser.isConnected()) {
            mediaController.registerCallback(this.mediaControllerCallback);
        }
    }
}
