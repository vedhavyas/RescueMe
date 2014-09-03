package org.rescueme;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;



public class RescueMe extends Activity {


    SharedPreferences pref;
    boolean isLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescue_me);
        pref = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME,MODE_PRIVATE);
        isLoggedIn = pref.getBoolean(RescueMeConstants.LOGIN,false);
        if(isLoggedIn){
            Intent intent = new Intent(this,RescueMeMainView.class);
            startActivity(intent);
        }else {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new RescueMeLogin())
                    .commit();

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
