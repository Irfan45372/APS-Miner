package com.example.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class CyberpunkAudioEngine(private val scope: CoroutineScope) {
    private val sampleRate = 44100

    private fun playTones(vararg pairs: Pair<Double, Int>) {
        scope.launch(Dispatchers.Default) {
            try {
                val totalSamples = pairs.sumOf { (it.second * sampleRate) / 1000 }
                val buffer = ShortArray(totalSamples)
                var currentIdx = 0

                for ((freq, durationMs) in pairs) {
                    val count = (durationMs * sampleRate) / 1000
                    for (i in 0 until count) {
                        val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                        val envelope = if (i < 100) (i / 100.0) else if (i > count - 100) ((count - i) / 100.0) else 1.0
                        val sample = (sin(angle) * Short.MAX_VALUE * 0.25 * envelope).toInt().toShort()
                        if (currentIdx < buffer.size) {
                            buffer[currentIdx++] = sample
                        }
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                val totalDurationMs = pairs.sumOf { it.second }.toLong()
                kotlinx.coroutines.delay(totalDurationMs + 50)
                track.stop()
                track.release()
            } catch (_: Exception) {
            }
        }
    }

    fun playMiningBeep() {
        playTones(880.0 to 40, 1320.0 to 50)
    }

    fun playUpgradeFanfare() {
        playTones(
            523.25 to 80,
            659.25 to 80,
            783.99 to 80,
            1046.50 to 200
        )
    }

    fun playRentalActivated() {
        playTones(
            440.0 to 70,
            660.0 to 70,
            880.0 to 140
        )
    }

    fun playClaimSound() {
        playTones(
            783.99 to 60,
            987.77 to 60,
            1174.66 to 60,
            1567.98 to 180
        )
    }

    fun playButtonTick() {
        playTones(1200.0 to 20)
    }

    fun playErrorBeep() {
        playTones(220.0 to 100, 180.0 to 120)
    }
}
