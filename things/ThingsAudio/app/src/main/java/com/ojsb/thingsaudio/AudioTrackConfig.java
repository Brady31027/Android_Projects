package com.ojsb.thingsaudio;

/**
 * Created by brady on 2017/5/10.
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
