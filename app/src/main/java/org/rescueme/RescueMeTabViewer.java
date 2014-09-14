package org.rescueme;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;


public class RescueMeTabViewer extends Activity implements ActionBar.TabListener {

    private ActionBar actionBar;
    private ViewPager viewPager;
    private SharedPreferences prefs;
    private Context context;
    private SimpleFacebook simpleFacebook;
    private OnLogoutListener fbLogoutListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleFacebook = SimpleFacebook.getInstance(this);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_rescue_me_tab_viewer);


        prefs = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        actionBar.setTitle(RescueMeConstants.RESCUE_ME_MAIN);

        context = getBaseContext();
        RescueMeTabAdapter sectionsPagerAdapter = new RescueMeTabAdapter(getFragmentManager());
        setFbLogoutListener();
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rescue_me_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            Toast.makeText(context, RescueMeConstants.LOGOUT_SUCCESS, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    private void logoutUser() {
        if (prefs.getBoolean(RescueMeConstants.LOGIN, false) && !simpleFacebook.isLogin()) {
            prefs.edit().putBoolean(RescueMeConstants.LOGIN, false).apply();
        } else if (prefs.getBoolean(RescueMeConstants.LOGIN, false) && simpleFacebook.isLogin()) {
            prefs.edit().putBoolean(RescueMeConstants.LOGIN, false).apply();
            simpleFacebook.logout(fbLogoutListener);
        } else if (!prefs.getBoolean(RescueMeConstants.LOGIN, false) && simpleFacebook.isLogin()) {
            simpleFacebook.logout(fbLogoutListener);
        }
        Toast.makeText(context, RescueMeConstants.LOGOUT_SUCCESS, Toast.LENGTH_SHORT).show();
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
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
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
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(this, requestCode, resultCode, data);
    }

    private void setFbLogoutListener() {
        fbLogoutListener = new OnLogoutListener() {
            @Override
            public void onLogout() {

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
}
