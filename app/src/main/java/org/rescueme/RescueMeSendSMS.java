package org.rescueme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.telephony.SmsManager;

import java.util.List;

/**
 * Authored by Vedhavyas Singareddi on 16-09-2014.
 */
public class RescueMeSendSMS extends AsyncTask<Location, Void, String> {

    private Context context;
    private Activity activity;
    private RescueMeDBFactory dbFactory;
    private SmsManager smsManager;
    private SharedPreferences prefs;

    public RescueMeSendSMS(Activity activity) {
        this.activity = activity;
        this.context = activity.getBaseContext();
        this.dbFactory = RescueMeDBFactory.getInstance(context);
        this.smsManager = SmsManager.getDefault();
        prefs = activity.getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
    }

    @Override
    protected void onPreExecute() {
        activity.setProgressBarIndeterminateVisibility(true);
    }

    @Override
    protected String doInBackground(Location... locations) {
        if (triggerSMS(locations[0])) {
            return RescueMeConstants.SUCCESS;
        } else {
            return RescueMeConstants.FAILED;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        activity.setProgressBarIndeterminateVisibility(false);
        if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
            RescueMeUtilClass.toastAndLog(context, "SMS Sent!!");
        } else {
            RescueMeUtilClass.toastAndLog(context, "Failed to send SMS!!");
        }
    }

    public boolean triggerSMS(Location location) {
        dbFactory.setTableName(RescueMeConstants.CONTACTS_TABLE);
        List<RescueMeUserModel> contacts = dbFactory.getAllContacts();

        dbFactory.setTableName(RescueMeConstants.USER_TABLE);
        String personalMessage = prefs.getString(RescueMeConstants.CUSTOM_MESSAGE, RescueMeConstants.DEFAULT_CUSTOM_MESSAGE);
        personalMessage = personalMessage + "\n" + RescueMeConstants.ADD_EXTRA_MESSAGE + "\n" + RescueMeConstants.LATITUDE +
                location.getLatitude() + "\n" + RescueMeConstants.LONGITUDE + location.getLongitude();
        if (contacts != null) {
            for (RescueMeUserModel contact : contacts) {
                if (contact.getNumber() != null) {
                    smsManager.sendTextMessage(contact.getNumber(), null, personalMessage, null, null);
                }
            }
        } else {
            return false;
        }

        return true;
    }


}
