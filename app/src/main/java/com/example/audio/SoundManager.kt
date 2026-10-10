package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural Audio & Haptics Engine.
 * Synthesizes pure kawaii, whimsical tones via AudioTrack with zero external audio assets.
 */
class SoundManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSfxEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) startBgm() else stopBgm()
        }
    var isVibrateEnabled: Boolean = true

    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null

    companion object {
        private const val SAMPLE_RATE = 44100
    }

    private fun vibrate(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!isVibrateEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun playPcm(buffer: ShortArray, volume: Float = 0.8f) {
        if (!isSfxEnabled) return
        scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufferSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.setVolume(volume)
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Wait for playback and release
                delay((buffer.size * 1000L / SAMPLE_RATE) + 100L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Soft pop for piece placement or button clicks
     */
    fun playPop() {
        vibrate(15, 60)
        val duration = 0.09 // 90ms
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Pitch bend up: 400Hz to 850Hz
            val freq = 400.0 + 450.0 * (t / duration)
            val envelope = exp(-t * 35.0) // snappy decay
            val sample = sin(2.0 * PI * freq * t) * envelope
            buffer[i] = (sample * 18000).toInt().toShort()
        }
        playPcm(buffer, 0.75f)
    }

    /**
     * Button tap sound
     */
    fun playButtonTap() {
        vibrate(10, 40)
        val duration = 0.05
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 520.0 + 200.0 * (1.0 - t / duration)
            val envelope = exp(-t * 60.0)
            val sample = sin(2.0 * PI * freq * t) * envelope
            buffer[i] = (sample * 14000).toInt().toShort()
        }
        playPcm(buffer, 0.6f)
    }

    /**
     * Whoosh sound for hints
     */
    fun playWhoosh() {
        vibrate(20, 70)
        val duration = 0.22
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / duration
            val freq = 300.0 + 600.0 * sin(progress * PI)
            val env = sin(progress * PI) * exp(-progress * 1.5)
            val sample = (sin(2.0 * PI * freq * t) * 0.7 + (Math.random() - 0.5) * 0.3) * env
            buffer[i] = (sample * 14000).toInt().toShort()
        }
        playPcm(buffer, 0.7f)
    }

    /**
     * Swoosh sound for undo
     */
    fun playSwoosh() {
        vibrate(25, 80)
        val duration = 0.18
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val progress = t / duration
            val freq = 800.0 - 450.0 * progress
            val env = sin(progress * PI)
            val sample = sin(2.0 * PI * freq * t) * env
            buffer[i] = (sample * 15000).toInt().toShort()
        }
        playPcm(buffer, 0.7f)
    }

    /**
     * Sparkling celebratory win jingle
     */
    fun playWinJingle() {
        vibrate(40, 150)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51) // C5, E5, G5, C6, E6
        val noteDur = 0.12
        val totalDur = notes.size * noteDur + 0.3
        val numSamples = (SAMPLE_RATE * totalDur).toInt()
        val buffer = ShortArray(numSamples)

        for ((index, freq) in notes.withIndex()) {
            val startSample = (index * noteDur * SAMPLE_RATE).toInt()
            val durSamples = (SAMPLE_RATE * 0.35).toInt()
            for (i in 0 until durSamples) {
                val idx = startSample + i
                if (idx < numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 9.0)
                    // Add gentle bell harmonic
                    val sample = (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * freq * 2.0 * t)) * env
                    val cur = buffer[idx].toInt()
                    val nextVal = (cur + sample * 12000).toInt().coerceIn(-32767, 32767)
                    buffer[idx] = nextVal.toShort()
                }
            }
        }
        playPcm(buffer, 0.85f)
    }

    /**
     * Cute gentle descending bloop for lose
     */
    fun playLoseBloop() {
        vibrate(30, 90)
        val notes = doubleArrayOf(392.0, 329.63, 261.63) // G4, E4, C4
        val noteDur = 0.14
        val totalDur = notes.size * noteDur + 0.2
        val numSamples = (SAMPLE_RATE * totalDur).toInt()
        val buffer = ShortArray(numSamples)

        for ((index, freq) in notes.withIndex()) {
            val startSample = (index * noteDur * SAMPLE_RATE).toInt()
            val durSamples = (SAMPLE_RATE * 0.25).toInt()
            for (i in 0 until durSamples) {
                val idx = startSample + i
                if (idx < numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 11.0)
                    val sample = sin(2.0 * PI * freq * t) * env
                    val cur = buffer[idx].toInt()
                    val nextVal = (cur + sample * 13000).toInt().coerceIn(-32767, 32767)
                    buffer[idx] = nextVal.toShort()
                }
            }
        }
        playPcm(buffer, 0.7f)
    }

    /**
     * Sparkling chime for level-up celebration
     */
    fun playLevelUp() {
        vibrate(60, 180)
        val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
        val noteDur = 0.08
        val totalDur = notes.size * noteDur + 0.4
        val numSamples = (SAMPLE_RATE * totalDur).toInt()
        val buffer = ShortArray(numSamples)

        for ((index, freq) in notes.withIndex()) {
            val startSample = (index * noteDur * SAMPLE_RATE).toInt()
            val durSamples = (SAMPLE_RATE * 0.4).toInt()
            for (i in 0 until durSamples) {
                val idx = startSample + i
                if (idx < numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = exp(-t * 8.0)
                    val sample = (sin(2.0 * PI * freq * t) + 0.4 * sin(2.0 * PI * freq * 2.0 * t)) * env
                    val cur = buffer[idx].toInt()
                    val nextVal = (cur + sample * 11000).toInt().coerceIn(-32767, 32767)
                    buffer[idx] = nextVal.toShort()
                }
            }
        }
        playPcm(buffer, 0.85f)
    }

    /**
     * Starts gentle background music loop (lullaby / music box style)
     */
    fun startBgm() {
        if (!isMusicEnabled || bgmJob != null) return
        bgmJob = scope.launch {
            // Cute dreamy lullaby arpeggio loop
            // Frequencies for a soft pentatonic music box: C5, D5, E5, G5, A5, C6
            val melody = doubleArrayOf(
                523.25, 659.25, 783.99, 659.25,
                587.33, 783.99, 880.00, 783.99,
                523.25, 659.25, 1046.50, 783.99,
                659.25, 587.33, 523.25, 392.00
            )
            var noteIndex = 0

            while (isActive && isMusicEnabled) {
                val freq = melody[noteIndex % melody.size]
                noteIndex++

                val duration = 0.32
                val numSamples = (SAMPLE_RATE * duration).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / SAMPLE_RATE
                    // Soft sine tone with gentle bell harmonic and warm decay
                    val env = exp(-t * 5.0)
                    val sample = (sin(2.0 * PI * freq * t) * 0.8 + sin(2.0 * PI * freq * 2.0 * t) * 0.2) * env
                    buffer[i] = (sample * 4200).toInt().toShort() // low background volume
                }
                playPcm(buffer, 0.22f)
                delay(340L)
            }
        }
    }

    fun stopBgm() {
        bgmJob?.cancel()
        bgmJob = null
    }
}
