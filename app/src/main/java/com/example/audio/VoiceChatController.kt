package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sqrt

data class VoiceChatState(
    val channelName: String = "Squad Voice",
    val isMicActive: Boolean = false,
    val isSpeakerEnabled: Boolean = true,
    val hasMicPermission: Boolean = false,
    val micLevel: Float = 0f,
    val activeSpeakerName: String? = null,
    val lastCalloutText: String? = null,
    val statusNotice: String? = null
)

class VoiceChatController(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(
        VoiceChatState(hasMicPermission = checkMicPermission())
    )
    val state: StateFlow<VoiceChatState> = _state.asStateFlow()

    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.US
                    ttsReady = true
                }
            }
        } catch (_: Throwable) {
            ttsReady = false
        }
    }

    fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionResult(granted: Boolean, callsign: String) {
        _state.update {
            it.copy(
                hasMicPermission = granted,
                statusNotice = if (granted) "Mic permission granted" else "Microphone permission denied"
            )
        }
        if (granted) {
            startMicCapture(callsign)
        }
    }

    fun toggleMic(callsign: String) {
        if (_state.value.isMicActive) {
            stopMicCapture()
        } else {
            if (!checkMicPermission()) {
                _state.update {
                    it.copy(
                        hasMicPermission = false,
                        statusNotice = "Grant microphone access to enable Squad Voice"
                    )
                }
                return
            }
            startMicCapture(callsign)
        }
    }

    fun toggleSpeaker() {
        _state.update {
            val next = !it.isSpeakerEnabled
            it.copy(
                isSpeakerEnabled = next,
                statusNotice = if (next) "Squad speaker unmuted" else "Squad speaker muted"
            )
        }
    }

    fun sendSquadCallout(senderCallsign: String, message: String) {
        _state.update {
            it.copy(
                activeSpeakerName = senderCallsign,
                lastCalloutText = message,
                statusNotice = "$senderCallsign: $message"
            )
        }
        if (_state.value.isSpeakerEnabled && ttsReady) {
            try {
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "voice_callout_${System.currentTimeMillis()}")
            } catch (_: Throwable) {
            }
        }
        scope.launch {
            delay(2800L)
            _state.update { current ->
                if (current.lastCalloutText == message) {
                    current.copy(activeSpeakerName = null)
                } else {
                    current
                }
            }
        }
    }

    fun clearStatusNotice() {
        _state.update { it.copy(statusNotice = null) }
    }

    private fun startMicCapture(callsign: String) {
        stopMicCapture()
        _state.update {
            it.copy(
                hasMicPermission = true,
                isMicActive = true,
                activeSpeakerName = callsign,
                statusNotice = "Squad Mic Live"
            )
        }

        recordJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 16000
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) {
                _state.update { it.copy(statusNotice = "Squad Mic Live (Audio hardware idle)") }
                return@launch
            }

            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    recorder.release()
                    _state.update { it.copy(statusNotice = "Squad Mic Live (No hardware mic)") }
                    return@launch
                }
                audioRecord = recorder
                recorder.startRecording()
                val buffer = ShortArray(minBuf / 2)
                while (isActive && _state.value.isMicActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            val s = buffer[i].toDouble()
                            sumSq += s * s
                        }
                        val rms = sqrt(sumSq / read).toFloat()
                        val normalized = (rms / 6500f).coerceIn(0f, 1f)
                        _state.update {
                            it.copy(
                                micLevel = normalized,
                                activeSpeakerName = if (normalized > 0.08f) callsign else it.activeSpeakerName
                            )
                        }
                    }
                    delay(65L)
                }
            } catch (_: SecurityException) {
                _state.update {
                    it.copy(
                        isMicActive = false,
                        hasMicPermission = false,
                        micLevel = 0f,
                        statusNotice = "Microphone permission required"
                    )
                }
            } catch (_: Throwable) {
                _state.update {
                    it.copy(
                        micLevel = 0f,
                        statusNotice = "Squad Voice connected (Callout mode)"
                    )
                }
            } finally {
                try {
                    recorder?.stop()
                } catch (_: Throwable) {
                }
                try {
                    recorder?.release()
                } catch (_: Throwable) {
                }
                audioRecord = null
            }
        }
    }

    fun stopMicCapture() {
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
        try {
            audioRecord?.release()
        } catch (_: Throwable) {
        }
        audioRecord = null
        _state.update {
            it.copy(
                isMicActive = false,
                micLevel = 0f,
                activeSpeakerName = null,
                statusNotice = "Mic muted"
            )
        }
    }

    fun release() {
        stopMicCapture()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Throwable) {
        }
        tts = null
    }
}
