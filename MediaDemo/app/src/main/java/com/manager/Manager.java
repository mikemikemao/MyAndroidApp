package com.manager;

public class Manager {
    public native int zzAudioRecord();
    static {
        System.loadLibrary("manager");
    }

}
