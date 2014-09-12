package org.rescueme;

/**
 * Created by Vedavyas Singareddi on 02-09-2014.
 */
public class RescueMeConstants {

    public static final String PREFERENCE_NAME = "RescueMe";
    public static final String LOGIN = "Log In";
    public static final String REGISTER = "Registration";
    public static final String NEW_EMERGENCY_CONTACT = "new emergency contact";
    public static final String UPDATE_EMERGENCY_CONTACT = "update emergency contact";
    public static final String UPDATE_EMERGENCY_CONTACT_SUCCESS = "Update Successful";
    public static final String UPDATE_EMERGENCY_CONTACT_FAIL = "Update Failed.. Check Email and PhoneNumber";
    public static final String ADDED_NEW_CONTACT = "Contact successfully added";
    public static final String FAILED_TO_ADD_CONTACT = "Failed to add the Contact.. Check Email and PhoneNumber";
    public static final String[] TABS = {"Rescue Me", "Profile", "Contacts", "Settings"};
    public static final int NO_OF_TABS = TABS.length;
    public static final String RESCUE_ME_MAIN = "Rescue Me";
    public static final String RESCUE_ME = "Rescue Me";
    public static final String LOGOUT_SUCCESS = "Logout Successful";
    public static final int SPLASH_SCREEN_TIMEOUT = 2000;
    public static final String EMAIL_REGEX = "@";
    public static final String FRAGMENT_TAG = "Fragment_tag";
    public static final String SELECT_TAG = "tag to load";

    //Login constants
    public static final String LOGIN_SUCCESS = "Login Successful";

    //Registration constants
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String EMAIL_FAIL = "Email Doesn't meet the constraints";
    public static final String PASSWORD_FAIL = "Password is too short (Min 5 characters)";
    public static final String PHONE_FAIL = "Not a 10 digit number or contains characters";
    public static final String NAME_EMPTY = "Name cannot be blank";
    public static final int PASSWORD_MIN_LENGTH = 5;
    public static final int PHONE_NUMBER_LENGTH = 10;

    //Database related constants
    public static final String DB_NAME = "rescueme.db";
    public static final String USER_TABLE = "user_details";
    public static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( ID INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "Name TEXT,"
            + "Password TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE)";
    public static final String CONTACTS_TABLE = "contacts_details";
    public static final String SQL_CONTACT_TABLE_QUERY = "CREATE TABLE " + CONTACTS_TABLE
                                                    +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                    +"Name TEXT,"
            + "Email TEXT UNIQUE,"
            + "Number TEXT UNIQUE)";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_EMAIL = "Email";
    public static final String SQL_LOGIN_QUERY = "SELECT "+COLUMN_PASSWORD+" FROM "
                                                    +USER_TABLE+" WHERE "
                                                    +COLUMN_EMAIL+" = ";
    public static final String COLUMN_NUMBER = "Number";
    public static final String SQL_SELECT_ALL_QUERY = "SELECT * FROM ";
    public static final String LOGIN_FAIL = "Login failed!!";

    //Facebook related constants
    public static final String FB_APP_ID = "710855998982153";
    public static final String FB_APP_NAME_SPACE = "test_login_ved";
    public static final String FB_LOGIN_SUCCESS = "Connected to Facebook";
    public static final String FB_NOT_ACCEPT_PERMISSIONS = "You did not Accept Permissions";
    public static final String FB_EXCEPTION_LOGIN = "Exception occurred during LogIn";
}
