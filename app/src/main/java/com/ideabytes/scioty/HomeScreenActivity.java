package com.ideabytes.scioty;

/**
 * Created by ideabytes on 2/26/16.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ideabytes.scioty.notifications.KiiPushBroadcastReceiver;
import com.ideabytes.scioty.user.CreateIOTAccount;
import com.ideabytes.scioty.user.SignInIOT;
import com.ideabytes.scioty.user.WelcomeActivity;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.UtilityMethods;
import com.ideabytes.scioty.utility.KiiOperations;


import com.kii.cloud.storage.KiiUser;

public class HomeScreenActivity extends Activity {
    private static final String SOURCE_FOLDER = "Android/data";
    private static final String FILE_NAME = "accesstoken.txt";

    private final String TAG = "HomeScreenActivity";

    private HorizontalPagerWithPageControl mPager;
    TextView createAccount;
    TextView signIn;
    private ProgressBar progressBar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.horizontal_pager_with_page_control);
        LinearLayout layout = (LinearLayout) findViewById(R.id.linear);
        mPager = (HorizontalPagerWithPageControl) findViewById(R.id.horizontal_pager);
        /*
         * We can add more views to the horizontal pager here with
		 * mPager.addChild() or in xml. When every view is in the horizontal
		 * pager just call addPagerControl() on the horzizontal pager.
		 */
        mPager.addPagerControl(layout);

        createAccount = (TextView) findViewById(R.id.btnCreateAccount);
        signIn = (TextView) findViewById(R.id.btnSignIN);

        progressBar = (ProgressBar) findViewById(R.id.progress_in_auto_signin);

        createAccount.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                // navigating to launcher screen
                Intent myIntent = new Intent (HomeScreenActivity.this, CreateIOTAccount.class);
                startActivity(myIntent);
                finish();
            }
        });

        signIn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                // navigating to launcher screen
                Intent myIntent = new Intent(HomeScreenActivity.this, SignInIOT.class);
                startActivity(myIntent);
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (new ConnectionDetector(this).isConnectingToInternet()) {
            signIn.setEnabled(false);
            createAccount.setEnabled(false);
            mPager.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            AutoSignIn autoSignIn = new AutoSignIn();
            autoSignIn.execute();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_check),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class AutoSignIn extends AsyncTask<Void, Void, Boolean> {

        private boolean isValidUser = false;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                final UtilityMethods methods = new UtilityMethods();
                String content = methods.getAccessToken(HomeScreenActivity.this);
                Log.v(TAG, "autologin with accessToken: " + content);
                if (content != null) {
                    final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    final KiiOperations kiiOperations = new KiiOperations();
                    kiiOperations.initialize();
                    final KiiUser kiiUser = kiiOperations.loginWithAccessToken(content.trim());
                    if (kiiUser != null) {
                        isValidUser = true;
                    }

                    return Boolean.TRUE;
                }
            } catch (Exception e) {
                Log.e(TAG, "autologin failed: " + e.getMessage());
                e.printStackTrace();
            }

            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            try {
                progressBar.setVisibility(View.GONE);
                signIn.setEnabled(true);
                createAccount.setEnabled(true);
                mPager.setEnabled(true);
                if (aBoolean.booleanValue()) {

                    //reset if any got notifications as loading everything here
                    KiiPushBroadcastReceiver.reset();

                    final Intent myIntent = new Intent(
                            HomeScreenActivity.this,
                            WelcomeActivity.class);
                    startActivity(myIntent);
                    finish();
                } else {
                    if (isValidUser) {
                        Log.e(TAG, "Info: Alert: HomeScreenActivity: onPostExecute: failed to load");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
