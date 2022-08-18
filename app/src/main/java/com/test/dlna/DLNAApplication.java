package com.test.dlna;

import android.app.Application;
import android.content.Context;

public class DLNAApplication extends Application {
    private final static String TAG = "DLNAApplication";
    private static DLNAApplication instance;
    private boolean isConnect = false;

    public static DLNAApplication getInstance() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }
}
