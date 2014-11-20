package org.rescueme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class RescueMeLocationService extends Service implements LocationListener {

    private static Location lastKnownLocation;
    protected LocationManager locationManager;


    public RescueMeLocationService() {
    }

    public static Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        RescueMeUtilClass.writeToLog("Location service created!!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, RescueMeConstants.MIN_TIME_FOR_UPDATE, RescueMeConstants.MIN_DISTANCE_FOR_UPDATE, this);
        RescueMeUtilClass.writeToLog("Location service started!!");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        prefs.edit().putBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, false).apply();
        RescueMeUtilClass.writeToLog("Location service destroyed!!");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            lastKnownLocation = location;

            //TODO remove notification for location change
            sendBroadcast(new Intent(RescueMeConstants.LOCATION_CHANGED));
            RescueMeUtilClass.writeToLog("lat : " + String.valueOf(location.getLatitude()) + " -- " +
                    "long : " + String.valueOf(location.getLongitude()));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        RescueMeUtilClass.writeToLog(provider + " is enabled!!");
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, RescueMeConstants.MIN_TIME_FOR_UPDATE, RescueMeConstants.MIN_DISTANCE_FOR_UPDATE, this);
        RescueMeUtilClass.writeToLog("Location updates requested!!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        RescueMeUtilClass.writeToLog(provider + " is disabled!!");
        sendBroadcast(new Intent(RescueMeConstants.SHOW_SETTINGS_DIALOG));
    }

}
