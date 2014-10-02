package org.rescueme;

/**
 * Authored by Vedhavyas Singareddi on 15-09-2014.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class RescueMeLocationService extends Service implements LocationListener {

    protected LocationManager locationManager;


    public RescueMeLocationService(Context context) {
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            locationManager.requestLocationUpdates(provider,
                    RescueMeConstants.MIN_TIME_FOR_UPDATE, RescueMeConstants.MIN_DISTANCE_FOR_UPDATE, this);
            if (locationManager != null) {
                return locationManager.getLastKnownLocation(provider);
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(RescueMeConstants.LOG_TAG, provider + " disabled");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(RescueMeConstants.LOG_TAG, provider + " enabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public boolean isProvidersEnabled(String provider) {
        return locationManager.isProviderEnabled(provider);
    }

    public void showSettingsAlert(final Activity activity, String provider) {
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


}