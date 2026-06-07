package com.example.tomabooks

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider

class BookWidget : GlanceAppWidget() {
    // Define where the state comes from
    override val stateDefinition = androidx.glance.state.PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read the current state from preferences
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val isPlaying = prefs[booleanPreferencesKey("is_playing")] ?: false
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(if (isPlaying) Color.Red else Color.Green)
                    .clickable(
                        actionStartService(
                            Intent(context, PlaybackService::class.java).apply {
                                action = "ACTION_TOGGLE"
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPlaying) "PAUSE" else "PLAY",
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class BookWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BookWidget()
}
