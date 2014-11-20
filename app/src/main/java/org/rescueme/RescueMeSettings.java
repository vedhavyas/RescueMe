package org.rescueme;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLoginListener;

public class RescueMeSettings extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ImageView profilePic;
    private TextView name, email, connectToFB, connectToGPlus;
    private Context context;
    private RescueMeDBFactory dbFactory;
    private String userID;
    private RescueMeUserModel userData;
    private SharedPreferences prefs;
    private CheckBox sendSMS, sendEmail, sendPush, receiveSMS, receiveEmail, receivePush, rescuer, pushLocationsUpdates;
    private SeekBar rescueMeDistance, rescuerDistance;
    private TextView receiveAlertView;
    private PopupWindow seekBarPopUp;
    private TextView seekBarValue;
    private LinearLayout rescueMeLinearLayout, rescuerLinearLayout;
    private Point size;
    private SimpleFacebook simpleFacebook;
    private GoogleApiClient googleApiClient;
    private OnLoginListener fbLoginListener;
    private ImageButton fbConnectBtn, gPlusConnectBtn;
    private boolean userConnectGPlus = false;


    public RescueMeSettings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_settings, container, false);
        context = rootView.getContext();
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        name = (TextView) rootView.findViewById(R.id.name);
        email = (TextView) rootView.findViewById(R.id.email);
        ImageButton editProfile = (ImageButton) rootView.findViewById(R.id.editProfile);
        LinearLayout customMessageTab = (LinearLayout) rootView.findViewById(R.id.customMessage);
        dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTableName(RescueMeConstants.USER_TABLE);
        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        userID = String.valueOf(prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1));
        receiveAlertView = (TextView) rootView.findViewById(R.id.receiveAlertView);
        rescueMeLinearLayout = (LinearLayout) rootView.findViewById(R.id.rescueMeLinearLayout);
        rescuerLinearLayout = (LinearLayout) rootView.findViewById(R.id.rescuerLinearLayout);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Plus.API)
                .build();
        fbConnectBtn = (ImageButton) rootView.findViewById(R.id.fbConnectBtn);
        gPlusConnectBtn = (ImageButton) rootView.findViewById(R.id.googlePlusConnectBtn);
        connectToFB = (TextView) rootView.findViewById(R.id.connectToFB);
        connectToGPlus = (TextView) rootView.findViewById(R.id.connectToGPlus);
        googleApiClient.connect();

        fbConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fbConnectBtn.setEnabled(false);
                simpleFacebook.login(fbLoginListener);
            }
        });

        gPlusConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gPlusConnectBtn.setEnabled(false);
                userConnectGPlus = true;
                googleApiClient.connect();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUtilActivity();
            }
        });

        customMessageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomMessageDialog();
            }
        });

        setCheckBoxOnClickListeners(rootView);
        setSeekerChangeListeners(rootView);
        setSeekBarPopUp();
        setSocialConnections();

        new setProfileTask().execute();
        setPreferredSettings();
        setFbLoginListener();
        return rootView;
    }


    private void startUtilActivity() {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_PROFILE);
        startActivity(intent);
    }

    private void setCheckBoxOnClickListeners(View view) {
        sendSMS = (CheckBox) view.findViewById(R.id.sendSms);
        sendEmail = (CheckBox) view.findViewById(R.id.sendEmail);
        sendPush = (CheckBox) view.findViewById(R.id.sendPush);
        receiveSMS = (CheckBox) view.findViewById(R.id.receiveSms);
        receiveEmail = (CheckBox) view.findViewById(R.id.receiveEmail);
        receivePush = (CheckBox) view.findViewById(R.id.receivePush);
        rescuer = (CheckBox) view.findViewById(R.id.isRescuer);
        pushLocationsUpdates = (CheckBox) view.findViewById(R.id.getLocation);

        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_SMS, true).apply();
                    RescueMeUtilClass.writeToLog("Send SMS - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_SMS, false).apply();
                    RescueMeUtilClass.writeToLog("Send SMS - false");
                }
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_EMAIL, true).apply();
                    RescueMeUtilClass.writeToLog("Send Email - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_EMAIL, false).apply();
                    RescueMeUtilClass.writeToLog("Send Email - false");
                }
            }
        });

        sendPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_PUSH, true).apply();
                    RescueMeUtilClass.writeToLog("Send Push - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.SEND_PUSH, false).apply();
                    RescueMeUtilClass.writeToLog("Send Push - false");
                }
            }
        });

        pushLocationsUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {

                    RescueMeUtilClass.writeToLog("Push Location Updates - true");
                    prefs.edit().putBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, true).apply();
                    Intent locationServiceIntent = new Intent(context, RescueMeLocationService.class);
                    context.startService(locationServiceIntent);
                    RescueMeUtilClass.writeToLog("Starting service...");

                } else {
                    RescueMeUtilClass.writeToLog("Push location updates - false!!");
                    prefs.edit().putBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, false).apply();
                    Intent locationServiceIntent = new Intent(context, RescueMeLocationService.class);
                    context.stopService(locationServiceIntent);
                    RescueMeUtilClass.writeToLog("Stopping service...");
                }
            }
        });

        receiveSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_SMS, true).apply();
                    RescueMeUtilClass.writeToLog("Receive SMS - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_SMS, false).apply();
                    RescueMeUtilClass.writeToLog("Receive SMS - false");
                }
            }
        });

        receiveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_EMAIL, true).apply();
                    RescueMeUtilClass.writeToLog("Receive Email - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_EMAIL, false).apply();
                    RescueMeUtilClass.writeToLog("Receive Email - false");
                }
            }
        });

        receivePush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_PUSH, true).apply();
                    RescueMeUtilClass.writeToLog("Receive Push - true");
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.RECEIVE_PUSH, false).apply();
                    RescueMeUtilClass.writeToLog("Receive Push - false");
                }
            }
        });

        rescuer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    prefs.edit().putBoolean(RescueMeConstants.IS_RESCUER, true).apply();
                    RescueMeUtilClass.writeToLog("Rescuer - true");
                    setRescuerSettings(true);
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.IS_RESCUER, false).apply();
                    RescueMeUtilClass.writeToLog("Rescuer - false");
                    setRescuerSettings(false);
                }
            }
        });
    }

    private void setSeekerChangeListeners(final View view) {
        rescueMeDistance = (SeekBar) view.findViewById(R.id.rescueMeDistance);
        rescuerDistance = (SeekBar) view.findViewById(R.id.rescuerDistance);

        rescueMeDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int changedValue = prefs.getInt(RescueMeConstants.RESCUE_DISTANCE, 400);

            @Override
            public void onProgressChanged(SeekBar seekBar, int changedValue, boolean b) {
                this.changedValue = changedValue;
                seekBarValue.setText(String.valueOf(this.changedValue) + " Metres");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarValue.setText(String.valueOf(this.changedValue) + " Metres");
                seekBarPopUp.showAsDropDown(rescueMeLinearLayout, size.x / 2 - 350, -400);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (changedValue != 0) {
                    prefs.edit().putInt(RescueMeConstants.RESCUE_DISTANCE, changedValue).apply();
                    RescueMeUtilClass.writeToLog(String.valueOf(changedValue));
                }
                seekBarPopUp.dismiss();
            }
        });

        rescuerDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int changedValue = prefs.getInt(RescueMeConstants.RESCUER_DISTANCE, 400);

            @Override
            public void onProgressChanged(SeekBar seekBar, int changedValue, boolean b) {
                this.changedValue = changedValue;
                seekBarValue.setText(String.valueOf(this.changedValue) + " Metres");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarValue.setText(String.valueOf(this.changedValue) + " Metres");
                seekBarPopUp.showAsDropDown(rescuerLinearLayout, size.x / 2 - 350, -400);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (changedValue != 0) {
                    prefs.edit().putInt(RescueMeConstants.RESCUER_DISTANCE, changedValue).apply();
                    RescueMeUtilClass.writeToLog(String.valueOf(changedValue));
                }
                seekBarPopUp.dismiss();
            }
        });
    }

    private void setPreferredSettings() {
        sendSMS.setChecked(prefs.getBoolean(RescueMeConstants.SEND_SMS, true));
        sendEmail.setChecked(prefs.getBoolean(RescueMeConstants.SEND_EMAIL, true));
        sendPush.setChecked(prefs.getBoolean(RescueMeConstants.SEND_PUSH, true));
        pushLocationsUpdates.setChecked(prefs.getBoolean(RescueMeConstants.PUSH_LOCATION_UPDATES, false));
        rescueMeDistance.setProgress(prefs.getInt(RescueMeConstants.RESCUE_DISTANCE, 400));

        boolean isRescuer = prefs.getBoolean(RescueMeConstants.IS_RESCUER, true);
        rescuer.setChecked(isRescuer);
        setRescuerSettings(isRescuer);
        receiveSMS.setChecked(prefs.getBoolean(RescueMeConstants.RECEIVE_SMS, true));
        receiveEmail.setChecked(prefs.getBoolean(RescueMeConstants.RECEIVE_EMAIL, true));
        receivePush.setChecked(prefs.getBoolean(RescueMeConstants.RECEIVE_PUSH, true));
        rescuerDistance.setProgress(prefs.getInt(RescueMeConstants.RESCUER_DISTANCE, 400));
    }

    private void setRescuerSettings(boolean isRescuer) {
        receiveSMS.setEnabled(isRescuer);
        receiveEmail.setEnabled(isRescuer);
        receivePush.setEnabled(isRescuer);
        rescuerDistance.setEnabled(isRescuer);
        receiveAlertView.setEnabled(isRescuer);
    }

    private void setSeekBarPopUp() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View popView = inflater.inflate(R.layout.popup_window_seekbar, null);
        seekBarPopUp = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        seekBarValue = (TextView) popView.findViewById(R.id.seekBarValue);
    }

    private void setSocialConnections() {
        if (simpleFacebook.isLogin()) {
            connectToFB.setText(RescueMeConstants.FB_LOGIN_SUCCESS);
            fbConnectBtn.setEnabled(false);
        }

        if (googleApiClient.isConnected()) {
            connectToGPlus.setText(RescueMeConstants.GOOGLE_CONNECTION_SUCCESS);
            gPlusConnectBtn.setEnabled(false);
        }
    }

    private void showCustomMessageDialog() {
        final AlertDialog.Builder customDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_message_dialog, null);
        final EditText customMessage = (EditText) view.findViewById(R.id.customMessage);
        customDialog.setView(view);
        customDialog.setIcon(R.drawable.ic_launcher);
        customDialog.setTitle(RescueMeConstants.CUSTOM_MESSAGE);
        customDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                prefs.edit().putString(RescueMeConstants.CUSTOM_MESSAGE
                        , customMessage.getText().toString()).apply();

            }
        });

        customMessage.setText(prefs.getString(RescueMeConstants.CUSTOM_MESSAGE, RescueMeConstants.DEFAULT_CUSTOM_MESSAGE));
        customDialog.show();
    }

    private void setFbLoginListener() {
        fbLoginListener = new OnLoginListener() {
            @Override
            public void onLogin() {
                RescueMeUtilClass.toastAndLog(context, "Connected to Facebook!!");
                fbConnectBtn.setEnabled(false);
                connectToFB.setText(RescueMeConstants.FB_LOGIN_SUCCESS);
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                RescueMeUtilClass.toastAndLog(context, "Permissions were not accepted!!");
                fbConnectBtn.setEnabled(true);
            }

            @Override
            public void onThinking() {
            }

            @Override
            public void onException(Throwable throwable) {
                RescueMeUtilClass.toastAndLog(context, "Connection Exception!!");
                fbConnectBtn.setEnabled(true);
            }

            @Override
            public void onFail(String reason) {
                RescueMeUtilClass.toastAndLog(context, reason);
                fbConnectBtn.setEnabled(true);
            }
        };
    }


    @Override
    public void onConnected(Bundle bundle) {
        connectToGPlus.setText(RescueMeConstants.GOOGLE_CONNECTION_SUCCESS);
        gPlusConnectBtn.setEnabled(false);
    }

    @Override
    public void onConnectionSuspended(int i) {
        gPlusConnectBtn.setEnabled(true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution() && userConnectGPlus) {
            RescueMeUtilClass.writeToLog("Connection has Resolution");
            try {
                result.startResolutionForResult(getActivity(), RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                RescueMeUtilClass.writeToLog("Exception caught!! Reconnecting to client");
                googleApiClient.connect();
            }
        } else {
            RescueMeUtilClass.writeToLog("No resolution!! Showing error Dialog");
            showErrorDialog(result.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialog_error", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errorDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                RescueMeUtilClass.writeToLog("Result code success for Google Login");
                googleApiClient.connect();
            } else {
                gPlusConnectBtn.setEnabled(true);
            }
        }
    }

    private class setProfileTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            userData = dbFactory.getUserDetails(String.valueOf(userID));
            Bitmap profilePicBitmap = null;
            try {
                profilePicBitmap = RescueMeUtilClass.getBitmapFromBlob(userData.getProfilePic());
            } catch (NullPointerException e) {
                Log.i(RescueMeConstants.LOG_TAG, e.toString());
            }
            return profilePicBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap profilePicBitmap) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            name.setText(userData.getName());
            email.setText(userData.getEmail());
            if (profilePicBitmap != null) {
                profilePic.setImageBitmap(profilePicBitmap);
            }
        }
    }

}
