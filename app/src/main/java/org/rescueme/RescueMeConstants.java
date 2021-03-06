package org.rescueme;

/**
 * Authored by Vedhavyas Singareddi on 02-09-2014.
 */
public class RescueMeConstants {

    public static final String PREFERENCE_NAME = "RescueMe";
    public static final String[] TABS = {"Rescue Me", "Contacts", "Settings"};
    public static final int NO_OF_TABS = TABS.length;
    public static final String RESCUE_ME_MAIN = "Rescue Me";
    public static final int SPLASH_SCREEN_TIMEOUT = 1000;
    public static final String EMAIL_REGEX = "@";
    public static final String FRAGMENT_TAG = "Fragment_tag";
    public static final String SELECT_TAG = "tag to load";
    public static final String UPDATE_PROFILE_FAIL = "Profile Update Failed";
    public static final String LOG_TAG = "RescueMe";
    public static final String EDIT_MODE = "edit mode";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";

    //Login constants
    public static final String LOGIN = "Log In";
    public static final String LOGGED_IN_USER_ID = "Logged in user ID";
    public static final String UPDATE_PROFILE = "Update Profile";

    //Registration constants
    public static final String REGISTRATION_FAILED = "Registration Failed";
    public static final String EMAIL_FAIL = "Email Doesn't meet the constraints";
    public static final String PHONE_FAIL = "Not a 10 digit number or contains characters";
    public static final String NAME_EMPTY = "Name cannot be blank";
    public static final int PHONE_NUMBER_LENGTH = 10;
    public static final String FACEBOOK = "Facebook";

    //Database related constants
    public static final String DB_NAME = "rescueme.db";
    public static final String USER_TABLE = "user_details";
    public static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Name TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE, " +
            "ProfilePic BLOB)";
    public static final String CONTACTS_TABLE = "contacts_details";
    public static final String SQL_CONTACT_TABLE_QUERY = "CREATE TABLE " + CONTACTS_TABLE
            + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Name TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE," +
            "ProfilePic BLOB )";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PROFILE_PIC = "ProfilePic";
    public static final String COLUMN_NUMBER = "Number";
    public static final String SQL_SELECT_ALL_QUERY = "SELECT * FROM ";

    //Facebook related constants
    public static final String FB_APP_ID = "822228331163024";
    public static final String FB_APP_NAME_SPACE = "test_login_ved_";
    public static final String FB_LOGIN_SUCCESS = "Connected to Facebook";
    public static final int FB_PROFILE_PIC_HEIGHT = 500;
    public static final int FB_PROFILE_PIC_WIDTH = 800;
    public static final String FB_FIRST_TIME_LOGIN = "First time login";

    //Emergency contacts related constants
    public static final String NEW_EMERGENCY_CONTACT = "Add Contact";
    public static final String UPDATE_EMERGENCY_CONTACT = "Update Contact";
    public static final String UPDATE_EMERGENCY_CONTACT_FAIL = "Update Failed.. Check Email and PhoneNumber";
    public static final String DELETE_EMERGENCY_CONTACT_FAIL = "Failed to Delete the contact";
    public static final String PICK_NUMBER = "Pick a Number";
    public static final String PICK_EMAIL = "Pick an Email";

    //Panic button related
    public static final String DIALOG_TITLE = "Emergency";
    public static final String PANIC_CNF = "Confirm";
    public static final String PANIC_FALSE_ALARM = "False Alarm";
    public static final String DIALOG_MESSAGE = "Do you think you are in Danger ?";
    public static final String JSON_STRING = "json_string";


    //GPS Location related Constants
    public static final long MIN_DISTANCE_FOR_UPDATE = 100; //meters
    public static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 20; //2 minutes
    public static final String ADD_EXTRA_MESSAGE = "I'm at the location : ";
    public static final String LATITUDE = "Latitude : ";
    public static final String LONGITUDE = "Longitude : ";
    public static final String SETTINGS = " Settings";
    public static final String PROVIDER_NOT_ENABLED = " is not enabled! Want to go to settings menu?";
    public static final String CANCEL = "Cancel";
    public static final int GPS_AFTER_SETTINGS_SLEEP = 6 * 1000;


    //request codes
    public static final int SELECT_PICTURE = 1;
    public static final int CROP_IMAGE = 2;
    public static final int PICK_CONTACT_FROM_LOCAL = 3;
    public static final int GOOGLE_PLUS_LOGIN_RESOLUTION = 4;
    public static final int GPS_LOCATION_SETTINGS = 5;
    public static final String GOOGLE_CONNECTION_SUCCESS = "Connected to Google";
    public static final String GOOGLE_PLUS = "G Plus";
    public static final String G_PLUS_FIRST_TIME_LOGIN = "gPlus first time login";

    //settings
    public static final String CUSTOM_MESSAGE = "Custom Message";
    public static final String DEFAULT_CUSTOM_MESSAGE = "This is an Emergency!!";
    public static final String SEND_SMS = "Send SMS";
    public static final String SEND_EMAIL = "Send Email";
    public static final String SEND_PUSH = "Send Push";
    public static final String PUSH_LOCATION_UPDATES = "get location updates";
    public static final String RESCUE_DISTANCE = "Rescue Me Distance";
    public static final String IS_RESCUER = "Is Rescuer";
    public static final String RECEIVE_SMS = "Receive SMS";
    public static final String RECEIVE_EMAIL = "Receive Email";
    public static final String RECEIVE_PUSH = "Receive Push";
    public static final String RESCUER_DISTANCE = "Rescuer Alert Distance";

    //popup activity
    public static final String POP_UP_ACTIVITY_TAG = "pop up activity tag";
    public static final String SETTINGS_DIALOG = "settings dialog";
    public static final String EMERGENCY_POP_UP = "emergency_pop_up";

    //Notification
    public static final int NOTIFICATION_ID = 1234;

    //receiver constants

    public static final String LOCATION_CHANGED = "org.rescueme.LOCATION_CHANGED";
    public static final String SHOW_SETTINGS_DIALOG = "org.rescueme.SHOW_SETTINGS_DIALOG";
}
