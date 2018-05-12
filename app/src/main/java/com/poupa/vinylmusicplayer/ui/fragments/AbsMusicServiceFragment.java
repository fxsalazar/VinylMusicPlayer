package com.poupa.vinylmusicplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.View;

import com.poupa.vinylmusicplayer.interfaces.MusicServiceEventListener;
import com.poupa.vinylmusicplayer.ui.activities.base.AbsMusicServiceActivity;
import com.simplecity.amp_library.playback.MediaManagerLifecycle;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsMusicServiceFragment extends Fragment implements MediaManagerLifecycle.Callback {
    private AbsMusicServiceActivity activity;
    private MediaControllerCompat.Callback musicServiceCallback;
    protected MediaControllerCompat mediaController;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (AbsMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + AbsMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.addMusicServiceEventListener(this);
        this.musicServiceCallback = registerMusicServiceCallback();
    }

    @NonNull
    protected abstract MediaControllerCompat.Callback registerMusicServiceCallback();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mediaController.unregisterCallback(musicServiceCallback);
    }

    @Override
    public void onServiceConnected() {
        mediaController = MediaControllerCompat.getMediaController(activity);
        mediaController.registerCallback(musicServiceCallback);
    }

    @Override
    public void onServiceConnectionSuspended() {
        mediaController.unregisterCallback(musicServiceCallback);
    }

    @Override
    public void onServiceConnectionError(@NotNull Exception exception) {
        // TODO: 12/05/2018 what if???
    }
}
