package com.ojsb.myapplication;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MediaPlayerActivity extends Activity {

    // TODO: add streaming and more file format
    // TODO: add progress showing
    // TODO: add seeking
    
    private TextView mTextState;
    private Button mBtnPlay;
    private Button mBtnStop;
    private Button mBtnPause;
    private MediaPlayerState mCurState;
    private MediaPlayer player;

    private void updateState(MediaPlayerState state) {
        String stateMsg = "State : IDLE";
        switch (state) {
            case STATE_END:
                stateMsg = "State : END";
                break;
            case STATE_ERROR:
                stateMsg = "State : ERROR";
                break;
            case STATE_IDLE:
                stateMsg = "State : IDLE";
                break;
            case STATE_INIT:
                stateMsg = "State : INIT";
                break;
            case STATE_PAUSED:
                stateMsg = "State : PAUSED";
                break;
            case STATE_PLAYBACKCOMPLETED:
                stateMsg = "State : PLAYBACKCOMPLETED";
                break;
            case STATE_PREPARED:
                stateMsg = "State : PREPARED";
                break;
            case STATE_PREPARING:
                stateMsg = "State : PREPARING";
                break;
            case STATE_STARTED:
                stateMsg = "State : SRARTED";
                break;
            case STATE_STOPPED:
                stateMsg = "State : STOPPED";
                break;
        }

        updateStateTextView(stateMsg);
    }

    private void updateStateTextView(String stateMsg) {
        final String s = stateMsg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextState.setText(s);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        bindUi();
        setupPlayer();

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
                mCurState = MediaPlayerState.STATE_STARTED;
                updateState(MediaPlayerState.STATE_STARTED);
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurState == MediaPlayerState.STATE_STARTED) {
                    player.pause();
                    mCurState = MediaPlayerState.STATE_PAUSED;
                    updateState(MediaPlayerState.STATE_PAUSED);
                }
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.stop();
                mCurState = MediaPlayerState.STATE_STOPPED;
                updateState(MediaPlayerState.STATE_STOPPED);

                // delete the current player
                player.release();
                player = null;

                // create a new player
                player = MediaPlayer.create(MediaPlayerActivity.this, R.raw.byte_music);
            }
        });
    }

    private void bindUi() {
        mTextState = (TextView) findViewById(R.id.textState);
        mBtnPause = (Button) findViewById(R.id.btnPause);
        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        mBtnStop = (Button) findViewById(R.id.btnStop);
    }

    private void setupPlayer() {
        // local file, use create()
        player = MediaPlayer.create(MediaPlayerActivity.this, R.raw.byte_music);
        updateState(MediaPlayerState.STATE_PREPARED);
    }
}
