package com.robot.asus.Sporden.WorkoutCategory;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private int[] mResources;

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments ) {
        super(fm);
        this.fragments = fragments;
    }

    @Override

    public Fragment getItem(int position)
    {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}

