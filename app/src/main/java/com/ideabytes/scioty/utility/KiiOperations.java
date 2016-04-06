package com.ideabytes.scioty.utility;

/**
 * Created by ideabytes on 2/26/16.
 */
import android.content.Context;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiPushSubscription;
import com.kii.cloud.storage.KiiThing;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.GroupOperationException;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.util.Log;

import com.ideabytes.scioty.notifications.GCMPreference;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class KiiOperations {

    private final String TAG = "KiiOperations";
    /**
     * used to initialize kii cloud
     */
    public void initialize() {
        //UtilityMethods.createFileWithContent("Info: KiiOperations: initialize: start");
        Log.v(TAG, " KiiOperations: initialize kii: start");
        Kii.initialize(Constants.APP_ID, Constants.APP_KEY, Kii.Site.US);
        //UtilityMethods.createFileWithContent("Info: KiiOperations: initialize: end");
        Log.v(TAG, " KiiOperations: initialize kii: end");
    }


    /**
     * Used to register new user
     *
     * @param id
     * @param password
     */
    public String register(final String loginame, final String id, final String password) throws AppException, IOException {
        //UtilityMethods.createFileWithContent("Info: KiiOperations: register: start");
        Log.v(TAG, " KiiOperations: register: start");
        KiiUser.Builder builder = KiiUser.builderWithName(loginame);
        builder.withEmail(id);
        final KiiUser user = builder.build();

        try{
            user.register(password);
            final String accessToken = user.getAccessToken();
            //UtilityMethods.createFileWithContent("Info: KiiOperations: register: end");
            Log.v(TAG, " KiiOperations: register: end");
            return accessToken;
        } catch (AppException e) {
            // Sign-up failed for some reasons
            Log.e(TAG, "Cann not register: " + e.getMessage());
            return e.getMessage();

        } catch (IOException e) {
            // Sign-up failed for some reasons
            Log.e(TAG, "Cann not register: " + e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Used to login existing user in the kii cloud
     *
     * @param id
     * @param password
     */
    public KiiUser login(final String id, final String password) throws AppException, IllegalArgumentException, IOException {

        return KiiUser.logIn(id, password);
    }

    /**
     * Used to auto login existing user by using accessToken
     *
     * @param content
     */
    public KiiUser loginWithAccessToken(final String content) throws AppException, IllegalArgumentException, IOException {
        return KiiUser.loginWithToken(content);
    }

    public void createNewGroup(String name, String groupID){
        if(! isGroupExist(groupID)){
            List<KiiUser> members = new ArrayList<KiiUser>();
            members.add(KiiUser.userWithID(KiiUser.getCurrentUser().getID()));
            try {
                KiiGroup group = KiiGroup.registerGroupWithID(groupID, name, members);
                Uri groupUri = group.toUri();
                Log.v(TAG, "new group created.");
            } catch (GroupOperationException e) {
                // Group creation failed for some reasons.
                // Please check GroupOperationException to see what went wrong...
                Log.v(TAG, " Group creation failed: " + e.getMessage());
            }
        }
       else{
           Log.v(TAG, "Group already exist");
       }
    }

    private boolean isGroupExist(String groupID){
        boolean flag;
        KiiGroup group2 = KiiGroup.groupWithID(groupID);

        try {
            // Refresh the instance to make it up-to-date.
            group2.refresh();

            // Do something with the group reference.
            String groupName = group2.getGroupName();
            String id = group2.getID();
            KiiUser user = group2.getOwner();
            String userID = user.getID();
            Log.v(TAG, "Group exist: " + groupName + id + userID);
            flag = true;

        } catch (GroupOperationException e) {
            Log.e(TAG, "Group instantiation failed: " + e.getMessage());
            // Group instantiation failed for some reasons.
            // Please check GroupOperationException to see what went wrong...
            flag = false;
        }

        return flag;
    }

    public String joinGroupChat(String groupID, String email, String topicName){
        String result;
        try {
            KiiUser user = KiiUser.findUserByEmail(email);

            //add user into group
            KiiGroup group = KiiGroup.groupWithID(groupID);
            group.addUser(user);
            try {
                group.save();

                //subscribe user into group topic
                try {
                    KiiTopic topic = group.topic(topicName);
                    KiiPushSubscription sub = user.pushSubscription();
                    if (!sub.isSubscribed(topic))
                        sub.subscribe(topic);
                    else
                        Log.v(TAG, "user already subscribe to the topic: " + topicName);

                    result = Constants.RESULT_SUCCESS;
                } catch (IOException ioe) {
                    // Subscription failed.
                    Log.e(TAG, "subscribe user to a group topic failed: " + ioe.getMessage());
                    result = Constants.RESULT_FAIL;
                } catch (AppException e) {
                    // Subscription failed.
                    Log.e(TAG, "subscribe user to a group topic failed: " + e.getMessage());
                    result = Constants.RESULT_FAIL;
                }

            } catch (GroupOperationException e) {
                // Please check GroupOperationException to see what went wrong...
                Log.e(TAG, "Can not add user into group: " + e.getMessage());
                result = Constants.RESULT_FAIL;
            }

        } catch (IOException e) {
            // Please check IOException to see what went wrong...
            Log.e(TAG, "Can not find user: " + e.getMessage());
            result = Constants.RESULT_FAIL;
        } catch (AppException e) {
            // Please check AppException to see what went wrong...
            Log.e(TAG, "Can not find user: " + e.getMessage());
            result = Constants.RESULT_FAIL;
        }

        return result;
    }

    public void createGroupTopic(String groupID, String topicName){
        KiiGroup group = KiiGroup.groupWithID(groupID);
        try {
            // Refresh the instance to make it up-to-date.
            group.refresh();

            // Do something with the group reference.
            String groupName = group.getGroupName();
            String id = group.getID();
            KiiUser user = group.getOwner();
            String userID = user.getID();
            Log.v(TAG, "Group exist2: " + groupName + id + userID);

            try {
                // Create an instance of group-scope topic.
                // (assume that the current user is a member of the group)
                KiiTopic topic = group.topic(topicName);

                if(!topic.exists()) {
                    // Save the topic to Kii Cloud
                    topic.save();
                    Log.v(TAG, "group topic created");
                }
                else
                    Log.v(TAG, "Topic " + topicName + " already exists in group " + groupID);
            } catch (IOException ioe) {
                // failed.
                Log.e(TAG, "createGroupTopic fail: " + ioe.getMessage());
            } catch (AppException e) {
                // failed
                Log.e(TAG, "createGroupTopic fail: " + e.getMessage());
            }

        } catch (GroupOperationException e) {
            Log.e(TAG, "Group instantiation failed: " + e.getMessage());
            // Group instantiation failed for some reasons.
            // Please check GroupOperationException to see what went wrong...
        }

    }

    public void subscribeGroupTopic(String groupID, String topicName){
        KiiGroup group = KiiGroup.groupWithID(groupID);
        try {
            // Refresh the instance to make it up-to-date.
            group.refresh();

            try {
                // Instantiate the group-scope topic.
                KiiTopic topic = group.topic(topicName);

                // Subscribe the current user to the topic.
                // (The current user must be a group member)
                KiiUser user = KiiUser.getCurrentUser();
                KiiPushSubscription sub = user.pushSubscription();

                if(! sub.isSubscribed(topic))
                    sub.subscribe(topic);
                else
                    Log.v(TAG, "current user already subscribe to the topic: " + topicName);

                Log.v(TAG, "subscribe current user to a group topic end: " + topicName);

            } catch (IOException ioe) {
                // Subscription failed.
                Log.e(TAG, "subscribe user to a group topic failed: " + ioe.getMessage());

            } catch (AppException e) {
                // Subscription failed.
                Log.e(TAG, "subscribe user to a group topic failed: " + e.getMessage());

            }
        } catch (GroupOperationException e) {
            Log.e(TAG, "Group instantiation failed: " + e.getMessage());
            // Group instantiation failed for some reasons.
            // Please check GroupOperationException to see what went wrong...
        }
    }

    public boolean addObject (String device_id, String deviceName, String ip){
        boolean isSuccess = false;
        String obj_id = "id_" + device_id;
        // Check whether the id is valid.
        if (KiiObject.isValidObjectID(obj_id)) {
            //Create an object in an application-scope bucket.
            KiiObject object = Kii.bucket(Constants.APP_BUCKET).object(obj_id);
            object.set("deviceID", device_id);
            object.set("deviceName", deviceName);
            object.set("Available", 1);
            object.set("SSID", ip);
            Log.v(TAG, "addObject: obj_id=" + obj_id + " deviceID=" + device_id + " SSID=" + ip);
            // Save the object
            try {
                object.saveAllFields(true);
                isSuccess = true;
            } catch (IOException e) {
                // Handle error
                Log.e(TAG, "Can not add object: " + e.getMessage());
            } catch (AppException e) {
                // Handle error
                Log.e(TAG, "Can not add object: " + e.getMessage());
            }
        }
        else
            Log.v(TAG, "Invalid object id");

        return isSuccess;

    }

    public boolean updateObjectStatus(String obj_id, int available, String ip){
        boolean isSuccess = false;
        KiiObject object = Kii.bucket(Constants.APP_BUCKET).object(obj_id);

        try{
            object.set("Available", available);
            object.set("SSID", ip);
            object.save();
            isSuccess = true;
        }catch (IOException e) {
            // Handle error
            Log.e(TAG, "Can not add object: " + e.getMessage());
        } catch (AppException e) {
            // Handle error
            Log.e(TAG, "Can not add object: " + e.getMessage());
        }

        return isSuccess;
    }

    /**
     * used to get all groups belonging to this user
     *
     * @param kiiUser
     * @return
     * @throws Exception
     */
    public List<KiiGroup> readAllGroups(final KiiUser kiiUser) throws Exception {
        return kiiUser.memberOfGroups();
    }

    /**
     * used to read all object of this bucket from kii cloud
     *
     * @param thingId
     * @param bucketName
     * @return
     * @throws Exception
     */
    public List<KiiObject> readBucketObjects(final String thingId, final String bucketName) throws Exception {
        KiiQuery all_query = new KiiQuery();
        all_query.setLimit(100);
        KiiQueryResult<KiiObject> result1 = KiiThing
                .loadWithVendorThingID(thingId)
                .bucket(bucketName)
                .query(all_query);
        List<KiiObject> allObjects = result1.getResult();
        // System.err.println("KiiOperations.readBucketObjects thing id before call ---------------------- " + thingId + "  bucketName " + bucketName + "  result size " + allObjects.size());
        while (result1.hasNext()) {
            result1 = result1.getNextQueryResult();
            allObjects.addAll(result1.getResult());
        }
        // List<KiiObject> result2 = result1.getResult();
        //System.err.println("KiiOperations.readBucketObjects thing id ---------------------- " + thingId + "  bucketName " + bucketName + "  result size " + result2.size());
        return allObjects;
    }

    /**
     * used to update the object of config bucket
     *
     * @param Uid
     * @param bucketName
     * @param parameter
     * @param value
     * @throws Exception
     */
    public void updateConfigBucketObject(final String Uid, final String bucketName, final String parameter, final String value) throws Exception {
        long start = System.currentTimeMillis();
        System.err.println("Webservice : start update config parameter : " + parameter + "    " + start);
        final KiiQueryResult<KiiObject> result1 = KiiThing
                .loadWithVendorThingID(Uid)
                .bucket(bucketName)
                .query(new KiiQuery());
        final List<KiiObject> objLists1 = result1.getResult();
        for (KiiObject obj : objLists1) {
            obj.set(parameter, value);
            obj.saveAllFields(true);
        }

        long end = System.currentTimeMillis();
        System.err.println("Webservice : end update config " + end);
        System.err.println("Webservice : total update config " + (end - start));
    }

    public String getNewAccessToken(final String emailId, final String password) {
        UtilityMethods.createFileWithContent("Info: KiiOperations: getNewAccessToken: start ");
        try {
            initialize();
            final KiiUser login = login(emailId, password);
            return login.getAccessToken();
        } catch (Exception e) {
            //UtilityMethods.createFileWithContent("Error: KiiOperations: getNewAccessToken: exception " + e.getMessage());
            Log.e(TAG, "Error: KiiOperations: getNewAccessToken: exception " + e.getMessage() );
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param vendorThingId
     * @return
     */
    public boolean deleteThing(String vendorThingId) {
        try {
            final KiiThing thing = KiiThing.loadWithVendorThingID(vendorThingId);
            thing.delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deleteGroup(String groupId) {
        try {
            final KiiGroup group = KiiGroup.groupWithID(groupId);
            group.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String resetPassword(String email) {
        //UtilityMethods.createFileWithContent("Error: KiiOperations: resetPassword: start");
        Log.v(TAG, "KiiOperations: resetPassword: start");
        try {
            KiiUser.resetPassword(email, KiiUser.NotificationMethod.EMAIL);
            //UtilityMethods.createFileWithContent("Error: KiiOperations: resetPassword: success");
            Log.v(TAG, "KiiOperations: resetPassword: success");
            return Constants.RESULT_SUCCESS;
        } catch (Exception e) {
           // UtilityMethods.createFileWithContent("Error: KiiOperations: resetPassword: message: " + e.getMessage());
            Log.e(TAG, "KiiOperations: resetPassword: message: " + e.getMessage());
            e.printStackTrace();
        }
        return Constants.RESULT_FAIL;
    }

    public boolean changePassword(String emailId, String prevsPassword, String newPassword) {
        //UtilityMethods.createFileWithContent("Info: KiiOperations: changePassword: start ");
        Log.v(TAG, "KiiOperations: changePassword: start");
        try {
            final KiiUser user = KiiUser.logIn(emailId, prevsPassword);
            user.changePassword(newPassword, prevsPassword);
            //UtilityMethods.createFileWithContent("Info: KiiOperations: changePassword: success ");
            Log.v(TAG, "KiiOperations: changePassword: success");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // Password change failed for some reasons

            // Please check IOExecption to see what went wrong...
           // UtilityMethods.createFileWithContent("Error: KiiOperations: changePassword: message: " + e.getMessage());
            Log.e(TAG, "KiiOperations: changePassword: message: " + e.getMessage());
        }
        //UtilityMethods.createFileWithContent("Info: KiiOperations: changePassword: end ");
        Log.v(TAG, "KiiOperations: changePassword: end");
        return false;
    }

    /**
     * @param context
     * @param gcm
     * @param userId
     * @param password
     * @throws Exception
     */
    public void registerPushNotification(Context context, GoogleCloudMessaging gcm, String userId, String password) throws Exception {

        // call register
        final String regId = gcm.register(Constants.GCM_SENDER_ID);

        // login
        KiiUser.logIn(userId, password);

        try {
            // install user device
            KiiUser.pushInstallation().install(regId);
        } catch (Exception e){
            Log.d(TAG, "Failed to complete pushInstallation", e);
        }

        // if all succeeded, save registration ID to preference.
        GCMPreference.setRegistrationId(context, regId);

    }

    public void unregisterPushNotification(Context context, GoogleCloudMessaging gcm, String userId, String password) throws Exception {

        // call register
        final String regId = gcm.register(Constants.GCM_SENDER_ID);

        // login
        KiiUser.logIn(userId, password);

        try {
            // uninstall user device
            KiiUser.pushInstallation().uninstall(regId);
        } catch (Exception e){
            Log.d(TAG, "Failed to complete pushInstallation", e);
        }



    }

}
