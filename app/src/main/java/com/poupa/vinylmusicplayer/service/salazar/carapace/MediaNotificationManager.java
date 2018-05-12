/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.poupa.vinylmusicplayer.service.salazar.carapace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.service.salazar.ExoMusicService;
import com.poupa.vinylmusicplayer.service.salazar.utils.LogHelper;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;

/**
 * Keeps track of a notification and updates it automatically for a given
 * MediaSession. Maintaining a visible notification (usually) guarantees that the music service
 * won't be killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final String TAG = LogHelper.makeLogTag(MediaNotificationManager.class);

    private static final String CHANNEL_ID = "com.shuttle.MUSIC_CHANNEL_ID";

    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;

    public static final String ACTION_PLAY_PAUSE = "com.shuttle.play_pause";
    public static final String ACTION_PREV = "com.shuttle.prev";
    public static final String ACTION_NEXT = "com.shuttle.next";
    public static final String ACTION_STOP = "com.shuttle.stop";
    public static final String ACTION_STOP_CASTING = "com.shuttle.stop_cast";

    private final MediaBrowserServiceCompat service;
    private MediaSessionCompat.Token sessionToken;
    private MediaControllerCompat controller;
    private MediaControllerCompat.TransportControls transportControls;

    private PlaybackStateCompat playbackState;
    private MediaMetadataCompat metadata;

    private final NotificationManager notificationManager;

    private final PendingIntent playPauseIntent;
    private final PendingIntent previousIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent stopIntent;

    private final PendingIntent stopCastIntent;

//    private final int notificationColor;

    private boolean started = false;

    public MediaNotificationManager(MediaBrowserServiceCompat service) throws RemoteException {
        this.service = service;
        updateSessionToken();

//        notificationColor = ResourceHelper.getThemeColor(service, R.attr.colorPrimary,
//                Color.DKGRAY);

        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        String pkg = service.getPackageName();
        playPauseIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_PLAY_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        previousIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        nextIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        stopIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        stopCastIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
                new Intent(ACTION_STOP_CASTING).setPackage(pkg),
                PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        notificationManager.cancelAll();
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification() {
        if (!started) {
            controller.registerCallback(cb);
            metadata = controller.getMetadata();
            playbackState = controller.getPlaybackState();

            // The notification must be updated after setting started to true
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_NEXT);
                filter.addAction(ACTION_PLAY_PAUSE);
                filter.addAction(ACTION_PREV);
                filter.addAction(ACTION_STOP_CASTING);
                service.registerReceiver(this, filter);

                service.startForeground(NOTIFICATION_ID, notification);
                started = true;
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (started) {
            started = false;
            controller.unregisterCallback(cb);
            try {
                notificationManager.cancel(NOTIFICATION_ID);
                service.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            service.stopForeground(true);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LogHelper.d(TAG, "Received intent with action " + action);
        switch (action) {
            case ACTION_PLAY_PAUSE:
                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    transportControls.pause();
                } else {
                    transportControls.play();
                }
                break;
            case ACTION_NEXT:
                transportControls.skipToNext();
                break;
            case ACTION_PREV:
                transportControls.skipToPrevious();
                break;
            case ACTION_STOP_CASTING:
                Intent i = new Intent(context, ExoMusicService.class);
                i.setAction(ExoMusicService.ACTION_CMD);
                i.putExtra(ExoMusicService.CMD_NAME, ExoMusicService.CMD_STOP_CASTING);
                service.startService(i);
                break;
            default:
                LogHelper.w(TAG, "Unknown intent ignored. Action=", action);
        }
    }

    /**
     * Update the state based on a change on the session token. Called either when
     * we are running for the first time or when the media session owner has destroyed the session
     * (see {@link android.media.session.MediaController.Callback#onSessionDestroyed()})
     */
    private void updateSessionToken() throws RemoteException {
        MediaSessionCompat.Token freshToken = service.getSessionToken();
        if (sessionToken == null && freshToken != null ||
                sessionToken != null && !sessionToken.equals(freshToken)) {
            if (controller != null) {
                controller.unregisterCallback(cb);
            }
            sessionToken = freshToken;
            if (sessionToken != null) {
                controller = new MediaControllerCompat(service, sessionToken);
                transportControls = controller.getTransportControls();
                if (started) {
                    controller.registerCallback(cb);
                }
            }
        }
    }

    private PendingIntent createContentIntent(MediaDescriptionCompat description) {
        Intent openUI = new Intent(service, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        openUI.putExtra(MainActivity.EXTRA_START_FULLSCREEN, true);
//        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description);
//        }
        return PendingIntent.getActivity(service, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private final MediaControllerCompat.Callback cb = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            playbackState = state;
            LogHelper.d(TAG, "Received new playback state", state);
            if (state.getState() == PlaybackStateCompat.STATE_STOPPED ||
                    state.getState() == PlaybackStateCompat.STATE_NONE) {
                stopNotification();
            } else {
                Notification notification = createNotification();
                if (notification != null) {
                    notificationManager.notify(NOTIFICATION_ID, notification);
                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat newMetadata) {
            metadata = newMetadata;
            LogHelper.d(TAG, "Received new metadata ", newMetadata);
            Notification notification = createNotification();
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            LogHelper.d(TAG, "Session was destroyed, resetting to the new session token");
            try {
                updateSessionToken();
            } catch (RemoteException e) {
                LogHelper.e(TAG, e, "could not connect media controller");
            }
        }
    };

    private Notification createNotification() {
        LogHelper.d(TAG, "updateNotificationMetadata. metadata=" + metadata);
        if (metadata == null || playbackState == null) {
            return null;
        }

        MediaDescriptionCompat description = metadata.getDescription();

        String fetchArtUrl = null;
        Bitmap art = null;
        if (description.getIconUri() != null) {
            String artUrl = description.getIconUri().toString();
            fetchArtUrl = artUrl;
            // use a placeholder art while the remote art is being downloaded
            art = BitmapFactory.decodeResource(service.getResources(),
                    R.drawable.default_album_art);
        }

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(service, CHANNEL_ID);

//        final int playPauseButtonPosition = addActions(notificationBuilder);
        boolean isPlaying = controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
        notificationBuilder
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(sessionToken))
                .addAction(
                        R.drawable.ic_skip_previous_white_24dp,
                        service.getString(R.string.action_previous),
                        previousIntent
                )
                .addAction(
                        isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp,
                        service.getString(isPlaying ? R.string.action_play_pause : R.string.action_play),
                        playPauseIntent
                )
                .addAction(
                        R.drawable.ic_skip_next_white_24dp,
                        service.getString(R.string.action_next),
                        nextIntent
                )
                // TODO: 04/02/2018 add favorite to notification
//                .addAction(
//                        isPlaying ? R.drawable.ic_favorite_24dp_scaled : R.drawable.ic_favorite_border_24dp_scaled,
//                        service.getString(R.string.fav_add),
//                        pauseIntent
//                )
                .setDeleteIntent(stopIntent)
//                .setColor(notificationColor)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(art);

        if (controller != null && controller.getExtras() != null) {
            String castName = controller.getExtras().getString(ExoMusicService.EXTRA_CONNECTED_CAST);
            if (castName != null) {
                String castInfo = "R.string.casting_to_device" + castName;
                notificationBuilder.setSubText(castInfo);
                notificationBuilder.addAction(R.drawable.ic_close_white_24dp,
                        "R.string.stop_casting", stopCastIntent);
            }
        }

        setNotificationPlaybackState(notificationBuilder);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl, notificationBuilder);
        }

        return notificationBuilder.build();
    }

    private int addActions(final NotificationCompat.Builder notificationBuilder) {
        LogHelper.d(TAG, "updatePlayPauseAction");

        int playPauseButtonPosition = 0;
        // If skip to previous action is enabled
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_previous_white_24dp,
                    service.getString(R.string.action_previous), previousIntent);

            // If there is a "skip to previous" button, the play/pause button will
            // be the second one. We need to keep track of it, because the MediaStyle notification
            // requires to specify the index of the buttons (actions) that should be visible
            // when in compact view.
            playPauseButtonPosition = 1;
        }

        // Play or pause button, depending on the current state.
        final String label;
        final int icon;
        final PendingIntent intent;
        if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            label = service.getString(R.string.action_play_pause);
            icon = R.drawable.ic_pause_white_24dp;
            intent = playPauseIntent;
        } else {
            label = service.getString(R.string.action_play_pause);
            icon = R.drawable.ic_play_arrow_white_24dp;
            intent = playPauseIntent;
        }
        notificationBuilder.addAction(new NotificationCompat.Action(icon, label, intent));

        // If skip to next action is enabled
        if ((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(R.drawable.ic_skip_next_white_24dp,
                    service.getString(R.string.action_play_next), nextIntent);
        }

        return playPauseButtonPosition;
    }

    private void setNotificationPlaybackState(NotificationCompat.Builder builder) {
        LogHelper.d(TAG, "updateNotificationPlaybackState. playbackState=" + playbackState);
        if (playbackState == null || !started) {
            LogHelper.d(TAG, "updateNotificationPlaybackState. cancelling notification!");
            service.stopForeground(true);
            return;
        }

        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    private void fetchBitmapFromURLAsync(final String bitmapUrl,
                                         final NotificationCompat.Builder builder) {

        Glide.with(service).asBitmap().load(bitmapUrl).into(
                new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        LogHelper.d(TAG, "fetchBitmapFromURLAsync: set bitmap to ", bitmapUrl);
                        builder.setLargeIcon(resource);
                        notificationManager.notify(NOTIFICATION_ID, builder.build());
                    }
                }
        );
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            //TODO add cast string cast_notification_default_channel_name
                            service.getString(android.R.string.unknownName),
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(
                            //TODO add cast string cast_notification_default_channel_name
                    service.getString(android.R.string.unknownName));

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
