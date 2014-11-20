package org.rescueme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

/**
 * Authored by vedhavyas.singareddi on 18-09-2014.
 */
public class RescueMeUtilClass {

    public static void sendEmergencyAlerts(Activity activity) {
        Location location = RescueMeLocationService.getLastKnownLocation();
        if (location == null) {
            RescueMeUtilClass.writeToLog("Location is Null");
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                RescueMeUtilClass.writeToLog("Show the Dialog!!");
                RescueMeUtilClass.showSettingsAlert(activity, LocationManager.NETWORK_PROVIDER);
            } else {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if (location != null) {
            RescueMeUtilClass.writeToLog(location.getLatitude() + " - " + location.getLongitude());
        }

        SharedPreferences prefs = activity.getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);

        if (prefs.getBoolean(RescueMeConstants.SEND_SMS, true) && location != null) {
            RescueMeUtilClass.toastAndLog(activity, "Sending SMS...");
            RescueMeSendSMS smsTask = new RescueMeSendSMS(activity);
            smsTask.execute(location);
        }
    }

    public static byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] blob = null;
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blob = stream.toByteArray();
        }

        return blob;
    }

    public static Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

    public static void toastAndLog(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.i(RescueMeConstants.LOG_TAG, message);
    }

    public static void writeToLog(String message) {
        Log.i(RescueMeConstants.LOG_TAG, message);
    }

    public static void showSettingsAlert(final Activity activity, String provider) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        alertDialog.setTitle(provider + RescueMeConstants.SETTINGS);

        alertDialog
                .setMessage(provider + RescueMeConstants.PROVIDER_NOT_ENABLED);

        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton(RescueMeConstants.SETTINGS,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                RescueMeConstants.GPS_LOCATION_SETTINGS);
                    }
                });

        alertDialog.setNegativeButton(RescueMeConstants.CANCEL,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }


    public static String isDataValid(RescueMeUserModel contact) {
        if (!isNameEmpty(contact.getName())) {
            if (validEmail(contact.getEmail())) {
                if (validNumber(contact.getNumber())) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.PHONE_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        } else {
            return RescueMeConstants.NAME_EMPTY;
        }

    }

    private static boolean validEmail(String email) {
        return email.contains(RescueMeConstants.EMAIL_REGEX);

    }

    private static boolean validNumber(String phoneNumber) {
        return phoneNumber.length() >= RescueMeConstants.PHONE_NUMBER_LENGTH;

    }

    private static boolean isNameEmpty(String name) {
        return name.isEmpty();
    }
}
