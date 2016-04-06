package com.ideabytes.scioty.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ideabytes.scioty.HomeScreenActivity;
import com.ideabytes.scioty.R;
import com.ideabytes.scioty.notifications.KiiPushBroadcastReceiver;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.KiiOperations;
import com.ideabytes.scioty.utility.UtilityMethods;
import com.ideabytes.scioty.utility.VarGlobal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.AppException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ideabytes on 3/11/16.
 */
public class SignInIOT extends Activity {
    // constants
    private final String TAG = "SignIn";
    private static final String OK = "OK";
    private static final String SIGNIN = "SignIN";
    private static final String ERROR_CODE = "errorCode";
    private TextView navigation;
    private EditText editTextEmail, editTextPassword;
    private Button signInButton;


    private GoogleCloudMessaging gcm;

    private ProgressBar progressBar;
    private long startTime;
    private TextView forgotPassword;
    private boolean isFirstTimeLogin = false;
    private boolean notificationnavi = false;
    private String serverMessage;
    private String accessToken;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "Info: SignIn: onCreate: start");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SignInIOT.this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        SignInIOT.this.requestWindowFeature(Window.PROGRESS_VISIBILITY_OFF);
        setContentView(R.layout.sign_in2);
        final Intent intent = getIntent();
        if (intent.hasExtra(Constants.NOTIFICATION_SIGN_OUT)) {
            notificationnavi = getIntent().getExtras().getBoolean(Constants.NOTIFICATION_SIGN_OUT);
        }
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                isFirstTimeLogin = extras.getBoolean(Constants.FIRST_TIME_LOGIN, false);
            }
        }
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());

        forgotPassword = (TextView) findViewById(R.id.forgot);
        progressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        progressBar.setVisibility(View.GONE);
        navigation = (TextView) findViewById(R.id.navigation);
        editTextEmail = (EditText) findViewById(R.id.etUserName);
        signInButton = (Button) findViewById(R.id.btnsignIn);


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Info: SignIn: onCreate: forgotPassword clicked");
                final Intent intent = new Intent(SignInIOT.this, ForgotPassword.class);
                startActivity(intent);
            }
        });

        editTextPassword = (EditText) findViewById(R.id.etPass);

        //editTextEmail.setText("hello");
        //editTextPassword.setText("123456");

        final KiiOperations kiiOperations = new KiiOperations();
        kiiOperations.initialize();

        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                final boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.v(TAG, "Info: SignIn: onCreate: SignIn clicked");
                    signInButton.performClick();
                }
                return handled;
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View arg0) {
                Log.v(TAG, "Info: SignIn: onCreate: onClick: start");
                startTime = System.currentTimeMillis();
                final String emailFromUser = editTextEmail.getText().toString();
                final String passwordFromUser = editTextPassword.getText()
                        .toString();
                if (emailFromUser.equals("") || passwordFromUser.equals("")) {
                    Log.v(TAG, "Info: SignIn: onCreate: onClick: email or password is empty");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields),
                            Toast.LENGTH_SHORT).show();
                } else if (emailFromUser.length() < 4 || passwordFromUser.length() < 4) {
                    Log.v(TAG, "Info: SignIn: onCreate: onClick: email or password is empty");
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.minimum_length),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "Info: SignIn: onCreate: onClick: checking internet connection");
                    if (new ConnectionDetector(SignInIOT.this).isConnectingToInternet()) {
                        Log.v(TAG, "Info: SignIn: onCreate: onClick: Internet connection available ");
                        progressBar.setVisibility(View.VISIBLE);
                        signInButton.setEnabled(false);
                        navigation.setEnabled(false);
                        editTextEmail.setEnabled(false);
                        editTextPassword.setEnabled(false);
                        final LoginInKiiAsync loginInKiiAsync = new LoginInKiiAsync();
                        loginInKiiAsync.execute(emailFromUser.trim(), passwordFromUser.trim());
                        //final EventsCount eventsCount = new EventsCount();
                        //eventsCount.execute();
                    } else {
                        Log.v(TAG, "Info: SignIn: onCreate: onClick: internet connection not available");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_check),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        navigation.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View arg0) {
                Log.v(TAG, "Info: SignIn: onCreate: Back: onClick: back button clicked");
                final Intent myIntent = new Intent(SignInIOT.this,
                        HomeScreenActivity.class);
                startActivity(myIntent);
                finish();
            }
        });



    }


    private class LoginInKiiAsync extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        private boolean isLoaded = false;
        private boolean isValidUser = false;

        @Override
        protected Boolean doInBackground(String... strings) {

            Log.v(TAG, "Info: SignIn: LoginInKiiAsync: doInBackground: start  ");
            final String emailFromUser = strings[0];
            final String passwordFromUser = strings[1];


            isValidUser = signInIOTServer(emailFromUser, passwordFromUser);

            if(isValidUser) {

                Log.v(TAG, "Info: SignIn: LoginInKiiAsync: doInBackground: login success, return token=" + accessToken);

                final UtilityMethods methods = new UtilityMethods();
                methods.writeAccessTokenToFile(SignInIOT.this, accessToken);
                final User user1 = new User(emailFromUser, passwordFromUser);
                final UserHandler userHandler = new UserHandler();
                userHandler.insertUser(SignInIOT.this, user1);
                Log.v(TAG, "Info: SignIn: LoginInKiiAsync: doInBackground: add user  ");
            }

            return isValidUser;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.v(TAG, "Info: SignIn: LoginInKiiAsync: onPostExecute: start");
            try {
                signInButton.setEnabled(true);
                navigation.setEnabled(true);
                editTextEmail.setEnabled(true);
                editTextPassword.setEnabled(true);
                progressBar.setVisibility(View.GONE);

                if(!aBoolean.booleanValue()){

                    Toast.makeText(getApplicationContext(), serverMessage,
                            Toast.LENGTH_LONG).show();

                    Intent myIntent = new Intent (SignInIOT.this, SignInIOT.class);
                    startActivity(myIntent);
                    finish();
                } else if (aBoolean.booleanValue()) {
                    Log.v(TAG, "Info: SignIn: LoginInKiiAsync: onPostExecute: loging success");

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("logincheck", MODE_PRIVATE);
                    isFirstTimeLogin = pref.getBoolean("loginch", false);
                    if (isFirstTimeLogin) {
                        Log.v(TAG, "Info: SignIn: LoginInKiiAsync: onPostExecute: first time login true");
                        final SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();

                    } //else {
                    Log.v(TAG, "Info: SignIn: LoginInKiiAsync: onPostExecute: load device list");

                    //    gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());
                    if(checkPlayServices()) {
                        registerGCM(SignInIOT.this);
                    }else{
                        String errorMessage = "This device is not supported GooglePlayServices.";
                        Toast.makeText(getApplicationContext(), errorMessage,
                                Toast.LENGTH_LONG).show();
                    }

                    //reset if any got notifications as loading everything here
                    KiiPushBroadcastReceiver.reset();


                    Intent myIntent = new Intent (SignInIOT.this, WelcomeActivity.class);
                    startActivity(myIntent);

                }
                finish();
                //}
            } catch (Exception e) {
                Log.v(TAG, "Error: SignIn: LoginInKiiAsync: onPostExecute: message: " + e.getMessage());
                e.printStackTrace();
            }

            Log.v(TAG, "Info: SignIn: LoginInKiiAsync: onPostExecute: end");
        }
    }

    public boolean signInIOTServer (String email, String password){
        boolean isSuccess = false;

        HttpClient httpclient  = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Constants.SERVER_URL_TO_POST_SIGNIN);
        httppost.setHeader("content-type", "application/json");
        httppost.setHeader("Accept", "application/json");

        String responseStr=null;
        JSONObject data = new JSONObject();

        String ip = getIPAddress();

        try{
            data.put("username", email);
            data.put("password", password);
            data.put("ipAddress", ip);

            Log.v(TAG, "data: " +  data.toString());
            try {

                httppost.setEntity(new StringEntity(data.toString(), "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                Log.v(TAG, "sign in user: " + email + " " + password);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    responseStr = EntityUtils.toString(resEntity).trim();
                    Log.v(TAG, "responseStr: " + responseStr);

                    try {
                        JSONObject json = new JSONObject(responseStr);
                        JSONObject json2 = json.getJSONObject("results");
                        String status = json2.getString("status");
                        if(status.equals("00")) {
                            serverMessage = json2.getString("message");
                            accessToken = json2.getString("accessToken");
                            isSuccess = true;
                        }
                        else {
                            JSONObject json3 = json2.getJSONObject("message");
                            serverMessage = json3.getString("error_description");
                        }

                        Log.v(TAG, "response result: " + status + " " + serverMessage);


                    } catch (JSONException e) {
                        Log.e(TAG, "error in jason parsing server response: " + e.getMessage());
                        serverMessage = e.getMessage();
                    }

                    // you can add an if statement here and do other actions based on the response
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "error in http: " + e.getMessage());
                serverMessage = e.getMessage();
                e.printStackTrace();
                responseStr = "Exception";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "error registration: " + e.getMessage());
                serverMessage = e.getMessage();
                e.printStackTrace();
                responseStr = "Exception";
            }


        } catch(JSONException e){
            Log.e(TAG, "error in jason for registration data: " + e.getMessage());
            serverMessage = e.getMessage();
            throw new RuntimeException(e);
        }

        return isSuccess;
    }


    private String getIPAddress(){
        String ip = " ";
        if(new ConnectionDetector(SignInIOT.this).isConnectingToInternet()){
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            ip = wm.getConnectionInfo().getSSID();
        }
        ip = ip.replace("\"", "");
        return ip;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Constants.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("SmartScaner", "This device is not supported GooglePlayServices.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerGCM(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    final KiiOperations kiiOperations = new KiiOperations();
                    final UserHandler userHandler = new UserHandler();
                    final User user = userHandler.populateUser(context);
                    final String userId = user.getId();
                    final String password = user.getPassword();

                    kiiOperations.registerPushNotification(context,
                            gcm, userId, password);
                    Log.v(TAG, "registerGCM() userId: " + userId + " password: " + password);
                } catch (Exception e) {

                }
                return null;
            }
        }.execute();
    }




}
