package org.rescueme;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import com.facebook.SessionDefaultAudience;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

/**
 * Authored by Vedhavyas Singareddi on 11-09-2014.
 */
public class RescueMeAppExtension extends Application {

    private boolean loggedIn;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME, MODE_PRIVATE);
        loggedIn = prefs.getBoolean(RescueMeConstants.LOGIN, false);

        RescueMeUtilClass.writeToLog("Initializing Simple Facebook!!");

        // simple facebook initialization
        {
            Logger.DEBUG_WITH_STACKTRACE = false;

            // initialize facebook configuration
            Permission[] permissions = new Permission[]{
                    Permission.PUBLIC_PROFILE,
                    Permission.USER_PHOTOS,
                    Permission.EMAIL
            };

            SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                    .setAppId(RescueMeConstants.FB_APP_ID)
                    .setNamespace(RescueMeConstants.FB_APP_NAME_SPACE)
                    .setPermissions(permissions)
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                    .setAskForAllPermissionsAtOnce(false)
                    .build();

            SimpleFacebook.setConfiguration(configuration);
        }

        //parse initialization
        {
            RescueMeUtilClass.writeToLog("Initializing parse!!");
            Parse.initialize(this, "p1S63CxoGmQVRTNp5OGE3nS976oe0W12HJNjEvOo", "xq7HB4QnIMWqYgJDLbYOWTJRImYL3SGMWDw4Qdpu");
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

        //Start location service if enabled
        {
            if (prefs.getBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, false)) {
                RescueMeUtilClass.writeToLog("Starting Location service");
                Intent intent = new Intent(getBaseContext(), RescueMeLocationService.class);
                startService(intent);
            }
        }

    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean result) {
        prefs.edit().putBoolean(RescueMeConstants.LOGIN, result).apply();
        this.loggedIn = result;
    }

}
