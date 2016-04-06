package com.ideabytes.scioty.user;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ideabytes.scioty.HomeScreenActivity;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.R;

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
public class CreateIOTAccount extends Activity {

    private final String TAG = "CreateIOTAccount";
    private TextView navigation;
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private EditText editUserName, editPhoneNumber;
    private Button createAccountButton;
    private ProgressBar progressBar;

    private String serverMessage;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: start");
        Log.v(TAG, "CreateAccount: onCreate: start");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // to set full screen mode ( removing title bar
        CreateIOTAccount.this
                .requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        CreateIOTAccount.this.requestWindowFeature(Window.PROGRESS_VISIBILITY_OFF);

        //to move the screen up when key board is active
        setProgressBarIndeterminateVisibility(false);

        // setting the layout of create account
        setContentView(R.layout.create_account);

        // hiding the progress bar
        progressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);
        progressBar.setVisibility(View.GONE);

        // getting view objects
        editUserName = (EditText) findViewById(R.id.etUserName);
        editPhoneNumber = (EditText) findViewById(R.id.etPhone);
        editTextEmail = (EditText) findViewById(R.id.etEmail);
        editTextPassword = (EditText) findViewById(R.id.etPassword);
        editTextConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        createAccountButton = (Button) findViewById(R.id.btn_create_account);
        navigation = (TextView) findViewById(R.id.navigation);

        //editUserName.setText("hello");
        //editTextEmail.setText("hello@test.com");
        //editPhoneNumber.setText("123456789");
        //editTextPassword.setText("123456");
        //editTextConfirmPassword.setText("123456");

        // to handle next in keyboard
        editTextConfirmPassword
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(final TextView v,
                                                  final int actionId, final KeyEvent event) {
                        //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: create account clicked");
                        Log.v(TAG, "CreateAccount: onCreate: create account clicked");
                        createAccountButton.performClick();
                        return true;
                    }
                });

        // on submit
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View arg0) {
                //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: onClick: start");
                Log.v(TAG, "CreateAccount start");
                final String userName = editUserName.getText().toString();
                final String phoneNumber = editPhoneNumber.getText().toString();
                final String emailId = editTextEmail.getText().toString();
                final String password = editTextPassword.getText()
                        .toString();
                final String confirmPasswordFromUser = editTextConfirmPassword
                        .getText().toString();

                try {
                    if (userName.equals("") || emailId.equals("") || phoneNumber.equals("")
                            || password.equals("")
                            || confirmPasswordFromUser.equals("")) {

                        //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: onClick: email/password/confirmPassword required");
                        Log.v(TAG, "CreateAccount: onCreate: onClick: user name/email/phone number/password/confirmPassword required");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields),
                                Toast.LENGTH_SHORT).show();
                    } else if (userName.length()<4 || emailId.length() < 4 || password.length() < 4) {
                        //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: onClick: email/password/confirmPassword minimum length should be 4 characters");
                        Log.v(TAG, "CreateAccount: onCreate: onClick: user name/email/password/confirmPassword minimum length should be 4 characters");
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.minimum_length),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (!password.equals(confirmPasswordFromUser)) {
                            //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: onClick: password and confirmPassword not matched");
                            Log.v(TAG, "CreateAccount: onCreate: onClick: password and confirmPassword not matched");
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_not_match),
                                    Toast.LENGTH_SHORT).show();

                            return;
                        }
                        ConnectionDetector cd;
                        cd = new ConnectionDetector(getApplicationContext());
                        if (!cd.isConnectingToInternet()) {
                            //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: onClick: internet connection not available");
                            Log.v(TAG, "CreateAccount: onCreate: onClick: internet connection not available");
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_check),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        setEnable(false);
                        final RegisterAsync registerAsync = new RegisterAsync();
                        registerAsync.execute(userName, emailId, phoneNumber, password);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        navigation.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View arg0) {
                //UtilityMethods.createFileWithContent("Info: CreateAccount: onCreate: Back: onClick: back button clicked");
                Log.v(TAG, "CreateAccount: onCreate: Back: onClick: back button clicked");
                final Intent myIntent = new Intent(CreateIOTAccount.this,
                        HomeScreenActivity.class);
                startActivity(myIntent);
                finish();
            }
        });

        Log.v(TAG, "CreateAccount: onCreate: end");
    }

    /**
     * To disable or enable the screen while calling server api
     *
     * @param disablestatus
     */
    private void setEnable(boolean disablestatus) {
        navigation.setEnabled(disablestatus);
        editUserName.setEnabled(disablestatus);
        editPhoneNumber.setEnabled(disablestatus);
        editTextEmail.setEnabled(disablestatus);
        editTextPassword.setEnabled(disablestatus);
        editTextConfirmPassword.setEnabled(disablestatus);
        createAccountButton.setEnabled(disablestatus);
    }

    /**
     * Background call running for register account in kii cloud
     */
    private class RegisterAsync extends AsyncTask<String, Void, Void> {
        private boolean isSuccess = false;
        private Exception exception;

        /**
         * @param lists
         * @return
         */
        @Override
        protected Void doInBackground(String... lists) {
            // UtilityMethods.createFileWithContent("Info: CreateAccount: RegisterAsync: doInBackground: start");
            Log.v(TAG, "CreateAccount: RegisterAsync: doInBackground: start");

            final String nameFromUser = lists[0];
            final String emailFromUser = lists[1];
            final String phoneFromUser = lists[2];
            final String passwordFromUser = lists[3];

            Log.v(TAG, "new user: " + nameFromUser + " " + emailFromUser + " " + phoneFromUser + " " + passwordFromUser);

            isSuccess = registerIOTServer(nameFromUser.trim(), emailFromUser.trim(), phoneFromUser.trim(), passwordFromUser.trim());

            Log.v(TAG, "CreateAccount: RegisterAsync: doInBackground: success");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref4", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("loginName", nameFromUser);  // Saving string
            editor.commit();

            //UtilityMethods.createFileWithContent("Info: CreateAccount: RegisterAsync: doInBackground: end");
            Log.v(TAG, "CreateAccount: RegisterAsync: doInBackground: end");
            return null;
        }


        protected void onPostExecute(Void result) {
            //super.onPostExecute(status);
            //UtilityMethods.createFileWithContent("Info: CreateAccount: RegisterAsync: onPostExecute: start");
            Log.v(TAG, "CreateAccount: RegisterAsync: onPostExecute: start");

            progressBar.setVisibility(View.GONE);
            setEnable(true);
            if (!isSuccess) {

                Toast.makeText(getApplicationContext(), serverMessage,
                        Toast.LENGTH_LONG).show();

            } else {

                    Log.v(TAG, "CreateAccount: RegisterAsync: onPostExecute: successfully register");
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("logincheck", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("loginch", true);
                    editor.commit();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_success),
                            Toast.LENGTH_LONG).show();
                    final Intent myIntent = new Intent(CreateIOTAccount.this, SignInIOT.class);
                    startActivity(myIntent);
                    finish();
            }

            Log.v(TAG, "CreateAccount: RegisterAsync: onPostExecute: end");
        }
    }

    public boolean registerIOTServer (String userName, String email, String phoneNumber, String password){
        boolean isSuccess = false;

        HttpClient httpclient  = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Constants.SERVER_URL_TO_POST_REGISTER);
        httppost.setHeader("content-type", "application/json");
        httppost.setHeader("Accept", "application/json");

        String device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        String ip = getIPAddress();

        String responseStr=null;
        JSONObject data = new JSONObject();

        try{
            data.put("loginName", userName);
            data.put("displayName", userName);
            data.put("country", "US");
            data.put("emailAddress", email);
            data.put("phoneNumber", phoneNumber);
            data.put("phoneNumberVerified", "true");
            data.put("password", password);
            data.put("device_id", device_id);
            data.put("ip_address", ip);

            Log.v(TAG, "data: " +  data.toString());
            try {

                httppost.setEntity(new StringEntity(data.toString(), "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                Log.v(TAG, "register user: " + userName + " " + email + " " + password);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    responseStr = EntityUtils.toString(resEntity).trim();
                    Log.v(TAG, "responseStr: " + responseStr);

                    try {
                        JSONObject json = new JSONObject(responseStr);
                        JSONObject json2 = json.getJSONObject("results");
                        String status = json2.getString("status");
                        if(status.equals("00"))
                            serverMessage = json2.getString("message");
                        else {
                            JSONObject json3 = json2.getJSONObject("message");
                            serverMessage = json3.getString("message");
                        }

                        Log.v(TAG, "response result: " + status + " " + serverMessage);

                        if(status.equals("00"))
                            isSuccess = true;

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

            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "error registration: " + e.getMessage());
                serverMessage = e.getMessage();
                e.printStackTrace();

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
        if(new ConnectionDetector(CreateIOTAccount.this).isConnectingToInternet()){
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            ip = wm.getConnectionInfo().getSSID();
        }

        ip = ip.replace("\"", "");
        return ip;
    }


}
