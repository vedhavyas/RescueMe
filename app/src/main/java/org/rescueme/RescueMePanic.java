package org.rescueme;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class RescueMePanic extends Fragment {


    private Context context;
    private RescueMeLocationService locationService;
    private Location location;
    private Activity activity;
    public RescueMePanic() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_panic, container, false);
        activity = getActivity();
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
            locationService.showSettingsAlert(activity, LocationManager.NETWORK_PROVIDER);
        } else {
            RescueMeSendSMS smsTask = new RescueMeSendSMS(getActivity());
            smsTask.execute(location);
        }
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
