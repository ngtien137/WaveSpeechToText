package com.lhd.wavespeech.speech

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.*
import kotlin.collections.HashMap

class TextToSpeechUtils {

    //region properties

    lateinit var textToSpeech: TextToSpeech
    private var isInitialized = false

    //endregion

    fun init(context: Context, onSuccess: () -> Unit = {}) {
        if (isInitialized) {
            onSuccess()
            return
        }
        textToSpeech = TextToSpeech(context, { status ->
            when (status) {
                TextToSpeech.SUCCESS -> {
                    isInitialized = true
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

        }

        override fun onDone(utteranceId: String?) {

        }

        override fun onError(utteranceId: String?) {

        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
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
        }

    }

    //endregion

    //region action

    fun speak(context: Context, text: String) {
        init(context) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, HashMap<String, String>())
        }
    }

    //endregion
}