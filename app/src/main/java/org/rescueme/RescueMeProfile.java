package org.rescueme;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.android.camera.CropImageIntentBuilder;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private File croppedImageFile;
    private String tempImagePath;

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

    private void startUtilActivity() {
        Intent intent = new Intent(context, RescueMeUtilActivity.class);
        intent.putExtra(RescueMeConstants.FRAGMENT_TAG, RescueMeConstants.UPDATE_PROFILE);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(getActivity(), requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RescueMeConstants.CROP_IMAGE) {
            profilePicBitmap = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
            profilePic.setImageBitmap(profilePicBitmap);
            if (tempImagePath != null) {
                File tempFile = new File(tempImagePath);
                if (tempFile.delete()) {
                    tempImagePath = null;
                }
            }
            new UpdateBlobInDB().execute();
        }

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

    private class DownloadImageAndSetTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageAndSetTask() {
            //empty constructor
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(RescueMeConstants.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return bitmap;

        }

        protected void onPostExecute(Bitmap bitmap) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (bitmap != null) {
                croppedImageFile = new File(context.getFilesDir(), "cropFile.jpg");
                Uri croppedImage = Uri.fromFile(croppedImageFile);
                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(450,
                        450, croppedImage);
                File outDir = context.getCacheDir().getAbsoluteFile();
                tempImagePath = outDir + "/temp_image";
                try {
                    FileOutputStream fos = new FileOutputStream(tempImagePath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    cropImage.setSourceImage(Uri.fromFile(new File(tempImagePath)));
                    startActivityForResult(cropImage.getIntent(getActivity()), RescueMeConstants.CROP_IMAGE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class UpdateBlobInDB extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            RescueMeUserModel user = new RescueMeUserModel();
            user.setId(userId);
            byte[] blob = RescueMeUtilClass.getBlob(profilePicBitmap);
            if (blob != null) {
                user.setProfilePic(blob);
                dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
                int result = dbFactory.updateProfilePicture(user);
                if (result > 0) {
                    profilePicBitmap = RescueMeUtilClass.getBitmapFromBlob(dbFactory.getUserDetails(userId).getProfilePic());
                    return RescueMeConstants.UPDATE_FB_PROFILE_PIC_SUCCESS;
                }
            }

            return RescueMeConstants.UPDATE_FB_PROFILE_PIC_FAIL;
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}