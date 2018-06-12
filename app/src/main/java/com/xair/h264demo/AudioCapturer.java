package com.xair.h264demo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

public class AudioCapturer {
    private static final String TAG = "AudioCapturer";

    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 8000;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static AudioRecord mAudioRecord;
    private static int mMinBufferSize = 0;

    private static Thread mCaptureThread;
    private static boolean mIsCaptureStarted = false;
    private static volatile boolean mIsLoopExit = false;
    private static OnAudioFrameCapturedListener mAudioFrameCapturedListener;


    public interface OnAudioFrameCapturedListener {
        public void onAudioFrameCaptured(short[] audioData);
    }
    public static boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }
    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }
    public static boolean startCapture() {
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }
    public static boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.d(TAG , "getMinBufferSize = "+mMinBufferSize+" bytes !");

        mAudioRecord = new AudioRecord(audioSource,sampleRateInHz
                ,channelConfig,audioFormat,mMinBufferSize);

        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }
        mAudioRecord.startRecording();
        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        mIsCaptureStarted = true;
        Log.d(TAG, "Start audio capture success !");
        return true;
    }
    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }
        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;
        Log.d(TAG, "Stop audio capture success !");
    }
    private static class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!mIsLoopExit) {
                short[] buffer = new short[320];
                int ret = mAudioRecord.read(buffer, 0, 320);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG , "Error ERROR_INVALID_OPERATION");
                }
                else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG , "Error ERROR_BAD_VALUE");
                }
                else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    byte[] data = new byte[320];
                    com.example.test.G711Code.G711aEncoder(buffer,data,ret);
                    //Log.d(TAG , "OK, Captured "+ret+" bytes !");
                    TCPSend.sendMsgThread(data);
                }

                SystemClock.sleep(10);
            }
        }
    }
}
