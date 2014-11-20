package org.rescueme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class RescueMePopUpActivity extends Activity {

    private KeyguardManager.KeyguardLock lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String activityTag = getIntent().getStringExtra(RescueMeConstants.POP_UP_ACTIVITY_TAG);

        if (activityTag.equalsIgnoreCase(RescueMeConstants.SETTINGS_DIALOG)) {

            showSettingsDialog();

        } else if (activityTag.equalsIgnoreCase(RescueMeConstants.EMERGENCY_POP_UP)) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            String jsonData = getIntent().getStringExtra(RescueMeConstants.JSON_STRING);

            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            setContentView(R.layout.activity_rescue_me_pop_up);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
            lock.disableKeyguard();
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

            showEmergencyDialog(this, jsonData);
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Location Disabled");

        alertDialog
                .setMessage("Enable GPS to push your location details");

        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        alertDialog.show();
    }

    private void showEmergencyDialog(final Context context, final String jsonData) {

        final NotificationManager notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("RescueMe Emergency");

        alertDialog
                .setMessage("Someone is in Danger!!\n" +
                        "Please Respond");

        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton("Respond",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RescueMeUtilClass.writeToLog("Responded");
                        lock.reenableKeyguard();
                        finish();
                        notificationmanager.cancel(RescueMeConstants.NOTIFICATION_ID);
                        Intent intent = new Intent(context, RescueMeMapsActivity.class);
                        intent.putExtra(RescueMeConstants.JSON_STRING, jsonData);
                        startActivity(intent);

                    }
                });

        alertDialog.setNegativeButton(RescueMeConstants.CANCEL,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RescueMeUtilClass.writeToLog("Ignored");
                        dialog.cancel();
                        finish();
                        notificationmanager.cancel(RescueMeConstants.NOTIFICATION_ID);
                        lock.reenableKeyguard();
                    }
                });

        alertDialog.show();
    }

}
