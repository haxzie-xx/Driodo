package me.haxzie.driodo.Home.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import me.haxzie.driodo.Home.Fragments.DashBoardFragment;
import me.haxzie.driodo.Home.Fragments.HomeFragment;
import me.haxzie.driodo.Home.Fragments.ProfileFragment;

/**
 * Created by User on 16-Jan-18.
 */

public class HomeFragmentsAdapter extends FragmentPagerAdapter {

    public HomeFragmentsAdapter (FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return HomeFragment.newInstance();
            case 1:
                return DashBoardFragment.newInstance();
            case 2:
                return ProfileFragment.newInstance();
            default:
                //Don't fuck things up!
                return HomeFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
