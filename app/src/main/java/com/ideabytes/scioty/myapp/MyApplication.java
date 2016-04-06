package com.ideabytes.scioty.myapp;

/**
 * Created by ideabytes on 2/26/16.
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

import com.ideabytes.scioty.utility.Constants;

public class MyApplication extends Application {

    public static boolean isForeground = false;
    public static String versionName;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        System.err.println("MyApplication.onCreate");
        ForeGround.init(this);

        try {

            final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            SharedPreferences pref = getSharedPreferences(Constants.MY_PREFERENCE, MODE_PRIVATE);
            final boolean aBoolean = pref.getBoolean(Constants.DISABLED, false);
            if (!aBoolean) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(Constants.LOG_STATUS, true);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
