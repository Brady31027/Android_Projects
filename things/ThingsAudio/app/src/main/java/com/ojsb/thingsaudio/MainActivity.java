package com.ojsb.thingsaudio;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;

public class MainActivity extends Activity {

    private static final String TAG = "Brady-Android-Things";
    private InputStream in;
    private DataInputStream din;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // setup audio config
        AudioTrackConfig config = new AudioTrackConfig(44100, /* sampling rate */
                AudioFormat.CHANNEL_OUT_STEREO, /* channel */
                AudioFormat.ENCODING_PCM_16BIT); /* encoding */

        // create audio device
        AudioTrackDevice device = new AudioTrackDevice(AudioManager.STREAM_MUSIC,
                config,
                AudioTrack.MODE_STREAM);

        playAudio(device);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        try {
            if (in.available() != 0) in.close();
            if (din.available() != 0) din.close();
        }catch (Exception e){
            Log.d(TAG, "Exception raises during closing audio stream");
        }
    }

    private void playAudio(AudioTrackDevice device){
        in = getResources().openRawResource(R.raw.nuke);
        din = new DataInputStream(in);
        byte[] buf = new byte[1024];
        AudioTrack audioTrack = device.track;
        int size = 0;
        audioTrack.play();
        Log.d(TAG, "Start playing audio stream");
        try {
            // Do we have to consider the offset of the data input stream?
            while ( (size = din.read(buf, 0, 1024)) > -1) {
                audioTrack.write(buf, 0, size);
            }
            Log.d(TAG, "Finished playing audio stream");
            audioTrack.stop();
            in.close();
            din.close();
        }catch(Exception e){
            Log.d(TAG, "Exception raises during playing audio stream");
            return;
        }

    }
}
