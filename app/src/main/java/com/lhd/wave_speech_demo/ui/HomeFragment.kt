package com.lhd.wave_speech_demo.ui

import com.lhd.wave_speech_demo.R
import com.lhd.wave_speech_demo.databinding.FragmentHomeBinding
import com.lhd.wavespeech.speech.SpeechRecognizerUtils
import com.lhd.wavespeech.speech.TextToSpeechUtils

class HomeFragment : BaseMainFragment<FragmentHomeBinding>(), SpeechRecognizerUtils.Listener {

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    private val speechRecognizerUtils by lazy {
        SpeechRecognizerUtils(activity).apply {
            setListener(this@HomeFragment)
        }
    }

    private val textToSpeechUtils by lazy {
        TextToSpeechUtils()
    }

    override fun initBinding() {

    }

    override fun initView() {
        speechRecognizerUtils.setWaveView(binding.waveView)
    }

    override fun onViewClick(vId: Int) {
        when (vId) {
            R.id.btnTalk -> {
                activity.grantPermission {
                    doTalkClick()
                }
            }
            R.id.btnTextToSpeak -> {
                textToSpeechUtils.speak(activity, binding.tvMain.text.toString())
            }
        }
    }

    private fun doTalkClick() {
        speechRecognizerUtils.startListening()
    }

    override fun onResultSpeech(result: String) {
        binding.tvMain.text = result
    }

}