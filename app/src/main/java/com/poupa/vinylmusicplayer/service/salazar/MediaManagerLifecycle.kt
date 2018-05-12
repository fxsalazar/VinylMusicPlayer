package com.simplecity.amp_library.playback

import android.arch.lifecycle.LifecycleObserver
import com.poupa.vinylmusicplayer.service.salazar.MediaManager

/**
 * Created by fxsalazar
 * 05/05/2018.
 */
interface MediaManagerLifecycle : LifecycleObserver {
    interface Callback {
        fun onMediaManagerConnected()

        fun onMediaManagerConnectionSuspended()

        fun onMediaManagerConnectionError(exception: Exception)
    }

    val mediaManager : MediaManager
}
