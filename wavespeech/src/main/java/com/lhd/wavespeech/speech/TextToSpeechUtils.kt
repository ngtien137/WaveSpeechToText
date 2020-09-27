package com.lhd.wavespeech.speech

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.lifecycle.MutableLiveData
import com.lhd.wavespeech.CustomViewSupport.loge
import java.util.*
import kotlin.collections.HashMap

class TextToSpeechUtils(private var context: Context) {

    //region properties

    lateinit var textToSpeech: TextToSpeech
    var liveState = MutableLiveData(State.NOT_READY)
    private val UTTERANCE_ID_FILE = "file"
    private val UTTERANCE_ID_SPEAK = "speak"
    var listener: Listener? = null

    //endregion

    fun init(onSuccess: () -> Unit = {}) {
        if (liveState.value != State.NOT_READY) {
            onSuccess()
            return
        }
        liveState.value = State.INITIALIZING
        textToSpeech = TextToSpeech(context, { status ->
            when (status) {
                TextToSpeech.SUCCESS -> {
                    liveState.value = State.READY
                    textToSpeech.language = Locale.UK
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val listGenderVoice: MutableSet<String> = HashSet()
                        listGenderVoice.add("male") //here you can give male if you want to select male voice.
                        val voice =
                            Voice(
                                "en-us-x-sfg#male_2-local",
                                Locale("en", "US"),
                                400,
                                200,
                                true,
                                listGenderVoice
                            )
                        textToSpeech.voice = voice
                        textToSpeech.setSpeechRate(0.8f)
                    }
                    textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener)
                    onSuccess()
                }
            }
        }, "com.google.android.tts")
    }

    //region UtteranceProgressListener

    var utteranceProgressListener = object :
        UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            loge("On Utterance Start")
            listener?.onTTSSpeakingStart()
        }

        override fun onDone(utteranceId: String?) {
            loge("On Utterance Done")
            Handler(Looper.getMainLooper()).post {
                liveState.value = State.STOP
                listener?.onTTSSpeakingEnd(false)
            }
        }

        override fun onError(utteranceId: String?) {
            loge("On Utterance onError")
            liveState.value = State.NOT_READY
            listener?.onTTSSpeakingError()
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            loge("On Utterance Stop")
            super.onStop(utteranceId, interrupted)
            liveState.value = State.STOP
            listener?.onTTSSpeakingEnd(true)
        }

        override fun onBeginSynthesis(
            utteranceId: String?,
            sampleRateInHz: Int,
            audioFormat: Int,
            channelCount: Int
        ) {
            super.onBeginSynthesis(
                utteranceId,
                sampleRateInHz,
                audioFormat,
                channelCount
            )
            loge("On Utterance onBeginSynthesis")
        }

    }

    //endregion

    //region action

    fun speak(text: String) {
        init {
            liveState.value = State.SPEAKING
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM.toString())
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID_SPEAK)
            })
        }
    }

    fun stop() {
        if (liveState.value == State.SPEAKING) {
            liveState.value = State.STOP
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }
        }
    }

    fun isSpeaking() = liveState.value == State.SPEAKING

    //endregion

    enum class State {
        NOT_READY, INITIALIZING, READY, SPEAKING, STOP
    }

    interface Listener {
        fun onTTSSpeakingStart()
        fun onTTSSpeakingEnd(isEndByStop: Boolean) {}
        fun onTTSSpeakingError() {}
    }
}