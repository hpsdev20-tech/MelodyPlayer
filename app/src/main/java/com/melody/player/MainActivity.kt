package com.melody.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.playback.PlaybackManager
import com.melody.player.ui.navigation.MelodyAppComposable
import com.melody.player.ui.theme.MelodyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackManager: PlaybackManager

    @Inject
    lateinit var musicRepository: MusicRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            playbackManager.connect()
        }

        setContent {
            MelodyTheme {
                MelodyAppComposable(
                    playbackManager = playbackManager,
                    musicRepository = musicRepository
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            playbackManager.saveState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackManager.disconnect()
    }
}
