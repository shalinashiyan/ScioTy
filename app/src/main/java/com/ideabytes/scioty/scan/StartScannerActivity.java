package com.ideabytes.scioty.scan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import com.ideabytes.scioty.R;
import com.ideabytes.scioty.user.WelcomeActivity;
import com.ideabytes.scioty.utility.ConnectionDetector;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.KiiOperations;
import com.ideabytes.scioty.utility.VarGlobal;


import java.io.IOException;

/**
 * Created by ideabytes on 3/3/16.
 */
public class StartScannerActivity extends Activity{
    private final String TAG = "StartScannerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_scan2);

        if (isCameraAvailable()) {
            //this will get scan result from scanner class
            Intent intent = new Intent(StartScannerActivity.this,
                    ScannerActivity.class);
            startActivityForResult(intent, 0);
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(StartScannerActivity.this,
                    "Rear camera not available",
                    Toast.LENGTH_SHORT).show();
        }

    }

    /*
    public void launchScanner(View v) {
        if (isCameraAvailable()) {
            Intent intent = new Intent(StartScannerActivity.this, ScannerActivity.class);
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    public void exitScanner(View v) {
        VarGlobal.isScanning = 0;
        VarGlobal.request_user_ID=" ";

        Intent myIntent = new Intent(StartScannerActivity.this, WelcomeActivity.class);
        startActivity(myIntent);
        finish();

    }
    */

    public void exitScanner2(){
        VarGlobal.isCameraInUse = 0;
        VarGlobal.request_user_ID=" ";

        Intent myIntent = new Intent(StartScannerActivity.this, WelcomeActivity.class);
        startActivity(myIntent);
        finish();

        moveTaskToBack(true);
    }

    @Override
    public void onBackPressed() {
        VarGlobal.isCameraInUse = 0;
        VarGlobal.request_user_ID=" ";

        Intent myIntent = new Intent(StartScannerActivity.this, WelcomeActivity.class);
        startActivity(myIntent);
        finish();

        super.onBackPressed();

    }


    /**
     * This method checks whether camera available for device
     *
     * @return boolean
     */
    // this method used in scanning a bar/QR code
    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // Getting bar or QR code results from scanner activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        String scanResult = data.getStringExtra(Constants.SCAN_RESULT);
                        // set scanner result to text view on main screen
                        TextView textView = (TextView) findViewById(R.id.scanResult);
                        textView.setText(scanResult);

                        Log.v(TAG, "Bar Code ==: " + scanResult);

                        //new KiiPushBroadcastReceiver().sendGroupNotification(Constants.GROUPID, Constants.TOPICNAME, scanResult);
                        //send scan result to server
                        AsyncTaskSendData asyncTaskToSendData = new AsyncTaskSendData(getApplicationContext());
                        asyncTaskToSendData.execute(scanResult);

                       try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e) {
                            Log.e(TAG, "thread sleep: " + e.getMessage());
                        }

                        exitScanner2();

                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
                        String error = data.getStringExtra(Constants.ERROR_INFO);
                        if (!TextUtils.isEmpty(error)) {
                            // Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Bar Code" +
                                    " error ==: " + error);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

}
