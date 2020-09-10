package com.lhd.wavespeech;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

public class BaseRecord {
    private AudioDataReceivedListener mListener;

    private static final int samplingRates[] = {16000, 11025, 11000, 8000, 6000};
    public static int SAMPLE_RATE = 16000;
    private AudioRecord mRecorder;
    private File mRecording;
    private short[] mBuffer;
    private String fileName;
    private boolean mIsRecording = false;
    private String RECORD_WAV_PATH = "";

    private Thread audioRecordThread;
    private boolean isPause = false;
    private Timer timer;
    private ProgressRecord recordProgress;
    private long rawLength = 0;

    public void setRecordProgress(ProgressRecord recordProgress) {
        this.recordProgress = recordProgress;
    }


    public void setAudioDataListener(AudioDataReceivedListener mListener) {
        this.mListener = mListener;
    }

    public interface ProgressRecord {
        void onRecordProgress(int miliSecond);
    }

    public BaseRecord() {
        initRecorder();
    }

    public static int getValidSampleRates() {
        for (int rate : samplingRates) {
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;
            }
        }
        return SAMPLE_RATE;
    }

    public void start(String folder, String name) {
        RECORD_WAV_PATH = folder;
        fileName = name;
        isPause = false;
        mIsRecording = true;
        mRecorder.startRecording();
        mRecording = getFile(fileName, "raw");
        startBufferedWrite(mRecording);
    }

    public String stop(String fileName) {
        try {
            mIsRecording = false;
            isPause = false;
            mRecorder.stop();
            timer.cancel();
            if (audioRecordThread != null) {
                audioRecordThread.interrupt();
                audioRecordThread = null;
            }
            File waveFile = getFile(fileName, "wav");
            rawToWave(mRecording, waveFile);
            return waveFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("Error saving file : ", e.getMessage());
        }
        return null;
    }

    public void release() {
        mRecorder.release();
    }

    public void pause() {
        if (!isPause) isPause = true;

    }

    public void resume() {
        if (isPause) isPause = false;

    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public boolean isPause() {
        return isPause;
    }

    private int TIME_DELAY = 1;

    private class MyTask extends TimerTask {
        int currentMilis = 0;

        @Override
        public void run() {
            if (mIsRecording && !isPause) {
                currentMilis = (int)(getCurrentAudioDurationFromRawLength() * 1000);
                if (recordProgress != null) recordProgress.onRecordProgress(currentMilis);
            }
        }
    }


    private void initRecorder() {
        SAMPLE_RATE = getValidSampleRates();
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new short[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        new File(RECORD_WAV_PATH).mkdir();
    }

    private void startBufferedWrite(final File file) {
        rawLength = 0;
        audioRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;
                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    timer = new Timer();
                    MyTask myTask = new MyTask();
                    timer.schedule(myTask, 0, TIME_DELAY);
                    while (mIsRecording) {
                        if (!isPause) {
                            double sum = 0;
                            int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                            int numberOfByteInShortType = 2;
                            rawLength += readSize * numberOfByteInShortType;
                            for (int i = 0; i < readSize; i++) {
                                output.writeShort(mBuffer[i]);
                                sum += mBuffer[i] * mBuffer[i];
                            }

                            if (mListener != null) mListener.onAudioDataReceived(mBuffer);
                            if (readSize > 0) {
                                final double amplitude = sum / readSize;
                            }
                        }
                    }
                } catch (
                        IOException e) {
                    Log.e("Error writing file : ", e.getMessage());
                } finally {

                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            Log.e("Error writing file : ", e.getMessage());
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Log.e("Error writing file : ", e.getMessage());
                            }
                        }
                    }
                }
            }
        });
        audioRecordThread.start();
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
                rawFile.delete();
            }
        }

        Log.d("xxx", "Length: " + rawData.length);
        Log.d("xxx", "Sample rate: " + SAMPLE_RATE);
    }

    float getCurrentAudioDurationFromRawLength() {
        int channel = 1;
        int bitsPerSample = 16;
        return (float)(this.rawLength) / ((float)(SAMPLE_RATE * channel * bitsPerSample) / 8);
    }

    private File getFile(final String fileName, final String suffix) {
        return new File(RECORD_WAV_PATH, fileName + "." + suffix);
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value); //value >> 0
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value); //value >> 0
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }


}
