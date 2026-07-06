package com.project.beam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.beam.navigation.BeamNavGraph
import com.project.beam.ui.theme.BEAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BEAMTheme {
                BeamNavGraph()
            }
        }
    }
}