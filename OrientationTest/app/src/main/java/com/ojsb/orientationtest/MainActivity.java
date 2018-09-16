package com.ojsb.orientationtest;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private static boolean sOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActivityOrientation();

        setContentView(R.layout.activity_main);

    }

    private void setActivityOrientation() {
        if (sOrientation) return;

        int surf_orientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRotation();

        Log.v("[Brady]", "Init Surface Orientation : " + String.valueOf(surf_orientation));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE /* 2 */) {
            Log.v("[Brady]", "Device Screen Orientation : Landscape ");

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            try {
                Thread.sleep(1000);

                int orientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay()
                        .getRotation();

                Log.v("[Brady]", "Changed Surface orientation : " + String.valueOf(orientation));


            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else /* 1 */{
            Log.v("[Brady]", "Init screen orientation : portrait");

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            try {
                Thread.sleep(1000);

                int orientation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay()
                        .getRotation();

                Log.v("[Brady]", "Surface Orientation After Requested : " + String.valueOf(orientation));


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sOrientation = true;


    }
}
