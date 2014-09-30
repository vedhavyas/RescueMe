package org.rescueme;

/**
 * Authored by Vedhavyas Singareddi on 02-09-2014.
 */
public class RescueMeConstants {

    public static final String PREFERENCE_NAME = "RescueMe";
    public static final String[] TABS = {"Rescue Me", "Profile", "Contacts", "Settings"};
    public static final int NO_OF_TABS = TABS.length;
    public static final String RESCUE_ME_MAIN = "Rescue Me";
    public static final String RESCUE_ME = "Rescue Me";
    public static final String LOGOUT_SUCCESS = "Logout Successful";
    public static final int SPLASH_SCREEN_TIMEOUT = 1500;
    public static final String EMAIL_REGEX = "@";
    public static final String FRAGMENT_TAG = "Fragment_tag";
    public static final String SELECT_TAG = "tag to load";
    public static final String EXCEPTION_CAUGHT = "Caught the exception";
    public static final String FAILED_TO_GET_USER_PROFILE = "Failed to get the profile";
    public static final String UPDATING_USER_PROFILE = "Updating Profile Info";
    public static final String UPDATE_PROFILE_FAIL = "Profile Update Failed";
    public static final String UPDATE_PROFILE_SUCCESS = "Updated Profile Info";
    public static final String LOG_TAG = "RescueME";

    //Login constants
    public static final String LOGIN = "Log In";
    public static final String LOGGED_IN_USER_ID = "Logged in user ID";
    public static final String UPDATE_PROFILE = "Update Profile";

    //Registration constants
    public static final String REGISTERING_USER = "Please wait while we Register you";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String REGISTRATION_FAILED = "Registration Failed";
    public static final String REGISTRATION_SUCCESS = "Registration Successful";
    public static final String EMAIL_FAIL = "Email Doesn't meet the constraints";
    public static final String PHONE_FAIL = "Not a 10 digit number or contains characters";
    public static final String NAME_EMPTY = "Name cannot be blank";
    public static final String MESSAGE_EMPTY = "Message cannot be blank";
    public static final int PHONE_NUMBER_LENGTH = 10;
    public static final String CHOOSE_THE_PROFILE_PIC = "Profile pic cannot be blank";
    public static final String FACEBOOK = "Facebook";

    //Database related constants
    public static final String DB_NAME = "rescueme.db";
    public static final String USER_TABLE = "user_details";
    public static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Name TEXT,"
            + "Password TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE, " +
            "ProfilePic BLOB," +
            "PersonalMessage TEXT DEFAULT 'Update to Add Custom Rescue Message')";
    public static final String CONTACTS_TABLE = "contacts_details";
    public static final String SQL_CONTACT_TABLE_QUERY = "CREATE TABLE " + CONTACTS_TABLE
            + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Name TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE)";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PROFILE_PIC = "ProfilePic";
    public static final String COLUMN_PERSONAL_MESSAGE = "PersonalMessage";
    public static final String COLUMN_NUMBER = "Number";
    public static final String SQL_SELECT_ALL_QUERY = "SELECT * FROM ";

    //Facebook related constants
    public static final String FB_APP_ID = "822228331163024";
    public static final String FB_APP_NAME_SPACE = "test_login_ved_";
    public static final String FB_LOGIN_SUCCESS = "Connected to Facebook";
    public static final String FB_NOT_ACCEPT_PERMISSIONS = "You did not Accept Permissions";
    public static final String FB_EXCEPTION_LOGIN = "Exception occurred during LogIn";
    public static final int FB_PROFILE_PIC_HEIGHT = 500;
    public static final int FB_PROFILE_PIC_WIDTH = 800;
    public static final String DOWNLOADING_PROFILE = "Downloading your Profile";
    public static final String GOT_PROFILE_DATA = "Downloaded your Profile";
    public static final String DOWNLOADING_PROFILE_PIC = "Downloading your Profile Picture";
    public static final String FB_FIRST_TIME_LOGIN = "First time login";

    //Emergency contacts related constants
    public static final String NEW_EMERGENCY_CONTACT = "Add Contact";
    public static final String UPDATE_EMERGENCY_CONTACT = "Update Contact";
    public static final String UPDATE_EMERGENCY_CONTACT_SUCCESS = "Contact Updated Successfully";
    public static final String UPDATE_EMERGENCY_CONTACT_FAIL = "Update Failed.. Check Email and PhoneNumber";
    public static final String ADDED_NEW_CONTACT = "Contact successfully added";
    public static final String FAILED_TO_ADD_CONTACT = "Failed to add the Contact..";
    public static final String DELETE_EMERGENCY_CONTACT_SUCCESS = "Contact Successfully deleted";
    public static final String DELETE_EMERGENCY_CONTACT_FAIL = "Failed to Delete the contact";
    public static final int PICK_CONTACT_FROM_LOCAL = 3;
    public static final String PICK_NUMBER = "Pick a Number";
    public static final String PICK_EMAIL = "Pick an Email";

    //Panic button related
    public static final String DIALOG_TITLE = "Emergency";
    public static final String PANIC_CNF = "Confirm";
    public static final String PANIC_FALSE_ALARM = "False Alarm";
    public static final String DIALOG_MESSAGE = "Do you think you are in Danger ?";
    public static final String SMS_SENT_SUCCESS = "SMS Sent to All of the contacts";
    public static final String GET_GPS_LOCATION_AGAIN = "Getting Coordinates again";
    public static final String SMS_FAILED = "SMS failed. You didn't configure Emergency contacts";


    //GPS Location related Constants
    public static final long MIN_DISTANCE_FOR_UPDATE = 10; //meters
    public static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2; //2 minutes
    public static final String ADD_EXTRA_MESSAGE = "I'm at the location : ";
    public static final String LATITUDE = "Latitude : ";
    public static final String LONGITUDE = "Longitude : ";
    public static final String SETTINGS = " Settings";
    public static final String PROVIDER_NOT_ENABLED = " is not enabled! Want to go to settings menu?";
    public static final String CANCEL = "Cancel";
    public static final int GPS_LOCATION_SETTINGS = 1;
    public static final int GPS_AFTER_SETTINGS_SLEEP = 6000;


    //request codes
    public static final int SELECT_PICTURE = 1;
    public static final int CROP_IMAGE = 2;
    public static final int GOOGLE_PLUS_LOGIN_RESOLUTION = 3;
    public static final String GOOGLE_CONNECTION_SUCCESS = "Connected to Google";
    public static final String GOOGLE_PLUS = "G Plus";
    public static final String G_PLUS_FIRST_TIME_LOGIN = "gPlus first time login";
}
