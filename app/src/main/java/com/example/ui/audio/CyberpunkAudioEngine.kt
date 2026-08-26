package com.example.ui.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Sound effects catalogue for UI and game interactions.
 */
enum class SoundEffect {
    CLICK,
    TOGGLE_ON,
    TOGGLE_OFF,
    OVERCLOCK,
    CLAIM,
    UPGRADE,
    ERROR,
    MUTE_TOGGLE,
    DIALOG_OPEN
}

/**
 * Cyberpunk Audio Engine that synthesizes and plays:
 * 1. A subtle, hypnotic ambient drone loop while mining is active (with dynamic overclock pitch boost).
 * 2. High-fidelity procedural sci-fi UI interaction sounds (clicks, power sweeps, upgrade fanfares, claims).
 * 3. Centralized Mute / Unmute state management.
 */
class CyberpunkAudioEngine(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "CyberpunkAudioEngine"
        private const val SAMPLE_RATE = 22050
        private const val AMBIENT_BASE_FREQ = 62.0      // Warm sci-fi sub-bass drone
        private const val AMBIENT_OVERCLOCK_FREQ = 104.0 // Higher energy nitro hum
    }

    private val isMuted = AtomicBoolean(false)
    private val isMiningActive = AtomicBoolean(false)
    private val isOverclockActive = AtomicBoolean(false)
    private val isAmbientLoopRunning = AtomicBoolean(false)

    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null

    // Cache pre-synthesized PCM bytes for ultra-low latency UI sound playback
    private val soundBuffers = ConcurrentHashMap<SoundEffect, ShortArray>()

    init {
        // Pre-generate short sound effect PCM buffers in background
        scope.launch(Dispatchers.Default) {
            pregenerateSoundBuffers()
        }
    }

    /**
     * Updates the mute status.
     * When muted, ambient audio stops immediately and UI sound synthesis is bypassed.
     */
    fun setMuted(muted: Boolean) {
        val changed = isMuted.getAndSet(muted) != muted
        if (muted) {
            stopAmbientMiningLoop()
        } else if (changed && isMiningActive.get()) {
            startAmbientMiningLoop(isOverclockActive.get())
        }
    }

    fun isMuted(): Boolean = isMuted.get()

    /**
     * Start or update the subtle ambient mining sound loop.
     */
    fun startAmbientMiningLoop(isOverclocked: Boolean = false) {
        isMiningActive.set(true)
        isOverclockActive.set(isOverclocked)

        if (isMuted.get()) {
            return
        }

        // If loop is already running with matching overclock state, keep running
        if (isAmbientLoopRunning.get() && ambientJob?.isActive == true) {
            return
        }

        stopAmbientMiningLoop()
        isAmbientLoopRunning.set(true)

        ambientJob = scope.launch(Dispatchers.IO) {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(SAMPLE_RATE / 4)

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                ambientTrack = track
                track.play()

                val pcmBuffer = ShortArray(bufferSize)
                var phase1 = 0.0
                var phase2 = 0.0
                var lfoPhase = 0.0

                while (isActive && isAmbientLoopRunning.get() && !isMuted.get()) {
                    val overclock = isOverclockActive.get()
                    val targetFreq = if (overclock) AMBIENT_OVERCLOCK_FREQ else AMBIENT_BASE_FREQ
                    val baseVolume = if (overclock) 0.18 else 0.12 // Subtle, pleasant ambient volume

                    val dt1 = 2.0 * PI * targetFreq / SAMPLE_RATE
                    val dt2 = 2.0 * PI * (targetFreq * 2.02) / SAMPLE_RATE
                    val dtLfo = 2.0 * PI * (if (overclock) 1.8 else 0.4) / SAMPLE_RATE

                    for (i in 0 until bufferSize) {
                        lfoPhase += dtLfo
                        if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI

                        // Subtle rhythmic amplitude modulation (cyber breathing drone)
                        val lfoMod = 0.82 + 0.18 * sin(lfoPhase)

                        phase1 += dt1
                        if (phase1 > 2.0 * PI) phase1 -= 2.0 * PI

                        phase2 += dt2
                        if (phase2 > 2.0 * PI) phase2 -= 2.0 * PI

                        val sample1 = sin(phase1)
                        val sample2 = 0.35 * sin(phase2)
                        val combined = (sample1 + sample2) * baseVolume * lfoMod

                        val pcmVal = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                        pcmBuffer[i] = pcmVal
                    }

                    track.write(pcmBuffer, 0, bufferSize)
                }

                track.stop()
                track.release()
            } catch (e: Exception) {
                Log.w(TAG, "Ambient audio track error: ${e.message}")
            } finally {
                isAmbientLoopRunning.set(false)
            }
        }
    }

    /**
     * Stops the ambient mining drone loop.
     */
    fun stopAmbientMiningLoop() {
        isMiningActive.set(false)
        isAmbientLoopRunning.set(false)
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED) {
                    it.stop()
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping ambient track: ${e.message}")
        }
        ambientTrack = null
    }

    /**
     * Updates ambient state to overclock mode dynamically without interrupting flow.
     */
    fun setOverclockAmbient(isOverclocked: Boolean) {
        isOverclockActive.set(isOverclocked)
        if (isMiningActive.get() && !isMuted.get()) {
            startAmbientMiningLoop(isOverclocked)
        }
    }

    /**
     * Plays a crisp, procedural sci-fi UI sound effect.
     */
    fun playUiSound(effect: SoundEffect) {
        if (isMuted.get()) return

        scope.launch(Dispatchers.IO) {
            try {
                val samples = soundBuffers[effect] ?: generatePcmForEffect(effect)
                if (samples.isEmpty()) return@launch

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()

                // Release track after playback completes
                val durationMs = (samples.size.toDouble() / SAMPLE_RATE * 1000.0).toLong() + 50L
                kotlinx.coroutines.delay(durationMs)
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // Ignore track cleanup race
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to play UI sound $effect: ${e.message}")
            }
        }
    }

    /**
     * Pre-generates PCM short arrays for all UI sound effects to eliminate click latency.
     */
    private fun pregenerateSoundBuffers() {
        for (effect in SoundEffect.values()) {
            soundBuffers[effect] = generatePcmForEffect(effect)
        }
    }

    /**
     * Synthesizes mathematical waveform samples for distinct cyberpunk UI audio cues.
     */
    private fun generatePcmForEffect(effect: SoundEffect): ShortArray {
        val durationMs: Int
        val generator: (timeSec: Double, progress: Double) -> Double

        when (effect) {
            SoundEffect.CLICK -> {
                durationMs = 28
                generator = { _, progress ->
                    val freq = 1400.0 - progress * 500.0
                    val envelope = exp(-progress * 9.0)
                    sin(2.0 * PI * freq * (progress * 0.028)) * envelope * 0.40
                }
            }
            SoundEffect.MUTE_TOGGLE -> {
                durationMs = 65
                generator = { t, progress ->
                    val freq = 880.0 + sin(progress * PI) * 440.0
                    val env = sin(progress * PI)
                    sin(2.0 * PI * freq * t) * env * 0.35
                }
            }
            SoundEffect.TOGGLE_ON -> {
                // Futuristic ascending sweep
                durationMs = 90
                generator = { t, progress ->
                    val freq = 320.0 + (progress * progress) * 780.0
                    val env = sin(progress * PI)
                    sin(2.0 * PI * freq * t) * env * 0.45
                }
            }
            SoundEffect.TOGGLE_OFF -> {
                // Descending power down drop
                durationMs = 95
                generator = { t, progress ->
                    val freq = 850.0 - (progress * 620.0)
                    val env = exp(-progress * 4.0)
                    sin(2.0 * PI * freq * t) * env * 0.40
                }
            }
            SoundEffect.OVERCLOCK -> {
                // High-energy turbo boost sonic flare
                durationMs = 180
                generator = { t, progress ->
                    val freq = 550.0 + progress * 1250.0
                    val harmonic = sin(2.0 * PI * (freq * 1.5) * t) * 0.3
                    val primary = sin(2.0 * PI * freq * t)
                    val env = sin(progress * PI)
                    (primary + harmonic) * env * 0.50
                }
            }
            SoundEffect.CLAIM -> {
                // Triumphant crystal harvest chord
                durationMs = 260
                generator = { t, progress ->
                    val phase = progress * 3.0
                    val currentFreq = when {
                        phase < 1.0 -> 880.0
                        phase < 2.0 -> 1320.0
                        else -> 1760.0
                    }
                    val env = exp(-(progress % 0.33) * 8.0)
                    sin(2.0 * PI * currentFreq * t) * env * 0.45
                }
            }
            SoundEffect.UPGRADE -> {
                // Tech advancement power fanfare (4-step ascending)
                durationMs = 320
                generator = { t, progress ->
                    val step = (progress * 4.0).toInt().coerceIn(0, 3)
                    val noteFreq = when (step) {
                        0 -> 523.25 // C5
                        1 -> 659.25 // E5
                        2 -> 783.99 // G5
                        else -> 1046.50 // C6
                    }
                    val stepProgress = (progress * 4.0) - step
                    val env = exp(-stepProgress * 4.0)
                    (sin(2.0 * PI * noteFreq * t) + 0.25 * sin(2.0 * PI * noteFreq * 2.0 * t)) * env * 0.42
                }
            }
            SoundEffect.ERROR -> {
                // Warning buzz
                durationMs = 120
                generator = { t, progress ->
                    val freq = 180.0
                    val env = sin(progress * PI)
                    val square = if (sin(2.0 * PI * freq * t) > 0.0) 0.3 else -0.3
                    square * env * 0.35
                }
            }
            SoundEffect.DIALOG_OPEN -> {
                // Smooth high-tech interface chirp
                durationMs = 45
                generator = { t, progress ->
                    val freq = 1200.0 + progress * 400.0
                    val env = exp(-progress * 6.0)
                    sin(2.0 * PI * freq * t) * env * 0.28
                }
            }
        }

        val totalSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val pcm = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = i.toDouble() / totalSamples
            val raw = generator(t, progress).coerceIn(-1.0, 1.0)
            pcm[i] = (raw * Short.MAX_VALUE).toInt().toShort()
        }

        return pcm
    }

    /**
     * Pause audio during activity lifecycle pauses.
     */
    fun pauseAmbientOnAppPause() {
        if (isAmbientLoopRunning.get()) {
            ambientJob?.cancel()
            ambientJob = null
            try {
                ambientTrack?.pause()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Resume ambient sound on activity resume if mining was active.
     */
    fun resumeAmbientOnAppResume() {
        if (isMiningActive.get() && !isMuted.get()) {
            startAmbientMiningLoop(isOverclockActive.get())
        }
    }

    /**
     * Cleanup and release resources.
     */
    fun release() {
        stopAmbientMiningLoop()
        soundBuffers.clear()
    }
}
