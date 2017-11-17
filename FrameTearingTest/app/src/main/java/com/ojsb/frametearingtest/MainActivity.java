package com.ojsb.frametearingtest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private LinearLayout mRootLayout;
    static boolean mBlackBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView(R.layout.activity_main);

        mRootLayout = (LinearLayout) findViewById(R.id.root_layout);
        mBlackBackground = true;


        mRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBlackBackground) {
                    mRootLayout.setBackgroundColor(Color.WHITE);
                } else {
                    mRootLayout.setBackgroundColor(Color.BLACK);
                }
                mBlackBackground = !mBlackBackground;
            }
        });
    }
}
