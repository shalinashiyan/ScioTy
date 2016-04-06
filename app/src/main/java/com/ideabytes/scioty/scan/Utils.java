package com.ideabytes.scioty.scan;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ideabytes.scioty.R;
/**
 * Created by suman on 23/2/16.
 */
public class Utils {
    private final String LOGTAG = "Utils";
    private Activity activity;
    public Utils() {

    }
    public Utils(Activity activity) {
        this.activity =activity;
    }
    /**
     * Shows a toast message
     *
     * @author suman
     * @since v.b.5.4.2
     */
    public void showToastMessage(String message) {
        try {
            // Create layout inflater object to inflate toast.xml file
            LayoutInflater inflater = activity.getLayoutInflater();

            // Call toast.xml file for toast layout
            View toastRoot = inflater.inflate(R.layout.custom_toast, null);
            TextView tvToast = (TextView) toastRoot.findViewById(R.id.tvToast);
            tvToast.setText(message);

            Toast toast = new Toast(activity);

            // Set layout to toast
            toast.setView(toastRoot);
            toast.setGravity(Gravity.CENTER_HORIZONTAL
                    | Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            Log.e(Utils.this.getClass().getSimpleName(),
                    "Error in custom toast message display " + e.toString());
        }
    }
}

