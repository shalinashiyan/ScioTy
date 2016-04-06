package com.ideabytes.scioty.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import com.ideabytes.scioty.HomeScreenActivity;
import com.ideabytes.scioty.R;
import com.ideabytes.scioty.scan.StartScannerActivity;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.KiiOperations;
import com.ideabytes.scioty.utility.VarGlobal;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.exception.app.AppException;

import java.io.IOException;

/**
 * Created by ideabytes on 3/4/16.
 */
public class WelcomeActivity extends Activity {
    private static final String TAG = "WelcomeActivity";

    private GoogleCloudMessaging gcm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.welcome_page);

        //gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());
        //registerGCM(WelcomeActivity.this);

        VarGlobal.isCameraInUse = 0;
        VarGlobal.request_user_ID=" ";
        //updateObject(getIPAddress(), true);
        //new KiiOperations().createNewGroup(Constants.GROUPNAME, Constants.GROUPID);
        //new KiiOperations().createGroupTopic(Constants.GROUPID, Constants.TOPICNAME);
        //new KiiOperations().subscribeGroupTopic(Constants.GROUPID, Constants.TOPICNAME);


    }

    public void signOut(View v){
        gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());
        unregisterGCM(WelcomeActivity.this);

        SharedPreferences prefs = WelcomeActivity.this.getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.ACCESS_TOKEN);
        editor.commit();

        Intent intent = new Intent(WelcomeActivity.this, HomeScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    private String getIPAddress(){
        String ip = " ";
        if(new ConnectionDetector(WelcomeActivity.this).isConnectingToInternet()){
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            ip = wm.getConnectionInfo().getSSID();
        }
        return ip;
    }

    private void unregisterGCM(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    final KiiOperations kiiOperations = new KiiOperations();
                    final UserHandler userHandler = new UserHandler();
                    final User user = userHandler.populateUser(context);
                    final String userId = user.getId();
                    final String password = user.getPassword();

                    kiiOperations.unregisterPushNotification(context,
                            gcm, userId, password);
                    Log.v(TAG, "unregisterGCM() userId: " + userId + " password: " + password);
                } catch (Exception e) {

                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }


}

