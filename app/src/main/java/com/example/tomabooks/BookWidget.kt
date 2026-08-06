package com.example.tomabooks

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class BookWidget : GlanceAppWidget() {
    // This ensures provideContent is recalled when the user resizes the widget
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition = androidx.glance.state.PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val widgetWidth = size.width
            val packageName = LocalContext.current.packageName
            // Adjust the 0.35f multiplier to fit your preferred look.
            val adaptiveTextSize = (widgetWidth.value * 0.36f).sp

            // Get App Version
            val versionName = try {
                context.packageManager.getPackageInfo(packageName, 0).versionName
            } catch (e: Exception) {
                "0.0.0"
            }
            Column(
                modifier = GlanceModifier.fillMaxSize().background(Color.Blue)
                    .clickable(actionStartActivity(MainActivity::class.java)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Giant red clock at the top
                AndroidRemoteViews(
                    remoteViews = RemoteViews(packageName, R.layout.widget_clock).apply {
                        // This tells the XML TextClock to change its text size dynamically
                        setTextViewTextSize(
                            R.id.widget_clock,
                            android.util.TypedValue.COMPLEX_UNIT_SP,
                            adaptiveTextSize.value
                        )
                    },
                    modifier = GlanceModifier.fillMaxWidth().wrapContentHeight()
                        .padding(top = 10.dp).background(Color.White)
                )

                Box(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            provider = ImageProvider(R.mipmap.ic_launcher),
                            contentDescription = "Open TomaBooks",
                            modifier = GlanceModifier.size(100.dp),
                        )
                        // App Version Text
                        Text(
                            text = "v$versionName", style = TextStyle(
                                //color = ColorProvider(Color.White),
                                color = ColorProvider(Color.White),
                                fontSize = 14.sp

                            ), modifier = GlanceModifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

class BookWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BookWidget()
}
