package org.rescueme;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.parse.ParseBroadcastReceiver;

public class RescueMeBroadcastReceiver extends ParseBroadcastReceiver {


    private long[] vibrationPattern = {100, 100, 100, 100, 100, 100, 100, 400, 100, 400, 100, 400, 100, 100, 100, 100, 100, 100};

    public RescueMeBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            RescueMeUtilClass.writeToLog("Phone Rebooted!!");
            SharedPreferences prefs = context.getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                    , Context.MODE_PRIVATE);
            if (prefs.getBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, false)) {
                RescueMeUtilClass.writeToLog("Starting service!!");
                Intent locationServiceIntent = new Intent(context, RescueMeLocationService.class);
                context.startService(locationServiceIntent);
            }

        } else if (intent.getAction().equalsIgnoreCase(RescueMeConstants.LOCATION_CHANGED)) {

            RescueMeUtilClass.writeToLog("Location changed!!");
            setLocationChangedNotification(context);

        } else if (intent.getAction().equalsIgnoreCase(RescueMeConstants.SHOW_SETTINGS_DIALOG)) {

            RescueMeUtilClass.writeToLog("Show Settings Dialog!!");
            showGPSDisabledDialog(context);

        } else {
            RescueMeUtilClass.writeToLog("Push from Parse!!");
            String jsonData = intent.getExtras().getString("com.parse.Data");
            RescueMeUtilClass.writeToLog(jsonData);
            setParsePushNotification(context, jsonData);
        }
    }


    private void setParsePushNotification(Context context, String jsonData) {

        Intent intent = new Intent(context, RescueMeMapsActivity.class);
        intent.putExtra(RescueMeConstants.JSON_STRING, jsonData);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);

        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setTicker("This is a Test notification");
        notification.setContentText("This is a Test notification");
        notification.setContentTitle("Test Notification");
        notification.setContentIntent(pIntent);
        notification.setAutoCancel(true);
        notification.setVibrate(vibrationPattern);

        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify(RescueMeConstants.NOTIFICATION_ID, notification.build());

        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.isKeyguardLocked()) {
            RescueMeUtilClass.writeToLog("Device Locked!!");
            showPopUp(context, jsonData);
        }

    }


    private void showPopUp(Context context, String jsonData) {
        Intent popUpActivity = new Intent(context, RescueMePopUpActivity.class);
        popUpActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        popUpActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        popUpActivity.putExtra(RescueMeConstants.POP_UP_ACTIVITY_TAG, RescueMeConstants.EMERGENCY_POP_UP);
        popUpActivity.putExtra(RescueMeConstants.JSON_STRING, jsonData);
        context.startActivity(popUpActivity);
    }

    private void setLocationChangedNotification(Context context) {
        Intent intent = new Intent(context, RescueMe.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);

        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setTicker("Your Location is changed");
        notification.setContentText("Tap to launch RescueMe");
        notification.setContentTitle("Location Changed");
        notification.setContentIntent(pIntent);
        notification.setAutoCancel(true);
        notification.setVibrate(vibrationPattern);

        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationmanager.notify(RescueMeConstants.NOTIFICATION_ID, notification.build());
    }

    private void showGPSDisabledDialog(Context context) {
        Intent settingsDialogActivity = new Intent(context, RescueMePopUpActivity.class);
        settingsDialogActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        settingsDialogActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        settingsDialogActivity.putExtra(RescueMeConstants.POP_UP_ACTIVITY_TAG, RescueMeConstants.SETTINGS_DIALOG);
        context.startActivity(settingsDialogActivity);
    }
}
