package com.ojsb.thingsaudio;

import android.media.AudioTrack;

/**
 * Created by brady on 2017/5/10.
 */

public class AudioTrackDevice {

    protected AudioTrackConfig config;
    protected AudioTrack track;
    protected int streamType;
    protected int minBufSize;
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
