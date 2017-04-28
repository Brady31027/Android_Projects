package com.ojsb.myapplication;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AudioTrackActivity extends Activity {

    private TextView mTextState;
    private Button mBtnMono8;
    private Button mBtnStereo8;
    private Button mBtnMono16;
    private Button mBtnStereo16;
    private Button mBtnPlay;
    private Button mBtnStop;
    private Button mBtnPause;
    private String[] mConfig = {"M8", "S8", "M16", "S16"};
    private HashMap<String, AudioTrack> mDeviceHash;
    private InputStream in;
    private DataInputStream din;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track);
        bindUi();
        setupDevices();

        mBtnMono8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic("M8");
            }
        });

        mBtnStereo8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic("S8");
            }
        });

        mBtnMono16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic("M16");
            }
        });

        mBtnStereo16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic("S16");
            }
        });
    }

    private void bindUi() {
        mTextState = (TextView) findViewById(R.id.text_state);
        mBtnMono8 = (Button) findViewById(R.id.btn_mono_8);
        mBtnStereo8 = (Button) findViewById(R.id.btn_stereo_8);
        mBtnMono16 = (Button) findViewById(R.id.btn_mono_16);
        mBtnStereo16 = (Button) findViewById(R.id.btn_stereo_16);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
    }

    private void setupDevices() {
        mDeviceHash = new HashMap<>();

        // add mono 8 config
        AudioTrackConfig m8Config = new AudioTrackConfig(44100,
                                      AudioFormat.CHANNEL_OUT_MONO,
                                      AudioFormat.ENCODING_PCM_8BIT);
        // add stereo 8 config
        AudioTrackConfig s8Config = new AudioTrackConfig(44100,
                                      AudioFormat.CHANNEL_OUT_STEREO,
                                      AudioFormat.ENCODING_PCM_8BIT);

        // add mono 16 config
        AudioTrackConfig m16Config = new AudioTrackConfig(44100,
                                       AudioFormat.CHANNEL_OUT_MONO,
                                       AudioFormat.ENCODING_PCM_16BIT);

        // add stereo 16 config
        AudioTrackConfig s16Config = new AudioTrackConfig(44100,
                                       AudioFormat.CHANNEL_OUT_STEREO,
                                       AudioFormat.ENCODING_PCM_16BIT);


        // create device base on config
        AudioTrackDevice m8Device = new AudioTrackDevice(AudioManager.STREAM_MUSIC,
                                                         m8Config,
                                                         AudioTrack.MODE_STREAM);

        AudioTrackDevice s8Device = new AudioTrackDevice(AudioManager.STREAM_MUSIC,
                                                         s8Config,
                                                         AudioTrack.MODE_STREAM );

        AudioTrackDevice m16Device = new AudioTrackDevice(AudioManager.STREAM_MUSIC,
                                                          m16Config,
                                                          AudioTrack.MODE_STREAM );

        AudioTrackDevice s16Device = new AudioTrackDevice(AudioManager.STREAM_MUSIC,
                                                          s16Config,
                                                          AudioTrack.MODE_STREAM );

        mDeviceHash.put("M8", m8Device.track);
        mDeviceHash.put("S8", s8Device.track);
        mDeviceHash.put("M16", m16Device.track);
        mDeviceHash.put("S16", s16Device.track);
    }

    private boolean removePlayingAudio() {
        boolean isSomeonePlayingOrPaused = false;
        for (String config : mConfig) {
            AudioTrack track = mDeviceHash.get(config);
            if (track.getPlayState() > AudioTrack.PLAYSTATE_STOPPED ){
                isSomeonePlayingOrPaused = true;
                track.stop();
            }
        }
        return isSomeonePlayingOrPaused;
    }

    // TODO: move to another worker thread
    private void playMusic(String config){

        boolean isSomeonePlayingOrPaused = removePlayingAudio();
        if (isSomeonePlayingOrPaused) {
            try {
                in.close();
                din.close();
            }catch(IOException e) {
                Log.e("[Brady]", "Cannot remove existed audio");
                return;
            }
        }

        in = getResources().openRawResource(R.raw.nuke);
        din = new DataInputStream(in);
        byte[] buffer = new byte[1024];
        AudioTrack track = mDeviceHash.get(config);
        int size = 0;
        track.play();
        try {
            while ( (size = din.read(buffer, 0, 1024)) > -1 ) {
                track.write(buffer, 0, size); // blocking ops
            }
            track.stop();
            in.close();
            din.close();
        } catch (IOException e) {
            Log.e("Brady", "Playing failed");
            removePlayingAudio();
            return;
        }
    }
}
