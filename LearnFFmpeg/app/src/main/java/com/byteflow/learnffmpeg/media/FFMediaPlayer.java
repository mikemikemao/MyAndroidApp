package com.byteflow.learnffmpeg.media;

import android.view.Surface;

public class FFMediaPlayer {
    //gl render type
    static {
        System.loadLibrary("learn-ffmpeg");
    }

    private long mNativePlayerHandle = 0;

    private EventCallback mEventCallback = null;

    public static String GetFFmpegVersion() {
        return native_GetFFmpegVersion();
    }

    private static native String native_GetFFmpegVersion();

    public interface EventCallback {
        void onPlayerEvent(int msgType, float msgValue);
    }

    public void seekToPosition(float position) {
        native_SeekToPosition(mNativePlayerHandle, position);
    }

    public void init(String url, int videoRenderType, Surface surface) {
        mNativePlayerHandle = native_Init(url, videoRenderType, surface);
    }

    private native void native_SeekToPosition(long playerHandle, float position);

    public void play() {
        native_Play(mNativePlayerHandle);
    }
    private native void native_Play(long playerHandle);

    public void pause() {
        native_Pause(mNativePlayerHandle);
    }
    private native void native_Pause(long playerHandle);

    public void stop() {
        native_Stop(mNativePlayerHandle);
    }
    private native void native_Stop(long playerHandle);

    public void addEventCallback(EventCallback callback) {
        mEventCallback = callback;
    }

    private native long native_Init(String url, int renderType, Object surface);

    public void unInit() {
        native_UnInit(mNativePlayerHandle);
    }
    private native void native_UnInit(long playerHandle);


    public long getMediaParams(int paramType) {
        return native_GetMediaParams(mNativePlayerHandle, paramType);
    }

    private native long native_GetMediaParams(long playerHandle, int paramType);

    private void playerEventCallback(int msgType, float msgValue) {
        if(mEventCallback != null)
            mEventCallback.onPlayerEvent(msgType, msgValue);

    }



}