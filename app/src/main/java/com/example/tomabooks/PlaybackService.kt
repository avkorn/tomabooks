package com.example.tomabooks

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val scope = MainScope()

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Update the widget whenever playback state changes
                updateWidgetState(isPlaying)
            }
        })
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_TOGGLE") {
            val player = mediaSession?.player
            if (player?.isPlaying == true) {
                player.pause()
                updateWidgetState(false)
            } else {
                player?.play()
                updateWidgetState(true)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        // If music is playing, don't stop the service, just let it continue
        if (player?.isPlaying == true) {
            return
        }
        // If not playing, we stop it to save battery
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    private fun updateWidgetState(isPlaying: Boolean) {
        val context = this
        CoroutineScope(Dispatchers.IO).launch {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(BookWidget::class.java)

            glanceIds.forEach { glanceId ->
                // FIX: Use .toMutablePreferences() and ensure the lambda returns the prefs object
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[booleanPreferencesKey("is_playing")] = isPlaying
                    } // .apply returns the MutablePreferences object, satisfying the expected return type
                }

                // Redraw the widget
                BookWidget().update(context, glanceId)
            }
        }
    }


}
