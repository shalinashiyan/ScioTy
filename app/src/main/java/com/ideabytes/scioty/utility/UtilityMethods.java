package com.ideabytes.scioty.utility;

/**
 * Created by ideabytes on 2/26/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Date;
import java.text.DateFormat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


import com.ideabytes.scioty.myapp.MyApplication;
import com.ideabytes.scioty.HomeScreenActivity;
import com.ideabytes.scioty.R;

public class UtilityMethods {
    private static final String SOURCE_FOLDER = "Android/data/scioty";
    private static final String FILE_NAME = "accesstoken.txt";

    public static String getGMTdiff() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        String localTime = date.format(currentLocalTime);
        return localTime;
    }

    public static String getConvertedDateToYYYYMMDD(String date) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy"); // Set your date format
            Date d = sdf1.parse(date); // Current time
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            String currentData = sdf2.format(d);
            return currentData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getTimeInMillis(String str) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String timeZone = "GMT" + UtilityMethods.getGMTdiff();
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
            final Date date = sdf.parse(str);
            System.err.println("in milliseconds: " + date.getTime());
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getAccessToken(Context context) {

        try {
            SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
            String content = pref.getString(Constants.ACCESS_TOKEN, null);
            return content;
        } catch (Exception e) {

        }
        return null;
    }

    public String getNewAccessToken(final Activity context, final String emailId, final String password) {
        UtilityMethods.createFileWithContent("Info: UtilityMethods: getNewAccessToken: start");
        try {
            final KiiOperations kiiOperations = new KiiOperations();
            final String newAccessToken = kiiOperations.getNewAccessToken(emailId, password);
            if (newAccessToken != null) {
                writeAccessTokenToFile(context, newAccessToken);
                UtilityMethods.createFileWithContent("Info: UtilityMethods: getNewAccessToken: got new access token");
                return newAccessToken;
            }
            UtilityMethods.createFileWithContent("Info: UtilityMethods: getNewAccessToken: unable to get new accessToken");
            UtilityMethods.createFileWithContent("Info: UtilityMethods: getNewAccessToken: show alert to re-login");
            final AlertDialog alertDialog = new AlertDialog.Builder(
                    context).create();
            alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertDialog.setCancelable(false);
            final View view = LayoutInflater.from(context).inflate(R.layout.alert_layout, null);
            final TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(context.getResources().getString(R.string.dev_login_failed));
            final TextView relogin = (TextView) view.findViewById(R.id.ok);
            relogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        UtilityMethods.createFileWithContent("Error: UtilityMethods: getNewAccessToken: ReloginAlert: onClick: start ");
                        final SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove(Constants.ACCESS_TOKEN);
                        editor.commit();
                        final Intent intent = new Intent(context, HomeScreenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                        context.finish();
                        alertDialog.dismiss();
                    } catch (Exception e) {
                        UtilityMethods.createFileWithContent("Error: UtilityMethods: getNewAccessToken: ReloginAlert: onClick: message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            alertDialog.setView(view);
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            UtilityMethods.createFileWithContent("Error: UtilityMethods: getNewAccessToken: message: " + e.getMessage());
        }
        UtilityMethods.createFileWithContent("Info: UtilityMethods: getNewAccessToken: end");
        return null;
    }

    public static File createFileWithContent(String content) {
        System.err.println(content);
        SharedPreferences pref = MyApplication.context.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        System.err.println("print pref");
        boolean b = pref.getBoolean(Constants.LOG_STATUS, false);
        if (b) {
            System.err.println("get date");
            final String format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
            System.err.println("print date");
            System.err.println(format);
            content = format + ": " + content;

            System.err.println(content);
            File file = null;
            try {
                file = new File(Constants.LOG_REPORT_FOLDER);
                if (!file.exists()) {
                    file.createNewFile();
                    content = "";
                    final String versionName = MyApplication.versionName;
                    content = "App version " + versionName + "\n";
                    String deviceName = android.os.Build.MODEL;

                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    content += "Device: " + " " + deviceName + " " + currentapiVersion;
                }
                FileOutputStream fOut = new FileOutputStream(file, true);
                BufferedWriter myOutWriter =
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                myOutWriter.append(content);
                myOutWriter.newLine();
                myOutWriter.flush();
                myOutWriter.append('\n');
                myOutWriter.close();
                fOut.flush();
                fOut.close();

            } catch (Exception e) {
                e.printStackTrace();

            }
            return file;
        }
        return null;
    }

    public void writeAccessTokenToFile(final Context context, final String accessToken) {
        try {
            SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(Constants.ACCESS_TOKEN, accessToken);
            editor.commit();
        } catch (Exception e) {

        }
    }


    /**
     * method returns the size of the circle to be set depending on the height of screen
     *
     * @param deviceHeight
     * @return int
     */
    public int getSizeOfSwipeCircle(final int deviceHeight) {

        if (deviceHeight <= 350) {
            return 6;
        } else if (deviceHeight <= 500) {
            return 8;
        } else if (deviceHeight <= 900) {
            return 10;
        } else if (deviceHeight <= 1350) {
            return 15;
        } else if (deviceHeight <= 2000) {
            return 25;
        } else {
            return 35;
        }
    }
    /**
     * method used to get the height of the device
     *
     * @param activity
     * @return int
     */

    public int getDeviceHeight(final Activity activity) {

        final Display display = activity.getWindowManager().getDefaultDisplay();
        final DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return outMetrics.heightPixels;

    }




}
