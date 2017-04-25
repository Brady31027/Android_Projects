package com.ojsb.myapplication;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SoundPoolActivity extends AppCompatActivity {

    private SoundPool mSoundPool;
    private Button mBtnMakeSound;
    private Button mBtnPitchHalf;
    private Button mBtnPitchOne;
    private Button mBtnPitchDouble;
    private TextView mTextPitchRate;

    private int mSoundId;
    private boolean mLoadCompleted;
    private float mPitchRate;

    final int TRY_CNT_LIMIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);
        initVariables();
        bindUi();
        createSoundPool();
        loadAudioSource();

        mBtnMakeSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int try_cnt = 0;
                while (!mLoadCompleted && try_cnt < TRY_CNT_LIMIT){
                    try {
                        Thread.sleep(100); // 0.1 sec
                    }catch(Exception e){
                        return;
                    }
                    try_cnt += 1;
                }
                if (mLoadCompleted)
                    mSoundPool.play(mSoundId, 1.0f, 1.0f, 1, 0, mPitchRate);
            }
        });


        mBtnPitchHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextPitchRate.setText("Pitch: 0.5X");
                mPitchRate = 0.5f;
            }
        });

        mBtnPitchOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextPitchRate.setText("Pitch: 1X");
                mPitchRate = 1.0f;
            }
        });

        mBtnPitchDouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextPitchRate.setText("Pitch: 2X");
                mPitchRate = 2.0f;
            }
        });
    }

    private void initVariables() {
        mSoundId = -1;
        mLoadCompleted = false;
        mPitchRate = 1.0f;
    }

    private void loadAudioSource() {

        // parameters
        // 1st: context
        // 2nd: resource id
        // 3rd: priority
        mSoundId = mSoundPool.load(SoundPoolActivity.this, R.raw.sheep_sound, 1);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoadCompleted = true;
                Toast.makeText(SoundPoolActivity.this, "Loaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createSoundPool() {
        // create SoundPool builder object
        SoundPool.Builder soundPoolBuilder = new SoundPool.Builder();

        // create AudioAttributes builder and set it up
        AudioAttributes.Builder audioAttrBuilder = new AudioAttributes.Builder();
        audioAttrBuilder.setUsage(AudioAttributes.USAGE_GAME);
        audioAttrBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);

        // in our case, we only need one sound stream -- sheep sound
        soundPoolBuilder.setMaxStreams(1);
        soundPoolBuilder.setAudioAttributes(audioAttrBuilder.build());
        mSoundPool = soundPoolBuilder.build();

    }

    private void bindUi() {
        mBtnMakeSound = (Button) findViewById(R.id.btn_makesound);
        mBtnPitchHalf = (Button) findViewById(R.id.btn_pitch_half);
        mBtnPitchOne = (Button) findViewById(R.id.btn_pitch_one);
        mBtnPitchDouble = (Button) findViewById(R.id.btn_pitch_double);
        mTextPitchRate = (TextView) findViewById(R.id.text_pitch_rate);
    }
}
