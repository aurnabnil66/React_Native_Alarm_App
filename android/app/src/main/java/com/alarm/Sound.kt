package com.alarm

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log

class Sound(private val context: Context) {

    companion object {
        private const val TAG = "AlarmSound"
        private const val DEFAULT_VIBRATION = 100L
    }

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val userVolume: Int = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun play(sound: String) {
        val soundUri = getSoundUri(sound)
        playSound(soundUri)
        startVibration()
    }

    fun stop() {
        try {
            if (mediaPlayer.isPlaying) {
                stopSound()
                stopVibration()
                mediaPlayer.release()
            }
        } catch (e: IllegalStateException) {
            Log.d(TAG, "Sound has probably been released already")
        }
    }

    private fun playSound(soundUri: Uri) {
        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.setScreenOnWhilePlaying(true)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
                mediaPlayer.setDataSource(context, soundUri)
                mediaPlayer.setVolume(1.0f, 1.0f)
                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play sound", e)
        }
    }

    private fun stopSound() {
        try {
            // Reset the volume to what it was before
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userVolume, AudioManager.FLAG_PLAY_SOUND)
            mediaPlayer.stop()
            mediaPlayer.reset()
        } catch (e: Exception) {
            Log.e(TAG, "ringtone: ${e.message}", e)
        }
    }

    private fun startVibration() {
        vibrator.vibrate(DEFAULT_VIBRATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

        val pattern = longArrayOf(0, 100, 1000)
        vibrator.vibrate(pattern, 0) // Repeat indefinitely
    }

    private fun stopVibration() {
        vibrator.cancel()
    }

    private fun getSoundUri(soundName: String): Uri {
        return if (soundName == "default") {
            Settings.System.DEFAULT_RINGTONE_URI
        } else {
            val resId = context.resources.getIdentifier(
                soundName.substringBeforeLast('.'),
                "raw",
                context.packageName
            )
            Uri.parse("android.resource://${context.packageName}/$resId")
        }
    }
}
