package com.ideabytes.scioty.user;


/**
 * Created by ideabytes on 3/8/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.ideabytes.scioty.R;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.KiiOperations;

public class ForgotPassword extends Activity {
    private final String TAG = "ForgotPassword";
    private EditText email_id;
    private String email;
    private ProgressBar progressBar;
    private TextView back;
    private Button requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "Info: ForgotPassword: onCreate: start");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.forgot_password);
        email_id = (EditText) findViewById(R.id.enter_email);
        progressBar = (ProgressBar) findViewById(R.id.progress_forgot);
        back = (TextView) findViewById(R.id.back);
        requestButton = (Button) findViewById(R.id.request_password);
        email_id.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                final boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.v(TAG, "Info: ForgotPassword: onCreate: request password button clicked");
                    requestButton.performClick();
                }
                return handled;
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View arg0) {
                Log.v(TAG, "Info: ForgotPassword: onCreate: back button clicked");
                finish();
            }
        });
        Log.v(TAG, "Info: ForgotPassword: onCreate: end");
    }


    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, MODE_PRIVATE);
        boolean isFinished = sharedPreferences.getBoolean(Constants.RESETED_PASSWORD, false);
        if (isFinished) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.RESETED_PASSWORD, false);
            editor.commit();
            finish();
        }
    }

    public void resetPassword(View v) {
        Log.v(TAG, "Info: ForgotPassword: resetPassword: start");
        email = email_id.getText().toString();
        if (email != null && !email.equals(Constants.EMPTY)) {
            progressBar.setVisibility(View.VISIBLE);
            setEnable(false);

            if (!new ConnectionDetector(this).isConnectingToInternet()) {
                Log.v(TAG, "Info: ForgotPassword: resetPassword: internet connection not available");
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_check),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... strings) {
                    String result = null;
                    try {
                        Log.v(TAG, "Info: ForgotPassword: resetPassword: doInBackground: start");
                        final String email = strings[0];
                        final KiiOperations kiiOperations = new KiiOperations();
                        result = kiiOperations.resetPassword(email);
                        Log.v(TAG, "Info: ForgotPassword: resetPassword: doInBackground: result " + result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error: ForgotPassword: resetPassword: error found in reset: message: " + e.getMessage());
                        e.printStackTrace();
                    }
                    Log.v(TAG, "Info: ForgotPassword: resetPassword: doInBackground: end");
                    return result;
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    Log.v(TAG, "Info: ForgotPassword: resetPassword: onPostExecute: start");
                    try {
                        progressBar.setVisibility(View.GONE);
                        setEnable(true);
                        if (result.equals(Constants.RESULT_SUCCESS)) {
                            Log.v(TAG, "Info: ForgotPassword: resetPassword: onPostExecute: success");
                            final Intent intent = new Intent(ForgotPassword.this, ResetForgotPassword.class);
                            startActivity(intent);
                        } else {
                            Log.v(TAG, "Info: ForgotPassword: resetPassword: onPostExecute: user not found");
                            Toast.makeText(ForgotPassword.this, getResources().getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error: ForgotPassword: resetPassword: error found in reset: message: " + e.getMessage());
                        e.printStackTrace();
                    }
                    Log.v(TAG, "Info: ForgotPassword: resetPassword: onPostExecute: end");
                }
            }.execute(new String[]{email});
        } else {
            Log.v(TAG, "Info: ForgotPassword: resetPassword: email is empty ");
            Toast.makeText(this, getResources().getString(R.string.Forgot_empty_email), Toast.LENGTH_SHORT).show();
        }
        Log.v(TAG, "Info: ForgotPassword: resetPassword: end");
    }


    private void setEnable(boolean b) {
        back.setEnabled(b);
        requestButton.setEnabled(b);
        email_id.setEnabled(b);
    }


}
