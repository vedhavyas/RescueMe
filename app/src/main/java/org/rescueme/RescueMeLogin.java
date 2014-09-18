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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;


public class RescueMeLogin extends Fragment {

    private EditText email;
    private EditText password;
    private SharedPreferences prefs;
    private Button logInBtn;
    private Button register;
    private ImageButton fbLogInBtn;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private Context context;
    private OnProfileListener onProfileListener;
    private Profile fbUserProfile;
    private byte[] blob;
    private File croppedImageFile;
    private String tempImagePath;

    public RescueMeLogin() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleFacebook = SimpleFacebook.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rescue_me_login, container, false);

        //initialize variables
        email = (EditText) rootView.findViewById(R.id.email);
        password = (EditText) rootView.findViewById(R.id.password);
        logInBtn = (Button) rootView.findViewById(R.id.logInBtn);
        fbLogInBtn = (ImageButton) rootView.findViewById(R.id.fbLoginBtn);
        register = (Button) rootView.findViewById(R.id.loginRegisterBtn);
        context = rootView.getContext();

        //initialize functions
        ((RescueMe) getActivity()).setTitle(RescueMeConstants.LOGIN);
        setFbLoginListener();

        //set onClickListeners
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegistrationFragment();
            }
        });
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LogIn().execute(v);
            }
        });
        fbLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleFacebook.login(fbLoginListener);
            }
        });

        prefs = getActivity().getSharedPreferences(RescueMeConstants.PREFERENCE_NAME
                , Context.MODE_PRIVATE);

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
        if (resultCode == Activity.RESULT_OK && requestCode == RescueMeConstants.CROP_IMAGE) {
            blob = RescueMeUtilClass.getBlob(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
            if (tempImagePath != null) {
                File tempFile = new File(tempImagePath);
                if (tempFile.delete()) {
                    tempImagePath = null;
                }
            }
            loadProfileEditFragment();
        }
    }

    private void loadRegistrationFragment() {
        ((RescueMe) getActivity()).loadFragment(RescueMeConstants.REGISTER, null);
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(context, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private boolean isEmailValid() {
        return email.getText().toString().contains("@");
    }

    private boolean isPasswordEmpty() {
        return password.getText().toString().isEmpty();
    }

    private void setFbLoginListener() {
        fbLoginListener = new OnLoginListener() {
            @Override
            public void onLogin() {
                Toast.makeText(context, RescueMeConstants.FB_LOGIN_SUCCESS, Toast.LENGTH_SHORT)
                        .show();
                if (prefs.getBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, true)) {
                    getProfileInfo();
                } else {
                    prefs.edit().putString(RescueMeConstants.LOGGED_IN_USER_ID, prefs.getString(RescueMeConstants.FB_USER_ID, String.valueOf(1))).apply();
                    loadAuthenticatedActivity();
                }
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                Toast.makeText(context, RescueMeConstants.FB_NOT_ACCEPT_PERMISSIONS, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onThinking() {

            }

            @Override
            public void onException(Throwable throwable) {
                Toast.makeText(context, RescueMeConstants.FB_EXCEPTION_LOGIN, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onFail(String s) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void getProfileInfo() {
        onProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                Toast.makeText(context, RescueMeConstants.GOT_PROFILE_DATA, Toast.LENGTH_SHORT).show();
                fbUserProfile = profile;
                new DownloadImageTask().execute(fbUserProfile.getPicture());
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

        simpleFacebook.getProfile(properties, onProfileListener);
        getActivity().setProgressBarIndeterminateVisibility(true);
        Toast.makeText(context, RescueMeConstants.DOWNLOADING_PROFILE, Toast.LENGTH_SHORT).show();
    }


    private void loadProfileEditFragment() {
        Bundle profileData = new Bundle();
        profileData.putString(RescueMeConstants.COLUMN_NAME, fbUserProfile.getName());
        profileData.putString(RescueMeConstants.COLUMN_EMAIL, fbUserProfile.getEmail());
        profileData.putByteArray(RescueMeConstants.COLUMN_PROFILE_PIC, blob);

        ((RescueMe) getActivity()).loadFragment(RescueMeConstants.UPDATE_PROFILE, profileData);
    }

    private class LogIn extends AsyncTask<View, Void, String> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(View... params) {
            if (isEmailValid()) {
                if (!isPasswordEmpty()) {
                    RescueMeUserModel user = new RescueMeUserModel(email.getText().toString()
                            , password.getText().toString());
                    RescueMeDBFactory dbFactory = RescueMeDBFactory.getInstance(context);
                    dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
                    int userId = dbFactory.loginUser(user);
                    if (userId > 0) {
                        prefs.edit().putString(RescueMeConstants.LOGGED_IN_USER_ID, String.valueOf(userId)).apply();
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.LOGIN_FAIL;
                    }
                } else {
                    return RescueMeConstants.PASSWORD_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                Toast.makeText(context, RescueMeConstants.LOGIN_SUCCESS, Toast
                        .LENGTH_SHORT).show();
                prefs.edit().putBoolean(RescueMeConstants.LOGIN, true).apply();
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask() {
            //empty constructor
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, RescueMeConstants.DOWNLOADING_PROFILE_PIC, Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            getActivity().setProgressBarIndeterminateVisibility(false);
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
