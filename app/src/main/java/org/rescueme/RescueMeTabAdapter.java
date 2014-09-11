package org.rescueme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by Vedavyas Singareddy on 11-09-2014.
 */
public class RescueMeTabAdapter extends FragmentPagerAdapter {

    public RescueMeTabAdapter(FragmentManager fm) {

        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        //return PlaceholderFragment.newInstance(position + 1);
        switch(position){
            case 0:
                return new RescueMePanic();
            case 1:
                return new RescueMeProfile();
            case 2:
                return new RescueMeCircles();
            case 3:
                return new RescueMeSettings();
        }

        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return RescueMeConstants.NO_OF_TABS;
    }
}
