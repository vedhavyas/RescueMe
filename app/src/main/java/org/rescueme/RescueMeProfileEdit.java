package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sromku.simple.fb.SimpleFacebook;


public class RescueMeProfileEdit extends Fragment {


    private EditText name;
    private EditText email;
    private EditText phoneNumber;
    private byte[] profilePic;
    private SimpleFacebook simpleFacebook;
    private RescueMeDBFactory dbFactory;
    private Context context;
    private SharedPreferences prefs;

    public RescueMeProfileEdit() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_profile_edit, container, false);
        context = rootView.getContext();
        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        phoneNumber = (EditText) rootView.findViewById(R.id.phoneNumber);
        Button updateProfileBtn = (Button) rootView.findViewById(R.id.updateProfileBtn);
        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new updateProfileTask().execute();
            }
        });

        Bundle userData = getArguments();
        name.setText(userData.getString(RescueMeConstants.COLUMN_NAME));
        email.setText(userData.getString(RescueMeConstants.COLUMN_EMAIL));
        profilePic = userData.getByteArray(RescueMeConstants.COLUMN_PROFILE_PIC);
        dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTable_name(RescueMeConstants.USER_TABLE);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    private String isDataValid() {
        if (!isNameEmpty()) {
            if (validEmail()) {
                if (validNumber()) {
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

    private boolean validEmail() {
        return email.getText().toString().contains(RescueMeConstants.EMAIL_REGEX);

    }

    private boolean validNumber() {
        return phoneNumber.getText().toString().length() == RescueMeConstants.PHONE_NUMBER_LENGTH && phoneNumber.getText().toString().matches("[0-9]+");

    }

    private boolean isNameEmpty() {
        return name.getText().toString().isEmpty();
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public class updateProfileTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = isDataValid();
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                RescueMeUserModel user = new RescueMeUserModel(name.getText().toString(),
                        email.getText().toString(),
                        phoneNumber.getText().toString(),
                        profilePic);
                int id = (int) dbFactory.registerUser(user);
                if (id > 0) {
                    prefs.edit().putString(RescueMeConstants.LOGGED_IN_USER_ID, String.valueOf(id)).apply();
                    prefs.edit().putString(RescueMeConstants.FB_USER_ID, String.valueOf(id)).apply();
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.FAILED;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                prefs.edit().putBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, false).apply();
                Toast.makeText(context, RescueMeConstants.REGISTRATION_SUCCESS, Toast.LENGTH_SHORT).show();
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
