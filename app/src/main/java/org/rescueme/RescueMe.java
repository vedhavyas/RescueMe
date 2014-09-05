package org.rescueme;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;


public class RescueMe extends Activity {


    SharedPreferences pref;
    boolean isLoggedIn;
    private String fragmentTag = RescueMeConstants.RESCUE_ME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_rescue_me);
        pref = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME,MODE_PRIVATE);
        isLoggedIn = pref.getBoolean(RescueMeConstants.LOGIN,false);
        if(isLoggedIn){
            Intent intent = new Intent(this,RescueMeMainView.class);
            startActivity(intent);
        }else {
            loadFragment(RescueMeConstants.LOGIN);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rescue_me, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTitle(String title){
        getActionBar().setTitle(title);
    }

    public void loadFragment(String tag){
        if(tag.equalsIgnoreCase(RescueMeConstants.LOGIN)){
            fragmentTag = tag;
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new RescueMeLogin())
                    .commit();
        }else if(tag.equalsIgnoreCase(RescueMeConstants.REGISTER)){
            fragmentTag = tag;
            getFragmentManager().beginTransaction().replace(R.id.container, new RescueMeRegister())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(fragmentTag.equalsIgnoreCase(RescueMeConstants.REGISTER)){
            loadFragment(RescueMeConstants.LOGIN);
        }else{
            super.onBackPressed();
        }
    }
}
