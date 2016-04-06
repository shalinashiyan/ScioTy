package com.ideabytes.scioty.scan;

/**
 * Created by ideabytes on 3/7/16.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.VarGlobal;

import java.io.BufferedReader;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class AsyncTaskSendData extends AsyncTask<String,Void,String> {
    private static final String TAG = "AsyncTaskSendData";
    private Context context;
    private HttpClient httpclient;
    private HttpPost httppost;

    public AsyncTaskSendData(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(String... params) {

        BufferedReader reader = null;
        Log.v(TAG, "Send scan result back to web browser: start");

        String msg = params[0];
        String responseStr = " ";

        //String deviceID = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);\
        //String userID = "576c66a00022-e9c8-5e11-abbd-05d8ace3";

        if (msg.length() > 0){
            httpclient = new DefaultHttpClient();
            httppost = new HttpPost(Constants.SERVER_URL_TO_POST_STATUS);
            httppost.setHeader("content-type", "application/json");
            httppost.setHeader("Accept", "application/json");

            JSONObject data = new JSONObject();
            try {

               try{
                   data.put("userID", VarGlobal.request_user_ID);
                   data.put("message", msg);
               } catch(JSONException e){
                   Log.e(TAG, "error in jason: " + e.getMessage());
                   throw new RuntimeException(e);
               }

                //StringEntity entity = new StringEntity(data.toString());
                httppost.setEntity(new StringEntity(data.toString(), "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                Log.v(TAG, "Send barcode: " + msg + " to user " + VarGlobal.request_user_ID);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {

                    responseStr = EntityUtils.toString(resEntity).trim();


                    Log.v(TAG, "Response: " +  responseStr);

                    // you can add an if statement here and do other actions based on the response
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                responseStr = "Exception";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                responseStr = "Exception";
            }

        } else {
            // display message if scan result is empty
            Toast.makeText(this.context,"no scan result",Toast.LENGTH_SHORT).show();
        }

        return responseStr;


    }


}
