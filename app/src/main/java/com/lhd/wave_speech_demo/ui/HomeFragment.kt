package com.lhd.wave_speech_demo.ui

import com.lhd.wave_speech_demo.R
import com.lhd.wave_speech_demo.databinding.FragmentHomeBinding
import com.lhd.wavespeech.speech.SpeechRecognizerUtils

class HomeFragment : BaseMainFragment<FragmentHomeBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    private val speechRecognizerUtils by lazy {
        SpeechRecognizerUtils(activity)
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
        }
    }

    private fun doTalkClick() {
        speechRecognizerUtils.startListening()
    }

}