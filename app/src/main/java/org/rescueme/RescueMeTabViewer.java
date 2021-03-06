package org.rescueme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;


public class RescueMeTabViewer extends Activity implements ActionBar.TabListener, GoogleApiClient.OnConnectionFailedListener
        , GoogleApiClient.ConnectionCallbacks {

    private ActionBar actionBar;
    private ViewPager viewPager;
    private Context context;
    private SimpleFacebook simpleFacebook;
    private GoogleApiClient googleApiClient;
    private OnLogoutListener fbLogoutListener;
    private Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_rescue_me_tab_viewer);

        activity = this;
        context = getBaseContext();
        simpleFacebook = SimpleFacebook.getInstance(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(RescueMeConstants.RESCUE_ME_MAIN);
        }

        RescueMeTabAdapter sectionsPagerAdapter = new RescueMeTabAdapter(getFragmentManager());
        setFbLogoutListener();
        googleApiClient.connect();

        int selectTab = getIntent().getIntExtra(RescueMeConstants.SELECT_TAG, 0);
        viewPager.setAdapter(sectionsPagerAdapter);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        for (String tab_name : RescueMeConstants.TABS) {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }


        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        viewPager.setCurrentItem(selectTab);

        checkGPSProvidersStatus();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkGPSProvidersStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rescue_me_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
        }
        return super.onOptionsItemSelected(item);
    }


    private void logoutUser() {
        boolean fbLogin = simpleFacebook.isLogin();
        boolean gPlusLogin = googleApiClient.isConnected();
        RescueMeAppExtension appExtension = (RescueMeAppExtension) getApplicationContext();
        appExtension.setLoggedIn(false);
        if (fbLogin && gPlusLogin) {
            simpleFacebook.logout(fbLogoutListener);
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            googleApiClient.disconnect();
        } else if (fbLogin) {
            simpleFacebook.logout(fbLogoutListener);
        } else if (gPlusLogin) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            googleApiClient.disconnect();
        }
        RescueMeUtilClass.toastAndLog(context, "Logged out successfully!!");
        startMainActivity();
    }


    private void startMainActivity() {
        Intent intent = new Intent(this, RescueMe.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == RescueMeConstants.GOOGLE_PLUS_LOGIN_RESOLUTION) {
            Fragment settingsFragment = getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
            if (resultCode == Activity.RESULT_OK) {
                googleApiClient.connect();
            }
            if (viewPager.getCurrentItem() == 2 && settingsFragment != null) {
                settingsFragment.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == RescueMeConstants.GPS_LOCATION_SETTINGS) {
            new android.os.Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    RescueMeUtilClass.writeToLog("Sending alerts again!!");
                    RescueMeUtilClass.sendEmergencyAlerts(activity);
                }
            }, RescueMeConstants.GPS_AFTER_SETTINGS_SLEEP);

        }
    }

    private void setFbLogoutListener() {
        fbLogoutListener = new OnLogoutListener() {
            @Override
            public void onLogout() {
                RescueMeUtilClass.writeToLog("Logged out from FB");
            }

            @Override
            public void onThinking() {

            }

            @Override
            public void onException(Throwable throwable) {

            }

            @Override
            public void onFail(String s) {

            }
        };
    }

    @Override
    public void onConnected(Bundle bundle) {
        RescueMeUtilClass.writeToLog("Connected to Google");
    }

    @Override
    public void onConnectionSuspended(int i) {
        RescueMeUtilClass.writeToLog("Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        RescueMeUtilClass.writeToLog("Connection Failed");
    }

    private void checkGPSProvidersStatus() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            RescueMeUtilClass.showSettingsAlert(this, LocationManager.NETWORK_PROVIDER);
        }
    }
}
