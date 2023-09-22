package com.android.services.nativePackage;

import android.content.Context;
import android.media.AudioManager;

import com.android.services.MyApplication;


public class CallRecorderFixHelper {

    static final class Singleton {
        static final CallRecorderFixHelper INSTANCE = new CallRecorderFixHelper();

        private Singleton() {
        }
    }

    public static CallRecorderFixHelper getInstance() {
        return Singleton.INSTANCE;
    }

    public void initialize() {
        AudioManager myAudioMgr = (AudioManager) MyApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        int nativeSampleRate = Integer.parseInt(myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        int nativeSampleBufSize = Integer.parseInt(myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
        load(nativeSampleRate, nativeSampleBufSize);
    }

    public void startFixe() {
        startCallFix();
    }

    public void stopFixe() {
        stopCallFix();
    }

    public static native int load(int sampleRate, int nativeBufSize);
    public static native int startCallFix();
    public static native int stopCallFix();

    static {
        System.loadLibrary("echo");
    }

}
