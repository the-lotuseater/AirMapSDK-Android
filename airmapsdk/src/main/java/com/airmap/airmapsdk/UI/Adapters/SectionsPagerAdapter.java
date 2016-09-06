package com.airmap.airmapsdk.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.nakama.arraypageradapter.ArrayFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/22/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class SectionsPagerAdapter extends ArrayFragmentPagerAdapter<Fragment> {
    public SectionsPagerAdapter(FragmentManager fm, List<Fragment> items) {
        super(fm, items);
    }

    @Override
    public Fragment getFragment(Fragment item, int position) {
        return item;
    }

    public ArrayList<Fragment> getItems() {
        ArrayList<Fragment> list = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            list.add(getItem(i));
        }
        return list;
    }
}