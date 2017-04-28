package com.ojsb.myapplication;

import android.media.AudioTrack;

/**
 * Created by brady on 2017/4/27.
 */

public class AudioTrackDevice {
    protected int streamType;
    protected AudioTrackConfig config;
    protected int minBufSize;
    protected AudioTrack track;
    protected int mode;

    public AudioTrackDevice(int streamType, AudioTrackConfig config, int mode) {
        this.streamType = streamType;
        this.config = config;
        this.mode = mode;

        this.minBufSize = AudioTrack.getMinBufferSize(
                this.config.samplingRate,
                this.config.channel,
                this.config.audioFormat);

        this.track = new AudioTrack( this.streamType,
                                     this.config.samplingRate,
                                     this.config.channel,
                                     this.config.audioFormat,
                                     this.minBufSize,
                                     this.mode);
    }

}
