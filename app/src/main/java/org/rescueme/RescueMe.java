package org.rescueme;

import android.app.ActionBar;
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
    private RescueMeAppExtension appExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appExtension = (RescueMeAppExtension) getApplicationContext();
        if (appExtension.isLoggedIn()) {
            RescueMeUtilClass.writeToLog("Already Logged in.. Loading Authenticated Activity");
            loadAuthenticatedActivity();
        } else {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            prefs = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME, MODE_PRIVATE);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setTitle(RescueMeConstants.LOGIN);
            }
            setContentView(R.layout.activity_rescue_me);
            activity = this;
            context = activity.getBaseContext();
            simpleFacebook = SimpleFacebook.getInstance(this);
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addScope(Plus.SCOPE_PLUS_PROFILE)
                    .addApi(Plus.API)
                    .build();
            fbLoginBtn = (ImageButton) findViewById(R.id.fbLoginBtn);
            gPlusLoginBtn = (ImageButton) findViewById(R.id.gPlusLogin);
            setFbLoginListener();
            dbFactory = RescueMeDBFactory.getInstance(context);
            dbFactory.setTableName(RescueMeConstants.USER_TABLE);

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
        RescueMeUtilClass.writeToLog("Login Successful!!");
        appExtension.setLoggedIn(true);
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
                RescueMeUtilClass.toastAndLog(context, "Connected to Facebook Successfully!!");
                if (prefs.getBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, true)) {
                    setButtonsClickable(false);
                    getFBProfileInfo();
                } else {
                    loadAuthenticatedActivity();
                }
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                RescueMeUtilClass.toastAndLog(context, "Permissions were not accepted!!");
                setButtonsClickable(true);
            }

            @Override
            public void onThinking() {
                setButtonsClickable(true);
            }

            @Override
            public void onException(Throwable throwable) {
                RescueMeUtilClass.toastAndLog(context, "Exception while connecting to Facebook!!");
                setButtonsClickable(true);
            }

            @Override
            public void onFail(String s) {
                RescueMeUtilClass.toastAndLog(context, "Failed to Connect to Facebook!!");
                setButtonsClickable(true);
            }
        };
    }

    private void getFBProfileInfo() {
        OnProfileListener fbProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                RescueMeUtilClass.toastAndLog(context, "Downloaded the Profile!!");
                userProfile = new RescueMeUserModel();
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
        RescueMeUtilClass.toastAndLog(context, "Downloading your Profile...");
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
                RescueMeUtilClass.toastAndLog(context, "Profile picture is necessary!!");
                startCropActivity();
            }
        } else if (requestCode == RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                RescueMeUtilClass.writeToLog("Google plus issue Resolved!!");
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
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(googleApiClient);
            if (currentPerson != null) {
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(googleApiClient);
                userProfile = new RescueMeUserModel();
                userProfile.setName(personName);
                userProfile.setEmail(email);

                RescueMeUtilClass.toastAndLog(context, "Downloaded your Google Profile!!");

                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 400;

                new downloadImageTask().execute(personPhotoUrl);

            } else {
                RescueMeUtilClass.toastAndLog(context, "Google Profile info is null !!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        RescueMeUtilClass.toastAndLog(context, "Connected to Google!!");
        if (prefs.getBoolean(RescueMeConstants.G_PLUS_FIRST_TIME_LOGIN, true)) {
            getUserGooglePlusProfile();
        } else {
            loadAuthenticatedActivity();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        RescueMeUtilClass.toastAndLog(context, "Google Connection suspended!!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            RescueMeUtilClass.writeToLog("Google - Issue has a Resolution!!");
            try {
                result.startResolutionForResult(activity, RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                RescueMeUtilClass.writeToLog("Google - Exception caught!! Reconnecting...");
                googleApiClient.connect();
            }
        } else {
            RescueMeUtilClass.writeToLog("Google - Issue has no resolution!!");
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
        setProgressBarIndeterminateVisibility(!clickable);
    }

    private class downloadImageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            RescueMeUtilClass.toastAndLog(context, "Downloading your Profile Picture...");
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
            if (register) {
                RescueMeUtilClass.toastAndLog(context, "Registering you now...");
            } else {
                RescueMeUtilClass.toastAndLog(context, "Updating your Profile...");
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
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                prefs.edit().putBoolean(RescueMeConstants.LOGIN, true).apply();
                if (register) {
                    RescueMeUtilClass.toastAndLog(context, "Registration Successful!! ");
                } else {
                    RescueMeUtilClass.toastAndLog(context, "Profile updated!!");
                }
                if (whichSocial.equalsIgnoreCase(RescueMeConstants.FACEBOOK)) {
                    prefs.edit().putBoolean(RescueMeConstants.FB_FIRST_TIME_LOGIN, false).apply();
                } else {
                    prefs.edit().putBoolean(RescueMeConstants.G_PLUS_FIRST_TIME_LOGIN, false).apply();
                }
                loadAuthenticatedActivity();
            } else {
                RescueMeUtilClass.toastAndLog(context, result);
                setButtonsClickable(true);
            }
        }
    }
}
