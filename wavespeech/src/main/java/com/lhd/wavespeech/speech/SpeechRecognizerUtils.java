package com.lhd.wavespeech.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.lhd.wavespeech.AudioDataReceivedListener;
import com.lhd.wavespeech.BaseRecord;
import com.lhd.wavespeech.CustomViewSupport;
import com.lhd.wavespeech.views.WaveRecordView;
import com.lhd.wavespeech.views.WaveSpeechView;

import java.util.List;

import static com.lhd.wavespeech.CustomViewSupport.loge;

public class SpeechRecognizerUtils {

    private final static String TEMP_SPEECH_FILE_NAME = "Temp_Speech";

    private Context context;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mIntentSpeech;
    private boolean isSupported = false;
    private WaveSpeechView waveSpeechView;
    private MutableLiveData<Boolean> liveListeningSpeech = new MutableLiveData(false);
    private BaseRecord baseRecord;
    private WaveRecordView waveRecordView;
    private Listener listener;
    private boolean isNeedStartIntentNewTask = false;
    private AudioDataReceivedListener audioDataReceivedListener = new AudioDataReceivedListener() {
        @Override
        public void onAudioDataReceived(short[] data) {
            if (waveRecordView != null) {
                waveRecordView.setSamples(data);
            }
        }
    };
    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            loge("Speech Ready");
        }

        @Override
        public void onBeginningOfSpeech() {
            liveListeningSpeech.setValue(true);
            loge("Speech Begin");
            if (waveRecordView != null) {
                if (baseRecord == null) {
                    baseRecord = new BaseRecord();
                    baseRecord.setKeepOutput(false);
                    baseRecord.setAudioDataListener(audioDataReceivedListener);
                }
                baseRecord.start(context.getExternalCacheDir().getAbsolutePath(), TEMP_SPEECH_FILE_NAME);
            }
        }

        @Override
        public void onRmsChanged(float v) {
            loge("Speech onRmsChanged: ", v);
            if (waveSpeechView != null && liveListeningSpeech.getValue() == true) {
                waveSpeechView.setValue(v + 10);
            }
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            loge("Speech onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            loge("Speech End");
            onEndSpeech();
        }

        @Override
        public void onError(int i) {
            checkError(i);
            onEndSpeech();
            if (listener != null) {
                listener.onResultSpeech("");
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            List<String> list = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            loge("Speech onResults: ", list);
            onEndOfSpeech();
            if (listener != null) {
                if (list == null || list.isEmpty())
                    listener.onResultSpeech("");
                else
                    listener.onResultSpeech(list.get(0));
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    private void onEndSpeech() {
        if (liveListeningSpeech.getValue() == true) {
            liveListeningSpeech.setValue(false);
            if (waveSpeechView != null) {
                waveSpeechView.reset(true);
            }
            if (waveRecordView != null && baseRecord != null) {
                baseRecord.stop(TEMP_SPEECH_FILE_NAME);
            }
        }

    }

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
            mSpeechRecognizer.setRecognitionListener(recognitionListener);
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

    public void setWaveView(WaveSpeechView waveSpeechView) {
        this.waveSpeechView = waveSpeechView;
    }

    public void setWaveView(WaveRecordView waveRecordView) {
        this.waveRecordView = waveRecordView;
    }

    public boolean isNeedStartIntentNewTask() {
        return isNeedStartIntentNewTask;
    }

    public void setNeedStartIntentNewTask(boolean needStartIntentNewTask) {
        isNeedStartIntentNewTask = needStartIntentNewTask;
    }

    public boolean isListeningSpeech() {
        return liveListeningSpeech.getValue();
    }

    public MutableLiveData<Boolean> getLiveListeningSpeech() {
        return liveListeningSpeech;
    }

    public void startListening() {
        if (mSpeechRecognizer != null && mIntentSpeech != null) {
            if (!liveListeningSpeech.getValue())
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

    public void cancelListener() {
        if (liveListeningSpeech.getValue()) {
            if (mSpeechRecognizer != null) {
                mSpeechRecognizer.cancel();
                mSpeechRecognizer.destroy();
            }
            if (baseRecord != null) {
                baseRecord.release();
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void enableLog(boolean enable) {
        CustomViewSupport.ENABLE_LOG = enable;
    }

    public interface Listener {
        default public void onStartResultSpeech() {
        }

        void onResultSpeech(@NonNull String result);
    }
}
