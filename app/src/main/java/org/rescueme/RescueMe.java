package org.rescueme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import com.sromku.simple.fb.SimpleFacebook;


public class RescueMe extends Activity {


    SharedPreferences pref;
    boolean isLoggedIn;
    private String fragmentTag = RescueMeConstants.RESCUE_ME;
    private SimpleFacebook simpleFacebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        simpleFacebook = SimpleFacebook.getInstance(this);
        setContentView(R.layout.activity_rescue_me);
        pref = getSharedPreferences(RescueMeConstants.PREFERENCE_NAME,MODE_PRIVATE);
        isLoggedIn = pref.getBoolean(RescueMeConstants.LOGIN,false);
        if(isLoggedIn || simpleFacebook.isLogin()){
            Intent intent = new Intent(this,RescueMeTabViewer.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }else {
            loadFragment(RescueMeConstants.LOGIN);
        }
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
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new RescueMeRegister())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        simpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    public void onBackPressed() {
        if(fragmentTag.equalsIgnoreCase(RescueMeConstants.REGISTER)){
            loadFragment(RescueMeConstants.LOGIN);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        simpleFacebook.onActivityResult(this,requestCode,resultCode,data);
    }
}
