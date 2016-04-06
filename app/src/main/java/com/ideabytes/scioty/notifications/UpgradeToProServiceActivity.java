package com.ideabytes.scioty.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ideabytes.scioty.user.User;
import com.ideabytes.scioty.user.UserHandler;
import com.ideabytes.scioty.user.WelcomeActivity;
import com.ideabytes.scioty.utility.KiiOperations;
import com.ideabytes.scioty.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by ideabytes on 3/2/16.
 */
public class UpgradeToProServiceActivity extends Activity {
    private Button btnSkip;
    private GoogleCloudMessaging gcm;

    /**
     * method overridden from the super class
     * this method invokes when this class creates
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.upgrade_to_pro);
        btnSkip = (Button) findViewById(R.id.btnskipfornow);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View view) {
                final Intent myIntent = new Intent(UpgradeToProServiceActivity.this, WelcomeActivity.class);
                startActivity(myIntent);
                finish();
            }
        });
        final KiiOperations kiiOperations = new KiiOperations();
        kiiOperations.initialize();
        // get the instance of GoogleCloudMessaging.
        gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());

        // if the id is saved in the preference, it skip the registration and just install push.
        final String regId = GCMPreference.getRegistrationId(this.getApplicationContext());

        registerGCM(UpgradeToProServiceActivity.this);


    }


    private void registerGCM(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    KiiOperations kiiOperations = new KiiOperations();
                    final UserHandler userHandler = new UserHandler();
                    final User user = userHandler.populateUser(context);
                    String userId = user.getId();
                    String password = user.getPassword();
                    kiiOperations.registerPushNotification(context,
                            gcm, userId, password);
                } catch (Exception e) {

                }
                return null;
            }
        }.execute();
    }

}