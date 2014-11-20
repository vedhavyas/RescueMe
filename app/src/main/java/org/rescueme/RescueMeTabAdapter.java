package org.rescueme;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Authored by Vedhavyas Singareddi on 11-09-2014.
 */
public class RescueMeTabAdapter extends FragmentPagerAdapter {

    public RescueMeTabAdapter(FragmentManager fm) {

        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new RescueMePanic();
            case 1:
                return new RescueMeContacts();
            case 2:
                return new RescueMeSettings();
        }

        return null;
    }

    @Override
    public int getCount() {
        return RescueMeConstants.NO_OF_TABS;
    }
}
