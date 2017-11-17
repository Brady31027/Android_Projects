package com.ojsb.externalstorage;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String outputFolder = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        Log.v("Brady", outputFolder);
    }
}
