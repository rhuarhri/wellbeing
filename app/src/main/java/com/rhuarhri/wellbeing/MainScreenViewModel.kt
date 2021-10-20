package com.rhuarhri.wellbeing

import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel

class MainScreenViewModel : ViewModel() {

    private lateinit var audioPlayer: MediaPlayer

    fun stepUpMusic(context: ComponentActivity) {
        if (this::audioPlayer.isInitialized == false) {
            audioPlayer = MediaPlayer.create(context, R.raw.background_music)
            audioPlayer.isLooping = true
        }
    }

    fun pauseBackgroundMusic() {
        audioPlayer.pause()
    }

    fun restartBackgroundMusic() {
        audioPlayer.start()
    }

    override fun onCleared() {
        if (this::audioPlayer.isInitialized == true) {
            audioPlayer.stop()
            audioPlayer.release()
        }
        super.onCleared()
    }
}