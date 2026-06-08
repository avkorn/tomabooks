package com.example.tomabooks

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size

class BookWidget : GlanceAppWidget() {
    // Define where the state comes from
    override val stateDefinition = androidx.glance.state.PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color.Blue)
                    .clickable(actionStartActivity(MainActivity::class.java)), // Launches the app
                contentAlignment = Alignment.Center
            ) {
//                Text(
//                    text = "Open App",
//                    style = TextStyle(
//                        color = ColorProvider(day = Color.White, night = Color.White),
//                        fontWeight = FontWeight.Bold
//                    )
//                )
                // Displays the app icon
                Image(
                    provider = ImageProvider(R.mipmap.ic_launcher),
                    contentDescription = "Open TomaBooks",
                    modifier = GlanceModifier
                        .size(64.dp) // Adjust the size as needed
                )
            }
        }
    }
}

class BookWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BookWidget()
}
