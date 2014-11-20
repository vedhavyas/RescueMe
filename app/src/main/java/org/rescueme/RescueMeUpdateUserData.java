package org.rescueme;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;


public class RescueMeUpdateUserData extends Fragment {


    private Context context;
    private ImageView profilePic;
    private EditText name;
    private EditText email;
    private EditText phoneNumber;
    private RescueMeUserModel userData;
    private RescueMeDBFactory dbFactory;
    private Bitmap profilePicBitmap;
    private String editMode;
    private String userId;
    private PopupWindow choosePicPopUp;

    public RescueMeUpdateUserData() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_update_user_data, container, false);
        context = rootView.getContext();
        SharedPreferences prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);
        profilePic = (ImageView) rootView.findViewById(R.id.profilePic);
        name = (EditText) rootView.findViewById(R.id.name);
        email = (EditText) rootView.findViewById(R.id.email);
        phoneNumber = (EditText) rootView.findViewById(R.id.number);
        editMode = getArguments().getString(RescueMeConstants.EDIT_MODE);
        dbFactory = RescueMeDBFactory.getInstance(context);

        RescueMeUtilClass.writeToLog(editMode);
        if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
            userId = getArguments().getString(RescueMeConstants.COLUMN_ID);
            dbFactory.setTableName(RescueMeConstants.CONTACTS_TABLE);
        } else {
            userId = String.valueOf(prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1));
            dbFactory.setTableName(RescueMeConstants.USER_TABLE);
        }

        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
                actionBar.setTitle(RescueMeConstants.UPDATE_EMERGENCY_CONTACT);
            } else {
                actionBar.setTitle(RescueMeConstants.UPDATE_PROFILE);
            }
        }

        new setUI().execute();
        setChoosePickPopUp();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                choosePicPopUp.showAsDropDown(profilePic, size.x / 2 - 575, -300);
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
        MenuItem discard = menu.findItem(R.id.discardContact);
        logout.setVisible(false);
        accept.setVisible(true);
        if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
            discard.setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.doneEditing) {
            new updateProfileTask().execute();
        } else if (itemId == R.id.discardContact) {
            new deleteContactTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getImageFromCamera() {
        RescueMeUtilClass.writeToLog("Choose pic from Camera!!");
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RescueMeConstants.SELECT_PICTURE);
    }


    private void getImageFromGallery() {
        RescueMeUtilClass.writeToLog("Choose pic from Gallery!!");
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

                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(400,
                        400, croppedImage);
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
        if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
            intent.putExtra(RescueMeConstants.SELECT_TAG, 1);
        } else {
            intent.putExtra(RescueMeConstants.SELECT_TAG, 2);
        }
        startActivity(intent);
    }

    private void setChoosePickPopUp() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View popView = inflater.inflate(R.layout.popup_choose_pic, null);
        choosePicPopUp = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        choosePicPopUp.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));
        choosePicPopUp.setOutsideTouchable(true);
        ImageButton cameraBtn = (ImageButton) popView.findViewById(R.id.cameraBtn);
        ImageButton galleryBtn = (ImageButton) popView.findViewById(R.id.galleryBtn);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicPopUp.dismiss();
                getImageFromCamera();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicPopUp.dismiss();
                getImageFromGallery();
            }
        });
    }

    private class setUI extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
                userData = dbFactory.getContact(userId);
            } else {
                userData = dbFactory.getUserDetails(userId);
            }
            profilePicBitmap = RescueMeUtilClass.getBitmapFromBlob(userData.getProfilePic());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (userData == null) {
                RescueMeUtilClass.toastAndLog(context, "Oops!! something happened..");
            } else {
                RescueMeUtilClass.writeToLog("Name - " + userData.getName());
                name.setText(userData.getName());
                email.setText(userData.getEmail());
                phoneNumber.setText(userData.getNumber());
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
            userData.setId(userId);
            userData.setName(name.getText().toString());
            userData.setEmail(email.getText().toString());
            userData.setNumber(phoneNumber.getText().toString());
            if (profilePicBitmap != null) {
                userData.setProfilePic(RescueMeUtilClass.getBlob(profilePicBitmap));
            }
            String result = RescueMeUtilClass.isDataValid(userData);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
                    if (dbFactory.updateEmergencyContact(userData) > 0) {
                        RescueMeUtilClass.writeToLog("Updated emergency contact...");
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.UPDATE_EMERGENCY_CONTACT_FAIL;
                    }
                } else {
                    if (dbFactory.updateUserData(userData) > 0) {
                        RescueMeUtilClass.writeToLog("User profile updated..");
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.UPDATE_PROFILE_FAIL;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                if (editMode.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
                    RescueMeUtilClass.toastAndLog(context, "Contact details updated!!");
                } else {
                    RescueMeUtilClass.toastAndLog(context, "Profile updated!!");
                }
                loadAuthenticatedActivity();
            } else {
                RescueMeUtilClass.toastAndLog(context, result);
            }
        }
    }

    private class deleteContactTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getActivity().setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            if (dbFactory.deleteContact(userId) < 0) {
                return RescueMeConstants.DELETE_EMERGENCY_CONTACT_FAIL;
            } else {
                return RescueMeConstants.SUCCESS;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                RescueMeUtilClass.toastAndLog(context, "Contact removed from List!!");
                loadAuthenticatedActivity();
            } else {
                RescueMeUtilClass.toastAndLog(context, result);
            }
        }
    }
}
