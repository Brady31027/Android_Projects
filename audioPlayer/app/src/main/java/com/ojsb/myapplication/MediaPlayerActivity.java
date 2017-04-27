package com.ojsb.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MediaPlayerActivity extends Activity {

    private TextView mTextState;
    private Button mBtnPlay;
    private Button mBtnStop;
    private Button mBtnPause;

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

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateState(MediaPlayerState.STATE_STARTED);
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateState(MediaPlayerState.STATE_PAUSED);
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateState(MediaPlayerState.STATE_STOPPED);
            }
        });
    }

    private void bindUi() {
        mTextState = (TextView) findViewById(R.id.textState);
        mBtnPause = (Button) findViewById(R.id.btnPause);
        mBtnPlay = (Button) findViewById(R.id.btnPlay);
        mBtnStop = (Button) findViewById(R.id.btnStop);
    }
}
