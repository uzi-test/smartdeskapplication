package com.smartdesk.constants;

import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.smartdesk.model.signup.SignupUserDTO;

public class Constants {

    //    public static final String topicPrefix = "/topics/";
    public static final String api_key = "AIzaSyAJwu52r7NzrZFYKsLW7zT8lVdvfWe1AF8";
    public static final String smartDeskLogo = "https://thumbs.dreamstime.com/z/gold-smart-table-lamp-system-icon-isolated-black-background-internet-things-concept-wireless-connection-vector-187995951.jpg";

    //SmartDesk email
    public static String emailID = "smartdesk0010@gmail.com";
    public static String password = "smartdesk123";

    public static String SmartDesk = "Smart-Desk";

    //Roles
    public static Integer adminRole = 1;
    public static Integer managerRole = 2;
    public static Integer deskUserRole = 3;

    //User Details Constants
    public static Integer USER_ROLE;
    public static String USER_NAME;
    public static String USER_MOBILE;
    public static String USER_Password;
    public static String USER_PROFILE;
    public static String USER_DOCUMENT_ID;
    public static Boolean USER_MECHANIC_ONLINE;

    //Share Preference Constants
    public static final String SP_FILE_NAME = "info";
    public static final String SP_MOBILE = "Mobile";
    public static final String SP_PASSWORD = "Password";
    public static final String SP_DOCUMENT_ID = "Document";
    public static final String SP_ISLOGIN = "isLogin";

    //Dto Constants
    public static SignupUserDTO const_usersSignupDTO;

    //Notification
    public static int notificationCount = 0;

    //Spinner Constants
    public static String genderSelection = "Select Gender ⁕";
    public static String[] genderItems = {"Male", "Female"};

    public static String wirelessChargingSelection = "Wireless Charging Supported? ⁕";
    public static String builtinSpeakerSelection = "Built-in Speaker Supported? ⁕";
    public static String bluetoothSelection = "Bluetooth Connectivity Supported? ⁕";
    public static String groupUserSelection = "Single or Group User? ⁕";

    public static String[] yesNo = {"Yes", "NO"};
    public static String[] groupUserOptions = {"Single User", "Group User (3 max)", "Group User (6 max)", "Group User (8 max)"};


    //Location Constants
    public static Double const_lat = 51.509865;
    public static Double const_lng = -0.118092;
    public static EditText addressEditext;

    //Otp Timeout
    public static String countryCode = "+44";
    public static int timeoutOtp = 90; // 120 seconds means 2 minutes expiration
    public static int changeIntentDelay = 600;

    //User Status
    public static String activeStatus = "Active";
    public static String newAccountStatus = "Your account is in review process, we will notify you once done. Please wait for admin approval";
    public static String disableStatus = "Your Account has been disabled! ";
    public static String blockedStatus = "Your Account has been blocked";

    //camera zoom
    public static int cameraZoomInMap = 15;

    //Location Alert Dialog
    public static AlertDialog confirmationAlert;
}
