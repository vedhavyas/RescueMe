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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;


public class RescueMeUpdateProfile extends Fragment {


    private Context context;
    private ImageView profilePic;
    private EditText name;
    private EditText email;
    private EditText phoneNumber;
    private EditText personalMessage;
    private RescueMeUserModel userData;
    private RescueMeDBFactory dbFactory;
    private SharedPreferences prefs;
    private Bitmap profilePicBitmap;

    public RescueMeUpdateProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_update_profile, container, false);
        context = rootView.getContext();
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        phoneNumber = (EditText) rootView.findViewById(R.id.phoneNumber);
        personalMessage = (EditText) rootView.findViewById(R.id.personalMessage);
        dbFactory = RescueMeDBFactory.getInstance(context);
        try {
            getActivity().getActionBar().setTitle(RescueMeConstants.UPDATE_PROFILE);
        } catch (NullPointerException e) {
            Log.i(RescueMeConstants.LOG_TAG, RescueMeConstants.EXCEPTION_CAUGHT);
        }
        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        new setUI().execute();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rescue_me_actionbar_menu, menu);
        MenuItem logout = menu.findItem(R.id.action_logout);
        MenuItem accept = menu.findItem(R.id.doneEditing);
        logout.setVisible(false);
        accept.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.doneEditing) {
            Toast.makeText(context, RescueMeConstants.UPDATING_USER_PROFILE, Toast.LENGTH_SHORT).show();
            new updateProfileTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private String isDataValid() {
        if (!isNameEmpty()) {
            if (validEmail()) {
                if (validNumber()) {
                    if (!isMessageEmpty()) {
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.MESSAGE_EMPTY;
                    }
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

    private boolean isMessageEmpty() {
        return personalMessage.getText().toString().isEmpty();
    }

    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RescueMeConstants.SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        File croppedImageFile = new File(context.getFilesDir(), "cropFile.jpg");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RescueMeConstants.SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Uri croppedImage = Uri.fromFile(croppedImageFile);

                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200,
                        200, croppedImage);
                cropImage.setSourceImage(selectedImageUri);
                startActivityForResult(cropImage.getIntent(getActivity()), RescueMeConstants.CROP_IMAGE);
            } else if (requestCode == RescueMeConstants.CROP_IMAGE) {
                profilePicBitmap = BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath());
                profilePic.setImageBitmap(profilePicBitmap);
            }
        }
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(RescueMeConstants.SELECT_TAG, 1);
        startActivity(intent);
    }

    private class setUI extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
            userData = dbFactory.getUserDetails(String.valueOf(prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1)));
            profilePicBitmap = RescueMeUtilClass.getBitmapFromBlob(userData.getProfilePic());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (userData == null) {
                Toast.makeText(context, RescueMeConstants.FAILED_TO_GET_USER_PROFILE, Toast.LENGTH_SHORT).show();
            } else {
                name.setText(userData.getName());
                email.setText(userData.getEmail());
                phoneNumber.setText(userData.getNumber());
                personalMessage.setText(userData.getMessage());
                if (profilePicBitmap != null) {
                    profilePic.setImageBitmap(profilePicBitmap);
                }
            }
        }
    }

    private class updateProfileTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = isDataValid();
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                userData.setId(String.valueOf(prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1)));
                userData.setName(name.getText().toString());
                userData.setEmail(email.getText().toString());
                userData.setNumber(phoneNumber.getText().toString());
                userData.setMessage(personalMessage.getText().toString());
                if (profilePicBitmap != null) {
                    userData.setProfilePic(RescueMeUtilClass.getBlob(profilePicBitmap));
                }
                dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
                if (dbFactory.updateUserData(userData) > 0) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.UPDATE_PROFILE_FAIL;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                Toast.makeText(context, RescueMeConstants.UPDATE_PROFILE_SUCCESS, Toast.LENGTH_SHORT).show();
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
