package com.davidegea.spotifystats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.davidegea.spotifystats.designsystem.SpotifyStatsTheme
import com.davidegea.spotifystats.ui.SpotifyStatsRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotifyStatsTheme {
                SpotifyStatsRoot()
            }
        }
    }
}
