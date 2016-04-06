package com.ideabytes.scioty.myapp;

/**
 * Created by ideabytes on 2/26/16.
 */

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ForeGround implements Application.ActivityLifecycleCallbacks {


    private static ForeGround instance;


    private boolean isForeground = false;

    public static void init(Application app) {
        if (instance == null) {
            instance = new ForeGround();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static ForeGround get() {
        return instance;
    }

    private ForeGround() {
    }

    public boolean isForeground() {
        return isForeground;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        isForeground=true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isForeground=false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}

