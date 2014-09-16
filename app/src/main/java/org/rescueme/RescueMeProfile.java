package org.rescueme;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RescueMeProfile extends Fragment {

    private Context context;
    private SimpleFacebook simpleFacebook;
    private SharedPreferences prefs;
    private RescueMeDBFactory dbFactory;
    private ImageView profilePic;
    private TextView name;
    private TextView email;
    private TextView phoneNumber;
    private TextView rescueMessage;
    private String userId;
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
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        dbFactory = RescueMeDBFactory.getInstance(context);
        dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
        setHasOptionsMenu(true);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        name = (TextView) rootView.findViewById(R.id.name);
        email = (TextView) rootView.findViewById(R.id.email);
        phoneNumber = (TextView) rootView.findViewById(R.id.phoneNumber);
        rescueMessage = (TextView) rootView.findViewById(R.id.rescueMessage);
        userId = prefs.getString(RescueMeConstants.LOGGED_IN_USER_ID, String.valueOf(1));

        setProfile();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_actionbar_menu, menu);
        MenuItem refresh = menu.findItem(R.id.refreshProfile);
        MenuItem logout = menu.findItem(R.id.action_logout);
        MenuItem updateProfile = menu.findItem(R.id.updateProfile);
        logout.setVisible(false);
        updateProfile.setVisible(true);
        if (simpleFacebook.isLogin()) {
            refresh.setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.refreshProfile) {
            updateProfilePicture();
        } else if (itemId == R.id.updateProfile) {
            startUtilActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    private void setProfile() {
        new setProfileTask().execute();
    }

    private Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

    private void updateProfilePicture() {
        OnProfileListener onProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                Toast.makeText(context, RescueMeConstants.GOT_PROFILE_DATA, Toast.LENGTH_SHORT).show();
                new DownloadImageAndSetTask().execute(profile.getPicture());
            }
        };

        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(RescueMeConstants.FB_PROFILE_PIC_HEIGHT);
        pictureAttributes.setWidth(RescueMeConstants.FB_PROFILE_PIC_WIDTH);
        pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);

        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.NAME)
                .add(Profile.Properties.EMAIL)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        getActivity().setProgressBarIndeterminateVisibility(true);
        simpleFacebook.getProfile(properties, onProfileListener);
        Toast.makeText(context, RescueMeConstants.UPDATE_FB_PROFILE_PIC, Toast.LENGTH_SHORT).show();
    }

    private byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);

        return stream.toByteArray();
    }

    private void startUtilActivity(){
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
            userData = dbFactory.getUserDetails(userId);
            try {
                profilePicBitmap = getBitmapFromBlob(userData.getProfilePic());
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

    private class DownloadImageAndSetTask extends AsyncTask<String, Void, String> {

        public DownloadImageAndSetTask() {
            //empty constructor
        }

        protected String doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            byte[] blob = getBlob(bitmap);
            RescueMeUserModel user = new RescueMeUserModel();
            user.setId(userId);
            user.setProfilePic(blob);
            dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
            int result = dbFactory.updateProfilePicture(user);
            if (result > 0) {
                profilePicBitmap = getBitmapFromBlob(dbFactory.getUserDetails(userId).getProfilePic());
                return RescueMeConstants.SUCCESS;
            } else {
                return RescueMeConstants.UPDATE_FB_PROFILE_PIC_FAIL;
            }
        }

        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                profilePic.setImageBitmap(profilePicBitmap);
                Toast.makeText(context, RescueMeConstants.UPDATE_FB_PROFILE_PIC_SUCCESS, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

}