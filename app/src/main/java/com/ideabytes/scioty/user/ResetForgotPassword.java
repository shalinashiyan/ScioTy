package com.ideabytes.scioty.user;

/**
 * Created by ideabytes on 3/8/16.
 */
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ideabytes.scioty.R;
import com.ideabytes.scioty.utility.Constants;


public class ResetForgotPassword extends Activity {

    private Button okay;
    SharedPreferences sharedPreferences;
    static String MyPrfs = "prefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reset_forgot_password);
        okay = (Button) findViewById(R.id.ok);
    }

    public void onClickOk(View v) {
        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.RESETED_PASSWORD, true);
        editor.putBoolean(Constants.CHANGED_PASSWORD_AFTER_RESET,false);
        editor.commit();
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
