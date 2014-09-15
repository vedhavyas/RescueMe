package org.rescueme;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;


public class RescueMeUtilActivity extends Activity {

    private String fragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_rescue_me_util);
        fragmentTag = getIntent().getStringExtra(RescueMeConstants.FRAGMENT_TAG);
        loadFragment();
    }

    public void loadFragment() {
        if (fragmentTag.equalsIgnoreCase(RescueMeConstants.NEW_EMERGENCY_CONTACT)) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new RescueMeAddEmergencyContact())
                    .commit();
        } else if (fragmentTag.equalsIgnoreCase(RescueMeConstants.UPDATE_EMERGENCY_CONTACT)) {
            String id = getIntent().getStringExtra(RescueMeConstants.COLUMN_ID);
            Bundle data = new Bundle();
            data.putString(RescueMeConstants.COLUMN_ID, id);
            RescueMeUpdateEmergencyContact updateContactFrag = new RescueMeUpdateEmergencyContact();
            updateContactFrag.setArguments(data);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, updateContactFrag)
                    .commit();
        } else if (fragmentTag.equalsIgnoreCase(RescueMeConstants.UPDATE_PROFILE)) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new RescueMeUpdateProfile())
                    .commit();
        }
    }
}
