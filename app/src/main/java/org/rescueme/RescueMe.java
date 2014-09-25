package org.rescueme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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


public class RescueMe extends Activity {


    private SharedPreferences prefs;
    private boolean isLoggedIn;
    private SimpleFacebook simpleFacebook;
    private OnLoginListener fbLoginListener;
    private Profile fbProfile;
    private Context context;
    private File croppedImageFile;
    private String tempImagePath;
    private byte[] blob;
    private Bitmap bitmap;
    private RescueMeDBFactory dbFactory;

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
            Activity activity = this;
            context = activity.getBaseContext();
            simpleFacebook = SimpleFacebook.getInstance(this);
            ImageButton fbLoginBtn = (ImageButton) findViewById(R.id.fbLoginBtn);
            ImageButton gPlusLoginBtn = (ImageButton) findViewById(R.id.gPlusLogin);
            setFbLoginListener();
            dbFactory = RescueMeDBFactory.getInstance(context);
            dbFactory.setTable_name(RescueMeConstants.USER_TABLE);
            fbLoginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    simpleFacebook.login(fbLoginListener);
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
                    getProfileInfo();
                } else {
                    prefs.edit().putString(RescueMeConstants.LOGGED_IN_USER_ID, prefs.getString(RescueMeConstants.FB_USER_ID, String.valueOf(1))).apply();
                    loadAuthenticatedActivity();
                }
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                Toast.makeText(context, RescueMeConstants.FB_NOT_ACCEPT_PERMISSIONS, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onThinking() {

            }

            @Override
            public void onException(Throwable throwable) {
                Toast.makeText(context, RescueMeConstants.FB_EXCEPTION_LOGIN, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String s) {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void getProfileInfo() {
        OnProfileListener fbProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                Toast.makeText(context, RescueMeConstants.GOT_PROFILE_DATA, Toast.LENGTH_SHORT).show();
                fbProfile = profile;
                new downloadImageTask().execute(fbProfile.getPicture());
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
        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(450,
                450, croppedImage);
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
                registerUser(RescueMeConstants.FACEBOOK);
            } else {
                Toast.makeText(context, RescueMeConstants.CHOOSE_THE_PROFILE_PIC, Toast.LENGTH_SHORT).show();
                startCropActivity();
            }
        }
    }

    private void registerUser(String tag) {
        RescueMeUserModel user = new RescueMeUserModel();
        if (tag.equalsIgnoreCase(RescueMeConstants.FACEBOOK)) {
            user.setName(fbProfile.getName());
            user.setEmail(fbProfile.getEmail());
            user.setProfilePic(blob);
        }

        new registerUserTask().execute(user);

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
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            Toast.makeText(context, RescueMeConstants.REGISTERING_USER, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(RescueMeUserModel... rescueMeUserModels) {
            RescueMeUserModel user = rescueMeUserModels[0];
            int id = (int) dbFactory.registerUser(user);
            if (id > 0) {
                prefs.edit().putString(RescueMeConstants.LOGGED_IN_USER_ID, String.valueOf(id)).apply();
                return RescueMeConstants.SUCCESS;
            } else {
                return RescueMeConstants.REGISTRATION_FAILED;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            if (result.equalsIgnoreCase(RescueMeConstants.SUCCESS)) {
                prefs.edit().putBoolean(RescueMeConstants.LOGIN, true).apply();
                Toast.makeText(context, RescueMeConstants.REGISTRATION_SUCCESS, Toast.LENGTH_SHORT).show();
                loadAuthenticatedActivity();
            } else {
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
