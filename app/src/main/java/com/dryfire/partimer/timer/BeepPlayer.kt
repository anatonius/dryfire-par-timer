package com.dryfire.partimer.timer

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Shot-timer style buzzer synthesized with AudioTrack.
 * Square-ish wave (fundamental + odd harmonics) with fast attack
 * and short release to avoid clicks, played at full STREAM_MUSIC volume
 * scaled by [BeepConfig.volumePercent].
 */
class BeepPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    fun play(config: BeepConfig) {
        job?.cancel()
        job = scope.launch {
            try {
                playBuzzer(
                    config.frequencyHz.coerceIn(500, 5000),
                    config.durationMs.coerceIn(100, 1000),
                    config.volumePercent.coerceIn(0, 100) / 100f
                )
            } catch (_: Exception) {
                // audio unavailable; ignore
            }
        }
    }

    fun playStart(config: BeepConfig) = play(config)
    fun playStop(config: BeepConfig) = play(config)

    /** End-of-drill: double buzz. */
    fun playEnd(config: BeepConfig) {
        job?.cancel()
        job = scope.launch {
            try {
                val half = (config.durationMs.coerceIn(100, 1000) / 2).coerceAtLeast(100)
                playBuzzer(config.frequencyHz.coerceIn(500, 5000), half, config.volumePercent / 100f)
                delay(150)
                playBuzzer(config.frequencyHz.coerceIn(500, 5000), half, config.volumePercent / 100f)
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun playBuzzer(freqHz: Int, durationMs: Int, volume: Float) {
        val sampleRate = 44100
        val frames = (sampleRate * durationMs / 1000)
        val pcm = ShortArray(frames)
        val attack = (frames * 0.02).toInt().coerceAtLeast(1)
        val release = (frames * 0.05).toInt().coerceAtLeast(1)
        for (i in pcm.indices) {
            val t = i / sampleRate.toDouble()
            // fundamental + 3rd/5th harmonics = buzzer-like square wave
            val s = sin(2 * PI * freqHz * t) +
                0.33 * sin(2 * PI * freqHz * 3 * t) +
                0.2 * sin(2 * PI * freqHz * 5 * t)
            val env = when {
                i < attack -> i / attack.toFloat()
                i > frames - release -> (frames - i) / release.toFloat()
                else -> 1f
            }
            pcm[i] = (s / 1.53 * env * volume * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
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
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        try {
            track.write(pcm, 0, pcm.size)
            track.play()
            delay(durationMs + 30L)
        } finally {
            track.stop()
            track.release()
        }
    }

    fun release() {
        job?.cancel()
    }
}
