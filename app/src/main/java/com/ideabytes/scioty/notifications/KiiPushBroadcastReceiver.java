package com.ideabytes.scioty.notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.ideabytes.scioty.camera.CameraActivity;
import com.ideabytes.scioty.myapp.ForeGround;
import com.ideabytes.scioty.scan.StartScannerActivity;
import com.ideabytes.scioty.utility.Constants;

import com.ideabytes.scioty.utility.VarGlobal;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiPushMessage;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.exception.GroupOperationException;
import com.kii.cloud.storage.exception.app.AppException;

import java.io.IOException;

/**
 * Created by ideabytes on 3/2/16.
 */


public class KiiPushBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "KiiPushReceiver";
    private static final String OK = "Ok";
    private static final String CANCEL = "Cancel";
    private static boolean ignoreNotication = false;
    private static boolean isRe_Loaded = false;

    private static final String SOURCE_FOLDER = "Android/data";
    private static final String FILE_NAME = "accesstoken.txt";
    public static String message = null;
    private static long lastReloadedTime = -1;
    private Context context;

    public static void reset() {

        isRe_Loaded = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.v(TAG, "onReceive called");
        if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String gcmMessageType = gcm.getMessageType(intent);

            if (gcmMessageType != null) {
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(gcmMessageType)) {
                    messageReceived(context, intent);
                    Log.e(TAG, "Error occurred while gcm messge sending.");
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(gcmMessageType)) {
                    Log.i(TAG, "Received deleted messages notification");
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(gcmMessageType)) {
                    messageReceived(context, intent);
                }
                setResultCode(Activity.RESULT_OK);
            } else {
                Log.e(TAG, "Unknown message type.");
            }
        }
    }


    protected void messageReceived(Context context, Intent intent) {
        // fileLog(TAG, "Received message :" + intent.getExtras().toString());

        // fileLog(TAG, "Time: " + System.currentTimeMillis());
        System.err.println("KiiPushBroadcastReceiver.messageReceived.......ignoreNotication.........." + ignoreNotication);
        String receivedMesssage = null;
        String config = null;
        String message = null;

        try {
            final Bundle extras = intent.getExtras();
            //VarGlobal.request_user_ID = extras.getString("call_user_id");
            receivedMesssage = extras.getString("message");
            //response = extras.getString("response");

            if(receivedMesssage != null) {
                //Log.v(TAG, "messageReceived: Message: " + receivedMesssage);
                //receivedMesssage.replaceAll("\\s+", "");
                String[] parts = receivedMesssage.split("::--::");
                message = parts[0].trim();
                VarGlobal.request_user_ID = parts[1].trim();
                Log.v(TAG, "messageReceived: Message: " + receivedMesssage + " --sender ID: " + VarGlobal.request_user_ID + " --message " + message);

                if(message.toLowerCase().contains("scan request")){
                    if(VarGlobal.isCameraInUse == 0) {
                        VarGlobal.isCameraInUse =1;

                        Intent myIntent = new Intent(context.getApplicationContext(), StartScannerActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myIntent);
                    }else
                        Log.v(TAG, "Camera already in use");

                }

                if(message.toLowerCase().contains("photo request")){
                    if(VarGlobal.isCameraInUse == 0) {
                        VarGlobal.isCameraInUse =1;

                        Intent myIntent = new Intent(context.getApplicationContext(), CameraActivity.class);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myIntent);
                    }else
                        Log.v(TAG, "Camera already in use");

                }

                if(message.toLowerCase().contains("group shop")){

                    String webLink = VarGlobal.request_user_ID;

                    Log.v(TAG, "Open web page: " + webLink);

                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(webLink));
                    webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(webIntent);

                    Log.v(TAG, "Web page opened: ");

                }

            }

         //   boolean appOnForeground = isAppOnForeground(context);

        //    System.err.println("KiiPushBroadcastReceiver.messageReceived.....is appOnForeground..." + appOnForeground);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGroupNotification(String groupID, String topicName, String message) {
        Log.v(TAG, "enter sendNotificaiton()");
        KiiGroup group = KiiGroup.groupWithID(groupID);
        try {
            group.refresh();
        } catch (GroupOperationException e) {
            // Refreshing group failed.
            Log.e(TAG, "Group refreshing failed: " + e.getMessage());
        }
        KiiTopic topic = group.topic(topicName);

        //Build a push message
        KiiPushMessage.Data data = new KiiPushMessage.Data();
        data.put("message", message);
        KiiPushMessage sendMsg = KiiPushMessage.buildWith(data).build();

        try {
            topic.sendMessage(sendMsg);
            Log.v(TAG, "messge send to group " + Constants.GROUPNAME + " with topic " + Constants.TOPICNAME );

        }  catch (IOException ioe) {
            Log.e(TAG, "could not send message: " + ioe.getMessage());
            // failed.
        } catch (AppException e) {
            Log.e(TAG, "could not send message: " + e.getMessage());
            // failed.
        }

    }


    private boolean isSigned(Context context) {
        try {
            final String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString();
            final SharedPreferences pref = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
            String content = pref.getString(Constants.ACCESS_TOKEN, null);
            if (content != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isAppOnForeground(Context context) {
        final ForeGround foreGround = ForeGround.get();
        return foreGround.isForeground();
    }
}
