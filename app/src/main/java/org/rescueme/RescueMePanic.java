package org.rescueme;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class RescueMePanic extends Fragment {


    private Context context;
    private RescueMeLocationService locationService;
    private Location location;
    public RescueMePanic() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_panic, container, false);
        context = rootView.getContext();
        ImageButton panicBtn = (ImageButton) rootView.findViewById(R.id.panicBtn);
        locationService = new RescueMeLocationService(context);
        panicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox();
            }
        });
        return rootView;
    }


    private void showDialogBox() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(RescueMeConstants.DIALOG_TITLE);
        alertDialog.setMessage(RescueMeConstants.DIALOG_MESSAGE);
        alertDialog.setIcon(R.drawable.ic_launcher);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, RescueMeConstants.PANIC_FALSE_ALARM, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, RescueMeConstants.PANIC_CNF, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendEmergencyAlerts();
            }
        });

        alertDialog.show();

    }

    private void sendEmergencyAlerts() {
        location = locationService.getLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            showSettingsAlert(LocationManager.NETWORK_PROVIDER);
        } else {
            RescueMeSendSMS smsTask = new RescueMeSendSMS(getActivity());
            smsTask.execute(location);
        }
    }


    public void showSettingsAlert(String provider) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle(provider + RescueMeConstants.SETTINGS);

        alertDialog
                .setMessage(provider + RescueMeConstants.PROVIDER_NOT_ENABLED);

        alertDialog.setIcon(R.drawable.ic_launcher);

        alertDialog.setPositiveButton(RescueMeConstants.SETTINGS,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RescueMeConstants.GPS_LOCATION_SETTINGS) {
            new android.os.Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, RescueMeConstants.GET_GPS_LOCATION_AGAIN, Toast.LENGTH_SHORT).show();
                    sendEmergencyAlerts();
                }
            }, RescueMeConstants.GPS_AFTER_SETTINGS_SLEEP);

        }
    }

}
