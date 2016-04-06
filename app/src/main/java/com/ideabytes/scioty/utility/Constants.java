package com.ideabytes.scioty.utility;

/**
 * Created by ideabytes on 2/26/16.
 */
public class Constants {
    public static final String APP_ID = "22f4d3df";
    public static final String APP_KEY = "2322c4cad505799743114f0c4ceea368";

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String MY_PREFERENCE = "MyPrfs";

    public static final String DISABLED = "disabled";
    public static final String LOG_STATUS = "logstatus";

    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_FAIL = "fail";

    public static final String LOG_REPORT_FOLDER = "/sdcard/Android/data/SmartScanner/LogReport.txt";

    public static final String APP_BUCKET = "devicesList";

    public static final String NOTIFICATION_SIGN_OUT = "notifisignout";
    public static final String FIRST_TIME_LOGIN = "first time login";

    public static final String RESETED_PASSWORD = "reseted_password";
    public static final String EMPTY = "";
    public static final String CHANGED_PASSWORD_AFTER_RESET = "changed password after resetEventReceived";

    public static final String EVENTSCOUNT = "eventsCount";

    public static final String LOAD_CAMERA_LIST = "LOAD_CAMERA_LIST";
    public static final String ERROR_CODE = "errorCode";
    public static final String NO_RESPONSE = "NO_RESPONSE";
    public static final String RESPONSE = "response";
    public static final String UNAUTHORIZED_USER = "UNAUTHORIZED_USER";
    public static final String ALREADY_RETRY = "already retry";
    public static final String LOAD_DEVICE_LIST = "LOAD_DEVICE_LIST";

    public static final String DEVICES_LIST = "devicesList";
    public static final String CONFIG_DEV_NAME = "deviceName";
    public static final String CONFIG_DEV_ID = "ID";
    public static final String CONFIG_DEV_STATUS = "deviceStatus";
    public static final String CONFIG_USER_PERMISSION = "fullPermission";

    public static final String GCM_SENDER_ID = "683523168820";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String USER_ID = "User_id";
    public static final String USER_PASSWORD = "User_pwd";

    public static final String GROUPID = "scanner-group";
    public static final String GROUPNAME = "smartScanner";
    public static final String TOPICNAME = "groupChat";

    //Scan constants
    public static final String SCAN_MODES = "SCAN_MODES";
    public static final String SCAN_RESULT = "SCAN_RESULT";
    public static final String SCAN_RESULT_TYPE = "SCAN_RESULT_TYPE";
    public static final String ERROR_INFO = "ERROR_INFO";
    public static final int TIME_OUT = 10*1000;

    public static final String SERVER_URL_TO_POST_STATUS = "http://104.238.67.134/IOT/api/web/iot/insertresponse.json";
    public static final String SERVER_URL_TO_POST_REGISTER = "http://104.238.67.134/IOT/api/web/iot/userregistration/22f4d3df/2.json";

    public static final String SERVER_URL_TO_POST_SIGNIN = "http://104.238.67.134/IOT/api/web/iot/updateTokenInformation/22f4d3df.json";

}
