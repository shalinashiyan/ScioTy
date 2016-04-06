package com.ideabytes.scioty.notifications;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ideabytes on 3/2/16.
 */
public class GCMPreference {
    private static final String PROPERTY_REG_ID = "GCMregId";

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, null);
        return registrationId;
    }

    public static void setRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }

    public static void clearRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(PROPERTY_REG_ID);
        editor.commit();
    }

    private static SharedPreferences getGCMPreferences(Context context) {
        return context.getSharedPreferences(UpgradeToProServiceActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

}
