package com.ojsb.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button mBtnAudioTrack;
    private Button mBtnSoundPlayer;
    private Button mBtnExoPlayer;
    private Button mBtnMediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindUi();

        mBtnAudioTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AudioTrackActivity.class);
                startActivity(intent);
            }
        });

        mBtnMediaPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MediaPlayerActivity.class);
                startActivity(intent);
            }
        });

        mBtnExoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Go to ExoPlayer", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnSoundPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SoundPoolActivity.class);
                startActivity(intent);
            }
        });
    }

    private void bindUi() {
        mBtnAudioTrack = (Button) findViewById(R.id.btnAudioTrack);
        mBtnExoPlayer = (Button) findViewById(R.id.btnExoPlayer);
        mBtnMediaPlayer = (Button) findViewById(R.id.btnMediaPlayer);
        mBtnSoundPlayer = (Button) findViewById(R.id.btnSoundPool);
    }
}
