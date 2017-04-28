package com.ojsb.myapplication;

/**
 * Created by brady on 2017/4/27.
 */

public class AudioTrackConfig {
    protected final int samplingRate;
    protected final int channel;
    protected final int audioFormat;

    public AudioTrackConfig(int samplingRate, int channel, int audioFormat){
        this.samplingRate = samplingRate;
        this.channel = channel;
        this.audioFormat = audioFormat;
    }
}
