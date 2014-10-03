package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RescueMeSettings extends Fragment {

    private ImageView profilePic;
    private TextView name, email;
    private Context context;
    private RescueMeDBFactory dbFactory;
    private String userID;
    private RescueMeUserModel userData;

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
        SharedPreferences prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        userID = String.valueOf(prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1));

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUtilActivity();
            }
        });
        customMessageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Custom message", Toast.LENGTH_SHORT).show();
            }
        });

        new setProfileTask().execute();
        return rootView;
    }


    private void startUtilActivity() {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_PROFILE);
        startActivity(intent);
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
