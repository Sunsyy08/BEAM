package com.project.beam.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class BeamWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = BeamWidget()
}