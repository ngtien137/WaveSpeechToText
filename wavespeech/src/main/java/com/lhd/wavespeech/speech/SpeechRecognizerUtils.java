package com.lhd.wavespeech.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.lhd.wavespeech.views.WaveRecordView;

import java.util.List;

import static com.lhd.wavespeech.CustomViewSupport.loge;

public class SpeechRecognizerUtils {

    private Context context;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mIntentSpeech;
    private boolean isSupported = false;
    private WaveRecordView waveRecordView;
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            loge("Speech Ready");
        }

        @Override
        public void onBeginningOfSpeech() {
            loge("Speech Begin");
        }

        @Override
        public void onRmsChanged(float v) {
            loge("Speech onRmsChanged: ", v);
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            loge("Speech onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            loge("Speech End");
        }

        @Override
        public void onError(int i) {
            checkError(i);
        }

        @Override
        public void onResults(Bundle bundle) {
            List<String> list = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            loge("Speech onResults: ", list);
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    public SpeechRecognizerUtils(Context context) {
        this.context = context;
        isSupported = initRecognizerIfSupported();
    }

    public boolean isGoogleSupported() {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    public boolean initRecognizerIfSupported() {
        boolean isSupported;
        if (isGoogleSupported()) {
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizer.setRecognitionListener(listener);
            mIntentSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mIntentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
            mIntentSpeech.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            mIntentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mIntentSpeech.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            mIntentSpeech.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            isSupported = true;
        } else {
            isSupported = false;
        }
        return isSupported;
    }

    public boolean isGoogleRecognizer() {
        String value = Settings.Secure.getString(context.getContentResolver(), "voice_recognition_service");
        return value != null;
    }

    public void setWaveRecordView(WaveRecordView waveRecordView) {
        this.waveRecordView = waveRecordView;
    }

    public void startListening() {
        if (mSpeechRecognizer != null && mIntentSpeech != null) {
            mSpeechRecognizer.startListening(mIntentSpeech);
        }
    }

    public boolean isSupported() {
        return isSupported;
    }

    private void checkError(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                loge("Error audio");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                loge("Error No match");
                break;
            default:
                loge("Speech Error", error);
                break;
        }
    }
}
