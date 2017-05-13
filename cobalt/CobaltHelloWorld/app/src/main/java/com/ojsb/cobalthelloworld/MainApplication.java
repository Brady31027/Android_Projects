package com.ojsb.cobalthelloworld;

import android.app.Application;
import org.cobaltians.cobalt.Cobalt;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //setContentView(R.layout.activity_main);
        Cobalt.getInstance(this).setResourcePath("www/common/");
    }
}
