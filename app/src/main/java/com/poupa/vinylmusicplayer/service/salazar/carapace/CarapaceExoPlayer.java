package com.poupa.vinylmusicplayer.service.salazar.carapace;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public interface CarapaceExoPlayer extends ExoPlayer {
    void unDuckVolume();

    void duckVolume();
}
