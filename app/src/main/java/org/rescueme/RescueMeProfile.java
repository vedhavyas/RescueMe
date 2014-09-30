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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RescueMeProfile extends Fragment {

    private Context context;
    private RescueMeDBFactory dbFactory;
    private ImageView profilePic;
    private TextView name;
    private TextView email;
    private TextView phoneNumber;
    private TextView rescueMessage;
    private int userId;
    private RescueMeUserModel userData;
    private Bitmap profilePicBitmap;
    public RescueMeProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_profile, container, false);
        context = rootView.getContext();
        SharedPreferences prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTableName(RescueMeConstants.USER_TABLE);
        setHasOptionsMenu(true);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        name = (TextView) rootView.findViewById(R.id.name);
        email = (TextView) rootView.findViewById(R.id.email);
        phoneNumber = (TextView) rootView.findViewById(R.id.phoneNumber);
        rescueMessage = (TextView) rootView.findViewById(R.id.rescueMessage);
        userId = prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1);

        setProfile();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_actionbar_menu, menu);
        MenuItem logout = menu.findItem(R.id.action_logout);
        MenuItem updateProfile = menu.findItem(R.id.updateProfile);
        logout.setVisible(false);
        updateProfile.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.updateProfile) {
            startUtilActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setProfile() {
        new setProfileTask().execute();
    }

    private void startUtilActivity() {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_PROFILE);
        startActivity(intent);
    }

    public class setProfileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            userData = dbFactory.getUserDetails(String.valueOf(userId));
            try {
                profilePicBitmap = RescueMeUtilClass.getBitmapFromBlob(userData.getProfilePic());
            } catch (NullPointerException e) {
                Log.i(RescueMeConstants.LOG_TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            name.setText(userData.getName());
            email.setText(userData.getEmail());
            phoneNumber.setText(userData.getNumber());
            rescueMessage.setText(userData.getMessage());
            if (profilePicBitmap != null) {
                profilePic.setImageBitmap(profilePicBitmap);
            }
        }
    }

}