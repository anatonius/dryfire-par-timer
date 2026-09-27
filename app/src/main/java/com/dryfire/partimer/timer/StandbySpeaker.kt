package com.dryfire.partimer.timer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/**
 * Speaks "Stand by!" using on-device TTS when each rep's standby phase begins.
 * Returns false from [speakStandby] if TTS isn't ready — caller should
 * play a fallback warning buzz instead.
 */
class StandbySpeaker(context: Context) {
    @Volatile
    var ready = false
        private set

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val res = tts?.setLanguage(Locale.US)
                if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                    pickMaleVoice()
                    // Urgent delivery: fast rate, slightly raised pitch.
                    try {
                        tts?.setSpeechRate(1.35f)
                        tts?.setPitch(1.05f)
                    } catch (_: Exception) {
                    }
                    ready = true
                }
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {}
            override fun onError(utteranceId: String?) {}
        })
    }

    /** Prefer an English male voice; fall back to default if none found. */
    private fun pickMaleVoice() {
        try {
            val voices = tts?.voices ?: return
            val male = voices.firstOrNull { v ->
                v.locale.language == "en" && v.name.contains("male", ignoreCase = true)
            } ?: voices.firstOrNull { v ->
                v.locale.language == "en" &&
                    (v.name.contains("daniel", ignoreCase = true) ||
                        v.name.contains("david", ignoreCase = true) ||
                        v.name.contains("mark", ignoreCase = true))
            } ?: return
            tts?.voice = male
        } catch (_: Exception) {
        }
    }

    fun speakStandby(): Boolean {
        val engine = tts ?: return false
        if (!ready) return false
        return try {
            engine.speak("Stand by!", TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
