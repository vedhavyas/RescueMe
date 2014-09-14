package org.rescueme;

import android.app.Application;

import com.facebook.SessionDefaultAudience;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.utils.Logger;

/**
 * Authored by Vedhavyas Singareddi on 11-09-2014.
 */
public class RescueMeAppExtension extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // set log to true
        Logger.DEBUG_WITH_STACKTRACE = true;

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
}
