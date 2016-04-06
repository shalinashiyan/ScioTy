package com.ideabytes.scioty.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.utility.KiiOperations;

/**
 * Created by ideabytes on 3/2/16.
 */


public class UserHandler {
    private final String TAG = "UserHandler";
    /**
     * used to register user in cloud
     *
     * @param loginname
     * @param id
     * @param password
     */
    public void registerUser(final String loginname, final String id, final String password) {
        try {
            final KiiOperations kiiOperations = new KiiOperations();
            kiiOperations.register(loginname, id, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * used to insert user into database
     *
     * @param user
     */
    public void insertUser(final Context context, final User user) {
        Log.v(TAG, "Info: UserHandler: insertUser: start");
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(Constants.USER_ID, user.getId());
        edit.putString(Constants.USER_PASSWORD, user.getPassword());
        edit.commit();
        Log.v(TAG, "Info: UserHandler: insertUser: end");
    }

    /**
     * used to delete the user from the Database
     *
     * @param context
     */
//    public void deleteUser(final Context context) {
//        final DatabaseHelper helper = DatabaseHelper.getInstanace(context);
//        helper.deleteAllUsers();
//    }

    /**
     * used to get the user from the database
     *
     * @param context
     * @return
     */
    public User populateUser(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MY_PREFERENCE, Context.MODE_PRIVATE);
        final String userID = sharedPreferences.getString(Constants.USER_ID, "");
        final String userPassword = sharedPreferences.getString(Constants.USER_PASSWORD, "");
        return new User(userID, userPassword);
    }
}
