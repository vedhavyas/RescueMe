package org.rescueme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.camera.CropImageIntentBuilder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
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


public class RescueMe extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private SharedPreferences prefs;
    private boolean isLoggedIn;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private RescueMeUserModel userProfile;
    private Context context;
    private File croppedImageFile;
    private String tempImagePath;
    private byte[] blob;
    private Bitmap bitmap;
    private RescueMeDBFactory dbFactory;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private String whichSocial;
    private ImageButton fbLoginBtn, gPlusLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        prefs = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME, MODE_PRIVATE);
        isLoggedIn = prefs.getBoolean(RescueMeConstants.LOGIN, false);
        if (isLoggedIn) {
            loadAuthenticatedActivity();
        } else {
            getActionBar().setTitle(RescueMeConstants.LOGIN);
            setContentView(R.layout.activity_rescue_me);
            activity = this;
            context = activity.getBaseContext();
            simpleFacebook = SimpleFacebook.getInstance(this);
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addApi(Plus.API)
                    .build();
            fbLoginBtn = (ImageButton) findViewById(R.id.fbLoginBtn);
            gPlusLoginBtn = (ImageButton) findViewById(R.id.gPlusLogin);
            setFbLoginListener();
            dbFactory = RescueMeDBFactory.getInstance(context);
            dbFactory.setTable_name(RescueMeConstants.USER_TABLE);

            //setup listeners
            fbLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    whichSocial = RescueMeConstants.FACEBOOK;
                    setButtonsClickable(false);
                    simpleFacebook.login(fbLoginListener);
                }
            });

            gPlusLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whichSocial = RescueMeConstants.GOOGLE_PLUS;
                    setButtonsClickable(false);
                    googleApiClient.connect();
                }
            });

        }
    }

    private void loadAuthenticatedActivity() {
        Intent intent = new Intent(this, RescueMeTabViewer.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void setFbLoginListener() {
        fbLoginListener = new OnLoginListener() {
            @Override
            public void onLogin() {
                Toast.makeText(context, RescueMeConstants.FB_LOGIN_SUCCESS, Toast.LENGTH_SHORT).show();
                if (prefs.getBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, true)) {
                    getFBProfileInfo();
                } else {
                    loadAuthenticatedActivity();
                }
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                Toast.makeText(context, RescueMeConstants.FB_NOT_ACCEPT_PERMISSIONS, Toast.LENGTH_SHORT).show();
                setButtonsClickable(true);
            }

            @Override
            public void onThinking() {
                setButtonsClickable(true);
            }

            @Override
            public void onException(Throwable throwable) {
                Toast.makeText(context, RescueMeConstants.FB_EXCEPTION_LOGIN, Toast.LENGTH_SHORT).show();
                setButtonsClickable(true);
            }

            @Override
            public void onFail(String s) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                setButtonsClickable(true);
            }
        };
    }

    private void getFBProfileInfo() {
        OnProfileListener fbProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                Toast.makeText(context, RescueMeConstants.GOT_PROFILE_DATA, Toast.LENGTH_SHORT).show();
                userProfile.setName(profile.getName());
                userProfile.setEmail(profile.getEmail());
                new downloadImageTask().execute(profile.getPicture());
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

        simpleFacebook.getProfile(properties, fbProfileListener);
        setProgressBarIndeterminateVisibility(true);
        Toast.makeText(context, RescueMeConstants.DOWNLOADING_PROFILE, Toast.LENGTH_SHORT).show();
    }

    private void startCropActivity() {
        croppedImageFile = new File(context.getFilesDir(), "cropFile.jpg");
        Uri croppedImage = Uri.fromFile(croppedImageFile);
        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(400,
                400, croppedImage);
        File outDir = context.getCacheDir().getAbsoluteFile();
        tempImagePath = outDir + "/temp_image";
        try {
            FileOutputStream fos = new FileOutputStream(tempImagePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            cropImage.setSourceImage(Uri.fromFile(new File(tempImagePath)));
            startActivityForResult(cropImage.getIntent(context), RescueMeConstants.CROP_IMAGE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == RescueMeConstants.CROP_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                blob = RescueMeUtilClass.getBlob(BitmapFactory.decodeFile(croppedImageFile.getAbsolutePath()));
                if (tempImagePath != null) {
                    File tempFile = new File(tempImagePath);
                    if (tempFile.delete()) {
                        tempImagePath = null;
                    }
                }
                registerUser();
            } else {
                Toast.makeText(context, RescueMeConstants.CHOOSE_THE_PROFILE_PIC, Toast.LENGTH_SHORT).show();
                startCropActivity();
            }
        } else if (requestCode == RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(RescueMeConstants.LOG_TAG, "Result code success for Google Login");
                googleApiClient.connect();
            } else {
                setButtonsClickable(true);
            }
        }
    }

    private void registerUser() {
        userProfile.setProfilePic(blob);
        if (prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, -1) > 0) {
            new registerUserTask(false).execute(userProfile);
        } else {
            new registerUserTask(true).execute(userProfile);
        }

    }

    private void getUserGooglePlusProfile() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(googleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String personGooglePlusProfile = currentPerson.getUrl();
                String email = Plus.AccountApi.getAccountName(googleApiClient);
                userProfile.setName(personName);
                userProfile.setEmail(email);
                Log.i(RescueMeConstants.LOG_TAG, "Name: " + personName + ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);


                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 400;
                setProgressBarIndeterminateVisibility(true);
                new downloadImageTask().execute(personPhotoUrl);

            } else {
                Toast.makeText(context,
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(context, RescueMeConstants.GOOGLE_CONNECTION_SUCCESS, Toast.LENGTH_SHORT).show();
        Log.i(RescueMeConstants.LOG_TAG, RescueMeConstants.GOOGLE_CONNECTION_SUCCESS);
        if (prefs.getBoolean(RescueMeConstants.G_PLUS_FIRST_TIME_LOGIN, true)) {
            getUserGooglePlusProfile();
        } else {
            loadAuthenticatedActivity();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "Connection to Google is Suspended!!", Toast.LENGTH_SHORT).show();
        Log.i(RescueMeConstants.LOG_TAG, "Connection to Google is Suspended!!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            Log.i(RescueMeConstants.LOG_TAG, "Connection has Resolution");
            try {
                result.startResolutionForResult(activity, RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.i(RescueMeConstants.LOG_TAG, "Exception caught!! Reconnecting to client");
                googleApiClient.connect();
            }
        } else {
            Log.i(RescueMeConstants.LOG_TAG, "No resolution!! Showing error Dialog");
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

    private void setButtonsClickable(Boolean clickable) {
        fbLoginBtn.setClickable(clickable);
        gPlusLoginBtn.setClickable(clickable);
    }

    private class downloadImageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, RescueMeConstants.DOWNLOADING_PROFILE_PIC, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... urls) {
            String url = urls[0];
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(RescueMeConstants.LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setProgressBarIndeterminateVisibility(false);
            startCropActivity();
        }
    }

    private class registerUserTask extends AsyncTask<RescueMeUserModel, Void, String> {

        private boolean register;

        public registerUserTask(boolean register) {
            this.register = register;
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            if (register) {
                Toast.makeText(context, RescueMeConstants.REGISTERING_USER, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, RescueMeConstants.UPDATING_USER_PROFILE, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(RescueMeUserModel... rescueMeUserModels) {
            RescueMeUserModel user = rescueMeUserModels[0];
            if (register) {
                int id = (int) dbFactory.registerUser(user);
                if (id > 0) {
                    prefs.edit().putInt(RescueMeConstants.LOGGED_IN_USER_ID, id).apply();
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.REGISTRATION_FAILED;
                }
            } else {
                int id = prefs.getInt(RescueMeConstants.LOGGED_IN_USER_ID, 1);
                user.setId(String.valueOf(id));
                int returnVal = dbFactory.updateUserData(user);
                if (returnVal > 0) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.UPDATE_PROFILE_FAIL;
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                prefs.edit().putBoolean(RescueMeConstants.LOGIN, true).apply();
                if (register) {
                    Toast.makeText(context, RescueMeConstants.REGISTRATION_SUCCESS, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, RescueMeConstants.UPDATE_PROFILE_SUCCESS, Toast.LENGTH_SHORT).show();
                }
                if (whichSocial.equalsIgnoreCase(RescueMeConstants.FACEBOOK)) {
                    prefs.edit().putBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, false).apply();
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.G_PLUS_FIRST_TIME_LOGIN, false).apply();
                }
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                setButtonsClickable(true);
            }
        }
    }
}
