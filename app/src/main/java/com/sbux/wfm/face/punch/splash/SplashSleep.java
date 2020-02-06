package com.sbux.wfm.face.punch.splash;

import android.app.Application;
import android.os.SystemClock;

public class SplashSleep extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        SystemClock.sleep(2000);
    }
}
